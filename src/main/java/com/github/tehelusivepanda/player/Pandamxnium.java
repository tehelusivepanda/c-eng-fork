package com.github.tehelusivepanda.player;

import com.github.tehelusivepanda.PorkNChubHelperConfig;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Pandamxnium {
    public static final String RSN = "Pandamxnium";

    @Inject
    private Client client;

    @Inject
    private PorkNChubHelperConfig config;

    private Player player = null;

    public void sendChatIfEnabled(String message) {
        if (config.showChatMessages()) {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, RSN, message, null);
        }
    }
}
