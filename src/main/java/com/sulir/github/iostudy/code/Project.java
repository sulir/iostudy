package com.sulir.github.iostudy.code;

import soot.G;
import soot.Scene;
import soot.options.Options;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Project {
    private static final String RT_JAR = Path.of(System.getProperty("java.home"), "lib", "rt.jar").toString();

    private final String name;
    private final List<String> jars;
    private final List<String> dependencies;

    public Project(Path path) {
        this.name = path.getFileName().toString().replace("__", "/");
        this.jars = listFiles(path.resolve("jars"));
        this.dependencies = listFiles(path.resolve("deps"));
    }

    public void setup() {
        G.reset();
        Options.v().set_process_dir(jars);

        String classpath = Stream.concat(Stream.of(RT_JAR), dependencies.stream())
                .collect(Collectors.joining(File.pathSeparator));
        Options.v().set_soot_classpath(classpath);

        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        Scene.v().loadNecessaryClasses();
    }

    public void saveToDB() {

    }

    private List<String> listFiles(Path directory) {
        File[] files = directory.toFile().listFiles();

        if (files == null)
            return List.of();
        else
            return Arrays.stream(files).map(File::getPath).sorted().toList();
    }
}
