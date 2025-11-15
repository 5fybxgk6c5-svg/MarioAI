package ch.idsia.agents.astar;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.RuleBaseAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * A* を使って Mario を操作するエージェント。
 * - RuleBaseAgent を継承することで Mario の状態取得が非常に簡単になる。
 * - 毎フレーム LevelMap / EnemyMap を構築して Planner に渡す。
 * - Planner から返ってきた ACT_* をゲームのキー入力に変換して実行。
 */
public class AStarAgent extends RuleBaseAgent implements Agent {

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

        // 受容野 (19×19)
        byte[][] scene = getScene();
        LevelMap level = new LevelMap(scene);

        // 敵マップ（簡易）
        EnemyMap enemies = new EnemyMap(scene.length, scene[0].length);
        // MarioAI では敵は getEnemiesCellValue から読める
        // → AStarPlanner 内で「敵踏み」が必要な場合のみ利用される


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

        // === 3) A* プランナーを作成 ===
        AStarPlanner planner = new AStarPlanner(level, enemies);

        // === 4) A* により「次に取るべき行動」を 1 手だけ取得 ===
        int act = planner.plan(state);

        // === 5) A* の ACT_* を Mario のキー配列に変換 ===
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
                // 何もしない（落下防止）
                if (onGround && sensors.gapDistance == 1) {
                    action[Mario.KEY_JUMP] = true; // その場ジャンプで生存優先
                }
                break;
        }

        return action;
    }
}