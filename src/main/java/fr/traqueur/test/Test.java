package fr.traqueur.test;

import fr.maxlego08.sarah.*;
import fr.maxlego08.sarah.database.DatabaseType;
import fr.maxlego08.sarah.database.Migration;

import java.io.File;
import java.sql.SQLException;

public class Test {

    public static void main(String[] args) throws SQLException {

        DatabaseConnection connection = new MySqlConnection(new DatabaseConfiguration("prefix_", "root", "", 3308, "localhost", "test", true, DatabaseType.MYSQL));
        connection.connect();

        SchemaBuilder.alter(null, "test", schema -> {
            schema.autoIncrement("id");
            schema.string("name", 255);
        }).execute(connection, System.out::println);
    }
}
