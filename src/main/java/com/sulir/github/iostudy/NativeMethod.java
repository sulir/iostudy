package com.sulir.github.iostudy;

public class NativeMethod {
    private final int id;
    private final String clazz;
    private final String method;

    public NativeMethod(int id, String clazz, String method) {
        this.id = id;
        this.clazz = clazz;
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return clazz + "." + method;
    }
}
