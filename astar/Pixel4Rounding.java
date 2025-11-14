package ch.idsia.agents.astar;

public class Pixel4Rounding implements RoundingStrategy {
    private static final int GRID = 4;

    @Override
    public int roundX(float x) {
        return (int)(Math.round(x / GRID) * GRID);
    }

    @Override
    public int roundY(float y) {
        return (int)(Math.round(y / GRID) * GRID);
    }
}