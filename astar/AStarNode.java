package ch.idsia.agents.astar;

/**
 * A* 探索で使用される「状態ノード」。
 * MarioState（抽象化した状態）を持ち、
 * g, h, f のコストと親ノード情報を保持する。
 */
public class AStarNode implements Comparable<AStarNode> {

    // このノードが表す状態
    public MarioState state;

    // A* の経路復元用
    public AStarNode parent;

    /**
     * このノードに来るために実行したアクション。
     * AStarPlanner の ACT_* 定数を使う:
     *   ACT_NONE      = 0
     *   ACT_RIGHT     = 1
     *   ACT_LEFT      = 2
     *   ACT_JUMP      = 3
     *   ACT_RUN_RIGHT = 4
     *   ACT_JUMP_RUN  = 5
     */
    public int action;

    // A* コスト
    public float g;   // スタートから現在までの実コスト
    public float h;   // 現在 → ゴールまでの推定コスト（ヒューリスティック）
    public float f;   // f = g + h

    // ==========================================================
    // コンストラクタ
    // ==========================================================
    public AStarNode(MarioState state, AStarNode parent, int action, float g, float h) {
        // State はコピーして持っておく（外部から書き換えられないように）
        this.state  = new MarioState(state);
        this.parent = parent;
        this.action = action;

        this.g = g;
        this.h = h;
        this.f = g + h;
    }

    // ==========================================================
    // 優先度付きキューで利用（f 値が小さいものが優先）
    // ==========================================================
    @Override
    public int compareTo(AStarNode o) {
        return Float.compare(this.f, o.f);
    }

    // ==========================================================
    // Closed リスト用の equals / hashCode
    // 「同じ状態（MarioState）」かどうかで判断する
    // ==========================================================
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AStarNode)) return false;
        AStarNode o = (AStarNode) obj;
        return this.state.equals(o.state);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return "Node{ f=" + f +
                ", g=" + g +
                ", h=" + h +
                ", state=" + state +
                " }";
    }
}