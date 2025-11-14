
package ch.idsia.agents.astar;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.RuleBaseAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * A* プランナーを利用して実際にゲームを動かすエージェント。
 * RuleBaseAgent を継承して、環境情報の取得部分を再利用する。
 */
public class AStarAgent extends RuleBaseAgent implements Agent {

    private AStarPlanner planner = new AStarPlanner();

    public AStarAgent() {
        super("AStarAgent");
    }

    @Override
    public boolean[] getAction() {

        // ======== 1. センサーを読み取る ========
        StateSensors sensors = new StateSensors(this);

        // ======== 2. MarioState に変換 ========
        MarioState state = new MarioState(
                sensors.row,
                sensors.col,
                sensors.onGround,
                sensors.ableToJump,
                sensors.wallDistance,
                sensors.gapDistance,
                sensors.enemyDistance,
                sensors.enemyAhead
        );

        // ======== 3. A* プランナーで1アクション決定 ========
        int act = planner.plan(state);

        // ======== 4. Mario のキー配列を構築 ========
        boolean[] action = new boolean[Environment.numberOfKeys];

        // デフォルト：右へ進む
        action[Mario.KEY_RIGHT] = true;

        switch (act) {

            case AStarPlanner.ACT_RIGHT:
                action[Mario.KEY_RIGHT] = true;
                break;

            case AStarPlanner.ACT_DASH_RIGHT:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_SPEED] = true;
                break;

            case AStarPlanner.ACT_JUMP:
                action[Mario.KEY_JUMP] = true;
                break;

            case AStarPlanner.ACT_LEFT:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_RIGHT] = false;
                break;

            case AStarPlanner.ACT_NONE:
                // 何もしない
                break;
        }

        return action;
    }
}