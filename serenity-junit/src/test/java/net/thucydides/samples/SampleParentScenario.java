package net.thucydides.samples;

import serenitycore.net.thucydides.core.annotations.Managed;
import serenitycore.net.thucydides.core.annotations.ManagedPages;
import serenitycore.net.thucydides.core.annotations.Steps;
import serenitycore.net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
public class SampleParentScenario {
    
    @Managed
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = "classpath:static-site/index.html")
    private Pages pages;
    
    @Steps
    private SampleScenarioSteps steps;

    protected SampleScenarioSteps getSteps() {
        return steps;
    }

}
