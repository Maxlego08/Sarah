package fr.maxlego08.sarah.requests;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.conditions.ColumnDefinition;
import fr.maxlego08.sarah.database.DatabaseType;
import fr.maxlego08.sarah.database.Executor;
import fr.maxlego08.sarah.database.Schema;
import fr.maxlego08.sarah.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpsertRequest implements Executor {

    private final Schema schema;

    public UpsertRequest(Schema schema) {
        this.schema = schema;
    }

    @Override
    public int execute(DatabaseConnection databaseConnection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException {
        DatabaseType databaseType = databaseConfiguration.getDatabaseType();
        StringBuilder insertQuery = new StringBuilder("INSERT INTO " + this.schema.getTableName() + " (");
        StringBuilder valuesQuery = new StringBuilder("VALUES (");
        StringBuilder onUpdateQuery = new StringBuilder();

        List<Object> values = new ArrayList<>();

        for (int i = 0; i < this.schema.getColumns().size(); i++) {
            ColumnDefinition columnDefinition = this.schema.getColumns().get(i);
            insertQuery.append(i > 0 ? ", " : "").append(columnDefinition.getSafeName());
            valuesQuery.append(i > 0 ? ", " : "").append("?");
            if (i > 0) {
                onUpdateQuery.append(", ");
            }
            if (databaseType == DatabaseType.SQLITE) {
                onUpdateQuery.append(columnDefinition.getSafeName()).append(" = excluded.").append(columnDefinition.getSafeName());
            } else {
                onUpdateQuery.append(columnDefinition.getSafeName()).append(" = ?");
            }
            values.add(columnDefinition.getObject());
        }

        insertQuery.append(") ");
        valuesQuery.append(")");

        String upsertQuery;

        if (databaseType == DatabaseType.SQLITE) {
            StringBuilder onConflictQuery = new StringBuilder(" ON CONFLICT (");
            List<String> primaryKeys = schema.getPrimaryKeys();
            for (int i = 0; i < primaryKeys.size(); i++) {
                onConflictQuery.append(i > 0 ? ", " : "").append(primaryKeys.get(i));
            }
            onConflictQuery.append(") DO UPDATE SET ");
            upsertQuery = insertQuery + valuesQuery.toString() + onConflictQuery + onUpdateQuery;
        } else {
            onUpdateQuery.insert(0, " ON DUPLICATE KEY UPDATE ");
            upsertQuery = insertQuery + valuesQuery.toString() + onUpdateQuery;
        }

        String finalQuery = databaseConfiguration.replacePrefix(upsertQuery);
        if (databaseConfiguration.isDebug()) {
            logger.info("Executing SQL: " + finalQuery);
        }

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(finalQuery)) {

            System.out.println(databaseType);
            if (databaseType == DatabaseType.SQLITE) {
                System.out.println("héhé");
                System.out.println("> " + values.size() + " -> " + values);
                for (int i = 0; i < values.size(); i++) {
                    System.out.println("j'ajouite : " + i + " -- " + values.get(i));
                    preparedStatement.setObject(i, values.get(i));
                }
            } else {
                for (int i = 0; i < values.size(); i++) {
                    preparedStatement.setObject(i + 1, values.get(i));
                }

                for (int i = 0; i < values.size(); i++) {
                    preparedStatement.setObject(i + 1 + values.size(), values.get(i));
                }
            }
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SQLException("Failed to execute upsert: " + exception.getMessage(), exception);
        }

        return -1;
    }
}
