/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 *
 * @author can.senturk
 * @version 2012/06/29
 */
public @interface Table {

    public String name() default "";

    public String catalog() default "";

    public String schema() default "";

    public UniqueConstraint[] uniqueConstraints() default {};
}
