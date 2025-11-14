package ch.idsia.agents.astar;

/**
 * A* シミュレーション用の超簡易 EnemyMap。
 * - 敵の位置はタイル単位で管理
 * - stomp により removeEnemy 可能
 */
public class EnemyMap {

    private final boolean[][] enemy;

    public EnemyMap(int rows, int cols) {
        enemy = new boolean[rows][cols];
    }

    /** MarioAI の enemies[][] から読み込む */
    public EnemyMap(byte[][] enemiesScene) {
        int h = enemiesScene.length;
        int w = enemiesScene[0].length;
        enemy = new boolean[h][w];

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                enemy[r][c] = enemiesScene[r][c] != 0;
            }
        }
    }

    /** 敵が居るか */
    public boolean hasEnemy(int row, int col) {
        if (row < 0 || row >= enemy.length) return false;
        if (col < 0 || col >= enemy[0].length) return false;
        return enemy[row][col];
    }

    /** 敵を消す（stomp） */
    public void removeEnemy(int row, int col) {
        if (row < 0 || row >= enemy.length) return;
        if (col < 0 || col >= enemy[0].length) return;
        enemy[row][col] = false;
    }
}