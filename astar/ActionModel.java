package ch.idsia.agents.astar;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class ActionModel {

    // A* が選べる“離散アクション”
    public static boolean[] walk() {
        return new boolean[] {false, true, false, false, false, false};
    }

    public static boolean[] jump() {
        boolean[] a = walk();
        a[Mario.KEY_JUMP] = true;
        return a;
    }

    public static boolean[] longJump() {
        boolean[] a = walk();
        a[Mario.KEY_JUMP]  = true;
        a[Mario.KEY_SPEED] = true;
        return a;
    }
}
