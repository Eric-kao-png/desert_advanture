# Desert Adventure

LibGDX prototype focused on **turn-based card combat** and a minimal linear stage run.

## MVP flow

1. Launch goes straight into **Gameplay** (no main menu).
2. **Hub** shows the current stage label (`Stage N / total — name`).
3. Press **1** or tap the label to start that stage’s fight.
4. **Combat** — assign cards to slots 1 & 3, confirm the round, resolve until win or lose.
5. **Win** (non-boss) → advance to the next stage, return to hub.
6. **Win boss** → stay on hub (final stage cleared).
7. **Lose** or player HP reaches 0 → rewind to stage 1, full heal, **all card cooldowns cleared** (same deck instances).

Cooldowns persist between fights in the same run and are only cleared on rewind.

## Controls

| Key | Action |
|-----|--------|
| 1 | Hub: start current stage battle |
| Click hub label | Same as 1 |
| Click + slots 1 & 3 | Assign action cards (combat) |
| Enter / Space | Confirm combat round |

## Run

```bash
gradle :desktop:runGame
```

macOS automatically adds `-XstartOnFirstThread` (required by LWJGL3).

## Tests

```bash
./gradlew :core:test -q
```

## Documentation

- [Combat card system](docs/Combat-Card-System.md) — turn phases, deck, balance
- Stage catalog: `core/assets/stages/stages.json`
- Enemy archetypes: `core/assets/enemies/enemy_archetypes.json`

## Shelved (not in MVP)

- Map exploration, items, meta card picks, victory screen
- Branching stage paths
