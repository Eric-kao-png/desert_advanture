# Desert Adventure — 回合制卡牌戰鬥

LibGDX、`core` 模組的 1v1 卡牌戰鬥參考：回合流程、牌庫、資料來源與整合點。

**最後與實作對齊：** 2026 年 6 月

---

## 1. 總覽

戰鬥為 **回合制 1v1**（玩家對一名敵人；Boss 關卡使用較大體型與較高 HP）。每 **回合**：

1. **規劃（PLANNING）** — 系統擲出本回合玩家可用的 **兩個格位**（見 §2）。玩家將 0–2 張行動卡放到這兩格（同一 **實例** 不可佔兩格）。敵人格位顯示敵方本回合將出的牌。
2. **結算（RESOLVING）** — 依序結算 **1 → 2 → 3 → 4**（每格短暫停留供 UI）。任一格結算後若一方 HP 歸零，戰鬥立即結束，其餘格跳過。
3. **回合末狀態與冷卻** — 存活實體結算負面狀態（如中毒）、狀態剩餘回合 −1；已出牌進入完整冷卻，再全牌庫冷卻 −1（本回合計入一次冷卻）。在四格皆結算完或中途戰鬥結束時執行。
4. **勝負判定** — 若有人死亡則結束；否則進入下一回合規劃。

卡牌 **不消耗**：結算後實例回到牌庫，以 **回合冷卻** 限制重複使用。

**流程範圍（MVP）**：僅 **Hub** 與 **COMBAT**（`GameplayMode`）。無地圖探索、沙暴、Boss 專用模式、勝利掉牌或 `CombatOutcomeApplier`。

---

## 2. 格位與玩家格位擲骰

時間軸固定四格：

| 格 | 擁有者 | 說明 |
|----|--------|------|
| 1 | 玩家或敵人 | 依本回合擲出的玩家格位對決定 |
| 2 | 敵人 | 敵方出牌格 |
| 3 | 玩家或敵人 | 同上 |
| 4 | 敵人 | 敵方出牌格 |

每回合開始時，`WeightedPlayerSlotRoller` 依 `CombatConfig` 權重擲出玩家可用的 **一對** 格位：

| 組合 | 權重常數 | 機率傾向 |
|------|----------|----------|
| 1 & 3 | `PLAYER_SLOTS_WEIGHT_13` (70) | 最常見 |
| 2 & 4 | `PLAYER_SLOTS_WEIGHT_24` (20) | 次常見 |
| 1 & 2 | `PLAYER_SLOTS_WEIGHT_12` (5) | 少見 |
| 3 & 4 | `PLAYER_SLOTS_WEIGHT_34` (5) | 少見 |

**規劃規則**

- 同一 **類型** 的不同實例（例如兩張攻擊）可同回合各出一張。
- 同一 **實例 ID** 不可佔兩個玩家格。
- 玩家格可留空；可部分確認後進入結算。

敵方由 **原型牌組** + `RandomEnemyAi` 選牌，**不限於 ATTACK**（見 §6）。

---

## 3. 行動卡資料（25 張）

定義來源（合併載入）：
- `core/assets/cards/offense_cards.json` — 攻擊牌（`category`: `OFFENSE`，19 張）
- `core/assets/cards/change_cards.json` — 變化牌（`category`: `UTILITY`，6 張）

`ActionCardType` 與各檔 JSON `id` 一一對應；執行時由 `CardManifestLoader` / `GdxCardRepositoryLoader` 合併為單一 `CardRepository`。

**效果權威來源**：JSON 的 `effects` 陣列 + `CardEffectResolver` 模板（`EffectTemplateId` / `ConditionType`）。`ActionCardType` 僅提供顯示名稱、描述、分類、冷卻、目標等 **從 JSON 讀取** 的欄位，**不再** 以 enum switch 決定傷害數值。

### 分類摘要

| 分類 | JSON `category` | 張數概略 | 代表效果 |
|------|-----------------|----------|----------|
| 攻擊牌 | `OFFENSE` | 約 18 | 直接傷害、條件加傷（格位、前一格攻擊、本回合是否用變化牌等） |
| 變化牌 | `UTILITY` | 約 7 | 治療、護盾、減半敵 HP、中毒、淨化、轉移負面狀態等 |

**攻擊牌（範例）**：攻擊、斬擊、重擊、迅擊、咒刃、追擊、雙刃、猛攻、伏擊、爪擊、蓄力斬、刃舞、巨刃、吸血、魔法彈、魔法箭、箭、毒彈、毒箭等。

**變化牌（範例）**：治療、護盾、生命魔術、毒術、淨化、魔鏡。

條件範例（皆在 JSON `when` 中）：`ROUND_EQUALS`、`TURN_HAS_USED_CATEGORY`、`SLOT_INDEX_EQUALS`、`PREVIOUS_SLOT_PLAYER_OFFENSE`、`CASTER_HAS_NEGATIVE_STATUS`、`OPPONENT_HAS_NEGATIVE_STATUS`。

UI 分類：`ActionCardCategory` — **ATTACK（攻擊）** / **CHANGE（變化）**，對應 `GameMessages` 英文標籤（面板標題）。

---

## 4. 起始牌庫

`ActionCardDeck.resetToDefault()`（新遊戲／重設時）：

- 攻擊 ×2
- 斬擊 ×1
- 治療 ×1
- 護盾 ×1
- 蓄力斬 ×1

冷卻在 **同一輪 run** 的各場戰鬥間保留；**落敗 rewind** 時僅 `clearAllCooldowns()`，不重建牌庫實例。

**MVP 無勝利掉牌**：打贏一般關卡不會自動 `addCard`。

---

## 5. 戰鬥狀態（正面 / 負面）

每個 `CombatEntity` 各有一個正面、一個負面狀態格。同極性新狀態 **覆蓋** 舊的。護盾獨立於正面狀態（吸收傷害的點數）。

| 極性 | 類型 | 說明 |
|------|------|------|
| 負面 | `POISON` | 回合末造成固定傷害（見下），**無視護盾** |
| 負面 | `BLEED` | 回合末傷害 = 剩餘回合數 |
| 負面 | `FEAR` | 戰鬥邏輯用（如 roster 行為），無每回合固定傷害模板 |

**回合末**（`CombatEntity.applyRoundEndStatusEffects` ← `CombatController`）：

1. 若負面為 `POISON` 且回合數 &gt; 0 → 造成 `CombatConfig.CARD_POISON_DAMAGE_PER_ROUND`（目前 **2**）。
2. 若為 `BLEED` → 造成等同剩餘回合的傷害。
3. 正負面剩餘回合各 −1，歸零則清除。

中毒持續回合等由 **卡牌 JSON**（`APPLY_NEGATIVE_STATUS` 等）決定，非 `CombatConfig` 常數。

---

## 6. 敵人原型與 AI

資料：`core/assets/enemies/enemy_archetypes.json`；關卡對應：`core/assets/stages/stages.json`。

| 原型 | HP 範圍 | 牌組特色（非僅 ATTACK） |
|------|---------|-------------------------|
| Desert Zombie | 7–8 | 爪擊、攻擊、治療、護盾、吸血等 |
| Wandering Wizard | 6–7 | 魔法彈、魔法箭、毒彈、毒術、淨化、魔鏡 |
| Skeleton Archer | 6–7 | 箭、毒箭 |

敵人 HP 由原型 `hpMin` / `hpMax` 擲骰，**不再** 使用已移除的 `ENEMY_HP_MIN` / `ENEMY_HP_MAX`。

Boss 關（`stage_boss`）：`CombatConfig.BOSS_BASE_HP`、`BOSS_HP_PER_DISTANCE_BAND`（若 run 有距離帶則疊加；MVP 線性關卡可能固定 band）。

無原型時的極少數 fallback：`CombatController` 對玩家造成 **2** 點傷害（等同 JSON `ATTACK` 的 `amount`）。

---

## 7. 戰鬥結束與 Run 整合

| 元件 | 角色 |
|------|------|
| `CombatController` | 回合、格位、結算、`CardEffectResolver` |
| `CombatOutcome` | `VICTORY` / `BOSS_VICTORY` / `DEFEAT` |
| `StageRunCoordinator` | 勝利 → 推進關卡（非 Boss）並回 Hub；落敗或玩家死亡 → rewind 關卡 1、滿血、清冷卻、回 Hub |
| `GameSession` | Hub 按 **1** 或點標籤開戰；模式 `HUB` / `COMBAT` |
| `GameplayModeUpdater` | 進入 COMBAT 時初始化戰鬥狀態 |

落敗 **不** 重置牌庫內容，僅冷卻歸零。

---

## 8. 手牌 UI

規劃階段底部為 **兩列手牌** + 右側資訊區：

| 區域 | 內容 |
|------|------|
| 上列（攻擊） | `ActionCardCategory.ATTACK` 牌，可橫向捲動 |
| 下列（變化） | `CHANGE` 牌 |
| 右側資訊面板 | 選中或檢視卡的 **描述**（JSON `description`） |
| 資訊區上方 | **確認** 按鈕 |

**操作**：點選手牌、拖曳至本回合可用玩家格（`CombatCardInput` + `COMBAT_CARD_DRAG_THRESHOLD`）、點格收回、確認 / Enter / Space 結束規劃。

佈局：`CombatSceneLayout`（兩列 Y）、`CombatCardLayout`（`attackPanel` / `changePanel`）、`CombatCardRenderer`、`CombatInfoPanelDrawer`。

---

## 9. 操作對照

| 輸入 | 動作 |
|------|------|
| Hub：**1** 或點關卡標籤 | 開始當前關戰鬥 |
| 點手牌 | 選取 / 取消 |
| 拖曳至玩家格 | 放置；從格拖回可收回 |
| 確認 / Enter / Space | 結束規劃 → 結算 |

---

## 10. 平衡常數（`CombatConfig`）

| 常數 | 用途 |
|------|------|
| `CARD_POISON_DAMAGE_PER_ROUND` | 中毒每回合傷害（2） |
| `PLAYER_SLOTS_WEIGHT_*` | 玩家格位對擲骰權重 |
| `RESOLVE_SLOT_SECONDS` | 每格結算 UI 停留時間 |
| `COMBAT_*_X_RATIO` | 戰鬥中角色水平位置 |
| `BOSS_BASE_HP` / `BOSS_HP_PER_DISTANCE_BAND` | Boss HP |

玩家初始 HP 等見 `PlayerConfig` / `GameConfig` facade。

---

## 11. 檔案索引

| 路徑 | 用途 |
|------|------|
| `core/assets/cards/offense_cards.json` | 攻擊牌定義與效果步驟 |
| `core/assets/cards/change_cards.json` | 變化牌定義與效果步驟 |
| `combat/card/data/CardManifestLoader.java` | 合併多份 manifest |
| `core/assets/enemies/enemy_archetypes.json` | 敵人 HP、牌組、戰利品表（loot 預留） |
| `core/assets/stages/stages.json` | 線性關卡與原型 ID |
| `combat/card/ActionCardType.java` | 卡 ID enum；顯示/分類/冷卻自 JSON |
| `combat/card/ActionCardDeck.java` | 實例集合、`resetToDefault()` |
| `combat/card/data/CardDatabase.java` | 載入 JSON |
| `combat/system/CardEffectResolver.java` | 效果與條件模板 |
| `combat/system/CombatContext.java` | 結算當下上下文 |
| `combat/system/CombatController.java` | 戰鬥回合主控 |
| `combat/system/slots/WeightedPlayerSlotRoller.java` | 玩家格位對擲骰 |
| `combat/model/CombatEntity.java` | HP、護盾、狀態、回合末 tick |
| `combat/model/NegativeStatusType.java` | POISON / BLEED / FEAR |
| `config/CombatConfig.java` | 戰鬥調參 |
| `run/StageRunCoordinator.java` | 勝敗後 Hub / rewind |
| `screen/input/CombatCardInput.java` | 戰鬥輸入 |
| `screen/layout/CombatCardLayout.java` | 手牌兩列 + 資訊區幾何 |
| `presentation/CombatCardRenderer.java` | 卡面、格位、拖曳幽靈 |
| `presentation/CombatInfoPanelDrawer.java` | 描述資訊面板 |

---

## 12. 擴充新卡

1. 在 `offense_cards.json` 或 `change_cards.json` 新增一筆（`category` 須與檔案一致；`id` 與 enum 同名）。
2. 在 `ActionCardType` 新增 enum 常數。
3. 使用既有 `EffectTemplateId` / `ConditionType`；若需新行為，在 `CardEffectResolver` 註冊模板或條件後於 JSON 引用。
4. 以 `CardEffectResolverTest` 或整合測試覆蓋新步驟。

敵人新行為：擴充 `enemy_archetypes.json` 的 `deck`，必要時調整 `EnemyAi` 實作。
