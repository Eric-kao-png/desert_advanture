package com.desertadventure.state;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameMessages;
import com.desertadventure.event.RequiredEventTracker;
import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.exploration.StormResetService;
import com.desertadventure.exploration.TileInteractionContext;
import com.desertadventure.exploration.TileInteractionHandler;
import com.desertadventure.exploration.TravelMovement;
import com.desertadventure.exploration.PathRunner;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.MapGenerator;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.player.PlayerStats;

public class GameSession implements ExplorationCallbacks {
    private final GameMap map;
    private final PlayerStats playerStats = new PlayerStats();
    private final PermanentProgress permanentProgress = new PermanentProgress();
    private final RequiredEventTracker eventTracker;
    private final StepBudgetService stepBudget = new StepBudgetService();
    private final StormResetService stormReset = new StormResetService();
    private final TravelMovement travel;
    private final CombatController combatController;
    private final MapViewState mapViewState = new MapViewState();
    private final MessageFeed messageFeed = new MessageFeed();
    private final MapTravelActions mapTravel;
    private final CombatOutcomeApplier combatOutcomes;
    private final TileInteractionContext tileContext;

    private int playerX;
    private int playerY;
    private GameplayMode mode = GameplayMode.EXPLORE_IDLE;
    private float stormTimer;
    private float scrollOffset;
    private boolean bossAvailableThisCycle;

    public GameSession() {
        map = MapGenerator.createWorld();
        travel = new TravelMovement(this);
        eventTracker = new RequiredEventTracker(permanentProgress);
        combatController = new CombatController(playerStats);
        mapTravel = new MapTravelActions(
                map, mapViewState, travel, messageFeed,
                modeAccess(), playerPosition());
        combatOutcomes = new CombatOutcomeApplier(
                map, playerStats, permanentProgress, travel, messageFeed,
                modeAccess(), this::triggerStorm, this::getPlayerGridPos);
        tileContext = new TileInteractionContext(
                this, map, playerStats, permanentProgress,
                eventTracker, travel::resume, available -> bossAvailableThisCycle = available);
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
        messageFeed.clear();
        bossAvailableThisCycle = false;
    }

    private SessionModeAccess modeAccess() {
        return new SessionModeAccess() {
            @Override
            public GameplayMode get() {
                return mode;
            }

            @Override
            public void set(GameplayMode value) {
                mode = value;
            }
        };
    }

    private MapTravelActions.PlayerPosition playerPosition() {
        return new MapTravelActions.PlayerPosition() {
            @Override
            public GridPos get() {
                return GameSession.this.getPlayerGridPos();
            }

            @Override
            public void set(int x, int y) {
                GameSession.this.setPlayerGridPos(x, y);
            }
        };
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
        return travel.getPathRunner();
    }

    public CombatController getCombatController() {
        return combatController;
    }

    @Override
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
        mapTravel.openOverlay();
    }

    public void panMapView(int dx, int dy) {
        mapViewState.pan(dx, dy, map);
    }

    @Override
    public GridPos getPlayerGridPos() {
        return new GridPos(playerX, playerY);
    }

    @Override
    public void setPlayerGridPos(int x, int y) {
        playerX = x;
        playerY = y;
    }

    public GridPos getDisplayGridPos() {
        PathRunner runner = activePathRunner();
        if (runner != null) {
            return new GridPos(Math.round(runner.getCurrentX()), Math.round(runner.getCurrentY()));
        }
        return getPlayerGridPos();
    }

    public float getDistanceFromOrigin() {
        PathRunner runner = activePathRunner();
        if (runner != null) {
            return (float) Math.hypot(runner.getCurrentX(), runner.getCurrentY());
        }
        return (float) Math.hypot(playerX, playerY);
    }

    private PathRunner activePathRunner() {
        if (mode == GameplayMode.RUNNING && travel.getPathRunner().isRunning()) {
            return travel.getPathRunner();
        }
        return null;
    }

    public MessageFeed getMessageFeed() {
        return messageFeed;
    }

    public float getStormTimer() {
        return stormTimer;
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    @Override
    public void addScrollOffset(float amount) {
        scrollOffset += amount;
    }

    public boolean isBossAvailableThisCycle() {
        return bossAvailableThisCycle;
    }

    public boolean trySelectDestination(GridPos destination) {
        return mapTravel.trySelectDestination(destination);
    }

    @Override
    public void setPendingMessage(String message) {
        messageFeed.push(message);
    }

    @Override
    public void handleTileInteraction(Tile tile, boolean duringMove) {
        TileInteractionHandler.handle(tile, duringMove, tileContext);
    }

    @Override
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
        setPendingMessage(GameMessages.SANDSTORM_RETURN);
        bossAvailableThisCycle = false;
    }

    public void onCombatEnd(CombatOutcome outcome) {
        combatOutcomes.apply(outcome);
    }

    public void updateRunning(float delta) {
        scrollOffset += travel.getPathRunner().getScrollOffset(delta);
        travel.update(delta);
    }

    public int getCurrentDistanceBand() {
        return map.distanceBand(getPlayerGridPos());
    }

    public Tile getCurrentTile() {
        return map.getTile(getDisplayGridPos());
    }
}
