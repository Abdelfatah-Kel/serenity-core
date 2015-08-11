package net.thucydides.core.model.stacktrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.util.EnvironmentVariables;

import java.util.List;

import static net.thucydides.core.ThucydidesSystemProperty.SIMPLIFIED_STACK_TRACES;

/**
 * Created by john on 30/01/15.
 */
public class StackTraceSanitizer {

    private final EnvironmentVariables environmentVariables;
    private final StackTraceElement[] stackTrace;

    private final static List<String> MASKED_PACKAGES = ImmutableList.of(
            "sun.",
            "com.sun",
            "java.",
            "org.junit",
            "org.gradle",
            "org.testng",
            "org.hamcrest",
            "com.jayway.restassured",
            "org.fest",
            "org.assertj",
            "org.openqa.selenium",
            "org.spockframework",
            "org.apache.maven.surefire",
            "com.intellij",
            "net.sf.cglib",
            "org.codehaus.groovy",
            "org.jbehave",
            "cucumber.runtime",
            "cucumber.api",
            "net.serenitybdd.core",
            "net.serenitybdd.junit",
            "net.serenitybdd.plugins",
            "net.serenitybdd.cucumber",
            "net.serenitybdd.jbehave",
            "net.thucydides.core",
            "net.thucydides.junit",
            "net.thucydides.plugins",
            "net.thucydides.jbehave");

    public StackTraceSanitizer(EnvironmentVariables environmentVariables, StackTraceElement[] stackTrace) {
        this.environmentVariables = environmentVariables;
        this.stackTrace = stackTrace;
    }

    public static StackTraceSanitizer forStackTrace(StackTraceElement[] stackTrace) {
        return new StackTraceSanitizer(Injectors.getInjector().getProvider(EnvironmentVariables.class).get(),
                                       stackTrace);
    }


    public StackTraceElement[] sanitized(StackTraceElement[] stackTrace) {
        return useSimplifedStackTraces() ? simplifiedStackTrace(stackTrace) : stackTrace;
    }

    private boolean useSimplifedStackTraces() {
        return SIMPLIFIED_STACK_TRACES.booleanFrom(environmentVariables ,true);
    }

    private StackTraceElement[] simplifiedStackTrace(StackTraceElement[] stackTrace) {
        List<StackTraceElement> cleanStackTrace = Lists.newArrayList();
        for(StackTraceElement element : stackTrace) {
            if (shouldDisplayInStackTrace(element)) {
                cleanStackTrace.add(element);
            }
        }
        return cleanStackTrace.toArray(new StackTraceElement[0]);
    }

    private boolean shouldDisplayInStackTrace(StackTraceElement element) {
        if (element.getClassName().contains("$")) {
            return false;
        }
        for(String maskedPackage : MASKED_PACKAGES) {
            if (element.getClassName().startsWith(maskedPackage)) {
                return false;
            }
        }
        return true;
    }

    public StackTraceElement[] getSanitizedStackTrace() {
        return sanitized(stackTrace);
    }
}
