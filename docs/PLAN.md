# Pac-Man — Modernization Plan

**Status:** in progress — Phase 0 done once the staged work is committed; Phases 1–3 partly done (reviewed against the code 2026-11-20)
**Scope:** the Swing Pac-Man in `pacman/`
**Goal:** turn a working maze-walker into a small, clean, tested, recognizable Pac-Man — **without a rewrite**.

---

## 1. Purpose

This project works: it compiles, opens a window, draws a tile maze, and lets you steer a
yellow square around it. The 180 lines of four-way copy-paste movement are now one ~20-line
`move()` driven by a `Direction` enum, the project has Gradle, git, a package, a real README and
its first JUnit 5 test, and the corner stall is fixed.

What's left is the "second layer" of the foundation: a test that passes for the wrong reason, a
couple of movement gaps (reversal, speed), a game loop that shares state across threads unsafely,
rendering that still allocates per frame, and no game layer (pellets are drawn but never eaten;
no score, win or lose).

The order stays the same: fix the foundation, then add gameplay, then polish.

---

## 2. Goals

- One source of truth for Pac-Man's direction. ✅
- A single, general movement routine instead of four duplicated blocks. ✅
- Simulation decoupled from rendering and from frame rate.
- Game logic free of `java.awt` so it can be tested headless. ✅ (keep it that way)
- A reproducible build and a repo that looks like a real project. ✅ (once the staged work is committed)
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

Repo: `pacman/` (remote `github.com/scott2100/pacman`), on branch `feature/testin_movement_refactor`, in sync with origin.
Last commit `ad9a546` (2026-11-18): package move, plan moved to `docs/`, `.idea` untracked, `.DS_Store`
ignored, guava removed, fully qualified `mainClass`, maze via constructor. **Note:** that commit's test
still used the old constructors, so `ad9a546` itself doesn't compile its tests (a red commit — see principle 8).

**Staged:** `PacMan` now uses its `size` argument; `PacManTest` updated to the new constructors.
**Unstaged:** `Maze` height/width setters and getters removed.

| File | Role | State |
|---|---|---|
| `app/src/main/java/io/github/scott2100/pacman/PacMan.java` | Entity + movement | 75 lines; maze + size via constructor ✅; corner stall fixed; `speed` unused; debug `println` in `move()` |
| `…/Direction.java` | Value type | Enum with `dx`/`dy` and `STOPPED` — good |
| `…/Maze.java` | Tile data | Hardcoded 20×20 default grid; constructor takes `height`/`width` and **ignores them**; `getMaze()`/`setMaze()` expose the array |
| `…/PacManPanel.java` | Render + input + loop | Raw `Thread` from the constructor; new image every frame; `public static` sizes; debug prints |
| `…/PacManFrame.java` | Bootstrap | Not on the EDT; no title; `setVisible` before setup |
| `app/src/test/java/io/github/scott2100/pacman/PacManTest.java` | Tests | In the right package ✅, compiles ✅ — but **never uses its own maze** (see defects) |
| `app/build.gradle.kts` | Build | `mainClass` fully qualified ✅ |
| `README.md` | Docs | Good. Structure section (no package path) and "corner pause" limitation are out of date; no screenshot yet |
| `docs/PLAN.md` | This file | In the repo ✅ (keep in sync with the project copy) |

---

## 5. Known defects

### Fixed since the first plan
- ~~NPE on first Up or Left~~ — `Direction.STOPPED` is the default.
- ~~`static` position/direction in `PacMan` and `Maze`~~.
- ~~Direction tracked twice (booleans + strings)~~ — one enum.
- ~~Four duplicated direction blocks~~ — one routine.
- ~~`PacmanFrame.java` / `PacManFrame` capitalization~~.
- ~~`.class` files committed~~ — `.gitignore` covers `*.class` and `build`.
- ~~One-tick stall at every L-corner~~ — `move()` reads `direction` *after* the queued turn is applied. **No regression test yet.**
- ~~`catch (Exception e) { // }`~~ — catches `InterruptedException`, restores the flag and exits.
- ~~Hidden `setMaze()` dependency on `PacMan`~~ — maze is a constructor argument.
- ~~Unused `guava` dependency~~.
- ~~Tests don't compile after the package move~~ — test moved into the package and updated (staged).
- ~~`mainClass` not fully qualified~~.
- ~~`PacMan` ignores its `size` argument~~ (staged).
- ~~`.idea` tracked; `.DS_Store` not ignored~~.
- ~~Dead `height`/`width` setters on `Maze`~~ (unstaged) — but the fields and constructor params are still dead.

### Still open
| Severity | Symptom | Where |
|---|---|---|
| **High** | **The test passes by accident.** When the constructors changed, the `setMaze(maze)` line was dropped, so `new Maze(3, 5)` uses the hardcoded 20×20 level, not the test's 3×5 fixture. It still passes only because row 1 of the real level happens to have a wall at column 0 too. The `maze` field in the test is dead. Root cause: `Maze`'s constructor *looks* like it configures the grid but doesn't. | `PacManTest`, `Maze` |
| High | **Data race between threads.** The game thread calls `move()` while the EDT paints and writes `queuedDirection` from `keyPressed`. No synchronization or `volatile`. | `PacManPanel` |
| Medium | `speed` field is ignored — movement is always 1 px per tick. Using it breaks the `% size == 0` alignment check for any speed that doesn't divide the tile size. | `PacMan` |
| Medium | No mid-tile reversal: pressing the opposite direction only takes effect at the next tile centre. | `PacMan.move()` |
| Medium | No bounds check on the maze lookup — tunnel wrap (Phase 5) will throw `ArrayIndexOutOfBoundsException`. | `PacMan.move()` |
| Medium | Movement speed depends on loop timing (`sleep(20)` + `repaint()` in one thread). | `PacManPanel.run()` |
| Medium | New image allocated every frame on top of Swing's own double buffering; `super.paintComponent` not called. | `PacManPanel.paintComponent()` |
| Low | `System.out.println` in `move()`, `paintComponent()` and `keyPressed()` (thread-name exercise) — remove before committing. | `PacMan`, `PacManPanel` |
| Low | `Maze` constructor takes `height`/`width` it never uses; `getMaze()`/`setMaze()` expose the internal array. Prefer `Maze(int[][] grid)` + `isWall(col, row)`. | `Maze` |
| Low | Size defined three times; preferred size passed as `(height, width)`. | several |
| Low | Magic numbers `20`, `400`, `1`, `2`, `7`, `5`. | everywhere |
| Low | Frame not created on the EDT; `setVisible(true)` before close op and background; no title. | `PacManFrame` |
| Low | README structure section and "corner pause" limitation out of date. | `README.md` |

---

## 6. Guiding principles

1. **Fix before feature.** No pellets or ghosts until movement is correct and tested.
2. **One representation.** Direction is a single value. ✅ Next: position should be one representation too (see Phase 2b).
3. **Model knows nothing about Swing.** `PacMan`, `Maze`, `Direction` must not import `java.awt`.
4. **Time is explicit.** Movement advances by elapsed time, not by "once per loop iteration".
5. **Test the rules, not the pixels.** Pure functions for walkability, scoring, win state, AI choice.
6. **Write the failing test first** for every bug you fix. *(The corner stall got fixed without one — backfill it.)*
7. **No toolkit migration** to solve a design problem.
8. **Keep every commit green.** A refactor (like a package move) isn't done until `./gradlew test run` passes.
9. **A test you've never seen fail hasn't proved anything.** After writing or changing a test, break the code (or the fixture) on purpose and watch it go red once. *(Added 2026-11-20 — the current test would have been caught this way.)*

---

## 7. Phases

### Phase 0 — Foundations — *done once the staged work is committed*

- [x] git repo, first commit is the 2015 code (the "before" photo).
- [x] `.gitignore` for `*.class`, `.gradle`, `build`, `.idea`, `.DS_Store`.
- [x] No `.class` files in the tree.
- [x] Gradle with a committed wrapper (`./gradlew`).
- [x] Standard layout (`app/src/main/java`, `app/src/test/java`).
- [x] Run task: `./gradlew run` launches `io.github.scott2100.pacman.PacManFrame`.
- [x] Move `PLAN.md` into the repo (`docs/PLAN.md`).
- [x] Add a package declaration (`io.github.scott2100.pacman`).
- [x] Remove the unused `guava` entry from `libs.versions.toml`.
- [x] Untrack `.idea`; ignore `.DS_Store`.
- [~] Finish the package move — test moved and updated (staged, not committed).
- [ ] Expand `README.md`: screenshot/GIF; refresh structure section and limitations.
- [ ] Commit the staged + unstaged work, then merge the feature branch when green.

**Acceptance:** a clean clone builds and runs with one documented command; plan and code live together.

### Phase 1 — Stabilize — *about two-thirds done*

- [x] Fix the null-direction crash.
- [x] Remove `static` from mutable fields in `PacMan` and `Maze`.
- [x] Delete dead imports, the `keys` array and debug prints. *(prints are back temporarily for the thread exercise)*
- [x] Fix the `PacmanFrame`/`PacManFrame` capitalization.
- [x] Stop swallowing exceptions in `run()` and restore the interrupt flag.
- [~] Remove the remaining dead state: `PacMan`'s ignored `size` ✅ (staged), `Maze` setters ✅ (unstaged). Still: `Maze.height/width` fields + constructor params, `PacManPanel`'s `public static` sizes.
- [ ] Replace magic numbers with named constants (tile size, speed, tile types). *Hint: tile types are a natural second enum.*
- [ ] Fix the swapped preferred-size arguments (better: compute size from maze dimensions × tile size).
- [ ] Start the UI on the EDT; set title, close op and background before `setVisible(true)`.

**Acceptance:** no `static` mutable state anywhere; no empty catch blocks; one place defines tile size.

### Phase 2 — Movement — *routine done, corner fixed*

Done:
- [x] Direction is an enum with a separate queued direction.
- [x] One routine decides turn / advance / stop.
- [x] Direction buffering (press early, turn at the corner) works.
- [x] Four duplicated blocks deleted; `move()` is ~20 lines.

2a — fix the routine:
- [x] **Fix the corner stall.** *(Still needs its regression test — Phase 3.)*
- [x] Inject the maze through `PacMan`'s constructor.
- [ ] **`Maze` takes its grid in the constructor** (`new Maze(grid)`) and drops `height`/`width`/`setMaze`. The 20×20 level moves out of `Maze` into whoever creates the game (for now `PacManPanel`, later a level file). *This is the quick target — see section 10.*
- [ ] Allow instant reversal mid-tile. *Hint: reversing is always legal because you came from that tile. Where in `move()` would the check go so it runs even when you're not on a tile centre?*
- [ ] Give `Maze` a query method (`isWall(col, row)`) and stop exposing the array — this is also where bounds/wrap handling will live. *Hint: once this exists, the two long `maze.getMaze()[…][…]` expressions in `move()` collapse to one readable line each.*

2b — make speed real:
- [ ] Use `speed`. *Hint: the `% size == 0` test only works because the step is 1 px. Options: (a) store tile + sub-tile offset instead of raw pixels, or (b) when a step would cross a tile centre, clamp to the centre, decide, then spend the leftover distance. Both are worth sketching on paper before coding.*

**Acceptance:** no stall at corners; reversal is instant; any speed works; each of these has a test.

### Phase 3 — Tests — *compiles again, but the one test is hollow*

- [x] JUnit 5 via Gradle.
- [x] Suite compiles in the new package (staged).
- [~] Moving into a wall stops on the last open tile — exists, but runs against the real level, not its fixture. Make it use its own maze and see it fail once (principle 9).
- [ ] Corner turn has no stall (regression test for the 2a fix).
- [ ] Buffered turn: queued direction is rejected into a wall and applied at the next open junction.
- [ ] Reversal mid-tile.
- [ ] Each of the four directions, with one parameterized test (`@ParameterizedTest` + `@EnumSource`).
- [ ] Speed > 1 still stops exactly on tile centres.
- [ ] Later (with Phase 5): pellet consumption → score; no pellets → win; tunnel wrap.
- [ ] Tidy the test class: local variables or `@BeforeEach`, not `public` fields; a tiny helper to build a maze from strings (`"#...#"`) makes fixtures readable.

**Acceptance:** a dozen focused tests, runnable with `./gradlew test`, none touching Swing.

### Phase 4 — Loop and rendering

*Before starting this phase, work through the learning track in section 13 — it explains why these changes matter.*

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
- [ ] Load the maze from a **text file** (resource) rather than a hardcoded array. *(Easy once `Maze` takes its grid in the constructor.)*
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
| Java version | **Decided: 21 (toolchain)** | Stated in the README ✅. |
| Test framework | **Decided: JUnit 5** | |
| Package name | **Decided: `io.github.scott2100.pacman`** | Classes are package-private — good; tests live in the same package ✅. |
| Plan location | **Decided: `docs/PLAN.md` in the repo** | Mirrored in the mentoring project. |
| Maze construction | **Decided (2026-11-20): `Maze(int[][] grid)`** | The level data lives outside `Maze`; `Maze` is just rules over a grid. Makes tests and level files trivial. |
| UI toolkit | Stay on Swing / Java2D | Unchanged. |
| TUI version | Rejected | Unchanged. |
| Tests | Narrow, logic-only | Unchanged. |
| Position representation | **Open (Phase 2b)** | Raw pixels vs. tile + offset. Decide before using `speed`. |
| Game loop threading | **Open (Phase 4)** | Recommend `javax.swing.Timer` over a raw thread. |

**Open questions**

- Is the goal a portfolio piece, a learning exercise, or just fun? *(The README says "for the craic"
  and the project brief says learning architecture with mentoring, so the plan favours clean structure
  and tests over art.)*

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

## 10. Suggested next steps

**Quick target (≈20–30 min): make the test honest, then commit green.**
1. Change `Maze` so its constructor takes the grid: `Maze(int[][] grid)`. Delete `height`, `width` and `setMaze`.
2. Move the 20×20 level array out of `Maze` to where the game is built (`PacManPanel` for now — e.g. a `private static final int[][] LEVEL_1`). *It's `static final` data, not mutable state, so that's fine.*
3. Let the compiler guide you: fix each call site it flags (panel and test). The test becomes `new Maze(maze)`.
4. Prove the test now tests something (principle 9): change the fixture so the wall is somewhere else, or change the expected value, run `./gradlew test`, watch it fail, then put it back.
5. Remove the debug prints (or keep them out of the commit), run `./gradlew test run`, and commit everything: "Maze takes its grid; test uses its own fixture".

Then:
6. **Backfill the corner-stall regression test** (L-shaped maze, queue UP before the corner, assert y decreases on the tick after reaching it). Bonus: write the reversal test, watch it fail, then fix it.
7. **Finish the thread exercises in section 13** and swap the raw thread for a `javax.swing.Timer`.

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
- GitHub Actions running `./gradlew test` on every push. *(Would have flagged the red `ad9a546` commit immediately.)*

## 12. Explicitly not worth it

- Networking/multiplayer.
- A map editor.
- 100% test coverage.
- Migrating frameworks before the current code is clean.

---

## 13. Learning track — threads and the Swing event loop

**Status:** started (thread-name prints are in the code). Finish before Phase 4.
**Why:** the data race in `PacManPanel` was found by reading the code, not by playing. The goal here is to be able to spot that kind of bug yourself.

### Key ideas (from the 2026-11-18 mentoring chat)

- **A `Runnable` is not a thread.** It's a recipe card: a `run()` method. A `Thread` is a worker that executes code. Objects don't belong to threads — any thread can call any object's methods.
- **The caller decides the thread, not the class.** Moving `keyPressed` into `PacMan` would not change which thread runs it; Swing would still call it from the EDT.
- **The EDT is a loop Swing runs.** It takes events off a queue (keys, mouse, resize, paint requests) and calls the matching methods. `addKeyListener` = observer pattern / callback ("don't call us, we'll call you"). `paintComponent` is a method Swing already calls; you override it.
- **`repaint()` doesn't paint.** It drops a "please repaint" note in the queue and returns. The EDT later calls `paintComponent`. That handoff is safe; reading `x`/`y` while another thread writes them is not.
- **Two threads + shared fields + no coordination = a data race.** Usually invisible on a desktop, which is what makes it dangerous.
- **Games prefer one thread, one loop:** read input → update → draw, in a fixed order. In Swing, `javax.swing.Timer` gives you that loop on the EDT.
- **How bugs get found:** logic bugs (the corner stall) by tests, slowing time down, logging per tick. Structural bugs (the race) by knowing the rules and listing which threads touch which fields — then removing the sharing rather than patching it.

### Exercises (small, do in order)

- [~] **See the threads.** Add `System.out.println(Thread.currentThread().getName())` to `move()`, `paintComponent()` and `keyPressed()`. Run and note which names appear where. *(prints added — record what you saw)*
- [ ] **See the event loop.** Put a breakpoint in `keyPressed`, run in the debugger, press a key, and read the call stack down to `EventDispatchThread.pumpEvents`.
- [ ] **Field/thread table.** For `PacMan` and `PacManPanel`, write a table: each mutable field → which threads write it → which threads read it. Mark every row with two threads.
- [ ] **Slow-motion mode.** Temporarily set the loop to 500 ms per tick (or add a step key) and watch the *reversal* gap.
- [ ] **Swap to `Timer`.** Replace the `Thread`/`Runnable`/`sleep` with `javax.swing.Timer`, rerun the thread-name exercise, and confirm everything prints the same EDT name.
- [ ] **Assert it stays fixed.** Add `assert SwingUtilities.isEventDispatchThread();` at the top of `move()` (run with `-ea`).

### Reading (pick one or two, no need for all)

- Oracle's Java Tutorial, Swing trail → **"Concurrency in Swing"** lesson (short, and the authoritative source for the EDT rule).
- Robert Nystrom, *Game Programming Patterns* (free online) → **"Game Loop"** chapter, and "Observer" for the listener idea.
- Optional, deeper: Brian Goetz et al., *Java Concurrency in Practice*, chapters 1–3 (visibility, sharing objects) and chapter 9 (GUI applications).

**Done when:** you can explain, without notes, which thread runs each method in `PacManPanel`, why the old version raced, and why the `Timer` version doesn't.

---

## Review log

- **2026-11-20 (code review)** — Reviewed `ad9a546` + staged/unstaged changes on `feature/testin_movement_refactor`.
  Done: `mainClass` fully qualified, test moved into the package and updated to the new constructors (staged), `PacMan` uses its
  `size` argument (staged), `Maze` setters removed (unstaged), `.idea` untracked, `.DS_Store` ignored. Phase 0 effectively complete.
  **New finding:** the test passes by accident — dropping `setMaze(maze)` during the constructor update left it running against
  the hardcoded 20×20 level, and its own fixture is dead. Root cause is `Maze`'s constructor taking `height`/`width` it ignores.
  Decided `Maze(int[][] grid)`; made it the quick target. Noted `ad9a546` was committed red. Added principle 9.
- **2026-11-18 (code review)** — Reviewed the working tree (last commit `0898748` + uncommitted package move).
  Done: corner stall fixed, interrupt handling fixed, maze injected via constructor, package added, plan moved to `docs/`,
  guava removed, README much improved, `.idea` ignored. Blockers: test suite didn't compile; `mainClass` not fully qualified.
  Added principle 8 ("keep every commit green").
- **2026-11-18** — Added section 13, a learning track on threads and the Swing event loop. Phase 4 now points to it.
- **2026-11-17** — Reviewed against the code. Phase 0 mostly done; Phase 1 about half done; Phase 2 routine done, two movement
  bugs found (corner stall, no reversal) and `speed` unused; Phase 3 started (1 test). Added Phase 2a/2b, a `Game` class in Phase 5.
- **2026-11-08** — First version.
