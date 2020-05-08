package com.atlasplugins.atlasmysql.mysql.interfaces;

import java.util.List;
import java.util.Map;

public interface DataSource {

	 /**
     * @param key   key que esteja a procurar
     * @param clazz classe que queira que retorne
     * @return storable desejado
     */
	
    public <T extends Storable> T find(String key, String tableName,  Class<T> clazz);

    public void insert(Storable storable, String tableName, boolean async);

    public void delete(String key, String tableName, boolean async);

    /**
     * @param clazz classe que queira que retorne
     * @return lista de storables desejado
     */
    public <T extends Storable> List<T> getAll(Class<T> clazz, String tableName);

    /**
     * @param key
     * @return se existe na database
     */
    public boolean exists(String key, String tableName);

    public void close();

    public boolean isClosed();
    
}
