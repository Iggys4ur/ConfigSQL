package site.modicum.configsql;

import com.pablo67340.SQLiteLib.Database.Database;
import com.pablo67340.SQLiteLib.Main.SQLiteLib;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.List;

public class ConfigSQL_Util {


    private CSQL plugin;
    private SQLiteLib sqlLib;
    private FileConfiguration config;
    private List<Database> databases;

    public ConfigSQL_Util(CSQL pluginINSTANCE, boolean initialize){
        plugin = pluginINSTANCE;
        config = plugin.getDatabaseConstructor();
        sqlLib = SQLiteLib.hookSQLiteLib(plugin);
        if(initialize){initializeDatabases();}
    }

    public String[][] getDBLayout(String databaseName){

        int arrayLength = 1 + getNumTables(databaseName) + getNumColumnsInDB(databaseName);

        String[][] output = new String[arrayLength][2];
        output[0][0] = databaseName;
        output[0][1] = "Tab: " + getNumTables(databaseName) + " Col: " + getNumColumnsInDB(databaseName);

        int outdex = 1;
        int index = 0;

        for (String table :getTableNames(databaseName)) {

            output[outdex][index++] = table;
            output[outdex++][index] = getTableCreationStatement(databaseName, table);

            for (String column: getColumnNames(databaseName, table)) {
                index = 0;
                output[outdex][index++] = column;
                output[outdex++][index] = getColumnCreationStatement(databaseName, table, column);
                index = 0;
            }
        }
        return output;

    }

    public List<String> dbInfo(String db){

        List<String> list = new ArrayList<>();
        int row = 0;

            list.add(db.toUpperCase());

            for (String[] arr: getDBLayout(db)) {

                list.add("ROW: " + row++);
                List<String> list2 = new ArrayList<>();

                for (String arg : arr) {

                    list2.add(arg);
                    list.add("[" + list2.indexOf(arg) + "]::" + arg);

                }
            }
        return list;
    }

    public void initializeDatabases(){
        int tableIndex = 0;
        List<Database> dbList = new ArrayList<>();
        List<String> tables = createTables();

            if (tables.toArray().length == getTotalTables()) {

                for (String db : getDatabaseNames()) {

                    for (String table : getTableNames(db)) {

                        sqlLib.initializeDatabase(db, tables.get(tableIndex++));
                        dbList.add(sqlLib.getDatabase(db));
                        databases = dbList;
                    }
                }
            }
    }

    public int getTotalTables(){
        int totalTables = 0;

        for (String db: getDatabaseNames()) {

            totalTables += getNumTables(db);
        }
        return totalTables;
    }

    public int getTotalColumns(){
        int totalColumns = 0;

        for (String db: getDatabaseNames()) {

            for (String table : getTableNames(db)){

                totalColumns += getNumColumns(db,table);
            }
        }
        return totalColumns;
    }

    public int getNumColumnsInDB(String databaseName){
        int columns = 0;

        for (String table: getTableNames(databaseName)) {

            columns += getNumColumns(databaseName, table);
        }

        return columns;
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

    public String tableCreationStatement(String db, String table){

        StringBuilder statement = new StringBuilder();
        statement.append(getTableCreationStatement(db,table)); //get raw creation statement ~ "CREATE TABLE IF NOT EXISTS"
        statement.append(" ");
        statement.append(table); //add table name
        statement.append("(");
        for (int i = 0; i < getNumColumns(db,table); i++) {
            statement.append(getColumnName(db, table, i));
            statement.append(" ");
            statement.append(getColumnCreationStatement(db,table,getColumnName(db,table,i)));
            if(i != getNumColumns(db,table) - 1){
                statement.append(", "); }
        }
        statement.append(")");
        //plugin.getLogger().info(statement.toString());
        return statement.toString();
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
        //plugin.getLogger().info(queries.toString());
        return queries.toString();

    }

    //Methods to invoke SQL statements without hardcoded strings

    public String addRowStatement(String tableName, List<String> columns, List<String> queries){
        return "INSERT INTO '" + tableName + "'" + iterateForSQL(columns.toArray()) + " VALUES " + iterateForSQL(queries.toArray()) + ";";
    }

    public String deleteRowStatement(String tableName, String columnToSearch, Object queryToDelete)
    {
        return "DELETE FROM '" + tableName + "' WHERE " + columnToSearch + " = '" + queryToDelete + "';";
    }

    public String deleteEntryStatement(String columnToDeleteEntryFrom, String tableName, String columnToSearch, Object queryToDelete)
    {
        return "UPDATE '" + tableName + "' SET '" + columnToDeleteEntryFrom + "' = NULL WHERE " + columnToSearch + " = '" + queryToDelete + "';";
    }

    public String updateEntryStatement(String tableName, String columnToUpdate, Object updatedValue, String columnToSearch, Object valueToSearchFor)
    {
        return "UPDATE '" + tableName + "' SET " + columnToUpdate + " = '" + updatedValue + "' WHERE " + columnToSearch + " = '" + valueToSearchFor + "';";
    }

    public String updateEntriesStatement(String tableName, List<String> columnsToUpdate, List<Object> updatedValues, String columnToSearch, Object valueToSearchFor)
    {
        return "UPDATE '" + tableName + "' SET " + iterateForSQL(columnsToUpdate.toArray()) + " = '" + iterateForSQL(updatedValues.toArray()) + "' WHERE " + columnToSearch + " = '" + valueToSearchFor + "';";
    }

    //getters

    public List<Database> getDatabases(){
        return databases;
    }

    public int getNumDatabases(){
        return config.getStringList("databaseNames").toArray().length;
    }

    public List<String> getDatabaseNames(){
        return config.getStringList("databaseNames");
    }

    public String getDatabaseName(int index){
        return config.getStringList("databaseNames").get(index);
    }

    public int getNumTables(String dataBaseName){
        return config.getStringList("databases." + dataBaseName + ".tableNames").toArray().length;
    }

    public List<String> getTableNames(String databaseName){
        return config.getStringList("databases." + databaseName + ".tableNames");
    }

    public String getTableName(String databaseName, int index){
        return config.getStringList("databases." + databaseName + ".tableNames").get(index);
    }

    public String getTableCreationStatement(String databaseName, String tableName){
        return config.getString("databases." + databaseName + ".tables." + tableName + "." + ".creationStatement");
    }

    public int getNumColumns(String databaseName, String tableName){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames").toArray().length;
    }

    public List<String> getColumnNames(String databaseName, String tableName){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames");
    }

    public String getColumnName(String databaseName, String tableName, int index){
        return config.getStringList("databases." + databaseName + ".tables." + tableName + ".columnNames").get(index);
    }

    public String getColumnCreationStatement(String databaseName, String tableName, String columnName){
        return config.getString("databases." + databaseName + ".tables." + tableName + ".columns." + columnName + ".creationStatement");
    }

    /* setters seem entirely pointless for this project, these need updating to reflect key mapping changes

    public void setDatabaseName(int index, String newName){
        List<String> list = getDatabaseNames();
        list.set(index,newName);
        config.set("databaseNames", list);
        plugin.saveConfig();
    }

    public void setDatabaseName(String oldName, String newName){
        List<String> list = getDatabaseNames();
        list.set(list.indexOf(oldName), newName);
        config.set("databaseNames", list);
        plugin.saveConfig();
    }

    public void setTableName(int index, String databaseName, String newName){
        List<String> list = getTableNames("databaseNames." + databaseName);
        list.set(index,newName);
        config.set("databaseNames." + databaseName + ".tableNames", list);
        plugin.saveConfig();
    }

    public void setTableName(String oldName, String databaseName, String newName){
        List<String> list = getTableNames("databaseNames." + databaseName);
        list.set(list.indexOf(oldName), newName);
        config.set("databaseNames." + databaseName + ".tableNames", list);
        plugin.saveConfig();
    }

    public void setTableCreationStatement(String databaseName, String tableName, String creationStatement){
        config.set("databaseNames." + databaseName + ".tableNames." + tableName + ".creationStatement", creationStatement);
        plugin.saveConfig();
    }

    public void setColumnName(String databaseName, String tableName, int index, String newName){
        List<String> list = getColumnNames(databaseName, tableName);
        list.set(index,newName);
        config.set("databaseNames." + databaseName + ".tableNames." + tableName + ".columnNames", list);
        plugin.saveConfig();
    }

    public void setColumnName(String databaseName, String tableName, String oldName, String newName){
        List<String> list = getColumnNames(databaseName, tableName);
        list.set(list.indexOf(oldName), newName);
        config.set("databaseNames." + databaseName + ".tableNames." + tableName + ".columnNames", list);
        plugin.saveConfig();
    }

    public void setColumnCreationStatement(String databaseName, String tableName, String columnName, String creationStatement){
        config.set("databaseNames." + databaseName + ".tableNames." + tableName + "." + columnName + ".creationStatement", creationStatement);
        plugin.saveConfig();
    }

    */

}
