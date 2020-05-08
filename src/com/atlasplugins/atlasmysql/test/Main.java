package com.atlasplugins.atlasmysql.test;

import org.bukkit.plugin.java.JavaPlugin;

import com.atlasplugins.atlasmysql.mysql.cores.AtlasStorage;
import com.atlasplugins.atlasmysql.mysql.listeners.AtlasUserListener;
import com.atlasplugins.atlasmysql.mysql.tasks.AutoSave;

public class Main extends JavaPlugin{

	private static AtlasStorage storager;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		storager = new AtlasStorage(this, "tabelaNova");
		new AutoSave(this);
		new AtlasUserListener(this);
		super.onEnable();
	}
	
	
	@Override
	public void onDisable() {
		storager.close();
		super.onDisable();
	}


	public static AtlasStorage getStorager() {
		return storager;
	}

}
