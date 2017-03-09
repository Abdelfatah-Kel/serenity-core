package net.serenitybdd.screenplay.targets;

import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;

import java.util.List;

public class ByTarget extends Target {

    private final By locator;

    public ByTarget(String targetElementName, By locator, IFrame iFrame) {
        super(targetElementName, iFrame);
        this.locator = locator;
    }

    public WebElementFacade resolveFor(Actor actor) {
        TargetResolver resolver = TargetResolver.switchIFrameIfRequired(BrowseTheWeb.as(actor).getDriver(), this);
        WebElementFacade resolvedTarget = resolver.find(locator);
        return resolvedTarget;
    }

    public List<WebElementFacade> resolveAllFor(Actor actor) {
        TargetResolver resolver = TargetResolver.switchIFrameIfRequired(BrowseTheWeb.as(actor).getDriver(), this);
        return resolver.findAll(locator);
    }

    public XPathOrCssTarget of(String... parameters) {
        throw new UnsupportedOperationException("The of() method is not supported for By-type Targets");
    }

    @Override
    public String getCssOrXPathSelector() {
        throw new UnsupportedOperationException("The getCssOrXPathSelector() method is not supported for By-type Targets");
    }

    public ByTarget called(String name) {
        return new ByTarget(name, locator, iFrame);
    }
}
