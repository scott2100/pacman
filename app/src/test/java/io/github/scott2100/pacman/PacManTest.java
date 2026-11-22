package io.github.scott2100.pacman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacManTest {

    public int[][] grid = {
            { 1, 1, 1, 1, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 1, 1, 1, 1 }
    };

    public PacMan pacman;
    public Maze maze;

    @Test
    public void testMovingLeftStopsWhenItHitsAWall() {
        maze = new Maze(grid);
        pacman = new PacMan(60, 20, 20, maze);
        pacman.setQueuedDirection(Direction.LEFT);
        for (int i = 50; i > 0; i--) {
            pacman.move();
        }

        assertEquals(20, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());
    }

}
