package ch.idsia.agents.astar;

/**
 * A* 探索で使用される「状態ノード」。
 * MarioState（抽象化した状態）を持ち、
 * g,h,f のコストと親ノード情報を保持する。
 */
public class AStarNode implements Comparable<AStarNode> {

    // このノードが表す状態
    public MarioState state;

    // A* の経路復元用
    public AStarNode parent;

    // このノードに来るために実行したアクション
    public int action;   // 0=右,1=ジャンプ,2=右ダッシュ,3=左,4=その場 など

    // A* コスト
    public float g;   // スタートから現在まで
    public float h;   // 現在 → ゴール推定
    public float f;   // f = g + h

    // ==========================================================
    // コンストラクタ
    // ==========================================================
    public AStarNode(MarioState state, AStarNode parent, int action, float g, float h) {
        this.state = new MarioState(state); // ← ディープコピーで安全
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
    // 同じ状態（MarioState）で判断する
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