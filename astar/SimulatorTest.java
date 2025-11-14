package ch.idsia.agents.astar;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Simulator が「まともに動いているか」を確認するユニットテスト。
 *  ・平らな地面の上で
 *      - 右移動しても落ちない
 *      - ジャンプ／ダッシュジャンプしても落ちない
 *  くらいを確認する、ゆるめのテストにしている。
 */
public class SimulatorTest {

    private LevelMap level;
    private EnemyMap enemies;
    private Simulator simulator;
    private MarioState start;

    @Before
    public void setUp() {
        // 5(行) x 10(列) のシンプルなステージ
        // 最下行(4行目)だけを地面タイルにする
        byte[][] scene = new byte[5][10];
        for (int c = 0; c < 10; c++) {
            scene[4][c] = 1;   // 何かしらのブロック（LevelMap側で SOLID 扱いになる）
        }

        level = new LevelMap(scene);
        enemies = new EnemyMap(5, 10);
        simulator = new Simulator(level, enemies, 4);

        // row=3, col=1 にマリオが立っているとみなす（地面の1マス上）
        start = new MarioState(
                3,      // row
                1,      // col
                true,   // onGround
                true,   // ableToJump
                -1,     // wallDistance
                -1,     // gapDistance
                -1,     // enemyDistance
                false   // enemyAhead
        );
    }

    @Test
    public void testWalkRight() {
        MarioState next = simulator.simulate(start, AStarPlanner.ACT_RIGHT);

        assertNotNull("右移動で即死してはいけない", next);
        assertTrue("右移動で左に戻ってはいけない", next.col >= start.col);
        assertEquals("平地なので高さは変わらない想定", start.row, next.row);
        assertTrue("着地しているはず", next.onGround);
    }

    @Test
    public void testJump() {
        MarioState next = simulator.simulate(start, AStarPlanner.ACT_JUMP);

        assertNotNull("ジャンプで即死してはいけない", next);
        assertTrue("ジャンプでも少なくとも右方向に進んでほしい", next.col >= start.col);
        assertEquals("平地上に着地するので最終的な高さは元と同じ", start.row, next.row);
        assertTrue("着地しているはず", next.onGround);
    }

    @Test
    public void testRunJump() {
        MarioState next = simulator.simulate(start, AStarPlanner.ACT_JUMP_RUN);

        assertNotNull("ダッシュジャンプで即死してはいけない", next);
        assertTrue("ダッシュジャンプなら右に少しは進んでいてほしい", next.col >= start.col);
        assertEquals("平地上に着地するので最終的な高さは元と同じ", start.row, next.row);
        assertTrue("着地しているはず", next.onGround);
    }
}