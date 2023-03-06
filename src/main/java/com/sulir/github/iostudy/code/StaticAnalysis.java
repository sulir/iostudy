package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethodList;

import java.nio.file.Path;
import java.sql.SQLException;

public class StaticAnalysis {
    private final Path path;

    public StaticAnalysis(Path path, String project) {
        this.path = path.resolve(project);
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();

        Project project = new Project(path);
        project.setup();

        ProjectCallGraph callGraph = new ProjectCallGraph(project, nativeMethods);
        callGraph.construct();
        callGraph.findNativeCallers();
        project.setCallGraph(callGraph);

        try {
            new ProjectPersistence(project).saveToDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
