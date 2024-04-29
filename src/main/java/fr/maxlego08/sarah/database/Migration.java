package fr.maxlego08.sarah.database;

/**
 * Represents a database migration for creating or modifying tables.
 */
public abstract class Migration {


    /**
     * Performs the migration to create or modify tables.
     */
    public abstract void up();
}

