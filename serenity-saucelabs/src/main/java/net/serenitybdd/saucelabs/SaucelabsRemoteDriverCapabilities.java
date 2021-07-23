package net.serenitybdd.saucelabs;

import net.serenitybdd.core.environment.EnvironmentSpecificConfiguration;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.webdriver.capabilities.RemoteTestName;
import net.thucydides.core.webdriver.capabilities.W3CCapabilities;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.*;

import static net.thucydides.core.ThucydidesSystemProperty.SAUCELABS_BROWSERNAME;
import static net.thucydides.core.ThucydidesSystemProperty.SAUCELABS_TEST_NAME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SaucelabsRemoteDriverCapabilities {

    private final EnvironmentVariables environmentVariables;

    private static final Map<String, String> SAUCELABS_BROWSER_NAMES = new HashMap<String, String>() {{
        put("iexplorer", "internet explorer");
        put("edge", "MicrosoftEdge");
    }};

    public SaucelabsRemoteDriverCapabilities(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Saucelabs capabilities are defined in the saucelabs environment configuration variables, e.g.
     * saucelabs {
     * screenResolution = "800x600"
     * recordScreenshots = false
     * }
     * <p>
     * These are added to the 'sauce:options' capability.
     */
    public DesiredCapabilities getCapabilities(DesiredCapabilities capabilities) {
        MutableCapabilities saucelabsCapabilities = saucelabsCapabilitiesDefinedIn(environmentVariables);

        MutableCapabilities w3cCapabilitiesInSaucelabsSection = W3CCapabilities.definedIn(environmentVariables).withPrefix("saucelabs");

        configureBrowserAndPlatformIfDefinedInSaucelabsBlock(w3cCapabilitiesInSaucelabsSection, capabilities);

        addBuildNumberTo(saucelabsCapabilities);
        configureTestName(saucelabsCapabilities);

        capabilities.setCapability("sauce:options", saucelabsCapabilities);
        capabilities.setJavascriptEnabled(true);

        return capabilities;
    }

    @NotNull
    private MutableCapabilities saucelabsCapabilitiesDefinedIn(EnvironmentVariables environmentVariables) {
        Properties saucelabsProperties = EnvironmentSpecificConfiguration.from(environmentVariables).getPropertiesWithPrefix("saucelabs.");
        MutableCapabilities sauceCaps = new MutableCapabilities();

        for (String propertyName : saucelabsProperties.stringPropertyNames()) {
            String unprefixedPropertyName = unprefixed(propertyName);
            sauceCaps.setCapability(unprefixedPropertyName, saucelabsProperties.getProperty(propertyName));
        }
        return sauceCaps;
    }

    /**
     * The Saucelabs URL, specified in the `saucelabs.url` property.
     */
    public String getUrl() {
        String saucelabsUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getOptionalProperty(ThucydidesSystemProperty.SAUCELABS_URL).orElse(null);
        if (saucelabsUrl == null) {
            return null;
        } else {
            return environmentVariables.injectSystemPropertiesInto(saucelabsUrl);
        }
    }

    private void addBuildNumberTo(MutableCapabilities capabilities) {
        if (environmentVariables.getProperty("BUILD_NUMBER") != null) {
            capabilities.setCapability("build", environmentVariables.getProperty("BUILD_NUMBER"));
        }
    }

    private void configureBrowserAndPlatformIfDefinedInSaucelabsBlock(MutableCapabilities sourceCapabilities, MutableCapabilities capabilities) {
        if (sourceCapabilities.getBrowserName() != null) {
            String browserName = sourceCapabilities.getBrowserName();
            capabilities.setCapability("browserName", SAUCELABS_BROWSER_NAMES.getOrDefault(browserName, browserName));
        }
        if (sourceCapabilities.getVersion() != null) {
            capabilities.setCapability("browserVersion", sourceCapabilities.getVersion());
        }
        if (sourceCapabilities.getCapability("platformName") != null) {
            capabilities.setCapability("platformName", sourceCapabilities.getCapability("platformName"));
        }
    }

    private void configureTestName(MutableCapabilities capabilities) {
        String testName = SAUCELABS_TEST_NAME.from(environmentVariables);
        if (isNotEmpty(testName)) {
            capabilities.setCapability("name", testName);
        } else {
            Optional<String> guessedTestName;
            Optional<TestOutcome> latestOutcome = StepEventBus.getEventBus().getBaseStepListener().latestTestOutcome();

            boolean sessionPerScenario = Arrays.asList("scenario", "example")
                    .contains(ThucydidesSystemProperty.SERENITY_RESTART_BROWSER_FOR_EACH.from(environmentVariables));

            guessedTestName = latestOutcome.map(testOutcome -> {
                // Add scenario name to session name only if browser session is restarted for every scenario
                return Optional.of(sessionPerScenario ? testOutcome.getCompleteName() : testOutcome.getStoryTitle());
            }).orElseGet(RemoteTestName::fromCurrentTest);

            guessedTestName.ifPresent(
                    name -> capabilities.setCapability("name", name)
            );
        }
    }

    private void configureTargetBrowser(DesiredCapabilities capabilities, EnvironmentVariables environmentVariables) {
        String remoteBrowser = SAUCELABS_BROWSERNAME.from(environmentVariables);

        if (isNotEmpty(remoteBrowser)) {
            capabilities.setBrowserName(SAUCELABS_BROWSER_NAMES.getOrDefault(remoteBrowser, remoteBrowser));
        }
    }

    private String unprefixed(String propertyName) {
        return propertyName.replace("saucelabs.", "");
    }
}
