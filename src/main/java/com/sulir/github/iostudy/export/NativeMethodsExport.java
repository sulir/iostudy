package com.sulir.github.iostudy.export;

import java.io.IOException;
import java.nio.file.Path;

public class NativeMethodsExport {
    private final Path file;

    public NativeMethodsExport(Path file) {
        this.file = file;
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
