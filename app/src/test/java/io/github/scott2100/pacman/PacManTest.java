package io.github.scott2100.pacman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacManTest {

    public int[][] maze = {
            { 1, 1, 1, 1, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 1, 1, 1, 1 }
    };

    public PacMan pacman;
    public Maze pacMaze;

    @Test
    public void testMovingLeftStopsWhenItHitsAWall() {
        pacMaze = new Maze();
        pacMaze.setMaze(maze);
        pacman = new PacMan(60, 20);
        pacman.setQueuedDirection(Direction.LEFT);
        pacman.setMaze(pacMaze);
        for (int i = 50; i > 0; i--) {
            pacman.move();
        }

        assertEquals(20, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());
    }

}
