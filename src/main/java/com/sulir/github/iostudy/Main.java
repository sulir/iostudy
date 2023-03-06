package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;
import com.sulir.github.iostudy.export.NativeMethodsExport;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0)
            runProgram(args);
        else
            printHelp();

        Database.disconnect();
    }

    private static void runProgram(String[] args) {
        switch (args[0]) {
            case "static" -> {
                if (args.length == 3) {
                    Database.setDirectory(Path.of(args[1]));
                    new StaticAnalysis(Path.of(args[1]), args[2]).run();
                } else {
                    printHelp();
                }
            }
            case "dynamic" -> {
                if (args.length == 2)
                    new DynamicAnalysis(Path.of(args[1])).run();
                else
                    printHelp();
            }
            case "export" -> {
                if (args.length == 2)
                    new NativeMethodsExport(Path.of(args[1])).run();
                else
                    printHelp();
            }
            default -> printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Arguments: static <dir> <project> | dynamic <dir> | export <file>");
    }
}