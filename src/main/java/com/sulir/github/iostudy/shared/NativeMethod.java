package com.sulir.github.iostudy.shared;

import org.jetbrains.annotations.NotNull;
import soot.SootMethod;
import soot.Type;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

public class NativeMethod implements Comparable<NativeMethod> {
    private static final Collator collator = Collator.getInstance(new Locale("en", "US"));

    private int id;
    private final String module;
    private final String className;
    private final String signature;
    private final String category;

    public NativeMethod(int id, String module, String className, String signature, String category) {
        this.id = id;
        this.module = module;
        this.className = className;
        this.signature = signature;
        this.category = category;
    }

    public NativeMethod(String module, SootMethod sootMethod) {
        this.id = -1;
        this.module = module;
        className = sootMethod.getDeclaringClass().getName();
        String methodName = sootMethod.getName();
        signature = methodName + "("
                + sootMethod.getParameterTypes().stream()
                .map(Type::toString)
                .collect(Collectors.joining(", ")) + ")";
        category = "?";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    public String getCategory() {
        return category;
    }

    public String toString() {
        return className + "." + signature;
    }

    @Override
    public int compareTo(@NotNull NativeMethod other) {
        return Comparator.comparing(NativeMethod::getModule, collator)
                .thenComparing(NativeMethod::getClassName, collator)
                .thenComparing(NativeMethod::getSignature, collator)
                .compare(this, other);
    }
}
