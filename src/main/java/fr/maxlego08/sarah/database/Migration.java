package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.SchemaBuilder;

import java.util.function.Consumer;

/**
 * Represents a database migration for creating or modifying tables.
 */
public abstract class Migration {


    /**
     * Performs the migration to create or modify tables.
     */
    public abstract void up();

    protected void create(String table, Consumer<Schema> consumer) {
        SchemaBuilder.create(this, table, consumer);
    }
}

