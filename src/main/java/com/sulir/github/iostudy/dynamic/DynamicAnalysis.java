package com.sulir.github.iostudy.dynamic;

import java.nio.file.Path;

public class DynamicAnalysis {
    private final Path path;

    public DynamicAnalysis(Path path) {
        this.path = path;
    }

    public void run() {
        System.out.println(path);
    }
}
