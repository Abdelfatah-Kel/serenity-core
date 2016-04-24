package net.serenitybdd.junit.finder

import net.serenitybdd.junit.runners.ParameterizedTestsOutcomeAggregator
import net.serenitybdd.junit.runners.SerenityParameterizedRunner
import net.thucydides.core.ThucydidesSystemProperty
import net.thucydides.core.batches.BatchManagerProvider
import net.thucydides.core.util.MockEnvironmentVariables
import net.thucydides.core.webdriver.Configuration
import net.thucydides.core.webdriver.SystemPropertiesConfiguration
import net.thucydides.core.webdriver.WebDriverFactory
import net.thucydides.junit.rules.QuietThucydidesLoggingRule
import net.thucydides.junit.rules.SaveWebdriverSystemPropertiesRule
import net.thucydides.samples.SampleDataDrivenScenarioWithQualifier
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.notification.RunNotifier
import spock.lang.Specification
import static net.thucydides.core.webdriver.SystemPropertiesConfiguration.JUNIT_RETRY_TESTS
import static net.thucydides.core.webdriver.SystemPropertiesConfiguration.MAX_RETRIES

class WhenRunningADataDrivenTestScenarioWithQualifier extends Specification {

    @Rule
    def TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    def SaveWebdriverSystemPropertiesRule saveWebdriverSystemPropertiesRule = new SaveWebdriverSystemPropertiesRule();

    @Rule
    def QuietThucydidesLoggingRule quietThucydidesLoggingRule = new QuietThucydidesLoggingRule();

    def MockEnvironmentVariables environmentVariables;

    def Configuration configuration;

    def setup() {
        environmentVariables = new MockEnvironmentVariables();
        environmentVariables.setProperty(MAX_RETRIES, "1");
        environmentVariables.setProperty(JUNIT_RETRY_TESTS, "true");
        configuration = new SystemPropertiesConfiguration(environmentVariables);
    }

    def "when test contains method to determine Qualifier it should be used for step names"() {
        given:
            def outputDirectory = tempFolder.newFolder("serenitybdd")
            environmentVariables.setProperty(ThucydidesSystemProperty.THUCYDIDES_OUTPUT_DIRECTORY.getPropertyName(),
                outputDirectory.getAbsolutePath())

            def runner = getStubbedTestRunnerUsing(SampleDataDrivenScenarioWithQualifier.class)
        when:
            runner.run(new RunNotifier())
            def outcomes = ParameterizedTestsOutcomeAggregator.from(runner).aggregateTestOutcomesByTestMethods()
        then:
            outcomes.size() == 1
            def happyDayOutcomes = outcomes.get(0);
            def happyDaySteps = happyDayOutcomes.getTestSteps()
            happyDaySteps.size() == 12;
        and:
            happyDayOutcomes.getTitle().matches("Happy day scenario")
            happyDaySteps.each { step ->
                assert step.getDescription().matches("Happy day scenario for [a-zA-Z]{1} and \\d{1,2}")
            }
    }

    def private SerenityParameterizedRunner getStubbedTestRunnerUsing(Class<?> testClass) throws Throwable {
        def configuration = new SystemPropertiesConfiguration(environmentVariables)
        def factory = new WebDriverFactory(environmentVariables)
        def batchManager = new BatchManagerProvider(configuration).get()
        return new SerenityParameterizedRunner(testClass, configuration, factory, batchManager) {
            @Override
            public void generateReports() {
                //do nothing
            }
        }
    }
}