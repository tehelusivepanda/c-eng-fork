package com.github.tehelusivepanda;

import com.github.tehelusivepanda.announce.AnnouncementTriggers;
import com.github.tehelusivepanda.eastereggs.EasterEggTriggers;
import com.github.tehelusivepanda.player.PorkNChub;
import com.github.tehelusivepanda.player.LoggedInState;
import com.github.tehelusivepanda.qol.QualityOfLifeTriggers;
import com.github.tehelusivepanda.sound.SoundEngine;
import com.github.tehelusivepanda.sound.SoundFileManager;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
        name = "Pork N Chub",
        description = "porked",
        tags = {"c engineer", "stats", "levels", "quests", "diary", "announce"}
)

public class PorkNChubPlugin extends Plugin {
    @Inject
    private Client client;

    @Getter(AccessLevel.PACKAGE)
    @Inject
    private ClientThread clientThread;

    @Inject
    private PorkNChubHelperConfig config;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private EventBus eventBus;

    @Inject
    private SoundEngine soundEngine;

    @Inject
    private PorkNChub cEngineer;

    @Inject
    private AnnouncementTriggers announcementTriggers;

    @Inject
    private EasterEggTriggers easterEggTriggers;

    @Inject
    private QualityOfLifeTriggers qolTriggers;

    @Inject
    private LoggedInState loggedInState;

    @Override
    protected void startUp() throws Exception {
        eventBus.register(cEngineer);
        eventBus.register(announcementTriggers);
        eventBus.register(easterEggTriggers);
        eventBus.register(qolTriggers);
        eventBus.register(loggedInState);
        loggedInState.setForCurrentGameState(client.getGameState());
        announcementTriggers.initialise();
    }

    @Override
    protected void shutDown() throws Exception {
        eventBus.unregister(cEngineer);
        eventBus.unregister(announcementTriggers);
        eventBus.unregister(easterEggTriggers);
        eventBus.unregister(qolTriggers);
        eventBus.unregister(loggedInState);

        announcementTriggers.shutDown();
        soundEngine.close();
    }

    @Provides
    PorkNChubHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PorkNChubHelperConfig.class);
    }
}
