# Pac-Man — Modernization Plan

**Status:** in progress — Phase 0 mostly done, Phases 1–3 partly done (reviewed against the code 2026-11-17)
**Scope:** the Swing Pac-Man in `pacman/`
**Goal:** turn a working maze-walker into a small, clean, tested, recognizable Pac-Man — **without a rewrite**.

---

## 1. Purpose

This project works: it compiles, opens a window, draws a tile maze, and lets you steer a
yellow square around it. Since the plan was first written, the biggest problem has been fixed: the
180 lines of four-way copy-paste movement are now one ~20-line `move()` driven by a `Direction`
enum, and the project has Gradle, git and its first JUnit 5 test.

What's left is the "second layer" of the foundation: a few real movement bugs, a game loop that
shares state across threads unsafely, rendering that still allocates per frame, and no game layer
(pellets are drawn but never eaten; no score, win or lose).

The order stays the same: fix the foundation, then add gameplay, then polish.

---

## 2. Goals

- One source of truth for Pac-Man's direction. ✅
- A single, general movement routine instead of four duplicated blocks. ✅
- Simulation decoupled from rendering and from frame rate.
- Game logic free of `java.awt` so it can be tested headless. ✅ (keep it that way)
- A reproducible build and a repo that looks like a real project. (mostly)
- A playable loop: eat pellets → score → win; ghosts → lose.
- No framework migration. Swing stays.

## 3. Non-goals

- Rewriting in JavaFX, libGDX, or another language.
- A terminal/TUI version.
- Multiplayer or networking.
- A map editor (a text-file maze gets 95% of the benefit for 1% of the effort).
- Chasing test coverage numbers.
- Pixel-perfect arcade authenticity.

---

## 4. Where the project is today

Repo: `pacman/` (remote `github.com/scott2100/pacman`), on branch `feature/testin_movement_refactor`,
with **uncommitted changes in 7 files** (JUnit 4 → 5, `static` removed from `Maze`, dead imports removed).

| File | Role | State |
|---|---|---|
| `app/src/main/java/PacMan.java` | Entity + movement | 73 lines, no `static`, enum direction + queued direction, one `move()` |
| `app/src/main/java/Direction.java` | Value type | Enum with `dx`/`dy` and `STOPPED` — good |
| `app/src/main/java/Maze.java` | Tile data | Hardcoded 20×20 array; unused `height`/`width` fields; `getMaze()` hands out the mutable array |
| `app/src/main/java/PacManPanel.java` | Render + input + loop | Raw `Thread` from the constructor, new image every frame, `public static` size fields |
| `app/src/main/java/PacManFrame.java` | Bootstrap | Name fixed; not started on the EDT; no title; `setVisible` called before setup |
| `app/src/test/java/PacManTest.java` | Tests | One JUnit 5 test (move left into a wall) — passes |
| `app/build.gradle.kts` | Build | `application` plugin, `mainClass = PacManFrame`, Java 21 toolchain, JUnit 5.14 |
| `README.md` | Docs | Four lines, no run instructions |
| `../PLAN.md` | This file | **Lives outside the git repo** — not versioned |

Still missing: package declaration (everything is in the default package).

---

## 5. Known defects

### Fixed since the first plan
- ~~NPE on first Up or Left~~ — `Direction.STOPPED` is the default.
- ~~`static` position/direction in `PacMan` and `Maze`~~ (pending commit for `Maze`).
- ~~Direction tracked twice (booleans + strings)~~ — one enum.
- ~~Four duplicated direction blocks~~ — one routine.
- ~~`System.out.println` in the hot loop~~, ~~unused `boolean[] keys`~~, ~~unused imports~~.
- ~~`PacmanFrame.java` / `PacManFrame` capitalization~~.
- ~~`.class` files committed~~ — `.gitignore` covers `*.class` and `build`.

### Still open
| Severity | Symptom | Where |
|---|---|---|
| High | **One-tick stall at every L-corner.** `dx`/`dy` are read into locals *before* the queued turn is applied, so the wall check after the turn uses the *old* direction, sees the wall, and sets `STOPPED`. Pac-Man turns on the next tick instead. (Verified: moving right into an up-corner, x stays at 60 for two ticks.) | `PacMan.move()` |
| High | **Data race between threads.** The game thread calls `move()` while the Swing event thread paints and writes `queuedDirection` from `keyPressed`. No synchronization or `volatile`. | `PacManPanel` |
| Medium | `speed` field is ignored — movement is always 1 px per tick. If you use it, the `% 20 == 0` alignment check silently breaks for any speed that doesn't divide 20 (Pac-Man skips the tile centre and never turns). | `PacMan` |
| Medium | No mid-tile reversal: pressing the opposite direction only takes effect at the next tile centre. In the arcade game reversing is instant. | `PacMan.move()` |
| Medium | No bounds check on the maze lookup — fine while the border is all walls, but tunnel wrap (Phase 5) will throw `ArrayIndexOutOfBoundsException`. | `PacMan.move()` |
| Medium | Movement speed depends on loop timing (`sleep(20)` + `repaint()` in one thread). | `PacManPanel.run()` |
| Medium | `catch (Exception e) { // }` hides failures as freezes. | `PacManPanel.run()` |
| Medium | New image allocated every frame on top of Swing's own double buffering; `super.paintComponent` not called. | `PacManPanel.paintComponent()` |
| Low | Hidden dependency: `PacMan` needs `setMaze()` before `move()` or it NPEs. Pass the maze to the constructor. | `PacMan` |
| Low | `Maze.getMaze()` returns the internal array; anyone can change the maze. Prefer a query like `isWall(col, row)`. | `Maze` |
| Low | Size defined three times (`Maze.height/width`, `PacManPanel.height/width`, tile size `20` in four places) and preferred size still passed as `(height, width)`. | several |
| Low | Magic numbers `20`, `400`, `1`, `2`, `7`, `5`. | everywhere |
| Low | Frame not created on the EDT (`SwingUtilities.invokeLater`); `setVisible(true)` before close op and background are set; no window title. | `PacManFrame` |
| Low | Leftover Gradle template: `guava` still in `libs.versions.toml`; `.idea/` files committed. | build |

---

## 6. Guiding principles

1. **Fix before feature.** No pellets or ghosts until movement is correct and tested.
2. **One representation.** Direction is a single value. ✅ Next: position should be one representation too (see Phase 2b).
3. **Model knows nothing about Swing.** `PacMan`, `Maze`, `Direction` must not import `java.awt`.
4. **Time is explicit.** Movement advances by elapsed time, not by "once per loop iteration".
5. **Test the rules, not the pixels.** Pure functions for walkability, scoring, win state, AI choice.
6. **Write the failing test first** for every bug you fix — the corner stall is the perfect first one.
7. **No toolkit migration** to solve a design problem.

---

## 7. Phases

### Phase 0 — Foundations — *mostly done*

- [x] git repo, first commit is the 2015 code (the "before" photo).
- [x] `.gitignore` for `*.class`, `.gradle`, `build`.
- [x] No `.class` files in the tree.
- [x] Gradle with a committed wrapper (`./gradlew`).
- [x] Standard layout (`app/src/main/java`, `app/src/test/java`).
- [x] Run task: `./gradlew run` launches `PacManFrame`.
- [ ] **Commit the work in progress** (7 modified files) and merge the feature branch when green.
- [ ] Move `PLAN.md` into the repo (e.g. `pacman/docs/PLAN.md`) so the plan is versioned with the code.
- [ ] Add a package declaration (e.g. `io.github.scott2100.pacman`) and move files to match.
- [ ] Decide on `.idea/`: either ignore it or keep only the shareable files deliberately.
- [ ] Remove the unused `guava` entry from `libs.versions.toml`.
- [ ] Expand `README.md`: what it is, screenshot/GIF, `./gradlew run`, `./gradlew test`, Java 21, known limitations.

**Acceptance:** a clean clone builds and runs with one documented command; plan and code live together.

### Phase 1 — Stabilize — *about half done*

- [x] Fix the null-direction crash.
- [x] Remove `static` from mutable fields in `PacMan` and `Maze`.
- [x] Delete dead imports, the `keys` array and debug prints.
- [x] Fix the `PacmanFrame`/`PacManFrame` capitalization.
- [ ] Stop swallowing exceptions in `run()` (and restore the interrupt flag on `InterruptedException`).
- [ ] Remove the remaining dead state: `Maze.height/width`, `PacManPanel`'s `public static` sizes.
- [ ] Replace magic numbers with named constants (tile size, speed, tile types). *Hint: tile types are a natural second enum.*
- [ ] Fix the swapped preferred-size arguments (better: compute size from maze dimensions × tile size).
- [ ] Start the UI on the EDT; set title, close op and background before `setVisible(true)`.

**Acceptance:** no `static` mutable state anywhere; no empty catch blocks; one place defines tile size.

### Phase 2 — Movement — *routine done, correctness not yet*

Done:
- [x] Direction is an enum with a separate queued direction.
- [x] One routine decides turn / advance / stop.
- [x] Direction buffering (press early, turn at the corner) works.
- [x] Four duplicated blocks deleted; `move()` is ~20 lines.

2a — fix the routine:
- [ ] **Fix the corner stall.** Write the failing test first: an L-shaped corridor, move right into the corner with UP queued, assert that on the tick after reaching the corner Pac-Man has moved up. *Hint: think about when you read `dx`/`dy` — before or after the direction can change?*
- [ ] Allow instant reversal mid-tile. *Hint: reversing is always legal because you came from that tile.*
- [ ] Inject the maze through the constructor; drop `setMaze`.
- [ ] Give `Maze` a query method (`isWall(col, row)`) and stop exposing the array — this is also where bounds/wrap handling will live.

2b — make speed real:
- [ ] Use `speed`. *Hint: the `% 20 == 0` test only works because the step is 1 px. Options: (a) store tile + sub-tile offset instead of raw pixels, or (b) when a step would cross a tile centre, clamp to the centre, decide, then spend the leftover distance. Both are worth sketching on paper before coding.*

**Acceptance:** no stall at corners; reversal is instant; any speed works; each of these has a test.

### Phase 3 — Tests — *started*

- [x] JUnit 5 via Gradle.
- [x] Moving into a wall stops on the last open tile.
- [ ] Corner turn has no stall (regression test for the 2a bug).
- [ ] Buffered turn: queued direction is rejected into a wall and applied at the next open junction.
- [ ] Reversal mid-tile.
- [ ] Each of the four directions, with one parameterized test (`@ParameterizedTest` + `@EnumSource`).
- [ ] Speed > 1 still stops exactly on tile centres.
- [ ] Later (with Phase 5): pellet consumption → score; no pellets → win; tunnel wrap.
- [ ] Tidy the test class: fields set in `@BeforeEach` (or local variables), not `public` fields; a tiny helper to build a maze from strings makes fixtures readable.

**Acceptance:** a dozen focused tests, runnable with `./gradlew test`, none touching Swing.

### Phase 4 — Loop and rendering

- [ ] Fix the data race. Simplest option for Swing: drive updates with `javax.swing.Timer` so `move()`, input and painting all happen on the EDT. *(Then the raw `Thread` and the `Runnable` go away.)*
- [ ] Fixed-timestep update; speed independent of machine.
- [ ] Use Swing's double buffering; stop allocating an image per frame; call `super.paintComponent`.
- [ ] Scale factor instead of hardcoded 20 px tiles; resizable window.
- [ ] Window title, centred on screen, clean shutdown.
- [ ] Pause (space) and restart (R). *Hint: replace the chain of `if (key == …)` with a `Map<Integer, Direction>` or Swing Key Bindings.*

**Acceptance:** same speed on a slow and a fast machine; no shared mutable state across threads; no allocation in the loop.

### Phase 5 — Make it a game

- [ ] Pellets (`2` tiles, already drawn) are consumed on contact and removed from the maze.
- [ ] Score, with a HUD.
- [ ] Win state when no pellets remain.
- [ ] Tunnel wrap on the left/right edges (needs the `Maze` query method from 2a).
- [ ] Load the maze from a **text file** (resource) rather than a hardcoded array.
- [ ] Persist a high score.
- [ ] *Architecture step:* introduce a `Game` (or `World`) class that owns `Maze`, `PacMan`, score and state, with an `update()` method. The panel then only reads it to draw and forwards input to it.

**Acceptance:** you can finish a level and see a win screen; the maze can be edited without recompiling.

### Phase 6 — Art pass, staying on Swing

- [ ] Anti-aliased drawing.
- [ ] Pac-Man as an arc with an animated mouth, oriented by direction.
- [ ] Ghosts drawn as ghosts, eyes tracking direction.
- [ ] Maze walls that read as connected walls, not a grid of outlines.
- [ ] Proper HUD font.
- [ ] Optional: sprite sheet and sound effects.

**Acceptance:** a screenshot reads as Pac-Man without a caption.

### Phase 7 — One ghost

- [ ] Ghost entity reusing the Phase 2 movement routine. *Hint: if `move()` lives in `PacMan`, the ghost can't reuse it — consider pulling the grid-movement logic into its own class that both entities use (composition over inheritance).*
- [ ] Greedy chase: at junctions pick the tile that reduces distance to Pac-Man; random otherwise.
- [ ] Contact = lose (life lost / game over).
- [ ] Optional: scatter/chase mode switching, frightened state, power pellets, lives.

**Acceptance:** the ghost reliably hunts; losing is possible; the game is recognizably Pac-Man.

---

## 8. Decisions and open questions

| Decision | Status | Notes |
|---|---|---|
| Build tool | **Decided: Gradle 9.8 + wrapper** | `application` plugin, `./gradlew run`. |
| Java version | **Decided: 21 (toolchain)** | State it in the README. |
| Test framework | **Decided: JUnit 5** | Migrated from the Gradle template's JUnit 4 (uncommitted). |
| UI toolkit | Stay on Swing / Java2D | Unchanged. |
| TUI version | Rejected | Unchanged. |
| Tests | Narrow, logic-only | Unchanged. |
| Package name | **Open** | Suggest `io.github.scott2100.pacman` to match the GitHub repo. |
| Position representation | **Open (Phase 2b)** | Raw pixels vs. tile + offset. Decide before using `speed`. |
| Game loop threading | **Open (Phase 4)** | Recommend `javax.swing.Timer` over a raw thread. |

**Open questions**

- Is the goal a portfolio piece, a learning exercise, or just fun? *(The project brief says learning
  architecture with mentoring, so the plan favours clean structure and tests over art.)*

---

## 9. Definition of done

- Clean clone → one command → playable.
- No `static` mutable game state.
- One movement routine, shared by Pac-Man and ghosts; no duplicated direction logic.
- Game logic independent of Swing and covered by a small test suite.
- Speed independent of frame rate.
- Pellets, score, win, lose, restart.
- A screenshot that looks like Pac-Man.
- README with screenshot, run instructions, and honest limitations.

---

## 10. Suggested next three steps

1. Commit the current changes on the feature branch (with a descriptive message — "Migrate tests to JUnit 5, remove static maze state"), then move `PLAN.md` into the repo.
2. Write the failing corner-stall test, fix `move()`, see it go green, commit.
3. Swap the raw thread for a `javax.swing.Timer` — small change, removes a whole class of bugs.

---

## 11. Backlog — cheap wins not yet scheduled

- Sound effects via `javax.sound.sampled`.
- Multiple levels / level progression.
- Bonus fruit.
- Lives + death animation.
- Score popups.
- Fullscreen toggle.
- Config file for constants.
- A second renderer (headless text or PNG dump) as proof the model is view-independent.
- GitHub Actions running `./gradlew test` on every push.

## 12. Explicitly not worth it

- Networking/multiplayer.
- A map editor.
- 100% test coverage.
- Migrating frameworks before the current code is clean.

---

## Review log

- **2026-11-17** — Reviewed against the code on `feature/testin_movement_refactor` (+ uncommitted changes).
  Phase 0 mostly done; Phase 1 about half done; Phase 2 routine done, two movement bugs found (corner stall, no reversal) and `speed` unused;
  Phase 3 started (1 test). New defects: thread data race, hidden `setMaze` dependency, exposed maze array,
  frame not on the EDT. Added Phase 2a/2b, a suggested `Game` class in Phase 5, and next steps.
- **2026-11-08** — First version.
