/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

/**
 * here we will simply store a named query
 *
 * @author can.senturk
 * @version 2012/07/01
 */
public class NamedQuery {

    private String name;
    private String query;
    private TableDefinition tableDefinition;

    public NamedQuery() {
    }

    public NamedQuery(TableDefinition tableDefinition, String name, String query) {
        this.name = name;
        this.query = query;
        this.tableDefinition = tableDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    public void setTableDefintion(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }
}
