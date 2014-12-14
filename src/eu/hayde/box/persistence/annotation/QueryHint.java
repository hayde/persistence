/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {})
@Retention(value = RetentionPolicy.RUNTIME)
/**
 *
 * @author cansenturk
 * @Version 2012/06/29
 */
public @interface QueryHint {

    public String name();

    public String value();
}
