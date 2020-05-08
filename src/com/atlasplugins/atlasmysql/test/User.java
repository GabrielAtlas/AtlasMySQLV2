package com.atlasplugins.atlasmysql.test;

import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;

public class User implements Storable{

	private final transient String table;
	private String key;

	public User(String key, String tableStored) {
		super();
		this.key = key;
		table = tableStored;
	}

	public String getKey() {
		return key;
	}

	public String getTableStored() {
		return table;
	}

}
