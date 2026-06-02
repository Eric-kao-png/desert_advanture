package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.config.GameConfig;
import com.desertadventure.player.PlayerStats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/** Turn-based 1v1 card combat (PLANNING → RESOLVING slots 1–4 → win/loss / next round). */
public class CombatController {
    private static final int SLOT_COUNT = 4;
    private static final int FIRST_SLOT_INDEX = 0;

    private final PlayerStats playerStats;
    private CombatEntity player;
    private final List<CombatEntity> enemies = new ArrayList<>();
    private ActionCardDeck deck;
    private boolean bossFight;
    private float arenaWidth;
    private float groundY;
    private Consumer<CombatOutcome> onCombatEnd;
    private boolean combatEnded;

    private CombatPhase phase = CombatPhase.PLANNING;
    private int roundNumber = 1;
    private int resolvingSlotIndex;
    private float resolveTimer;
    private final Integer[] slotInstanceIds = new Integer[SLOT_COUNT];
    private final Set<Integer> playedThisRound = new HashSet<>();
    private boolean roundEndCooldownsApplied;
    private Integer selectedInstanceId;
    private final CardEffectResolver effectResolver = new CardEffectResolver();

    // --- Player animation state (presentation hint) ---
    private float playerAttackTimer;
    private CombatOutcome pendingCombatEndOutcome;
    private boolean pendingCombatEndNeedsRoundCleanup;

    /** Which two slots the player may use this round (0-based indices). */
    private int playerSlotA = 0;
    private int playerSlotB = 2;

    public CombatController(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public boolean isActive() {
        return player != null && !combatEnded;
    }

    public CombatPhase getPhase() {
        return phase;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getResolvingSlotDisplayIndex() {
        return resolvingSlotIndex + 1;
    }

    public Integer getSelectedInstanceId() {
        return selectedInstanceId;
    }

    public void setSelectedInstanceId(Integer instanceId) {
        selectedInstanceId = instanceId;
    }

    public void startCombat(
            int distanceBand,
            boolean boss,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        initializeCombatSession(distanceBand, boss, arenaWidth, groundY, actionDeck, onEnd);
    }

    public CombatEntity getPlayer() {
        return player;
    }

    public List<CombatEntity> getEnemies() {
        return enemies;
    }

    public boolean isBossFight() {
        return bossFight;
    }

    public ActionCardType getEnemyCardForSlot(int slotIndex) {
        if (isEnemySlot(slotIndex)) {
            return ActionCardType.ATTACK;
        }
        return null;
    }

    public Integer getSlotInstanceId(int slotIndex) {
        if (!isValidSlotIndex(slotIndex)) {
            return null;
        }
        return slotInstanceIds[slotIndex];
    }

    public ActionCardInstance getSlotCard(int slotIndex) {
        Integer id = getSlotInstanceId(slotIndex);
        if (id == null || deck == null) {
            return null;
        }
        return deck.findById(id);
    }

    public boolean isPlayerSlot(int slotIndex) {
        return slotIndex == playerSlotA || slotIndex == playerSlotB;
    }

    private boolean isEnemySlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < SLOT_COUNT && !isPlayerSlot(slotIndex);
    }

    private int otherPlayerSlot(int slotIndex) {
        return slotIndex == playerSlotA ? playerSlotB : playerSlotA;
    }

    /** Cards shown in the hand row (includes cooldown; excludes slot-assigned). */
    public List<ActionCardInstance> getVisibleHand() {
        List<ActionCardInstance> hand = new ArrayList<>();
        if (deck == null) {
            return hand;
        }
        Set<Integer> assigned = assignedInstanceIds();
        for (ActionCardInstance instance : deck.getInstances()) {
            if (!assigned.contains(instance.getInstanceId())) {
                hand.add(instance);
            }
        }
        return hand;
    }

    public boolean canAssignCard(ActionCardInstance instance) {
        if (instance == null || instance.isOnCooldown()) {
            return false;
        }
        return !assignedInstanceIds().contains(instance.getInstanceId());
    }

    /** Assignable hand cards only (not on cooldown, not in a slot). */
    public List<ActionCardInstance> getHandCandidates() {
        List<ActionCardInstance> hand = new ArrayList<>();
        if (deck == null) {
            return hand;
        }
        Set<Integer> assigned = assignedInstanceIds();
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance == null || instance.isOnCooldown()) {
                continue;
            }
            if (assigned.contains(instance.getInstanceId())) {
                continue;
            }
            hand.add(instance);
        }
        return hand;
    }

    public ActionCardInstance findCard(int instanceId) {
        if (deck == null) {
            return null;
        }
        return deck.findById(instanceId);
    }

    public boolean canConfirmPlanning() {
        return isActive() && phase == CombatPhase.PLANNING;
    }

    public void assignToPlayerSlot(int slotIndex, int instanceId) {
        if (!isActive() || phase != CombatPhase.PLANNING || !isPlayerSlot(slotIndex)) {
            return;
        }
        ActionCardInstance card = deck.findById(instanceId);
        if (card == null || card.isOnCooldown()) {
            return;
        }
        int otherPlayerSlot = otherPlayerSlot(slotIndex);
        Integer otherId = slotInstanceIds[otherPlayerSlot];
        if (otherId != null && otherId == instanceId) {
            return;
        }
        removeInstanceFromSlots(instanceId);
        slotInstanceIds[slotIndex] = instanceId;
        selectedInstanceId = null;
    }

    public void clearPlayerSlot(int slotIndex) {
        if (!isPlayerSlot(slotIndex)) {
            return;
        }
        slotInstanceIds[slotIndex] = null;
    }

    public void confirmPlanning() {
        if (!canConfirmPlanning()) {
            return;
        }
        phase = CombatPhase.RESOLVING;
        resolvingSlotIndex = 0;
        resolveTimer = 0f;
        roundEndCooldownsApplied = false;
    }

    public void update(float delta) {
        if (!isActive()) {
            return;
        }
        if (playerAttackTimer > 0f) {
            playerAttackTimer = Math.max(0f, playerAttackTimer - delta);
        }
        if (pendingCombatEndOutcome != null) {
            // Let attack animation finish before ending combat (prevents instant victory pop).
            if (playerAttackTimer <= 0f) {
                if (pendingCombatEndNeedsRoundCleanup) {
                    applyRoundEndEffects();
                    clearAllSlots();
                }
                CombatOutcome outcome = pendingCombatEndOutcome;
                pendingCombatEndOutcome = null;
                pendingCombatEndNeedsRoundCleanup = false;
                endCombat(outcome);
            }
            return;
        }
        if (phase != CombatPhase.RESOLVING) {
            return;
        }
        resolveTimer -= delta;
        if (resolveTimer > 0f) {
            return;
        }
        resolveSlot(resolvingSlotIndex);
        if (combatEnded) {
            return;
        }
        resolvingSlotIndex++;
        if (resolvingSlotIndex >= SLOT_COUNT) {
            finishRound();
            return;
        }
        resolveTimer = GameConfig.COMBAT_RESOLVE_SLOT_SECONDS;
    }

    private void resolveSlot(int slotIndex) {
        if (isEnemySlot(slotIndex)) {
            resolveEnemySlot();
        } else {
            resolvePlayerSlot(slotIndex);
        }
        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            if (shouldDelayCombatEndForPlayerAttack()) {
                pendingCombatEndOutcome = outcome;
                pendingCombatEndNeedsRoundCleanup = true;
                return;
            }
            endCombat(outcome);
            applyRoundEndEffects();
            clearAllSlots();
        }
    }

    /** Returns outcome if combat finished (player defeated or all enemies defeated). */
    private CombatOutcome checkCombatOutcomeIfFinished() {
        if (player == null || !player.isAlive()) {
            return CombatOutcome.DEFEAT;
        }
        enemies.removeIf(enemy -> !enemy.isAlive());
        if (enemies.isEmpty()) {
            return bossFight ? CombatOutcome.BOSS_VICTORY : CombatOutcome.VICTORY;
        }
        return null;
    }

    private void applyCardEffect(ActionCardType type) {
        CombatContext ctx = new CombatContext(this, roundNumber, playedThisRound);
        effectResolver.resolve(ctx, type);
    }

    private boolean changeCardResolvedThisRound() {
        if (deck == null) {
            return false;
        }
        for (int instanceId : playedThisRound) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null && card.getType().getCategory() == ActionCardCategory.CHANGE) {
                return true;
            }
        }
        return false;
    }

    void halveEnemyHp() {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.setHp((float) Math.floor(enemy.getHp() / 2f));
            }
        }
    }

    void applyNegativeStatusToEnemies(String statusId, int turns) {
        NegativeStatusType type = NegativeStatusType.valueOf(statusId);
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.setNegativeStatus(type, turns);
            }
        }
    }

    void dealDamageToEnemy(float amount) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.takeDamage(amount);
            }
        }
    }

    private void dealDamageToPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.takeDamage(amount);
        syncPlayerStatsHp();
    }

    void healPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.heal(amount);
        syncPlayerStatsHp();
    }

    private void finishRound() {
        applyRoundEndEffects();
        clearAllSlots();

        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            if (shouldDelayCombatEndForPlayerAttack()) {
                pendingCombatEndOutcome = outcome;
                pendingCombatEndNeedsRoundCleanup = false; // already cleaned up above
                return;
            }
            endCombat(outcome);
            return;
        }

        roundNumber++;
        rollPlayerSlotsForPlanning();
        phase = CombatPhase.PLANNING;
        selectedInstanceId = null;
    }

    private void rollPlayerSlotsForPlanning() {
        int w13 = GameConfig.COMBAT_PLAYER_SLOTS_WEIGHT_13;
        int w24 = GameConfig.COMBAT_PLAYER_SLOTS_WEIGHT_24;
        int w12 = GameConfig.COMBAT_PLAYER_SLOTS_WEIGHT_12;
        int w34 = GameConfig.COMBAT_PLAYER_SLOTS_WEIGHT_34;
        int total = Math.max(1, w13 + w24 + w12 + w34);
        int roll = ThreadLocalRandom.current().nextInt(total);
        if (roll < w13) {
            setPlayerSlots(0, 2); // 1 & 3
        } else if (roll < w13 + w24) {
            setPlayerSlots(1, 3); // 2 & 4
        } else if (roll < w13 + w24 + w12) {
            setPlayerSlots(0, 1); // 1 & 2
        } else {
            setPlayerSlots(2, 3); // 3 & 4
        }
    }

    private void setPlayerSlots(int a, int b) {
        playerSlotA = a;
        playerSlotB = b;
        // Clear any previously assigned cards in now-invalid player slots.
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotInstanceIds[i] != null && !isPlayerSlot(i)) {
                slotInstanceIds[i] = null;
            }
        }
    }

    /**
     * End-of-round: status effects, cooldowns (played cards get full CD then all instances tick once).
     * Runs when a round completes normally or combat ends mid-resolve.
     */
    private void applyRoundEndEffects() {
        if (roundEndCooldownsApplied) {
            return;
        }
        roundEndCooldownsApplied = true;
        applyRoundEndStatusEffects();
        applyRoundEndCooldownsOnly();
    }

    private void applyRoundEndStatusEffects() {
        float poisonDamage = GameConfig.CARD_POISON_DAMAGE_PER_ROUND;
        if (player != null) {
            player.applyRoundEndStatusEffects(poisonDamage);
            syncPlayerStatsHp();
        }
        for (CombatEntity enemy : enemies) {
            enemy.applyRoundEndStatusEffects(poisonDamage);
        }
    }

    private void applyRoundEndCooldownsOnly() {
        if (deck == null) {
            playedThisRound.clear();
            return;
        }
        for (int instanceId : new HashSet<>(playedThisRound)) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null) {
                card.setCooldownRemaining(card.getType().getCooldownTurns());
            }
        }
        playedThisRound.clear();
        for (ActionCardInstance instance : deck.getInstances()) {
            instance.tickCooldown();
        }
    }

    private void clearAllSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotInstanceIds[i] = null;
        }
    }

    private void removeInstanceFromSlots(int instanceId) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotInstanceIds[i] != null && slotInstanceIds[i] == instanceId) {
                slotInstanceIds[i] = null;
            }
        }
    }

    private Set<Integer> assignedInstanceIds() {
        Set<Integer> ids = new HashSet<>();
        for (Integer id : slotInstanceIds) {
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private boolean isValidSlotIndex(int slotIndex) {
        return slotIndex >= FIRST_SLOT_INDEX && slotIndex < SLOT_COUNT;
    }

    private void initializeCombatSession(
            int distanceBand,
            boolean boss,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        this.bossFight = boss;
        this.arenaWidth = arenaWidth;
        this.groundY = groundY;
        this.deck = actionDeck;
        this.onCombatEnd = onEnd;

        enemies.clear();
        combatEnded = false;
        phase = CombatPhase.PLANNING;
        roundNumber = 1;
        playedThisRound.clear();
        roundEndCooldownsApplied = false;
        clearAllSlots();
        selectedInstanceId = null;
        resolveTimer = 0f;

        player = createPlayerEntity(arenaWidth, groundY);
        enemies.add(createOpponentEntity(distanceBand, boss, arenaWidth, groundY));
        rollPlayerSlotsForPlanning();
    }

    private CombatEntity createPlayerEntity(float arenaWidth, float groundY) {
        float playerX = arenaWidth * GameConfig.COMBAT_PLAYER_X_RATIO;
        CombatEntity playerEntity = new CombatEntity(
                CombatEntity.Kind.PLAYER, playerX, groundY, playerStats.getMaxHp(), playerStats.getAttack(), 0f);
        playerEntity.setHp(playerStats.getHp());
        playerEntity.clearCombatStatus();
        return playerEntity;
    }

    private CombatEntity createOpponentEntity(int distanceBand, boolean boss, float arenaWidth, float groundY) {
        if (boss) {
            float bossHp = GameConfig.BOSS_BASE_HP + distanceBand * GameConfig.BOSS_HP_PER_DISTANCE_BAND;
            float bossX = arenaWidth * GameConfig.COMBAT_BOSS_X_RATIO;
            CombatEntity bossEntity = new CombatEntity(CombatEntity.Kind.BOSS, bossX, groundY, bossHp, 0, 0f);
            bossEntity.clearCombatStatus();
            return bossEntity;
        }

        float enemyX = arenaWidth * GameConfig.COMBAT_ENEMY_X_RATIO;
        int minHp = GameConfig.ENEMY_HP_MIN;
        int maxHp = GameConfig.ENEMY_HP_MAX;
        float enemyHp = ThreadLocalRandom.current().nextInt(minHp, maxHp + 1);
        CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, enemyX, groundY, enemyHp, 0, 0f);
        enemy.clearCombatStatus();
        return enemy;
    }

    private void resolveEnemySlot() {
        dealDamageToPlayer(ActionCardType.ATTACK.getPrimaryValue());
    }

    private void resolvePlayerSlot(int slotIndex) {
        Integer instanceId = slotInstanceIds[slotIndex];
        if (instanceId == null) {
            return;
        }
        ActionCardInstance card = deck.findById(instanceId);
        if (card == null) {
            return;
        }
        if (card.getType().getCategory() == ActionCardCategory.ATTACK) {
            triggerPlayerAttackAnimation();
        }
        applyCardEffect(card.getType());
        playedThisRound.add(instanceId);
    }

    public boolean isPlayerAttacking() {
        return playerAttackTimer > 0f;
    }

    /** Normalized progress for the current attack animation (0..1). */
    public float getPlayerAttackProgress(float attackDurationSeconds) {
        if (attackDurationSeconds <= 0f) {
            return 1f;
        }
        float remaining = Math.max(0f, Math.min(attackDurationSeconds, playerAttackTimer));
        return 1f - (remaining / attackDurationSeconds);
    }

    private void triggerPlayerAttackAnimation() {
        playerAttackTimer = Math.max(playerAttackTimer, GameConfig.COMBAT_PLAYER_ATTACK_ANIM_SECONDS);
    }

    private boolean shouldDelayCombatEndForPlayerAttack() {
        // Only delay if player is currently playing the attack animation.
        return playerAttackTimer > 0f;
    }

    private void syncPlayerStatsHp() {
        float hp = player != null ? player.getHp() : 0f;
        playerStats.setHp(Math.max(0f, hp));
    }

    private void endCombat(CombatOutcome result) {
        if (combatEnded) {
            return;
        }
        combatEnded = true;
        syncPlayerStatsHp();
        if (onCombatEnd != null) {
            onCombatEnd.accept(result);
        }
    }
}
