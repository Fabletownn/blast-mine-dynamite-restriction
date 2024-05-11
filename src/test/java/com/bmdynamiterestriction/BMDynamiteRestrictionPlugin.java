package com.bmdynamiterestriction;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BMDynamiteRestrictionPlugin
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BMDynamiteRestrictionPlugin.class);
		RuneLite.main(args);
	}
}