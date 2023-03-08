package com.sulir.github.iostudy.export;

import com.sulir.github.iostudy.Program;

import java.io.IOException;
import java.nio.file.Path;

@Program(name = "export", arguments = "<file>")
public class NativeMethodsExport implements Runnable {
    private final Path file;

    public NativeMethodsExport(String file) {
        this.file = Path.of(file);
    }

    public void run() {
        try {
            UncategorizedNativeMethodList list = UncategorizedNativeMethodList.loadFromJARs();
            list.saveToTSV(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
