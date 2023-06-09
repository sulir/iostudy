package com.sulir.github.iostudy.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Project {
    private static final Logger log = LoggerFactory.getLogger(Project.class);

    private final String name;
    private final List<String> jars;
    private final List<String> dependencies;
    private ProjectCallGraph callGraph;

    public Project(Path path) {
        this.name = path.getFileName().toString().replace("__", "/");
        this.jars = listJARs(path.resolve("jars"));
        this.dependencies = listJARs(path.resolve("deps"));
    }

    private List<String> listJARs(Path directory) {
        File[] files = directory.toFile().listFiles((dir, name) -> name.endsWith(".jar"));

        if (files == null)
            return List.of();
        else
            return Arrays.stream(files).map(File::getPath).sorted().toList();
    }

    public void setup() {
        G.reset();

        Options.v().set_process_dir(jars);
        List<String> jreClasspath = listJARs(Path.of(System.getProperty("java.home"), "mods"));
        String classpath = Stream.concat(dependencies.stream(), jreClasspath.stream())
                .collect(Collectors.joining(File.pathSeparator));
        Options.v().set_soot_classpath(classpath);

        Options.v().set_output_dir(Path.of(System.getProperty("java.io.tmpdir"), "soot").toString());
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_allow_phantom_elms(true);

        Scene.v().loadNecessaryClasses();
    }

    public String getName() {
        return name;
    }

    public Stream<SootClass> getSourceClasses() {
        return Scene.v().getApplicationClasses().stream()
                .filter(c -> !c.isPhantom()
                        && !Modifier.isSynthetic(c.getModifiers())
                        && !c.getName().contains("$lambda_"));
    }

    public Stream<SootMethod> getSourceMethods() {
        return getSourceClasses()
                .flatMap(c -> c.getMethods().stream())
                .filter(m -> !Modifier.isSynthetic(m.getModifiers())
                        && !m.isStaticInitializer()
                        && m.isConcrete());
    }

    public ProjectCallGraph getCallGraph() {
        return callGraph;
    }

    public void setCallGraph(ProjectCallGraph callGraph) {
        this.callGraph = callGraph;
    }

    public void logPhantomClasses() {
        for (SootClass c : Scene.v().getPhantomClasses())
            log.warn("Phantom class: {}.", c.getName());
    }
}
