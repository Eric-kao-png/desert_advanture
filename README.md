# Desert Adventure

A LibGDX desert roguelite prototype with turn-based card combat.

## MVP mode (card run)

Launch goes straight into a **linear card run**:

1. **Camp Hub** — start the current stage fight
2. **4 stages** — 3 normal encounters + 1 boss (`core/assets/stages/stages.json`)
3. **Win** a non-boss fight → advance stage, return to hub (small HP heal between fights)
4. **Beat the boss** → victory screen (Enter starts a new run)
5. **Lose** or run out of HP → **rewind** to stage 1, **all card cooldowns cleared** (same deck instances; no meta card pick)

Cooldowns **persist between fights** in the same run and are only cleared on rewind.

## Controls (MVP)

| Key | Action |
|-----|--------|
| 1 | Hub: start current stage battle |
| Click hub line | Same as 1 |
| Click + slots 1 & 3 | Assign action cards (combat) |
| Enter / Space | Confirm combat round |
| Enter | Victory screen: play again |

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
