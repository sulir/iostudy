package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.objects.NativeMethodList;

public class StaticAnalysis {
    private final String path;

    public StaticAnalysis(String path) {
        this.path = path;
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();
        System.out.println(nativeMethods);
    }
}
