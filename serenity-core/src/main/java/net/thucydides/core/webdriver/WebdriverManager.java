package net.thucydides.core.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Manage WebDriver instances.
 * It instantiates browser drivers, based on the test configuration, and manages them for the
 * duration of the tests.
 * 
 * @author johnsmart
 *
 */
public interface WebdriverManager {

    WebDriver getWebdriver();

    WebdriverContext inContext(String context);

    WebDriver getWebdriver(String driver);
    WebDriver getWebdriverByName(String actorName);

    String getCurrentDriverType();

    SessionId getSessionId();

    void closeDriver();

    void closeAllCurrentDrivers();

    void closeAllDrivers();

    void resetDriver();

    int getCurrentActiveWebdriverCount();

    int getActiveWebdriverCount();

    boolean isDriverInstantiated();

    void setCurrentDriver(WebDriver driver);

    void clearCurrentDriver();

    void registerDriver(WebDriver driver);
}