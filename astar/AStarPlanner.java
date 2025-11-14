package ch.idsia.agents.astar;

import java.util.*;

/**
 * A* 探索本体。
 * Heuristic（評価関数）と Simulator（遷移モデル）を外部クラスに分離した
 * 綺麗な構造の A*Planner。
 */
public class AStarPlanner { 

    // ======= 行動種類 =======
    public static final int ACT_NONE      = 0;
    public static final int ACT_RIGHT     = 1;
    public static final int ACT_LEFT      = 2;
    public static final int ACT_JUMP      = 3;
    public static final int ACT_RUN_RIGHT = 4;  // ★ 新規
    public static final int ACT_JUMP_RUN  = 5;  // ★ 新規


    private Heuristic heuristic = new Heuristic();
    private Simulator simulator = new Simulator();

    // ─────────────────────────────────────────────
    // A* 検索（1ステップ分の行動を返す）
    // ─────────────────────────────────────────────
    public int plan(MarioState start) {

        PriorityQueue<AStarNode> open = new PriorityQueue<>();
        HashSet<AStarNode> closed = new HashSet<>();

        AStarNode startNode = new AStarNode(
                start,
                null,
                ACT_NONE,
                0,
                heuristic.evaluate(start)
        );

        open.add(startNode);

        while (!open.isEmpty()) {

            AStarNode cur = open.poll();

            // ── ゴール条件：6 タイル以上前進したら成功 ──
            if (cur.state.col - start.col >= 6) {
                return reconstructAction(cur);
            }

            closed.add(cur);

            // ── 次の行動候補を生成 ──
            for (int act : possibleActions(cur.state)) {

                // 物理シミュレーション → 次状態生成
                MarioState next = simulator.simulate(cur.state, act);
                if (next == null) continue;

                AStarNode nextNode = new AStarNode(
                        next,
                        cur,
                        act,
                        cur.g + 1,
                        heuristic.evaluate(next)
                );

                if (closed.contains(nextNode)) continue;

                open.add(nextNode);
            }
        }

        // 行動なし
        return ACT_NONE;
    }


    // ─────────────────────────────────────────────
    // 可能な行動一覧
    // ─────────────────────────────────────────────
    private List<Integer> possibleActions(MarioState s) {

        List<Integer> list = new ArrayList<>();

        list.add(ACT_RIGHT);

        if (s.ableToJump)
            list.add(ACT_JUMP);

        if (s.onGround)
            list.add(ACT_DASH_RIGHT);

        list.add(ACT_LEFT);
        list.add(ACT_NONE);

        return list;
    }

    // ─────────────────────────────────────────────
    // 経路復元：最初の 1 手だけ返す
    // ─────────────────────────────────────────────
    private int reconstructAction(AStarNode node) {

        AStarNode cur = node;
        AStarNode parent = cur.parent;

        // 親が null になる直前が「最初のアクション」
        while (parent != null && parent.parent != null) {
            cur = parent;
            parent = cur.parent;
        }

        return cur.action;
    }
}