package ch.idsia.agents.astar;

import java.util.*;

/**
 * A* 探索本体（後退込み・完全版）。
 *
 * - Simulator を使って「本物に近い物理」で次状態を生成
 * - 左右移動・ジャンプ・ダッシュジャンプをすべて候補に入れる
 * - MarioState の equals/hashCode に基づく再訪チェックでループを防止
 * - 「ある程度右に進んで地面に立てている状態」をゴールとみなす
 */
public class AStarPlanner {

    // ======= 行動種類 =======
    public static final int ACT_NONE      = 0;
    public static final int ACT_RIGHT     = 1;
    public static final int ACT_LEFT      = 2;
    public static final int ACT_JUMP      = 3;
    public static final int ACT_RUN_RIGHT = 4;  // ダッシュ右
    public static final int ACT_JUMP_RUN  = 5;  // ダッシュジャンプ

    // ======= 検索パラメータ =======
    /** 「これだけ右に進めば OK」とするタイル数 */
    private static final int GOAL_DELTA_COL = 6;

    /** どれだけ左に下がるのを許すか（start.col - BACK_LIMIT より左は展開しない） */
    private static final int BACKWARD_LIMIT = 6;

    /** ノード展開上限（暴走防止） */
    private static final int MAX_EXPANDED_NODES = 4000;

    /** 経路長の上限（g の最大値） */
    private static final int MAX_STEPS = 40;

    /** デバッグログを出すかどうか */
    private final boolean DEBUG;

    private final Heuristic heuristic;
    private final Simulator simulator;

    // ==========================================================
    // コンストラクタ
    // ==========================================================
    public AStarPlanner(LevelMap level, EnemyMap enemies) {
        this(level, enemies, false);
    }

    public AStarPlanner(LevelMap level, EnemyMap enemies, boolean debug) {
        this.heuristic = new Heuristic();
        this.simulator = new Simulator(level, enemies);
        this.DEBUG = debug;
    }

    // ==========================================================
    // A* 検索（1 ステップ分の行動を返す）
    // ==========================================================
    public int plan(MarioState start) {

        // f 値が小さい順に取り出す優先度付きキュー
        PriorityQueue<AStarNode> open = new PriorityQueue<>();

        // その状態に到達した時点での「最良 g 値」を記録するテーブル
        // これを使って、g が悪い再訪ノードは捨てる
        HashMap<MarioState, Float> bestG = new HashMap<>();

        AStarNode startNode = new AStarNode(
                start,
                null,
                ACT_NONE,
                0.0f,
                heuristic.evaluate(start)
        );

        open.add(startNode);
        bestG.put(new MarioState(start), 0.0f);

        int expanded = 0;

        while (!open.isEmpty()) {

            AStarNode cur = open.poll();
            MarioState cs = cur.state;

            // g が限界を超えていたら捨てる
            if (cur.g > MAX_STEPS) {
                continue;
            }

            // 既により良い g で探索済みならスキップ
            Float recordedG = bestG.get(cs);
            if (recordedG != null && cur.g > recordedG + 1e-6f) {
                continue;
            }

            expanded++;
            if (expanded > MAX_EXPANDED_NODES) {
                if (DEBUG) {
                    System.out.println("[A*] Node limit reached, fallback NONE");
                }
                return ACT_NONE;
            }

            if (DEBUG) {
                System.out.println("[A*] Expand: " + cur);
            }

            // ── ゴール条件 ─────────────────────
            // 1. start.col から GOAL_DELTA_COL 以上右に進んでいる
            // 2. 地面の上に立っている
            if (cs.col - start.col >= GOAL_DELTA_COL && cs.onGround) {
                if (DEBUG) {
                    System.out.println("[A*] Goal reached: " + cs);
                }
                return reconstructAction(cur);
            }

            // ── 次の行動候補を展開 ───────────────
            for (int act : possibleActions(cs)) {

                // 物理シミュレーション → 次状態生成
                MarioState next = simulator.simulate(cs, act);
                if (next == null) {
                    // 壁に激突 or 穴に落ちたなど
                    continue;
                }

                // あまり左に行き過ぎる探索は切る
                if (next.col < start.col - BACKWARD_LIMIT) {
                    continue;
                }

                float cost = actionCost(act);
                float nextG = cur.g + cost;
                float nextH = heuristic.evaluate(next);

                // 既により良い g で訪れているならスキップ
                Float oldG = bestG.get(next);
                if (oldG != null && nextG >= oldG - 1e-6f) {
                    continue;
                }

                AStarNode nextNode = new AStarNode(
                        next,
                        cur,
                        act,
                        nextG,
                        nextH
                );

                bestG.put(new MarioState(next), nextG);
                open.add(nextNode);
            }
        }

        // パスが見つからなかった場合
        if (DEBUG) {
            System.out.println("[A*] No path found, return NONE");
        }
        return ACT_NONE;
    }

    // ==========================================================
    // 行動候補の列挙（後退込み）
    // ==========================================================
    private List<Integer> possibleActions(MarioState s) {

        List<Integer> list = new ArrayList<>();

        // まずは「前進系」を優先的に並べる（キューは f でソートされるが、若干のバイアス）
        // 地上ならダッシュ前進を積極的に
        if (s.onGround) {
            list.add(ACT_RUN_RIGHT);
        }

        // 常に右移動は候補に
        list.add(ACT_RIGHT);

        // ジャンプ可能なら前ジャンプ・ダッシュジャンプも
        if (s.ableToJump) {
            list.add(ACT_JUMP);
            list.add(ACT_JUMP_RUN);
        }

        // その場維持も一応許す（狭い足場などで有用）
        list.add(ACT_NONE);

        // 後退も候補に入れる（ブロックを踏み台にするなどのため）
        list.add(ACT_LEFT);

        return list;
    }

    // ==========================================================
    // 行動ごとのコスト
    // ここで「後退やその場」は少しだけペナルティを増やして、
    // なるべく前進するパスが優先されるようにする。
    // ==========================================================
    private float actionCost(int act) {
        switch (act) {
            case ACT_LEFT:
                return 1.2f;   // 後退は少し重い
            case ACT_NONE:
                return 1.1f;   // その場も少し重い
            default:
                return 1.0f;   // 前進系は標準コスト
        }
    }

    // ==========================================================
    // 経路復元：スタートから見た「最初の 1 手」だけ返す
    // ==========================================================
    private int reconstructAction(AStarNode node) {

        AStarNode cur    = node;
        AStarNode parent = cur.parent;

        // 親が null になる直前が「最初のアクション」を持つノード
        while (parent != null && parent.parent != null) {
            cur = parent;
            parent = cur.parent;
        }

        if (DEBUG) {
            System.out.println("[A*] First action = " + cur.action);
        }

        return cur.action;
    }
}