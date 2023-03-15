package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.methods.DynamicCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.methods.NativeMethodList;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

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
        MethodEntryRequest request = vm.eventRequestManager().createMethodEntryRequest();
        request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        request.enable();
    }

    private void handleEvents() {
        EventQueue queue = vm.eventQueue();
        while (true) {
            try {
                for (Event event : queue.remove()) {
                    if (event instanceof MethodEntryEvent)
                        handleMethodEntry((MethodEntryEvent) event);
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
