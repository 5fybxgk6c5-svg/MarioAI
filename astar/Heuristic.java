package ch.idsia.agents.astar;

/**
 * A* 探索で用いるヒューリスティック関数。
 *
 * 「f = g + h」の h 部分を計算する。
 * 値が小さいノードほど優先される前提なので、
 * ・前進 = h を小さく（＝負方向に大きく）
 * ・壁/穴/敵 = h を大きく（ペナルティ）
 * という符号にしている。
 */
public class Heuristic {

    /**
     * 状態 s のヒューリスティック値 h を返す。
     * 値が小さいほど「好ましい」状態。
     */
    public float evaluate(MarioState s) {

        float h = 0.0f;

        // ① 前進ボーナス（列が大きいほどゴールに近い）
        //   col が大きいほど h がより負になる（＝優先度↑）
        h += -(s.col) * 5.0f;

        // ② 壁ペナルティ
        //   壁が近いほどペナルティを大きくする
        if (s.wallDistance >= 0) {
            h += (8 - s.wallDistance) * 3.0f;
        }

        // ③ 穴ペナルティ（重め）
        if (s.gapDistance >= 0) {
            h += (8 - s.gapDistance) * 6.0f;
        }

        // ④ 敵ペナルティ
        if (s.enemyDistance >= 0) {
            h += (6 - s.enemyDistance) * 4.0f;
        }

        return h;
    }
}