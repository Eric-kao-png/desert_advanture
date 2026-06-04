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
import com.desertadventure.combat.model.PositiveStatusType;
import com.desertadventure.combat.status.StatusEffectRuntime;
import com.desertadventure.config.CombatConfig;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;
import com.desertadventure.combat.system.slots.RandomIntSource;
import com.desertadventure.combat.system.slots.SlotRollWeights;
import com.desertadventure.combat.system.slots.WeightedPlayerSlotRoller;
import com.desertadventure.player.PlayerStats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/** Turn-based 1v1 card combat (PLANNING → RESOLVING slots 1–4 → win/loss / next round). */
public class CombatController {
    private static final int FIRST_SLOT_INDEX = 0;

    private final PlayerStats playerStats;
    private final CombatSlotManager playerSlots = new CombatSlotManager();
    private final EnemySlotPlanner enemySlots;
    private final CombatEntityFactory entityFactory;
    private CombatEntity player;
    private final List<CombatEntity> enemies = new ArrayList<>();
    private ActionCardDeck deck;
    private boolean bossFight;
    private Consumer<CombatOutcome> onCombatEnd;
    private boolean combatEnded;

    private CombatPhase phase = CombatPhase.PLANNING;
    private int roundNumber = 1;
    private int resolvingSlotIndex;
    private float resolveTimer;
    private final Set<Integer> playedThisRound = new HashSet<>();
    private boolean roundEndCooldownsApplied;
    private Integer selectedInstanceId;
    private final CardEffectResolver effectResolver = new CardEffectResolver();

    enum DamageSource {
        OFFENSE_CARD,
        OTHER
    }

    private final PlayerSlotRoller playerSlotRoller;
    private final CombatOutcomeFinalization outcomeFinalization = new CombatOutcomeFinalization();
    private final Set<Integer> enemyPlayedThisRound = new HashSet<>();
    private final EnemyAi enemyAi;
    private final RandomIntSource enemyHpRng;
    private RandomIntSource cardEffectRng = defaultCardEffectRng();

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
        this.enemySlots = new EnemySlotPlanner(this.enemyAi, playerSlots);
        this.entityFactory = new CombatEntityFactory(playerStats, this.enemyHpRng);
    }

    public CombatOutcome getPendingOutcome() {
        return outcomeFinalization.getPendingOutcome();
    }

    public boolean hasPendingOutcome() {
        return outcomeFinalization.hasPendingOutcome();
    }

    public void finalizePendingOutcome() {
        CombatOutcomeFinalization.ConsumedOutcome consumed = outcomeFinalization.consume();
        if (consumed == null) {
            return;
        }

        if (consumed.needsRoundCleanup()) {
            applyRoundEndEffects();
            playerSlots.clearPlayerSlots();
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
            EnemyArchetypeId encounterArchetype,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        initializeCombatSession(
                stageIndex, boss, encounterArchetype, arenaWidth, groundY, actionDeck, onEnd);
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
        return enemySlots.getCurrentEnemyArchetype();
    }

    public String getOpponentDisplayName() {
        if (bossFight) {
            return "Boss";
        }
        EnemyArchetypeId archetype = enemySlots.getCurrentEnemyArchetype();
        if (archetype == null) {
            return null;
        }
        return EnemyArchetypeRegistry.getRequired(archetype).displayName();
    }

    public ActionCardType getEnemyCardForSlot(int slotIndex) {
        return enemySlots.getEnemyCardForSlot(slotIndex);
    }

    public boolean isEnemySlotIndex(int slotIndex) {
        return playerSlots.isEnemySlot(slotIndex);
    }

    public ActionCardType getPlannedEnemyCardForSlot(int slotIndex) {
        return getEnemyCardForSlot(slotIndex);
    }

    List<ActionCardInstance> enemyDeckInstancesForTests() {
        return enemySlots.enemyDeckInstancesForTests();
    }

    ActionCardType getResolvedEnemyCardForSlot(int slotIndex) {
        return enemySlots.getResolvedEnemyCardForSlot(slotIndex);
    }

    public Integer getSlotInstanceId(int slotIndex) {
        return playerSlots.getSlotInstanceId(slotIndex);
    }

    public ActionCardInstance getSlotCard(int slotIndex) {
        Integer id = playerSlots.getSlotInstanceId(slotIndex);
        if (id == null) {
            return null;
        }
        return findCard(id);
    }

    void setPlayerSlotInstanceForTest(int slotIndex, int instanceId) {
        playerSlots.setPlayerSlotInstanceForTest(slotIndex, instanceId);
    }

    boolean previousSlotIsPlayerOffense(EffectCaster caster, int resolvingSlotIndex) {
        if (resolvingSlotIndex <= FIRST_SLOT_INDEX || caster == EffectCaster.ENEMY) {
            return false;
        }
        int previousIndex = resolvingSlotIndex - 1;
        if (!playerSlots.isPlayerSlot(previousIndex)) {
            return false;
        }
        ActionCardInstance card = getSlotCard(previousIndex);
        return card != null && card.getType().getCategory() == ActionCardCategory.ATTACK;
    }

    boolean roundHasUsedCategory(ActionCardCategory category, EffectCaster caster) {
        for (int slotIndex = 0; slotIndex < CombatSlotManager.SLOT_COUNT; slotIndex++) {
            if (caster == EffectCaster.ENEMY) {
                if (!playerSlots.isEnemySlot(slotIndex)) {
                    continue;
                }
                ActionCardInstance card = enemySlots.getEnemySlotCard(slotIndex);
                if (card != null && card.getType().getCategory() == category) {
                    return true;
                }
            } else {
                if (!playerSlots.isPlayerSlot(slotIndex)) {
                    continue;
                }
                ActionCardInstance card = getSlotCard(slotIndex);
                if (card != null && card.getType().getCategory() == category) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPlayerSlot(int slotIndex) {
        return playerSlots.isPlayerSlot(slotIndex);
    }

    public List<ActionCardInstance> getVisibleHand() {
        return playerSlots.collectUnassignedHandCards(deck, false);
    }

    public boolean canAssignCard(ActionCardInstance instance) {
        return playerSlots.canAssignCard(instance);
    }

    public List<ActionCardInstance> getHandCandidates() {
        return playerSlots.collectUnassignedHandCards(deck, true);
    }

    public ActionCardInstance findCard(int instanceId) {
        if (deck != null) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null) {
                return card;
            }
        }
        ActionCardDeck enemyDeck = enemySlots.getEnemyDeck();
        if (enemyDeck != null) {
            return enemyDeck.findById(instanceId);
        }
        return null;
    }

    public boolean canConfirmPlanning() {
        return isActive() && phase == CombatPhase.PLANNING;
    }

    public void assignToPlayerSlot(int slotIndex, int instanceId) {
        if (!isActive() || phase != CombatPhase.PLANNING) {
            return;
        }
        if (playerSlots.assignToPlayerSlot(slotIndex, instanceId, deck)) {
            selectedInstanceId = null;
        }
    }

    public void clearPlayerSlot(int slotIndex) {
        playerSlots.clearPlayerSlot(slotIndex);
    }

    public void confirmPlanning() {
        if (!canConfirmPlanning()) {
            return;
        }
        phase = CombatPhase.RESOLVING;
        resolvingSlotIndex = 0;
        resolveTimer = 0f;
        roundEndCooldownsApplied = false;
        enemySlots.clearResolvedEnemySlotCards();
    }

    public void update(float delta) {
        if (!isActive()) {
            return;
        }
        if (outcomeFinalization.hasPendingOutcome()) {
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
        if (resolvingSlotIndex >= CombatSlotManager.SLOT_COUNT) {
            finishRound();
            return;
        }
        resolveTimer = CombatConfig.RESOLVE_SLOT_SECONDS;
    }

    private void resolveSlot(int slotIndex) {
        if (playerSlots.isEnemySlot(slotIndex)) {
            resolveEnemySlot();
        } else {
            resolvePlayerSlot(slotIndex);
        }
        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            outcomeFinalization.setPendingOutcome(outcome, true);
        }
    }

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
        applyCardEffect(type, EffectCaster.PLAYER);
    }

    private void applyCardEffect(ActionCardType type, EffectCaster caster) {
        CombatContext ctx = new CombatContext(this, roundNumber, resolvingSlotIndex, caster);
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
        boolean ignoreShield = source == DamageSource.OFFENSE_CARD
                && player != null
                && StatusEffectRuntime.casterIgnoresShieldOnOffense(player);
        damageAliveEnemies(amount, source, ignoreShield);
        if (source == DamageSource.OFFENSE_CARD && player != null && player.isAlive()) {
            float heal = StatusEffectRuntime.outgoingOffenseHealCaster(player);
            if (heal > 0f) {
                healPlayer(heal);
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

    void applyPositiveStatusToPlayer(PositiveStatusType type, int turns) {
        if (player != null && player.isAlive()) {
            player.setPositiveStatus(type, turns);
        }
    }

    void dealDamageToEnemyIgnoringShield(float amount) {
        damageAliveEnemies(amount, DamageSource.OFFENSE_CARD, true);
        if (player != null && player.isAlive()) {
            float heal = StatusEffectRuntime.outgoingOffenseHealCaster(player);
            if (heal > 0f) {
                healPlayer(heal);
            }
        }
    }

    private void damageAliveEnemies(float amount, DamageSource source, boolean ignoreShield) {
        CombatEntityRoster.forEachAliveEnemy(enemies, enemy -> {
            float finalAmount = source == DamageSource.OFFENSE_CARD
                    ? CombatEntityRoster.offenseDamageWithStatusModifiers(amount, enemy)
                    : amount;
            if (ignoreShield) {
                enemy.takeDamageIgnoringShield(finalAmount);
            } else {
                enemy.takeDamage(finalAmount);
            }
        });
    }

    void dealDamageToPlayer(float amount) {
        dealDamageToPlayer(amount, DamageSource.OTHER);
    }

    void dealDamageToPlayer(float amount, DamageSource source) {
        if (player == null || !player.isAlive()) {
            return;
        }
        if (source == DamageSource.OFFENSE_CARD) {
            StatusEffectRuntime.IncomingOffenseToSelfResult resolved =
                    StatusEffectRuntime.resolveIncomingOffenseToSelf(amount, player);
            if (resolved.damageToBearer() > 0f) {
                player.takeDamage(resolved.damageToBearer());
                syncPlayerStatsHp();
            }
            applyRetaliationToAttacker(resolved.retaliateAttacker());
            return;
        }
        player.takeDamage(amount);
        syncPlayerStatsHp();
    }

    void dealDamageToPlayerIgnoringShield(float amount) {
        dealDamageToPlayerIgnoringShield(amount, DamageSource.OTHER);
    }

    void dealDamageToPlayerIgnoringShield(float amount, DamageSource source) {
        if (player == null || !player.isAlive()) {
            return;
        }
        if (source == DamageSource.OFFENSE_CARD) {
            StatusEffectRuntime.IncomingOffenseToSelfResult resolved =
                    StatusEffectRuntime.resolveIncomingOffenseToSelf(amount, player);
            if (resolved.damageToBearer() > 0f) {
                player.takeDamageIgnoringShield(resolved.damageToBearer());
                syncPlayerStatsHp();
            }
            applyRetaliationToAttacker(resolved.retaliateAttacker());
            return;
        }
        player.takeDamageIgnoringShield(amount);
        syncPlayerStatsHp();
    }

    private void applyRetaliationToAttacker(float amount) {
        if (amount <= 0f) {
            return;
        }
        CombatEntity attacker = CombatEntityRoster.firstAliveEnemy(enemies);
        if (attacker != null && attacker.isAlive()) {
            attacker.takeDamageIgnoringShield(amount);
        }
    }

    private void finishRound() {
        applyRoundEndEffects();
        playerSlots.clearPlayerSlots();

        CombatOutcome outcome = checkCombatOutcomeIfFinished();
        if (outcome != null) {
            outcomeFinalization.setPendingOutcome(outcome, false);
            return;
        }

        roundNumber++;
        rollPlayerSlotsForPlanning();
        enemySlots.rollEnemySlotCards();
        phase = CombatPhase.PLANNING;
        selectedInstanceId = null;
    }

    private void rollPlayerSlotsForPlanning() {
        PlayerSlotPlan plan = playerSlotRoller.rollPlan();
        playerSlots.setPlayerSlots(plan.slotA(), plan.slotB());
    }

    private void applyRoundEndEffects() {
        if (roundEndCooldownsApplied) {
            return;
        }
        roundEndCooldownsApplied = true;
        applyRoundEndStatusEffects();
        applyRoundEndCooldownsOnly();
    }

    private void applyRoundEndStatusEffects() {
        if (player != null) {
            player.applyRoundEndStatusEffects();
            syncPlayerStatsHp();
        }
        for (CombatEntity enemy : enemies) {
            enemy.applyRoundEndStatusEffects();
        }
    }

    private void applyRoundEndCooldownsOnly() {
        RoundEndCooldownApplier.apply(deck, playedThisRound);
        RoundEndCooldownApplier.apply(enemySlots.getEnemyDeck(), enemyPlayedThisRound);
    }

    private void initializeCombatSession(
            int stageIndex,
            boolean boss,
            EnemyArchetypeId encounterArchetype,
            float arenaWidth,
            float groundY,
            ActionCardDeck actionDeck,
            Consumer<CombatOutcome> onEnd) {
        this.bossFight = boss;
        this.deck = actionDeck;
        this.onCombatEnd = onEnd;

        enemies.clear();
        combatEnded = false;
        phase = CombatPhase.PLANNING;
        roundNumber = 1;
        playedThisRound.clear();
        roundEndCooldownsApplied = false;
        playerSlots.clearPlayerSlots();
        enemySlots.clearEnemySlots();
        enemyPlayedThisRound.clear();
        selectedInstanceId = null;
        resolveTimer = 0f;

        EnemyArchetypeId resolvedArchetype = boss
                ? null
                : EnemyArchetypeRegistry.resolveNormalEncounter(encounterArchetype, enemyHpRng);
        enemySlots.setCurrentEnemyArchetype(resolvedArchetype);
        if (resolvedArchetype != null) {
            EnemyArchetypeDef archetype = EnemyArchetypeRegistry.getRequired(resolvedArchetype);
            enemySlots.setEnemyDeck(ActionCardDeck.fromCardTypes(archetype.deckCardTypes()));
        } else {
            enemySlots.setEnemyDeck(null);
        }

        player = entityFactory.createPlayerEntity(arenaWidth, groundY);
        enemies.add(entityFactory.createOpponentEntity(
                stageIndex, boss, arenaWidth, groundY, resolvedArchetype));
        rollPlayerSlotsForPlanning();
        enemySlots.rollEnemySlotCards();
    }

    private void resolveEnemySlot() {
        if (enemySlots.isBossEncounter()) {
            dealDamageToPlayer(2f, DamageSource.OTHER);
            return;
        }
        ActionCardInstance card = enemySlots.resolveCardForSlot(resolvingSlotIndex);
        if (card == null) {
            return;
        }
        applyCardEffect(card.getType(), EffectCaster.ENEMY);
        enemyPlayedThisRound.add(card.getInstanceId());
    }

    private void resolvePlayerSlot(int slotIndex) {
        Integer instanceId = playerSlots.getSlotInstanceId(slotIndex);
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
