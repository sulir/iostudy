package com.sulir.github.iostudy.code;

import soot.SootMethod;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.function.Predicate;

public class EntryPointPredicate implements Predicate<SootMethod> {
    private static final Predicate<SootMethod> isMain = SootMethod::isMain;

    private static final Predicate<SootMethod> isJUnit3Test = (m) ->
            m.getDeclaringClass().getSuperclass().getName().equals("junit.framework.TestCase")
            && m.getName().startsWith("test") && m.getParameterCount() == 0;

    private static final Predicate<SootMethod> isJUnit4test = (m) ->
            hasAnnotation(m, "org.junit.Test") && m.getParameterCount() == 0;

    private static final Predicate<SootMethod> isJUnit5Test = (m) ->
            hasAnnotation(m, "org.junit.jupiter.api.Test") && m.getParameterCount() == 0;

    @Override
    public boolean test(SootMethod sootMethod) {
        return isMain.or(isJUnit3Test).or(isJUnit4test).or(isJUnit5Test).test(sootMethod);
    }

    private static boolean hasAnnotation(SootMethod method, String annotationType) {
        String typeSignature = 'L' + annotationType.replace('.', '/') + ';';
        VisibilityAnnotationTag tag = (VisibilityAnnotationTag) method.getTag("VisibilityAnnotationTag");
        if (tag != null)
            return tag.getAnnotations().stream().anyMatch(a -> a.getType().equals(typeSignature));
        else
            return false;
    }
}
