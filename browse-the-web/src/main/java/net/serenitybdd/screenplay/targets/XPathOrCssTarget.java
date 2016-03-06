package net.serenitybdd.screenplay.targets;

import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;

import java.util.List;

import static net.serenitybdd.screenplay.targets.EnsureFieldVisible.ensureThat;

public class XPathOrCssTarget extends Target {

    private final String cssOrXPathSelector;

    public XPathOrCssTarget(String targetElementName, String cssOrXPathSelector) {
        super(targetElementName);
        this.cssOrXPathSelector = cssOrXPathSelector;
    }

    public WebElementFacade resolveFor(Actor theActor) {
        TargetResolver resolver = new TargetResolver(BrowseTheWeb.as(theActor).getDriver());
        WebElementFacade resolvedTarget = resolver.findBy(cssOrXPathSelector);
        ensureThat(theActor).canSee(resolvedTarget);
        return resolvedTarget;
    }

    public List<WebElementFacade> resolveAllFor(Actor actor) {
        TargetResolver resolver = new TargetResolver(BrowseTheWeb.as(actor).getDriver());
        return resolver.findAll(cssOrXPathSelector);
    }

    public Target of(String... parameters) {
        return new XPathOrCssTarget(targetElementName, instantiated(cssOrXPathSelector, parameters));
    }

    public Target called(String name) {
        return new XPathOrCssTarget(name, cssOrXPathSelector);
    }

    public String getCssOrXPathSelector() {
        return cssOrXPathSelector;
    }

    private String instantiated(String cssOrXPathSelector, String[] parameters) {
        return new TargetSelectorWithVariables(cssOrXPathSelector).resolvedWith(parameters);
    }
}
