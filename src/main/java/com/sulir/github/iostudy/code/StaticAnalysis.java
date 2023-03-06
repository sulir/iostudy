package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethodList;

import java.nio.file.Path;

public class StaticAnalysis {
    private final Path path;

    public StaticAnalysis(Path path) {
        this.path = path;
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();
        Project project = new Project(path);
        project.setup();
        project.saveToDB();

        ProjectCallGraph callGraph = new ProjectCallGraph(project, nativeMethods);
        callGraph.construct();
        callGraph.findNativeCallers();
        callGraph.saveToDB();
    }
}
