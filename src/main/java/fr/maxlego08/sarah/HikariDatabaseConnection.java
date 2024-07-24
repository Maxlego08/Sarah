package fr.maxlego08.sarah;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariDatabaseConnection extends DatabaseConnection {

    private HikariDataSource dataSource;

    public HikariDatabaseConnection(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
        this.initializeDataSource();
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = "jdbc:mysql://" + databaseConfiguration.getHost() + ":" + databaseConfiguration.getPort() + "/" + databaseConfiguration.getDatabase();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(databaseConfiguration.getUser());
        config.setPassword(databaseConfiguration.getPassword());
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection connectToDatabase() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() {
        // Connection is managed by HikariCP, no need to implement this.
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean isValid() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected boolean isConnected(Connection connection) {
        try {
            return connection != null && connection.isValid(1);
        } catch (SQLException exception) {
            return false;
        }
    }
}
