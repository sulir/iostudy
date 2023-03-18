package com.sulir.github.iostudy.natives;

import com.sulir.github.iostudy.methods.NativeMethod;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UncategorizedNativeMethodList {
    private final List<NativeMethod> methods = new ArrayList<>();
    private String[] moduleJars;

    public static UncategorizedNativeMethodList loadFromJARs() throws IOException {
        UncategorizedNativeMethodList list = new UncategorizedNativeMethodList();
        list.readAllModules();
        list.sort();
        return list;
    }

    private UncategorizedNativeMethodList() { }

    private void readAllModules() throws IOException {
        File modulesPath = Path.of(System.getProperty("java.home"), "mods").toFile();
        File[] files = modulesPath.listFiles();
        if (files == null)
            throw new IOException("No module JARs in " + modulesPath);

        moduleJars = Arrays.stream(files).map(File::getPath).toArray(String[]::new);

        for (String moduleJar : moduleJars) {
            setupSoot();
            readModule(moduleJar);
        }
    }

    private void setupSoot() {
        G.reset();

        Options.v().set_soot_classpath(String.join(File.pathSeparator, moduleJars));
        Options.v().set_include_all(true);
        Options.v().set_whole_program(true);
    }

    private void readModule(String moduleJar) {
        Options.v().set_process_dir(Collections.singletonList(moduleJar));
        Scene.v().loadNecessaryClasses();

        int fileNameStart = moduleJar.lastIndexOf(File.separator) + 1;
        String moduleName = moduleJar.substring(fileNameStart, moduleJar.lastIndexOf('.'));

        Scene.v().getApplicationClasses().stream()
                .flatMap(clazz -> clazz.getMethods().stream())
                .filter(SootMethod::isNative)
                .map(method -> new NativeMethod(moduleName, method))
                .forEach(methods::add);
    }

    private void sort() {
        methods.sort(null);

        for (int i = 0; i < methods.size(); i++)
            methods.get(i).setId(i + 1);
    }

    public void saveToTSV(String file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (NativeMethod method : methods) {
                writer.printf("%d\t%s\t%s\t%s\t%s\n", method.getId(), method.getModule(),
                        method.getClassName(), method.getSignature(), method.getCategory());
            }
        }
    }
}
