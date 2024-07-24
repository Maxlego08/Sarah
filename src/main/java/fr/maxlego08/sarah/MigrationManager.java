package fr.maxlego08.sarah;

import fr.maxlego08.sarah.database.Migration;
import fr.maxlego08.sarah.database.Schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import fr.maxlego08.sarah.logger.Logger;
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

    public static void execute(DatabaseConnection databaseConnection, Logger logger) {

        createMigrationTable(databaseConnection, logger);

        List<String> migrations = getMigrations(databaseConnection, logger);

        MigrationManager.migrations.stream().filter(migration -> !migrations.contains(migration.getClass().getSimpleName())).forEach(Migration::up);

        schemas.forEach(schema -> {
            try {
                schema.execute(databaseConnection, logger);
                insertMigration(databaseConnection, logger, schema.getMigration());
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

        @Column("migration")
        private final String migration;

        public MigrationTable(String migration) {
            this.migration = migration;
        }

        public String getMigration() {
            return migration;
        }
    }
}
