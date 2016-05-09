/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import eu.hayde.box.persistence.TableDefinition.InternalAccessException;
import eu.hayde.box.persistence.execptions.HaydePersistenceException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * here we wills store queries of the session object.
 *
 * @author can.senturk
 * @version	2012/07/01
 */
public class Query {

    public static class UnknownParameterInQueryException extends HaydePersistenceException {

        public UnknownParameterInQueryException(String string) {
            super(string);
        }
    }

    public static class ExpectedUniqueResultException extends HaydePersistenceException {

        public ExpectedUniqueResultException(String string) {
            super(string);
        }
    }
    private TableDefinition tableDefinition = null;
    private Connection connection = null;
    private Statistics statistics = null;
    private StatelessSession statelessSession = null;
    private String queryStatement = null;
    private String processName = null;

    private Query() {
    }

    public Query(Connection connection,
            TableDefinition tableDefinition,
            Statistics statistics,
            StatelessSession statelessSession,
            String queryStatement) {
        this.connection = connection;
        this.tableDefinition = tableDefinition;
        this.statistics = statistics;
        this.statelessSession = statelessSession;
        this.queryStatement = queryStatement;

        // here we will parse the incoming sql query to check, if it is already
        // or not.
        //
        // if the the tabledefinition is null: only plain sql is allowed
        if (tableDefinition != null) {
            this.queryStatement = tableDefinition.prepareQuery(queryStatement);
        }
    }

    public void setParameter(String parameter, Object value) throws UnknownParameterInQueryException {
        if (!queryStatement.contains(":" + parameter)) {
            throw new UnknownParameterInQueryException("The parameter '" + parameter + "' is unknown to Query: " + queryStatement);
        }
        else {
            if (value instanceof java.util.Date) {
                java.sql.Date sqlDate;
                sqlDate = new java.sql.Date(((java.util.Date) value).getTime());
                queryStatement = queryStatement.replace(":" + parameter, sqlDate.toString());

            }
            else {
                queryStatement = queryStatement.replace(":" + parameter, value.toString());

            }
        }
    }

    public List list() throws SQLException, InternalAccessException {

        List returnValue;
        if (tableDefinition == null) {
            returnValue = this._executeSQLObject();
        }
        else {
            returnValue = this._executeSQL();
        }
        return returnValue;
    }

    public <EntityClass> EntityClass uniqueResult() throws ExpectedUniqueResultException, SQLException, InternalAccessException {
        List<EntityClass> returnValue = this._executeSQL();

        if (returnValue.size() > 1) {
            throw new ExpectedUniqueResultException("Expected Unique result, but received " + returnValue.size() + " elements for query statement: " + queryStatement);
        }
        else if (returnValue.isEmpty()) {
            return null;
        }
        return returnValue.get(0);
    }

    
    public int executeUpdate() throws SQLException {
        int returnValue = 0;
        if (queryStatement != null && !queryStatement.equals("")) {
            Statement stmt = connection.createStatement();
            // statistics ...
            statistics.writeToLogFile(queryStatement);

            try {
                returnValue = stmt.executeUpdate(queryStatement);
            }
            catch (SQLException sqlEx) {
                stmt.close();
                statistics.writeToLogFile(sqlEx.getMessage());
                throw sqlEx;
            }
            stmt.close();
        }
        return returnValue;
    }

    private <EntityClass> List<EntityClass> _executeSQL() throws SQLException, InternalAccessException {
        ResultSet rset;
        Statement stmt = connection.createStatement();
        // statistics ...
        statistics.writeToLogFile(queryStatement);
        try {
            rset = stmt.executeQuery(queryStatement);
        }
        catch (SQLException sqlEx) {
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }

        List<EntityClass> returnValue = new ArrayList<EntityClass>();

        while (rset.next()) {

            // if we now what type of class is expected, we could return the
            // right element. but if not ... look to else
            EntityClass newObject;
            try {
                newObject = (EntityClass) tableDefinition.getEntityClass().newInstance();
                tableDefinition.loadValuesFromResultSet(newObject, rset);
            }
            catch (InstantiationException ex) {
                rset.close();
                stmt.close();
                throw new InternalAccessException(ex.getMessage());
            }
            catch (IllegalAccessException ex) {
                rset.close();
                stmt.close();
                throw new InternalAccessException(ex.getMessage());
            }

            returnValue.add(newObject);
        }

        rset.close();
        stmt.close();
        rset = null;
        stmt = null;

        return returnValue;
    }

    private List<Object[]> _executeSQLObject() throws SQLException {
        ResultSet rset;
        Statement stmt = connection.createStatement();
        // statistics ...
        statistics.writeToLogFile(queryStatement);
        try {
            rset = stmt.executeQuery(queryStatement);
        }
        catch (SQLException sqlEx) {
            stmt.close();
            statistics.writeToLogFile(sqlEx.getMessage());
            throw sqlEx;
        }

        List<Object[]> returnValue = new ArrayList<Object[]>();
        ResultSetMetaData rsmd = rset.getMetaData();
        int columnCount = rsmd.getColumnCount();

        while (rset.next()) {
            // ... if we don't know what is coming back, return a object array
            Object[] objects = new Object[columnCount];

            for (int i = 0; i < columnCount; i++) {
                objects[i] = rset.getObject(i + 1);
            }

            returnValue.add(objects);
        }

        rset.close();
        stmt.close();
        rset = null;
        stmt = null;

        return returnValue;
    }
}
