package serenityscreenplaywebdriver.net.serenitybdd.screenplay.questions.targets;

import serenityscreenplay.net.serenitybdd.screenplay.Actor;
import serenityscreenplay.net.serenitybdd.screenplay.Question;
import serenityscreenplay.net.serenitybdd.screenplay.annotations.Subject;
import serenityscreenplaywebdriver.net.serenitybdd.screenplay.questions.Attribute;
import serenityscreenplaywebdriver.net.serenitybdd.screenplay.targets.Target;

import java.util.List;

@Subject("#target")
public class TargetAttributes implements Question<List<String>> {

    private final Target target;
    private final String name;

    TargetAttributes(Target target, String name) {
        this.target = target;
        this.name = name;
    }

    @Override
    public List<String> answeredBy(Actor actor) {
        return Attribute.of(target).named(name).viewedBy(actor).asList();
    }
}
