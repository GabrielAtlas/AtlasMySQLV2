package com.atlasplugins.atlasmysql.mysql.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.atlasplugins.atlasmysql.mysql.cores.AtlasStorage;
import com.atlasplugins.atlasmysql.test.Main;

public abstract class AtlasListener implements Listener{

	
	protected final Main core;
	protected final AtlasStorage storage;
	
	public AtlasListener(Main core) {
		this.core = core;
		this.storage = core.getStorager();
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	public JavaPlugin getCore() {
		return core;
	}
	public AtlasStorage getStorage() {
		return storage;
	}
	
	
}
