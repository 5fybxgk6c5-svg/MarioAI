package ch.idsia.agents.astar;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.RuleBaseAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class AStarAgent extends RuleBaseAgent implements Agent {

    // ★ デバッグフラグ：状況を詳しく見たいとき true にする
    private static final boolean DEBUG = true;

    public AStarAgent() {
        super("AStarAgent");
    }

    @Override
    public boolean[] getAction() {

        // === 1) Mario の状態を取得 ===
        int row = getMarioEgoRow();
        int col = getMarioEgoCol();

        boolean onGround   = isMarioOnGround();
        boolean ableToJump = isMarioAbleToJump();

        byte[][] scene = getScene();
        LevelMap level = new LevelMap(scene);

        // 敵マップ（簡易）
        EnemyMap enemies = new EnemyMap(scene.length, scene[0].length);
        // ★必要ならここで getEnemiesCellValue を走査して EnemyMap に詰めてもよい

        // === 2) センサー取得 ===
        StateSensors sensors = new StateSensors(this);

        MarioState state = new MarioState(
                row, col,
                onGround, ableToJump,
                sensors.wallDistance,
                sensors.gapDistance,
                sensors.enemyDistance,
                sensors.enemyAhead
        );

        if (DEBUG) {
            System.out.println("===== AStarAgent.getAction() =====");
            System.out.println("Mario pos   : row=" + row + ", col=" + col);
            System.out.println("onGround    : " + onGround + ", ableToJump=" + ableToJump);
            System.out.println("Sensors     : " + sensors);
            System.out.println("MarioState  : " + state);
        }

        // === 3) A* プランナーを作成（DEBUG=true） ===
        AStarPlanner planner = new AStarPlanner(level, enemies, DEBUG);

        // === 4) A* で 1 手だけ計画 ===
        int act = planner.plan(state);

        if (DEBUG) {
            System.out.println("A* decided act = " + actToString(act));
        }

        // === 5) ACT_* → キー入力 ===
        boolean[] action = new boolean[Environment.numberOfKeys];

        switch (act) {

            case AStarPlanner.ACT_RIGHT:
                action[Mario.KEY_RIGHT] = true;
                break;

            case AStarPlanner.ACT_LEFT:
                action[Mario.KEY_LEFT] = true;
                break;

            case AStarPlanner.ACT_JUMP:
                action[Mario.KEY_JUMP] = true;
                break;

            case AStarPlanner.ACT_RUN_RIGHT:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_SPEED] = true;
                break;

            case AStarPlanner.ACT_JUMP_RUN:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_SPEED] = true;
                action[Mario.KEY_JUMP]  = true;
                break;

            case AStarPlanner.ACT_NONE:
            default:
                // 何もしない（ただし即死しそうなら保険ジャンプ）
                if (onGround && sensors.gapDistance == 1) {
                    action[Mario.KEY_JUMP] = true;
                    if (DEBUG) {
                        System.out.println("ACT_NONE but gapDistance==1 → 保険ジャンプ");
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("ACT_NONE → その場維持");
                    }
                }
                break;
        }

        if (DEBUG) {
            System.out.print("Output keys :");
            if (action[Mario.KEY_LEFT])  System.out.print(" L");
            if (action[Mario.KEY_RIGHT]) System.out.print(" R");
            if (action[Mario.KEY_JUMP])  System.out.print(" J");
            if (action[Mario.KEY_SPEED]) System.out.print(" S");
            System.out.println();
            System.out.println("==================================");
        }

        return action;
    }

    private String actToString(int act) {
        switch (act) {
            case AStarPlanner.ACT_NONE:      return "NONE";
            case AStarPlanner.ACT_RIGHT:     return "RIGHT";
            case AStarPlanner.ACT_LEFT:      return "LEFT";
            case AStarPlanner.ACT_JUMP:      return "JUMP";
            case AStarPlanner.ACT_RUN_RIGHT: return "RUN_RIGHT";
            case AStarPlanner.ACT_JUMP_RUN:  return "JUMP_RUN";
        }
        return "UNKNOWN(" + act + ")";
    }
}