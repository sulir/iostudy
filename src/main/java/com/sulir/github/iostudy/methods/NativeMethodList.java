package com.sulir.github.iostudy.methods;

import com.sulir.github.iostudy.Database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class NativeMethodList {
    private static final String INSERT_SQL = "INSERT INTO natives VALUES (?, ?, ?, ?, ?)";

    private final Map<String, NativeMethod> methods = new LinkedHashMap<>();

    public static NativeMethodList load() {
        try {
            return loadFromDB();
        } catch (SQLException e1) {
            NativeMethodList list = loadFromTSV();
            try {
                list.saveToDB();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
            return list;
        }
    }

    private static NativeMethodList loadFromDB() throws SQLException {
        NativeMethodList list = new NativeMethodList();
        Connection connection = Database.getConnection();
        PreparedStatement selectAll = connection.prepareStatement("SELECT * FROM natives ORDER BY native_id");
        ResultSet results = selectAll.executeQuery();

        while (results.next()) {
            long id = results.getLong("native_id");
            String module = results.getString("module");
            String className = results.getString("class");
            String signature = results.getString("signature");
            String category = results.getString("category");
            NativeMethod method = new NativeMethod(id, module, className, signature, category);
            list.methods.put(method.getUniqueKey(), method);
        }

        if (list.methods.isEmpty())
            throw new SQLException("Native methods not yet imported");
        else
            return list;
    }

    private static NativeMethodList loadFromTSV() {
        NativeMethodList list = new NativeMethodList();
        InputStream stream = NativeMethodList.class.getResourceAsStream("/natives.tsv");
        assert stream != null;
        Scanner scanner = new Scanner(stream);

        while (scanner.hasNextLine()) {
            String[] fields = scanner.nextLine().split("\t");
            NativeMethod method = new NativeMethod(Long.parseLong(fields[0]),
                    fields[1], fields[2], fields[3], fields[4]);
            list.methods.put(method.getUniqueKey(), method);
        }

        return list;
    }

    private NativeMethodList() { }

    public void saveToDB() throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (NativeMethod method : methods.values()) {
                Database.setValues(statement, method.getId(), method.getModule(), method.getClassName(),
                        method.getSignature(), method.getCategory());
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
        }
    }

    public NativeMethod getNative(String key) {
        return methods.get(key);
    }
}
