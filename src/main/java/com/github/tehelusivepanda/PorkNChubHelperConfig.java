package com.github.tehelusivepanda;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(PorkNChubHelperConfig.GROUP)
public interface PorkNChubHelperConfig extends Config {
    String GROUP = "cengineercompleted";
    String LEAGUES_TASK_HIDDEN_REMINDER_CONFIG = "needToRemindAboutDisablingLeaguesTasks";

    @ConfigSection(
            name = "Announce Achievements",
            description = "Which sounds that are achievements should play.",
            position = 0
    )
    String SECTION_ACHIEVEMENT_ANNOUNCEMENTS = "Announce Achievements";

    @ConfigItem(
            keyName = "announceLevelUp",
            name = "Level ups",
            description = "Should C Engineer announce when you gain a level in a skill?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 1
    )
    default boolean announceLevelUp() {
        return true;
    }

    @ConfigItem(
            keyName = "announceLevelUpIncludesVirtual",
            name = "Include virtual level ups",
            description = "Should C Engineer announce when you gain a virtual (>99) level in a skill?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 2
    )
    default boolean announceLevelUpIncludesVirtual() {
        return false;
    }

    @ConfigItem(
            keyName = "announceQuestCompletion",
            name = "Quest completions",
            description = "Should C Engineer announce when you complete a quest?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 3
    )
    default boolean announceQuestCompletion() {
        return true;
    }

    @ConfigItem(
            keyName = "announceCollectionLog",
            name = "New collection log entry",
            description = "Should C Engineer announce when you fill in a new slot in your collection log? This one relies on you having chat messages (included with the popup option) enabled in game settings!",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 4
    )
    default boolean announceCollectionLog() {
        return true;
    }

    @ConfigItem(
            keyName = "announceAchievementDiary",
            name = "Completed achievement diaries",
            description = "Should C Engineer announce when you complete a new achievement diary?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 5
    )
    default boolean announceAchievementDiary() {
        return true;
    }

    @ConfigItem(
            keyName = "announceCombatAchievement",
            name = "Completed combat achievement tasks",
            description = "Should C Engineer announce when you complete a new combat achievement task?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 6
    )
    default boolean announceCombatAchievement() {
        return true;
    }

    @ConfigItem(
            keyName = "announceSlayerTasks",
            name = "Completed Slayer Tasks",
            description = "Should C Engineer announce when you complete a slayer task?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 7
    )
    default boolean announceSlayerTasks() {
        return true;
    }

    @ConfigItem(
            keyName = "announceFarmingContracts",
            name = "Completed Farming Contracts",
            description = "Should C Engineer announce when you complete a farming contract?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 8
    )
    default boolean announceFarmingContracts() {
        return true;
    }

    @ConfigItem(
            keyName = "announceHunterRumours",
            name = "Completed Hunter Rumours",
            description = "Should C Engineer announce when you receive the required creature part for a hunter rumour?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 9
    )
    default boolean announceHunterRumours() {
        return true;
    }

    @ConfigItem(
            keyName = "announceLeaguesTasks",
            name = "Completed Leagues Tasks",
            description = "Should C Engineer announce when you complete a leagues task?",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 10
    )
    default boolean announceLeaguesTasks() {
        return true;
    }

    @ConfigItem(
            keyName = LEAGUES_TASK_HIDDEN_REMINDER_CONFIG,
            name = "Need to remind user they can disable leagues tasks announcements if they want",
            description = "Leagues tasks can get spammy, and some users might not know they can toggle individual announcements instead of the whole plugin",
            section = SECTION_ACHIEVEMENT_ANNOUNCEMENTS,
            hidden = true
    )
    default boolean needToRemindAboutDisablingLeaguesTasks() {
        return true;
    }

    @ConfigSection(
            name = "Announce Other",
            description = "Which sounds that are not necessarily achievements (and not easter eggs) should play.",
            position = 20
    )
    String SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS = "Announce Other";

    @ConfigItem(
            keyName = "announceDeath",
            name = "When you die",
            description = "Should C Engineer relive his PvP HCIM death when you die?",
            section = SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 21
    )
    default boolean announceDeath() {
        return true;
    }

    @ConfigItem(
            keyName = "announceNonTrouverInfernal",
            name = "Non-locked infernal in wildy (once per plugin session)",
            description = "Should C Engineer warn you when you enter the wilderness (only once per plugin session) with an infernal cape that has not been locked with a trouver parchment?",
            section = SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 22
    )
    default boolean announceNonTrouverInfernal() {
        return true;
    }

    @ConfigItem(
            keyName = "announceGrubbyKeyDrop",
            name = "Announce Grubby Key Drop",
            description = "Should C Engineer announce when you receive a grubby key?",
            section = SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 23
    )
    default boolean announceGrubbyKeyDrop() {
        return true;
    }

    @ConfigItem(
            keyName = "announceLarransKeyDrop",
            name = "Announce Larran's Key Drop",
            description = "Should C Engineer announce when you receive a Larran's key?",
            section = SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 24
    )
    default boolean announceLarransKeyDrop() {
        return true;
    }

    @ConfigItem(
            keyName = "announceBrimstoneKeyDrop",
            name = "Announce Brimstone Key Drop",
            description = "Should C Engineer announce when you receive a brimstone key?",
            section = SECTION_NON_ACHIEVEMENT_ANNOUNCEMENTS,
            position = 25
    )
    default boolean announceBrimstoneKeyDrop() {
        return true;
    }

    @ConfigSection(
            name = "General Announcement Settings",
            description = "Settings for other details when achievement sounds play.",
            position = 40
    )
    String SECTION_GENERAL_ANNOUNCEMENT_SETTINGS = "General Announcement Settings";

    @ConfigItem(
            keyName = "showChatMessages",
            name = "Show fake public chat message (only you will see it)",
            description = "Should C Engineer announce your achievements in game chat as well as audibly?",
            section = SECTION_GENERAL_ANNOUNCEMENT_SETTINGS,
            position = 41
    )
    default boolean showChatMessages() {
        return true;
    }

    @Range(
            min = 0,
            max = 200
    )
    @ConfigItem(
            keyName = "announcementVolume",
            name = "Announcement volume",
            description = "Adjust how loud the audio announcements are played!",
            section = SECTION_GENERAL_ANNOUNCEMENT_SETTINGS,
            position = 42
    )
    default int announcementVolume() {
        return 100;
    }
}
