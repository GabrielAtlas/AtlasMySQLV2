package com.atlasplugins.atlasmysql.mysql.interfaces;

import java.sql.Connection;

public interface AtlasSQLConnection {

	public Connection getConnection();

	public boolean openConnection();

	public void closeConnection();
	
}
