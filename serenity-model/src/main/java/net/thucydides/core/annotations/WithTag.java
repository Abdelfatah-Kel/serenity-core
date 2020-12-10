package net.thucydides.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that a test case or test relates to a particular issue or story card in the issue tracking system.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithTags.class)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface WithTag {
    String value() default "";
    String name() default "";
    String type() default "feature";
}