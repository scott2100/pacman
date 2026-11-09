class PacMan {

	private int pacmanX = 20;
	private int pacmanY = 20;
	private int speed = 2;
	private String direction;
	private String queuedDirection;
	private int[][] currentTile;

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

	public String getDirection() {
		return this.direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void setCurrentTile() {

	}

	public void move() {
		System.out.println("Position: " + this.getPacManX() + " " + this.getPacManY());
		if (this.getDirection() == "up") {
			this.setPacManY(pacmanY - this.getSpeed());
		}
		if (this.getDirection() == "right") {
			this.setPacManX(pacmanX + this.getSpeed());
		}
		if (this.getDirection() == "down") {
			this.setPacManY(pacmanY + this.getSpeed());
		}
		if (this.getDirection() == "left") {
			this.setPacManX(pacmanX - this.getSpeed());
		}
		System.out.println("Position: " + this.getPacManX() + " " + this.getPacManY());
		System.out.println("grid position: " + this.getPacManX() / 20 + " " + this.getPacManY() / 20);
	}
}
