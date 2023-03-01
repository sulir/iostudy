package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar iostudy.jar static <path> | dynamic <path>");
        } else {
            Database.setPath(args[1]);

            if (args[0].equals("static")) {
                StaticAnalysis analysis = new StaticAnalysis(args[1]);
                analysis.run();
            } else if (args[0].equals("dynamic")) {
                DynamicAnalysis analysis = new DynamicAnalysis();
                analysis.run();
            }
        }

        Database.getInstance().close();
    }
}