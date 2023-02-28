package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.NativeMethodSet;

import java.sql.SQLException;

public class StaticAnalysis {
    public static void main(String[] args) {
        StaticAnalysis analysis = new StaticAnalysis();
        try {
            analysis.run();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Database.close();
        }
    }

    public void run() throws SQLException {
        NativeMethodSet nativeMethods = NativeMethodSet.load();
        System.out.println(nativeMethods);
    }
}
