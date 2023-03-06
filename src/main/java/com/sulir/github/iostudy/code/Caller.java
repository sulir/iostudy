package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.JavaMethod;
import com.sulir.github.iostudy.shared.NativeMethod;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

public class Caller extends JavaMethod {
    private final SootMethod method;
    private final List<NativeMethod> calledNatives = new ArrayList<>();

    public Caller(SootMethod method) {
        super(-1, method.getDeclaringClass().getName(), getSignature(method));
        this.method = method;
    }

    public SootMethod getSootMethod() {
        return method;
    }

    public void addCalledNative(NativeMethod nativeMethod) {
        calledNatives.add(nativeMethod);
    }

    public void saveToDB() {
        System.out.println(getClassName() + " " + getSignature() + " " + calledNatives.size());
    }
}
