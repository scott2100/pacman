class PacMan {

	private int pacmanX;
	private int pacmanY;
	private int speed = 2;
	private Direction direction = Direction.STOPPED;
	private Direction queuedDirection = Direction.STOPPED;
	private Maze maze;

	public PacMan(int x, int y) {
		this.pacmanX = x;
		this.pacmanY = y;
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

	public void setMaze(Maze maze) {
		this.maze = maze;
	}

	public int getAdjustedCoordinate(int coordinate) {
		return coordinate / 20;
	}

	public void move() {

		if (this.getPacManX() % 20 == 0 && this.getPacManY() % 20 == 0) {
			int qdx = queuedDirection.getDx();
			int qdy = queuedDirection.getDy();

			if ((this.maze.getMaze()[getAdjustedCoordinate(this.getPacManY())
					+ qdy][getAdjustedCoordinate(this.getPacManX()) + qdx] != 1)) {
				this.direction = this.queuedDirection;
			} else {
				this.queuedDirection = this.direction;
			}
		}

		int dx = direction.getDx();
		int dy = direction.getDy();

		if ((this.maze.getMaze()[getAdjustedCoordinate(this.getPacManY()) + dy][getAdjustedCoordinate(this.getPacManX())
				+ dx] != 1)) {
			this.setPacManY(this.getPacManY() + dy);
			this.setPacManX(this.getPacManX() + dx);
		}
	}
}
