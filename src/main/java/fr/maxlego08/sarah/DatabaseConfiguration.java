package fr.maxlego08.sarah;

public class DatabaseConfiguration {

    private final String user;
    private final String password;
    private final int port;
    private final String host;
    private final String database;
    private final boolean debug;

    public DatabaseConfiguration(String user, String password, int port, String host, String database, boolean debug) {
        this.user = user;
        this.password = password;
        this.port = port;
        this.host = host;
        this.database = database;
        this.debug = debug;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public boolean isDebug() {
        return debug;
    }
}
