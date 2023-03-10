package com.sulir.github.iostudy.methods;

import soot.SootMethod;
import soot.Type;

import java.io.Serializable;
import java.util.stream.Collectors;

public abstract class JavaMethod implements Serializable {
    private final String className;
    private final String signature;

    public JavaMethod(String className, String signature) {
        this.className = className;
        this.signature = signature;
    }

    public JavaMethod(SootMethod sootMethod) {
        this.className = sootMethod.getDeclaringClass().getName();
        this.signature = sootMethod.getName() + "("
                + sootMethod.getParameterTypes().stream()
                .map(Type::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    public String getUniqueKey() {
        return className + "." + signature;
    }

    public String toString() {
        return getUniqueKey();
    }
}
