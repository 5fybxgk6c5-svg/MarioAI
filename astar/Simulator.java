package ch.idsia.agents.astar;

public class Simulator {

    private static final int TILE_SIZE = 16;
    private static final int MAX_ROW   = 18;

    // ★ここにもデバッグフラグ
    private static final boolean DEBUG = true;

    private final LevelMap level;
    private final EnemyMap enemies;

    private final int unitPixels;
    private final int gravity;
    private final int shortJumpVy0;
    private final int runJumpVy0;
    private final int walkVx;
    private final int runVx;

    public Simulator(LevelMap level, EnemyMap enemies) {
        this(level, enemies, 4);
    }

    public Simulator(LevelMap level, EnemyMap enemies, int unitPixels) {
        this.level      = level;
        this.enemies    = enemies;
        this.unitPixels = unitPixels;

        this.gravity      = unitPixels;
        this.shortJumpVy0 = -5 * unitPixels;
        this.runJumpVy0   = -6 * unitPixels;

        this.walkVx = 4 * unitPixels;
        this.runVx  = 6 * unitPixels;
    }

    private int quantize(int px) {
        if (unitPixels <= 1) return px;
        if (px >= 0) {
            return (px / unitPixels) * unitPixels;
        } else {
            return -((-px + unitPixels - 1) / unitPixels) * unitPixels;
        }
    }

    public MarioState simulate(MarioState s, int action) {

        int px = quantize(s.col * TILE_SIZE);
        int py = quantize(s.row * TILE_SIZE);

        if (DEBUG) {
            System.out.println(" [Sim] simulate state=" + s + ", action=" + actToString(action)
                    + " (px=" + px + ", py=" + py + ")");
        }

        MarioState result;

        switch (action) {

            case AStarPlanner.ACT_RIGHT:
                result = simulateWalk(s, px, py, walkVx);
                break;

            case AStarPlanner.ACT_LEFT:
                result = simulateWalk(s, px, py, -walkVx);
                break;

            case AStarPlanner.ACT_RUN_RIGHT:
                result = simulateWalk(s, px, py, runVx);
                break;

            case AStarPlanner.ACT_JUMP:
                result = simulateJump(s, px, py, walkVx, shortJumpVy0);
                break;

            case AStarPlanner.ACT_JUMP_RUN:
                result = simulateJump(s, px, py, runVx, runJumpVy0);
                break;

            default:
                int afterFallY = applyFall(px, py);
                if (afterFallY == Integer.MIN_VALUE) result = null;
                else result = makeNextStateFromPixels(s, px, afterFallY, true);
                break;
        }

        if (DEBUG) {
            if (result == null) {
                System.out.println(" [Sim] result = null  (collision or fall)");
            } else {
                System.out.println(" [Sim] result = " + result);
            }
        }

        return result;
    }

    private MarioState simulateWalk(MarioState s, int px, int py, int vx) {
        int nextPx = quantize(px + vx);
        int nextPy = py;

        if (DEBUG) {
            System.out.println("   [Sim] Walk: from (" + px + "," + py + ") to (" + nextPx + "," + nextPy + ")");
        }

        if (collides(nextPx, nextPy)) {
            if (DEBUG) {
                System.out.println("   [Sim] Walk collision at (" + nextPx + "," + nextPy + ")");
            }
            return null;
        }

        int fallY = applyFall(nextPx, nextPy);
        if (fallY == Integer.MIN_VALUE) {
            if (DEBUG) {
                System.out.println("   [Sim] Walk fell into hole");
            }
            return null;
        }

        return makeNextStateFromPixels(s, nextPx, fallY, true);
    }

    private MarioState simulateJump(MarioState s, int px, int py, int vx, int vy0) {
        int x = px;
        int y = py;
        int vy = vy0;

        final int MAX_FRAMES = 32;

        if (DEBUG) {
            System.out.println("   [Sim] Jump start: x=" + x + ", y=" + y + ", vx=" + vx + ", vy0=" + vy0);
        }

        for (int t = 0; t < MAX_FRAMES; t++) {

            int nextX = quantize(x + vx);
            int nextY = quantize(y + vy);

            if (DEBUG) {
                System.out.println("     [Sim] t=" + t + " -> (" + nextX + "," + nextY + "), vy=" + vy);
            }

            if (vy < 0 && collides(nextX, nextY)) {
                if (DEBUG) {
                    System.out.println("     [Sim] Hit ceiling, start falling");
                }
                int landingY = applyFall(x, y);
                if (landingY == Integer.MIN_VALUE) return null;
                return makeNextStateFromPixels(s, x, landingY, true);
            }

            x = nextX;
            y = nextY;

            if (collides(x, y)) {
                if (DEBUG) {
                    System.out.println("     [Sim] Jump collision (wall) at (" + x + "," + y + ")");
                }
                return null;
            }

            if (y / TILE_SIZE > MAX_ROW + 1) {
                if (DEBUG) {
                    System.out.println("     [Sim] Jump fell out of map");
                }
                return null;
            }

            // stomp（省略してもよいなら DEBUG だけ）
            if (vy > 0 && enemies != null) {
                int col = x / TILE_SIZE;
                int row = (y + TILE_SIZE - 1) / TILE_SIZE;
                if (enemies.hasEnemy(row, col)) {
                    if (DEBUG) {
                        System.out.println("     [Sim] Stomp enemy at (" + row + "," + col + ")");
                    }
                    enemies.removeEnemy(row, col);
                    int landingY = row * TILE_SIZE;
                    return makeNextStateFromPixels(s, col * TILE_SIZE, landingY, true);
                }
            }

            if (isOnGround(x, y) && vy >= 0) {
                int landingY = alignToGround(x, y);
                if (DEBUG) {
                    System.out.println("     [Sim] Land on ground at (" + x + "," + landingY + ")");
                }
                return makeNextStateFromPixels(s, x, landingY, true);
            }

            vy += gravity;
        }

        int finalY = applyFall(x, y);
        if (finalY == Integer.MIN_VALUE) return null;
        return makeNextStateFromPixels(s, x, finalY, true);
    }

    private int applyFall(int px, int startPy) {
        int y = startPy;
        for (int i = 0; i < 64; i++) {
            if (isOnGround(px, y)) {
                int aligned = alignToGround(px, y);
                if (DEBUG) {
                    System.out.println("   [Sim] applyFall landed at y=" + aligned);
                }
                return aligned;
            }
            int nextY = y + unitPixels;
            if (nextY / TILE_SIZE > MAX_ROW + 1) {
                if (DEBUG) {
                    System.out.println("   [Sim] applyFall fell out of map");
                }
                return Integer.MIN_VALUE;
            }
            if (collides(px, nextY)) {
                if (DEBUG) {
                    System.out.println("   [Sim] applyFall collision at y=" + nextY);
                }
                return Integer.MIN_VALUE;
            }
            y = nextY;
        }
        if (DEBUG) {
            System.out.println("   [Sim] applyFall exceeded max loop");
        }
        return Integer.MIN_VALUE;
    }

    private boolean isOnGround(int px, int py) {
        int footY = py + TILE_SIZE;
        int col   = px / TILE_SIZE;
        int row   = footY / TILE_SIZE;
        return level.tileAt(row, col) == LevelMap.TILE_SOLID;
    }

    private int alignToGround(int px, int py) {
        int col = px / TILE_SIZE;
        int row = (py + TILE_SIZE) / TILE_SIZE;
        return row * TILE_SIZE - TILE_SIZE;
    }

    private boolean collides(int px, int py) {
        int col = px / TILE_SIZE;
        int row = py / TILE_SIZE;
        if (level.tileAt(row, col) == LevelMap.TILE_SOLID) return true;
        if (level.tileAt(row - 1, col) == LevelMap.TILE_SOLID) return true;
        return false;
    }

    private MarioState makeNextStateFromPixels(MarioState base, int px, int py, boolean recomputeJumpable) {
        int col = px / TILE_SIZE;
        int row = py / TILE_SIZE;

        boolean onGround   = isOnGround(px, py);
        boolean ableToJump = recomputeJumpable ? onGround : base.ableToJump;

        MarioState ns = new MarioState(
                row,
                col,
                onGround,
                ableToJump,
                base.wallDistance,
                base.gapDistance,
                base.enemyDistance,
                base.enemyAhead
        );

        if (DEBUG) {
            System.out.println("   [Sim] makeNextState -> " + ns);
        }

        return ns;
    }

    private String actToString(int act) {
        switch (act) {
            case AStarPlanner.ACT_NONE:      return "NONE";
            case AStarPlanner.ACT_RIGHT:     return "RIGHT";
            case AStarPlanner.ACT_LEFT:      return "LEFT";
            case AStarPlanner.ACT_JUMP:      return "JUMP";
            case AStarPlanner.ACT_RUN_RIGHT: return "RUN_RIGHT";
            case AStarPlanner.ACT_JUMP_RUN:  return "JUMP_RUN";
        }
        return "UNKNOWN(" + act + ")";
    }
}