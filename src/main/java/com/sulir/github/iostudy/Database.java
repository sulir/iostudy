package com.sulir.github.iostudy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Database {
    private static final String IMPORT = "INSERT INTO natives VALUES (?, ?, ?, ?)";
    private static String path = "results.db3";
    private static Database instance;
    private Connection connection;

    public static void setDirectory(String directory) {
        path = Paths.get(directory, path).toString();
    }

    public static Database getInstance() {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                boolean existed = new File(path).exists() && new File(path).length() > 0;
                connection = DriverManager.getConnection("jdbc:sqlite:" + path);
                connection.setAutoCommit(false);

                if (!existed) {
                    createSchema();
                    importNativeMethods();
                }
            } catch (SQLException|IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return connection;
    }

    private void createSchema() throws IOException, SQLException {
        try (InputStream stream = Database.class.getResourceAsStream("/schema.sql")) {
            assert stream != null;
            String schema = new String(stream.readAllBytes());

            for (String statement : schema.split(";\\s*")) {
                connection.createStatement().execute(statement);
            }
        }
    }

    private void importNativeMethods() throws SQLException {
        InputStream stream = Database.class.getResourceAsStream("/natives.tsv");
        assert stream != null;

        try (Scanner scanner = new Scanner(stream)) {
            PreparedStatement statement = connection.prepareStatement(IMPORT);

            while (scanner.hasNextLine()) {
                String[] fields = scanner.nextLine().split("\t");
                for (int i = 0; i < fields.length; i++)
                    statement.setString(i + 1, fields[i]);

                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
