package com.sulir.github.iostudy.methods;

import org.jetbrains.annotations.NotNull;
import soot.SootMethod;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class NativeMethod extends JavaMethod implements Comparable<NativeMethod> {
    private static final Collator collator = Collator.getInstance(new Locale("en", "US"));

    private final String module;
    private final String category;
    protected long id = -1;

    public NativeMethod(long id, String module, String className, String signature, String category) {
        super(className, signature);
        this.id = id;
        this.module = module;
        this.category = category;
    }

    public NativeMethod(String module, SootMethod sootMethod) {
        super(sootMethod);
        this.module = module;
        this.category = "?";
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NotNull NativeMethod other) {
        return Comparator.comparing(NativeMethod::getModule, collator)
                .thenComparing(NativeMethod::getClassName, collator)
                .thenComparing(NativeMethod::getSignature, collator)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        NativeMethod that = (NativeMethod) other;

        if (id == -1 || that.id == -1)
            return getClassName().equals(that.getClassName()) && getSignature().equals(that.getSignature());
        else
            return id == that.id;
    }

    @Override
    public int hashCode() {
        if (id == -1)
            return Objects.hash(getClassName(), getSignature());
        else
            return Objects.hash(id);
    }
}
