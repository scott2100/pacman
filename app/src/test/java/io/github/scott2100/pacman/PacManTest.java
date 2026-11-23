package io.github.scott2100.pacman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

public class PacManTest {

    private int[][] grid = {
            { 1, 1, 1, 1, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 0, 0, 0, 1 },
            { 1, 1, 1, 1, 1 }
    };

    private PacMan pacman;
    private Maze maze;

    @BeforeEach
    public void setup() {
        maze = new Maze(grid);
        pacman = new PacMan(40, 20, 20, 1, maze);
    }

    @Test
    public void testMovingLeftStopsWhenItHitsAWall() {
        pacman.setQueuedDirection(Direction.LEFT);
        for (int i = 50; i > 0; i--) {
            pacman.move();
        }

        assertEquals(20, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());
    }

    @Test
    public void testMovingUpStopsWhenItHitsAWall() {
        pacman.setPacManY(40);
        pacman.setQueuedDirection(Direction.UP);
        for (int i = 50; i > 0; i--) {
            pacman.move();
        }

        assertEquals(40, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());
    }

    @Test
    public void testReverseMovementInTheMiddleOfATileRightToLeft() {
        pacman.setQueuedDirection(Direction.RIGHT);

        for (int i = 0; i < 5; i++) {
            pacman.move();
        }
        pacman.setQueuedDirection(Direction.LEFT);
        pacman.move();
        assertEquals(44, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());

    }

    @Test
    public void testShouldNotTurn90DegreesWhenNotCenteredOnTile() {
        pacman.setQueuedDirection(Direction.RIGHT);

        for (int i = 0; i < 5; i++) {
            pacman.move();
        }
        pacman.setQueuedDirection(Direction.UP);
        pacman.move();
        assertEquals(46, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());
    }

    @Test
    public void speedThreeMovePacmanThreePixelsPerTick() {
        pacman.setSpeed(3);
        pacman.setQueuedDirection(Direction.RIGHT);

        for (int i = 0; i < 5; i++) {
            pacman.move();
        }

        assertEquals(55, pacman.getPacManX());
        assertEquals(20, pacman.getPacManY());

    }
}
