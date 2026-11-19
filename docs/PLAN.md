# Pac-Man — Modernization Plan

**Status:** in progress — Phase 0 nearly done, Phases 1–3 partly done (reviewed against the code 2026-11-18)
**Scope:** the Swing Pac-Man in `pacman/`
**Goal:** turn a working maze-walker into a small, clean, tested, recognizable Pac-Man — **without a rewrite**.

---

## 1. Purpose

This project works: it compiles, opens a window, draws a tile maze, and lets you steer a
yellow square around it. The 180 lines of four-way copy-paste movement are now one ~20-line
`move()` driven by a `Direction` enum, the project has Gradle, git, a package, a real README and
its first JUnit 5 test, and the corner stall is fixed.

What's left is the "second layer" of the foundation: a build that's currently broken by the
package move, a couple of movement gaps (reversal, speed), a game loop that shares state across
threads unsafely, rendering that still allocates per frame, and no game layer (pellets are drawn
but never eaten; no score, win or lose).

The order stays the same: fix the foundation, then add gameplay, then polish.

---

## 2. Goals

- One source of truth for Pac-Man's direction. ✅
- A single, general movement routine instead of four duplicated blocks. ✅
- Simulation decoupled from rendering and from frame rate.
- Game logic free of `java.awt` so it can be tested headless. ✅ (keep it that way)
- A reproducible build and a repo that looks like a real project. (nearly — build broken in working tree)
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

Repo: `pacman/` (remote `github.com/scott2100/pacman`), on branch `feature/testin_movement_refactor`.
Last commit `0898748` (2026-11-17, README + build + movement fixes). **Uncommitted:** sources moved
into package `io.github.scott2100.pacman`, `PLAN.md` moved to `docs/`, `.idea` added to `.gitignore`,
`guava` removed from the version catalog, `PacMan` constructor now takes the maze.

| File | Role | State |
|---|---|---|
| `app/src/main/java/io/github/scott2100/pacman/PacMan.java` | Entity + movement | 79 lines; maze via constructor; corner stall fixed; `size` arg ignored; debug `println` in `move()` |
| `…/Direction.java` | Value type | Enum with `dx`/`dy` and `STOPPED` — good |
| `…/Maze.java` | Tile data | Hardcoded 20×20; unused `height`/`width` (now with setters); `getMaze()`/`setMaze()` expose the array |
| `…/PacManPanel.java` | Render + input + loop | Raw `Thread` from the constructor; new image every frame; `public static` sizes; interrupt handled properly now |
| `…/PacManFrame.java` | Bootstrap | Not on the EDT; no title; `setVisible` before setup |
| `app/src/test/java/PacManTest.java` | Tests | **Does not compile**: still in the default package and calls the old constructors |
| `app/build.gradle.kts` | Build | **`mainClass` still `"PacManFrame"`** — needs the fully qualified name |
| `README.md` | Docs | Good: status, requirements, run/test, controls, structure, limitations, roadmap. Structure section and "corner pause" limitation now out of date; no screenshot yet |
| `docs/PLAN.md` | This file | In the repo now ✅ (keep in sync with the project copy) |

---

## 5. Known defects

### Fixed since the first plan
- ~~NPE on first Up or Left~~ — `Direction.STOPPED` is the default.
- ~~`static` position/direction in `PacMan` and `Maze`~~.
- ~~Direction tracked twice (booleans + strings)~~ — one enum.
- ~~Four duplicated direction blocks~~ — one routine.
- ~~`PacmanFrame.java` / `PacManFrame` capitalization~~.
- ~~`.class` files committed~~ — `.gitignore` covers `*.class` and `build`.
- ~~One-tick stall at every L-corner~~ — `move()` now reads `direction` *after* the queued turn is applied. (Verified 2026-11-18: at the corner, Pac-Man moves up on the very next tick.) **No regression test yet.**
- ~~`catch (Exception e) { // }`~~ — now catches `InterruptedException`, restores the flag and exits.
- ~~Hidden `setMaze()` dependency~~ — maze is a constructor argument (but `setMaze` still exists).
- ~~Unused `guava` dependency~~ (uncommitted).

### Still open
| Severity | Symptom | Where |
|---|---|---|
| **Blocker** | **Tests don't compile.** `PacManTest` is in the default package, so it can't see the package-private classes in `io.github.scott2100.pacman` (and Java can't import from the default package at all). It also calls `new Maze()` and `new PacMan(60, 20)`, which no longer exist. | `app/src/test/java/PacManTest.java` |
| **Blocker** | **`./gradlew run` will fail** with `ClassNotFoundException`: `mainClass` is still `"PacManFrame"`. | `app/build.gradle.kts` |
| High | **Data race between threads.** The game thread calls `move()` while the EDT paints and writes `queuedDirection` from `keyPressed`. No synchronization or `volatile`. | `PacManPanel` |
| Medium | `speed` field is ignored — movement is always 1 px per tick. Using it breaks the `% size == 0` alignment check for any speed that doesn't divide the tile size. | `PacMan` |
| Medium | No mid-tile reversal: pressing the opposite direction only takes effect at the next tile centre. (Verified: right 5 px, press left → keeps going right.) | `PacMan.move()` |
| Medium | No bounds check on the maze lookup — tunnel wrap (Phase 5) will throw `ArrayIndexOutOfBoundsException`. | `PacMan.move()` |
| Medium | Movement speed depends on loop timing (`sleep(20)` + `repaint()` in one thread). | `PacManPanel.run()` |
| Medium | New image allocated every frame on top of Swing's own double buffering; `super.paintComponent` not called. | `PacManPanel.paintComponent()` |
| Low | Constructor ignores its `size` argument (`this.size = 20`). Either use it or remove the parameter. | `PacMan` |
| Low | `System.out.println` in `move()`, `paintComponent()` and `keyPressed()` (from the thread-name exercise) — fine while learning, remove before committing. | `PacMan`, `PacManPanel` |
| Low | `Maze.getMaze()`/`setMaze()` expose the internal array; `height`/`width` are unused and gained setters. Prefer `isWall(col, row)` and a constructor that takes the grid. | `Maze` |
| Low | Size defined three times and preferred size still passed as `(height, width)`. | several |
| Low | Magic numbers `20`, `400`, `1`, `2`, `7`, `5`. | everywhere |
| Low | Frame not created on the EDT; `setVisible(true)` before close op and background; no title. | `PacManFrame` |
| Low | `.idea` is now in `.gitignore`, but its five files are still tracked (`git rm -r --cached .idea`). `.DS_Store` files are untracked and not ignored. | repo |

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

---

## 7. Phases

### Phase 0 — Foundations — *nearly done*

- [x] git repo, first commit is the 2015 code (the "before" photo).
- [x] `.gitignore` for `*.class`, `.gradle`, `build`.
- [x] No `.class` files in the tree.
- [x] Gradle with a committed wrapper (`./gradlew`).
- [x] Standard layout (`app/src/main/java`, `app/src/test/java`).
- [x] Run task: `./gradlew run` launches `PacManFrame`. *(Broken again by the package move — see below.)*
- [x] Move `PLAN.md` into the repo (`docs/PLAN.md`). *(uncommitted)*
- [x] Add a package declaration (`io.github.scott2100.pacman`). *(uncommitted)*
- [x] Remove the unused `guava` entry from `libs.versions.toml`. *(uncommitted)*
- [x] Expand `README.md`. Still to do: screenshot/GIF; refresh structure section and limitations.
- [ ] **Finish the package move:** `mainClass.set("io.github.scott2100.pacman.PacManFrame")`; move the test to `app/src/test/java/io/github/scott2100/pacman/` with a matching `package` line; update it to the new constructors.
- [ ] Untrack `.idea` (`git rm -r --cached .idea`) and add `.DS_Store` to `.gitignore`.
- [ ] Commit with `git add -A` so git records the moves as renames, then merge the feature branch when green.

**Acceptance:** a clean clone builds and runs with one documented command; plan and code live together.

### Phase 1 — Stabilize — *about two-thirds done*

- [x] Fix the null-direction crash.
- [x] Remove `static` from mutable fields in `PacMan` and `Maze`.
- [x] Delete dead imports, the `keys` array and debug prints. *(prints are back temporarily for the thread exercise)*
- [x] Fix the `PacmanFrame`/`PacManFrame` capitalization.
- [x] Stop swallowing exceptions in `run()` and restore the interrupt flag.
- [ ] Remove the remaining dead state: `Maze.height/width` (+ setters), `PacManPanel`'s `public static` sizes, `PacMan`'s ignored `size` argument.
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
- [x] **Fix the corner stall.** *(Fixed; still needs its regression test — Phase 3.)*
- [ ] Allow instant reversal mid-tile. *Hint: reversing is always legal because you came from that tile. Where in `move()` would the check go so it runs even when you're not on a tile centre?*
- [~] Inject the maze through the constructor — done; now drop `setMaze`.
- [ ] Give `Maze` a query method (`isWall(col, row)`) and stop exposing the array — this is also where bounds/wrap handling will live. *Hint: once this exists, the two long `maze.getMaze()[…][…]` expressions in `move()` collapse to one readable line each.*

2b — make speed real:
- [ ] Use `speed`. *Hint: the `% size == 0` test only works because the step is 1 px. Options: (a) store tile + sub-tile offset instead of raw pixels, or (b) when a step would cross a tile centre, clamp to the centre, decide, then spend the leftover distance. Both are worth sketching on paper before coding.*

**Acceptance:** no stall at corners; reversal is instant; any speed works; each of these has a test.

### Phase 3 — Tests — *started, currently not compiling*

- [x] JUnit 5 via Gradle.
- [x] Moving into a wall stops on the last open tile.
- [ ] **Get the suite compiling again** (package + constructors — see Phase 0).
- [ ] Corner turn has no stall (regression test for the 2a fix).
- [ ] Buffered turn: queued direction is rejected into a wall and applied at the next open junction.
- [ ] Reversal mid-tile.
- [ ] Each of the four directions, with one parameterized test (`@ParameterizedTest` + `@EnumSource`).
- [ ] Speed > 1 still stops exactly on tile centres.
- [ ] Later (with Phase 5): pellet consumption → score; no pellets → win; tunnel wrap.
- [ ] Tidy the test class: fields set in `@BeforeEach` (or local variables), not `public` fields; a tiny helper to build a maze from strings makes fixtures readable.

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
| Java version | **Decided: 21 (toolchain)** | Stated in the README ✅. |
| Test framework | **Decided: JUnit 5** | |
| Package name | **Decided: `io.github.scott2100.pacman`** | Classes are package-private — good; tests must live in the same package. |
| Plan location | **Decided: `docs/PLAN.md` in the repo** | Mirrored in the mentoring project. |
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

## 10. Suggested next three steps

1. **Make the build green again:** fully qualified `mainClass`; move `PacManTest` into the package and update it to the new constructors; run `./gradlew test run`. Then untrack `.idea`, ignore `.DS_Store`, and commit with `git add -A` ("Move sources into io.github.scott2100.pacman; move plan to docs/").
2. **Backfill the corner-stall regression test** (L-shaped maze, queue UP before the corner, assert y decreases on the tick after reaching it). Bonus: write the reversal test, watch it fail, then fix it.
3. **Finish the thread exercises in section 13**, remove the debug prints, and swap the raw thread for a `javax.swing.Timer`.

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
- GitHub Actions running `./gradlew test` on every push. *(Would have caught today's broken test immediately.)*

## 12. Explicitly not worth it

- Networking/multiplayer.
- A map editor.
- 100% test coverage.
- Migrating frameworks before the current code is clean.

---

## 13. Learning track — threads and the Swing event loop

**Status:** started (thread-name prints are in the code as of 2026-11-18). Finish before Phase 4.
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
- [ ] **Slow-motion mode.** Temporarily set the loop to 500 ms per tick (or add a step key) and watch Pac-Man take a corner — now that the stall is fixed, use it to watch the *reversal* gap instead.
- [ ] **Swap to `Timer`.** Replace the `Thread`/`Runnable`/`sleep` with `javax.swing.Timer`, rerun the thread-name exercise, and confirm everything prints the same EDT name.
- [ ] **Assert it stays fixed.** Add `assert SwingUtilities.isEventDispatchThread();` at the top of `move()` (run with `-ea`).

### Reading (pick one or two, no need for all)

- Oracle's Java Tutorial, Swing trail → **"Concurrency in Swing"** lesson (short, and the authoritative source for the EDT rule).
- Robert Nystrom, *Game Programming Patterns* (free online) → **"Game Loop"** chapter, and "Observer" for the listener idea.
- Optional, deeper: Brian Goetz et al., *Java Concurrency in Practice*, chapters 1–3 (visibility, sharing objects) and chapter 9 (GUI applications).

**Done when:** you can explain, without notes, which thread runs each method in `PacManPanel`, why the old version raced, and why the `Timer` version doesn't.

---

## Review log

- **2026-11-18 (code review)** — Reviewed the working tree on `feature/testin_movement_refactor` (last commit `0898748` + uncommitted package move).
  Done: corner stall fixed (verified by running `move()` headless), interrupt handling fixed, maze injected via constructor,
  package added, plan moved to `docs/`, guava removed, README much improved, `.idea` ignored.
  **Blockers:** test suite doesn't compile after the package/constructor changes; `mainClass` not fully qualified.
  New low-severity items: `PacMan` ignores its `size` argument; `Maze` gained setters for dead fields; `.idea` still tracked;
  `.DS_Store` not ignored. Still open: reversal, speed, bounds, data race, per-frame allocation.
  Added principle 8 ("keep every commit green").
- **2026-11-18** — Added section 13, a learning track on threads and the Swing event loop (notes from the mentoring chat, exercises, reading). Phase 4 now points to it.
- **2026-11-17** — Reviewed against the code on `feature/testin_movement_refactor` (+ uncommitted changes).
  Phase 0 mostly done; Phase 1 about half done; Phase 2 routine done, two movement bugs found (corner stall, no reversal) and `speed` unused;
  Phase 3 started (1 test). New defects: thread data race, hidden `setMaze` dependency, exposed maze array,
  frame not on the EDT. Added Phase 2a/2b, a suggested `Game` class in Phase 5, and next steps.
- **2026-11-08** — First version.
