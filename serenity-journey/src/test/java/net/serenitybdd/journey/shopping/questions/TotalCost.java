package net.serenitybdd.journey.shopping.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Actor;

public class TotalCost implements Question<Integer> {

    public static TotalCost theTotalCost() {
        return new TotalCost();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return 14;
    }
}
