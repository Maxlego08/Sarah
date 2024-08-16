package fr.maxlego08.sarah;

import fr.maxlego08.sarah.conditions.ColumnDefinition;
import fr.maxlego08.sarah.database.Migration;
import fr.maxlego08.sarah.database.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.maxlego08.sarah.database.SchemaType;
import fr.maxlego08.sarah.logger.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class MigrationManager {

    private static final Map<String, Schema> schemas = new HashMap<>();
    private static final List<Migration> migrations = new ArrayList<>();
    private static String migrationTableName = "migrations";

    public static String getMigrationTableName() {
        return migrationTableName;
    }

    public static void setMigrationTableName(String migrationTableName) {
        MigrationManager.migrationTableName = migrationTableName;
    }

    public static void registerSchema(Schema schema) {
        schemas.put(schema.getTableName(), schema);
    }

    public static void execute(DatabaseConnection databaseConnection, Logger logger) {

        createMigrationTable(databaseConnection, logger);

        List<String> migrationsFromDatabase = getMigrations(databaseConnection, logger);

        MigrationManager.migrations.forEach(Migration::up);

        schemas.forEach((table, schema) -> {
            try {
                if(!migrationsFromDatabase.contains(schema.getMigration().getClass().getSimpleName())) {
                    Result result = schema.execute(databaseConnection, logger);
                    if(result == Result.SUCCESS) {
                        insertMigration(databaseConnection, logger, schema.getMigration());
                    }
                } else {
                    if(!schema.getMigration().isAlter()) {
                        return;
                    }

                    List<ColumnDefinition> mustBeAdd = new ArrayList<>();

                    String tableName = schema.getTableName();
                    tableName = tableName.replace("%prefix%", databaseConnection.getDatabaseConfiguration().getTablePrefix());
                    for (ColumnDefinition column : schema.getColumns()) {
                        Schema columnExistQuery = SchemaBuilder.selectCount("information_schema.COLUMNS")
                                .where("TABLE_NAME", tableName)
                                .where("TABLE_SCHEMA", databaseConnection.getDatabaseConfiguration().getDatabase());
                        long result = columnExistQuery.where("COLUMN_NAME", column.getName()).executeSelectCount(databaseConnection, logger);
                        if(result == 0) {
                            mustBeAdd.add(column);
                        }
                    }

                    if (mustBeAdd.isEmpty()) {
                        return;
                    }

                    SchemaBuilder.alter(null, tableName, (schemaAlter) -> {
                        for (ColumnDefinition column : mustBeAdd) {
                            schemaAlter.addColumn(column).nullable();
                        }
                    }).execute(databaseConnection, logger);
                    insertMigration(databaseConnection, logger, schema.getMigration());
                }

            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

    }

    public static List<Migration> getMigrations() {
        return migrations;
    }

    private static void createMigrationTable(DatabaseConnection databaseConnection, Logger logger) {
        Schema schema = SchemaBuilder.create(null, migrationTableName, sc -> {
            sc.text("migration");
            sc.createdAt();
        });
        try {
            schema.execute(databaseConnection, logger);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static List<String> getMigrations(DatabaseConnection databaseConnection, Logger logger) {
        Schema schema = SchemaBuilder.select(migrationTableName);
        try {
            return schema.executeSelect(MigrationTable.class, databaseConnection, logger).stream().map(MigrationTable::getMigration).collect(Collectors.toList());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void insertMigration(DatabaseConnection databaseConnection, Logger logger, Migration migration) {
        try {
            SchemaBuilder.insert(migrationTableName, schema -> {
                schema.string("migration", migration.getClass().getSimpleName());
            }).execute(databaseConnection, logger);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void registerMigration(Migration migration) {
        migrations.add(migration);
    }

    public static class MigrationTable {

        @Column(value = "migration")
        private final String migration;

        public MigrationTable(String migration) {
            this.migration = migration;
        }

        public String getMigration() {
            return migration;
        }
    }
}
