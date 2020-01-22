package net.thucydides.core.requirements.model;

import com.google.common.base.Preconditions;
import net.serenitybdd.core.collect.NewList;
import net.thucydides.core.model.TestTag;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Narrative {
    private final Optional<String> title;
    private final Optional<String> id;
    private final Optional<String> cardNumber;
    private final List<String> versionNumbers;
    private final String text;
    private String type;
    private List<TestTag> tags;
    private List<String> scenarios = new ArrayList<>();

    public Narrative(Optional<String> title, Optional<String> id, Optional<String> cardNumber, List<String> versionNumbers, String type, String text) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(text);
        this.title = title;
        this.id = id;
        this.cardNumber = cardNumber;
        this.versionNumbers = versionNumbers;
        this.type = type;
        this.text = text;
        this.tags = new ArrayList<>();
    }

    public Narrative(Optional<String> title,
                     Optional<String> id,
                     Optional<String> cardNumber,
                     List<String> versionNumbers,
                     String type,
                     String text,
                     List<TestTag> tags) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(text);
        this.title = title;
        this.id = id;
        this.cardNumber = cardNumber;
        this.versionNumbers = versionNumbers;
        this.type = type;
        this.text = text;
        this.tags = new ArrayList<>(tags);
    }


    public Narrative(Optional<String> title,
                     Optional<String> id,
                     Optional<String> cardNumber,
                     List<String> versionNumbers,
                     String type,
                     String text,
                     List<TestTag> tags,
                     List<String> scenarios) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(text);
        this.title = title;
        this.id = id;
        this.cardNumber = cardNumber;
        this.versionNumbers = versionNumbers;
        this.type = type;
        this.text = text;
        this.tags = new ArrayList<>(tags);
        this.scenarios = scenarios;
    }
    public Narrative(String type, String text) {
        this(Optional.<String>empty(), Optional.<String>empty(), Optional.<String>empty(), NewList.<String>of(), type, text);
    }


    public List<TestTag> getTags() {
        return tags;
    }

    public Optional<String> getId() {
        return id;
    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getCardNumber() {
        return cardNumber;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public List<String> getVersionNumbers() {
        return NewList.copyOf(versionNumbers);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("title", title)
                .append("text", text)
                .append("type", type)
                .toString();
    }

    public List<String> getScenarios() {
        return scenarios;
    }
}
