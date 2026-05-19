# Desert Adventure — 背景捲動邏輯

探索／戰鬥畫面的**橫向視差背景**、**地板拼貼**、**房屋裝飾**之捲動、繪製與生成規則。與網格移動、步數的關係見 [`Movement-and-Map-Logic.md`](Movement-and-Map-Logic.md)。

**對齊實作版本：** 2026 年 5 月（`core` 模組，LibGDX 1.12，Java 17）

---

## 1. 總覽

| 概念 | 說明 |
|------|------|
| **視覺捲動** | 玩家站在畫面左側約 35% 處，世界「向左流過」；由 `SCROLL_SPEED` 與各層倍率驅動 |
| **何時捲動** | 僅在 `drawParallaxBackground(..., running=true, ...)` 時累加 scroll；`running` 通常對應 `GameplayMode.RUNNING` |
| **與地圖座標** | 背景 scroll **不**綁定 `GameMap` 格子；邏輯移動在 `PathRunner` / `TravelMovement`，兩者並行 |
| **`GameSession.scrollOffset`** | 累計「邏輯距離」用的數值（路徑段結束時也會加），**目前未用於繪製** |

### 1.1 類別分工

```
GameplayScreen
    └── drawExploreScene / 戰鬥背景
            └── GameplayRenderer.drawParallaxBackground()
                    ├── ParallaxBackground   （三層 PNG + 地板）
                    └── BackgroundHouseSpawner（房屋，插在 back 與 middle 之間）
```

| 檔案 | 職責 |
|------|------|
| `presentation/GameplayRenderer.java` | 門面：組合捲動、繪製順序、探索前景（玩家方塊、頂部 HUD 底） |
| `presentation/SkyBackdrop.java` | 米黃全屏底 + 固定太陽（不隨捲動） |
| `presentation/ParallaxBackground.java` | 三張視差圖 + 地板 `TextureRegion` 的水平拼貼 |
| `presentation/BackgroundHouseSpawner.java` | 房屋列表、捲動座標、移動中／重填生成 |
| `presentation/sprites/DesertSpriteAtlas.java` | 從 JSON 裁切地板與房屋 sprite |
| `config/GameConfig.java` | 速度、倍率、房屋生成參數 |
| `screen/GameplayScreen.java` | 依 `GameplayMode` 決定 `running`、何時 `repopulateHouseProps` |
| `exploration/PathRunner.java` | `getScrollOffset(delta)` → 餵給 `GameSession`（非繪製） |

---

## 2. 繪製順序（Z 序）

`GameplayRenderer.drawParallaxBackground` **必須**在已 `SpriteBatch.begin()` 的狀態下呼叫。順序固定為：

0. **sky** — 全螢幕米黃底色（`GameConfig.SKY_BASE_COLOR`）+ 固定太陽 `sprites/Sun.png`（`SkyBackdrop`）
1. **back** — `backgrounds/parallax_back.png`
2. **houses** — `desert_house_1/2/3`（`BackgroundHouseSpawner`）
3. **middle** — `backgrounds/parallax_middle.png`
4. **forward** — `backgrounds/parallax_forward.png`
5. **floor** — `floor_tile`（圖集裁切，非全屏 PNG）

之後（同一幀、**另一個** `ShapeRenderer`  pass）：

6. `renderExploreForeground` — 玩家 placeholder、畫面上方半透明條
7. 地圖覆蓋層／沙暴／戰鬥實體等（依模式）

房屋若畫在 middle **之後**，會被 middle／forward 全屏圖蓋住；若畫在 back **之前** 則過暗。目前插在 **back 與 middle 之間**。

---

## 3. 何時捲動、何時靜止

`GameplayScreen` 傳入的 `running` 旗標：

| `GameplayMode` | `running` | 備註 |
|----------------|-----------|------|
| `RUNNING` | `true` | 路徑動畫中，視差與房屋皆前進 |
| `EXPLORE_IDLE` | `false` | 站立，背景凍結 |
| `MAP_OVERLAY` | `false` | 開地圖時仍畫探索場景，但不捲動 |
| `STORM` | `false` | 沙暴淡出；進入 STORM 時會 **重填房屋** |
| `COMBAT` / `BOSS_COMBAT` | `false` | 只畫視差底圖（靜止），無房屋捲動累加 |
| `VICTORY` | — | 切換畫面，不經此流程 |

**房屋重填**（`GameplayRenderer.repopulateHouseProps`）時機：

- 新遊戲 `GameplayScreen.show()`
- 剛進入 `STORM`（`lastMode != STORM` → `STORM`）

重填會 `props.clear()`、`scrollProps = 0`，並在螢幕寬度內隨機嘗試生成（見 §6）。

---

## 4. 設定常數（`GameConfig`）

### 4.1 天空底色與太陽

| 常數 | 值 | 用途 |
|------|-----|------|
| `SKY_BASE_COLOR` | RGB ≈ `(245, 230, 199)` | 全屏米黃；`GameplayScreen` GL clear 同色 |
| `SUN_TEXTURE_PATH` | `sprites/Sun.png` | 32×32 原圖 |
| `SUN_DISPLAY_SIZE` | `112` | 螢幕繪製邊長 |
| `SUN_X` / `SUN_Y` | 左上 + `SUN_VERTICAL_OFFSET`（**-190**） | 太陽左下角座標，**不捲動** |

視差層若天空區域不透明，太陽會被遮住；可調資源透明度或將 `sky.draw` 改到 `drawBack` 之後。

### 4.2 視差垂直偏移

| 常數 | 值 | 用途 |
|------|-----|------|
| `PARALLAX_VERTICAL_OFFSET` | `-40` | 各層共用基準 |
| `PARALLAX_BACK_EXTRA_OFFSET` | `-150` | 遠景 back（合計 Y = **-190**） |
| `PARALLAX_MIDDLE_EXTRA_OFFSET` | `-40` | 中景 middle 與房屋（合計 **-80**） |
| `PARALLAX_FORWARD_EXTRA_OFFSET` | `-30` | 僅 forward 層（forward 合計 Y = **-70**） |

地板、玩家不受視差偏移影響；太陽使用獨立的 `SUN_VERTICAL_OFFSET`。

### 4.3 視差速度

基準：`SCROLL_SPEED = 200`（像素／秒，邏輯單位與螢幕座標一致）。

每幀增量（`running == true`）：

```
base = SCROLL_SPEED * delta
scrollBack     += base * PARALLAX_BACK_MULT      // 0.25
scrollMiddle   += base * PARALLAX_MIDDLE_MULT    // 0.55
scrollForward  += base * PARALLAX_FORWARD_MULT   // 1.0
scrollProps    += base * HOUSE_PROP_PARALLAX_MULT // (0.25+0.55)/2 = 0.4
```

後景最慢、前景最快，製造深度感。房屋速度取 back 與 middle 之間。

### 4.4 房屋生成

| 常數 | 值 | 用途 |
|------|-----|------|
| `HOUSE_SPAWN_CHANCE` | `0.22` | 每次「嘗試生成」通過的機率 |
| `HOUSE_SPAWN_GAP_MIN` / `MAX` | `520` / `880` | 移動中，累積捲動距離達此區間隨機值後觸發一次嘗試 |
| `HOUSE_SPAWN_JITTER` | `200` | 移動中生成時，`scrollX = scrollProps + random(0, jitter)` |
| `HOUSE_OVERLAP_MAX` | `2` | 與現有房屋水平重疊數 ≥ 此值則放棄本次生成 |
| `HOUSE_REPOPULATE_ATTEMPTS` | `4` | 開局／沙暴重填時的隨機 X 嘗試次數 |
| `HOUSE_PROP_MIN_Y` / `MAX_Y` | `340` / `390` | 房屋 **底邊** 的螢幕 Y（LibGDX 左下原點） |
| `HOUSE_PROP_DISPLAY_HEIGHT` | `112` | 繪製高度；寬度依 sprite 寬高比 |

### 4.5 探索前景

| 常數 | 位置 | 值 |
|------|------|-----|
| `EXPLORE_GROUND_Y` | `GameplayRenderer` | `120` — 地板高度、玩家腳下 Y |
| 玩家螢幕 X | `renderExploreForeground` | `VIEW_WIDTH * 0.35f` |

---

## 5. `ParallaxBackground` — 三層 PNG 與地板

### 5.1 資源路徑

| 層 | 檔案 |
|----|------|
| Back | `core/assets/backgrounds/parallax_back.png` |
| Middle | `core/assets/backgrounds/parallax_middle.png` |
| Forward | `core/assets/backgrounds/parallax_forward.png` |
| Floor | `sprites/desert_sheet.png` 內 `floor_tile`（見 JSON） |

載入時 `TextureFilter.Linear`。生命週期：`GameplayRenderer.dispose()` → `parallax.dispose()`（三張 PNG；地板 region 的獨立 Texture 由 `DesertSpriteAtlas` 釋放）。

### 5.2 全屏層拼貼（`drawTiled`）

對每一層：

1. `scale = screenH / texture.getHeight()` — 高度撐滿視窗  
2. `tileW = texture.getWidth() * scale` — 單塊拼貼寬度  
3. `offset = scroll % tileW`（負數時 `+= tileW` 正規化）  
4. 從 `x = -offset` 開始，步進 `tileW` 直到蓋滿 `screenW`；繪製 Y 依層為 base、base+middle extra、或 base+forward extra

`scroll` 為該層累積的**虛擬世界 X**（只增不減，除非未來手動重置）。數值越大，圖樣越往左移（捲動方向為「場景向左」）。

### 5.3 地板（`drawFloor`）

- 使用 **`scrollForward`** 作為水平 offset（與最前景同速），視覺上貼在前景層腳下。  
- `tileH = groundY`（`EXPLORE_GROUND_Y`）  
- `tileW = tileH * (regionW / regionH)`  
- 同樣 `offset = scrollForward % tileW` 後水平拼貼，`y = 0`。

JSON 中地板矩形（左下原點）：`left=0, bottom=64, right=32, top=128` → 32×64 邏輯像素，經 `DesertSpriteAtlas` Pixmap 裁切為獨立 `TextureRegion`。

---

## 6. `BackgroundHouseSpawner` — 房屋

### 6.1 座標系

每棟房屋存 **`scrollX`**（世界捲動空間中的 X，生成時的絕對位置）：

```
screenX = prop.scrollX - scrollProps
```

- `scrollProps`：與房屋層同步累積的捲動量（§4.1）。  
- 繪製時用 `screenX` 當 `SpriteBatch.draw` 的 x。  
- 離開螢幕左側過遠（`< -maxWidth`）的 prop 會從 list **移除**。

### 6.2 移動中生成（`scroll`）

僅 `running == true`：

1. 更新 `scrollProps` 與 `distanceSinceLastSpawn`（增量皆為 `SCROLL_SPEED * delta * HOUSE_PROP_PARALLAX_MULT`）。  
2. 若 `distanceSinceLastSpawn >= nextSpawnGap`：  
   - 重置計距、`nextSpawnGap = random(GAP_MIN, GAP_MAX)`  
   - `trySpawn(scrollProps + random(0, JITTER))`

### 6.3 `trySpawn` 條件

1. `random() <= HOUSE_SPAWN_CHANCE`  
2. 隨機 `houseIndex` ∈ {1,2,3}  
3. 以即將繪製的 `screenX` 計算寬度，與現有 props 水平區間重疊數 `< HOUSE_OVERLAP_MAX`  
4. `baseY = random(MIN_Y, MAX_Y)`  
5. 加入 `HouseProp(scrollX, houseIndex, baseY)`

### 6.4 重填（`repopulate`）

- 清空列表、`scrollProps = 0`、重設 `nextSpawnGap`。  
- `margin = 180`，在 `[margin, screenW - margin]` 內均勻隨機 `scrollX`，呼叫 `trySpawn` 共 `HOUSE_REPOPULATE_ATTEMPTS` 次（仍受機率與重疊限制）。

### 6.5 繪製尺寸

```
drawW = HOUSE_PROP_DISPLAY_HEIGHT * (regionW / regionH)
drawH = HOUSE_PROP_DISPLAY_HEIGHT
```

`batch.draw(region, screenX, baseY, drawW, drawH)` — `baseY` 為 sprite **底邊**。

---

## 7. `GameplayRenderer` 門面

### 7.1 建構

1. `DesertSpriteAtlas`  
2. `ParallaxBackground(atlas.get(FLOOR_TILE))`  
3. `BackgroundHouseSpawner(atlas)`  
4. `MapOverlayRenderer`（與背景無關，同一門面管理）

投影：`setProjectionMatrix` 同步到 `screenProjection` 與 map overlay。

### 7.2 `drawParallaxBackground` 流程

```text
parallax.scroll(delta, running)
houses.scroll(delta, running)
batch.setProjectionMatrix(screenProjection)
parallax.drawBack → houses.draw → parallax.drawMiddle → drawForward → drawFloor(EXPLORE_GROUND_Y)
```

### 7.3 探索前景

- `ShapeRenderer` 畫玩家矩形；`RUNNING` 時 `sin(time)*4` 上下晃動。  
- 頂部 `y = VIEW_HEIGHT - 60` 半透明黑條（HUD 文字在 `GameplayHud` 另層繪製）。

---

## 8. 與移動系統的關係（非繪製）

### 8.1 `PathRunner.getScrollOffset`

路徑動畫進行中每幀：

```java
return GameConfig.SCROLL_SPEED * delta;  // 無視差倍率
```

### 8.2 `GameSession.updateRunning`

```java
scrollOffset += travel.getPathRunner().getScrollOffset(delta);
travel.update(delta);
```

### 8.3 `TravelMovement.onPathComplete`

路徑段走完、消耗剩餘步數時：

```java
session.addScrollOffset(50f * remaining);
```

`remaining` 為該段尚未計入的歐氏距離。係數 `50` 為**額外邏輯累計**，與視差 `SCROLL_SPEED` 無直接換算；**目前沒有任何 renderer 讀取 `getScrollOffset()`**。

若未來要做「小地圖里程／成就／同步背景」，可在此掛鉤或改為驅動 `scrollProps` 重置策略。

---

## 9. 戰鬥與其他模式

- **戰鬥**：`drawParallaxBackground(batch, false, delta)` — 背景靜止，不更新房屋 scroll；戰鬥實體用 `ShapeRenderer` 畫在 batch 之上。  
- **地圖覆蓋**：先 `drawExploreScene(false)` 再 `renderMapOverlay` — 背景靜止、玩家仍顯示。  
- **沙暴**：背景靜止 + `renderStorm(progress)` 全屏淡黃遮罩。

---

## 10. 修改指南

### 10.1 調整捲動手感

| 目標 | 建議修改 |
|------|----------|
| 整體更快／慢 | `GameConfig.SCROLL_SPEED` |
| 景深更強 | 拉大 `PARALLAX_*_MULT` 差距（back 更小、forward 維持 1） |
| 地板與前景不同步 | `ParallaxBackground.drawFloor` 目前綁 `scrollForward`；可改為獨立 scroll 或倍率 |
| 房屋像在中景 | `HOUSE_PROP_PARALLAX_MULT`（現為 back/middle 平均） |

### 10.2 調整房屋密度

- 提高出現率：`HOUSE_SPAWN_CHANCE` 或縮小 `HOUSE_SPAWN_GAP_*`  
- 允許更擠：`HOUSE_OVERLAP_MAX`  
- 開局更多棟：提高 `HOUSE_REPOPULATE_ATTEMPTS` 或暫時設 `CHANCE = 1` 測試  

### 10.3 常見問題

| 現象 | 可能原因 |
|------|----------|
| 房屋看不見 | 繪製順序錯誤（不在 back/middle 之間）；或 `baseY` 超出視窗 |
| 地板拉花／錯格 | JSON 矩形與圖集不一致；勿直接用 `TextureRegion(y=64)` 而未經 Pixmap 裁切 |
| 背景不動 | `running` 為 false（非 `RUNNING` 模式） |
| 沙暴後房屋重疊異常 | `repopulate` 只重試 4 次且受機率限制，屬預期隨機性 |

### 10.4 新增裝飾層

1. 新增類似 `BackgroundHouseSpawner` 的列表 + `scrollX` 邏輯。  
2. 在 `GameplayRenderer.drawParallaxBackground` 插入 `draw` 呼叫（注意 Z 序）。  
3. 常數集中放在 `GameConfig`。  
4. 若需與沙暴同步重置，在 `GameplayScreen.detectModeTransitions` 增加呼叫。

---

## 11. 資產清單

| 資產 | 路徑 |
|------|------|
| 視差後景 | `core/assets/backgrounds/parallax_back.png` |
| 視差中景 | `core/assets/backgrounds/parallax_middle.png` |
| 視差前景 | `core/assets/backgrounds/parallax_forward.png` |
| 精靈圖集 | `core/assets/sprites/desert_sheet.png` |
| 裁切定義 | `core/assets/data/desert_sprite_sheet.json` |

---

## 12. 相關文件

- [`Movement-and-Map-Logic.md`](Movement-and-Map-Logic.md) — 直線移動、`PathRunner`、`RUNNING` 模式、途中遇格  
- 執行遊戲：`gradle :desktop:runGame`
