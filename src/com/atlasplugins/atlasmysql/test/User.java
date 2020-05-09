package com.atlasplugins.atlasmysql.test;

import com.atlasplugins.atlasmysql.mysql.interfaces.Storable;

public class User implements Storable{

	private String key;

	public User(String key) {
		super();
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getTableStored() {
		return "tabelaNova";
	}

}
