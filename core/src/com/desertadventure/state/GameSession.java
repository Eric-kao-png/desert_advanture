package com.desertadventure.state;

import com.desertadventure.combat.system.CombatController;
import com.desertadventure.map.model.MapGenerator;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.event.RequiredEventTracker;
import com.desertadventure.exploration.PathRunner;
import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.exploration.StormResetService;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.StraightLinePath;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.model.TileType;
import com.desertadventure.player.PlayerStats;

public class GameSession {
    private final GameMap map;
    private final PlayerStats playerStats = new PlayerStats();
    private final PermanentProgress permanentProgress = new PermanentProgress();
    private final RequiredEventTracker eventTracker;
    private final StepBudgetService stepBudget = new StepBudgetService();
    private final StormResetService stormReset = new StormResetService();
    private final PathRunner pathRunner = new PathRunner();
    private final CombatController combatController;
    private final MapViewState mapViewState = new MapViewState();

    private int playerX;
    private int playerY;
    private GameplayMode mode = GameplayMode.EXPLORE_IDLE;
    private String pendingMessage;
    private float stormTimer;
    private float scrollOffset;
    private boolean bossAvailableThisCycle;
    private StraightLinePath.Plan activeMovePlan;
    private float stepsAppliedForCurrentMove;
    private float moveStepBaseline;
    private GridPos lastCellDuringMove;
    private GridPos moveOriginCell;
    private float pauseWorldX;
    private float pauseWorldY;

    public GameSession() {
        map = MapGenerator.createWorld();
        eventTracker = new RequiredEventTracker(permanentProgress);
        combatController = new CombatController(playerStats);
        resetToSpawn();
        stepBudget.resetForCycle(playerStats);
        map.revealAround(getPlayerGridPos());
    }

    public void startNewGame() {
        permanentProgress.resetForNewGame();
        playerStats.resetForNewGame();
        map.resetCycleState();
        resetToSpawn();
        stepBudget.resetForCycle(playerStats);
        map.revealAround(getPlayerGridPos());
        mode = GameplayMode.EXPLORE_IDLE;
        pendingMessage = null;
        bossAvailableThisCycle = false;
    }

    private void resetToSpawn() {
        GridPos spawn = map.getSpawnPosition();
        playerX = spawn.x;
        playerY = spawn.y;
    }

    public GameMap getMap() {
        return map;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public PermanentProgress getPermanentProgress() {
        return permanentProgress;
    }

    public RequiredEventTracker getEventTracker() {
        return eventTracker;
    }

    public StepBudgetService getStepBudget() {
        return stepBudget;
    }

    public PathRunner getPathRunner() {
        return pathRunner;
    }

    public CombatController getCombatController() {
        return combatController;
    }

    public GameplayMode getMode() {
        return mode;
    }

    public void setMode(GameplayMode mode) {
        this.mode = mode;
    }

    public MapViewState getMapViewState() {
        return mapViewState;
    }

    public MapOverlayLayout createMapOverlayLayout() {
        return new MapOverlayLayout(map, mapViewState.getOriginX(), mapViewState.getOriginY());
    }

    public void openMapOverlay() {
        mapViewState.centerOn(getPlayerGridPos(), map);
        mode = GameplayMode.MAP_OVERLAY;
    }

    public void panMapView(int dx, int dy) {
        mapViewState.pan(dx, dy, map);
    }

    public GridPos getPlayerGridPos() {
        return new GridPos(playerX, playerY);
    }

    /** Grid cell used for presentation while moving (may differ during straight-line travel). */
    public GridPos getDisplayGridPos() {
        if (mode == GameplayMode.RUNNING && pathRunner.isRunning()) {
            return new GridPos(Math.round(pathRunner.getCurrentX()), Math.round(pathRunner.getCurrentY()));
        }
        return getPlayerGridPos();
    }

    /** Euclidean distance from world origin (0, 0); updates during straight-line travel. */
    public float getDistanceFromOrigin() {
        if (mode == GameplayMode.RUNNING && pathRunner.isRunning()) {
            return (float) Math.hypot(pathRunner.getCurrentX(), pathRunner.getCurrentY());
        }
        return (float) Math.hypot(playerX, playerY);
    }

    public String getPendingMessage() {
        return pendingMessage;
    }

    public void clearPendingMessage() {
        pendingMessage = null;
    }

    public float getStormTimer() {
        return stormTimer;
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    public boolean isBossAvailableThisCycle() {
        return bossAvailableThisCycle;
    }

    public boolean trySelectDestination(GridPos destination) {
        if (mode != GameplayMode.MAP_OVERLAY) {
            return false;
        }
        if (!map.isInside(destination)) {
            return false;
        }
        if (!map.canEnter(destination)) {
            pendingMessage = "Cannot enter this tile.";
            return false;
        }
        if (destination.equals(getPlayerGridPos())) {
            mode = GameplayMode.EXPLORE_IDLE;
            return true;
        }

        GridPos start = getPlayerGridPos();
        StraightLinePath.Plan plan = StraightLinePath.plan(map, start, destination);
        if (plan == null) {
            pendingMessage = "Straight path is blocked.";
            return false;
        }
        mode = GameplayMode.RUNNING;
        activeMovePlan = plan;
        stepsAppliedForCurrentMove = 0f;
        moveStepBaseline = 0f;
        moveOriginCell = start;
        lastCellDuringMove = start;
        pathRunner.startStraightMove(
                start.x, start.y,
                destination.x, destination.y,
                plan.getDistance(),
                () -> onStraightMoveComplete(plan)
        );
        return true;
    }

    private void onStraightMoveComplete(StraightLinePath.Plan plan) {
        if (mode == GameplayMode.STORM || mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT) {
            return;
        }

        applyRemainingMoveStepCost(plan);
        activeMovePlan = null;
        stepsAppliedForCurrentMove = 0f;

        playerX = plan.getDestination().x;
        playerY = plan.getDestination().y;

        for (GridPos cell : plan.getCellsOnLine()) {
            map.revealAround(cell);
        }

        Tile tile = map.getTile(getPlayerGridPos());
        if (tile.needsInteractionOnArrival()) {
            handleTileInteraction(tile, false);
            return;
        }

        mode = GameplayMode.EXPLORE_IDLE;

        if (stepBudget.isExhausted()) {
            triggerStorm();
        }
    }

    private void applyRemainingMoveStepCost(StraightLinePath.Plan plan) {
        float remaining = plan.getDistance() - stepsAppliedForCurrentMove;
        if (remaining > 1e-4f) {
            stepBudget.consumeSteps(remaining);
            scrollOffset += 50f * remaining;
            stepsAppliedForCurrentMove = plan.getDistance();
        }
    }

    private void interruptMoveForStorm() {
        if (pathRunner.isRunning()) {
            playerX = Math.round(pathRunner.getCurrentX());
            playerY = Math.round(pathRunner.getCurrentY());
            pathRunner.cancel();
        }
        activeMovePlan = null;
        stepsAppliedForCurrentMove = 0f;
        moveStepBaseline = 0f;
        map.revealAround(getPlayerGridPos());
        triggerStorm();
    }

    private void pauseMovementForInteraction() {
        pauseWorldX = pathRunner.getCurrentX();
        pauseWorldY = pathRunner.getCurrentY();
        pathRunner.pause();
        playerX = Math.round(pauseWorldX);
        playerY = Math.round(pauseWorldY);
        lastCellDuringMove = getPlayerGridPos();
        map.revealAround(getPlayerGridPos());
    }

    private void resumePausedMove() {
        StraightLinePath.Plan plan = activeMovePlan;
        if (plan == null) {
            mode = GameplayMode.EXPLORE_IDLE;
            return;
        }
        GridPos dest = plan.getDestination();
        float remaining = (float) Math.hypot(dest.x - pauseWorldX, dest.y - pauseWorldY);
        if (remaining < 0.01f) {
            onStraightMoveComplete(plan);
            return;
        }
        moveStepBaseline = stepsAppliedForCurrentMove;
        mode = GameplayMode.RUNNING;
        pathRunner.startStraightMove(
                pauseWorldX, pauseWorldY,
                dest.x, dest.y,
                remaining,
                () -> onStraightMoveComplete(plan)
        );
        lastCellDuringMove = getPlayerGridPos();
    }

    private void checkMidMoveTileEncounter() {
        GridPos cell = getDisplayGridPos();
        if (cell.equals(lastCellDuringMove)) {
            return;
        }
        lastCellDuringMove = cell;
        if (cell.equals(moveOriginCell)) {
            return;
        }
        if (!activeMovePlan.getCellsOnLine().contains(cell)) {
            return;
        }
        Tile tile = map.getTile(cell);
        if (!tile.needsInteractionOnArrival()) {
            return;
        }
        pauseMovementForInteraction();
        handleTileInteraction(tile, true);
    }

    private void handleTileInteraction(Tile tile, boolean duringMove) {
        switch (tile.getType()) {
            case ITEM -> {
                if (!tile.isItemCollectedThisCycle()) {
                    tile.setItemCollectedThisCycle(true);
                    map.markCycleModified(tile.getPosition());
                    playerStats.applyItemBonus();
                    playerStats.addExperience(10);
                    pendingMessage = "Item collected! ATK +2, step limit +1";
                }
                if (duringMove && activeMovePlan != null) {
                    resumePausedMove();
                } else {
                    mode = GameplayMode.EXPLORE_IDLE;
                }
            }
            case EVENT -> {
                String eventId = tile.getEventId();
                if (eventId != null && permanentProgress.isEventCompleted(eventId)) {
                    pendingMessage = "Ruins already investigated.";
                } else if (eventId != null) {
                    eventTracker.completeEvent(eventId);
                    permanentProgress.save();
                    pendingMessage = "Required event completed: " + eventId;
                    tile.setCycleCleared(true);
                    map.markCycleModified(tile.getPosition());
                }
                if (duringMove && activeMovePlan != null) {
                    resumePausedMove();
                } else {
                    mode = GameplayMode.EXPLORE_IDLE;
                }
            }
            case COMBAT -> {
                mode = GameplayMode.COMBAT;
            }
            case BOSS_SUMMON -> {
                if (eventTracker.allRequiredEventsComplete()) {
                    bossAvailableThisCycle = true;
                    mode = GameplayMode.BOSS_COMBAT;
                } else {
                    pendingMessage = "Required events incomplete (" + eventTracker.getCompletedCount()
                            + "/" + eventTracker.getRequiredCount() + ")";
                    if (duringMove && activeMovePlan != null) {
                        resumePausedMove();
                    } else {
                        mode = GameplayMode.EXPLORE_IDLE;
                    }
                }
            }
            default -> {
                if (duringMove && activeMovePlan != null) {
                    resumePausedMove();
                } else {
                    mode = GameplayMode.EXPLORE_IDLE;
                }
            }
        }
    }

    public void triggerStorm() {
        mode = GameplayMode.STORM;
        stormTimer = 0f;
    }

    public void updateStorm(float delta) {
        stormTimer += delta;
    }

    public void completeStorm() {
        stormReset.applyCycleReset(map, playerStats, stepBudget);
        resetToSpawn();
        map.revealAround(getPlayerGridPos());
        permanentProgress.save();
        mode = GameplayMode.EXPLORE_IDLE;
        pendingMessage = "Sandstorm... You returned to camp center.";
        bossAvailableThisCycle = false;
    }

    public void onCombatEnd(String result) {
        Tile tile = map.getTile(getPlayerGridPos());
        switch (result) {
            case "victory" -> {
                tile.setCycleCleared(true);
                map.markCycleModified(tile.getPosition());
                playerStats.addExperience(20);
                pendingMessage = "Battle won!";
                if (activeMovePlan != null) {
                    resumePausedMove();
                } else {
                    mode = GameplayMode.EXPLORE_IDLE;
                }
            }
            case "boss_victory" -> {
                permanentProgress.setGameWon(true);
                permanentProgress.save();
                mode = GameplayMode.VICTORY;
            }
            case "defeat" -> triggerStorm();
            default -> mode = GameplayMode.EXPLORE_IDLE;
        }
    }

    public void updateRunning(float delta) {
        scrollOffset += pathRunner.getScrollOffset(delta);
        if (activeMovePlan == null || !pathRunner.isRunning()) {
            return;
        }

        float segmentTravel = pathRunner.getProgress() * pathRunner.getTotalDistance();
        float targetConsumed = moveStepBaseline + segmentTravel;
        float deltaCost = targetConsumed - stepsAppliedForCurrentMove;
        if (deltaCost > 1e-5f) {
            stepBudget.consumeSteps(deltaCost);
            stepsAppliedForCurrentMove = targetConsumed;
            map.revealAround(getDisplayGridPos());
        }

        if (stepBudget.isExhausted()) {
            interruptMoveForStorm();
            return;
        }

        checkMidMoveTileEncounter();
    }

    public int getCurrentDistanceBand() {
        return map.distanceBand(getPlayerGridPos());
    }

    public Tile getCurrentTile() {
        return map.getTile(getDisplayGridPos());
    }
}
