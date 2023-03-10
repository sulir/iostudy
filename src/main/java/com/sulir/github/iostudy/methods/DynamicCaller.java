package com.sulir.github.iostudy.methods;

import com.sun.jdi.Method;

public class DynamicCaller extends Caller {
    private int byteCount;

    public DynamicCaller(Method method) {
        super(method.declaringType().name(),
                method.name() + "(" + String.join(", ", method.argumentTypeNames()) + ")");
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }
}
