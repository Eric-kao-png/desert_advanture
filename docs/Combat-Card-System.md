# Desert Adventure — Turn-Based Card Combat

Reference for the 1v1 card combat loop, action deck, balance constants, and integration points (LibGDX, Java 17, `core` module).

**Last aligned with implementation:** June 2026

---

## 1. Overview

Combat is **turn-based** and **1v1** (player vs one enemy or boss). Each **round**:

1. **PLANNING** — Player assigns 0–2 action cards to slots **1** and **3** (same card *instance* cannot occupy both). Slots **2** and **4** show the enemy’s planned attacks for the round.
2. **RESOLVING** — Slots resolve in order **1 → 2 → 3 → 4** (brief pause per slot for UI). If HP reaches 0 after any slot, combat ends immediately and remaining slots are skipped.
3. **Round-end cooldowns** — Played cards get full cooldown, then **every** instance (including played) ticks down once so the ending round counts as one cooldown turn. Runs when all four slots finish **or** when combat ends early mid-resolve (same as a normal round end).
4. **Outcome check** — If either side is dead, combat ends; otherwise a new **PLANNING** round begins.

Cards are **not consumed**: after resolving, instances return to the deck with **turn-based cooldown** counters.

Exploration **action cards** are separate from the **item inventory** (potions/gems).

---

## 2. Phases & Slots

| Slot | Owner | Planning |
|------|--------|----------|
| 1 | Player | Optional (0–2 cards total across 1 & 3) |
| 2 | Enemy | Attack (same card type as player) |
| 3 | Player | Optional |
| 4 | Enemy | Attack (same card type as player) |

**Rules**

- Different **instances** of the same type (e.g. two Attack cards) may both be played in one round.
- The same **instance ID** cannot be in slot 1 and slot 3.
- Empty player slots are allowed; confirm proceeds with partial plans.

---

## 3. Player Action Cards (v0.2)

| Card | Effect | Cooldown (turns) |
|------|--------|------------------|
| Attack | 2 damage to enemy | 1 |
| Strong Attack | 3 damage to enemy | 2 |
| Heal | 4 HP to self | 4 |

Constants live in `GameConfig` (`CARD_*`). Enemy slots use `ActionCardType.ATTACK` (same damage/cooldown display as player).

**Starter deck** (reset on **new game** only): 2× Attack, 1× Strong Attack, 1× Heal.

**Victory reward** (normal enemy `CombatOutcome.VICTORY` only, not boss): one random card via `ActionCardRewards.rollVictoryCard()` — 50% Heal, 50% Strong Attack — added to the deck with a `GameMessages` line (`Gained: …`). Boss camp-clear uses `BOSS_VICTORY` and does not grant this loot.

---

## 4. Enemy AI (MVP)

Always plays **Attack** in slots 2 and 4 — same `ActionCardType.ATTACK` as the player (currently 2 damage; cooldown shown in UI only, enemies do not use CD).

---

## 5. Action Card Deck

- Class: `com.desertadventure.combat.card.ActionCardDeck`
- Instances: `ActionCardInstance` (`instanceId`, `ActionCardType`, `cooldownRemaining`)
- `addCard(ActionCardType)` — append a new instance (victory loot, future rewards).
- Reset hook: `ActionCardDeckResetPolicy` — default `DefaultActionCardDeckResetPolicy` wipes to starter deck on **new game** only.
- Sandstorm (`StormResetService`) does **not** reset the deck; cards persist across defeat cycles until a new run.

**Wired into**

- `GameSession` constructor / `startNewGame` (starter deck)
- `CombatOutcomeApplier.applyVictory` (random card reward)

---

## 6. Balance (`GameConfig`)

| Constant | Value |
|----------|-------|
| `PLAYER_INITIAL_MAX_HP` | 20 |
| `PLAYER_LEVEL_HP_GAIN` | 2 |
| `BASE_STEP_BUDGET` | 100 |
| `ITEM_HEALTH_POTION_RESTORE` | 8 |
| `ITEM_STAMINA_POTION_RESTORE` | 25 |
| `ITEM_HEALTH_GEM_BONUS` | 4 |
| `ITEM_STAMINA_GEM_BONUS` | 15 |
| `ENEMY_HP_MIN` / `ENEMY_HP_MAX` | 1 / 3 (uniform roll per normal fight) |
| `BOSS_BASE_HP` | 35 |
| `BOSS_HP_PER_DISTANCE_BAND` | 8 |

Normal enemy HP is random in `[ENEMY_HP_MIN, ENEMY_HP_MAX]` at combat start. Boss HP scales with `GameMap.distanceBand`.

---

## 7. Integration

| Piece | Role |
|-------|------|
| `CombatController` | Turn session, slots, resolve, win/loss |
| `CombatOutcome` / `CombatOutcomeApplier` | Victory → XP + random card; boss victory → win screen; defeat → sandstorm (deck kept) |
| `ActionCardRewards` | Victory loot roll (extensible for future tables) |
| `GameplayMode.COMBAT` / `BOSS_COMBAT` | Mode gating |
| `GameplayModeUpdater` | Starts combat with deck + distance band |
| `CombatCardInput` | Pointer + Enter/Space confirm |
| `CombatCardRenderer` + `CombatCardLayout` | Timeline, hand, confirm |
| `GameplaySceneDrawer` | Parallax background, entities left/right, card UI |

Defeat still triggers **sandstorm** (cycle reset). Victory clears the combat tile and resumes travel if a path was active.

---

## 8. Controls

| Input | Action |
|-------|--------|
| Click hand card | Select / deselect |
| Click slot 1 or 3 | Place selected card, or pick up card from slot |
| Confirm button / Enter / Space | End planning → resolve |
| M / N | Not available during combat |

---

## 9. File Index

| Path | Purpose |
|------|---------|
| `combat/card/ActionCardType.java` | Card definitions (damage, target, cooldown) |
| `combat/card/ActionCardDeck.java` | Instance collection |
| `combat/card/ActionCardRewards.java` | Victory / future loot rolls |
| `combat/card/ActionCardDeckResetPolicy.java` | Reset policy hook |
| `combat/system/CombatController.java` | Turn-based session |
| `screen/input/CombatCardInput.java` | Combat input |
| `screen/layout/CombatCardLayout.java` | UI geometry |
| `presentation/CombatCardRenderer.java` | Card/slot drawing |

---

## 10. Extending Cards

Add enum values to `ActionCardType` with `ActionCardTarget`, primary value, and cooldown. Resolution is centralized in `CombatController.applyCardEffect`. New enemy behaviors can replace `getEnemyCardForSlot` / `resolveSlot` enemy branch.
