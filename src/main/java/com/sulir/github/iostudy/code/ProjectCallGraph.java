package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethod;
import com.sulir.github.iostudy.shared.NativeMethodList;
import org.slf4j.LoggerFactory;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectCallGraph {
    private static final EntryPointPredicate entryPointPredicate = new EntryPointPredicate();

    private final Project project;
    private final NativeMethodList nativeMethods;
    private CallGraph graph;
    private List<Caller> reachableSourceMethods = new ArrayList<>();

    public ProjectCallGraph(Project project, NativeMethodList nativeMethods) {
        this.project = project;
        this.nativeMethods = nativeMethods;
    }

    public void construct() {
        findEntryPoints();
        SparkTransformer.v().transform();
        graph = Scene.v().getCallGraph();
        findReachableSourceMethods();
    }

    public void findNativeCallers() {
        for (Caller caller : reachableSourceMethods) {
            Iterator<SootMethod> entryPoint = List.of(caller.getSootMethod()).iterator();
            Filter withoutStaticInitializers = new Filter(e -> !e.isClinit());
            ReachableMethods targets = new ReachableMethods(graph, entryPoint, withoutStaticInitializers);
            targets.update();
            Iterator<MethodOrMethodContext> iterator = targets.listener();

            while (iterator.hasNext()) {
                SootMethod target = iterator.next().method();
                if (target.isNative()) {
                    NativeMethod jreNative = nativeMethods.getNative(new NativeMethod(target).getKey());
                    if (jreNative != null)
                        caller.addCalledNative(jreNative);
                    else
                        LoggerFactory.getLogger(ProjectCallGraph.class)
                                .warn("Native method {} not in JRE", target.getSignature());
                }
            }
        }
    }

    public List<Caller> getCallers() {
        return reachableSourceMethods;
    }

    private void findEntryPoints() {
        Scene.v().setEntryPoints(project.getSourceMethods()
                .filter(entryPointPredicate)
                .toList());
    }

    private void findReachableSourceMethods() {
        ReachableMethods reachableFromEntry = Scene.v().getReachableMethods();

        reachableSourceMethods = project.getSourceMethods()
                .filter(reachableFromEntry::contains)
                .map(Caller::new)
                .collect(Collectors.toList());
    }
}
