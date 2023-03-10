package com.sulir.github.iostudy.methods;

import soot.SootMethod;

import java.util.HashSet;
import java.util.Set;

public class Caller extends JavaMethod {
    private final Set<NativeMethod> calledNatives = new HashSet<>();

    public Caller(String className, String signature) {
        super(className, signature);
    }

    public Caller(SootMethod sootMethod) {
        super(sootMethod);
    }

    public Set<NativeMethod> getCalledNatives() {
        return calledNatives;
    }

    public void addCalledNative(NativeMethod nativeMethod) {
        calledNatives.add(nativeMethod);
    }
}
