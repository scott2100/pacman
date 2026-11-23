# PacMan

A small Pac-Man clone written in Java with Swing.

I wrote the first version in 2015 to brush up on Java when I moved to the US. I'm now
bringing it up to date with modern tooling, tests and a cleaner architecture, for the craic.

<!-- TODO: add a screenshot or GIF here, e.g. ![Gameplay](docs/screenshot.png) -->

## Status

Work in progress. Right now you can steer Pac-Man around a tile maze. He stops at walls, turns
cleanly at corners, reverses instantly when you press the opposite direction, and moves at a
configurable speed without overshooting tile centres. You can press a
direction early and he turns at the next opening. Pellets are drawn but can't be eaten yet, and
there are no ghosts, score or win/lose states.

## Requirements

- **Java 21.** The build uses a Gradle toolchain, so Gradle downloads a JDK 21 for you if you
  don't have one installed.
- Nothing else. The Gradle wrapper is included, so you don't need Gradle installed.

## Run the game

From this folder:

```bash
./gradlew run
```

On Windows, use `gradlew.bat run`.

## Run the tests

```bash
./gradlew test
```

The tests use JUnit 5 and only cover game logic, so they run without opening a window.

## Controls

| Key | Action |
|---|---|
| Arrow keys | Move. Press a direction early to turn at the next opening; press the opposite direction to turn around immediately. |

## Project structure

```
app/src/main/java/io/github/scott2100/pacman/
  PacManFrame.java   Entry point; creates the window
  PacManPanel.java   Draws the maze and Pac-Man, handles keyboard input, runs the game loop,
                     and holds the level layout for now
  PacMan.java        Pac-Man's position, direction and movement rules
  Direction.java     UP / DOWN / LEFT / RIGHT / STOPPED, each with its x/y step and opposite
  Maze.java          The tile grid, answered through queries (isWall, isPill, width, height)
  Tile.java          Tile types: EMPTY, WALL, PILL
app/src/test/java/io/github/scott2100/pacman/
  PacManTest.java    Movement tests against a small hand-made maze
docs/
  PLAN.md            Modernization plan and review log
```

The game logic (`PacMan`, `Maze`, `Direction`, `Tile`) doesn't depend on Swing, which is what lets
it be tested without a window.

## Known limitations

- Speed is a whole number of pixels per tick; fractional speeds aren't supported yet.
- Game speed depends on the loop timing rather than elapsed time, and the game loop runs on its
  own thread alongside Swing's event thread without synchronization.
- The window is a fixed 400×400 pixels and the maze is hardcoded in `PacManPanel`.
- Pac-Man is drawn as a square.
- No pellet eating, score, ghosts, lives or win/lose yet.

## Roadmap

1. Move the game loop onto a Swing timer and decouple speed from frame rate.
2. Pellets, score and a win state; load the maze from a text file.
3. A proper art pass: an animated Pac-Man, real-looking walls, a HUD.
4. Ghosts.

See [`docs/PLAN.md`](docs/PLAN.md) for the detailed plan.

## Built with

- Java 21, Swing / Java2D
- Gradle 9 (wrapper included)
- JUnit 5
