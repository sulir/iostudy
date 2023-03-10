package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;
import com.sulir.github.iostudy.export.NativeMethodsExport;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final List<Class<? extends Runnable>> programs = List.of(
            StaticAnalysis.class,
            DynamicAnalysis.class,
            NativeMethodsExport.class);

    public static void main(String[] args) {
        try {
            for (Class<? extends Runnable> program : programs) {
                if (program.getAnnotation(Program.class).name().equals(args[0])) {
                    runProgram(program, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
            printHelp();
        } catch (Exception e) {
            printHelp();
        }
    }

    private static void runProgram(Class<? extends Runnable> program, Object[] args) throws Exception {
        Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        Constructor<?> constructor = program.getConstructor(types);
        Runnable runnable = (Runnable) constructor.newInstance(args);

        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database.disconnect();
    }

    private static void printHelp() {
        System.out.println("Arguments: " + programs.stream()
                .map(p -> p.getAnnotation(Program.class))
                .map(p -> p.name() + " " + p.arguments())
                .collect(Collectors.joining(" | ")));
    }
}