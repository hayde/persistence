/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
/**
 * compatible to javax.persistence
 *
 * @author cansenturk
 * @version 2012/06/29
 */
public @interface ManyToOne {

    public Class targetEntity() default void.class;

    public CascadeType[] cascade() default {};

    public FetchType fetch() default FetchType.EAGER;

    public boolean optional() default true;
}
