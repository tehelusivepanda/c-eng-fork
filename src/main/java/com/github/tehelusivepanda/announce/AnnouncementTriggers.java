package com.github.tehelusivepanda.announce;

import com.github.tehelusivepanda.PorkNChubHelperConfig;
import com.github.tehelusivepanda.player.PorkNChub;
import com.github.tehelusivepanda.player.LoggedInState;
import com.github.tehelusivepanda.player.Pandamxnium;
import com.github.tehelusivepanda.sound.Sound;
import com.github.tehelusivepanda.sound.SoundEngine;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

public class AnnouncementTriggers {
    private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log:.*");
    private static final Pattern COMBAT_TASK_REGEX = Pattern.compile("CA_ID:\\d+\\|Congratulations, you've completed an? \\w+ combat task:.*");
    private static final Pattern LEAGUES_TASK_REGEX = Pattern.compile("Congratulations, you've completed an? \\w+ task:.*");
    private static final Pattern QUEST_REGEX = Pattern.compile("Congratulations, you've completed a quest:.*");
    private static final Pattern SLAYER_TASK_REGEX = Pattern.compile("You have completed your task! You killed .*. You gained .* xp.");
    private static final String HUNTER_RUMOUR_MESSAGE = Text.standardize("You find a rare piece of the creature! You should take it back to the Hunter Guild.");
    private static final String FARMING_CONTRACT_MESSAGE = Text.standardize("You've completed a Farming Guild Contract. You should return to Guildmaster Jane.");

    private static final Random random = new Random();

    private static final int[] VARBITS_ACHIEVEMENT_DIARIES = {
            Varbits.DIARY_ARDOUGNE_EASY,   Varbits.DIARY_ARDOUGNE_MEDIUM,   Varbits.DIARY_ARDOUGNE_HARD,   Varbits.DIARY_ARDOUGNE_ELITE,
            Varbits.DIARY_DESERT_EASY, 	   Varbits.DIARY_DESERT_MEDIUM, 	Varbits.DIARY_DESERT_HARD, 	   Varbits.DIARY_DESERT_ELITE,
            Varbits.DIARY_FALADOR_EASY,    Varbits.DIARY_FALADOR_MEDIUM,    Varbits.DIARY_FALADOR_HARD,    Varbits.DIARY_FALADOR_ELITE,
            Varbits.DIARY_KANDARIN_EASY,   Varbits.DIARY_KANDARIN_MEDIUM,   Varbits.DIARY_KANDARIN_HARD,   Varbits.DIARY_KANDARIN_ELITE,
            Varbits.DIARY_KARAMJA_EASY,    Varbits.DIARY_KARAMJA_MEDIUM,    Varbits.DIARY_KARAMJA_HARD,    Varbits.DIARY_KARAMJA_ELITE,
            Varbits.DIARY_KOUREND_EASY,    Varbits.DIARY_KOUREND_MEDIUM,    Varbits.DIARY_KOUREND_HARD,    Varbits.DIARY_KOUREND_ELITE,
            Varbits.DIARY_LUMBRIDGE_EASY,  Varbits.DIARY_LUMBRIDGE_MEDIUM,  Varbits.DIARY_LUMBRIDGE_HARD,  Varbits.DIARY_LUMBRIDGE_ELITE,
            Varbits.DIARY_MORYTANIA_EASY,  Varbits.DIARY_MORYTANIA_MEDIUM,  Varbits.DIARY_MORYTANIA_HARD,  Varbits.DIARY_MORYTANIA_ELITE,
            Varbits.DIARY_VARROCK_EASY,    Varbits.DIARY_VARROCK_MEDIUM,    Varbits.DIARY_VARROCK_HARD,    Varbits.DIARY_VARROCK_ELITE,
            Varbits.DIARY_WESTERN_EASY,    Varbits.DIARY_WESTERN_MEDIUM,    Varbits.DIARY_WESTERN_HARD,    Varbits.DIARY_WESTERN_ELITE,
            Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM, Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE
    };

    private static final Set<Integer> badCollectionLogNotificationSettingValues = Set.of(0, 2);

    @Inject
    private Client client;

    @Getter(AccessLevel.PACKAGE)
    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private PorkNChubHelperConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private SoundEngine soundEngine;

    @Inject
    private PorkNChub pork;

    @Inject
    private Pandamxnium panda;

    @Inject
    private ItemManager itemManager;

    @Inject
    private LoggedInState loggedInState;

    private final Map<Skill, Integer> oldExperience = new EnumMap<>(Skill.class);
    private final Map<Integer, Integer> oldAchievementDiaries = new HashMap<>();

    private int lastColLogSettingWarning = -1;

    public void initialise() {
        clientThread.invoke(this::initialiseOldXPAndDiaryMaps);
    }

    public void shutDown() {
        oldExperience.clear();
        oldAchievementDiaries.clear();
    }

    private void initialiseOldXPAndDiaryMaps() {
        if (loggedInState.isLoggedOut()) {
            oldExperience.clear();
            oldAchievementDiaries.clear();
            return;
        }
        for (final Skill skill : Skill.values()) {
            oldExperience.put(skill, client.getSkillExperience(skill));
        }
        for (@Varbit int diary : VARBITS_ACHIEVEMENT_DIARIES) {
            int value = client.getVarbitValue(diary);
            oldAchievementDiaries.put(diary, value);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState gameState = event.getGameState();

        if (gameState != GameState.LOGGED_IN) {
            if (gameState != GameState.CONNECTION_LOST) {
                oldExperience.clear();
                oldAchievementDiaries.clear();
            }
            lastColLogSettingWarning = client.getTickCount(); // avoid warning during DC
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        final Skill skill = statChanged.getSkill();

        // Modified from Nightfirecat's virtual level ups plugin as this info isn't (yet?) built in to statChanged event
        final int xpAfter = client.getSkillExperience(skill);
        final int levelAfter = Experience.getLevelForXp(xpAfter);
        final int xpBefore = oldExperience.getOrDefault(skill, -1);
        final int levelBefore = xpBefore == -1 ? -1 : Experience.getLevelForXp(xpBefore);

        oldExperience.put(skill, xpAfter);

        // Do not proceed if any of the following are true:
        //  * xpBefore == -1              (don't fire when first setting new known value)
        //  * xpAfter <= xpBefore         (do not allow 200m -> 200m exp drops)
        //  * levelBefore >= levelAfter   (stop if if we're not actually reaching a new level)
        //  * levelAfter > MAX_REAL_LEVEL && config says don't include virtual (level is virtual and config ignores virtual)
        if (xpBefore == -1 || xpAfter <= xpBefore || levelBefore >= levelAfter ||
                (levelAfter > Experience.MAX_REAL_LEVEL && !config.announceLevelUpIncludesVirtual())) {
            return;
        }

        // If we get here, 'skill' was leveled up!
        if (config.announceLevelUp()) {
            pork.sendChatIfEnabled("Level up: completed.");
            soundEngine.playClip(Sound.LEVEL_UP, executor);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarbitId() == Varbits.COLLECTION_LOG_NOTIFICATION) {
            checkAndWarnForCollectionLogNotificationSetting(varbitChanged.getValue());
        }

        // As we can't listen to specific varbits, we get a tonne of events BEFORE the game has even set the player's
        // diary varbits correctly, meaning it assumes every diary is on 0, then suddenly every diary that has been
        // completed gets updated to the true value and tricks the plugin into thinking they only just finished it.
        // To avoid this behaviour, we make sure the current tick count is sufficiently high that we've already passed
        // the initial wave of varbit changes from logging in.
        if (loggedInState.isLoggedOut() || loggedInState.onlyJustLoggedIn(8)) {
            return; // Ignoring varbit change as only just logged in
        }

        // Apparently I can't check if it's a particular varbit using the names from Varbits enum, so this is the way
        for (@Varbit int diary : VARBITS_ACHIEVEMENT_DIARIES) {
            int newValue = client.getVarbitValue(diary);
            int previousValue = oldAchievementDiaries.getOrDefault(diary, -1);
            oldAchievementDiaries.put(diary, newValue);
            if (config.announceAchievementDiary() && previousValue != -1 && previousValue != newValue && isAchievementDiaryCompleted(diary, newValue)) {
                // value was not unknown (we know the previous value), value has changed, and value indicates diary is completed now
                pork.sendChatIfEnabled("Achievement diary: completed.");
                soundEngine.playClip(Sound.ACHIEVEMENT_DIARY, executor);
            }
        }
    }

    private void checkAndWarnForCollectionLogNotificationSetting(int newVarbitValue) {
        if (!config.announceCollectionLog() ||
                loggedInState.isLoggedOut() ||
                !badCollectionLogNotificationSettingValues.contains(newVarbitValue))
            return;

        if (lastColLogSettingWarning == -1 || client.getTickCount() - lastColLogSettingWarning > 16) {
            lastColLogSettingWarning = client.getTickCount();
            sendHighlightedMessageForColLogNotifSetting();
        }
    }

    private void sendHighlightedMessageForColLogNotifSetting() {
        sendHighlightedMessage("Please enable \"Collection log - New addition notification\" in your game settings for C Engineer to know when to announce it! (The chat message one, pop-up doesn't matter)");
    }

    private void sendHighlightedMessageForLeaguesTaskSetting() {
        sendHighlightedMessage("C Engineer announcing leagues tasks might get spammy, but remember you can disable these announcements while keeping the others active in the plugin settings!");
    }

    private void sendHighlightedMessage(String message) {
        String highlightedMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(highlightedMessage)
                .build());
    }

    private boolean isAchievementDiaryCompleted(int diary, int value) {
        switch (diary) {
            case Varbits.DIARY_KARAMJA_EASY:
            case Varbits.DIARY_KARAMJA_MEDIUM:
            case Varbits.DIARY_KARAMJA_HARD:
                return value == 2; // jagex, why?
            default:
                return value == 1;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (PorkNChubHelperConfig.GROUP.equals(event.getGroup())) {

			if ("announcementVolume".equals(event.getKey())) {
				soundEngine.playClip(Sound.LEVEL_UP, executor);
			} else if ("announceCollectionLog".equals(event.getKey())) {
                clientThread.invokeLater(() ->
                        checkAndWarnForCollectionLogNotificationSetting(client.getVarbitValue(Varbits.COLLECTION_LOG_NOTIFICATION)));
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
            return;

        if (config.announceCollectionLog() && COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage()).matches()) {
            pork.sendChatIfEnabled("Collection log slot: completed.");
            soundEngine.playClip(Sound.COLLECTION_LOG_SLOT, executor);

        } else if (config.announceQuestCompletion() && QUEST_REGEX.matcher(chatMessage.getMessage()).matches()) {
            pork.sendChatIfEnabled("Quest: completed.");
            soundEngine.playClip(Sound.QUEST, executor);

        } else if (config.announceCombatAchievement() && COMBAT_TASK_REGEX.matcher(chatMessage.getMessage()).matches()) {
            pork.sendChatIfEnabled("Combat task: completed.");
            soundEngine.playClip(Sound.COMBAT_TASK, executor);

        } else if (config.announceLeaguesTasks() && LEAGUES_TASK_REGEX.matcher(chatMessage.getMessage()).matches()) {
            pork.sendChatIfEnabled("Leagues task: completed.");
            soundEngine.playClip(Sound.LEAGUES_TASK, executor);

            if (config.needToRemindAboutDisablingLeaguesTasks()) {
                configManager.setConfiguration(PorkNChubHelperConfig.GROUP, PorkNChubHelperConfig.LEAGUES_TASK_HIDDEN_REMINDER_CONFIG, false);
                sendHighlightedMessageForLeaguesTaskSetting();
            }

        } else if (config.announceSlayerTasks() && SLAYER_TASK_REGEX.matcher(Text.removeTags(chatMessage.getMessage())).matches()) {
            pork.sendChatIfEnabled("Slayer task: completed.");
            soundEngine.playClip(Sound.SLAYER_TASK, executor);
            return;
        }

        String standardizedMessage = Text.standardize(chatMessage.getMessage());
        if (config.announceHunterRumours() && HUNTER_RUMOUR_MESSAGE.equals(standardizedMessage)) {
            pork.sendChatIfEnabled("Hunter Rumour: completed.");
            soundEngine.playClip(Sound.HUNTER_RUMOUR, executor);

        } else if (config.announceFarmingContracts() && FARMING_CONTRACT_MESSAGE.equals(standardizedMessage)) {
            pork.sendChatIfEnabled("Farming Contract: completed.");
            soundEngine.playClip(Sound.FARMING_CONTRACT, executor);
        }
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        pork.sendChatIfEnabled("Woah you got a " + event.getItem().toString() + "!!");
    }

    @Subscribe
    public void onNpcLootReceived(NpcLootReceived npcLootReceived) {
        Collection<ItemStack> loot = npcLootReceived.getItems();
        for (ItemStack itemStack : loot) {
            int itemId = itemStack.getId();

            if (itemId == ItemID.GRUBBY_KEY && config.announceGrubbyKeyDrop()) {
                pork.sendChatIfEnabled("Another grubby key.");
                soundEngine.playClip(Sound.GRUBBY_KEY, executor);

            } else if (itemId == ItemID.LARRANS_KEY && config.announceLarransKeyDrop()) {
                pork.sendChatIfEnabled("Another Larran's key.");
                soundEngine.playClip(Sound.LARRANS_KEY, executor);

            } else if (itemId == ItemID.BRIMSTONE_KEY && config.announceBrimstoneKeyDrop()) {
                pork.sendChatIfEnabled("Another brimstone key.");
                soundEngine.playClip(Sound.BRIMSTONE_KEY, executor);
            }

            if (itemManager.getItemPriceWithSource(itemId, true) > 1000000) {
                pork.sendChatIfEnabled("gzzzzzzzz");
            }
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        if (actorDeath.getActor() != client.getLocalPlayer())
            return;

        // Death easter egg and normal announcement kept together to make it one or the other, never both
        if (config.announceDeath()) {
            diedToCEngineer();
        }
    }

    private void diedToCEngineer() {
        if (random.nextBoolean()) {
            pork.sendChatIfEnabled("Dying to " + PorkNChub.RSN + ": completed.");
        } else {
            pork.sendChatIfEnabled("Sit");
        }
        soundEngine.playClip(Sound.DEATH_TO_C_ENGINEER, executor);
    }

    private void diedToAnythingElse() {
        pork.sendChatIfEnabled("This is somehow Peter's fault...");
        soundEngine.playClip(Sound.DEATH, executor);
    }
}
