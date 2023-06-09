package com.sulir.github.iostudy;

import org.sqlite.SQLiteConnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static final String PATH = "results.db3";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + PATH + "?foreign_keys=on");
                connection.setAutoCommit(false);
                ((SQLiteConnection) connection).setBusyTimeout(30_000);
                createSchema();
            } catch (SQLException|IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return connection;
    }

    private static void createSchema() throws IOException, SQLException {
        try (InputStream stream = Database.class.getResourceAsStream("/schema.sql")) {
            assert stream != null;
            String schema = new String(stream.readAllBytes());

            for (String statement : schema.split(";\\s*")) {
                connection.createStatement().execute(statement);
            }
            connection.commit();
        }
    }

    public static void setValues(PreparedStatement statement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++)
            statement.setObject(i + 1, values[i]);
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
