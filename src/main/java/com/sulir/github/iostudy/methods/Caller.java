package com.sulir.github.iostudy.methods;

import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

public class Caller extends JavaMethod {
    private final List<NativeMethod> calledNatives = new ArrayList<>();

    public Caller(String className, String signature) {
        super(className, signature);
    }

    public Caller(SootMethod sootMethod) {
        super(sootMethod);
    }

    public List<NativeMethod> getCalledNatives() {
        return calledNatives;
    }

    public void addCalledNative(NativeMethod nativeMethod) {
        calledNatives.add(nativeMethod);
    }
}
