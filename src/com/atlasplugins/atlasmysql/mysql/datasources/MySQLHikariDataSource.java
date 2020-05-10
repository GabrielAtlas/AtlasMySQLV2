package com.atlasplugins.atlasmysql.mysql.datasources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.atlasplugins.atlasmysql.mysql.adapters.ItemStackAdapter;
import com.atlasplugins.atlasmysql.mysql.adapters.LocationAdapter;
import com.atlasplugins.atlasmysql.mysql.interfaces.DataSource;
import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLHikariDataSource implements DataSource{

	private JavaPlugin core;
	private List<String> tables;
	private HikariDataSource dataSource;
	private Gson gson;
	private ExecutorService executor;

	public MySQLHikariDataSource(JavaPlugin core, String... tableNames) {
		this.core = core;
		this.tables = new ArrayList<String>();
		for (String s : tableNames) {
			tables.add(s);
		}
		setup();
	}

	public MySQLHikariDataSource(JavaPlugin core, List<String> tables) {
		this.core = core;
		this.tables = new ArrayList<String>();
		for (String s : tables) {
			tables.add(s);
		}
		setup();
	}

	public MySQLHikariDataSource(JavaPlugin core, String tableName) {
		this.core = core;
		this.tables = new ArrayList<String>();
		this.tables.add(tableName);
		setup();
	}

	private void setup() {
		this.executor = Executors.newFixedThreadPool(3);
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.enableComplexMapKeySerialization();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationAdapter());
		gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter());
		gson = gsonBuilder.create();
		String url = "jdbc:mysql://" + core.getConfig().getString("Database.IP").replace(":3306", "") + ":3306/" 
		+ core.getConfig().getString("Database.DB") + "?autoReconnect=true";
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(url);
		hikariConfig.setUsername(core.getConfig().getString("Database.User"));
		hikariConfig.setPassword(core.getConfig().getString("Database.Pass"));
		hikariConfig.setMaximumPoolSize(3);
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		try {
			this.dataSource = new HikariDataSource(hikariConfig);
			for (String table : tables) {
				createTable(table);
			}
			Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL Hikari] Conexão com o MySQL estabelecida.");
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("§c[AtlasMySQL Hikari] Erro ao conectar-se ao MySQL.");
			e.printStackTrace();
		}
	}

	private void createTable(String tableName) {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + tableName
					+ "`(`key` VARCHAR(16) NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY (`key`))");
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T extends Storable> T find(String key, String tableName, Class<T> clazz) {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `key` = ?");
			preparedStatement.setString(1, key);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return gson.fromJson(resultSet.getString("json"), clazz);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void insert(Storable storable, String tableName, boolean async) {
		Runnable runnable = () -> {
			try (Connection connection = dataSource.getConnection()) {
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" + tableName + "`(`key`, `json`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `json` = VALUES(`json`)");
				preparedStatement.setString(1, storable.getKey());
				preparedStatement.setString(2, gson.toJson(storable));
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		};

		if (async) executor.submit(runnable);
		else runnable.run();
	}

	@Override
	public void delete(String key, String tableName, boolean async) {
		Runnable runnable = () -> {
			try (Connection connection = dataSource.getConnection()) {
				PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `" + tableName + "` WHERE `key` = ?");
				preparedStatement.setString(1, key);
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		};

		if (async) executor.submit(runnable);
		else runnable.run();
	}

	@Override
	public <T extends Storable> List<T> getAll(Class<T> clazz, String tableName) {
		List<T> toReturn = new ArrayList<T>();
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + tableName + "`");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				try {
					T storable = gson.fromJson(resultSet.getString("json"), clazz);
					if (storable == null || storable.getKey() == null) continue;
					toReturn.add(storable);
				} catch (JsonSyntaxException e) {
					continue;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public boolean exists(String key, String tableName) {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `key` = ?");
			preparedStatement.setString(1, key);
			return preparedStatement.executeQuery().next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void close() {
		dataSource.close();
	}

	@Override
	public boolean isClosed() {
		return dataSource == null || dataSource.isClosed();
	}

}
