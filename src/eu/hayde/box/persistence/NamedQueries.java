/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import eu.hayde.box.persistence.execptions.HaydePersistenceException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * this is the container of named queries.
 *
 * @author can.senturk
 * @version 2012/07/01
 */
public class NamedQueries {

    //<editor-fold defaultstate="collapsed" desc="Exceptions">
    public static class NamedQueryNotUniqueException extends HaydePersistenceException {

        public NamedQueryNotUniqueException(String string) {
            super(string);
        }
    }
    //</editor-fold>
    private HashMap<String, NamedQuery> container = new HashMap<String, NamedQuery>();

    public void put(TableDefinition tableDefinition, String name, String query) throws NamedQueryNotUniqueException {
        put(new NamedQuery(tableDefinition, name, query));

    }

    public void put(NamedQuery namedQuery) throws NamedQueryNotUniqueException {
        if (container.containsKey(namedQuery.getName())) {
            // this is already known to the container!!
            throw new NamedQueryNotUniqueException("The NamedQuery with the Name: " + namedQuery.getName() + "' is already registered. Names has to be unique for the complete persistence! Check other Classes for that name, too!");
        } else {
            container.put(namedQuery.getName(), namedQuery);
        }
    }

    public void put(NamedQueries namedQueries) throws NamedQueryNotUniqueException {
        Collection<NamedQuery> newQueries = namedQueries.container.values();
        Iterator<NamedQuery> queryIterator = newQueries.iterator();

        while (queryIterator.hasNext()) {
            this.put(queryIterator.next());
        }
    }

    public boolean contains(NamedQuery namedQuery) {
        return this.contains(namedQuery.getName());
    }

    public boolean contains(String name) {
        return container.containsKey(name);
    }

    public void remove(String name) {
        container.remove(name);
    }

    public void remove(NamedQuery namedQuery) {
        this.remove(namedQuery.getName());
    }

    public void remove(NamedQueries namedQueries) {
        Set<String> keysToRemove = namedQueries.container.keySet();
        Iterator<String> keyIterator = keysToRemove.iterator();

        while (keyIterator.hasNext()) {
            this.remove(keyIterator.next());
        }
    }

    public NamedQuery get(String key) {
        return this.container.get(key);
    }
}
