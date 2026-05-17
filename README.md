# Desert Adventure

A LibGDX desert roguelite prototype: 2D grid exploration, side-scrolling travel presentation, and real-time combat.

## Controls

| Key | Action |
|-----|--------|
| Enter | Start from main menu |
| M | Open map (while exploring) |
| Arrow keys | Pan map view (while map is open) |
| Mouse click | Select destination on map |
| Esc | Close map / return to main menu |
| WASD / Arrow keys | Combat movement |
| J / K / L | Attack / Skill / Ultimate |

## Run

```bash
gradle :desktop:runGame
```

macOS automatically adds `-XstartOnFirstThread` (required by LWJGL3).

## World

- **501×501** procedural desert map; world origin **(0, 0)** is the center and spawn (coords about **-250..250**)
- Movement uses **steps** (float, Euclidean distance) per cycle along a **straight line**; you may select any unblocked destination—if steps run out mid-travel, a sandstorm begins
- Map overlay shows a **51×51** window; use arrow keys to pan across the world

## Documentation

- [Movement & map logic](docs/Movement-and-Map-Logic.md) — path planning, steps, mid-path encounters, file index

## Goal

1. Explore the map and complete 3 required events (yellow tiles)
2. Running out of steps triggers a sandstorm reset (progress is kept)
3. After all events, reach the purple Boss tile and defeat the Boss to win
