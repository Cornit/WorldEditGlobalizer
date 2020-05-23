package me.illgilp.worldeditglobalizerbungee.storage;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.illgilp.worldeditglobalizerbungee.storage.table.Table;

public class Database {
	private JdbcConnectionSource source;
	private String driver;
	private String username;
	private String password;


	private List<Table> registeredTables = new ArrayList<>();
	private List<Table> pendingInit = new ArrayList<>();

	public Database(File databasefile) {
		if(databasefile.getParentFile() != null)databasefile.getParentFile().mkdirs();
		if(!databasefile.exists()){
			try {
				databasefile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		driver = "jdbc:sqlite:"+databasefile.getPath();
	}

	public Database(String host, int port, String databaseName, String username, String password) {
		this.driver = "jdbc:mysql://"+host+":"+port+"/"+databaseName+"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&useSSL=false";
		this.username = username;
		this.password = password;

	}

	public void initDatabase() throws Exception {
			source = new JdbcConnectionSource(driver,username,password);
			for(Table table : new ArrayList<>(pendingInit)){
				if (table.init(source)) {
					registeredTables.add(table);
					pendingInit.remove(table);
				}
			}
	}

	public boolean registerTable(Table table){
		for(Table tab : registeredTables){
			if(tab.getClass().equals(table.getClass()))return false;
		}

		if(source == null){
			pendingInit.add(table);
			return true;
		}else {
			if (table.init(source)) {
				registeredTables.add(table);
			}


			return table.isInitialized();
		}
	}

	public <T extends Table> T getTable(Class<T> tableClass){
		for(Table tab : registeredTables){
			if(tab.getClass().equals(tableClass) && tab.isInitialized()){
				return (T) tab;
			}
		}
		return null;
	}

	public void disconnect(){
		try {
			this.source.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isInit(){return this.source !=null;}

}
