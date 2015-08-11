package net.thucydides.core.screenshots.integration

import net.serenitybdd.core.Serenity
import net.thucydides.core.screenshots.ScreenshotProcessor
import net.thucydides.core.steps.BaseStepListener
import net.thucydides.core.steps.StepEventBus
import spock.lang.Specification

class WhenTakingArbitraryScreenshots extends Specification {

    def screenshotProcessor = Mock(ScreenshotProcessor)
    def baseStepListener = Mock(BaseStepListener)

    def setup() {
        StepEventBus.eventBus.registerListener(baseStepListener)
    }

    def "should take an extra screenshot at any time if requested"() {

        when: "we ask for a screenshot at an arbitrary point in a step"
            Serenity.takeScreenshot()
        then: "a screenshot should always be recorded"
            1 * baseStepListener.takeScreenshot()

    }
}
