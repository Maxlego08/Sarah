package fr.maxlego08.sarah;

import fr.maxlego08.sarah.database.DatabaseType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Represents a connection to a MySQL database.
 * This class handles establishing and managing the connection to the database.
 */
public class DatabaseConnection {

    private final DatabaseConfiguration databaseConfiguration;
    private Connection connection;
    private File folder;
    private String fileName = "database.db";

    public DatabaseConnection(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the DatabaseConfiguration instance associated with this connection.
     *
     * @return The DatabaseConfiguration instance.
     */
    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    /**
     * Checks if the connection to the database is valid.
     *
     * @return true if the connection is valid, false otherwise.
     */
    public boolean isValid() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
            return false;
        }

        if (!isConnected(connection)) {
            try {
                Connection temp_connection = this.databaseConfiguration.databaseType() == DatabaseType.SQLITE ? this.connectSqlite() : this.connectMySql();

                if (isConnected(temp_connection)) {
                    temp_connection.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given database connection is connected and valid.
     *
     * @param connection The database connection to check.
     * @return true if the connection is valid, false otherwise.
     */
    private boolean isConnected(Connection connection) {
        if (connection == null) {
            return false;
        }

        try {
            return connection.isValid(1);
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * Disconnects from the database.
     */
    public void disconnect() {
        if (isConnected(connection)) {
            try {
                connection.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Establishes a connection to the database.
     */
    public void connect() {
        if (!isConnected(connection)) {
            try {
                connection = this.databaseConfiguration.databaseType() == DatabaseType.SQLITE ? this.connectSqlite() : this.connectMySql();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private Connection connectSqlite() throws Exception {

        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }

        File databaseFile = new File(this.folder, "database.db");
        if (!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    private Connection connectMySql() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("useSSL", "false");
        properties.setProperty("user", databaseConfiguration.user());
        properties.setProperty("password", databaseConfiguration.password());
        return DriverManager.getConnection("jdbc:mysql://" + databaseConfiguration.host() + ":" + databaseConfiguration.port() + "/" + databaseConfiguration.database(), properties);
    }

    /**
     * Gets the connection to the database.
     * If the connection is not established, it attempts to connect first.
     *
     * @return The database connection.
     */
    public Connection getConnection() {
        connect();
        return connection;
    }
}
