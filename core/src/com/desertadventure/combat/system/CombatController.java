package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardTarget;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.player.PlayerStats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Turn-based 1v1 card combat (PLANNING → RESOLVING slots 1–4 → win/loss / next round). */
public class CombatController {
    private static final int SLOT_COUNT = 4;
    private static final int PLAYER_SLOT_A = 0;
    private static final int PLAYER_SLOT_B = 2;
    private static final int ENEMY_SLOT_A = 1;
    private static final int ENEMY_SLOT_B = 3;

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
    private Integer selectedInstanceId;

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
        clearAllSlots();
        selectedInstanceId = null;
        resolveTimer = 0f;

        float playerX = arenaWidth * GameConfig.COMBAT_PLAYER_X_RATIO;
        player = new CombatEntity(CombatEntity.Kind.PLAYER, playerX, groundY,
                playerStats.getMaxHp(), playerStats.getAttack(), 0f);
        player.setHp(playerStats.getHp());

        float enemyHp = GameConfig.ENEMY_BASE_HP + distanceBand * GameConfig.ENEMY_HP_PER_DISTANCE_BAND;
        float enemyX = arenaWidth * GameConfig.COMBAT_ENEMY_X_RATIO;
        if (boss) {
            enemyHp = GameConfig.BOSS_BASE_HP + distanceBand * GameConfig.BOSS_HP_PER_DISTANCE_BAND;
            enemyX = arenaWidth * GameConfig.COMBAT_BOSS_X_RATIO;
            enemies.add(new CombatEntity(CombatEntity.Kind.BOSS, enemyX, groundY, enemyHp, 0, 0f));
        } else {
            enemies.add(new CombatEntity(CombatEntity.Kind.ENEMY, enemyX, groundY, enemyHp, 0, 0f));
        }
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
        if (slotIndex == ENEMY_SLOT_A || slotIndex == ENEMY_SLOT_B) {
            return ActionCardType.ATTACK;
        }
        return null;
    }

    public Integer getSlotInstanceId(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) {
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
        return slotIndex == PLAYER_SLOT_A || slotIndex == PLAYER_SLOT_B;
    }

    public List<ActionCardInstance> getHandCandidates() {
        List<ActionCardInstance> hand = new ArrayList<>();
        if (deck == null) {
            return hand;
        }
        Set<Integer> assigned = assignedInstanceIds();
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance.isOnCooldown()) {
                continue;
            }
            if (assigned.contains(instance.getInstanceId())) {
                continue;
            }
            hand.add(instance);
        }
        return hand;
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
        int otherPlayerSlot = slotIndex == PLAYER_SLOT_A ? PLAYER_SLOT_B : PLAYER_SLOT_A;
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
    }

    public void update(float delta) {
        if (!isActive() || phase != CombatPhase.RESOLVING) {
            return;
        }
        resolveTimer -= delta;
        if (resolveTimer > 0f) {
            return;
        }
        resolveSlot(resolvingSlotIndex);
        resolvingSlotIndex++;
        if (resolvingSlotIndex >= SLOT_COUNT) {
            finishRound();
            return;
        }
        resolveTimer = GameConfig.COMBAT_RESOLVE_SLOT_SECONDS;
    }

    private void resolveSlot(int slotIndex) {
        if (slotIndex == ENEMY_SLOT_A || slotIndex == ENEMY_SLOT_B) {
            dealDamageToPlayer(GameConfig.ENEMY_CARD_ATTACK_DAMAGE);
            return;
        }
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

    private void applyCardEffect(ActionCardType type) {
        if (type.getTarget() == ActionCardTarget.ENEMY) {
            dealDamageToEnemy(type.getPrimaryValue());
        } else if (type.getTarget() == ActionCardTarget.SELF) {
            healPlayer(type.getPrimaryValue());
        }
    }

    private void dealDamageToEnemy(float amount) {
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
        playerStats.setHp(player.getHp());
    }

    private void healPlayer(float amount) {
        if (player == null) {
            return;
        }
        player.heal(amount);
        playerStats.setHp(player.getHp());
    }

    private void finishRound() {
        Set<Integer> played = new HashSet<>(playedThisRound);
        for (int instanceId : played) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null) {
                card.setCooldownRemaining(card.getType().getCooldownTurns());
            }
        }
        playedThisRound.clear();
        clearAllSlots();

        if (!player.isAlive()) {
            endCombat(CombatOutcome.DEFEAT);
            return;
        }
        enemies.removeIf(e -> !e.isAlive());
        if (enemies.isEmpty()) {
            endCombat(bossFight ? CombatOutcome.BOSS_VICTORY : CombatOutcome.VICTORY);
            return;
        }

        for (ActionCardInstance instance : deck.getInstances()) {
            if (!played.contains(instance.getInstanceId())) {
                instance.tickCooldown();
            }
        }
        roundNumber++;
        phase = CombatPhase.PLANNING;
        selectedInstanceId = null;
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

    private void endCombat(CombatOutcome result) {
        if (combatEnded) {
            return;
        }
        combatEnded = true;
        playerStats.setHp(Math.max(0f, player != null ? player.getHp() : 0f));
        if (onCombatEnd != null) {
            onCombatEnd.accept(result);
        }
    }
}
