package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.Program;

import java.nio.file.Path;

@Program(name = "dynamic", arguments = "<path>")
public class DynamicAnalysis implements Runnable {
    private final Path path;

    public DynamicAnalysis(String path) {
        this.path = Path.of(path);
    }

    public void run() {
        System.out.println(path);
    }
}
