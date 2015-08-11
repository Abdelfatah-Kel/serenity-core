package net.thucydides.core.steps;

import com.google.common.collect.Lists;
import net.serenitybdd.core.di.DependencyInjector;
import net.serenitybdd.core.injectors.EnvironmentDependencyInjector;
import net.thucydides.core.annotations.Fields;
import net.serenitybdd.core.pages.PageObject;
import net.thucydides.core.pages.Pages;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class PageObjectDependencyInjector implements DependencyInjector {

    private final Pages pages;

    EnvironmentDependencyInjector environmentDependencyInjector;

    public PageObjectDependencyInjector(Pages pages) {
        this.pages = pages;
        this.environmentDependencyInjector = new EnvironmentDependencyInjector();
    }

    public void injectDependenciesInto(Object target) {
        environmentDependencyInjector.injectDependenciesInto(target);
        List<Field> pageObjectFields = pageObjectFieldsIn(target);
        for(Field pageObjectField : pageObjectFields) {
            instantiatePageObjectIfNotAssigned(pageObjectField, target);
        }
    }

    @Override
    public void reset() {}

    private void instantiatePageObjectIfNotAssigned(Field pageObjectField, Object target) {
        try {
            pageObjectField.setAccessible(true);
            if (pageObjectField.get(target) == null) {
                Class<PageObject> pageObjectClass = (Class<PageObject>) pageObjectField.getType();
                PageObject newPageObject = pages.getPage(pageObjectClass);
                injectDependenciesInto(newPageObject);
                pageObjectField.set(target, newPageObject);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instanciate page objects in " + target);
        }
    }

    private List<Field> pageObjectFieldsIn(Object target) {
        Set<Field> allFields = Fields.of(target.getClass()).allFields();
        List<Field> pageObjectFields = Lists.newArrayList();
        for(Field field : allFields) {
            if (PageObject.class.isAssignableFrom(field.getType())) {
                pageObjectFields.add(field);
            }
        }
        return pageObjectFields;
    }
}
