/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
/**
 *
 * @author cansenturk
 * @version 2012/06/29
 */
public @interface NamedQuery {

    public String name();

    public String query();

    public QueryHint[] hints() default {};
}
