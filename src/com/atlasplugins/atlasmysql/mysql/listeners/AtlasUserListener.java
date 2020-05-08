package com.atlasplugins.atlasmysql.mysql.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.atlasplugins.atlasmysql.test.Main;
import com.atlasplugins.atlasmysql.test.User;

public class AtlasUserListener extends AtlasListener{
	
	private static boolean isDebbugable = false;
	
	public AtlasUserListener(Main core) {
		super(core);
		isDebbugable = core.getConfig().getBoolean("Database.Debug");
	}

	
	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = true)
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
		String user = event.getName();
		if(!this.getStorage().getCache().containsKey(user)) {
			User userObject = null;
			if(this.getStorage().getDataSource().exists(user, "tabelaNova")) {
				userObject = Main.getStorager().getDataSource().find(user, "tabelaNova", User.class);
				if(isDebbugable) {
				Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL] Download do usuário efetuado com sucesso.");
				}
			}else {
				userObject = new User(user, "tabelaNova");
				if(isDebbugable) {
				Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL] Usuário "+user+" é novato, criando objeto...");
				}
			}
			this.getStorage().getCache().put(user, userObject);
		}
	}
	
	 @EventHandler
	 public void onQuit(PlayerQuitEvent e){
		 if (!this.getStorage().getCache().containsKey(e.getPlayer().getName())) return;
		 User storable = this.getStorage().get(e.getPlayer().getName());
		 this.getStorage().getDataSource().insert(storable, storable.getTableStored(), false);
		 this.getStorage().getCache().remove(e.getPlayer().getName());
		 Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL] Usuário "+e.getPlayer().getName()+" salvo com sucesso.");
	 }
}
