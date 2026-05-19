package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.OverlayCloseButton;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Map overlay: pan, destination pick, dismiss. */
public final class MapOverlayPointerInput {
    private final GameSession session;
    private final GameViewport viewport;
    private final MapOverlayInput mapInput;

    public MapOverlayPointerInput(GameSession session, GameViewport viewport, MapOverlayInput mapInput) {
        this.session = session;
        this.viewport = viewport;
        this.mapInput = mapInput;
    }

    public void handle() {
        if (GameInputBindings.justCloseMap()) {
            session.closeMapOverlay();
            return;
        }
        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        panMapView();
        updateHover();
        if (!Gdx.input.justTouched()) {
            return;
        }
        if (OverlayCloseButton.contains(worldX, worldY)) {
            session.closeMapOverlay();
            return;
        }
        if (mapInput.getHoveredGridPos() != null) {
            session.trySelectDestination(mapInput.getHoveredGridPos());
        }
    }

    public void updateHover() {
        if (session.getMode() != GameplayMode.MAP_OVERLAY) {
            return;
        }
        MapOverlayLayout layout = session.createMapOverlayLayout();
        mapInput.updateHover(layout, viewport.pointerWorldX(), viewport.pointerWorldY());
    }

    public boolean isDismissHovered() {
        return mapInput.isHoveredDismiss();
    }

    public boolean isDismissPressed() {
        return Gdx.input.isTouched() && mapInput.isHoveredDismiss();
    }

    public GridPos hoveredCell() {
        return mapInput.getHoveredGridPos();
    }

    private void panMapView() {
        int step = GameConfig.MAP_PAN_STEP;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            session.panMapView(-step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            session.panMapView(step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            session.panMapView(0, step);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            session.panMapView(0, -step);
        }
    }
}
