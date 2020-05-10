package com.atlasplugins.atlasmysql.mysql.cores;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.atlasplugins.atlasmysql.mysql.datasources.MySQLDataSource;
import com.atlasplugins.atlasmysql.mysql.datasources.MySQLHikariDataSource;
import com.atlasplugins.atlasmysql.mysql.datasources.SQLLiteDataSource;
import com.atlasplugins.atlasmysql.mysql.interfaces.DataSource;
import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;
import com.atlasplugins.atlasmysql.test.Main;

public class AtlasStorage {

	
	private JavaPlugin core;
	private DataSource dataSource;
	private DataSourceType dataSourceType;
	private Map<String,Storable> cache;
	
	public AtlasStorage(JavaPlugin core, String... tables) {
		super();
		this.cache = new HashMap<String,Storable>();
		this.core = core;
		if(core.getConfig().getString("Database.Tipo").equalsIgnoreCase("MYSQL")) {
			this.dataSource = new MySQLDataSource(core, tables);
			this.dataSourceType = DataSourceType.MYSQL;
		}else if(core.getConfig().getString("Database.Tipo").equalsIgnoreCase("MYSQL_HIKARI")){
			this.dataSource = new MySQLHikariDataSource(core, tables);
			this.dataSourceType = DataSourceType.MYSQL_HIKARI;
		}else {
			this.dataSource = new SQLLiteDataSource(core, tables);
			this.dataSourceType = DataSourceType.SQLLITE;
		}
		Bukkit.getConsoleSender().sendMessage("§e[AtlasStorage] Tipo de armazenamento configurado: "+core.getConfig().getString("Database.Tipo").toUpperCase());
	}
	
	public <T extends Storable> T get(String key) {
	     return (T) cache.get(key);
	}
	
	public void close() {
		cache.values().forEach(storable->{
			Main.getStorager().getDataSource().insert(storable, storable.getTableStored(), false);
		});
		dataSource.close();
	}

	public Map<String, Storable> getCache() {
		return cache;
	}

	public DataSourceType getDataSourceType() {
		return dataSourceType;
	}

	public JavaPlugin getCore() {
		return core;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	
	public enum DataSourceType{
		MYSQL,MYSQL_HIKARI,SQLLITE;
	}
	
}
