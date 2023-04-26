package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.methods.DynamicCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.methods.NativeMethodList;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.VMDeathRequest;

public class MethodTracer {
    private final VirtualMachine vm;
    private final NativeMethodList nativeMethods;
    private final Benchmark benchmark;

    public MethodTracer(VirtualMachine vm, NativeMethodList nativeMethods, Benchmark benchmark) {
        this.vm = vm;
        this.nativeMethods = nativeMethods;
        this.benchmark = benchmark;
    }

    public void trace() {
        setupEventRequests();
        vm.resume();
        handleEvents();
    }

    private void setupEventRequests() {
        EventRequestManager manager = vm.eventRequestManager();

        MethodEntryRequest methodEntryRequest = manager.createMethodEntryRequest();
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        methodEntryRequest.enable();

        VMDeathRequest vmDeathRequest = manager.createVMDeathRequest();
        vmDeathRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        vmDeathRequest.enable();
    }

    private void handleEvents() {
        EventQueue queue = vm.eventQueue();
        while (true) {
            try {
                for (Event event : queue.remove()) {
                    if (event instanceof MethodEntryEvent methodEntryEvent)
                        handleMethodEntry(methodEntryEvent);
                    else if (event instanceof VMDeathEvent)
                        vm.resume();
                }
            } catch (VMDisconnectedException e) {
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMethodEntry(MethodEntryEvent event) {
        if (event.method().isNative()) {
            recordNativeMethod(event);
            event.thread().resume();
        } else {
            event.thread().resume();
            recordNonNativeMethod(event);
        }
    }

    private void recordNativeMethod(MethodEntryEvent event) {
        if (!isInJREModule(event.method()))
            throw new RuntimeException("Non-JRE native method: " + event.method());

        NativeMethod nativeMethod = nativeMethods.getNative(new DynamicCaller(event.method()).getUniqueKey());
        try {
            for (StackFrame frame : event.thread().frames())
                benchmark.registerCalledNative(frame.location().method(), nativeMethod);
        } catch (IncompatibleThreadStateException e) {
            e.printStackTrace();
        }
    }

    private void recordNonNativeMethod(MethodEntryEvent event) {
        Method method = event.method();

        if (isInJREModule(method) || isInJDKModule(method)
                || method.isSynthetic() || method.isBridge()
                || method.isStaticInitializer() || method.isAbstract())
            return;

        benchmark.registerCaller(event.method());
    }

    private boolean isInJREModule(Method method) {
        String module = method.declaringType().module().name();
        return module != null && module.startsWith("java.");
    }

    private boolean isInJDKModule(Method method) {
        String module = method.declaringType().module().name();
        return module != null && module.startsWith("jdk.");
    }
}
