package net.serenitybdd.junit5.datadriven.samples;

import net.serenitybdd.junit5.SerenityBDD;
import net.serenitybdd.junit5.StepsInjectorTestInstancePostProcessor;
import net.thucydides.core.annotations.Steps;
import net.thucydides.samples.SampleNonWebSteps;
import net.thucydides.samples.SampleScenarioSteps;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SerenityBDD
@ExtendWith(StepsInjectorTestInstancePostProcessor.class)
public class SimpleDataDrivenTestScenarioWithValueSource {

    @Steps
    public SampleScenarioSteps steps;

    @ParameterizedTest(name = "run {index} with {arguments}")
    @ValueSource(strings = { "Hello", "JUnit" })
    void withValueSource(String word) {
        steps.stepThatSucceeds();
        steps.anotherStepThatSucceeds();
        assertNotNull(word);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1,2,3 })
    void withValueSourceIntegers(int number) {
        steps.stepThatSucceeds();
        steps.anotherStepThatSucceeds();
    }
}
