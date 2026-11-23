package io.github.scott2100.pacman;

class PacMan {
	private int pacmanX;
	private int pacmanY;
	private int speed;
	private int size;
	private Direction direction = Direction.STOPPED;
	private Direction queuedDirection = Direction.STOPPED;
	private Maze maze;

	public void move() {
		for (int i = 0; i < this.getSpeed(); i++) {
			if (this.direction.opposite() == this.queuedDirection) {
				this.direction = this.queuedDirection;
			}

			if (this.getPacManX() % this.getSize() == 0 && this.getPacManY() % this.getSize() == 0) {
				if (!this.maze.isWall((getAdjustedCoordinate(this.getPacManY()) + queuedDirection.getDy()),
						(getAdjustedCoordinate(this.getPacManX()) + queuedDirection.getDx()))) {
					this.direction = this.queuedDirection;
				}

				if (this.maze.isWall((getAdjustedCoordinate(this.getPacManY()) + direction.getDy()),
						(getAdjustedCoordinate(this.getPacManX()) + direction.getDx()))) {
					this.direction = Direction.STOPPED;
				}
			}

			this.setPacManX(this.getPacManX() + direction.getDx());
			this.setPacManY(this.getPacManY() + direction.getDy());
		}
	}

	public PacMan(int x, int y, int size, int speed, Maze maze) {
		this.pacmanX = x;
		this.pacmanY = y;
		this.size = size;
		this.speed = speed;
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
}
