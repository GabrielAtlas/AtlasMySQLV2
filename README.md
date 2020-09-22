# AtlasMySQLV2
The best and the most compact API for Bukkit MySQL

# How to use?
In your Main.class, make this:
```java
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
```

# Getting Json Object from MySQL

```java
  Main.getStorager().get(key) // get a user cached
  Main.getStorager().getDataSource().exists(key, table) //get a user in MySQL or SQLLITE DB
  Main.getStorager().getDataSource().close() //close the datasource connection
  Main.getStorager().getDataSource().isClosed() //Check if the connection is closed.
  Main.getStorager().getDataSource().getAll(Class<T> clazz, String tableName) //Get All users in MySQL or SQLLITE DB
  Main.getStorager().getDataSource().delete(String key, String tableName, boolean async) //Delete a user asynchronously
  Main.getStorager().getDataSource().insert(Storable storable, String tableName, boolean async) // insert a user in MySQL or SQLLITE DB
  Main.getStorager().getDataSource().find(String key, String tableName,  Class<T> clazz) //check if contains a user in MySQL or SQLLITE DB
```

# Config.yml


# Tipos de MySQL:
 * MYSQL_HIKARI = Versão de conexão do plugin mais performática porém consome mais memória & cpu
 * MYSQL =  Versão de conexão do plugin com as threads do próprio SPIGOT/BUKKIT
 * SQLLITE = Utiliza o arquivo "database.sql" como armazenamento.
 ```yml
Database:
  Tipo: SQLITE 
  IP: localhost:3306
  DB: test
  User: root
  Pass: ""
  Debug: true
```

