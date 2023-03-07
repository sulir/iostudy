package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.shared.NativeMethod;
import com.sulir.github.iostudy.shared.NativeMethodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Kind;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectCallGraph {
    private static final EntryPointPredicate entryPointPredicate = new EntryPointPredicate();
    private static final Logger log = LoggerFactory.getLogger(ProjectCallGraph.class);

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

    public void findNativeCalls() {
        for (Caller caller : reachableSourceMethods) {
            Iterator<SootMethod> entryPoint = List.of(caller.getSootMethod()).iterator();
            Filter filter = new Filter(e -> e.kind() != Kind.CLINIT && e.kind() != Kind.FINALIZE);
            ReachableMethods targets = new ReachableMethods(graph, entryPoint, filter);
            targets.update();
            Iterator<MethodOrMethodContext> iterator = targets.listener();

            while (iterator.hasNext()) {
                SootMethod target = iterator.next().method();
                if (target.isNative()) {
                    NativeMethod jreNative = nativeMethods.getNative(new NativeMethod(target).getKey());
                    if (jreNative != null)
                        caller.addCalledNative(jreNative);
                    else
                        log.warn("Native method {} not in JRE", target.getSignature());
                }
            }
        }
    }

    public List<Caller> getCallers() {
        return reachableSourceMethods;
    }

    public void printCallTrees() {
        for (SootMethod entryPoint : Scene.v().getEntryPoints()) {
            System.out.println(entryPoint);
            recursivelyPrintCallTree(entryPoint, "  ", new HashSet<>());
        }
    }

    private void recursivelyPrintCallTree(SootMethod method, String indent, Set<SootMethod> visited) {
        Iterator<Edge> edges = graph.edgesOutOf(method);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            System.out.println(indent + edge.kind() + " to " + edge.tgt());
            if (visited.add(edge.tgt()))
                recursivelyPrintCallTree(edge.tgt(), indent + "  ", visited);
        }
    }
}
