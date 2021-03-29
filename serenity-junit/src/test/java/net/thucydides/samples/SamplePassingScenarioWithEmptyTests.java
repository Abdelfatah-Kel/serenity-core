package net.thucydides.samples;

import serenitycore.net.thucydides.core.annotations.Managed;
import serenitycore.net.thucydides.core.annotations.ManagedPages;
import serenitycore.net.thucydides.core.annotations.Steps;
import serenitycore.net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ThucydidesRunner.class)
public class SamplePassingScenarioWithEmptyTests {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplePassingScenarioWithEmptyTests.class);

    @Managed
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "classpath:static-site/index.html")
    public Pages pages;
    
    @Steps
    public SampleScenarioSteps steps;

    @Test
    public void happy_day_scenario() throws Throwable {
        steps.stepThatSucceeds();
//        steps.stepThatIsIgnored();
        steps.anotherStepThatSucceeds();
    }

    @Test
    public void edge_case_1() {}

    @Test
    public void edge_case_2() {}
}
