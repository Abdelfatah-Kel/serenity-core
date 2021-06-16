package net.serenitybdd.saucelabs;

import net.thucydides.core.ThucydidesSystemProperty;
import net.serenitybdd.core.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.core.webdriver.OverrideDriverCapabilities;
import net.serenitybdd.core.webdriver.enhancers.BeforeAWebdriverScenario;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.webdriver.SupportedWebDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class BeforeASauceLabsScenario implements BeforeAWebdriverScenario {

    private static final String SAUCELABS = "saucelabs.";
    private static final Map<String, String> LEGACY_TO_W3C = new HashMap<>();
    private static final List<String> UNOVERRIDABLE_FIELDS  = Collections.singletonList("server");

    static {
        LEGACY_TO_W3C.put("user", "userName");
        LEGACY_TO_W3C.put("key", "accessKey");
        LEGACY_TO_W3C.put("os_version", "osVersion");
        LEGACY_TO_W3C.put("browser", "browserName");
        LEGACY_TO_W3C.put("browser_version", "browserVersion");
        LEGACY_TO_W3C.put("project", "projectName");
        LEGACY_TO_W3C.put("name", "sessionName");
        LEGACY_TO_W3C.put("build", "buildName");
        LEGACY_TO_W3C.put("device", "deviceName");
        LEGACY_TO_W3C.put("appium_version", "appiumVersion");
    }

    // ["server", "user", "key"]
    private static List<String> NON_SAUCE_PROPERTIES
            = Arrays.asList(
            "browserName",
            "browserVersion",
            "server",
            "user",
            "key"
    );

    @Override
    public DesiredCapabilities apply(EnvironmentVariables environmentVariables,
                                     SupportedWebDriver driver,
                                     TestOutcome testOutcome,
                                     DesiredCapabilities capabilities) {

        // Skipp setting up capabilities if it's not SauceLabs execution
        if (!ThucydidesSystemProperty.SAUCELABS_URL.isDefinedIn(environmentVariables)) {
            return capabilities;
        }

        Properties sauceLabsProperties = EnvironmentSpecificConfiguration
                                                    .from(environmentVariables)
                                                    .getPropertiesWithPrefix(SAUCELABS);

        Properties sauceLabsPropertiesWithOverrides = caterForOverridesIn(sauceLabsProperties);
        OverrideDriverCapabilities.getProperties()
                .forEach((key, value) -> sauceLabsPropertiesWithOverrides.setProperty(key, value.toString()));

        setNonW3CCapabilities(capabilities, sauceLabsPropertiesWithOverrides);

        Map<String, Object> saucelabsOptions = w3CPropertyMapFrom(sauceLabsPropertiesWithOverrides);
        String testName = testOutcome.getStoryTitle() + " - " + testOutcome.getTitle();
        saucelabsOptions.put("name", testName);

        capabilities.setCapability("sauce:options", saucelabsOptions);
        return capabilities;
    }

    private Properties caterForOverridesIn(Properties sauceLabsProperties) {
        Properties propertiesWithOverrides = new Properties();
        sauceLabsProperties
                .stringPropertyNames()
                .stream()
                .filter(this::shouldNotOveride)
                .forEach(
                        name -> propertiesWithOverrides.put(name, sauceLabsProperties.getProperty(name))
                );

        return propertiesWithOverrides;
    }

    private void setNonW3CCapabilities(DesiredCapabilities capabilities, Properties sauceLabsProperties) {
        sauceLabsProperties.stringPropertyNames()
                .stream()
                .filter(this::isNonW3CProperty)
                .forEach(
                        key -> capabilities.setCapability(w3cKey(key), sauceLabsProperties.getProperty(key))
                );
    }

    private boolean shouldNotOveride(String propertyName) {
        return (!OverrideDriverCapabilities.shouldOverrideDefaults()
                || UNOVERRIDABLE_FIELDS.contains(propertyName)
                || UNOVERRIDABLE_FIELDS.contains(unprefixed(propertyName)));
    }

    private Map<String, Object> w3CPropertyMapFrom(Properties properties) {
        Map<String, Object> w3cOptions = new HashMap<>();
        Map<String,Map<String, Object>> nestedOptions = new HashMap<>();

        properties.stringPropertyNames()
                .stream()
                .filter(this::isW3CProperty)
                .forEach(
                        key -> {
                            String unprefixedKey = unprefixed(key);
                            String w3cKey = LEGACY_TO_W3C.getOrDefault(unprefixedKey, unprefixedKey);
                            if (w3cKey.contains(".")) {
                                String parentKey = StringUtils.split(w3cKey,".")[0];
                                String childKey = StringUtils.split(w3cKey,".")[1];
                                Map<String, Object> nestedProperties = nestedOptions.getOrDefault(parentKey, new HashMap<>());
                                nestedProperties.put(childKey, properties.getProperty(key));
                                nestedOptions.put(parentKey, nestedProperties);
                            } else {
                                w3cOptions.put(w3cKey, properties.getProperty(key));
                            }
                        }
                );
        w3cOptions.putAll(nestedOptions);
        return w3cOptions;
    }

    private boolean isNonW3CProperty(String key) {
        return (NON_SAUCE_PROPERTIES.contains(w3cKey(unprefixed(key)))
                || NON_SAUCE_PROPERTIES.contains(w3cKey(key)));
    }

    private boolean isW3CProperty(String key) {
        return !isNonW3CProperty(key);
    }

    private String w3cKey(String key) {
        return LEGACY_TO_W3C.getOrDefault(unprefixed(key), unprefixed(key));
    }

    private String unprefixed(String propertyName) {
        return propertyName.replace(SAUCELABS, "");
    }

}
