package ch.idsia.agents.astar;

import ch.idsia.agents.controllers.RuleBaseAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;

/**
 * MarioAI 環境の生データから
 * A* に必要な特徴量だけを抽出するセンサークラス。
 */
public class StateSensors {

    // マリオ位置（受容野内）
    public int row;
    public int col;

    // マリオの状態
    public boolean onGround;
    public boolean ableToJump;

    // 周囲の環境
    public int wallDistance;
    public int gapDistance;
    public int enemyDistance;
    public boolean enemyAhead;

    /** 簡易コンストラクタ（最低限の情報だけで作る場合） */
    public StateSensors(int row, int col, boolean onGround, boolean ableToJump) {
        this.row = row;
        this.col = col;
        this.onGround = onGround;
        this.ableToJump = ableToJump;

        // 他の情報は「不明扱い」にしておく
        this.wallDistance = -1;
        this.gapDistance = -1;
        this.enemyDistance = -1;
        this.enemyAhead = false;
    }

    /**
     * RuleBaseAgent を参照して、現在の特徴量をまとめて取得する。
     */
    public StateSensors(RuleBaseAgent agent) {

        this.onGround   = agent.isMarioOnGround();
        this.ableToJump = agent.isMarioAbleToJump();

        // Marioの座標
        this.row = agent.getMarioEgoRow();
        this.col = agent.getMarioEgoCol();

        // （将来のために LevelMap を作っておくが、今は使わなくてもよい）
        LevelMap map = new LevelMap(agent.getScene());

        this.wallDistance  = detectWall(agent, row, col);
        this.gapDistance   = detectGap(agent, row, col);
        this.enemyDistance = detectEnemy(agent, row, col);
        this.enemyAhead    = (enemyDistance >= 0);
    }

    // ==============================================================
    // 壁検出：前方 1～8 マスのどこに障害物タイルがあるか
    // ==============================================================
    private int detectWall(RuleBaseAgent agent, int r, int c) {
        for (int dx = 1; dx <= 8; dx++) {
            int cell = agent.getReceptiveFieldCellValue(r, c + dx);
            if (isObstacle(cell)) return dx;
        }
        return -1;
    }

    private boolean isObstacle(int cell) {
        return cell == GeneralizerLevelScene.BRICK ||
               cell == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH ||
               cell == GeneralizerLevelScene.FLOWER_POT_OR_CANNON;
    }

    // ==============================================================
    // 穴検出：前方 1～8 タイルの中で「縦方向が全部 0」の列があるか
    // ==============================================================
    private int detectGap(RuleBaseAgent agent, int r, int c) {

        for (int dx = 1; dx <= 8; dx++) {

            boolean empty = true;

            // 下方向に 5 マスほどチェック
            for (int dy = 0; dy < 5; dy++) {
                int v = agent.getReceptiveFieldCellValue(r + dy, c + dx);
                if (v != 0) {
                    empty = false;
                    break;
                }
            }

            if (empty) return dx;
        }

        return -1;
    }

    // ==============================================================
    // 敵検出：前方 1～8 マス、上下 ±2 の範囲で敵スプライトを探す
    // ==============================================================
    private int detectEnemy(RuleBaseAgent agent, int r, int c) {
        int minDist = Integer.MAX_VALUE;
        boolean found = false;

        for (int dx = 1; dx <= 8; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int v = agent.getEnemiesCellValue(r + dy, c + dx);
                if (v != 0) {
                    found = true;
                    minDist = Math.min(minDist, dx);
                }
            }
        }

        return found ? minDist : -1;
    }

    // ==============================================================
    // デバッグ用
    // ==============================================================
    @Override
    public String toString() {
        return "[Sensors] r=" + row + ", c=" + col +
                " ground=" + onGround +
                " jump=" + ableToJump +
                " wall=" + wallDistance +
                " gap=" + gapDistance +
                " enemy=" + enemyDistance;
    }
}