package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.combat.enemy.RandomEnemyAi;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.config.CombatConfig;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;
import com.desertadventure.combat.system.slots.RandomIntSource;
import com.desertadventure.combat.system.slots.SlotRollWeights;
import com.desertadventure.combat.system.slots.WeightedPlayerSlotRoller;
import com.desertadventure.combat.system.presentation.PlayerAttackAnimation;
import com.desertadventure.player.PlayerStats;

import java.util.ArrayList;
import java.util.Arrays;
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

    enum DamageSource {
        OFFENSE_CARD,
        OTHER
    }

    // --- Slot rolling (domain rule, swappable) ---
    private final PlayerSlotRoller playerSlotRoller;

    // --- Presentation hints (owned externally; injected) ---
    private PlayerAttackAnimation playerAttackAnimation = new NoopPlayerAttackAnimation();

    /** Which two slots the player may use this round (0-based indices). */
    private int playerSlotA = 0;
    private int playerSlotB = 2;

    // --- Combat end outcome (produced immediately; finalization decided externally) ---
    private final CombatOutcomeFinalization outcomeFinalization = new CombatOutcomeFinalization();

    // --- Enemy archetype (normal fights only; boss uses legacy fixed attack) ---
    private EnemyArchetypeId currentEnemyArchetype;
    private ActionCardDeck enemyDeck;
    private final Set<Integer> enemyPlayedThisRound = new HashSet<>();
    private final EnemyAi enemyAi;
    private final RandomIntSource enemyHpRng;
    private final Integer[] enemySlotInstanceIds = new Integer[SLOT_COUNT];
    private final ActionCardType[] resolvedEnemySlotCards = new ActionCardType[SLOT_COUNT];
    private EnemyArchetypeId lastDefeatedEnemyArchetype;

    public CombatController(PlayerStats playerStats) {
        this(playerStats, defaultPlayerSlotRoller(), defaultEnemyAi(), defaultEnemyHpRng());
    }

    CombatController(PlayerStats playerStats, PlayerSlotRoller playerSlotRoller) {
        this(playerStats, playerSlotRoller, defaultEnemyAi(), defaultEnemyHpRng());
    }

    CombatController(
            PlayerStats playerStats,
            PlayerSlotRoller playerSlotRoller,
            EnemyAi enemyAi,
            RandomIntSource enemyHpRng) {
        this.playerStats = playerStats;
        this.playerSlotRoller = playerSlotRoller;
        this.enemyAi = enemyAi != null ? enemyAi : defaultEnemyAi();
        this.enemyHpRng = enemyHpRng != null ? enemyHpRng : defaultEnemyHpRng();
    }

    public void setPlayerAttackAnimation(PlayerAttackAnimation playerAttackAnimation) {
        this.playerAttackAnimation = playerAttackAnimation != null ? playerAttackAnimation : new NoopPlayerAttackAnimation();
    }

    public CombatOutcome getPendingOutcome() {
        return outcomeFinalization.getPendingOutcome();
    }

    public boolean hasPendingOutcome() {
        return outcomeFinalization.hasPendingOutcome();
    }

    /**
     * Finalizes a previously produced outcome: runs any deferred round-end cleanup and triggers the end callback.
     * Presentation layer decides when to call this (e.g., after attack animation finishes).
     */
    public void finalizePendingOutcome() {
        CombatOutcomeFinalization.ConsumedOutcome consumed = outcomeFinalization.consume();
        if (consumed == null) {
            return;
        }

        if (consumed.needsRoundCleanup()) {
            applyRoundEndEffects();
            clearAllSlots();
        }
        endCombat(consumed.outcome());
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

    public EnemyArchetypeId getCurrentEnemyArchetype() {
        return currentEnemyArchetype;
    }

    /** Archetype of the normal enemy defeated in the last VICTORY; null if none yet. */
    public EnemyArchetypeId getLastDefeatedEnemyArchetype() {
        return lastDefeatedEnemyArchetype;
    }

    public ActionCardType getEnemyCardForSlot(int slotIndex) {
        ActionCardInstance instance = getEnemySlotCard(slotIndex);
        if (instance != null) {
            return instance.getType();
        }
        if (!isEnemySlot(slotIndex)) {
            return null;
        }
        return currentEnemyArchetype == null ? ActionCardType.ATTACK : null;
    }

    private ActionCardInstance getEnemySlotCard(int slotIndex) {
        if (!isEnemySlot(slotIndex) || currentEnemyArchetype == null || enemyDeck == null) {
            return null;
        }
        Integer instanceId = enemySlotInstanceIds[slotIndex];
        if (instanceId == null) {
            return null;
        }
        return enemyDeck.findById(instanceId);
    }

    /** True when the given slot index is an enemy slot for the current round. */
    public boolean isEnemySlotIndex(int slotIndex) {
        return isEnemySlot(slotIndex);
    }

    /** Planned enemy card for UI/debug; null until rolled. */
    public ActionCardType getPlannedEnemyCardForSlot(int slotIndex) {
        return getEnemyCardForSlot(slotIndex);
    }

    /** Package-private: enemy deck instances for tests. */
    List<ActionCardInstance> enemyDeckInstancesForTests() {
        if (enemyDeck == null) {
            return List.of();
        }
        return enemyDeck.getInstances();
    }

    /** Package-private: used by tests to assert plan==resolve. */
    ActionCardType getResolvedEnemyCardForSlot(int slotIndex) {
        if (!isEnemySlot(slotIndex)) {
            return null;
        }
        if (currentEnemyArchetype == null) {
            return ActionCardType.ATTACK;
        }
        return resolvedEnemySlotCards[slotIndex];
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
        if (deck != null) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null) {
                return card;
            }
        }
        if (enemyDeck != null) {
            return enemyDeck.findById(instanceId);
        }
        return null;
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
        Arrays.fill(resolvedEnemySlotCards, null);
    }

    public void update(float delta) {
        if (!isActive()) {
            return;
        }
        if (outcomeFinalization.hasPendingOutcome()) {
            // Outcome already decided; core waits for presentation to finalize.
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
        if (combatEnded || outcomeFinalization.hasPendingOutcome()) {
            return;
        }
        resolvingSlotIndex++;
        if (resolvingSlotIndex >= SLOT_COUNT) {
            finishRound();
            return;
        }
        resolveTimer = CombatConfig.RESOLVE_SLOT_SECONDS;
    }

    private void resolveSlot(int slotIndex) {
        if (isEnemySlot(slotIndex)) {
            resolveEnemySlot();
        } else {
            resolvePlayerSlot(slotIndex);
        }
        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            // Defer end callback until presentation decides it's safe (e.g. let attack anim finish).
            outcomeFinalization.setPendingOutcome(outcome, true);
        }
    }

    /** Returns outcome if combat finished (player defeated or all enemies defeated). */
    private CombatOutcome checkCombatOutcomeIfFinished() {
        if (player == null || !player.isAlive()) {
            return CombatOutcome.DEFEAT;
        }
        enemies.removeIf(enemy -> !enemy.isAlive());
        if (enemies.isEmpty()) {
            if (!bossFight && currentEnemyArchetype != null) {
                lastDefeatedEnemyArchetype = currentEnemyArchetype;
            }
            return bossFight ? CombatOutcome.BOSS_VICTORY : CombatOutcome.VICTORY;
        }
        return null;
    }

    private void applyCardEffect(ActionCardType type) {
        applyCardEffect(type, EffectCaster.PLAYER);
    }

    private void applyCardEffect(ActionCardType type, EffectCaster caster) {
        Set<Integer> resolvedThisRound =
                caster == EffectCaster.ENEMY ? enemyPlayedThisRound : playedThisRound;
        CombatContext ctx = new CombatContext(this, roundNumber, resolvedThisRound, resolvingSlotIndex, caster);
        effectResolver.resolve(ctx, type);
    }

    void halveEnemyHp() {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.setHp((float) Math.floor(enemy.getHp() / 2f));
            }
        }
    }

    void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.setNegativeStatus(type, turns);
            }
        }
    }

    void dealDamageToEnemy(float amount) {
        dealDamageToEnemy(amount, DamageSource.OFFENSE_CARD);
    }

    void dealDamageToEnemy(float amount, DamageSource source) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                float finalAmount = amount;
                if (source == DamageSource.OFFENSE_CARD
                        && enemy.getNegativeStatusType() == NegativeStatusType.FEAR
                        && enemy.getNegativeTurnsRemaining() > 0) {
                    finalAmount += 1f;
                }
                enemy.takeDamage(finalAmount);
            }
        }
    }

    void healPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.heal(amount);
        syncPlayerStatsHp();
    }

    void healEnemy(float amount) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.heal(amount);
            }
        }
    }

    void addEnemyShield(int amount) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.addShield(amount);
            }
        }
    }

    void halvePlayerHp() {
        if (player == null) {
            return;
        }
        player.setHp((float) Math.floor(player.getHp() / 2f));
        syncPlayerStatsHp();
    }

    void applyNegativeStatusToPlayer(NegativeStatusType type, int turns) {
        if (player != null && player.isAlive()) {
            player.setNegativeStatus(type, turns);
        }
    }

    void dealDamageToPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.takeDamage(amount);
        syncPlayerStatsHp();
    }

    private void finishRound() {
        applyRoundEndEffects();
        clearAllSlots();

        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            outcomeFinalization.setPendingOutcome(outcome, false); // already cleaned up above
            return;
        }

        roundNumber++;
        rollPlayerSlotsForPlanning();
        rollEnemySlotCards();
        phase = CombatPhase.PLANNING;
        selectedInstanceId = null;
    }

    private void rollPlayerSlotsForPlanning() {
        PlayerSlotPlan plan = playerSlotRoller.rollPlan();
        setPlayerSlots(plan.slotA(), plan.slotB());
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
        float poisonDamage = CombatConfig.CARD_POISON_DAMAGE_PER_ROUND;
        if (player != null) {
            player.applyRoundEndStatusEffects(poisonDamage);
            syncPlayerStatsHp();
        }
        for (CombatEntity enemy : enemies) {
            enemy.applyRoundEndStatusEffects(poisonDamage);
        }
    }

    private void applyRoundEndCooldownsOnly() {
        applyRoundEndCooldownsForDeck(deck, playedThisRound);
        applyRoundEndCooldownsForDeck(enemyDeck, enemyPlayedThisRound);
    }

    private static void applyRoundEndCooldownsForDeck(ActionCardDeck actionDeck, Set<Integer> playedThisRound) {
        if (actionDeck == null) {
            playedThisRound.clear();
            return;
        }
        for (int instanceId : new HashSet<>(playedThisRound)) {
            ActionCardInstance card = actionDeck.findById(instanceId);
            if (card != null) {
                card.setCooldownRemaining(card.getType().getCooldownTurns());
            }
        }
        playedThisRound.clear();
        for (ActionCardInstance instance : actionDeck.getInstances()) {
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
        clearEnemySlots();
        enemyPlayedThisRound.clear();
        selectedInstanceId = null;
        resolveTimer = 0f;

        currentEnemyArchetype = boss ? null : EnemyArchetypeId.DESERT_ZOMBIE;
        if (currentEnemyArchetype != null) {
            EnemyArchetypeDef archetype = EnemyArchetypeRegistry.getRequired(currentEnemyArchetype);
            enemyDeck = ActionCardDeck.fromCardTypes(archetype.deckCardTypes());
        } else {
            enemyDeck = null;
        }

        player = createPlayerEntity(arenaWidth, groundY);
        enemies.add(createOpponentEntity(distanceBand, boss, arenaWidth, groundY));
        rollPlayerSlotsForPlanning();
        rollEnemySlotCards();
    }

    private CombatEntity createPlayerEntity(float arenaWidth, float groundY) {
        float playerX = arenaWidth * CombatConfig.COMBAT_PLAYER_X_RATIO;
        CombatEntity playerEntity = new CombatEntity(
                CombatEntity.Kind.PLAYER, playerX, groundY, playerStats.getMaxHp(), playerStats.getAttack(), 0f);
        playerEntity.setHp(playerStats.getHp());
        playerEntity.clearCombatStatus();
        return playerEntity;
    }

    private CombatEntity createOpponentEntity(int distanceBand, boolean boss, float arenaWidth, float groundY) {
        if (boss) {
            float bossHp = CombatConfig.BOSS_BASE_HP + distanceBand * CombatConfig.BOSS_HP_PER_DISTANCE_BAND;
            float bossX = arenaWidth * CombatConfig.COMBAT_BOSS_X_RATIO;
            CombatEntity bossEntity = new CombatEntity(CombatEntity.Kind.BOSS, bossX, groundY, bossHp, 0, 0f);
            bossEntity.clearCombatStatus();
            return bossEntity;
        }

        float enemyX = arenaWidth * CombatConfig.COMBAT_ENEMY_X_RATIO;
        EnemyArchetypeDef archetype = EnemyArchetypeRegistry.getRequired(currentEnemyArchetype);
        float enemyHp = archetype.rollMaxHp(enemyHpRng);
        CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, enemyX, groundY, enemyHp, 0, 0f);
        enemy.clearCombatStatus();
        return enemy;
    }

    private void rollEnemySlotCards() {
        clearEnemySlots();
        if (currentEnemyArchetype == null || enemyDeck == null) {
            return;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!isEnemySlot(i)) {
                continue;
            }
            List<ActionCardInstance> candidates = enemySlotPickCandidates();
            ActionCardInstance picked = enemyAi.pickCard(candidates);
            if (picked != null) {
                enemySlotInstanceIds[i] = picked.getInstanceId();
            }
        }
    }

    private List<ActionCardInstance> enemySlotPickCandidates() {
        List<ActionCardInstance> candidates = new ArrayList<>();
        if (enemyDeck == null) {
            return candidates;
        }
        Set<Integer> assigned = assignedEnemySlotInstanceIds();
        for (ActionCardInstance instance : enemyDeck.getInstances()) {
            if (instance.isOnCooldown()) {
                continue;
            }
            if (assigned.contains(instance.getInstanceId())) {
                continue;
            }
            candidates.add(instance);
        }
        return candidates;
    }

    private Set<Integer> assignedEnemySlotInstanceIds() {
        Set<Integer> ids = new HashSet<>();
        for (Integer id : enemySlotInstanceIds) {
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private void clearEnemySlots() {
        Arrays.fill(enemySlotInstanceIds, null);
    }

    private void resolveEnemySlot() {
        if (currentEnemyArchetype == null) {
            dealDamageToPlayer(ActionCardType.ATTACK.getPrimaryValue());
            return;
        }
        ActionCardInstance card = getEnemySlotCard(resolvingSlotIndex);
        if (card == null) {
            ActionCardInstance picked = enemyAi.pickCard(enemySlotPickCandidates());
            if (picked == null) {
                return;
            }
            card = picked;
            enemySlotInstanceIds[resolvingSlotIndex] = picked.getInstanceId();
        }
        resolvedEnemySlotCards[resolvingSlotIndex] = card.getType();
        applyCardEffect(card.getType(), EffectCaster.ENEMY);
        enemyPlayedThisRound.add(card.getInstanceId());
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
        return playerAttackAnimation.isAttacking();
    }

    /** Normalized progress for the current attack animation (0..1). */
    public float getPlayerAttackProgress(float attackDurationSeconds) {
        return playerAttackAnimation.getAttackProgress(attackDurationSeconds);
    }

    private void triggerPlayerAttackAnimation() {
        playerAttackAnimation.triggerAttack(CombatConfig.PLAYER_ATTACK_ANIM_SECONDS);
    }

    private static PlayerSlotRoller defaultPlayerSlotRoller() {
        SlotRollWeights weights = new SlotRollWeights(
                CombatConfig.PLAYER_SLOTS_WEIGHT_13,
                CombatConfig.PLAYER_SLOTS_WEIGHT_24,
                CombatConfig.PLAYER_SLOTS_WEIGHT_12,
                CombatConfig.PLAYER_SLOTS_WEIGHT_34);
        RandomIntSource rng = defaultEnemyHpRng();
        return new WeightedPlayerSlotRoller(weights, rng);
    }

    private static EnemyAi defaultEnemyAi() {
        return new RandomEnemyAi(defaultEnemyHpRng());
    }

    private static RandomIntSource defaultEnemyHpRng() {
        return bound -> ThreadLocalRandom.current().nextInt(bound);
    }

    private static final class NoopPlayerAttackAnimation implements PlayerAttackAnimation {
        @Override
        public void triggerAttack(float attackAnimSeconds) {
        }

        @Override
        public boolean isAttacking() {
            return false;
        }

        @Override
        public float getAttackProgress(float attackDurationSeconds) {
            return 1f;
        }
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
