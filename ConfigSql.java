package com.modicum.configsql;

import com.pablo67340.SQLiteLib.Database.Database;
import com.pablo67340.SQLiteLib.Main.SQLiteLib;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigSql {


    private JavaPlugin plugin;
    private SQLiteLib sqlLib;
    private FileConfiguration config = plugin.getConfig();
    private List<Database> databases;

    public ConfigSql(JavaPlugin PLUGIN_INSTANCE){
        plugin = PLUGIN_INSTANCE;
        sqlLib = SQLiteLib.hookSQLiteLib(plugin);
        initializeDatabases();
    }

    private String[][] getDBLayout(String databaseName){

        String[][] output = {{databaseName}};
        int outdex = 1;
        int index = 0;

        for (String table :getTableNames(databaseName)) {

            output[outdex][index++] = table;
            output[outdex++][index] = getTableCreationStatement(databaseName, table);

            for (String column: getColumnNames(databaseName, table)) {
                index = 0;

                output[outdex][index++] = column;
                output[outdex++][index] = getColumnCreationStatement(databaseName, table, column);
            }
        }

        return output;

    }

    private void initializeDatabases(){
        int tableIndex = 0;

        if (createTables().toArray().length == getTotalTables())
        {
            for (String db: getDatabaseNames()) {

                for (String table: getTableNames(db)) {

                    sqlLib.initializeDatabase(db, createTables().get(tableIndex++));
                    databases.add(sqlLib.getDatabase(db));
                }
            }
        }
    }

    private int getTotalTables(){
        int totalTables = 0;

        for (String db: getDatabaseNames()) {

            totalTables += getNumTables(db);
        }
        return totalTables;
    }

    private int getTotalColumns(){
        int totalColumns = 0;

        for (String db: getDatabaseNames()) {

            for (String table :getTableNames(db)){

                totalColumns += getNumColumns(db,table);
            }
        }
        return totalColumns;
    }

    private List<String> createTables(){

        List<String> output = new ArrayList<>();

        for (String db:getDatabaseNames()){

                for (String table:getTableNames(db)){

                    output.add(tableCreationStatement(db, table));
                }
            }
        return output;
    }

    private String tableCreationStatement(String db, String table){
        StringBuilder statement = new StringBuilder();
        statement.append(getTableCreationStatement(db,table)); //get raw creation statement ~ "CREATE TABLE IF NOT EXISTS"
        statement.append(" ");
        statement.append(table); //add table name
        statement.append("(");
        for (int i = 0; i < getNumColumns(db,table); i++) {
            statement.append(getColumnName(db, table, i));
            statement.append(getColumnCreationStatement(db,table,getColumnName(db,table,i)));
            statement.append(" ");
            if(i != getNumColumns(db,table) - 1){
                statement.append(","); } }
        statement.append(")");
        return statement.toString();
    }

    private String addStatement(String tableName, String[] columns, Object[] queries){
        return "INSERT INTO '" + tableName + "'" + iterateForSQL(columns) + " VALUES " + iterateForSQL(queries) + ";";
    }

    private String deleteStatement(String tableName, String columnToSearch, Object queryToDelete)
    {
        return "DELETE FROM '" + tableName + "' WHERE " + columnToSearch + " = '" + queryToDelete + "';";
    }

    private String iterateForSQL(Object[] query){

        StringBuilder queries = new StringBuilder();

        queries.append("(");
        for (int i = 0; i < query.length ; i++) {
            queries.append("'");
            queries.append(query[i]);
            queries.append("'");
            if(i != query.length - 1){
                queries.append(","); } }
        queries.append(")");

        return queries.toString();
    }

    //getters
    private int getNumDatabases(){
        return config.getStringList("databases").toArray().length;
    }

    private List<String> getDatabaseNames(){
        return config.getStringList("databases");
    }

    private String getDatabaseName(int index){
        return config.getStringList("databases").get(index);
    }

    private int getNumTables(String dataBaseName){
        return config.getStringList("databases." + dataBaseName + ".tables").toArray().length;
    }

    private List<String> getTableNames(String databaseName){
        return config.getStringList("databases." + databaseName + ".tables");
    }

    private String getTableName(String databaseName, int index){
        return config.getStringList("databases." + databaseName + ".tables").get(index);
    }

    private String getTableCreationStatement(String databaseName, String tableName){
        return config.getString("databases." + databaseName + ".tables." + tableName + "." + ".creationStatement");
    }

    private int getNumColumns(String databaseName, String tableName){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames").toArray().length;
    }

    private List<String> getColumnNames(String databaseName, String tableName){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames");
    }

    private String getColumnName(String databaseName, String tableName, int index){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames").get(index);
    }

    private String getColumnCreationStatement(String databaseName, String tableName, String columnName){
        return config.getString("databases." + databaseName + ".tables." + tableName + "." + columnName + ".creationStatement");
    }

    //setters
    private void setDatabaseName(int index, String newName){
        List<String> list = getDatabaseNames();
        list.set(index,newName);
        config.set("databases", list);
        plugin.saveConfig();
    }

    private void setDatabaseName(String oldName, String newName){
        List<String> list = getDatabaseNames();
        list.set(list.indexOf(oldName), newName);
        config.set("databases", list);
        plugin.saveConfig();
    }

    private void setTableName(int index, String databaseName, String newName){
        List<String> list = getTableNames("databases." + databaseName);
        list.set(index,newName);
        config.set("databases." + databaseName + ".tables", list);
        plugin.saveConfig();
    }

    private void setTableName(String oldName, String databaseName, String newName){
        List<String> list = getTableNames("databases." + databaseName);
        list.set(list.indexOf(oldName), newName);
        config.set("databases." + databaseName + ".tables", list);
        plugin.saveConfig();
    }

    private void setTableCreationStatement(String databaseName, String tableName, String creationStatement){
        config.set("databases." + databaseName + ".tables." + tableName + ".creationStatement", creationStatement);
        plugin.saveConfig();
    }

    private void setColumnName(String databaseName, String tableName, int index, String newName){
        List<String> list = getColumnNames(databaseName, tableName);
        list.set(index,newName);
        config.set("databases." + databaseName + ".tables." + tableName + ".columnNames", list);
        plugin.saveConfig();
    }

    private void setColumnName(String databaseName, String tableName, String oldName, String newName){
        List<String> list = getColumnNames(databaseName, tableName);
        list.set(list.indexOf(oldName), newName);
        config.set("databases." + databaseName + ".tables." + tableName + ".columnNames", list);
        plugin.saveConfig();
    }

    private void setColumnCreationStatement(String databaseName, String tableName, String columnName, String creationStatement){
        config.set("databases." + databaseName + ".tables." + tableName + "." + columnName + ".creationStatement", creationStatement);
        plugin.saveConfig();
    }



}
