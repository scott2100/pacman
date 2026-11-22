package io.github.scott2100.pacman;

class Maze {

	private final int[][] grid;

	public Maze(int[][] grid) {
		this.grid = grid;
	}

	public boolean isWall(int row, int col) {
		return this.grid[row][col] == Tile.WALL.getType();
	}

	public boolean isPill(int row, int col) {
		return this.grid[row][col] == Tile.PILL.getType();
	}

	public int height() {
		return this.grid.length;
	}

	public int width() {
		return this.grid[0].length;
	}
}
