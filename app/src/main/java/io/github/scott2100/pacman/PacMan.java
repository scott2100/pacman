package io.github.scott2100.pacman;

class PacMan {
	private int pacmanX;
	private int pacmanY;
	private int speed = 2;
	private int size = 20;
	private Direction direction = Direction.STOPPED;
	private Direction queuedDirection = Direction.STOPPED;
	private Maze maze;

	public PacMan(int x, int y, int size, Maze maze) {
		this.pacmanX = x;
		this.pacmanY = y;
		this.size = size;
		this.maze = maze;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPacManX() {
		return pacmanX;
	}

	public int getPacManY() {
		return pacmanY;
	}

	public void setPacManX(int pacmanX) {
		this.pacmanX = pacmanX;
	}

	public void setPacManY(int pacmanY) {
		this.pacmanY = pacmanY;
	}

	public void setQueuedDirection(Direction queuedDirection) {
		this.queuedDirection = queuedDirection;
	}

	public int getSize() {
		return size;
	}

	public int getAdjustedCoordinate(int coordinate) {
		return coordinate / this.getSize();
	}

	public void move() {
		System.out.println("move. " + Thread.currentThread().getName());

		if (this.getPacManX() % this.getSize() == 0 && this.getPacManY() % this.getSize() == 0) {

			if ((this.maze.grid[getAdjustedCoordinate(this.getPacManY())
					+ queuedDirection.getDy()][getAdjustedCoordinate(this.getPacManX())
							+ queuedDirection.getDx()] != 1)) {
				this.direction = this.queuedDirection;
			}

			if ((this.maze.grid()[getAdjustedCoordinate(this.getPacManY())
					+ direction.getDy()][getAdjustedCoordinate(this.getPacManX()) + direction.getDx()] == 1)) {
				this.direction = Direction.STOPPED;
			}
		}

		this.setPacManY(this.getPacManY() + direction.getDy());
		this.setPacManX(this.getPacManX() + direction.getDx());
	}
}
