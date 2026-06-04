# Desert Adventure

A LibGDX desert roguelite prototype with turn-based card combat.

## MVP mode (card run)

The default flow after **Enter** is a **linear card run** (no map exploration):

1. **Camp Hub** — start the current stage fight or view your action deck (with cooldowns)
2. **4 stages** — 3 normal encounters + 1 boss (`core/assets/stages/stages.json`)
3. **Win** a non-boss fight → advance stage, return to hub (small HP heal between fights)
4. **Beat the boss** → victory screen
5. **Lose** or run out of HP → **rewind** to stage 1, **all card cooldowns cleared** (same deck instances; no meta card pick)

Cooldowns **persist between fights** in the same run and are only cleared on rewind.

## Controls (MVP)

| Key | Action |
|-----|--------|
| Enter | Start from main menu |
| 1 | Hub: start current stage battle |
| 2 | Hub: view deck (name + cooldown remaining) |
| B / Esc | Back to hub from deck; Esc from hub → main menu |
| Click hub lines | Same as 1–2 |
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

- [Combat card system](docs/Combat-Card-System.md) — turn phases, deck, balance, controls
- Stage catalog: `core/assets/stages/stages.json`
- Enemy archetypes: `core/assets/enemies/enemy_archetypes.json`

## Shelved (not in MVP)

- Meta card retention / pick-a-card on death
- Map exploration as the default loop
- Branching stage paths
