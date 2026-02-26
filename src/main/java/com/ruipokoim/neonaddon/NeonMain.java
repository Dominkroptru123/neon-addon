package com.ruipokoim.neonaddon;

import com.ruipokoim.neonaddon.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.orbit.EventHandler;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.MeteorClient;

public class NeonMain extends MeteorAddon {
	public static final Category CATEGORY = new Category("neon addon");
	public static int VERSION = 1;
	@Override
	public void onInitialize(){
		Modules.get().add(new AutoBreakCrystal());
		Modules.get().add(new AnchorCharge());
		Modules.get().add(new KelpESP());
		Modules.get().add(new RotatedDeepslateESP());
		//Modules.get().add(new AutoPlaceCrystal());
		MeteorClient.EVENT_BUS.subscribe(this);
	}
	@Override
	public void onRegisterCategories(){
		Modules.registerCategory(CATEGORY);
	}
	@Override
	public String getPackage(){
		return "com.ruipokoim.neonaddon";
	}
}