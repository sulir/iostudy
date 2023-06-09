package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.Program;
import com.sulir.github.iostudy.methods.NativeMethodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;

@Program(name = "static", arguments = "<project>")
public class StaticAnalysis implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(StaticAnalysis.class);
    private static final String CORPUS_DIR = "corpus";

    private final Path path;

    public StaticAnalysis(String project) {
        this.path = Path.of(CORPUS_DIR, project);
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();

        Project project = new Project(path);
        project.setup();
        project.logPhantomClasses();

        ProjectCallGraph callGraph = new ProjectCallGraph(project, nativeMethods);
        callGraph.construct();
        project.setCallGraph(callGraph);

        long start = System.currentTimeMillis();
        callGraph.findNativeCalls();
        log.debug("Native callers found in {} seconds.",
                String.format("%.1f", (System.currentTimeMillis() - start) / 1000d));

        try {
            new ProjectPersistence(project).saveToDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
