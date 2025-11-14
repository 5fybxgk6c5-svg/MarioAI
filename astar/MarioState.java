package ch.idsia.agents.astar;

public class MarioState {

    public int row;
    public int col;

    public boolean onGround;
    public boolean ableToJump;

    public int wallDistance;
    public int gapDistance;
    public int enemyDistance;
    public boolean enemyAhead;

    /** B用：位置と地面状態だけ指定する簡易コンストラクタ */
    public MarioState(int row, int col, boolean onGround, boolean ableToJump) {
        this.row = row;
        this.col = col;
        this.onGround = onGround;
        this.ableToJump = ableToJump;

        // 残りの特徴量は「未使用」の意味で -1 / false にしておく
        this.wallDistance  = -1;
        this.gapDistance   = -1;
        this.enemyDistance = -1;
        this.enemyAhead    = false;
    }

    /** 従来のフルコンストラクタ（必要ならこのまま残す） */
    public MarioState(int row, int col,
                      boolean onGround,
                      boolean ableToJump,
                      int wallDistance,
                      int gapDistance,
                      int enemyDistance,
                      boolean enemyAhead) {

        this.row = row;
        this.col = col;
        this.onGround = onGround;
        this.ableToJump = ableToJump;

        this.wallDistance = wallDistance;
        this.gapDistance = gapDistance;
        this.enemyDistance = enemyDistance;
        this.enemyAhead = enemyAhead;
    }

    /** ディープコピー */
    public MarioState(MarioState other) {
        this.row = other.row;
        this.col = other.col;

        this.onGround = other.onGround;
        this.ableToJump = other.ableToJump;

        this.wallDistance = other.wallDistance;
        this.gapDistance = other.gapDistance;
        this.enemyDistance = other.enemyDistance;
        this.enemyAhead = other.enemyAhead;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarioState)) return false;
        MarioState o = (MarioState) obj;

        return this.row == o.row &&
               this.col == o.col &&
               this.onGround == o.onGround &&
               this.ableToJump == o.ableToJump &&
               this.wallDistance == o.wallDistance &&
               this.gapDistance == o.gapDistance &&
               this.enemyDistance == o.enemyDistance &&
               this.enemyAhead == o.enemyAhead;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        result = 31 * result + (onGround ? 1 : 0);
        result = 31 * result + (ableToJump ? 1 : 0);
        result = 31 * result + wallDistance;
        result = 31 * result + gapDistance;
        result = 31 * result + enemyDistance;
        result = 31 * result + (enemyAhead ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State { r=" + row + ", c=" + col +
                ", ground=" + onGround +
                ", jump=" + ableToJump +
                ", wall=" + wallDistance +
                ", gap=" + gapDistance +
                ", enemy=" + enemyDistance +
                " }";
    }
}