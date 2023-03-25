package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.methods.DynamicCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.methods.NativeMethodList;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.VMDeathRequest;

import java.util.HashMap;
import java.util.Map;

public class MethodTracer {
    private final VirtualMachine vm;
    private final Map<Long, MethodStack> threadStacks = new HashMap<>();
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

        setupRequest(manager.createMethodEntryRequest());
        setupRequest(manager.createMethodExitRequest());
        setupRequest(manager.createThreadDeathRequest());

        VMDeathRequest vmDeathRequest = manager.createVMDeathRequest();
        vmDeathRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        vmDeathRequest.enable();
    }

    private void setupRequest(EventRequest request) {
        request.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        request.enable();
    }

    private void handleEvents() {
        EventQueue queue = vm.eventQueue();
        while (true) {
            try {
                for (Event event : queue.remove()) {
                    if (event instanceof MethodEntryEvent methodEntryEvent)
                        handleMethodEntry(methodEntryEvent);
                    else if (event instanceof MethodExitEvent methodExitEvent)
                        handleMethodExit(methodExitEvent);
                    else if (event instanceof ThreadDeathEvent threadDeathEvent)
                        handleThreadDeath(threadDeathEvent);
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
        long threadId = event.thread().uniqueID();
        threadStacks.putIfAbsent(threadId, new MethodStack());
        threadStacks.get(threadId).push(event.method());

        if (event.method().isNative())
            recordNativeMethod(event);
        else
            recordNonNativeMethod(event);
    }

    private void handleMethodExit(MethodExitEvent event) {
        long threadId = event.thread().uniqueID();
        threadStacks.get(threadId).pop();
    }

    private void handleThreadDeath(ThreadDeathEvent event) {
        threadStacks.remove(event.thread().uniqueID());
    }

    private void recordNativeMethod(MethodEntryEvent event) {
        if (!isInJREModule(event.method()))
            throw new RuntimeException("Non-JRE native method: " + event.method());

        NativeMethod nativeMethod = nativeMethods.getNative(new DynamicCaller(event.method()).getUniqueKey());
        MethodStack stack = threadStacks.get(event.thread().uniqueID());

        for (Method method : stack.toArray(new Method[0]))
            benchmark.registerCalledNative(method, nativeMethod);
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
