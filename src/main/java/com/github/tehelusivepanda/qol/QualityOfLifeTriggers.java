package com.github.tehelusivepanda.qol;

import com.github.tehelusivepanda.PorkNChubHelperConfig;
import com.github.tehelusivepanda.player.PorkNChub;
import com.github.tehelusivepanda.player.LoggedInState;
import com.github.tehelusivepanda.sound.Sound;
import com.github.tehelusivepanda.sound.SoundEngine;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;

public class QualityOfLifeTriggers {
    private static final int INFERNAL_PARCHMENT_WARN_COOLDOWN = 36;

    @Inject
    private Client client;

    @Inject
    private PorkNChubHelperConfig config;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private SoundEngine soundEngine;

    @Inject
    private PorkNChub cEngineer;

    @Inject
    private LoggedInState loggedInState;

    private int lastInfernalParchmentWarningTick = -1;

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarbitId() == Varbits.IN_WILDERNESS && varbitChanged.getValue() == 1) {
            checkAndWarnForUnparchmentedInfernal();
        }
    }

    private void checkAndWarnForUnparchmentedInfernal() {
        if (!config.announceNonTrouverInfernal())
            return;

        if (loggedInState.isLoggedOut())
            return;

        if (lastInfernalParchmentWarningTick != -1 && client.getTickCount() - lastInfernalParchmentWarningTick < INFERNAL_PARCHMENT_WARN_COOLDOWN)
            return;

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        boolean warnForEquip = equipment != null &&
                (equipment.contains(ItemID.INFERNAL_CAPE) || equipment.contains(ItemID.INFERNAL_MAX_CAPE));
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        boolean warnForInvent = inventory != null &&
                (inventory.contains(ItemID.INFERNAL_CAPE) || inventory.contains(ItemID.INFERNAL_MAX_CAPE));

        if (warnForEquip || warnForInvent) {
            lastInfernalParchmentWarningTick = client.getTickCount();
            cEngineer.sendChatIfEnabled("Your infernal cape is not parched!");
            soundEngine.playClip(Sound.QOL_NON_PARCH_INFERNAL, executor);
        }
    }
}
