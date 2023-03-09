package com.sulir.github.iostudy.shared;

import soot.SootMethod;
import soot.jimple.ReturnVoidStmt;

import java.util.ArrayList;
import java.util.List;

public class Caller extends JavaMethod {
    private final SootMethod method;
    private final List<NativeMethod> calledNatives = new ArrayList<>();

    public Caller(SootMethod method) {
        super(method.getDeclaringClass().getName(), getSignature(method));
        this.method = method;
    }

    public SootMethod getSootMethod() {
        return method;
    }

    public List<NativeMethod> getCalledNatives() {
        return calledNatives;
    }

    public void addCalledNative(NativeMethod nativeMethod) {
        calledNatives.add(nativeMethod);
    }

    public int getUnitCount() {
        return method.retrieveActiveBody().getUnits().size();
    }

    public boolean isEmpty() {
        return getUnitCount() == 1 && method.retrieveActiveBody().getUnits().getFirst() instanceof ReturnVoidStmt;
    }
}
