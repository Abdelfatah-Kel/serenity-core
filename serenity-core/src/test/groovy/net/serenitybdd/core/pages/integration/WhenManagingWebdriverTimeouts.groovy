package net.serenitybdd.core.pages.integration

import net.serenitybdd.core.pages.WebElementFacade
import net.thucydides.core.ThucydidesSystemProperty
import net.thucydides.core.pages.integration.StaticSitePage
import net.thucydides.core.steps.ExecutedStepDescription
import net.thucydides.core.steps.StepEventBus
import net.thucydides.core.steps.StepFailure
import net.thucydides.core.util.EnvironmentVariables
import net.thucydides.core.util.MockEnvironmentVariables
import net.thucydides.core.webdriver.StaticTestSite
import net.thucydides.core.webdriver.ThucydidesWebdriverManager
import net.thucydides.core.webdriver.exceptions.ElementShouldBeDisabledException
import net.thucydides.core.webdriver.exceptions.ElementShouldBeEnabledException
import net.thucydides.core.webdriver.exceptions.ElementShouldBeInvisibleException
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.TimeoutException
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

/**
 * Timeouts are highly configurable in Serenity.
 *
 * The first level of timeouts is related to the standard WebDriver implicit waits
 * (see http://docs.seleniumhq.org/docs/04_webdriver_advanced.jsp#implicit-waits).
 * <quote>"An implicit wait is to tell WebDriver to poll the DOM for a certain amount of
 * time when trying to find an element or elements if they are not immediately available."</quote>
 * The WebDriver default is 0. It can be overriden using the webdriver.timeouts.implicitlywait system property.
 *
 *
 */
class WhenManagingWebdriverTimeouts extends Specification {

    StaticTestSite staticTestSite
    StaticSitePage page
    def driver
    EnvironmentVariables environmentVariables

    def setup() {
        environmentVariables = new MockEnvironmentVariables();
        def phantomJSPath = MockEnvironmentVariables.fromSystemEnvironment().getProperty(ThucydidesSystemProperty.PHANTOMJS_BINARY_PATH)
        if (phantomJSPath) {
            environmentVariables.setProperty("phantomjs.binary.path", phantomJSPath)
        }
        StepEventBus.eventBus.clear()
    }

    def cleanup() {
        ThucydidesWebdriverManager.inThisTestThread().closeAllDrivers();
    }

    def StaticSitePage openTestPageUsing(String browser) {
        staticTestSite = new StaticTestSite(environmentVariables)
        driver = staticTestSite.open(browser)
        page = new StaticSitePage(driver, environmentVariables);
        return page
    }

    def defaultBrowser = "htmlunit"

    //
    // IMPLICIT WAITS
    //
    def "WebDriver implicit waits are defined using the webdriver.timeouts.implicitlywait system property"() {
        given: "The #slow-loader field takes 4 seconds to load"
        and: "We configure the WebDriver implicit wait to be 0 seconds"
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
        when: "We access the field"
            page = openTestPageUsing(defaultBrowser)
            page.slowLoadingField.isDisplayed()
        then: "An error should be thrown"
            thrown(org.openqa.selenium.ElementNotVisibleException)
    }

    @Timeout(2)
    def "Slow loading fields should not wait once a step has failed"() {
        given: "The #slow-loader field takes 4 seconds to load"
        and: "A step has failed"
            def stepFailure = Mock(StepFailure)
            StepEventBus.getEventBus().testStarted("a test")
            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle("a step"))
            StepEventBus.getEventBus().stepFailed(stepFailure);
        when: "We access the field"
            page = openTestPageUsing(defaultBrowser)
            page.verySlowLoadingField.isDisplayed()
        then: "Not error should not be thrown"
            notThrown(org.openqa.selenium.ElementNotVisibleException)
    }

    def "The default implicit wait is set to 2 seconds"() {
        given: "The #city field takes 500 ms to load"
            page = openTestPageUsing(defaultBrowser)
        when: "We access the field using the default implicit wait timeouts"
        then: "The field should be retrieved correctly"
            page.city.isDisplayed()
    }

    def "The implicit waits apply when you find a list of elements"() {
        when: "We access the a list of elements"
            page = openTestPageUsing("phantomjs")
            int itemCount = page.elementItems.size()
        then: "They should all be found"
            itemCount == 4
    }

    def "If the implicit wait times out when fetching a list of values only the currently loaded values will be returned"() {
        given: "We configure the WebDriver implicit wait to be 0 milliseconds"
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
        when: "We access the a list of elements"
            page = openTestPageUsing(defaultBrowser)
            int itemCount = page.elementItems.size()
        then: "Only the elements loaded after the timeout should be loaded"
            itemCount == 0
    }

    def "You can force an extra delay to give elements time to load"() {
        given: "We configure the WebDriver implicit wait to be 0 milliseconds"
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "0")
        when: "We access the a list of elements"
            page = openTestPageUsing(defaultBrowser)
            def count = page.withTimeoutOf(5,SECONDS).waitFor(page.elementItems).size()
        then: "Only the elements loaded after the timeout should be loaded"
            count == 4
    }

    @Timeout(20)
    def "You can override the implicit wait during test execution"() {
        given: "The #slow-loader field takes 3 seconds to load"
            page = openTestPageUsing("phantomjs")
        when: "We override the implicit timeout to allow the slow-loader field to load"
            page.setImplicitTimeout(100, SECONDS)
        then: "we should be able to access the slow-loader field"
            page.firstElementItem.isVisible()
        and: "we can reset the driver timeouts to the default value once we are done"
            page.resetImplicitTimeout()
        and:
            !page.fieldDoesNotExist.isVisible()
        and:
            page.driver.currentImplicitTimeout.in(SECONDS) == 2
    }

    def "Implicit timeout should not be affected by isCurrently* methods"() {
        given: "The #slow-loader WebElementFacade field takes 3 seconds to load"
            page = openTestPageUsing("phantomjs")
        when: "We override the implicit timeout to allow the slow-loader field to load"
            page.setImplicitTimeout(5, SECONDS)
        and: "isCurrently* methods should not use the implicit timeout"
            !page.slowLoadingField.isCurrentlyVisible()
        then: "we can reset the driver timeouts to the default value once we are done"
            page.driver.currentImplicitTimeout.in(SECONDS) == 5
        and: "we can reload a slow loading WebElementFacade normally"
            page.slowLoadingField.isDisplayed()
    }


    def "Element loading times should not be affected by isCurrently* methods"() {
        given: "The #slow-loader field takes 3 seconds to load"
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","5000")
            page = openTestPageUsing("phantomjs")
        when: "we should be able to access the slow-loader field"
            page.country.isCurrentlyVisible()
        then: "we can reload a slow element normally"
            page.slowLoadingField.isDisplayed()
    }

    @Unroll
    def "When you check the visibility of a field using isVisible() Serenity should use the current implicit wait timeout (#field)"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when: "We check the visibility of a slow-loading field"
            def visibility = page."$field".isVisible()
        then:
            visibility == expectedVisibility
        where:
            field                | expectedVisibility
            "hiddenField"        | false                 // Invisible
            "firstName"          | true                  // Immediately visible
            "city"               | true                  // loads in 500 ms
            "slowLoadingField"   | false                 // loads in 4 seconds
    }

    @Unroll
    def "If you want to check the current visibility of a field using isCurrentlyVisible() Serenity will return a result immediately"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when: "We check the visibility of a slow-loading field"
            def visibility = page."$field".isCurrentlyVisible()
        then:
            visibility == expectedVisibility
        where:
            field                | expectedVisibility
            "hiddenField"        | false                 // Invisible
            "firstName"          | true                  // Immediately visible
            "city"               | false                 // loads in 500 ms
            "slowLoadingField"   | false                 // loads in 3 seconds
    }

    def "The webdriver.timeouts.implicitlywait value is used when loading elements using the findAll() method."() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            page = openTestPageUsing(defaultBrowser)
        when: "We fetch a list of elements using findElements"
            def elements = page.findAll(By.cssSelector("#elements option"))
        then:
            elements.size() == 0
    }


    def "The webdriver.timeouts.implicitlywait value is used when loading non-existant elements"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            page = openTestPageUsing(defaultBrowser)
        when: "We check for an element that does not exist"
            def elementVisible = page.isElementVisible(By.cssSelector("#does-not-exist"))
        then:
            !elementVisible
    }


    //
    // WAIT-FOR TIMEOUTS
    //
    def "You can also explicitly wait for fields to appear. This will use the webdriver.wait.for.timeout property rather than the implicit timeouts"() {
        given: "We set the webdriver.wait.for.timeout to 5 seconds"
            environmentVariables.setProperty("webdriver.implicit.wait","0")
            environmentVariables.setProperty("webdriver.wait.for.timeout","5000")
        and:
            page = openTestPageUsing(defaultBrowser)
        when: "We wait for a field to appear that takes 2 seconds to load"
            page.slowLoadingField.waitUntilVisible()
        then:
            page.slowLoadingField.isCurrentlyVisible()
    }

    def "Waiting for a field will fail if it exceeds the wait.for.timeout value"() {
        given: "We set the webdriver.wait.for.timeout to 1 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "1000")
        and: "The implicit wait will timeout"
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
        and:
            page = openTestPageUsing(defaultBrowser)
        when: "We wait for a field to appear that takes 2 seconds to load"
            page.slowLoadingField.waitUntilVisible()
        then:
            NoSuchElementException timeout = thrown()
        and:
            timeout.message.contains("Timed out after 1 second")
    }

    def "You can wait for elements to be not visible"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.placetitle.waitUntilNotVisible()
        then:
            !page.placetitle.isCurrentlyVisible()

    }

    def "You can wait for elements to be enabled"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.initiallyDisabled.waitUntilEnabled()
        then:
            page.initiallyDisabled.isCurrentlyEnabled()
    }


    def "You can wait for elements to be disabled"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.initiallyEnabled.waitUntilDisabled()
        then:
            !page.initiallyEnabled.isCurrentlyEnabled()
    }

    def "The wait.for.timeout applies for checking methods like isElementVisible()"() {
        given:
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            def cityIsVisible = page.isElementVisible(By.cssSelector("#city"))
        then:
            !cityIsVisible
    }

    def "The default wait.for.timeout will work checking methods like isElementVisible() with slow-loadding fields"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            def cityIsVisible = page.isElementVisible(By.cssSelector("#city"))
        then:
            cityIsVisible
    }

    def "The waitUntilDisabled method can be configured (globally) using the webdriver.wait.for.timeout property"() {
        given: "We set the webdriver.wait.for.timeout to a low value"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "100")
        and:
            page = openTestPageUsing(defaultBrowser)
        when: "we wait for a field to be disabled"
            page.initiallyEnabled.waitUntilDisabled()
        then: "the action should timeout"
            thrown(ElementShouldBeDisabledException)
    }

    def "The waitUntilEnabled method can be configured (globally) using the webdriver.wait.for.timeout property"() {
        given: "We set the webdriver.wait.for.timeout to 1 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "100")
        and:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.initiallyDisabled.waitUntilEnabled()
        then:
            thrown(ElementShouldBeEnabledException)
    }


    def "The waitUntilVisible method can be configured (globally) using the webdriver.wait.for.timeout property"() {
        given: "We set the webdriver.wait.for.timeout to 1 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "100")
        and:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.city.waitUntilVisible()
        then:
            thrown(NoSuchElementException)
    }

    def "The waitUntilInvisible method can be configured (globally) using the webdriver.wait.for.timeout property"() {
        given: "We set the webdriver.wait.for.timeout to 1 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "100")
        and:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.placetitle.waitUntilNotVisible()
        then:
            thrown(ElementShouldBeInvisibleException)
    }


    //
    // Using the withTimeoutOf() methods
    //

    def "The withTimeoutOf() method can be used to override the global webdriver.wait.for.timeout value"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            def cityIsDisplayed = page.withTimeoutOf(50, MILLISECONDS).elementIsDisplayed(By.cssSelector("#city"))
        then:
            !cityIsDisplayed
    }

    def "The withTimeoutOf() method can be used to wait until a button is clickable"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.initiallyDisabled.withTimeoutOf(5, SECONDS).waitUntilClickable().click()
        then:
            noExceptionThrown()
    }

    def "The withTimeoutOf() method can be used to wait until a button is clickable and will fail if it waits too long"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            page.initiallyDisabled.withTimeoutOf(50, MILLISECONDS).waitUntilClickable().click()
        then:
            thrown(TimeoutException)
    }

    def "The withTimeoutOf() method can be used to override the global webdriver.wait.for.timeout value (positive case)"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            def cityIsDisplayed = page.withTimeoutOf(2, SECONDS).elementIsDisplayed(By.cssSelector("#city"))
        then:
            cityIsDisplayed
    }

    def "The withTimeoutOf() method can be used to modify the timeout for elementIsPresent methods"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
            def cityIsPresent = page.withTimeoutOf(2, SECONDS).elementIsPresent(By.cssSelector("#city"))
        then:
            cityIsPresent
    }


    def "The withTimeoutOf() method can be used to override the global webdriver.wait.for.timeout value for elements"() {
        given:
            page = openTestPageUsing(defaultBrowser)
        when:
           page.placetitle.withTimeoutOf(50, MILLISECONDS).waitUntilNotVisible()
        then:
            thrown(ElementShouldBeInvisibleException)
    }


    def "The withTimeoutOf() method can be used to override the global timeouts when waiting lists"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            page.withTimeoutOf(5, SECONDS).waitForPresenceOf(By.cssSelector("#elements option"))
        then:
            page.elementItems.size() == 4
    }

    def "The withTimeoutOf() method can be used to override the global timeouts for elements"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            page.withTimeoutOf(5, SECONDS).waitFor(By.cssSelector("#city"))
        then:
            page.city.isCurrentlyVisible()
            page.isElementVisible(By.cssSelector("#city"))
    }


    @Timeout(3)
    def "Should not hang if CSS selector is incorrect"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            page.waitFor("NOT!%**##CSS")
        then:
            thrown(TimeoutException)
    }

    def "The withTimeoutOf() method can be used to override the global timeouts when retrieving lists"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            def elements = page.withTimeoutOf(5, SECONDS).findAll("#elements option")
        then:
            elements.size() == 4
    }


    def "The withTimeoutOf() method can be used to reduce the global timeouts when retrieving lists"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","5000")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "5000")
            page = openTestPageUsing(defaultBrowser)
        when:
            def elements = page.withTimeoutOf(0, SECONDS).findAll("#elements option")
        then:
            elements.size() == 0
    }


    def "The withTimeoutOf() method can be used to override the global timeouts when retrieving elements"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","50")
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50")
            page = openTestPageUsing(defaultBrowser)
        when:
            WebElementFacade city = page.withTimeoutOf(5, SECONDS).find("#city")
        then:
            city.isCurrentlyVisible()
    }

    @Timeout(5)
    def "waitForAbsenceOf should return immediately if no elements are present"() {
        when:
            environmentVariables.setProperty("webdriver.wait.for.timeout", "50000")
            page = openTestPageUsing(defaultBrowser)
        then:
            page.waitForAbsenceOf("#does-not-exist")
    }

    @Timeout(8)
    def "waitForAbsenceOf should wait no more than the time needed for the element to dissapear"() {
        when: "placetitle will dissapear after 2 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "16000")
            page = openTestPageUsing(defaultBrowser)
        then:
            page.waitForAbsenceOf("#placetitle")
    }


    def "waitForAbsenceOf with explicit timeout should wait no more than the time needed for the element to dissapear"() {
        given: "placetitle will dissapear after 2 seconds"
            environmentVariables.setProperty("webdriver.wait.for.timeout", "10000")
            page = openTestPageUsing(defaultBrowser)
        when:
            page.withTimeoutOf(1, SECONDS).waitForAbsenceOf("#placetitle")
        then:
            thrown(org.openqa.selenium.TimeoutException)
    }

    def "Timeouts for individual fields can be specified using the timeoutInSeconds parameter of the FindBy annotation"() {
        given:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
            page = openTestPageUsing(defaultBrowser)
        when:
            page.country.isDisplayed()
        then: "Annotated timeouts on fields override configured implicit timeouts"
            page.country.isCurrentlyVisible()
    }

    def "You can check whether a child element is present using a By selector"() {
        when:
        environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
        page = openTestPageUsing(defaultBrowser)
        then:
        page.clients.shouldContainElements(By.cssSelector(".color"))
        and:
        page.clients.shouldContainElements(".color")
    }

    def "You can check whether a child element is present"() {
        when:
            environmentVariables.setProperty("webdriver.timeouts.implicitlywait","0")
            page = openTestPageUsing(defaultBrowser)
        then:
            page.clients.containsElements(By.cssSelector(".color"))
        and:
            !page.clients.containsElements(By.cssSelector(".flavor"))
    }

    def "You can check whether a child element is present with waits"() {
        when:
            page = openTestPageUsing(defaultBrowser)
        then:
            page.clients.withTimeoutOf(0, TimeUnit.SECONDS).containsElements(By.cssSelector(".color"))
    }

}
