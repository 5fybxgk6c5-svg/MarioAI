package ch.idsia.agents.astar;

/**
 * A* 探索用の抽象アクション定義
 * 実際の boolean[5] への変換は Simulator が行う
 */
public final class Actions {

    // --- 基本動作 ---
    public static final int ACT_NONE = 0;      // 何もしない
    public static final int ACT_RIGHT = 1;     // 右移動
    public static final int ACT_LEFT = 2;      // 左移動

    // --- ジャンプ ---
    public static final int ACT_JUMP = 3;      // その場ジャンプ
    public static final int ACT_JUMP_RIGHT = 4; // 右＋ジャンプ
    public static final int ACT_JUMP_LEFT = 5;  // 左＋ジャンプ

    // --- ダッシュ ---
    public static final int ACT_RUN = 6;       // ダッシュ（スピード）
    public static final int ACT_RUN_JUMP = 7;  // ダッシュジャンプ
    public static final int ACT_RUN_JUMP_RIGHT = 8;
    public static final int ACT_RUN_JUMP_LEFT = 9;

    // 必須：全アクションを列挙して返す
    public static final int[] ALL_ACTIONS = {
            ACT_NONE,
            ACT_RIGHT, ACT_LEFT,
            ACT_JUMP, ACT_JUMP_RIGHT, ACT_JUMP_LEFT,
            ACT_RUN,
            ACT_RUN_JUMP,
            ACT_RUN_JUMP_RIGHT,
            ACT_RUN_JUMP_LEFT
    };

    private Actions() {}
}
