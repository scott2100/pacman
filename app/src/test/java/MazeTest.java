import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MazeTest {

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

        assertEquals(pacman.getPacManX(), 20);
        assertEquals(pacman.getPacManY(), 20);
    }

}
