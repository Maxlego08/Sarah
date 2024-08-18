package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.logger.Logger;

public interface Executor {

    int execute(DatabaseConnection databaseConnection, DatabaseConfiguration databaseConfiguration, Logger logger);

}
