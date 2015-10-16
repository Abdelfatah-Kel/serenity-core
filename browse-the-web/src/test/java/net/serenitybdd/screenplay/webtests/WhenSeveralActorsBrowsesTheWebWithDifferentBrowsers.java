package net.serenitybdd.screenplay.webtests;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.webtests.tasks.OpenedTheApplication;
import net.serenitybdd.screenplay.webtests.tasks.TheProfile;
import net.serenitybdd.screenplay.webtests.tasks.UpdateHerProfile;
import net.serenitybdd.screenplay.webtests.tasks.ViewMyProfile;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SerenityRunner.class)
public class WhenSeveralActorsBrowsesTheWebWithDifferentBrowsers {

    @Managed(driver = "phantomjs")
    WebDriver firstBrowser;

    @Managed(driver = "phantomjs")
    WebDriver secondBrowser;

    @Managed(driver = "phantomjs")
    WebDriver thirdBrowser;

    @Managed(driver = "phantomjs")
    WebDriver fourthBrowser;

    @Test
    public void multipleUsersCanUseDifferentBrowsers() {

        Actor dana = new Actor("Dana");
        dana.can(BrowseTheWeb.with(firstBrowser));

        Actor jane = new Actor("Jane");
        jane.can(BrowseTheWeb.with(secondBrowser));

        Actor jill = new Actor("Jill");
        jill.can(BrowseTheWeb.with(thirdBrowser));

        givenThat(dana).has(openedTheApplication);

        when(dana).attemptsTo(viewHerProfile);
        and(dana).attemptsTo(UpdateHerProfile.withName("Dana").andCountryOfResidence("France"));

        then(dana).should(seeThat(TheProfile.name(), equalTo("Dana")));
        and(dana).should(seeThat(TheProfile.country(), equalTo("France")));

        givenThat(jane).has(openedTheApplication);
        when(jane).attemptsTo(viewHerProfile);
        and(jane).attemptsTo(UpdateHerProfile.withName("Jane").andCountryOfResidence("United Kingdom"));

        then(jane).should(seeThat(TheProfile.name(), equalTo("Jane")));
        and(jane).should(seeThat(TheProfile.country(), equalTo("United Kingdom")));

        givenThat(jill).has(openedTheApplication);
        when(jill).attemptsTo(viewHerProfile);
        and(jill).attemptsTo(UpdateHerProfile.withName("Jill").andCountryOfResidence("France"));

        then(jill).should(seeThat(TheProfile.name(), equalTo("Jill")));
        and(jill).should(seeThat(TheProfile.country(), equalTo("France")));

    }


    @Test
    public void multipleUsersCanUseDifferentBrowsersInDifferentOrders() {

        Actor dana = new Actor("Dana");
        dana.can(BrowseTheWeb.with(firstBrowser));

        Actor jane = new Actor("Jane");
        jane.can(BrowseTheWeb.with(secondBrowser));

        Actor jill = new Actor("Jill");
        jill.can(BrowseTheWeb.with(thirdBrowser));

        givenThat(dana).has(openedTheApplication);
        andThat(jane).has(openedTheApplication);
        andThat(jill).has(openedTheApplication);

        when(dana).attemptsTo(viewHerProfile);
        and(dana).attemptsTo(UpdateHerProfile.withName("Dana").andCountryOfResidence("France"));
        and(jane).attemptsTo(viewHerProfile);
        and(jane).attemptsTo(UpdateHerProfile.withName("Jane").andCountryOfResidence("United Kingdom"));

        then(dana).should(seeThat(TheProfile.name(), equalTo("Dana")));
        and(dana).should(seeThat(TheProfile.country(), equalTo("France")));

        and(jane).should(seeThat(TheProfile.name(), equalTo("Jane")));
        and(jane).should(seeThat(TheProfile.country(), equalTo("United Kingdom")));

        when(jill).attemptsTo(viewHerProfile);
        and(jill).attemptsTo(UpdateHerProfile.withName("Jill").andCountryOfResidence("France"));

        then(jill).should(seeThat(TheProfile.name(), equalTo("Jill")));
        and(jill).should(seeThat(TheProfile.country(), equalTo("France")));

    }
    @Steps
    OpenedTheApplication openedTheApplication;

    @Steps
    ViewMyProfile viewHerProfile;

    @Steps
    TheProfile herProfile;

}
