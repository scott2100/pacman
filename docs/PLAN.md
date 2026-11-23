# Pac-Man — Modernization Plan

**Status:** in progress. `main` is **green** at `41cfd80`. `feature/reversal-mid-tile` (`ad31099`, pushed, not merged) now has
**uncommitted** work on top: speed via sub-stepping, `Tiles.java` → `Tile.java`, debug print removed from `move()`, README
refreshed, a new speed test. Phases 1–3 partly done. *(Reviewed 2026-11-22, evening.)*
**Scope:** the Swing Pac-Man in `pacman/`
**Goal:** turn a working maze-walker into a small, clean, tested, recognizable Pac-Man — **without a rewrite**.

---

## 1. Purpose

This project works: it compiles, opens a window, draws a tile maze, and lets you steer a yellow square around it.
Movement is one short `move()` driven by a `Direction` enum; the project has Gradle, git, a package, a README and
five JUnit 5 tests on a non-square fixture. `Maze` owns a private grid behind query methods, tile types are an
enum, the corner stall is fixed, Pac-Man reverses instantly mid-tile, and `speed` is now honoured by sub-stepping.

What's left is the "second layer" of the foundation: a game loop that shares state across threads unsafely,
rendering that still allocates per frame, and no game layer (pellets are drawn but never eaten; no score, win or lose).

The order stays the same: fix the foundation, then add gameplay, then polish.

---

## 2. Goals

- One source of truth for Pac-Man's direction. ✅
- A single, general movement routine instead of four duplicated blocks. ✅
- Any integer speed works without skipping tile centres. ✅ (uncommitted — and the test doesn't prove it yet, see 2b)
- Simulation decoupled from rendering and from frame rate.
- Game logic free of `java.awt` so it can be tested headless. ✅ (keep it that way)
- A reproducible build and a repo that looks like a real project. ✅ — `main` must stay green (principle 8).
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

Repo: `pacman/` (local `~/projects/java/pacmanold/pacman`, remote `github.com/scott2100/pacman`).

- `main` = `41cfd80` "Refactoring" (2026-11-21) — `Maze` encapsulation + `Tile` enum. **Compiles.** In sync with origin.
- `feature/reversal-mid-tile` = `ad31099` (2026-11-22) — reversal + two tests. Pushed, **not merged**. Checked out, with
  uncommitted changes (below). Two features now share one branch — see step 1 in section 10.
- Stale branches: `refactoring` (local, at `22cb205`) and `feature/testin_movement_refactor` (merged) — delete.
- No `.github/workflows/` yet.

Uncommitted on the working tree:
- `PacMan.move()` wraps the 1-px step in `for (i < speed)` — **sub-stepping** (Phase 2b, option 1). Debug print removed.
- `PacMan` fields `speed`/`size` no longer have inline defaults; the constructor sets them.
- `PacManPanel` creates Pac-Man at speed **3**.
- `Tiles.java` deleted, `Tile.java` untracked → `git add -A` and git will record it as a rename.
- New test `speedThreeMovePacmanThreePixelsPerTick`; README status/structure/limitations refreshed.

| File | Role | State |
|---|---|---|
| `…/PacMan.java` | Entity + movement | Reversal ✅, sub-stepping ✅. Still four `getAdjustedCoordinate(...)` calls and two long `isWall(...)` conditions; the loop body is now the natural "one-pixel step" — a method waiting to be extracted. `maze`/`size` could be `final`. |
| `…/Direction.java` | Value type | `opposite()` as an exhaustive `switch` expression ✅. |
| `…/Maze.java` | Tile rules | `private final` grid + queries ✅. No defensive copy; no bounds check. `(row, col)` ✅. |
| `…/Tile.java` | Tile types | ✅ file name now matches the enum (uncommitted). |
| `…/PacManPanel.java` | Render + input + loop + level data | Level as `private static int[][]` (not `final`, shared with `Maze`); raw `Thread`; new image every frame; `public static` sizes; `Dimension(height, width)` passed to `createImage`/`fillRect` swapped (hidden because both are 400); debug prints in `paintComponent` (50×/s) and `keyPressed`. |
| `…/PacManFrame.java` | Bootstrap | Close op before `setVisible` ✅; not on the EDT; no title. |
| `…/PacManTest.java` | Tests | 5 tests, 6×5 non-square fixture ✅. The new speed test passes — but read Phase 2b's review note. |
| `README.md` | Docs | Refreshed (uncommitted). Still says speed isn't used; no screenshot. |
| `docs/PLAN.md` | Repo copy | Synced with this version 2026-11-22. |

---

## 5. Known defects

### Fixed since the first plan
- ~~NPE on first Up or Left~~ · ~~`static` position/direction~~ · ~~Direction tracked twice~~ · ~~Four duplicated direction blocks~~
- ~~`PacmanFrame` capitalization~~ · ~~`.class` files committed~~ · ~~Swallowed exceptions in `run()`~~ · ~~Hidden `setMaze()`~~
- ~~One-tick stall at every L-corner~~ — **no regression test yet.**
- ~~Unused `guava`~~ · ~~Tests don't compile after the package move~~ · ~~`mainClass` not fully qualified~~ · ~~`PacMan` ignores `size`~~
- ~~`.idea` tracked; `.DS_Store` not ignored~~ · ~~Test passes by accident~~ · ~~`Maze` ignores `height`/`width`~~ · ~~`setVisible` before close op~~
- ~~`Maze` exposes its array~~ · ~~Magic `1`/`2` tile values~~ · ~~`main` doesn't compile~~
- ~~No mid-tile reversal~~ — `ad31099`, with a test. Merge pending.
- ~~`Tile` enum lives in `Tiles.java`~~ — renamed (uncommitted).
- ~~`speed` field is ignored~~ — sub-stepping (uncommitted). Reviewer traced speed 3 into a wall from x=20 and x=40: stops on 60 exactly.
- ~~Debug `println` in `move()`~~ (uncommitted).

### Still open
| Severity | Symptom | Where |
|---|---|---|
| High | **Data race between threads** (`move()` on the game thread; paint and `keyPressed` on the EDT). | `PacManPanel` |
| Medium | Speed test doesn't test the overshoot bug it exists for (see Phase 2b). | `PacManTest` |
| Medium | No CI — `main` was red twice before; nothing stops a third. | repo |
| Medium | No defensive copy: `Maze` shares the panel's `static` array (eating pellets later would mutate the template). | `Maze`, `PacManPanel` |
| Medium | No bounds check in `Maze` — tunnel wrap will throw. | `Maze` |
| Medium | Movement speed depends on loop timing (`sleep(20)`). | `PacManPanel.run()` |
| Medium | New image allocated every frame; `super.paintComponent` not called. | `PacManPanel.paintComponent()` |
| Low | Debug `println` in `paintComponent()` and `keyPressed()`. | `PacManPanel` |
| Low | Level data lives in the view class. Moves to a text resource in Phase 5. | `PacManPanel` |
| Low | Size defined three times; `(height, width)` order swapped; magic `20`, `400`, `7`, `5`, speed `3`. | several |
| Low | Frame not created on the EDT; no title. | `PacManFrame` |

---

## 6. Guiding principles

1. **Fix before feature.** No pellets or ghosts until movement is correct and tested.
2. **One representation.** Direction is a single value. ✅ Next: position (revisit when fractional speeds arrive).
3. **Model knows nothing about Swing.** `PacMan`, `Maze`, `Direction`, `Tile` must not import `java.awt`.
4. **Time is explicit.** Movement advances by elapsed time, not by "once per loop iteration".
5. **Test the rules, not the pixels.**
6. **Write the failing test first** for every bug you fix. *(The corner stall still needs its backfilled test.)*
7. **No toolkit migration** to solve a design problem.
8. **Keep every commit green.** *(Broken twice — `ad9a546`, `68c2c09`. Make a machine check it: CI.)*
9. **A test you've never seen fail hasn't proved anything.**
10. **Data is not code.** Level layouts, tuning numbers and spawn points belong in data files.
11. **Make conditions read like sentences.** Name them, read them aloud, keep one argument order (`row, col`) everywhere.
12. **Separate "what happens" from "what it looks like".** Skipped pixels in the *simulation* are a logic problem, fixed in
    `move()`; smooth motion on *screen* is a rendering problem (interpolation), fixed in Phase 4.
13. **A test must fail for the bug it's named after.** *(Added 2026-11-22, evening.)* Seeing it red isn't enough (principle 9)
    — it has to be red *for the right reason*. Ask: "what's the simplest wrong implementation that would still pass this?"
14. **One branch, one idea; one commit, one reason.** *(Added 2026-11-22, evening.)* Small, single-purpose commits make
    `git log` a story, reviews quick and reverts safe.

---

## 7. Phases

### Phase 0 — Foundations — *done except CI and README*

- [x] git, `.gitignore`, Gradle wrapper, standard layout, run task, package, `PLAN.md` in repo, guava removed, `.idea` untracked.
- [x] Finish the package move; merge the feature branch.
- [x] Commit the working tree so `main` compiles again (`41cfd80`).
- [ ] **GitHub Actions: `./gradlew build` on every push and PR**; then protect `main` (require the check before merging).
- [ ] Delete merged/stale branches (`refactoring`, `feature/testin_movement_refactor`).
- [~] `README.md`: structure section refreshed ✅ (uncommitted); screenshot/GIF still to do.

**Acceptance:** a clean clone builds and runs with one documented command; CI is green on `main`.

### Phase 1 — Stabilize — *mostly done*

- [x] Null-direction crash, `static` mutable fields, dead imports, capitalization, swallowed exceptions, dead state.
- [~] Magic numbers → named constants. `Tile` enum + file ✅. Still: tile size, speed, `7`/`5`, `400`.
- [ ] Remove `PacManPanel`'s `public static` sizes; compute preferred size from `maze.width()/height()` × tile size (fixes the swapped order too).
- [ ] Start the UI on the EDT; set title.

**Acceptance:** no `static` mutable state anywhere; no empty catch blocks; one place defines tile size.

### Phase 2 — Movement

Done:
- [x] Direction enum + queued direction; one routine; buffering; four blocks deleted.
- [x] Fix the corner stall. *(regression test still owed)*
- [x] Inject the maze through `PacMan`'s constructor; `Maze` takes its grid; `private final` grid + query methods.
- [x] Instant reversal mid-tile (`ad31099`). Merge it.
- [x] **Use `speed`** — sub-stepping (uncommitted). Logic verified by hand-trace and a harness.

2a — remaining tidy-ups:
- [ ] Defensive copy in `Maze`'s constructor; bounds handling in `isWall`.
- [ ] Readability pass on `move()`. Now that it's `for (speed) { …one pixel… }`, the loop body *is* a concept. *Hint:* what
      would you name it? Once extracted, look at what's left in each half and see whether `currentRow()`/`currentCol()`,
      `isCentredOnTile()` and `canMove(Direction)` fall out on their own.

2b — make speed real — ***nearly done***:
- [x] Sub-stepping implemented.
- [ ] **Strengthen the test (review note, 2026-11-22).** `speedThreeMovePacmanThreePixelsPerTick` asserts x = 55 after 5 ticks
      from x = 40. It went red before the fix (it was 45), so it proves `speed` is *used*. But a naive `x += dx * speed` —
      the version that walks into walls — also gives exactly 55. So it doesn't yet prove the bug this phase is about is gone.
      *Hint:* where on this fixture does a 3-px stride fail to land on a centre? Pick a start and tick count where the naive
      version and the sub-stepping version give **different** answers, assert the right one, and check the naive version
      would fail it (temporarily swap it in — principle 13).
- [ ] *Bonus question to answer in your own words:* why do the four 1-px tests still pass unchanged with sub-stepping?

Later — fractional speeds (e.g. 1.5 px/tick, for arcade speed levels and frame-rate independence) are where sub-stepping
stops being enough. Revisit "Position representation" (section 8) then.

**Acceptance:** no stall at corners; reversal is instant; any speed works; each of these has a test that would fail for its bug.

### Phase 3 — Tests — *five tests*

- [x] JUnit 5 via Gradle.
- [x] Moving into a wall stops on the last open tile (left and up).
- [x] Reversal mid-tile.
- [x] No 90° turn when not centred.
- [x] Non-square fixture (6×5).
- [~] Speed 3 — test exists; needs to target the overshoot (Phase 2b).
- [ ] Corner turn has no stall: L-shaped path, queue the turn early, run an *exact* number of ticks, assert the exact coordinate.
- [ ] Buffered turn: rejected into a wall, applied at the next junction.
- [ ] Each of the four directions, one `@ParameterizedTest` + `@EnumSource`.
- [ ] `Maze` unit tests: `isWall`, `width`/`height`.
- [ ] Later: pellet → score; no pellets → win; tunnel wrap.
- [ ] Tidy: consistent naming (`test…` vs. not — pick one), `@DisplayName`s; a maze-from-strings helper (becomes the Phase 5 level parser).

**Acceptance:** a dozen focused tests, runnable with `./gradlew test`, none touching Swing.

### Phase 4 — Loop and rendering

*Before starting, work through section 13.*

- [ ] Fix the data race: `javax.swing.Timer`.
- [ ] Fixed-timestep update; speed independent of machine. **Render interpolation lives here.**
- [ ] Stop allocating an image per frame; call `super.paintComponent`; draw straight to `g`.
- [ ] Optional: cache the static wall layer in a `BufferedImage`.
- [ ] Scale factor; resizable window.
- [ ] Title, centred, clean shutdown.
- [ ] Pause (space) and restart (R). *Hint: `Map<Integer, Direction>` or Key Bindings.*

**Acceptance:** same speed on a slow and a fast machine; no shared mutable state across threads; no allocation in the loop.

### Phase 5 — Make it a game

- [ ] **Level from a text resource** (`src/main/resources/levels/level1.txt`; `#` wall, `.` pellet, space, `P` spawn, later `G`, `o`).
- [ ] Pellets consumed on contact (`maze.eatPellet(...)`) and counted (`pelletsRemaining()`).
- [ ] Score, with a HUD.
- [ ] Win state when no pellets remain.
- [ ] Tunnel wrap (in `Maze`).
- [ ] Persist a high score.
- [ ] *Architecture step:* a `Game`/`World` class owning `Maze`, `PacMan`, score and state, with `update()`.

**Acceptance:** you can finish a level and see a win screen; the maze can be edited without recompiling.

### Phase 6 — Art pass, staying on Swing

- [ ] Anti-aliasing; arc Pac-Man with animated mouth; ghosts; connected walls; HUD font; optional sprites and sound.

**Acceptance:** a screenshot reads as Pac-Man without a caption.

### Phase 7 — One ghost

- [ ] Ghost reusing the movement routine (pull grid movement into its own class — composition over inheritance).
- [ ] Greedy chase at junctions; contact = lose; optional modes, power pellets, lives.

**Acceptance:** the ghost reliably hunts; losing is possible.

---

## 8. Decisions and open questions

| Decision | Status | Notes |
|---|---|---|
| Build tool | **Decided: Gradle 9.8 + wrapper** | |
| Java version | **Decided: 21** | |
| Test framework | **Decided: JUnit 5** | |
| Package name | **Decided: `io.github.scott2100.pacman`** | |
| Plan location | **Decided: `docs/PLAN.md` in the repo** | Mirrored in the mentoring project. |
| Maze API | **Decided & done: private grid + query methods** | Later: `tileAt`, `eatPellet`, `pelletsRemaining`. No `getGrid()`. |
| Tile types | **Decided & done: `Tile` enum with `EMPTY`, in `Tile.java`** | |
| Level data format | **Decided: ASCII text resource** | |
| Reversal | **Decided & done** | `Direction.opposite()`, checked outside the tile-centre test. |
| Speed / position representation | **Decided for now: sub-stepping on integer pixels** | Smallest change, obviously correct. Revisit (clamp-to-centre or tile + offset) when fractional speeds arrive. |
| CI | **Decided: GitHub Actions, `./gradlew build`** | Not yet added. |
| Branch workflow | **In use: feature branch → PR → merge** | One feature per branch (principle 14). Add CI as a required check. |
| UI toolkit | Stay on Swing / Java2D | |
| Game loop threading | **Open (Phase 4)** | Recommend `javax.swing.Timer`. |

---

## 9. Definition of done

- Clean clone → one command → playable; CI green on `main`.
- No `static` mutable game state.
- One movement routine, shared by Pac-Man and ghosts.
- Game logic independent of Swing and covered by a small test suite.
- Speed independent of frame rate.
- Pellets, score, win, lose, restart.
- Levels loaded from data files.
- A screenshot that looks like Pac-Man; README with screenshot, run instructions, honest limitations.

---

## 10. Suggested next steps

**1. Untangle and land the two features (≈15 min).** Reversal is pushed; speed is sitting uncommitted on the same branch.
*Think about:* what should `git log` on `main` look like afterwards? Options, simplest first — (a) commit the rename +
README as one commit and speed as another on this branch, then PR; (b) `git stash`, merge reversal alone, then branch
`feature/speed` from the new `main` and `git stash pop`. Either is fine; (b) practises principle 14. Run `./gradlew test` before each commit.

**2. Make the speed test bite (≈15 min).** Phase 2b review note. Prove it by swapping in the naive version and watching it go red.

**3. CI (≈10 min).** One workflow file running `./gradlew build`; required check on `main`.

**4. Extract the one-pixel step (≈20 min).** Phase 2a hint. All tests must stay green without edits — that's your safety net
doing its job.

Then: corner-stall regression test → buffered-turn test → defensive copy + bounds → thread exercises (section 13) → `javax.swing.Timer`.

---

## 11. Backlog — cheap wins not yet scheduled

- Sound effects (`javax.sound.sampled`).
- Multiple levels (trivial once levels are files).
- Bonus fruit; lives + death animation; score popups.
- Fullscreen toggle.
- Config file for constants.
- A second renderer (headless text/PNG) as proof the model is view-independent.

## 12. Explicitly not worth it

- Networking/multiplayer; a map editor; 100% coverage; framework migration before the code is clean.
- Optimizing the tile-drawing loop (400 tiles is nothing — measure first). Dirty-rectangle rendering.
- Optimizing sub-stepping (≤ a few iterations per tick is free).

---

## 13. Learning track — threads and the Swing event loop

**Status:** started (thread-name prints still in `PacManPanel`). Finish before Phase 4.

### Key ideas (from the 2026-11-18 mentoring chat)

- **A `Runnable` is not a thread.** Any thread can call any object's methods.
- **The caller decides the thread, not the class.**
- **The EDT is a loop Swing runs** (observer pattern).
- **`repaint()` doesn't paint** — it queues a request.
- **Two threads + shared fields + no coordination = a data race.**
- **Games prefer one thread, one loop:** input → update → draw. In Swing, `javax.swing.Timer`.
- **How bugs get found:** logic bugs by tests and slow motion; structural bugs by knowing the rules.

### Exercises

- [~] **See the threads.** `move()`'s print is gone; the panel's two remain. Write down which thread name each printed, then remove them.
- [ ] **See the event loop.** Breakpoint in `keyPressed`; read the stack to `EventDispatchThread.pumpEvents`.
- [ ] **Field/thread table.** For every field `move()` touches: who writes it, who reads it, on which thread.
- [ ] **Slow-motion mode** (500 ms/tick) — great for *seeing* sub-stepping vs. the naive stride at speed 3.
- [ ] **Swap to `Timer`**; rerun the thread-name exercise.
- [ ] **Assert it stays fixed** (`assert SwingUtilities.isEventDispatchThread();`, run with `-ea`).

### Reading

- Oracle Java Tutorial → "Concurrency in Swing".
- Nystrom, *Game Programming Patterns* → "Game Loop", "Observer", "Update Method".
- Glenn Fiedler, "Fix Your Timestep!" (Phase 4 — this is where interpolation belongs).
- Optional: *Java Concurrency in Practice*, ch. 1–3, 9.

**Done when:** you can explain which thread runs each method in `PacManPanel`, why the old version raced, and why the `Timer` version doesn't.

---

## Review log

- **2026-11-22 (evening)** — Sub-stepping implemented in `move()` (uncommitted on `feature/reversal-mid-tile`); game runs at
  speed 3. Reviewer re-ran the logic in a harness: all 5 tests pass, and speed 3 into a wall from x=20 and x=40 stops on 60
  exactly. But the new speed test can't tell sub-stepping from the naive stride (both give 55) — hint given, not the answer.
  `Tiles.java` → `Tile.java` done. Debug print out of `move()`, still in the panel. Two features now on one branch.
  Added principles 13 and 14. Position representation decided for now: sub-stepping. Next: land the work, sharpen the test, CI.
- **2026-11-22** — `main` green (`41cfd80`). Reversal on `feature/reversal-mid-tile` (`ad31099`). Two new tests. Speed discussed:
  the bug is skipping the tile-centre *event*, not rendering. Hint given: sub-stepping. Added principle 12.
- **2026-11-21 (evening)** — `Maze` encapsulated; three row/col or negation bugs found and fixed; added principle 11.
- **2026-11-21 (code review)** — `main` red after PR #1 merge; CI promoted to Phase 0. Decided ASCII level resource, reversal via
  `Direction.opposite()`, CI. Added principle 10.
- **2026-11-20** — Test passes by accident; decided `Maze(int[][] grid)`; added principle 9.
- **2026-11-18** — Corner stall fixed; added principle 8; added section 13.
- **2026-11-17** — Added Phase 2a/2b, a `Game` class in Phase 5.
- **2026-11-08** — First version.
