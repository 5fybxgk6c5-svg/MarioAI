package ch.idsia.agents.astar;

import java.util.*;

public class AStarPlanner {

    public static final int ACT_NONE      = 0;
    public static final int ACT_RIGHT     = 1;
    public static final int ACT_LEFT      = 2;
    public static final int ACT_JUMP      = 3;
    public static final int ACT_RUN_RIGHT = 4;
    public static final int ACT_JUMP_RUN  = 5;

    private static final int GOAL_DELTA_COL     = 6;
    private static final int BACKWARD_LIMIT     = 6;
    private static final int MAX_EXPANDED_NODES = 4000;
    private static final int MAX_STEPS          = 40;

    private final boolean DEBUG;

    private final Heuristic heuristic;
    private final Simulator simulator;

    public AStarPlanner(LevelMap level, EnemyMap enemies) {
        this(level, enemies, false);
    }

    public AStarPlanner(LevelMap level, EnemyMap enemies, boolean debug) {
        this.heuristic  = new Heuristic();
        this.simulator  = new Simulator(level, enemies);
        this.DEBUG      = debug;
    }

    public int plan(MarioState start) {

        if (DEBUG) {
            System.out.println("[A*] ==== new planning ====");
            System.out.println("[A*] start=" + start);
        }

        PriorityQueue<AStarNode> open = new PriorityQueue<>();
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

            if (cur.g > MAX_STEPS) {
                if (DEBUG) {
                    System.out.println("[A*] Skip node (g too large): " + cur);
                }
                continue;
            }

            Float recordedG = bestG.get(cs);
            if (recordedG != null && cur.g > recordedG + 1e-6f) {
                if (DEBUG) {
                    System.out.println("[A*] Skip worse g: " + cur + " (bestG=" + recordedG + ")");
                }
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
                System.out.println("[A*] Expand(" + expanded + "): " + cur);
            }

            // ゴール条件
            if (cs.col - start.col >= GOAL_DELTA_COL && cs.onGround) {
                if (DEBUG) {
                    System.out.println("[A*] Goal reached at node: " + cur);
                }
                int first = reconstructAction(cur);
                if (DEBUG) {
                    System.out.println("[A*] First action to take = " + first);
                }
                return first;
            }

            // 行動列挙
            for (int act : possibleActions(cs)) {

                if (DEBUG) {
                    System.out.println("   [A*] Try act=" + actToString(act) + " from " + cs);
                }

                MarioState next = simulator.simulate(cs, act);

                if (next == null) {
                    if (DEBUG) {
                        System.out.println("      -> simulate returned null (wall/void)");
                    }
                    continue;
                }

                if (DEBUG) {
                    System.out.println("      -> next=" + next);
                }

                if (next.col < start.col - BACKWARD_LIMIT) {
                    if (DEBUG) {
                        System.out.println("      -> pruned (too far left)");
                    }
                    continue;
                }

                float cost = actionCost(act);
                float nextG = cur.g + cost;
                float nextH = heuristic.evaluate(next);

                Float oldG = bestG.get(next);
                if (oldG != null && nextG >= oldG - 1e-6f) {
                    if (DEBUG) {
                        System.out.println("      -> pruned (oldG=" + oldG + " <= nextG=" + nextG + ")");
                    }
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

                if (DEBUG) {
                    System.out.println("      -> push to OPEN, f=" + nextNode.f + ", g=" + nextG + ", h=" + nextH);
                }
            }
        }

        if (DEBUG) {
            System.out.println("[A*] No path found, return NONE");
        }
        return ACT_NONE;
    }

    private List<Integer> possibleActions(MarioState s) {
        List<Integer> list = new ArrayList<>();

        if (s.onGround) {
            list.add(ACT_RUN_RIGHT);
        }
        list.add(ACT_RIGHT);

        if (s.ableToJump) {
            list.add(ACT_JUMP);
            list.add(ACT_JUMP_RUN);
        }

        list.add(ACT_NONE);
        list.add(ACT_LEFT);

        return list;
    }

    private float actionCost(int act) {
        switch (act) {
            case ACT_LEFT: return 1.2f;
            case ACT_NONE: return 1.1f;
            default:       return 1.0f;
        }
    }

    private int reconstructAction(AStarNode node) {
        AStarNode cur    = node;
        AStarNode parent = cur.parent;

        while (parent != null && parent.parent != null) {
            cur = parent;
            parent = cur.parent;
        }
        return cur.action;
    }

    private String actToString(int act) {
        switch (act) {
            case ACT_NONE:      return "NONE";
            case ACT_RIGHT:     return "RIGHT";
            case ACT_LEFT:      return "LEFT";
            case ACT_JUMP:      return "JUMP";
            case ACT_RUN_RIGHT: return "RUN_RIGHT";
            case ACT_JUMP_RUN:  return "JUMP_RUN";
        }
        return "UNKNOWN(" + act + ")";
    }
}