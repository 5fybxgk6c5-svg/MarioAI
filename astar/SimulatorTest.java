package ch.idsia.agents.astar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simulator の総合動作テスト。
 */
public class SimulatorTest {

    /** 空のマップを作る */
    private LevelMap makeEmptyMap(int h, int w) {
        byte[][] s = new byte[h][w];
        return new LevelMap(s);
    }

    /** 床（1 行だけブロック）を作る */
    private LevelMap makeFlatGround(int h, int w) {
        byte[][] s = new byte[h][w];
        for (int c = 0; c < w; c++) {
            s[h - 1][c] = 1; // 最下段にブロック
        }
        return new LevelMap(s);
    }

    /** 敵マップの空実装 */
    private static class EmptyEnemyMap extends EnemyMap {
        public EmptyEnemyMap(int h, int w) { super(h, w); }
    }


    //━━━━━━━━━━━━━━━━━━━━━━━
    // 1. 歩行テスト（右移動）
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testWalkRight() {
        LevelMap level = makeFlatGround(15, 30);
        EnemyMap emap = new EmptyEnemyMap(15, 30);
        Simulator sim = new Simulator(level, emap);

        MarioState s = new MarioState(13, 5, true, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_RIGHT);
        assertNotNull(next);
        assertEquals(5 + 1, next.col);  // 横に 1 マス進む
        assertEquals(13, next.row);     // 高さは変わらない
    }

    //━━━━━━━━━━━━━━━━━━━━━━━
    // 2. 落下テスト
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testFall() {
        LevelMap level = makeFlatGround(15, 30);
        EnemyMap emap = new EmptyEnemyMap(15, 30);
        Simulator sim = new Simulator(level, emap);

        // 床より 3 マス上
        MarioState s = new MarioState(10, 5, false, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_RIGHT);
        assertNotNull(next);

        // 最終的には row=13 の床に着地するはず
        assertEquals(13, next.row);
    }

    //━━━━━━━━━━━━━━━━━━━━━━━
    // 3. ジャンプ（短距離）
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testShortJump() {
        LevelMap level = makeFlatGround(20, 40);
        EnemyMap emap = new EmptyEnemyMap(20, 40);
        Simulator sim = new Simulator(level, emap);

        MarioState s = new MarioState(18, 5, true, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_JUMP);
        assertNotNull(next);

        // 着地位置は元の位置より右＆地面にいる
        assertTrue(next.col >= 5);
        assertEquals(18, next.row);
    }

    //━━━━━━━━━━━━━━━━━━━━━━━
    // 4. ダッシュジャンプ（より遠くへ）
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testRunJump() {
        LevelMap level = makeFlatGround(20, 40);
        EnemyMap emap = new EmptyEnemyMap(20, 40);
        Simulator sim = new Simulator(level, emap);

        MarioState s = new MarioState(18, 5, true, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_JUMP_RUN);
        assertNotNull(next);

        // ダッシュジャンプは短ジャンプより遠く飛ぶ
        assertTrue(next.col > 5 + 1);
    }

    //━━━━━━━━━━━━━━━━━━━━━━━
    // 5. 壁衝突テスト
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testWallCollision() {
        byte[][] scene = new byte[20][40];
        for (int r = 0; r < 20; r++) {
            scene[r][10] = 1;  // col=10 に壁
        }

        LevelMap level = new LevelMap(scene);
        EnemyMap emap = new EmptyEnemyMap(20, 40);
        Simulator sim = new Simulator(level, emap);

        MarioState s = new MarioState(18, 9, true, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_RIGHT);

        // 壁があるので動作失敗（null）
        assertNull(next);
    }
  //━━━━━━━━━━━━━━━━━━━━━━━
    // 6. 敵踏み（stomp）
    //━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    public void testStomp() {
        LevelMap level = makeFlatGround(20, 40);
        EnemyMap emap = new EnemyMap(20, 40);
        emap.addEnemy(17, 7);  // 地面の 1 マス上に敵

        Simulator sim = new Simulator(level, emap);

        MarioState s = new MarioState(10, 5, false, true, -1, -1, -1, false);

        MarioState next = sim.simulate(s, AStarPlanner.ACT_JUMP_RUN);
        assertNotNull(next);

        // 踏んだ敵は消えている
        assertFalse(emap.hasEnemy(17, 7));

        // 踏んだタイルに着地している
        assertEquals(17, next.row);
        assertEquals(7, next.col);
    }
    
}