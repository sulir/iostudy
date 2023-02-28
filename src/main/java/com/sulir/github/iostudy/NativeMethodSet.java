package com.sulir.github.iostudy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NativeMethodSet {
    private final Set<NativeMethod> methods = new HashSet<>();

    public static NativeMethodSet load() throws SQLException {
        NativeMethodSet methodSet = new NativeMethodSet();

        Connection connection = Database.getConnection();
        PreparedStatement selectAll = connection.prepareStatement("SELECT * FROM natives");
        ResultSet results = selectAll.executeQuery();
        while (results.next()) {
            int id = results.getInt("native_id");
            String clazz = results.getString("class");
            String method = results.getString("method");
            methodSet.add(new NativeMethod(id, clazz, method));
        }

        return methodSet;
    }

    public void add(NativeMethod method) {
        methods.add(method);
    }

    public String toString() {
        return methods.stream().map(NativeMethod::toString).collect(Collectors.joining("\n"));
    }
}
