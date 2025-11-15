package ch.idsia.agents.astar;

/**
 * MarioAI の物理を「簡略化＋4px 量子化」した Simulator（B〜C 中間くらい）。
 * - 内部座標系はピクセル
 * - タイルサイズ 16px
 * - unitPixels ごとの格子座標上で動く（デフォルト 4px）
 * - ジャンプ軌道は「重力付きの離散シミュレーション」
 * - 敵踏み（stomp）も考慮
 *
 * NOTE:
 *  - wall/gap/enemyDistance などのセンサー値は「とりあえず元の状態をコピー」している。
 *    本気で詰めるなら LevelMap / EnemyMap から再計算するヘルパを別途用意すると良い。
 */
public class Simulator {

    // ======== 物理＆量子化パラメータ ========
    private static final int TILE_SIZE = 16;   // MarioAI のタイルサイズ
    private static final int MAX_ROW = 18;     // だいたい受容野の下端（穴判定用の適当な上限）

    private final LevelMap level;
    private final EnemyMap enemies;

    /** 量子化単位（px）。デフォルト 4px。ここを変えれば簡単に調整できる。 */
    private final int unitPixels;

    /** 重力加速度（1 ステップあたりの vy 増分） */
    private final int gravity;

    /** ジャンプ初速 */
    private final int shortJumpVy0;
    private final int runJumpVy0;

    /** 横方向スピード（1 アクションあたりの移動量 px） */
    private final int walkVx;
    private final int runVx;

    public Simulator(LevelMap level, EnemyMap enemies) {
        this(level, enemies, 4);
    }

    public Simulator(LevelMap level, EnemyMap enemies, int unitPixels) {
        this.level = level;
        this.enemies = enemies;
        this.unitPixels = unitPixels;

        // ここを変えればジャンプ特性を簡単にチューニングできる
        this.gravity      = unitPixels;       // 例えば 4px/step^2
        this.shortJumpVy0 = -5 * unitPixels;  // -20px
        this.runJumpVy0   = -6 * unitPixels;  // -24px

        // ★修正ポイント★
        // 「1 アクション = おおよそ 1 タイル分進む」ようにする
        // unitPixels=4 のとき:
        //   walkVx = 16px (1 タイル)
        //   runVx  = 24px (1.5 タイルくらい)
        this.walkVx = 4 * unitPixels;   // = TILE_SIZE
        this.runVx  = 6 * unitPixels;   // ≒ 1.5 タイル
    }

    // =========================================
    // 量子化ヘルパ
    // =========================================
    private int quantize(int px) {
        if (unitPixels <= 1) return px;
        if (px >= 0) {
            return (px / unitPixels) * unitPixels;
        } else {
            // 負の値も綺麗に丸める
            return -((-px + unitPixels - 1) / unitPixels) * unitPixels;
        }
    }

    // =========================================
    // 公開 API：1 ステップシミュレーション
    // =========================================

    /**
     * 1 手（AStarPlanner の ACT_*）をシミュレートして次の MarioState を返す。
     * null を返した場合は「その行動は壁 or 落下で失敗」という意味。
     */
    public MarioState simulate(MarioState s, int action) {

        // タイル座標 → ピクセル座標（左上基準）
        int px = s.col * TILE_SIZE;
        int py = s.row * TILE_SIZE;
        px = quantize(px);
        py = quantize(py);

        switch (action) {

            case AStarPlanner.ACT_RIGHT:
                return simulateWalk(s, px, py, walkVx);

            case AStarPlanner.ACT_LEFT:
                return simulateWalk(s, px, py, -walkVx);

            case AStarPlanner.ACT_RUN_RIGHT:
                return simulateWalk(s, px, py, runVx);

            case AStarPlanner.ACT_JUMP:
                return simulateJump(s, px, py, walkVx, shortJumpVy0);

            case AStarPlanner.ACT_JUMP_RUN:
                return simulateJump(s, px, py, runVx, runJumpVy0);

            default:
                // 何もしない行動などは「そのまま（ただし落下だけ適用）」でもよい
                int afterFallY = applyFall(px, py);
                if (afterFallY == Integer.MIN_VALUE) return null;
                return makeNextStateFromPixels(s, px, afterFallY, true);
        }
    }

    // =========================================
    // 歩き（左右移動）+ 落下
    // =========================================

    private MarioState simulateWalk(MarioState s, int px, int py, int vx) {
        int nextPx = quantize(px + vx);
        int nextPy = py;

        // 横方向の壁チェック
        if (collides(nextPx, nextPy)) {
            return null;
        }

        // 重力で足場まで落とす
        int fallY = applyFall(nextPx, nextPy);
        if (fallY == Integer.MIN_VALUE) {
            // 穴に落ちた
            return null;
        }

        return makeNextStateFromPixels(s, nextPx, fallY, true);
    }

    // =========================================
    // ジャンプ（敵踏み込み込み）
    // =========================================

    private MarioState simulateJump(MarioState s, int px, int py, int vx, int vy0) {
        int x = px;
        int y = py;
        int vy = vy0;

        // 安全のため上限ステップ数を決めておく（長すぎるループ防止）
        final int MAX_FRAMES = 32;

        for (int t = 0; t < MAX_FRAMES; t++) {

            int nextX = quantize(x + vx);
            int nextY = quantize(y + vy);

            // 上方向に移動しているときの天井衝突
            if (vy < 0 && collides(nextX, nextY)) {
                // 頭を打ったとみなしてジャンプ終了 → その場から落下に移行
                int landingY = applyFall(x, y);
                if (landingY == Integer.MIN_VALUE) return null;
                return makeNextStateFromPixels(s, x, landingY, true);
            }

            x = nextX;
            y = nextY;

            // 横・斜めに壁にめり込んだら失敗
            if (collides(x, y)) {
                return null;
            }

            // 穴に落ちた
            if (y / TILE_SIZE > MAX_ROW + 1) {
                return null;
            }

            // ======== stomp チェック =========
            // 「下降中（vy > 0）」かつ「足元タイルに敵がいる」なら stomp とする
         // ======== stomp チェック（テスト仕様に合わせた厳密なもの） ========
            if (vy > 0 && enemies != null) {

                // 現在位置と次フレーム位置の間に敵マスがあるか確認
                int curCol = x / TILE_SIZE;
                int nextCol = nextX / TILE_SIZE;

                // x方向に1タイルずつチェック（飛び越え対策）
                int step = (nextCol > curCol ? 1 : -1);

                for (int c = curCol; c != nextCol + step; c += step) {

                    int stompRow = (y + TILE_SIZE - 1) / TILE_SIZE;

                    if (enemies.hasEnemy(stompRow, c)) {
                        enemies.removeEnemy(stompRow, c);
                        int landingY = stompRow * TILE_SIZE;
                        return makeNextStateFromPixels(s, c * TILE_SIZE, landingY, true);
                    }
                }
            }

            // ======== 地面に着いたら着地 ========
            if (isOnGround(x, y) && vy >= 0) {
                int landingY = alignToGround(x, y);
                return makeNextStateFromPixels(s, x, landingY, true);
            }

            // 次フレームへ
            vy += gravity;
        }

        // MAX_FRAMES まで行っても着地しなかったら、とりあえずその場から落下させる
        int finalY = applyFall(x, y);
        if (finalY == Integer.MIN_VALUE) return null;
        return makeNextStateFromPixels(s, x, finalY, true);
    }

    // =========================================
    // 落下処理（真下に unitPixels ずつ落とす）
    // =========================================

    private int applyFall(int px, int startPy) {
        int y = startPy;
        // 適当な上限：画面高さ相当
        for (int i = 0; i < 64; i++) {
            if (isOnGround(px, y)) {
                // 足場上に揃える
                return alignToGround(px, y);
            }
            int nextY = y + unitPixels;
            // 穴の下まで落ちた
            if (nextY / TILE_SIZE > MAX_ROW + 1) {
                return Integer.MIN_VALUE;
            }
            // めり込みチェック
            if (collides(px, nextY)) {
                return Integer.MIN_VALUE;
            }
            y = nextY;
        }
        return Integer.MIN_VALUE;
    }

    // 「地面の上に立っているか？」の判定
    private boolean isOnGround(int px, int py) {
        int footY = py + TILE_SIZE; // 足元周辺
        int col = px / TILE_SIZE;
        int row = footY / TILE_SIZE;
        return level.tileAt(row, col) == LevelMap.TILE_SOLID;
    }

    // 足場タイルの上端に y を揃える
    private int alignToGround(int px, int py) {
        int col = px / TILE_SIZE;
        int row = (py + TILE_SIZE) / TILE_SIZE;
        // row タイルの上端に立たせる
        return row * TILE_SIZE - TILE_SIZE;
    }

    // =========================================
    // 衝突判定（簡略版）
    // =========================================

    /**
     * マリオの「体」が (px, py) 付近にあるとき、ブロックにめり込んでいるかを大雑把に判定。
     * ここでは「縦に 2 タイル分（row, row-1）」「横に 1 タイル分（col）」だけを見る簡易バージョン。
     */
    private boolean collides(int px, int py) {
        int col = px / TILE_SIZE;
        int row = py / TILE_SIZE;

        // 身体本体（row）とその上（row-1）のどちらかが SOLID なら衝突とみなす
        if (level.tileAt(row, col) == LevelMap.TILE_SOLID) return true;
        if (level.tileAt(row - 1, col) == LevelMap.TILE_SOLID) return true;

        return false;
    }

    // =========================================
    // MarioState への変換
    // =========================================

    /**
     * ピクセル座標 (px, py) と onGround フラグから、新しい MarioState を作る。
     * 壁・穴・敵距離などのセンサー値は、とりあえず元の値をコピー。
     * （きちんとやるなら LevelMap / EnemyMap から再計算する関数を後で足す）
     */
    private MarioState makeNextStateFromPixels(MarioState base, int px, int py, boolean recomputeJumpable) {
        int col = px / TILE_SIZE;
        int row = py / TILE_SIZE;

        boolean onGround = isOnGround(px, py);
        boolean ableToJump = recomputeJumpable ? onGround : base.ableToJump;

        return new MarioState(
                row,
                col,
                onGround,
                ableToJump,
                base.wallDistance,
                base.gapDistance,
                base.enemyDistance,
                base.enemyAhead
        );
    }
}