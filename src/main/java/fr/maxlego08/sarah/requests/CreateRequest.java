package fr.maxlego08.sarah.requests;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.conditions.ColumnDefinition;
import fr.maxlego08.sarah.database.Executor;
import fr.maxlego08.sarah.database.Schema;
import fr.maxlego08.sarah.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateRequest implements Executor {

    private final Schema schema;

    public CreateRequest(Schema schema) {
        this.schema = schema;
    }

    @Override
    public int execute(DatabaseConnection databaseConnection, DatabaseConfiguration databaseConfiguration, Logger logger) {

        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        createTableSQL.append(this.schema.getTableName()).append(" (");

        List<String> columnSQLs = new ArrayList<>();
        for (ColumnDefinition column : this.schema.getColumns()) {
            columnSQLs.add(column.build(databaseConfiguration));
        }
        createTableSQL.append(String.join(", ", columnSQLs));

        if (!this.schema.getPrimaryKeys().isEmpty()) {
            createTableSQL.append(", PRIMARY KEY (").append(String.join(", ", this.schema.getPrimaryKeys())).append(")");
        }

        for (String fk : this.schema.getForeignKeys()) {
            createTableSQL.append(", ").append(fk);
        }

        createTableSQL.append(")");

        String finalQuery = databaseConfiguration.replacePrefix(createTableSQL.toString());
        if (databaseConfiguration.isDebug()) {
            logger.info("Executing SQL: " + finalQuery);
        }

        try (Connection connection = databaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(finalQuery)) {
            preparedStatement.execute();
            return preparedStatement.getUpdateCount();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return -1;
        }
    }
}
