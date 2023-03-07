package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;

public class StaticAnalysis {
    private static final Logger log = LoggerFactory.getLogger(StaticAnalysis.class);

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
        project.setCallGraph(callGraph);
        callGraph.printCallTrees();

        long start = System.currentTimeMillis();
        callGraph.findNativeCallers();
        log.debug("Native callers found in {} seconds.",
                String.format("%.1f", (System.currentTimeMillis() - start) / 1000d));
        try {
            new ProjectPersistence(project).saveToDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
