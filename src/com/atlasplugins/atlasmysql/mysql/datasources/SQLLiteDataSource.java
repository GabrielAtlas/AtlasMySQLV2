package com.atlasplugins.atlasmysql.mysql.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
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
import com.atlasplugins.atlasmysql.mysql.interfaces.DataSource;
import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SQLLiteDataSource implements DataSource {

	private List<String> tables;
	private JavaPlugin core;
	private Gson gson;
	private Connection connection;

	public SQLLiteDataSource(JavaPlugin core, List<String> tables) {
		this.core = core;
		this.tables = tables;
		setup(core);
	}

	public SQLLiteDataSource(JavaPlugin core, String... tables) {
		this.core = core;
		this.tables = new ArrayList<String>();
		for (String s : tables) {
			this.tables.add(s);
		}
		setup(core);
	}

	public SQLLiteDataSource(JavaPlugin core, String table) {
		this.core = core;
		this.tables = new ArrayList<String>();
		this.tables.add(table);
		setup(core);
	}

	private void setup(JavaPlugin core) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.enableComplexMapKeySerialization();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationAdapter());
		gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter());
		gson = gsonBuilder.create();
		try {
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager
					.getConnection("jdbc:sqlite:" + core.getDataFolder().getPath() + "/database.db");
			for (String table : tables) {
				createTables(table);
			}
			Bukkit.getConsoleSender().sendMessage("§a[AtlasSQLLite] Conexão com o SQLLite foi estabelecida.");
		} catch (SQLException | ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage("§c[AtlasSQLLite] Erro ao conectar com SQLLite.");
			Bukkit.getConsoleSender().sendMessage("§c[AtlasMySQL] StackTrace: " + e.getMessage());
		}
	}

	private void createTables(String table) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table
				+ "`(`key` VARCHAR(16) NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY (`key`))")) {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}
	}

	@Override
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

	@Override
	public void insert(Storable storable, String tableName, boolean async) {
		if (async) {
			new BukkitRunnable() {

				@Override
				public void run() {
					try (PreparedStatement preparedStatement = connection.prepareStatement(
							"INSERT OR REPLACE INTO `" + tableName + "`(`key`, `json`) VALUES (?, ?)")) {
						preparedStatement.setString(1, storable.getKey());
						preparedStatement.setString(2, gson.toJson(storable));
						preparedStatement.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(core);
		} else {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT OR REPLACE INTO `" + tableName + "`(`key`, `json`) VALUES (?, ?)")) {
				preparedStatement.setString(1, storable.getKey());
				preparedStatement.setString(2, gson.toJson(storable));
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
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

	@Override
	public <T extends Storable> List<T> getAll(Class<T> clazz, String tableName) {
		 List<T> toReturn = new ArrayList<T>();
	        try {
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
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `key` = ?")){
            preparedStatement.setString(1, key);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
	}

	@Override
	public void close() {
		try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

	@Override
	public boolean isClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
	}

}
