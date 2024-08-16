package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.Result;
import fr.maxlego08.sarah.logger.Logger;

import java.sql.SQLException;

public interface Executor {

    Result execute(DatabaseConnection databaseConnection, DatabaseConfiguration databaseConfiguration, Logger logger);

}
