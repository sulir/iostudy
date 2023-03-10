package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.methods.DynamicCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.shared.NativeMethodList;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

import java.util.*;

public class MethodTracer {
    private final VirtualMachine vm;
    private final NativeMethodList nativeMethods;
    private final Map<String, DynamicCaller> callers = new HashMap<>();

    public MethodTracer(VirtualMachine vm, NativeMethodList nativeMethods) {
        this.vm = vm;
        this.nativeMethods = nativeMethods;
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
        NativeMethod nativeMethod = nativeMethods.getNative(new DynamicCaller(event.method()).getUniqueKey());
        try {
            for (StackFrame frame : event.thread().frames()) {
                DynamicCaller caller = callers.get(new DynamicCaller(frame.location().method()).getUniqueKey());
                if (caller != null)
                    caller.addCalledNative(nativeMethod);
            }
        } catch (IncompatibleThreadStateException e) {
            e.printStackTrace();
        }
    }

    private void recordNonNativeMethod(MethodEntryEvent event) {
        String module = event.method().declaringType().module().name();
        if (module != null && (module.startsWith("java.") || module.startsWith("jdk.")))
            return;

        DynamicCaller caller = new DynamicCaller(event.method());
        callers.putIfAbsent(caller.getUniqueKey(), caller);
    }

    public List<DynamicCaller> getCallers() {
        return new ArrayList<>(callers.values());
    }
}
