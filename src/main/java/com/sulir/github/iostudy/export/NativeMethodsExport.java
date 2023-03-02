package com.sulir.github.iostudy.export;

import java.io.IOException;

public class NativeMethodsExport {
    private final String file;

    public NativeMethodsExport(String file) {
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
