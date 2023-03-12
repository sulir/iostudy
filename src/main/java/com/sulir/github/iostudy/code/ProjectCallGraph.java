package com.sulir.github.iostudy.code;

import com.sulir.github.iostudy.methods.NativeMethod;
import com.sulir.github.iostudy.methods.StaticCaller;
import com.sulir.github.iostudy.shared.NativeMethodList;
import com.sulir.github.iostudy.shared.Project;
import com.sulir.github.iostudy.shared.TestPredicate;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProjectCallGraph {
    private static final Predicate<SootMethod> entryPointPredicate = new TestPredicate().or(SootMethod::isMain);
    private static final Logger log = LoggerFactory.getLogger(ProjectCallGraph.class);

    private final Project project;
    private final NativeMethodList nativeMethods;
    private CallGraph graph;
    private List<StaticCaller> reachableSourceMethods = new ArrayList<>();

    public ProjectCallGraph(Project project, NativeMethodList nativeMethods) {
        this.project = project;
        this.nativeMethods = nativeMethods;
    }

    public void construct() {
        findEntryPoints();
        SparkTransformer.v().transform();
        graph = Scene.v().getCallGraph();
        findReachableSourceMethods();

        if (System.getenv("IOSTUDY_TREE") != null)
            printCallTrees();
        if (System.getenv("IOSTUDY_REACHABLE") != null)
            printReachableMethods();
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
                .map(StaticCaller::new)
                .collect(Collectors.toList());
    }

    public void findNativeCalls() {
        for (StaticCaller caller : reachableSourceMethods) {
            Iterator<SootMethod> entryPoint = List.of(caller.getSootMethod()).iterator();
            ReachableMethods targets = new ReachableMethods(graph, entryPoint, createFilter());
            targets.update();
            Iterator<MethodOrMethodContext> iterator = targets.listener();

            while (iterator.hasNext()) {
                SootMethod target = iterator.next().method();
                if (target.isNative()) {
                    NativeMethod jreNative = nativeMethods.getNative(new NativeMethod(target).getUniqueKey());
                    if (jreNative != null)
                        caller.addCalledNative(jreNative);
                    else
                        log.warn("Native method {} not in JRE", target.getSignature());
                }
            }
        }
    }

    private Filter createFilter() {
        return new Filter(e -> e.kind() != Kind.CLINIT && e.kind() != Kind.FINALIZE);
    }

    public List<StaticCaller> getCallers() {
        return reachableSourceMethods;
    }

    public void printCallTrees() {
        for (SootMethod entryPoint : Scene.v().getEntryPoints()) {
            System.out.println(entryPoint);
            recursivelyPrintCallTree(entryPoint, "  ", new HashSet<>());
        }
    }

    private void recursivelyPrintCallTree(SootMethod method, String indent, Set<SootMethod> visited) {
        Iterator<Edge> edges = createFilter().wrap(graph.edgesOutOf(method));
        Set<SootMethod> printed = new HashSet<>();

        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (!printed.add(edge.tgt()))
                continue;

            System.out.println(indent + edge.kind() + " to " + edge.tgt());
            if (visited.add(edge.tgt()))
                recursivelyPrintCallTree(edge.tgt(), indent + "  ", visited);
            else
                System.out.println(indent + "  ...");
        }
    }

    public void printReachableMethods() {
        for (StaticCaller caller : reachableSourceMethods) {
            System.out.println(caller);

            Iterator<SootMethod> entryPoint = List.of(caller.getSootMethod()).iterator();
            ReachableMethods targets = new ReachableMethods(graph, entryPoint, createFilter());
            targets.update();
            Iterator<MethodOrMethodContext> iterator = targets.listener();

            List<SootMethod> sorted = new ArrayList<>();
            while (iterator.hasNext())
                sorted.add(iterator.next().method());
            sorted.sort(Comparator.comparing(SootMethod::getSignature));

            for (SootMethod target : sorted)
                System.out.println("  " + target);
        }
    }
}
