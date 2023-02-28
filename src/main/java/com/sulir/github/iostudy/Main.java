package com.sulir.github.iostudy;

import com.sulir.github.iostudy.code.StaticAnalysis;
import com.sulir.github.iostudy.dynamic.DynamicAnalysis;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar iostudy.jar static <params> | dynamic <params>");
        } else {
            if (args[0].equals("static"))
                StaticAnalysis.main(Arrays.copyOfRange(args, 1, args.length));
            else if (args[0].equals("dynamic"))
                DynamicAnalysis.main(Arrays.copyOfRange(args, 1, args.length));
        }
    }
}