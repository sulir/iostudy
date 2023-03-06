package com.sulir.github.iostudy.shared;

import soot.SootMethod;
import soot.Type;

import java.util.stream.Collectors;

public class JavaMethod {

    protected final String className;
    protected final String signature;

    protected static String getSignature(SootMethod sootMethod) {
        return sootMethod.getName() + "("
                + sootMethod.getParameterTypes().stream()
                .map(Type::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

    public JavaMethod(String className, String signature) {
        this.className = className;
        this.signature = signature;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    public String toString() {
        return className + "." + signature;
    }
}
