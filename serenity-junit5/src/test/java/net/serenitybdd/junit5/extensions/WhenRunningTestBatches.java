package net.serenitybdd.junit5.extensions;

import net.serenitybdd.junit5.AbstractTestStepRunnerTest;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.util.MockEnvironmentVariables;
import net.thucydides.core.webdriver.WebDriverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.runners.model.InitializationError;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class WhenRunningTestBatches extends AbstractTestStepRunnerTest {

    @Mock
    FirefoxDriver firefoxDriver;

    MockEnvironmentVariables environmentVariables;


    WebDriverFactory webDriverFactory;

    @Before
    public void createATestableDriverFactory() throws Exception {
        MockitoAnnotations.initMocks(this);
        environmentVariables = new MockEnvironmentVariables();
        webDriverFactory = new WebDriverFactory(environmentVariables);
        StepEventBus.getEventBus().clear();
    }

    @Test
    public void the_test_runner_records_the_steps_as_they_are_executed() throws InitializationError {

        runTestForClass(net.serenitybdd.junit5.samples.SamplePassingScenario.class);

        List<TestOutcome> executedSteps = StepEventBus.getEventBus().getBaseStepListener().getTestOutcomes();
        assertThat(executedSteps.size(), is(3));

        assertThat(inTheTestOutcomes(executedSteps).theOutcomeFor("happy_day_scenario").getTestSteps().size(), is(4));
        assertThat(inTheTestOutcomes(executedSteps).theOutcomeFor("edge_case_1").getTestSteps().size(), is(3));
        assertThat(inTheTestOutcomes(executedSteps).theOutcomeFor("edge_case_2").getTestSteps().size(), is(2));
    }

    private void runTestForClass(Class testClass){
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        LauncherFactory.create().execute(request);
    }

}