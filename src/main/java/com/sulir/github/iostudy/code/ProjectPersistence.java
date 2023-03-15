package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.methods.Caller;
import com.sulir.github.iostudy.methods.StaticCaller;
import com.sulir.github.iostudy.methods.NativeMethod;

import java.sql.*;

public class ProjectPersistence {
    private static final String REPLACE_PROJECTS = "REPLACE INTO projects " +
            "(name, classes, methods) VALUES (?, ?, ?)";
    private static final String INSERT_CALLER = "INSERT INTO callers " +
            "(project_id, class, signature, units, empty, test) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_REFERENCE = "INSERT INTO %scallers_natives " +
            "(%scaller_id, native_id) VALUES (?, ?)";

    private final Project project;
    private long projectId;

    public ProjectPersistence(Project project) {
        this.project = project;
    }

    public void saveToDB() throws SQLException {
        projectId = saveProject();

        for (StaticCaller caller : project.getCallGraph().getCallers()) {
            saveCaller(caller);
        }

        Database.getConnection().commit();
    }

    private long saveProject() throws SQLException {
        long classCount = project.getSourceClasses().count();
        long methodCount = project.getSourceMethods().count();
        Connection connection = Database.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(REPLACE_PROJECTS,
                Statement.RETURN_GENERATED_KEYS)) {
            Database.setValues(statement, project.getName(), classCount, methodCount);
            statement.execute();

            ResultSet newId = statement.getGeneratedKeys();
            newId.next();
            return newId.getLong(1);
        }
    }

    private void saveCaller(StaticCaller caller) throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement insert = connection.prepareStatement(INSERT_CALLER, Statement.RETURN_GENERATED_KEYS)) {
            Database.setValues(insert, projectId, caller.getClassName(), caller.getSignature(),
                    caller.getUnitCount(), caller.isEmpty(), caller.isTest());
            insert.execute();

            ResultSet newId = insert.getGeneratedKeys();
            newId.next();
            saveCalledNatives(caller, newId.getLong(1), "");
        }
    }

    public static void saveCalledNatives(Caller caller, long callerId, String prefix) throws SQLException {
        Connection connection = Database.getConnection();

        String sql = String.format(INSERT_REFERENCE, prefix, prefix);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (NativeMethod nativeMethod : caller.getCalledNatives()) {
                Database.setValues(statement, callerId, nativeMethod.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
