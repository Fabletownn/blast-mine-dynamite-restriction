package com.test;

import com.bmdynamiterestriction.BMDynamiteRestrictionPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BMDynamiteRestrictionTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BMDynamiteRestrictionPlugin.class);
		RuneLite.main(args);
	}
}