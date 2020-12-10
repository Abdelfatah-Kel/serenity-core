package net.thucydides.core.util;

import net.serenitybdd.core.collect.NewList;
import net.thucydides.core.tags.Taggable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

/**
 * This is an INTERNAL helper class of serenity, it should not be used directly and may be subject to refactoring.
 * <p>
 * It serves to decouple serenity-model (and serenity-core) from JUnit. If JUnit is on the classpath, JUnit
 * classes will be handled specifically. But serenity will continue to function without JUnit being on the classpath.
 * Furthermore, users can choose between JUnit 4 and JUnit 5 or even use both together.
 * <p>
 * Ideally this approach could be generalized to avoid any special treatment of JUnit as a test framework.
 * Test-framework specific strategies could be registered from the framework-specific modules. That would allow
 * to place the implementation of (and the tests for) the test-framework specific strategies into the modules that know
 * about those frameworks and also have the necessary dependencies.
 * <p>
 * But this would be a more extensive change, potentially breaking backwards compatibility. So for now this class does
 * exactly only this: It provides an adapter for JUnit 4 and JUnit 5, and makes explicit the parts of the code where
 * there has previously been a hard dependency on JUnit 4.
 * <p>
 * As such it is self-contained and should be possible to be grasped rather easily. And it marks the starting point for
 * a potential refactoring towards a more general approach.
 */
public class JUnitAdapter {

    private static List<JUnitStrategy> strategies = new ArrayList<>();

    static {
        if (isClassPresent("org.junit.runner.RunWith")) {
            strategies.add(new JUnit4Strategy());
        }
        if (isClassPresent("org.junit.jupiter.api.Test")) {
            strategies.add(new JUnit5Strategy());
        }
    }

    // used only for testing purposes
    static List<JUnitStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    public static boolean isTestClass(final Class<?> testClass) {
        if (testClass == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isTestClass(testClass));
    }

    public static boolean isTestMethod(final Method method) {
        if (method == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isTestMethod(method));
    }

    public static boolean isTestSetupMethod(final Method method) {
        if (method == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isTestSetupMethod(method));
    }

    public static boolean isSerenityTestCase(final Class<?> testClass) {
        if (testClass == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isSerenityTestCase(testClass));
    }

    public static boolean isAssumptionViolatedException(final Throwable throwable) {
        return delegateToStrategies(s -> s.isAssumptionViolatedException(throwable));
    }

    public static boolean isATaggableClass(final Class<?> testClass) {
        if (testClass == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isATaggableClass(testClass));
    }

    public static boolean isIgnored(final Method method) {
        if (method == null) {
            return false;
        }
        return delegateToStrategies(s -> s.isIgnored(method));
    }

    private static boolean delegateToStrategies(
            final Function<JUnitStrategy, Boolean> jUnitStrategyBooleanFunction) {
        return strategies.stream().map(jUnitStrategyBooleanFunction).filter(Boolean::booleanValue).findFirst()
                .orElse(false);
    }

    private static boolean isClassPresent(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private interface JUnitStrategy {

        boolean isTestClass(final Class<?> testClass);

        boolean isTestMethod(final Method method);

        boolean isTestSetupMethod(final Method method);

        boolean isSerenityTestCase(final Class<?> testClass);

        boolean isAssumptionViolatedException(final Throwable throwable);

        boolean isATaggableClass(final Class<?> testClass);

        boolean isIgnored(final Method method);

    }

    private static class JUnit4Strategy implements JUnitStrategy {

        private final List<String> LEGAL_SERENITY_RUNNER_NAMES
                = NewList.of("SerenityRunner", "ThucydidesRunner", "SerenityParameterizedRunner","ThucydidesParameterizedRunner");

        @Override
        public boolean isTestClass(final Class<?> testClass) {
            return containsAnnotationCalled(testClass.getAnnotations(), "RunWith");
//            return (testClass.getAnnotation(org.junit.runner.RunWith.class) != null);
        }

        private boolean containsAnnotationCalled(Annotation[] annotations, String annotationName) {
            return stream(annotations).anyMatch(annotation -> annotation.annotationType().getSimpleName().equals(annotationName));
        }

        @Override
        public boolean isTestMethod(final Method method) {
            return containsAnnotationCalled(method.getAnnotations(), "Test");
//            return (method.getAnnotation(org.junit.Test.class) != null);
        }

        @Override
        public boolean isTestSetupMethod(final Method method) {
            return containsAnnotationCalled(method.getAnnotations(), "Before")
                    || containsAnnotationCalled(method.getAnnotations(), "BeforeClass");

//            return (method.getAnnotation(org.junit.Before.class) != null)
//                    || (method.getAnnotation(org.junit.BeforeClass.class) != null);
        }

        @Override
        public boolean isSerenityTestCase(Class<?> testClass) {
            org.junit.runner.RunWith runWithAnnotation = testClass.getAnnotation(org.junit.runner.RunWith.class);
            if (runWithAnnotation != null) {
                return LEGAL_SERENITY_RUNNER_NAMES.contains(runWithAnnotation.value().getSimpleName());
            }
            return false;
        }

        @Override
        public boolean isAssumptionViolatedException(final Throwable throwable) {
            return (throwable instanceof org.junit.internal.AssumptionViolatedException);
        }

        @Override
        public boolean isATaggableClass(Class<?> testClass) {
            org.junit.runner.RunWith runWith = testClass.getAnnotation(org.junit.runner.RunWith.class);
            return (runWith != null && Taggable.class.isAssignableFrom(runWith.value()));
        }

        @Override
        public boolean isIgnored(final Method method) {
            // intentionally left at previous implementation based on annotation name to change as little as possible
            Annotation[] annotations = method.getAnnotations();
            return stream(annotations).anyMatch(
                    annotation -> annotation.annotationType().getSimpleName().equals("Ignore")
            );
        }

    }

    private static class JUnit5Strategy implements JUnitStrategy {

        @Override
        public boolean isTestClass(final Class<?> testClass) {
            for (final Method method : testClass.getDeclaredMethods()) {
                if (isTestMethod(method)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isTestMethod(final Method method) {
            return isAnnotated(method, org.junit.jupiter.api.Test.class);
        }

        @Override
        public boolean isTestSetupMethod(final Method method) {
            return isAnnotated(method, org.junit.jupiter.api.BeforeEach.class) || isAnnotated(method, org.junit.jupiter.api.BeforeAll.class);
        }

        @Override
        public boolean isSerenityTestCase(Class<?> testClass) {
            return findRepeatableAnnotations(testClass, org.junit.jupiter.api.extension.ExtendWith.class).stream()
                    .flatMap(annotation -> stream((annotation).value()))
                    .anyMatch(extension -> extension.getSimpleName().matches("Serenity.*Extension"));
        }

        @Override
        public boolean isIgnored(final Method method) {
            return isAnnotated(method, org.junit.jupiter.api.Disabled.class);
        }

        @Override
        public boolean isAssumptionViolatedException(final Throwable throwable) {
            return (throwable instanceof org.opentest4j.TestAbortedException);
        }

        @Override
        public boolean isATaggableClass(final Class<?> testClass) {
            return findRepeatableAnnotations(testClass, org.junit.jupiter.api.extension.ExtendWith.class).stream()
                    .flatMap(annotation -> stream((annotation).value()))
                    .anyMatch(Taggable.class::isAssignableFrom);
        }
    }
}
