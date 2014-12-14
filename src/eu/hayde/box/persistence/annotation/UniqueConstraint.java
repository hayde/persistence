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
 * @author can.senturk
 */
public @interface UniqueConstraint {

    public String[] columnNames();
}
