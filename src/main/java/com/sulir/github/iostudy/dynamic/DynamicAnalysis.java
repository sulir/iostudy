package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.Database;
import com.sulir.github.iostudy.Program;
import com.sulir.github.iostudy.shared.NativeMethodList;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Program(name = "dynamic", arguments = "<benchmark> <port> <dir>")
public class DynamicAnalysis implements Runnable {
    private final String benchmark;
    private final String port;

    public DynamicAnalysis(String benchmark, String port, String directory) {
        this.benchmark = benchmark;
        this.port = port;
        Database.setDirectory(directory);
    }

    public void run() {
        NativeMethodList nativeMethods = NativeMethodList.load();

        try {
            Debugger debugger = new Debugger(port);
            VirtualMachine vm = debugger.attach();

            MethodTracer tracer = new MethodTracer(vm, nativeMethods);
            tracer.trace();

            System.out.println(benchmark);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("dynamic.bin"))) {
                out.writeObject(tracer.getCallers());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        }
    }
}
