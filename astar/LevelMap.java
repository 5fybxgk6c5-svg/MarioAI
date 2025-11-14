package ch.idsia.agents.astar;

public class LevelMap {

    public static final int TILE_EMPTY = 0;
    public static final int TILE_SOLID = 1;

    private final int[][] tiles;

    /**
     * MarioAI の受容野 (byte[][] scene) から LevelMap を構築する。
     * scene は GeneralizerLevelScene の値であり、
     * val != 0 なら基本的に固体扱いするのが安定。
     */
    public LevelMap(byte[][] scene) {

        int h = scene.length;
        int w = scene[0].length;

        tiles = new int[h][w];

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {

                int v = scene[r][c] & 0xFF;

                // GeneralizerLevelScene.Z1 は:
                // 0 = 空
                // 1 = 堅いブロック
                // 2 = パイプ or カノン
                // 3〜 = その他のブロック
                tiles[r][c] = (v == 0 ? TILE_EMPTY : TILE_SOLID);
            }
        }
    }

    public int tileAt(int r, int c) {
        if (r < 0 || r >= tiles.length) return TILE_SOLID;
        if (c < 0 || c >= tiles[0].length) return TILE_SOLID;
        return tiles[r][c];
    }
}