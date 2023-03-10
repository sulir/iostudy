package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.Program;
import com.sulir.github.iostudy.shared.NativeMethodList;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.sql.SQLException;

@Program(name = "dynamic", arguments = "<benchmark> <port> <dir>")
public class DynamicAnalysis implements Runnable {
    private final Benchmark benchmark;
    private final String port;

    public DynamicAnalysis(String benchmark, String port, String directory) {
        this.benchmark = new Benchmark(benchmark);
        this.port = port;
        Database.setDirectory(directory);
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();

        try {
            Debugger debugger = new Debugger(port);
            VirtualMachine vm = debugger.attach();

            MethodTracer tracer = new MethodTracer(vm, nativeMethods, benchmark);
            tracer.trace();

            new BenchmarkPersistence(benchmark).saveToDB();
        } catch (IllegalConnectorArgumentsException | SQLException e) {
            e.printStackTrace();
        }
    }
}
