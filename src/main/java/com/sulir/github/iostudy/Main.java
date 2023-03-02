package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;
import com.sulir.github.iostudy.export.NativeMethodExport;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Arguments: static <dir> | dynamic <dir> | export <file>");
        } else {
            Database.setDirectory(args[1]);

            switch (args[0]) {
                case "static" -> new StaticAnalysis(args[1]).run();
                case "dynamic" -> new DynamicAnalysis(args[1]).run();
                case "export" -> new NativeMethodExport(args[1]).run();
                default -> System.out.println("Unknown command: " + args[0]);
            }
        }

        Database.getInstance().close();
    }
}