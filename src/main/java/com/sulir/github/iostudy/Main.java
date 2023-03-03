package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;
import com.sulir.github.iostudy.export.NativeMethodsExport;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Arguments: static <dir> | dynamic <dir> | export <file>");
        } else {
            Path path = Path.of(args[1]);
            Database.setDirectory(path);

            switch (args[0]) {
                case "static" -> new StaticAnalysis(path).run();
                case "dynamic" -> new DynamicAnalysis(path).run();
                case "export" -> new NativeMethodsExport(path).run();
                default -> System.out.println("Unknown command: " + args[0]);
            }
        }

        Database.disconnect();
    }
}