package net.serenitybdd.journey.shopping.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The role of a task is to simulate a high-level action performed by the user.
 * It is common practice to add methods, e.g.
 *
 */
public class PlaceInBasket implements Performable {

    public static PlaceInBasket placed_the_item_in_her_basket() {
        return instrumented(PlaceInBasket.class);
    }

    @Step("And {0} has placed the item in her shopping basket")
    public void performAs(Actor actor) {
    }
}
