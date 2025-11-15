package ch.idsia.agents.astar;

/**
 * A* 用に抽象化した「マリオの状態」クラス。
 * - 位置（row, col）
 * - 地上/空中フラグ
 * - ジャンプ可能フラグ
 * - センサー情報（壁・穴・敵）
 *
 * AStarNode からの比較・ハッシュ計算に使うため、
 * equals / hashCode / toString を実装している。
 */
public class MarioState {

    // 受容野内でのマリオ位置（タイル座標）
    public int row;
    public int col;

    // マリオの状態
    public boolean onGround;
    public boolean ableToJump;

    // センサー情報
    // - wallDistance : 前方にある最初の壁までの距離（タイル）。なければ -1。
    // - gapDistance  : 前方にある最初の穴までの距離（タイル）。なければ -1。
    // - enemyDistance: 前方にいる最初の敵までの距離（タイル）。なければ -1。
    // - enemyAhead   : 前方に敵がいるかどうか
    public int  wallDistance;
    public int  gapDistance;
    public int  enemyDistance;
    public boolean enemyAhead;

    // ==========================================================
    // コンストラクタ
    // ==========================================================

    public MarioState(int row, int col,
                      boolean onGround, boolean ableToJump,
                      int wallDistance, int gapDistance, int enemyDistance,
                      boolean enemyAhead) {
        this.row = row;
        this.col = col;
        this.onGround = onGround;
        this.ableToJump = ableToJump;
        this.wallDistance = wallDistance;
        this.gapDistance = gapDistance;
        this.enemyDistance = enemyDistance;
        this.enemyAhead = enemyAhead;
    }

    /** コピーコンストラクタ（AStarNode からのディープコピー用） */
    public MarioState(MarioState other) {
        this.row           = other.row;
        this.col           = other.col;
        this.onGround      = other.onGround;
        this.ableToJump    = other.ableToJump;
        this.wallDistance  = other.wallDistance;
        this.gapDistance   = other.gapDistance;
        this.enemyDistance = other.enemyDistance;
        this.enemyAhead    = other.enemyAhead;
    }

    // ==========================================================
    // equals / hashCode / toString
    // ==========================================================

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarioState)) return false;
        MarioState o = (MarioState) obj;

        return this.row == o.row &&
               this.col == o.col &&
               this.onGround == o.onGround &&
               this.ableToJump == o.ableToJump &&
               this.wallDistance == o.wallDistance &&
               this.gapDistance == o.gapDistance &&
               this.enemyDistance == o.enemyDistance &&
               this.enemyAhead == o.enemyAhead;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        result = 31 * result + (onGround ? 1 : 0);
        result = 31 * result + (ableToJump ? 1 : 0);
        result = 31 * result + wallDistance;
        result = 31 * result + gapDistance;
        result = 31 * result + enemyDistance;
        result = 31 * result + (enemyAhead ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State { r=" + row + ", c=" + col +
                ", ground=" + onGround +
                ", jump=" + ableToJump +
                ", wall=" + wallDistance +
                ", gap=" + gapDistance +
                ", enemy=" + enemyDistance +
                ", enemyAhead=" + enemyAhead +
                " }";
    }
}