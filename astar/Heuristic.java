package ch.idsia.agents.astar;

public class Heuristic {

    // 必要に応じて on/off
    private static final boolean DEBUG = true;

    public float evaluate(MarioState s) {

        float h = 0;

        float forward = -(s.col) * 5.0f;
        h += forward;

        float wallTerm = 0;
        if (s.wallDistance >= 0) {
            wallTerm = (8 - s.wallDistance) * 3.0f;
            h += wallTerm;
        }

        float gapTerm = 0;
        if (s.gapDistance >= 0) {
            gapTerm = (8 - s.gapDistance) * 6.0f;
            h += gapTerm;
        }

        float enemyTerm = 0;
        if (s.enemyDistance >= 0) {
            enemyTerm = (6 - s.enemyDistance) * 4.0f;
            h += enemyTerm;
        }

        if (DEBUG) {
            System.out.println(String.format(
                    " [H] state=%s  h=%.2f (forward=%.2f, wall=%.2f, gap=%.2f, enemy=%.2f)",
                    s, h, forward, wallTerm, gapTerm, enemyTerm
            ));
        }

        return h;
    }
}
