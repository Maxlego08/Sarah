package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.logger.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public interface Executor {

    int execute(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException;

}
