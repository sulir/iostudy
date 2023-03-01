package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.NativeMethodSet;

import java.sql.SQLException;

public class StaticAnalysis {
    private final String path;

    public StaticAnalysis(String path) {
        this.path = path;
    }

    public void run() {
        try {
            NativeMethodSet nativeMethods = NativeMethodSet.load();
            System.out.println(nativeMethods);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
