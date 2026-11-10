class PacMan {

	private int pacmanX = 20;
	private int pacmanY = 20;
	private int speed = 2;
	private Direction direction;
	private Direction queuedDirection;
	private Maze maze;
	private int row;
	private int col;

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

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Direction getQueuedDirection() {
		return this.queuedDirection;
	}

	public void setQueuedDirection(Direction queuedDirection) {
		this.queuedDirection = queuedDirection;
	}

	public void setMaze(Maze maze) {
		this.maze = maze;
	}

	public int getAdjustedCoordinate(int coordinate) {
		return coordinate / 20;
	}

	public void move() {

		if (this.getPacManX() % 20 == 0 && this.getPacManY() % 20 == 0) {
			row = getAdjustedCoordinate(this.getPacManY());
			col = getAdjustedCoordinate(this.getPacManX());

			if (this.queuedDirection == Direction.UP) {
				if ((this.maze.getMaze()[row - 1][col] != 1)) {
					this.direction = this.queuedDirection;
				}
			}
			if (this.queuedDirection == Direction.RIGHT) {
				if ((this.maze.getMaze()[row][col + 1] != 1)) {
					this.direction = this.queuedDirection;
				}
			}
			if (this.queuedDirection == Direction.DOWN) {
				if ((this.maze.getMaze()[row + 1][col] != 1)) {
					this.direction = this.queuedDirection;
				}
			}
			if (this.queuedDirection == Direction.LEFT) {
				if ((this.maze.getMaze()[row][col - 1] != 1)) {
					this.direction = this.queuedDirection;
				}
			}
		}

		if (this.direction == Direction.UP) {
			if ((this.maze.getMaze()[row - 1][col] != 1)) {
				this.setPacManY(this.getPacManY() - this.getSpeed());
			}
		}
		if (this.direction == Direction.RIGHT) {
			if ((this.maze.getMaze()[row][col + 1] != 1)) {
				this.setPacManX(this.getPacManX() + this.getSpeed());
			}
		}
		if (this.direction == Direction.DOWN) {
			if ((this.maze.getMaze()[row + 1][col] != 1)) {
				this.setPacManY(this.getPacManY() + this.getSpeed());
			}
		}
		if (this.direction == Direction.LEFT) {
			if ((this.maze.getMaze()[row][col - 1] != 1)) {
				this.setPacManX(this.getPacManX() - this.getSpeed());
			}
		}
	}
}
