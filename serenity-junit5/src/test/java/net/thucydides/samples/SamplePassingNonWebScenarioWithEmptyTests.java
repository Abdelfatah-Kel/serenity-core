package net.thucydides.samples;

import net.thucydides.core.annotations.Pending;
import net.thucydides.core.annotations.Steps;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePassingNonWebScenarioWithEmptyTests {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplePassingNonWebScenarioWithEmptyTests.class);

    @Steps
    public SampleNonWebSteps steps;

    @Test
    public void happy_day_scenario() throws Throwable {
        steps.stepThatSucceeds();
        steps.stepThatIsIgnored();
        steps.anotherStepThatSucceeds();
    }

    @Test
    public void edge_case_1() {}

    @Pending @Test
    public void edge_case_2() {}
}

