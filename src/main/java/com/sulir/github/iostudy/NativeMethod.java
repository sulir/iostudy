package com.sulir.github.iostudy;

import org.jetbrains.annotations.NotNull;
import soot.SootMethod;
import soot.Type;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

public class NativeMethod implements Comparable<NativeMethod> {
    private static final Collator collator = Collator.getInstance(new Locale("en", "US"));

    private final int id;
    private String moduleName;
    private final String className;
    private final String methodSignature;

    public NativeMethod(String moduleName, SootMethod sootMethod) {
        this.id = -1;
        this.moduleName = moduleName;
        className = sootMethod.getDeclaringClass().getName();
        String methodName = sootMethod.getName();
        methodSignature = methodName + "("
                + sootMethod.getParameterTypes().stream()
                .map(Type::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

    public NativeMethod(int id, String clazz, String methodSignature) {
        this.id = id;
        this.className = clazz;
        this.methodSignature = methodSignature;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return className + "." + methodSignature;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    @Override
    public int compareTo(@NotNull NativeMethod other) {
        return Comparator.comparing(NativeMethod::getModuleName, collator)
                .thenComparing(NativeMethod::getClassName, collator)
                .thenComparing(NativeMethod::getMethodSignature, collator)
                .compare(this, other);
    }
}
