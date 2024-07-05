package fr.maxlego08.sarah;

import fr.maxlego08.sarah.database.Migration;
import fr.maxlego08.sarah.database.Schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MigrationManager {

    private static final List<Schema> schemas = new ArrayList<>();
    private static final List<Migration> migrations = new ArrayList<>();
    private static String migrationTableName = "migrations";

    public static String getMigrationTableName() {
        return migrationTableName;
    }

    public static void setMigrationTableName(String migrationTableName) {
        MigrationManager.migrationTableName = migrationTableName;
    }

    public static void registerSchema(Schema schema) {
        schemas.add(schema);
    }

    public static void execute(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) {

        createMigrationTable(connection, databaseConfiguration, logger);

        List<String> migrations = getMigrations(connection, databaseConfiguration, logger);

        MigrationManager.migrations.stream().filter(migration -> !migrations.contains(migration.getClass().getSimpleName())).forEach(Migration::up);

        schemas.forEach(schema -> {
            try {
                schema.execute(connection, databaseConfiguration, logger);
                insertMigration(connection, databaseConfiguration, logger, schema.getMigration());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

    }

    public static List<Migration> getMigrations() {
        return migrations;
    }

    private static void createMigrationTable(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) {
        Schema schema = SchemaBuilder.create(null, migrationTableName, sc -> {
            sc.text("migration");
            sc.createdAt();
        });
        try {
            schema.execute(connection, databaseConfiguration, logger);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static List<String> getMigrations(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) {
        Schema schema = SchemaBuilder.select(migrationTableName);
        try {
            return schema.executeSelect(MigrationTable.class, connection, databaseConfiguration, logger).stream().map(MigrationTable::getMigration).collect(Collectors.toList());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void insertMigration(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger, Migration migration) {
        try {
            SchemaBuilder.insert(migrationTableName, schema -> {
                schema.string("migration", migration.getClass().getSimpleName());
            }).execute(connection, databaseConfiguration, logger);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void registerMigration(Migration migration) {
        migrations.add(migration);
    }

    public static class MigrationTable {
        private final String migration;

        public MigrationTable(String migration) {
            this.migration = migration;
        }

        public String getMigration() {
            return migration;
        }
    }
}
