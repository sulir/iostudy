package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethodList;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

public class ProjectCallGraph {
    private final NativeMethodList nativeMethods;
    private final EntryPointPredicate entryPointPredicate = new EntryPointPredicate();
    private CallGraph graph;

    public ProjectCallGraph(NativeMethodList nativeMethods) {
        this.nativeMethods = nativeMethods;
    }

    public void construct() {
        findEntryPoints();
        SparkTransformer.v().transform();
        graph = Scene.v().getCallGraph();
        deleteStaticInitializers();
    }

    private void findEntryPoints() {
        Scene.v().setEntryPoints(Scene.v().getApplicationClasses().stream()
                .flatMap(c -> c.getMethods().stream())
                .filter(entryPointPredicate)
                .toList());
    }

    private void deleteStaticInitializers() {
        for (SootClass clazz : Scene.v().getClasses()) {
            clazz.getMethods().stream()
                    .filter(SootMethod::isStaticInitializer)
                    .findFirst()
                    .ifPresent(clazz::removeMethod);
        }
    }
}
