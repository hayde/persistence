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
 * marker, that this class is a entity for the persistence manager
 *
 * Position: header of class
 *
 * @author can.senturk
 * @version 2012/06/29
 */
public @interface Entity {
}
