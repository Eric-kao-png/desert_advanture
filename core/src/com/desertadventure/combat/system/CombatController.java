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
    private RandomIntSource cardEffectRng = defaultCardEffectRng();
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

    public CombatOutcome getPendingOutcome() {
        return outcomeFinalization.getPendingOutcome();
    }

    public boolean hasPendingOutcome() {
        return outcomeFinalization.hasPendingOutcome();
    }

    /**
     * Finalizes a previously produced outcome: runs any deferred round-end cleanup and triggers the end callback.
     * Presentation layer may defer this until the current frame's draw/update cycle completes.
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
            int stageIndex,
            boolean boss,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        startCombat(stageIndex, boss, null, arenaWidth, groundY, actionDeck, onEnd);
    }

    public void startCombat(
            int stageIndex,
            boolean boss,
            EnemyArchetypeId tileEncounterArchetype,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        initializeCombatSession(
                stageIndex, boss, tileEncounterArchetype, arenaWidth, groundY, actionDeck, onEnd);
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

    /** English label for the non-player opponent; null for player-only views. */
    public String getOpponentDisplayName() {
        if (bossFight) {
            return "Boss";
        }
        if (currentEnemyArchetype == null) {
            return null;
        }
        return EnemyArchetypeRegistry.getRequired(currentEnemyArchetype).displayName();
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
        return collectUnassignedHandCards(false);
    }

    public boolean canAssignCard(ActionCardInstance instance) {
        if (instance == null || instance.isOnCooldown()) {
            return false;
        }
        return !assignedInstanceIds().contains(instance.getInstanceId());
    }

    /** Assignable hand cards only (not on cooldown, not in a slot). */
    public List<ActionCardInstance> getHandCandidates() {
        return collectUnassignedHandCards(true);
    }

    private List<ActionCardInstance> collectUnassignedHandCards(boolean excludeCooldown) {
        List<ActionCardInstance> hand = new ArrayList<>();
        if (deck == null) {
            return hand;
        }
        Set<Integer> assigned = assignedInstanceIds();
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance == null) {
                continue;
            }
            if (excludeCooldown && instance.isOnCooldown()) {
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
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy ->
                enemy.setHp((float) Math.floor(enemy.getHp() / 2f)));
    }

    void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> enemy.setNegativeStatus(type, turns));
    }

    void clearNegativeStatusOnPlayer() {
        if (player != null && player.isAlive()) {
            player.clearNegativeStatus();
        }
    }

    void clearNegativeStatusOnEnemies() {
        CombatEntityRoster.forEachAliveEnemy(enemies, CombatEntity::clearNegativeStatus);
    }

    void transferNegativeStatusFromPlayerToEnemies() {
        if (player == null || !player.hasNegativeStatus()) {
            return;
        }
        NegativeStatusType type = player.getNegativeStatusType();
        int turns = player.getNegativeTurnsRemaining();
        player.clearNegativeStatus();
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> enemy.setNegativeStatus(type, turns));
    }

    void transferNegativeStatusFromEnemyToPlayer() {
        CombatEntity enemy = CombatEntityRoster.firstAliveEnemy(enemies);
        if (enemy == null || !enemy.hasNegativeStatus() || player == null) {
            return;
        }
        NegativeStatusType type = enemy.getNegativeStatusType();
        int turns = enemy.getNegativeTurnsRemaining();
        enemy.clearNegativeStatus();
        if (player.isAlive()) {
            player.setNegativeStatus(type, turns);
        }
    }

    boolean enemyHasNegativeStatus() {
        CombatEntity enemy = CombatEntityRoster.firstAliveEnemy(enemies);
        return enemy != null && enemy.hasNegativeStatus();
    }

    /**
     * One roll for poison bolt: &lt;25 → 1 turn, &lt;50 → 2 turns, else none (mutually exclusive).
     */
    int rollPoisonBoltPoisonTurns() {
        int roll = cardEffectRng.nextInt(100);
        if (roll < 25) {
            return 1;
        }
        if (roll < 50) {
            return 2;
        }
        return 0;
    }

    /** Roll 0–99; succeeds when roll &lt; {@code chancePercent}. */
    boolean rollPercentChance(int chancePercent) {
        return cardEffectRng.nextInt(100) < chancePercent;
    }

    void setCardEffectRngForTests(RandomIntSource rng) {
        cardEffectRng = rng != null ? rng : defaultCardEffectRng();
    }

    void dealDamageToEnemy(float amount) {
        dealDamageToEnemy(amount, DamageSource.OFFENSE_CARD);
    }

    void dealDamageToEnemy(float amount, DamageSource source) {
        damageAliveEnemies(amount, source, false);
    }

    void healPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.heal(amount);
        syncPlayerStatsHp();
    }

    void healEnemy(float amount) {
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> enemy.heal(amount));
    }

    void addEnemyShield(int amount) {
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> enemy.addShield(amount));
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

    void dealDamageToEnemyIgnoringShield(float amount) {
        damageAliveEnemies(amount, DamageSource.OFFENSE_CARD, true);
    }

    private void damageAliveEnemies(float amount, DamageSource source, boolean ignoreShield) {
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> {
            float finalAmount = source == DamageSource.OFFENSE_CARD
                    ? CombatEntityRoster.offenseDamageWithFearBonus(amount, enemy)
                    : amount;
            if (ignoreShield) {
                enemy.takeDamageIgnoringShield(finalAmount);
            } else {
                enemy.takeDamage(finalAmount);
            }
        });
    }

    void dealDamageToPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.takeDamage(amount);
        syncPlayerStatsHp();
    }

    void dealDamageToPlayerIgnoringShield(float amount) {
        if (player == null) {
            return;
        }
        player.takeDamageIgnoringShield(amount);
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
     * End-of-round: status effects, cooldowns (tick existing CD, then played cards enter full CD).
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
        RoundEndCooldownApplier.apply(deck, playedThisRound);
        RoundEndCooldownApplier.apply(enemyDeck, enemyPlayedThisRound);
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
            int stageIndex,
            boolean boss,
            EnemyArchetypeId tileEncounterArchetype,
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

        currentEnemyArchetype = boss
                ? null
                : EnemyArchetypeRegistry.resolveNormalEncounter(tileEncounterArchetype, enemyHpRng);
        if (currentEnemyArchetype != null) {
            EnemyArchetypeDef archetype = EnemyArchetypeRegistry.getRequired(currentEnemyArchetype);
            enemyDeck = ActionCardDeck.fromCardTypes(archetype.deckCardTypes());
        } else {
            enemyDeck = null;
        }

        player = createPlayerEntity(arenaWidth, groundY);
        enemies.add(createOpponentEntity(stageIndex, boss, arenaWidth, groundY));
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

    private CombatEntity createOpponentEntity(int stageIndex, boolean boss, float arenaWidth, float groundY) {
        if (boss) {
            float bossHp = CombatConfig.BOSS_BASE_HP + stageIndex * CombatConfig.BOSS_HP_PER_DISTANCE_BAND;
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
        applyCardEffect(card.getType());
        playedThisRound.add(instanceId);
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

    private static RandomIntSource defaultCardEffectRng() {
        return bound -> ThreadLocalRandom.current().nextInt(bound);
    }

    private static RandomIntSource defaultEnemyHpRng() {
        return bound -> ThreadLocalRandom.current().nextInt(bound);
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
