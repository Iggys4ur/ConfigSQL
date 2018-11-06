package site.modicum.configsql;
/**
 * @Author Iggysaur
 * @Purpose creating SQLite databases from a YAML config
 *
 */

import com.pablo67340.SQLiteLib.Database.Database;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class CSQL extends JavaPlugin {

    private static CSQL INSTANCE;
    private static ConfigSQL_Util cSQL;
    private List<Database> databases;
    private File customConfigFile;
    private FileConfiguration databaseConstructor;

    public static CSQL getInstance(){
        return INSTANCE;
    }

    public static ConfigSQL_Util getUtil(){
        return cSQL;
    }

    public List<Database> getDatabases(){
        return databases;
    }

    public FileConfiguration getDatabaseConstructor(){
        return databaseConstructor;
    }

    @Override
    public void onEnable(){
        saveDefaultConfig();
        INSTANCE = this;

        getLogger().info("===================[ConfigSQL]=====================");
        boolean flag = getConfig().getBoolean("initializeDatabases");
        getLogger().info("Initializing Databases: " + flag);
        createDBConstructor();
        cSQL = new ConfigSQL_Util(INSTANCE, flag);
        databases = cSQL.getDatabases();

        if(!flag) {
            getLogger().info("Can be set in CSQL/config.yml");
        }else{getLogger().info("Loading DB = " + databaseConstructor.getStringList("databaseNames").toString());}

        getLogger().info("===================================================");
    }

    private void createDBConstructor() {

        String dbConfigFileName = "databaseConstructor.yml";
        customConfigFile = new File(getDataFolder(), dbConfigFileName);
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource(dbConfigFileName, false);
        }
        databaseConstructor = new YamlConfiguration();
        try {
            databaseConstructor.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveDBConstructor(String dbConfigFileName) {
        try{
            saveResource(dbConfigFileName, true);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable(){}

}

