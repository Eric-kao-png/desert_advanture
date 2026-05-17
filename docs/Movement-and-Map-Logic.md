# Desert Adventure — Movement & Map Logic

Reference for map overlay, straight-line movement, step budget, mid-path tile encounters, and related implementation (LibGDX, Java 17, `core` module).

**Last aligned with implementation:** May 2026

---

## 1. Overview

Exploration uses a large square grid world (**501×501** cells) with world coordinates centered at **(0, 0)**. The player selects any walkable destination on a map overlay; movement follows a **straight line** through grid cell centers (Bresenham), animated in world space. Step cost is **Euclidean distance** (float). There is **no upfront step-budget check** when planning a move; steps are consumed progressively during travel. If the budget hits zero while moving, a **sandstorm** interrupts the trip. Interactive tiles (combat, items, events) can trigger when the player **enters their cell during travel**, not only at the final destination.

---

## 2. World & Coordinate Model

### 2.1 GameMap

- Class: `com.desertadventure.map.model.GameMap`
- `MAP_SIZE = 501` (`GameConfig.MAP_SIZE`); valid world coordinates: **-250 … +250** on X and Y
- Spawn / origin: **(0, 0)** — `TileType.SPAWN`
- Internal storage: `Tile[][]` indexed by world coord → array index via `minCoord = -(size/2)`
- `distanceBand(pos)`: Manhattan distance from origin divided by 3 (combat scaling)

### 2.2 GridPos

- Immutable integer pair `(x, y)` in world space
- Used for logical cell identity, path cells, and map queries

### 2.3 Player position vs display position

| Concept | Source |
|--------|--------|
| Logical grid | `playerX` / `playerY` in `GameSession` — authoritative after stop or pause |
| Float along path | `PathRunner` during `RUNNING` |
| `getDisplayGridPos()` | While `RUNNING` and `pathRunner.isRunning()`, `round(float)`; else player grid |
| `getDistanceFromOrigin()` | `hypot(x, y)` from float while running, else from player integers |
| HUD | `GameplayScreen.drawHud` — display tile + Euclidean distance |

---

## 3. Tile Types & Passability

### 3.1 TileType table

| Type | Walkable | Interactable | Notes |
|------|----------|--------------|-------|
| EMPTY | Yes | No | Default terrain |
| SPAWN | Yes | No | Camp center; no arrival interaction |
| BLOCKED | No | No | Obstacle; blocks path planning |
| ITEM | Yes | Yes | Collect once per cycle; then walkable |
| EVENT | Yes | Yes | Required ruins; cleared per cycle |
| COMBAT | Yes | Yes | Cleared after victory this cycle |
| BOSS_SUMMON | Yes | Yes | Boss gate when all required events done |

### 3.2 `blocksMovement()` vs path vs entry

- `isBlockedForPath` / `StraightLinePath`: `blocksMovement()` on every cell on the line **except start**
- `canEnter(destination)`: used when clicking the map
- Collected **ITEM** and cleared **COMBAT** tiles become passable

### 3.3 `needsInteractionOnArrival()`

- **False** for EMPTY, SPAWN, already-collected ITEM, cleared COMBAT/EVENT
- **True** for BOSS_SUMMON always (even if requirements fail — shows message)
- Otherwise **true** when `TileType.isInteractable()`
- Used at **end of move** and during **mid-path cell entry**

---

## 4. Gameplay Modes (Movement-Relevant)

| Mode | Role |
|------|------|
| `EXPLORE_IDLE` | Standing; **M** opens overlay |
| `MAP_OVERLAY` | 51×51 window; click destination; arrows pan; **Esc** closes |
| `RUNNING` | `PathRunner` animating; `updateRunning` consumes steps + encounters |
| `COMBAT` / `BOSS_COMBAT` | Movement paused; `activeMovePlan` may remain for resume |
| `STORM` | Step budget exhausted; cycle reset after fade |
| `TILE_INTERACTION` | Enum present but **unused** |
| `VICTORY` | Boss defeated |

---

## 5. Map Overlay UI

- `MapViewState`: `viewOriginX/Y` = bottom-left of 51×51 viewport in world coords
- `openMapOverlay()`: centers on player → `MAP_OVERLAY`
- `pan(dx, dy)`: `MAP_PAN_STEP = 8` tiles (arrow keys in `GameplayScreen`)
- `MapOverlayLayout`: `cellSize = 560/51`; **+Y up** on screen; `viewOriginY` is bottom row
- `MapOverlayInput` + `screenToGrid`: click/hover → `GridPos` (explored or not)
- Destination **does not** need to be explored-only

---

## 6. Path Planning — `StraightLinePath`

### 6.1 Algorithm

- **Bresenham** line from start cell center to destination (integer coords)
- `Plan.cellsOnLine`: ordered cells the line passes through
- `Plan.distance`: `hypot(dx, dy)` — step cost for the full trip
- Start cell skipped for blocker checks; all other cells must pass `isBlockedForPath`
- Returns `null` if OOB, end not enterable, start == end, or any blocker on line

### 6.2 Selection (`trySelectDestination`)

1. Only in `MAP_OVERLAY`
2. Reject if OOB or `!canEnter(destination)`
3. Same cell as player → close overlay, `EXPLORE_IDLE`
4. `StraightLinePath.plan(...)`; on failure: `"Straight path is blocked."`
5. On success: `RUNNING`, store `activeMovePlan`, reset step counters, `lastCellDuringMove = moveOriginCell = start`
6. `pathRunner.startStraightMove(start, dest, plan.distance, onComplete)`
7. **No** `canAfford` check before start

---

## 7. Movement Animation — `PathRunner`

- Linear interpolation: `start + (end - start) * progress`, `progress = elapsed / duration`
- `duration = max(0.15s, distance * TILE_TRAVEL_SECONDS)`; `TILE_TRAVEL_SECONDS = 0.4`
- On completion: `onPathComplete` → `onStraightMoveComplete`
- `pause()`: stops update, keeps elapsed/start/end (tile encounters)
- `resume()`: continues same segment (rare; resume usually starts a **new** segment)
- `cancel()`: sandstorm interrupt
- `SCROLL_SPEED`: parallax during `RUNNING` (visual only)

---

## 8. Step Budget

- `StepBudgetService`: budget from `PlayerStats.getTotalStepBudget()` (`BASE_STEP_BUDGET` + bonuses)
- `consumeSteps(float)`; `isExhausted` when `stepsUsed >= stepBudget`
- During `RUNNING` (`updateRunning`):
  - `segmentTravel = progress * pathRunner.getTotalDistance()`
  - `targetConsumed = moveStepBaseline + segmentTravel`
  - Consume delta vs `stepsAppliedForCurrentMove`
- `moveStepBaseline = 0` on new move; set to `stepsAppliedForCurrentMove` when resuming after pause
- On complete: `applyRemainingMoveStepCost` catches FP remainder vs `plan.distance`
- Exhausted mid-move: `interruptMoveForStorm` → snap position, cancel path, `STORM`
- Combat defeat also triggers storm

---

## 9. Fog of War / Exploration

- `permanentlyExplored`: `"x,y"` strings; not cleared per cycle
- `revealAround(center)`: center + 8 neighbors
- During move: at `getDisplayGridPos()` as steps accrue; on storm interrupt / spawn
- On move complete: `revealAround` for **every** cell on the line

---

## 10. Mid-Path Tile Encounters

### 10.1 Detection (`checkMidMoveTileEncounter`)

Each frame while `RUNNING`, after step consumption:

1. Compare `getDisplayGridPos()` to `lastCellDuringMove`; on change, update `lastCellDuringMove`
2. Skip if cell equals `moveOriginCell`
3. Skip if cell not in `activeMovePlan.getCellsOnLine()`
4. If `tile.needsInteractionOnArrival()` → `pauseMovementForInteraction()` → `handleTileInteraction(tile, true)`

### 10.2 Pause / resume

- **Pause:** save `pauseWorldX/Y`, `pathRunner.pause()`, `playerX/Y = round(pause)`, reveal fog
- `activeMovePlan` kept until complete or storm
- **Resume:** new segment `pauseWorldX/Y` → `plan.destination`; `remaining = hypot(...)`; `moveStepBaseline = stepsAppliedForCurrentMove`; new `startStraightMove`
- If `remaining < 0.01` → `onStraightMoveComplete`

### 10.3 Per tile type (`duringMove = true`)

| Tile | Behavior |
|------|----------|
| ITEM | Collect (+ATK, +step), message → `resumePausedMove` if plan active |
| EVENT | Complete event if applicable → resume |
| COMBAT | `COMBAT` — resume only after victory |
| BOSS_SUMMON | Boss fight if ready; else message → resume |
| Victory handler | `onCombatEnd("victory")`: clear tile; if `activeMovePlan != null` → `resumePausedMove` |

Destination-only: `onStraightMoveComplete` → `handleTileInteraction(tile, false)` (no auto-resume for combat until `onCombatEnd`).

---

## 11. Move Completion (`onStraightMoveComplete`)

1. Early return if `STORM`, `COMBAT`, or `BOSS_COMBAT`
2. `applyRemainingMoveStepCost(plan)`; clear plan and step counters
3. Set `playerX/Y` to destination
4. Reveal fog for all cells on line
5. If destination needs interaction → `handleTileInteraction(tile, false)`
6. Else `EXPLORE_IDLE`; if budget exhausted → `triggerStorm()`

---

## 12. Sandstorm (Cycle Reset)

- `triggerStorm`: mode `STORM`, timer 0
- `completeStorm` (after `STORM_FADE_SECONDS`): reset cycle map flags, spawn, step budget
- `interruptMoveForStorm`: cancel runner, clear plan, snap position, reveal, storm

---

## 13. Gameplay Loop (Screen)

`GameplayScreen.update` (simplified):

```
RUNNING:
  pathRunner.update(delta)
  session.updateRunning(delta)  // steps + mid-path encounters

COMBAT / BOSS_COMBAT:
  ensureCombatInitialized → CombatController.startCombat(distanceBand, ...)
  on end → session.onCombatEnd(result)
```

---

## 14. Key State Fields (`GameSession`)

| Field | Purpose |
|-------|---------|
| `activeMovePlan` | `StraightLinePath.Plan` for current trip; null when idle |
| `stepsAppliedForCurrentMove` | Steps already charged this trip |
| `moveStepBaseline` | Steps charged before current PathRunner segment |
| `moveOriginCell` | Start cell; ignored for mid-path triggers |
| `lastCellDuringMove` | Last grid cell for encounter detection |
| `pauseWorldX`, `pauseWorldY` | Float position when pausing |
| `playerX`, `playerY` | Logical position when not animating |

---

## 15. Configuration Constants

| Constant | Value / role |
|----------|----------------|
| `MAP_SIZE` | 501 |
| `MAP_VIEW_TILES` | 51 — overlay window |
| `MAP_PAN_STEP` | 8 — tiles per pan |
| `BASE_STEP_BUDGET` | 12 — base steps per cycle |
| `TILE_TRAVEL_SECONDS` | 0.4 — seconds per unit distance |
| `SCROLL_SPEED` | 200 — explore parallax |
| `STORM_FADE_SECONDS` | 2 — storm screen duration |

---

## 16. Source File Index

| File | Responsibility |
|------|----------------|
| `GameSession.java` | Select destination, run, pause, resume, storm, interactions |
| `StraightLinePath.java` | Bresenham planning and `Plan` |
| `PathRunner.java` | Straight-line animation, pause/cancel |
| `StepBudgetService.java` | Float step pool per cycle |
| `GameMap.java` | Grid, fog, passability |
| `Tile.java` / `TileType.java` | Per-cell rules |
| `MapViewState.java` | Overlay viewport origin |
| `MapOverlayLayout.java` | Screen ↔ grid, +Y up |
| `MapOverlayInput.java` | Hover cell |
| `GameplayScreen.java` | Input, HUD, update loop |
| `GameConfig.java` | Tunables |

Paths under `core/src/com/desertadventure/`.

---

## 17. Flow Summary

### 17.1 Happy path (no encounters)

`MAP_OVERLAY` → click dest → `RUNNING` → steps drain → arrive → `EXPLORE_IDLE` (or `STORM` if exhausted at end)

### 17.2 Mid-path combat

`RUNNING` → enter COMBAT cell → pause → `COMBAT` → victory → `resumePausedMove` → `RUNNING` → … → complete

### 17.3 Mid-path item

`RUNNING` → enter ITEM cell → pause → collect → `resumePausedMove` → `RUNNING` → …

### 17.4 Steps run out while moving

`RUNNING` → `isExhausted` → `interruptMoveForStorm` → `STORM` → spawn reset

---

## 18. Design Notes / Future Hooks

- Path is **straight Euclidean** through cell centers, not A* (obstacles block the whole line)
- Step cost = `plan.distance` (Euclidean), not Manhattan or per-cell count
- Bresenham list vs float lerp rounding may differ slightly for encounter timing; encounters use **rounded display cell** on the line list
- `GameplayMode.TILE_INTERACTION` is unused — consider removing or wiring UI
- Combat uses `distanceBand` from **player grid** at fight start, not float path position
