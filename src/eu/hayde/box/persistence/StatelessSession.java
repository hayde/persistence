/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import eu.hayde.box.persistence.PersistenceManager.ClassUnknownToPersistence;
import eu.hayde.box.persistence.TableDefinition.InternalAccessException;
import eu.hayde.box.persistence.execptions.HaydePersistenceException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author can.senturk
 * @version	2012/07/01
 */
public class StatelessSession {

    public static class NamedQueryUnknownException extends HaydePersistenceException {

        public NamedQueryUnknownException(String string) {
            super(string);
        }
    }
    private HashMap<String, TableDefinition> tables = null;
    private NamedQueries namedQueries = null;
    private Connection connection = null;
    private Statistics statistics = null;

    public StatelessSession(Connection connection,
            HashMap<String, TableDefinition> tables,
            NamedQueries namedQueries,
            Statistics statistics) {
        this.connection = connection;
        this.tables = tables;
        this.namedQueries = namedQueries;
        this.statistics = statistics;

        // statistics ...
        if (statistics.isStatisticsEnabled()) {
            statistics.incSessionOpenCount();
        }

    }

    // disable simple creation of this class.
    private StatelessSession() {
    }

    public void close() {

        // statistics ...
        if (statistics.isStatisticsEnabled()) {
            statistics.incSessionCloseCount();
        }
        this.tables = null;
        this.namedQueries = null;
        this.connection = null;
        this.statistics = null;
    }

    @Override
    protected void finalize() throws Throwable {
//        System.out.println("Stateless Session closed");
        super.finalize();
    }

    public TableDefinition getTableDefinition(Object obj) throws ClassUnknownToPersistence {
        TableDefinition returnValue = null;

        if (!tables.containsKey(obj.getClass().getCanonicalName())) {
            // no such class registered to persistence
            throw new ClassUnknownToPersistence("The class " + obj.getClass() + " is not announced to persistence ( with method addClass )");
        } else {

            returnValue = tables.get(obj.getClass().getCanonicalName());
        }

        return returnValue;
    }

    public String getSQLSave(Object obj) throws ClassUnknownToPersistence, InternalAccessException {
        return this.getTableDefinition(obj).getSQLSave(obj);
    }

    public String getSQLLoad(Object obj) throws ClassUnknownToPersistence, InternalAccessException {
        return this.getTableDefinition(obj).getSQLLoad(obj);
    }

    public String getSQLDelete(Object obj) throws ClassUnknownToPersistence, InternalAccessException {
        return this.getTableDefinition(obj).getSQLDelete(obj);
    }

    public String getSQLAll(Object obj) throws ClassUnknownToPersistence {
        return this.getTableDefinition(obj).getSQLAll();
    }

    /**
     * will run a sql statement (in general UPDATE, ALTER TABLE, etc) and will
     * return the number of changed rows
     *
     * @param sqlStatement
     * @return number of rows, that has been changed
     * @throws SQLException
     */
    public int executeSQL(String sqlStatement) throws SQLException {
        int returnValue = 0;
        Statement statement = connection.createStatement();

        // statistics ...
        statistics.writeToLogFile(sqlStatement);
        returnValue = statement.executeUpdate(sqlStatement);

        statement.close();

        return returnValue;
    }

    /**
     * will load a object to the given object class type If not existing, it
     * will return a null object
     *
     * @param classType	the class
     * @param idValue	the unique key of the table
     * @return
     * @throws
     * eu.hayde.box.persistence.PersistenceManager.ClassUnknownToPersistence
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws InstantiationException
     * @throws SQLException
     */
    public <EntityObject> EntityObject load(Class<EntityObject> classType, Object idValue) throws ClassUnknownToPersistence, SQLException, InternalAccessException {

        // create a new instance of the object tyme
        EntityObject container;
        try {
            container = classType.newInstance();
        } catch (InstantiationException ex) {
            throw new InternalAccessException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new InternalAccessException(ex.getMessage());
        }

        // load the id into the container, so that we can handle it further
        this.getTableDefinition(container).setID(container, idValue);

        // create the sql statement for retrieving the data
        String sqlStatement = this.getTableDefinition(container).getSQLLoad(container);

        // load that information
        Statement stmt = connection.createStatement();
        ResultSet rset = null;

        // statistics ...
        statistics.writeToLogFile(sqlStatement);

        try {
            rset = stmt.executeQuery(sqlStatement);
        } catch (SQLException sqlEx) {
            // we make this here, to be sure, that the stmt will be closed,
            // and then we throw the message further ...
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }

        if (rset.isBeforeFirst()) {
            // now fill the data to the system
            this._loadValues(container, rset);
        } else {
            container = null;
        }

        rset.close();
        stmt.close();

        // statistics ...
        if (statistics.isStatisticsEnabled()) {
            statistics.incEntityLoadCount();
        }

        return container;
    }

    public <EntityObject> EntityObject save(EntityObject obj) throws SQLException, ClassUnknownToPersistence, InternalAccessException {
        Statement stmt = connection.createStatement();
        String sqlStatement = this.getSQLSave(obj);
        ResultSet rs = null;
        String keyDBName = this.getTableDefinition(obj).getIDColumn().getDbName();

        // statistics ...
        statistics.writeToLogFile(sqlStatement);

        try {
            stmt.executeUpdate(sqlStatement, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException sqlEx) {
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }

        // now get the id
        try {
            rs = stmt.getGeneratedKeys();
        } catch (SQLException sqlEx) {
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());

            throw new SQLException("Error checking the GeneratedKey after insert for Entity '" + obj.getClass().getCanonicalName() + "' and statement: " + sqlStatement + " ");
        }

        if (rs.next()) {
            // now depending on the format of the id field
            Long tmpID = (Long) rs.getObject(1);

            if (this.getTableDefinition(obj).getIDColumn().getValueType() == Integer.class) {
                // convert to int
                this.getTableDefinition(obj).setID(obj, tmpID.intValue());
            } else {
                // keep it as long
                this.getTableDefinition(obj).setID(obj, tmpID.longValue());
            }
            // statistics ...
            if (statistics.isStatisticsEnabled()) {
                statistics.incEntityInsertCount();
            }
        } else {
            // no key generated, ... it's an update!
            // statistics ...
            if (statistics.isStatisticsEnabled()) {
                statistics.incEntityUpdateCount();
            }
        }

        rs.close();
        stmt.close();

        return obj;
    }

    public <EntityObject> void delete(EntityObject obj) throws ClassUnknownToPersistence, SQLException, InternalAccessException {
        Statement stmt = connection.createStatement();
        String sqlStatement = this.getSQLDelete(obj);

        // statistics ...
        statistics.writeToLogFile(sqlStatement);
        try {
            stmt.executeUpdate(sqlStatement);
        } catch (SQLException sqlEx) {
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }
        stmt.close();

        // statistics ...
        if (statistics.isStatisticsEnabled()) {
            statistics.incEntityDeleteCount();
        }
    }

    public Query getNamedQuery(String queryName) throws NamedQueryUnknownException {
        Query returnValue;
        if (!namedQueries.contains(queryName)) {
            throw new NamedQueryUnknownException("the requested NamedQuery '" + queryName + "' is unknown to this persistence!");
        } else {
            NamedQuery nq = namedQueries.get(queryName);
            returnValue = new Query(this.connection, nq.getTableDefinition(), statistics, this, nq.getQuery());
        }

        return returnValue;
    }

    /**
     * creates from the sql a query, where the return type list is defined by
     * the class of the return type.<br/>
     * <br/>
     * example: query = session.createSQLQuery( "SELECT * FROM customer",
     * Customer.class);
     *
     * @param sqlStatement the statement
     * @param returnType which class should be returned in the query list.
     * @return
     */
    public Query createSQLQuery(String sqlStatement, Class returnType) {
        Query returnValue;
        TableDefinition tableDefinition = null;

        // check if the returnType is existing ...
        if (returnType != null && tables.containsKey(returnType.getCanonicalName())) {
            tableDefinition = tables.get(returnType.getCanonicalName());
        }

        returnValue = new Query(this.connection, tableDefinition, statistics, this, sqlStatement);

        return returnValue;
    }

    private void _loadValues(Object objectToFill, ResultSet rset) throws ClassUnknownToPersistence, InternalAccessException, SQLException {
        this.getTableDefinition(objectToFill).loadValuesFromResultSet(objectToFill, rset);
    }

    public String checkTableStructure() throws ClassUnknownToPersistence, SQLException, InternalAccessException {
        String returnValue = "";
        if (tables != null) {

            for (Map.Entry<String, TableDefinition> table : tables.entrySet()) {
                if (!table.getValue().isGenericTable()) {
                    returnValue += checkTable(table.getValue());
                }
            }
        }
        statistics.writeToLogFile(returnValue);
        return returnValue;
    }

    private String checkTable(TableDefinition tableDefinition) throws ClassUnknownToPersistence, SQLException, InternalAccessException {

        // create a new instance of the object tyme
        String returnValue = "";

        // create the sql statement for retrieving the structure
        String sqlStatement = tableDefinition.getSQLDummy();

        // load that information
        Statement stmt = connection.createStatement();
        ResultSet rset = null;
        ResultSetMetaData rmd = null;

        // statistics ...
        statistics.writeToLogFile(sqlStatement);

        try {
            rset = stmt.executeQuery(sqlStatement);
            rmd = rset.getMetaData();
        } catch (SQLException sqlEx) {

            // in general, we don't have the table her, so just return the error message
            returnValue += "Error: " + sqlEx.getMessage() + "\n";

            // we make this here, to be sure, that the stmt will be closed,
            // and then we throw the message further ...
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }

        if (rmd != null) {
            returnValue += tableDefinition.validate(rmd);
        }

        rset.close();
        stmt.close();

        // statistics ...
        if (statistics.isStatisticsEnabled()) {
            statistics.incEntityLoadCount();
        }

        return returnValue;
    }
}
