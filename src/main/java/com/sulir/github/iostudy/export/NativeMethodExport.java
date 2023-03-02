package com.sulir.github.iostudy.export;

import com.sulir.github.iostudy.NativeMethod;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class NativeMethodExport {
    private final String fileName;
    private PrintWriter writer;
    private int id;
    private String[] moduleJars;

    public NativeMethodExport(String fileName) {
        this.fileName = fileName;
    }

    public void run() {
        try {
            writer = new PrintWriter(fileName);
            exportAllModules();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private void exportAllModules() throws IOException {
        File modulesPath = Paths.get(System.getProperty("java.home"), "mods").toFile();
        File[] files = modulesPath.listFiles();
        if (files == null)
            throw new IOException("Cannot read " + modulesPath);

        moduleJars = Arrays.stream(files)
                .map(File::getPath)
                .sorted()
                .toArray(String[]::new);

        id = 1;
        for (String moduleJar : moduleJars) {
            setupSoot();
            exportModule(moduleJar);
        }
    }

    private void setupSoot() {
        G.reset();

        Options.v().set_soot_classpath(String.join(File.pathSeparator, moduleJars));
        Options.v().set_include_all(true);
        Options.v().set_whole_program(true);
    }

    private void exportModule(String moduleJar) {
        String moduleName = moduleJar.substring(moduleJar.lastIndexOf(File.separator) + 1, moduleJar.lastIndexOf('.'));

        Options.v().set_process_dir(Collections.singletonList(moduleJar));
        Scene.v().loadNecessaryClasses();

        Scene.v().getApplicationClasses().stream()
                .flatMap(clazz -> clazz.getMethods().stream())
                .filter(SootMethod::isNative)
                .map(method -> new NativeMethod(moduleName, method))
                .sorted()
                .forEachOrdered(m -> writer.println(id++ + "\t" + formatMethod(m)));
    }

    private String formatMethod(NativeMethod method) {
        return method.getModuleName() + "\t" + method.getClassName() + "\t" + method.getMethodSignature();
    }
}
