package net.thucydides.samples;

import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static net.thucydides.core.steps.stepdata.StepData.withTestDataFrom;

@RunWith(SerenityRunner.class)
public class SamplePassingScenarioWithTestSpecificData {

    @Managed(driver="htmlunit")
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "classpath:static-site/index.html")
    public Pages pages;

    @Steps
    public SampleDataDrivenSteps steps;


    @Test
    public void happy_day_scenario() throws Throwable {
        withTestDataFrom("test-data/simple-data.csv").run(steps).data_driven_test_step();
    }
}
