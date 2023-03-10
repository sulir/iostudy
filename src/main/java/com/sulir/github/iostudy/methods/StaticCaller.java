package com.sulir.github.iostudy.methods;

import soot.SootMethod;
import soot.jimple.ReturnVoidStmt;

public class StaticCaller extends Caller {
    private final SootMethod method;

    public StaticCaller(SootMethod method) {
        super(method);
        this.method = method;
    }

    public SootMethod getSootMethod() {
        return method;
    }

    public int getUnitCount() {
        return method.retrieveActiveBody().getUnits().size();
    }

    public boolean isEmpty() {
        return getUnitCount() == 1 && method.retrieveActiveBody().getUnits().getFirst() instanceof ReturnVoidStmt;
    }
}
