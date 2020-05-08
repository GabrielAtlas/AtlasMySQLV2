package com.atlasplugins.atlasmysql.mysql.cores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.atlasplugins.atlasmysql.mysql.exceptions.AtlasSQLException;
import com.atlasplugins.atlasmysql.mysql.interfaces.AtlasSQLConnection;

public class MySQLConnection implements AtlasSQLConnection{

	private static final long KEEP_ALIVE_DELAY = 1000L * 60L * 4L;
	public static final ConsoleCommandSender console = Bukkit.getConsoleSender();
	
	private String host;
	private String user;
	private String password;
	private String database;
	private List<String> tablesNames = new ArrayList<String>();
	private List<String> tableStatements = new ArrayList<String>();
	private int port;

	private Connection connection;
	private JavaPlugin plugin;
	
	public MySQLConnection(JavaPlugin plugin, String host, String user, String password, String database, int port) {
		super();
		this.plugin = plugin;
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;
		this.port = port;
	}
	
	@Override
	public Connection getConnection() {
		return connection;
	}
	
	public void setTables(List<String> tables) {
		this.tablesNames = tables;
	}
	
	public void addTableStatement(String tableQuery) {
		this.tablesNames.add(tableQuery);
	}
	
	public void addTableAndName(String tableName) {
		this.tablesNames.add(tableName);
		this.tableStatements.add("CREATE TABLE IF NOT EXISTS `"+tableName+"` ( `id` INT NOT NULL AUTO_INCREMENT , `jogador` VARCHAR(50) NOT NULL , `json` LONGTEXT NOT NULL , PRIMARY KEY (`id`));");
	}
	
	public boolean openConnection() {
		if (connection != null) {
			Bukkit.getConsoleSender().sendMessage("§c[AtlasMySQL] Já existe uma conexão MySQL aberta e por isso não foi possível abrir a conexão.");
			return false;
		}
		try {
			this.connection = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database+"?autoReconnect=true", user, password);
			Bukkit.getConsoleSender().sendMessage("§a[AtlasMySQL] A conexão com o MySQLConnection foi efetuada com sucesso, criando tabelas...");
			createTable();
			keepAlive();
			return true;
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage("§c[AtlasMySQL] Erro ao abrir conexão com MySQL");
			Bukkit.getConsoleSender().sendMessage("§c[AtlasMySQL "+e.getErrorCode()+"] StackTrace: "+e.getMessage());
			return false;
		}
		
	}
	
	private void keepAlive() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(String table : MySQLConnection.this.tablesNames) {
				 try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM "+table+";")) {
					 statement.executeQuery();
				 } catch (SQLException e) {
					e.printStackTrace();
				}
				}
			}
		}.runTaskTimerAsynchronously(JavaPlugin.getPlugin(plugin.getClass()), KEEP_ALIVE_DELAY, KEEP_ALIVE_DELAY);
	}

	@Override
	public void closeConnection() {
		if(connection == null) {
			throw new AtlasSQLException("[AtlasMySQL] A conexão remota com o servidor MySQLConnection já foi encerrada.");
		}
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createTable() {
		if(connection!=null) {
			if(this.tablesNames.size() > 0) {
				for(String tableStatement : this.tableStatements) {
					try(Statement stmt = connection.createStatement()){
						stmt.executeUpdate(tableStatement);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}else {
				throw new AtlasSQLException("[AtlasMySQL] Nenhuma tabela foi adicionada para ser carregada.");
			}
		}else {
			throw new AtlasSQLException("[AtlasMySQL] A conexão com o servidor MySQLConnection ainda não foi aberta por este motivo a tabela não foi criada.");
		}
	}

}
