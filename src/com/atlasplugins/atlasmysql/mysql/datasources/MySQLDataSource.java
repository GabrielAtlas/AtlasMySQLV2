package com.atlasplugins.atlasmysql.mysql.datasources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.atlasplugins.atlasmysql.mysql.adapters.ItemStackAdapter;
import com.atlasplugins.atlasmysql.mysql.adapters.LocationAdapter;
import com.atlasplugins.atlasmysql.mysql.cores.MySQLConnection;
import com.atlasplugins.atlasmysql.mysql.interfaces.DataSource;
import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class MySQLDataSource implements DataSource {

	private JavaPlugin core;
	private List<String> tables;
	private Gson gson;
	private MySQLConnection mySQL;
	private Connection connection;

	public MySQLDataSource(JavaPlugin core, String tableName) {
		this.tables = new ArrayList<String>();
		this.tables.add(tableName);
		setup(core);
	}

	public MySQLDataSource(JavaPlugin core, List<String> tables) {
		this.tables = tables;
		setup(core);
	}

	public MySQLDataSource(JavaPlugin core, String... tableNames) {
		this.tables = new ArrayList<String>();
		for (String s : tableNames) {
			tables.add(s);
		}
		setup(core);
	}

	private void setup(JavaPlugin core) {
		this.core = core;
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.enableComplexMapKeySerialization();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationAdapter());
		gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter());
		gson = gsonBuilder.create();
		mySQL = new MySQLConnection(core, core.getConfig().getString("Database.IP").replace(":3306", ""),
				core.getConfig().getString("Database.User"), core.getConfig().getString("Database.Pass"),
				core.getConfig().getString("Database.DB"), 3306);
		mySQL.setTables(tables);
		if (mySQL.openConnection()) {
			this.connection = mySQL.getConnection();
			for (String table : tables) {
				createTable(table);
			}
			Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL] Conexão estabilizada com sucesso.");
		} else {
			Bukkit.getConsoleSender().sendMessage(
					"§a[AtlasSQLLite] Inicializando recuperação de conexão por SQLLITE já que o servidor MySQL não encontra-se disponível.");
			new SQLLiteDataSource(core);
		}
	}

	private void createTable(String tableName) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + tableName
					+ "`(`key` VARCHAR(16) NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY (`key`))");
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Find a key in MySQL
	 * 
	 */

	public <T extends Storable> T find(String key, String tableName, Class<T> clazz) {
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `key` = ?")) {
			preparedStatement.setString(1, key);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return gson.fromJson(resultSet.getString("json"), clazz);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Insert a key storable in MySQL
	 * 
	 */

	public void insert(Storable storable, String tableName, boolean async) {
		if (async) {
			new BukkitRunnable() {

				@Override
				public void run() {
					try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" + tableName
							+ "`(`key`, `json`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `json` = VALUES(`json`)")) {
						preparedStatement.setString(1, storable.getKey());
						preparedStatement.setString(2, gson.toJson(storable));
						preparedStatement.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(core);
		} else {
			try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" + tableName
					+ "`(`key`, `json`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `json` = VALUES(`json`)")) {
				preparedStatement.setString(1, storable.getKey());
				preparedStatement.setString(2, gson.toJson(storable));
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Delete a key from MySQL
	 * 
	 */

	public void delete(String key, String tableName, boolean async) {
		if (async) {
			new BukkitRunnable() {

				@Override
				public void run() {
					try (PreparedStatement preparedStatement = connection
							.prepareStatement("DELETE FROM `" + tableName + "` WHERE `key` = ?")) {
						preparedStatement.setString(1, key);
						preparedStatement.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(core);
		} else {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("DELETE FROM `" + tableName + "` WHERE `key` = ?")) {
				preparedStatement.setString(1, key);
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Get all storable informations from MySQL
	 * 
	 */

	public <T extends Storable> List<T> getAll(Class<T> clazz, String tableName) {
		List<T> toReturn = new ArrayList<T>();
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + tableName + "`");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				try {
					T storable = gson.fromJson(resultSet.getString("json"), clazz);
					if (storable == null || storable.getKey() == null)
						continue;
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

	/*
	 * Check if exists a key in database
	 * 
	 */

	public boolean exists(String key, String tableName) {
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `key` = ?");
			preparedStatement.setString(1, key);
			return preparedStatement.executeQuery().next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Close connection
	 * 
	 */

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Check if connection is closed
	 * 
	 */

	public boolean isClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
	}

}
