package com.github.tehelusivepanda;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PorkNChubTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PorkNChubPlugin.class);
		RuneLite.main(args);
	}
}