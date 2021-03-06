package com.atherys.battlegrounds.listener;

import com.atherys.battlegrounds.BattlegroundsConfig;
import com.atherys.battlegrounds.facade.BattlePointFacade;
import com.atherys.battlegrounds.facade.MilestoneFacade;
import com.atherys.battlegrounds.facade.RespawnFacade;
import com.atherys.battlegrounds.facade.TeamFacade;
import com.atherys.core.utils.EntityUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@Singleton
public class PlayerListener {

    @Inject
    private TeamFacade teamFacade;

    @Inject
    private BattlePointFacade battlePointFacade;

    @Inject
    private RespawnFacade respawnFacade;

    @Inject
    private MilestoneFacade milestoneFacade;

    @Inject
    private BattlegroundsConfig config;

    public PlayerListener() {
    }

    @Listener
    public void onPlayerMovement(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
        battlePointFacade.onPlayerMovement(player, event.getFromTransform(), event.getToTransform());
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player victim, @Root EntityDamageSource source) {
        EntityUtils.playerAttackedEntity(source).ifPresent(attacker -> teamFacade.grantPointsOnKill(victim, attacker));
    }

    @Listener
    public void onPlayerDeathAny(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player victim) {
        respawnFacade.offerRespawn(victim);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        battlePointFacade.onPlayerJoin(player);
        teamFacade.onPlayerJoin(player);
        if (config.MILESTONES_ENABLED) {
            milestoneFacade.checkMilestones(player);
        }
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player) {
        battlePointFacade.onPlayerDisconnect(player);
    }

    @Listener
    public void onTransaction(EconomyTransactionEvent event) {
        if (config.MILESTONES_ENABLED) {
            milestoneFacade.onTransaction(event.getTransactionResult());
        }
    }
}
