package com.github.tehelusivepanda.player;

import com.github.tehelusivepanda.PorkNChubHelperConfig;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static net.runelite.api.PlayerComposition.ITEM_OFFSET;

@Singleton
public class PorkNChub {
    public static final String RSN = "Pork N Chub";

    @Inject
    private Client client;

    @Inject
    private PorkNChubHelperConfig config;

    private Player player = null;

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned playerSpawned) {
        Player spawnedPlayer = playerSpawned.getPlayer();
        if (RSN.equals(spawnedPlayer.getName())) {
            this.player = spawnedPlayer;
        }
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned playerDespawned) {
        Player despawnedPlayer = playerDespawned.getPlayer();
        if (RSN.equals(despawnedPlayer.getName())) {
            this.player = null;
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged interactingChanged) {
        if (isOutOfRenderDistance())
            return;
    }

    public boolean isOutOfRenderDistance() {
        return player == null;
    }

    public boolean isWearing(int itemId) {
        if (player == null)
            return false;

        int[] equipmentIds = player.getPlayerComposition().getEquipmentIds();
        return Arrays.stream(equipmentIds)
                .filter(i -> i > ITEM_OFFSET)
                .map(i -> i - ITEM_OFFSET)
                .anyMatch(i -> i == itemId);
    }

    public boolean isFollowingMe() {
        return isInteracting(client.getLocalPlayer());
    }

    public boolean isInteracting(Actor actor) {
        if (actor == null)
            return false;

        if (isOutOfRenderDistance())
            return false;

        Actor cEngineerInteractTarget = player.getInteracting();
        return cEngineerInteractTarget == actor;
    }

    public boolean actorEquals(Actor other) {
        return player == other;
    }

    public void sendChatIfEnabled(String message) {
        if (config.showChatMessages()) {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, RSN, message, null);
        }
    }
}
