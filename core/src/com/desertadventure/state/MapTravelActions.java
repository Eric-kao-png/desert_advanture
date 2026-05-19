package com.desertadventure.state;

import com.desertadventure.config.GameMessages;
import com.desertadventure.exploration.TravelMovement;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.StraightLinePath;

/** Map overlay open/close and destination selection. */
public final class MapTravelActions {
    private final GameMap map;
    private final MapViewState mapViewState;
    private final TravelMovement travel;
    private final MessageFeed messages;
    private final SessionModeAccess mode;
    private final PlayerPosition player;

    public interface PlayerPosition {
        GridPos get();

        void set(int x, int y);
    }

    public MapTravelActions(
            GameMap map,
            MapViewState mapViewState,
            TravelMovement travel,
            MessageFeed messages,
            SessionModeAccess mode,
            PlayerPosition player) {
        this.map = map;
        this.mapViewState = mapViewState;
        this.travel = travel;
        this.messages = messages;
        this.mode = mode;
        this.player = player;
    }

    public void openOverlay() {
        if (mode.get() == GameplayMode.RUNNING) {
            travel.stopForMap();
        }
        mapViewState.centerOn(player.get(), map);
        mode.set(GameplayMode.MAP_OVERLAY);
    }

    public boolean trySelectDestination(GridPos destination) {
        if (mode.get() != GameplayMode.MAP_OVERLAY) {
            return false;
        }
        if (!map.isInside(destination)) {
            return false;
        }
        if (!map.canEnter(destination)) {
            messages.push(GameMessages.CANNOT_ENTER_TILE);
            return false;
        }
        if (destination.equals(player.get())) {
            mode.set(GameplayMode.EXPLORE_IDLE);
            return true;
        }

        GridPos start = player.get();
        StraightLinePath.Plan plan = StraightLinePath.plan(map, start, destination);
        if (plan == null) {
            messages.push(GameMessages.PATH_BLOCKED);
            return false;
        }
        travel.start(plan, start);
        return true;
    }
}
