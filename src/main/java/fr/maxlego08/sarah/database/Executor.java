package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.DatabaseConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public interface Executor {

    int execute(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException;

}
