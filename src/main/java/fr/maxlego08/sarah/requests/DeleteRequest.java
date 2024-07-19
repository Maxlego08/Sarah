package fr.maxlego08.sarah.requests;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.database.Executor;
import fr.maxlego08.sarah.database.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import fr.maxlego08.sarah.logger.Logger;

public class DeleteRequest implements Executor {

    private final Schema schemaBuilder;

    public DeleteRequest(Schema schemaBuilder) {
        this.schemaBuilder = schemaBuilder;
    }

    @Override
    public int execute(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(schemaBuilder.getTableName());
        schemaBuilder.whereConditions(sql);

        String finalQuery = databaseConfiguration.replacePrefix(sql.toString());
        if (databaseConfiguration.isDebug()) {
            logger.info("Executing SQL: " + finalQuery);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(finalQuery)) {
            schemaBuilder.applyWhereConditions(preparedStatement, 1);
            preparedStatement.executeUpdate();
        }
        return -1;
    }
}
