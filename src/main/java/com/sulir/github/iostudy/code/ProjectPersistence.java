package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.methods.StaticCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.shared.Project;

import java.sql.*;

public class ProjectPersistence {
    private static final String REPLACE_PROJECTS = "REPLACE INTO projects " +
            "(name, classes, methods) VALUES (?, ?, ?)";
    private static final String INSERT_CALLER = "INSERT INTO callers " +
            "(project_id, class, signature, units, empty) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_REFERENCE = "INSERT INTO callers_natives " +
            "(caller_id, native_id) VALUES (?, ?)";

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
                    caller.getUnitCount(), caller.isEmpty());
            insert.execute();

            ResultSet newId = insert.getGeneratedKeys();
            newId.next();
            saveCalledNatives(caller, newId.getLong(1));
        }
    }

    private void saveCalledNatives(StaticCaller caller, long callerId) throws SQLException {
        Connection connection = Database.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(INSERT_REFERENCE)) {
            for (NativeMethod nativeMethod : caller.getCalledNatives()) {
                Database.setValues(statement, callerId, nativeMethod.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
