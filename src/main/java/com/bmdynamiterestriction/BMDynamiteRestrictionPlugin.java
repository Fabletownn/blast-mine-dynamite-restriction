package com.bmdynamiterestriction;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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

	private static final int HARD_ROCK_ID = 28579;
	private static final int HARD_ROCK_ID_2 = 28580;
	private static final int UNNOTED_DYNAMITE_ID = 13573;

	private boolean properLogged = false;
	private boolean hadDynamite = false;
	private boolean hasDynamite = false;
	private boolean inBank = false;

	private ItemContainer previousInventory = null;

	private static final String CHISEL_OPTION = "Excavate";
	private static final String NO_DYNAMITE_MSG = "That was the last of your dynamite! You can no longer load cavities.";
	private static final String REPLENISH_DYNAMITE_MSG = "You have dynamite and can load chiseled cavities once more.";

	@Subscribe
	protected void onGameStateChanged(GameStateChanged state) throws Exception {
		if (state.getGameState() == GameState.LOGGED_IN)
		{
			Timer logTimer = new Timer();
			logTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					properLogged = true; // Prevent a chat notification from being sent if they just logged in
					logTimer.cancel();
				}
			}, 3000);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		ItemContainer currentInventory = client.getItemContainer(InventoryID.INVENTORY);

		if (!isPlayerInBlastMine())
		{
			return;
		}

		if (previousInventory != null && currentInventory != null)
		{
			boolean prevDynamite = hadDynamite;

			clientThread.invoke(() -> {
				hadDynamite = previousInventory.contains(UNNOTED_DYNAMITE_ID);
				hasDynamite = previousInventory.contains(UNNOTED_DYNAMITE_ID);
			});

			if (!prevDynamite && hasDynamite && isPlayerAtBankChest() && properLogged)
			{
				if (hasReplenishedDynamiteMessagesEnabled())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", REPLENISH_DYNAMITE_MSG, null);
				}
			}
			else if (prevDynamite && !hasDynamite && !inBank && properLogged)
			{
				if (hasOutOfDynamiteMessagesEnabled())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", NO_DYNAMITE_MSG, null);
				}

				playOutOfDynamiteEffect();
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded entry)
	{
		ItemContainer currentInventory = client.getItemContainer(InventoryID.INVENTORY);

		if (!isPlayerInBlastMine())
		{
			return;
		}

		if (currentInventory != null)
		{
			if (entry.getOption().equals(CHISEL_OPTION) && !currentInventory.contains(UNNOTED_DYNAMITE_ID) && (entry.getIdentifier() == HARD_ROCK_ID || entry.getIdentifier() == HARD_ROCK_ID_2))
			{
				if (config.depmenu())
				{
					entry.getMenuEntry().setDeprioritized(true);
				}
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged container)
	{
		ItemContainer playerInventory = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer playerBank = client.getItemContainer(InventoryID.BANK);
		ItemContainer changedContainer = container.getItemContainer();

		if (!isPlayerInBlastMine())
		{
			return;
		}

		if (changedContainer != null && playerBank != null)
		{
			inBank = changedContainer == playerBank;
		}
		else if (playerBank == null)
		{
			inBank = false;
		}

		if (changedContainer != null && playerInventory != null)
		{
			if (changedContainer.getId() == playerInventory.getId())
			{
				previousInventory = playerInventory;
			}
		}
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

	private boolean isPlayerInBlastMine()
	{
		LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();

		int playerLocationX = WorldPoint.fromLocalInstance(client, playerLocation).getX();
		int playerLocationY = WorldPoint.fromLocalInstance(client, playerLocation).getY();

		return playerLocationX >= 1465 && playerLocationX <= 1515 &&
			   playerLocationY >= 3840 && playerLocationY <= 3890;
	}

	private boolean isPlayerAtBankChest()
	{
		LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();

		int playerLocationX = WorldPoint.fromLocalInstance(client, playerLocation).getX();
		int playerLocationY = WorldPoint.fromLocalInstance(client, playerLocation).getY();

		return (playerLocationX == 1502 && playerLocationY == 3870) ||
			   (playerLocationX == 1477 && playerLocationY == 3874) ||
			   (playerLocationX == 1478 && playerLocationY == 3856) ||
			   (playerLocationX == 1499 && playerLocationY == 3857);
	}

	@Provides
	BMDynamiteRestrictionConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BMDynamiteRestrictionConfig.class);
	}
}
