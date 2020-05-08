package com.atlasplugins.atlasmysql.mysql.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;
import com.atlasplugins.atlasmysql.test.Main;

public class AutoSave extends BukkitRunnable {

    private final Main main;
    
    public AutoSave(Main main) {
        this.main = main;
        runTaskTimerAsynchronously(main, 20l*60*30, 20l*60*30);
    }

    @Override
    public void run() {
    	int dadosSalvos = 0;
        for (Storable storable : Main.getStorager().getCache().values()) {
        	Main.getStorager().getDataSource().insert(storable, storable.getTableStored(), false); // não precisa ser em async já que já é em async
        	dadosSalvos++;
        }
        Bukkit.getConsoleSender().sendMessage("§a[AtlasAutoSave] Foram salvos "+dadosSalvos+" dados na tabela com sucesso.");
    }

}
