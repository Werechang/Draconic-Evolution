package com.brandon3055.draconicevolution;

import com.brandon3055.draconicevolution.client.creativetab.DETab;
import com.brandon3055.draconicevolution.common.CommonProxy;
import com.brandon3055.draconicevolution.common.utills.LogHelper;
import com.brandon3055.draconicevolution.common.lib.References;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;

import java.util.Arrays;

@Mod(modid = References.MODID, name = References.MODNAME, version = References.VERSION, canBeDeactivated = false, guiFactory = References.GUIFACTORY,  dependencies = "after:NotEnoughItems;after:NotEnoughItems;after:ThermalExpansion;after:ThermalFoundation;")
public class DraconicEvolution { // TODO Update Licence and add change log to github

	@Mod.Instance(References.MODID)
	public static DraconicEvolution instance;

	@SidedProxy(clientSide = References.CLIENTPROXYLOCATION, serverSide = References.COMMONPROXYLOCATION)
	public static CommonProxy proxy;

	public static CreativeTabs tabToolsWeapons = new DETab(CreativeTabs.getNextID(), References.MODID, "toolsAndWeapons", 0);
	public static CreativeTabs tabBlocksItems = new DETab(CreativeTabs.getNextID(), References.MODID, "blocksAndItems", 1);

	public static final String networkChannelName = "DraconicEvolution";
	public static SimpleNetworkWrapper network;

	public static boolean debug = false;//todo

	public static Enchantment reaperEnchant;
	
	public DraconicEvolution()
	{
		LogHelper.info("Hello Minecraft!!!");
	}
	
	@Mod.EventHandler
	public static void preInit(final FMLPreInitializationEvent event)
	{if(debug)
		LogHelper.info("Initialization");

		event.getModMetadata().autogenerated = false;
		event.getModMetadata().credits = "";
		event.getModMetadata().description = "This is a mod originally made for the Tolkiencraft mod pack";
		event.getModMetadata().authorList = Arrays.asList("brandon3055");
		event.getModMetadata().logoFile = "banner.png";
		event.getModMetadata().url = "http://www.tolkiencraft.com/draconic-evolution/";
		event.getModMetadata().version = References.VERSION + "-MC1.7.10";

		proxy.preInit(event);

		/*
		public static Achievement ultimatePower;
		ultimatePower = new Achievement("achievment.ultimatePower", "Ultimate Power!!!", 1, -2, ModItems.draconicDestructionStaff, null).registerStat();
		AchievementPage draconicEvolution = new AchievementPage("Draconic Evolution", ultimatePower);
		AchievementPage.registerAchievementPage(draconicEvolution);
		*/

	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event)
	{if(debug)
		System.out.println("init()");
		
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(final FMLPostInitializationEvent event)
	{if(debug)
		System.out.println("postInit()");
	
		proxy.postInit(event);
		
	}
}