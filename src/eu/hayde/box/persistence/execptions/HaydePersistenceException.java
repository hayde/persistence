/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence.execptions;

/**
 * this is the general exception class for the haydePersistence Manager every
 * exception is a child of this class
 *
 * @author can.senturk
 * @version 2012/07/01
 */
public class HaydePersistenceException extends Exception {

    private HaydePersistenceException() {
    }

    public HaydePersistenceException(String string) {
        super(string);
    }
}
