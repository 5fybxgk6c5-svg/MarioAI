package ch.idsia.agents.astar;

/**
 * Simulator / A* 用の簡易 EnemyMap。
 * - マップは (row, col) のタイル座標
 * - 敵がいれば true
 * - stomp で removeEnemy される
 *
 * 本家 MarioAI の EnemyMap よりずっと単純化しているが、
 * A* で「敵がいるタイルを踏む」チェックには十分。
 */
public class EnemyMap {

    private final boolean[][] map;

    public EnemyMap(int rows, int cols) {
        map = new boolean[rows][cols];
    }

    /** 敵がいるか？ */
    public boolean hasEnemy(int r, int c) {
        if (r < 0 || r >= map.length) return false;
        if (c < 0 || c >= map[0].length) return false;
        return map[r][c];
    }

    /** 敵を追加（テスト用 or 初期状態構築用） */
    public void addEnemy(int r, int c) {
        if (r < 0 || r >= map.length) return;
        if (c < 0 || c >= map[0].length) return;
        map[r][c] = true;
    }

    /** stomp された敵を削除 */
    public void removeEnemy(int r, int c) {
        if (r < 0 || r >= map.length) return;
        if (c < 0 || c >= map[0].length) return;
        map[r][c] = false;
    }

    public int getRows() { return map.length; }
    public int getCols() { return map[0].length; }
}