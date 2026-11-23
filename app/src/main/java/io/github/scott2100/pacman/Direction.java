package io.github.scott2100.pacman;

enum Direction {
	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), STOPPED(0, 0);

	private final int dx;
	private final int dy;

	Direction(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	public int getDx() {
		return this.dx;
	}

	public int getDy() {
		return this.dy;
	}

	public Direction opposite() {
		return switch (this) {
			case RIGHT -> LEFT;
			case LEFT -> RIGHT;
			case UP -> DOWN;
			case DOWN -> UP;
			case STOPPED -> STOPPED;
		};
	}
}
