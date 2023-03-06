package com.sulir.github.iostudy.shared;

import org.jetbrains.annotations.NotNull;
import soot.SootMethod;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class NativeMethod extends JavaMethod implements Comparable<NativeMethod> {
    private static final Collator collator = Collator.getInstance(new Locale("en", "US"));

    private final String module;
    private final String category;
    protected long id;

    public NativeMethod(long id, String module, String className, String signature, String category) {
        super(className, signature);
        this.id = id;
        this.module = module;
        this.category = category;
    }

    public NativeMethod(String module, SootMethod sootMethod) {
        this(-1, module, sootMethod.getDeclaringClass().getName(), getSignature(sootMethod), "?");
    }

    public NativeMethod(SootMethod sootMethod) {
        this(null, sootMethod);
    }

    public String getModule() {
        return module;
    }

    public String getCategory() {
        return category;
    }

    public String getKey() {
        return getClassName() + "." + getSignature();
    }

    @Override
    public int compareTo(@NotNull NativeMethod other) {
        return Comparator.comparing(NativeMethod::getModule, collator)
                .thenComparing(NativeMethod::getClassName, collator)
                .thenComparing(NativeMethod::getSignature, collator)
                .compare(this, other);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
