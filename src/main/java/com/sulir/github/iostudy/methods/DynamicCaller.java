package com.sulir.github.iostudy.methods;

import com.sun.jdi.Method;

public class DynamicCaller extends Caller {
    public DynamicCaller(Method method) {
        super(method.declaringType().name(),
                method.name() + "(" + String.join(", ", method.argumentTypeNames()) + ")");
    }
}
