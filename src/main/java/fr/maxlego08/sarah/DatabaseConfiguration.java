package fr.maxlego08.sarah;

import fr.maxlego08.sarah.database.DatabaseType;

/**
 * Represents the configuration for connecting to a database.
 * This record encapsulates the database connection details, including the prefix, username, password, port, host,
 * database name, and debug mode.
 */
public record DatabaseConfiguration(String tablePrefix, String user, String password, int port, String host,
                                    String database, boolean debug, DatabaseType databaseType) {

    public static DatabaseConfiguration create(String user, String password, int port, String host, String database, DatabaseType databaseType) {
        return new DatabaseConfiguration(null, user, password, port, host, database, false, databaseType);
    }

    public static DatabaseConfiguration create(String user, String password, int port, String host, String database) {
        return new DatabaseConfiguration(null, user, password, port, host, database, false, DatabaseType.MYSQL);
    }

    public static DatabaseConfiguration create(String user, String password, int port, String host, String database, boolean debug) {
        return new DatabaseConfiguration(null, user, password, port, host, database, debug, DatabaseType.MYSQL);
    }

    public static DatabaseConfiguration create(String user, String password, String host, String database, DatabaseType databaseType) {
        return new DatabaseConfiguration(null, user, password, 3306, host, database, false, databaseType);
    }

    public static DatabaseConfiguration create(String user, String password, int port, String host, String database, boolean debug, DatabaseType databaseType) {
        return new DatabaseConfiguration(null, user, password, port, host, database, debug, databaseType);
    }

    public static DatabaseConfiguration sqlite(boolean debug) {
        return new DatabaseConfiguration(null, null, null, 0, null, null, debug, DatabaseType.SQLITE);
    }

    /**
     * Replaces the placeholder %prefix% in the given table name with the actual prefix.
     *
     * @param tableName The table name possibly containing the %prefix% placeholder.
     * @return The table name with the %prefix% placeholder replaced by the actual prefix.
     */
    public String replacePrefix(String tableName) {
        return this.tablePrefix == null ? tableName : tableName.replaceAll("%prefix%", this.tablePrefix);
    }

}
