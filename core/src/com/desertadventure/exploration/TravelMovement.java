package com.desertadventure.exploration;

import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.StraightLinePath;
import com.desertadventure.map.model.Tile;
import com.desertadventure.state.ExplorationCallbacks;
import com.desertadventure.state.GameplayMode;

/**
 * Straight-line travel, step consumption, mid-path encounters, pause/resume.
 */
public class TravelMovement {
    private final PathRunner pathRunner = new PathRunner();
    private final ExplorationCallbacks session;

    private StraightLinePath.Plan activePlan;
    private float stepsApplied;
    private float stepBaseline;
    private GridPos lastCell;
    private GridPos originCell;
    private float pauseX;
    private float pauseY;

    public TravelMovement(ExplorationCallbacks session) {
        this.session = session;
    }

    public PathRunner getPathRunner() {
        return pathRunner;
    }

    public boolean hasActivePlan() {
        return activePlan != null;
    }

    public void start(StraightLinePath.Plan plan, GridPos start) {
        activePlan = plan;
        stepsApplied = 0f;
        stepBaseline = 0f;
        originCell = start;
        lastCell = start;
        session.setMode(GameplayMode.RUNNING);
        pathRunner.startStraightMove(
                start.x, start.y,
                plan.getDestination().x, plan.getDestination().y,
                plan.getDistance(),
                this::onPathComplete
        );
    }

    public void update(float delta) {
        if (activePlan == null || !pathRunner.isRunning()) {
            return;
        }

        applyProgressSteps();
        if (session.getStepBudget().isExhausted()) {
            interruptForStorm();
            return;
        }

        checkMidPathEncounter();
    }

    public void pauseForInteraction() {
        pauseX = pathRunner.getCurrentX();
        pauseY = pathRunner.getCurrentY();
        pathRunner.pause();
        syncPlayerToRunnerPosition();
        lastCell = session.getPlayerGridPos();
        session.getMap().revealAround(session.getPlayerGridPos());
    }

    /** Stops in-progress travel for the map overlay; keeps rounded grid position, clears the plan. */
    public void stopForMap() {
        if (activePlan == null) {
            return;
        }
        if (pathRunner.isRunning()) {
            applyProgressSteps();
            cancelRunnerAtCurrentCell();
        }
        clearPlan();
        session.getMap().revealAround(session.getPlayerGridPos());
    }

    public void resume() {
        if (activePlan == null) {
            session.setMode(GameplayMode.EXPLORE_IDLE);
            return;
        }
        GridPos dest = activePlan.getDestination();
        float remaining = (float) Math.hypot(dest.x - pauseX, dest.y - pauseY);
        if (remaining < GameConfig.PATH_REMAINING_EPSILON) {
            onPathComplete();
            return;
        }
        stepBaseline = stepsApplied;
        session.setMode(GameplayMode.RUNNING);
        pathRunner.startStraightMove(pauseX, pauseY, dest.x, dest.y, remaining, this::onPathComplete);
        lastCell = session.getPlayerGridPos();
    }

    private void onPathComplete() {
        if (session.getMode().interruptsTravel()) {
            return;
        }

        StraightLinePath.Plan plan = activePlan;
        float remaining = plan.getDistance() - stepsApplied;
        if (remaining > GameConfig.PLAN_DISTANCE_EPSILON) {
            session.getStepBudget().consumeSteps(remaining);
            session.addScrollOffset(GameConfig.SCROLL_OFFSET_PER_STEP * remaining);
            stepsApplied = plan.getDistance();
        }

        clearPlan();
        GridPos dest = plan.getDestination();
        session.setPlayerGridPos(dest.x, dest.y);
        revealLine(plan);

        Tile tile = session.getMap().getTile(dest);
        if (tile.needsInteractionOnArrival()) {
            session.handleTileInteraction(tile, false);
            return;
        }

        session.setMode(GameplayMode.EXPLORE_IDLE);
        if (session.getStepBudget().isExhausted()) {
            session.triggerStorm();
        }
    }

    private void interruptForStorm() {
        cancelRunnerAtCurrentCell();
        clearPlan();
        session.getMap().revealAround(session.getPlayerGridPos());
        session.triggerStorm();
    }

    private void checkMidPathEncounter() {
        GridPos cell = session.getDisplayGridPos();
        if (cell.equals(lastCell) || cell.equals(originCell)) {
            return;
        }
        if (!activePlan.getCellsOnLine().contains(cell)) {
            return;
        }
        lastCell = cell;
        Tile tile = session.getMap().getTile(cell);
        if (!tile.needsInteractionOnArrival()) {
            return;
        }
        pauseForInteraction();
        session.handleTileInteraction(tile, true);
    }

    private void applyProgressSteps() {
        if (!pathRunner.isRunning()) {
            return;
        }
        float segmentTravel = pathRunner.getProgress() * pathRunner.getTotalDistance();
        float targetConsumed = stepBaseline + segmentTravel;
        float deltaCost = targetConsumed - stepsApplied;
        if (deltaCost > GameConfig.STEP_CONSUME_EPSILON) {
            session.getStepBudget().consumeSteps(deltaCost);
            stepsApplied = targetConsumed;
            session.getMap().revealAround(session.getDisplayGridPos());
        }
    }

    private void syncPlayerToRunnerPosition() {
        session.setPlayerGridPos(Math.round(pathRunner.getCurrentX()), Math.round(pathRunner.getCurrentY()));
    }

    private void cancelRunnerAtCurrentCell() {
        if (!pathRunner.isRunning()) {
            return;
        }
        syncPlayerToRunnerPosition();
        pathRunner.cancel();
    }

    private void revealLine(StraightLinePath.Plan plan) {
        GameMap map = session.getMap();
        for (GridPos cell : plan.getCellsOnLine()) {
            map.revealAround(cell);
        }
    }

    private void clearPlan() {
        activePlan = null;
        stepsApplied = 0f;
        stepBaseline = 0f;
    }
}
