package io.github.scott2100.pacman;

enum Tile {
    PILL(2), WALL(1), EMPTY(0);

    private final int type;

    Tile(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}