package net.serenitybdd.junit5.datadriven.samples;

import net.serenitybdd.junit5.StepsInjectorTestInstancePostProcessor;
import net.thucydides.core.annotations.Steps;
import net.thucydides.samples.SampleScenarioSteps;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(StepsInjectorTestInstancePostProcessor.class)
public class SimpleDataDrivenTestScenarioWithEnumSource {

    @Steps
    public SampleScenarioSteps steps;

    @ParameterizedTest
    @EnumSource(Books.class)
    public void withEnumSource(Books book) {
        steps.stepThatSucceeds();
        steps.anotherStepThatSucceeds();
        assertNotNull(book.value());
    }

    @ParameterizedTest
    @EnumSource(value = Books.class, names = {"BDD_IN_ACTION","SPRING_IN_ACTION"} )
    public void withEnumSourceSelectedBooks(Books book) {
        steps.stepThatSucceeds();
        steps.anotherStepThatSucceeds();
        assertNotNull(book.value());
    }

    @ParameterizedTest
    @EnumSource(value = Books.class, mode = EnumSource.Mode.EXCLUDE, names = {"SPRING_IN_ACTION"} )
    public void withEnumSourceExcludedBooks(Books book) {
        steps.stepThatSucceeds();
        steps.anotherStepThatSucceeds();
        assertNotNull(book.value());
    }
}
