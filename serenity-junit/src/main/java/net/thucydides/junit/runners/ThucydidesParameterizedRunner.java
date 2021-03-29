package net.thucydides.junit.runners;

import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import serenitymodel.net.thucydides.core.batches.BatchManager;
import serenitycore.net.thucydides.core.webdriver.DriverConfiguration;
import serenitycore.net.thucydides.core.webdriver.WebDriverFactory;

/**
 * @deprecated Replaced by SerenityParameterizedRunner
 *
 * Provided for backwards compatibility
 */
@Deprecated
public class ThucydidesParameterizedRunner extends SerenityParameterizedRunner {
    public ThucydidesParameterizedRunner(Class<?> klass, DriverConfiguration configuration, WebDriverFactory webDriverFactory, BatchManager batchManager) throws Throwable {
        super(klass, configuration, webDriverFactory, batchManager);
    }

    public ThucydidesParameterizedRunner(Class<?> klass) throws Throwable {
        super(klass);
    }
}
