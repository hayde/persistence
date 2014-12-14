/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import eu.hayde.box.persistence.ColumnDefinition.ColumnAnnotationRequireDatabaseFieldNameException;
import eu.hayde.box.persistence.ColumnDefinition.VariableTypeHasNoMatchingSQLTypeException;
import eu.hayde.box.persistence.NamedQueries.NamedQueryNotUniqueException;
import eu.hayde.box.persistence.TableDefinition.ColumnAnnotationExpectedException;
import eu.hayde.box.persistence.TableDefinition.EntityAnnotationMissingException;
import eu.hayde.box.persistence.TableDefinition.IdAnnotationMissingException;
import eu.hayde.box.persistence.TableDefinition.MoreThanOneIDFieldInTableException;
import eu.hayde.box.persistence.TableDefinition.TableAnnotationMissingException;
import eu.hayde.box.persistence.execptions.HaydePersistenceException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author can.senturk
 * @version 2012/06/29
 */
public class PersistenceManager {

    //<editor-fold defaultstate="collapsed" desc="Exceptions">
    public static class ClassAlreadyKnownToPersistenceException extends HaydePersistenceException {

        public ClassAlreadyKnownToPersistenceException(String string) {
            super(string);
        }
    }

    public static class ClassUnknownToPersistence extends HaydePersistenceException {

        public ClassUnknownToPersistence(String string) {
            super(string);
        }
    }

    public static class ConnectionNotOpenException extends HaydePersistenceException {

        public ConnectionNotOpenException(String string) {
            super(string);
        }
    }

    public static class UnknownConnectionErrorException extends HaydePersistenceException {

        public UnknownConnectionErrorException(String string) {
            super(string);
        }
    }
    //</editor-fold>

    private class ConnectionInfo {

        public String driver = null;
        public String url = null;
        public String user = null;
        public String password = null;
    }
    private HashMap<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
    private NamedQueries namedQueries = new NamedQueries();
    private Connection connection = null;
    private ConnectionInfo connectionInfo = new ConnectionInfo();
    private Statistics statistics = new Statistics();

    public PersistenceManager() {
    }

    public PersistenceManager(String driver, String url, String dbuser, String dbpassword) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        this.openConnection(driver, url, dbuser, dbpassword);
    }

    public void openConnection(String driver, String url, String dbuser, String dbpassword) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Class.forName(driver).newInstance();
        connection = DriverManager.getConnection(url, dbuser, dbpassword);

        // we place the information down here. Cause if the connection failed
        // there will be no use of this info
        connectionInfo.driver = driver;
        connectionInfo.url = url;
        connectionInfo.user = dbuser;
        connectionInfo.password = dbpassword;
    }

    public Statistics getStatistics() {
        return this.statistics;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            // exception will occure only, if the connection is already thrown away!
        } finally {
            connectionInfo = new ConnectionInfo();
        }
    }

    public void close() {
        this.closeConnection();
    }

    public boolean isClosed() throws SQLException {
        return (connection == null || connection.isClosed());
    }

    public void reconnect() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        ConnectionInfo tmpConnectionInfo = this.connectionInfo;
        this.close();
        this.openConnection(tmpConnectionInfo.driver, tmpConnectionInfo.url, tmpConnectionInfo.user, tmpConnectionInfo.password);
    }

    public void addClass(Class theClass) throws EntityAnnotationMissingException, TableAnnotationMissingException, ColumnAnnotationExpectedException, ColumnAnnotationRequireDatabaseFieldNameException, VariableTypeHasNoMatchingSQLTypeException, MoreThanOneIDFieldInTableException, IdAnnotationMissingException, ClassAlreadyKnownToPersistenceException, NamedQueryNotUniqueException {
        this.addClass(theClass, false);
    }

    /**
     * does add a class to the persistence driver.
     *
     * theClass is the java class, that will be registered to the persistence
     * driver isGeneric is a marker, if the class is a generic class and does
     * not require a valid table on the db site
     *
     * @param theClass
     * @param isGeneric
     * @throws
     * eu.hayde.box.persistence.TableDefinition.EntityAnnotationMissingException
     * @throws
     * eu.hayde.box.persistence.TableDefinition.TableAnnotationMissingException
     * @throws
     * eu.hayde.box.persistence.TableDefinition.ColumnAnnotationExpectedException
     * @throws
     * eu.hayde.box.persistence.ColumnDefinition.ColumnAnnotationRequireDatabaseFieldNameException
     * @throws
     * eu.hayde.box.persistence.ColumnDefinition.VariableTypeHasNoMatchingSQLTypeException
     * @throws
     * eu.hayde.box.persistence.TableDefinition.MoreThanOneIDFieldInTableException
     * @throws
     * eu.hayde.box.persistence.TableDefinition.IdAnnotationMissingException
     * @throws
     * eu.hayde.box.persistence.PersistenceManager.ClassAlreadyKnownToPersistenceException
     * @throws
     * eu.hayde.box.persistence.NamedQueries.NamedQueryNotUniqueException
     */
    public void addClass(Class theClass, boolean isGeneric) throws EntityAnnotationMissingException, TableAnnotationMissingException, ColumnAnnotationExpectedException, ColumnAnnotationRequireDatabaseFieldNameException, VariableTypeHasNoMatchingSQLTypeException, MoreThanOneIDFieldInTableException, IdAnnotationMissingException, ClassAlreadyKnownToPersistenceException, NamedQueryNotUniqueException {

        // check if this class is already in the persistence library
        if (tables.containsKey(theClass.getCanonicalName())) {
            throw new ClassAlreadyKnownToPersistenceException("The class '" + theClass.getCanonicalName() + "' is already added to the persistence. You need to remove it first to refresh the information!");
        } else {
            TableDefinition table = new TableDefinition(theClass);
            table.setGenericTable(isGeneric);
            // load named queries and check, if they are already known to the system
            NamedQueries newNamedQueries = table.getNamedQueries();
            this.namedQueries.put(newNamedQueries);

            // put to tables definition
            tables.put(theClass.getCanonicalName(), table);
        }

    }

    public void removeClass(Class theClass) {
        if (tables.containsKey(theClass.getCanonicalName())) {

            // 1. remove all named queries
            TableDefinition toRemoveTD = tables.get(theClass.getCanonicalName());
            NamedQueries tempNQ = toRemoveTD.getNamedQueries();
            this.namedQueries.remove(tempNQ);

            // 2. remove table definition from tables HashMap
            tables.remove(theClass.getCanonicalName());

        }
    }

    public StatelessSession openStatelessSession() throws ConnectionNotOpenException, UnknownConnectionErrorException, SQLException {

        StatelessSession returnValue = null;

        // if there is no connection ...
        if (connectionInfo.driver == null) {
            throw new ConnectionNotOpenException("You never tried to open a connection or already closed the connection!");
        } else if (connection == null || connection.isClosed()) {
            throw new UnknownConnectionErrorException("The connection is no more valid due unknown circumstances (maybe timed out?!)!");
        } else {

            returnValue = new StatelessSession(connection, tables, namedQueries, statistics);

        }

        return returnValue;
    }
}
