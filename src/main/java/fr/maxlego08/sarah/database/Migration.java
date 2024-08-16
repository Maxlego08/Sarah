package fr.maxlego08.sarah.database;

import fr.maxlego08.sarah.SchemaBuilder;

import java.util.function.Consumer;

/**
 * Represents a database migration for creating or modifying tables.
 */
public abstract class Migration {

    private boolean alter = false;

    /**
     * Performs the migration to create or modify tables.
     */
    public abstract void up();

    protected void create(String table, Consumer<Schema> consumer) {
        SchemaBuilder.create(this, table, consumer);
    }

    protected void create(String table, Class<?> template) {
        SchemaBuilder.create(this, table, template);
    }

    protected void createOrAlter(String table, Consumer<Schema> consumer) {
        this.create(table, consumer);
        this.alter = true;
    }

    protected void createOrAlter(String table, Class<?> template) {
        this.create(table, template);
        this.alter = true;
    }

    public boolean isAlter() {
        return alter;
    }
}

