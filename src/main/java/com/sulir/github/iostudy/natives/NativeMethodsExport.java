package com.sulir.github.iostudy.natives;

import com.sulir.github.iostudy.Program;

import java.io.IOException;

@Program(name = "natives")
public class NativeMethodsExport implements Runnable {
    private static final String FILE = "natives.tsv";

    public void run() {
        try {
            UncategorizedNativeMethodList list = UncategorizedNativeMethodList.loadFromJARs();
            list.saveToTSV(FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
