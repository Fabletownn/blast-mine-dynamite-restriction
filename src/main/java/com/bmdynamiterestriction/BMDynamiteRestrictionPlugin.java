package com.bmdynamiterestriction;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Blast Mine Dynamite Restriction",
		description = "Disallows chiseling hard rock in the Blast Mine without unnoted dynamite in the inventory"
)
public class BMDynamiteRestrictionPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BMDynamiteRestrictionConfig config;

	// IDs
	private static final int HARD_ROCK_ID = 28579;
	private static final int HARD_ROCK_ID_2 = 28580;
	private static final int UNNOTED_DYNAMITE_ID = 13573;

	// Notifications
	private boolean outOfDynamiteNotified = false;
	private boolean hasDynamiteNotified = false;

	// Inventory
	private ItemContainer previousInventory = null;

	@Subscribe
	protected void onGameStateChanged(GameStateChanged state)
	{
		if (state.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() -> previousInventory = client.getItemContainer(InventoryID.INVENTORY));
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded entry)
	{
		ItemContainer currentInventory = client.getItemContainer(InventoryID.INVENTORY);

		if (currentInventory == null) return;
		if (entry.getOption().equals("Excavate") && !checkUnnotedDynamite(currentInventory) && (entry.getIdentifier() == HARD_ROCK_ID || entry.getIdentifier() == HARD_ROCK_ID_2))
		{
			if (config.depmenu())
			{
				entry.getMenuEntry().setDeprioritized(true);
			}

			if (hasOutOfDynamiteMessagesEnabled() && !outOfDynamiteNotified)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You ran out of dynamite and cannot chisel Hard Rock.", null);
				playOutOfDynamiteEffect();
			}

			outOfDynamiteNotified = true;
			hasDynamiteNotified = false;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged container)
	{
		ItemContainer playerInventory = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer changedContainer = container.getItemContainer();

		if (previousInventory == null)
		{
			previousInventory = changedContainer;
		}
		else if (playerInventory != null && changedContainer != null)
		{
			if (changedContainer.getId() == playerInventory.getId())
			{
				boolean hadDynamite = checkUnnotedDynamite(previousInventory);
				boolean hasDynamite = checkUnnotedDynamite(playerInventory);

				log.info("Previous: {} Player: {}", hadDynamite, hasDynamite);

				if (!hadDynamite && hasDynamite)
				{
					if (hasReplenishedDynamiteMessagesEnabled() && !hasDynamiteNotified)
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You now have dynamite to chisel Hard Rock once more.", null);
					}

					outOfDynamiteNotified = false;
					hasDynamiteNotified = true;
				}
			}
			previousInventory = changedContainer;
		}
	}

	private boolean checkUnnotedDynamite(ItemContainer container)
	{
		if (container != null)
		{
			for (Item item : container.getItems())
			{
				if (item.getId() == UNNOTED_DYNAMITE_ID)
				{
					return true;
				}
			}
		}
		return false;
	}

	private void playOutOfDynamiteEffect()
	{
		if (config.soundfx())
		{
			clientThread.invoke(() -> client.playSoundEffect(config.soundid(), config.soundfxvol()));
		}
	}

	private boolean hasOutOfDynamiteMessagesEnabled()
	{
		BMDynamiteRestrictionConfig.MessagesEnabledType messagesEnabled = config.chatmsgs();

        return messagesEnabled == BMDynamiteRestrictionConfig.MessagesEnabledType.ALL_MESSAGES ||
			   messagesEnabled == BMDynamiteRestrictionConfig.MessagesEnabledType.OUT_OF_DYNAMITE;
    }

	private boolean hasReplenishedDynamiteMessagesEnabled()
	{
		BMDynamiteRestrictionConfig.MessagesEnabledType messagesEnabled = config.chatmsgs();

		return messagesEnabled == BMDynamiteRestrictionConfig.MessagesEnabledType.ALL_MESSAGES ||
				messagesEnabled == BMDynamiteRestrictionConfig.MessagesEnabledType.REPLENISHED_DYNAMITE;
	}

	@Provides
	BMDynamiteRestrictionConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BMDynamiteRestrictionConfig.class);
	}
}
