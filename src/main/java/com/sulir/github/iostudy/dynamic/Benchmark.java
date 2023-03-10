package com.sulir.github.iostudy.dynamic;

import com.sulir.github.iostudy.methods.DynamicCaller;
import com.sulir.github.iostudy.methods.NativeMethod;
import com.sun.jdi.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Benchmark {
    private final String name;
    private final Map<String, DynamicCaller> callers = new HashMap<>();

    public Benchmark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void registerCaller(Method callingMethod) {
        DynamicCaller caller = new DynamicCaller(callingMethod);
        if (callers.putIfAbsent(caller.getUniqueKey(), caller) == null)
            caller.setByteCount(callingMethod.bytecodes().length);
    }

    public void registerCalledNative(Method callingMethod, NativeMethod nativeMethod) {
        DynamicCaller caller = callers.get(new DynamicCaller(callingMethod).getUniqueKey());
        if (caller != null)
            caller.addCalledNative(nativeMethod);
    }

    public List<DynamicCaller> getCallers() {
        return new ArrayList<>(callers.values());
    }
}
