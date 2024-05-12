package com.bmdynamiterestriction;

import net.runelite.api.SoundEffectVolume;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("bmdynamiterestriction")
public interface BMDynamiteRestrictionConfig extends Config
{
	// Chat Messages
	/////////////////////////////////////////
	@ConfigItem(
			keyName = "chatMessages",
			name = "Chat Messages Enabled",
			description = "The types of chat messages you receive"
	)

	default MessagesEnabledType chatmsgs() { return MessagesEnabledType.ALL_MESSAGES; }

	enum MessagesEnabledType
	{
		ALL_MESSAGES ("All Messages"),
		OUT_OF_DYNAMITE("Out Of Dynamite Only"),
		REPLENISHED_DYNAMITE("Replenished Dynamite Only");

		private final String name;
		MessagesEnabledType(String name) { this.name = name; }

		@Override
		public String toString() { return name; }
	}

	// Sound Effects
	/////////////////////////////////////////
	@ConfigItem(
			keyName = "soundfx",
			name = "Sound FX",
			description = "The sound effects to notify you when you are out of dynamite"
	)

	default boolean soundfx() { return true; }

	@Range(
			max = 4996
	)
	@ConfigItem(
			keyName = "soundid",
			name = "Sound ID",
			description = "The sound effect ID to play when you are out of dynamite"
	)

	default int soundid() { return 2277; }

	@Range(
			max = SoundEffectVolume.HIGH
	)
	@ConfigItem(
			keyName = "soundfxvol",
			name = "Sound FX Volume",
			description = "The volume at which the sound effects will play"
	)

	default int soundfxvol() { return SoundEffectVolume.MEDIUM_LOW; }

	// Functionality
	/////////////////////////////////////////
	@ConfigItem(
			keyName = "depmenu",
			name = "Deprioritize Menu Entries",
			description = "Whether or not to set Chisel menu entries to right-click only if you are out of dynamite"
	)

	default boolean depmenu() { return true; }
}
