package com.sulir.github.iostudy.info;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.Program;
import com.sulir.github.iostudy.methods.StaticCaller;
import com.sulir.github.iostudy.shared.Project;
import com.sulir.github.iostudy.shared.TestPredicate;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Program(name = "info", arguments = "<corpus> <project>")
public class AdditionalInfo implements Runnable {
    private static final TestPredicate testPredicate = new TestPredicate();
    private static final String ALTER_TEST = "ALTER TABLE callers " +
            "ADD test BOOLEAN NOT NULL DEFAULT FALSE";
    private static final String SELECT_PROJECT = "SELECT project_id FROM projects WHERE name = ?";
    private static final String UPDATE_TEST = "UPDATE callers " +
            "SET test = TRUE WHERE project_id = ? AND class = ? AND signature = ?";

    private final Path path;

    public AdditionalInfo(String corpusDir, String project) {
        this.path = Path.of(corpusDir, project);
        Database.setDirectory(corpusDir);
    }

    @Override
    public void run() {
        Project project = new Project(path);
        project.setup();

        try {
            createColumnIfNotPresent();
            findTests(project, getProjectId(project));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createColumnIfNotPresent() {
        try (PreparedStatement statement = Database.getConnection().prepareStatement(ALTER_TEST)) {
            statement.execute();
        } catch (SQLException e) {
            // already present
        }
    }

    private long getProjectId(Project project) throws SQLException {
        try (PreparedStatement statement = Database.getConnection().prepareStatement(SELECT_PROJECT)) {
            Database.setValues(statement, project.getName());
            ResultSet result = statement.executeQuery();
            return result.next() ? result.getLong(1) : -1;
        }
    }

    private void findTests(Project project, long projectId) throws SQLException {
        List<StaticCaller> tests = project.getSourceMethods()
                .filter(testPredicate)
                .map(StaticCaller::new)
                .toList();

        for (StaticCaller test : tests) {
            try (PreparedStatement statement = Database.getConnection().prepareStatement(UPDATE_TEST)) {
                Database.setValues(statement, projectId, test.getClassName(), test.getSignature());
                statement.execute();
            }
        }

        Database.getConnection().commit();
    }
}
