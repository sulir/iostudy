package com.sulir.github.iostudy.dynamic;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.io.IOException;
import java.util.Map;

public class Debugger {
    private final String port;

    public Debugger(String port) {
        this.port = port;
    }

    public VirtualMachine attach() throws IllegalConnectorArgumentsException {
        AttachingConnector connector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(c -> c.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue("localhost");
        arguments.get("port").setValue(port);

        while (true) {
            try {
                return connector.attach(arguments);
            } catch (IOException e) {
                waitSecond();
            }
        }
    }

    private void waitSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
