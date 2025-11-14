package ch.idsia.agents.astar;

public class Heuristic {

    public float evaluate(MarioState s) {

        float h = 0;

        // ① 前進ボーナス（強め）
        h += -(s.col) * 5.0f;

        // ② 壁ペナルティ
        if (s.wallDistance >= 0)
            h += (8 - s.wallDistance) * 3.0f;

        // ③ 穴ペナルティ（重め）
        if (s.gapDistance >= 0)
            h += (8 - s.gapDistance) * 6.0f;

        // ④ 敵ペナルティ
        if (s.enemyDistance >= 0)
            h += (6 - s.enemyDistance) * 4.0f;

        return h;
    }
}