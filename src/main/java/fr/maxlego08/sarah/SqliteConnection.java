package fr.maxlego08.sarah;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteConnection extends DatabaseConnection {

    private final File folder;
    private String fileName = "database.db";

    public SqliteConnection(DatabaseConfiguration databaseConfiguration, File folder) {
        super(databaseConfiguration);
        this.folder = folder;
    }

    @Override
    public Connection connectToDatabase() throws Exception {
        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }

        File databaseFile = new File(this.folder, this.fileName);
        if (!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    public File getFolder() {
        return folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Connection getConnection() {
        try {
            return connectToDatabase();
        } catch (Exception exception) {
            connect();
            return connection;
        }
    }
}
