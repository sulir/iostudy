package com.sulir.github.iostudy.shared;

import com.sulir.github.iostudy.Database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class NativeMethodList {
    private static final String SAVE_SQL = "INSERT INTO natives VALUES (?, ?, ?, ?, ?)";

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

    public static NativeMethodList loadFromDB() throws SQLException {
        NativeMethodList list = new NativeMethodList();
        Connection connection = Database.getConnection();
        PreparedStatement selectAll = connection.prepareStatement("SELECT * FROM natives ORDER BY native_id");
        ResultSet results = selectAll.executeQuery();

        while (results.next()) {
            int id = results.getInt("native_id");
            String module = results.getString("module");
            String className = results.getString("class");
            String signature = results.getString("signature");
            String category = results.getString("category");
            NativeMethod method = new NativeMethod(id, module, className, signature, category);
            list.methods.put(method.getKey(), method);
        }

        if (list.methods.isEmpty())
            throw new SQLException("Native methods not yet imported");
        else
            return list;
    }

    public static NativeMethodList loadFromTSV() {
        NativeMethodList list = new NativeMethodList();
        InputStream stream = NativeMethodList.class.getResourceAsStream("/natives.tsv");
        assert stream != null;
        Scanner scanner = new Scanner(stream);

        while (scanner.hasNextLine()) {
            String[] fields = scanner.nextLine().split("\t");
            NativeMethod method = new NativeMethod(Integer.parseInt(fields[0]),
                    fields[1], fields[2], fields[3], fields[4]);
            list.methods.put(method.getKey(), method);
        }

        return list;
    }

    private NativeMethodList() { }

    public void saveToDB() throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {
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

    public String toString() {
        return methods.values().stream().map(NativeMethod::toString).collect(Collectors.joining("\n"));
    }
}
