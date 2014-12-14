/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import eu.hayde.box.persistence.ColumnDefinition.ColumnAnnotationForFieldException;
import eu.hayde.box.persistence.ColumnDefinition.ColumnAnnotationRequireDatabaseFieldNameException;
import eu.hayde.box.persistence.ColumnDefinition.VariableTypeHasNoMatchingSQLTypeException;
import eu.hayde.box.persistence.NamedQueries.NamedQueryNotUniqueException;
import eu.hayde.box.persistence.execptions.HaydePersistenceException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author can.senturk
 * @version 2012/06/29
 */
public class TableDefinition {

    //<editor-fold defaultstate="collapsed" desc="Exceptions">
    public static class InternalAccessException extends HaydePersistenceException {

        public InternalAccessException(String message) {
            super(message);
        }
    }

    public class EntityAnnotationMissingException extends HaydePersistenceException {

        public EntityAnnotationMissingException(String msg) {
            super(msg);
        }
    }

    public class TableAnnotationMissingException extends HaydePersistenceException {

        public TableAnnotationMissingException(String msg) {
            super(msg);
        }
    }

    public static class ColumnAnnotationExpectedException extends HaydePersistenceException {

        public ColumnAnnotationExpectedException(String string) {
            super(string);
        }
    }

    public static class MoreThanOneIDFieldInTableException extends HaydePersistenceException {

        public MoreThanOneIDFieldInTableException(String string) {
            super(string);
        }
    }

    public static class IdAnnotationMissingException extends HaydePersistenceException {

        public IdAnnotationMissingException(String string) {
            super(string);
        }
    }
    //</editor-fold>
    private String tableName;
    private boolean genericTable;
    private Class object = null;
    private String schema = null;
    private String catalog = null;
    private String idField = null;
    private HashMap<String, ColumnDefinition> columns = new HashMap<String, ColumnDefinition>();
    private NamedQueries namedQueries = new NamedQueries();

    public TableDefinition(Class obj) throws EntityAnnotationMissingException,
            TableAnnotationMissingException,
            ColumnAnnotationExpectedException,
            ColumnAnnotationRequireDatabaseFieldNameException,
            VariableTypeHasNoMatchingSQLTypeException,
            MoreThanOneIDFieldInTableException,
            IdAnnotationMissingException,
            NamedQueryNotUniqueException {

        object = obj;
        this._loadTableHeader(obj);

        this._loadColumns(obj);

    }

    public boolean isGenericTable() {
        return genericTable;
    }

    public void setGenericTable(boolean genericTable) {
        this.genericTable = genericTable;
    }

    public Class getEntityClass() {
        return object;
    }

    public void setID(Object obj, Object idValue) throws InternalAccessException {
        ColumnDefinition idColumn = this.getIDColumn();
        Field currentObjectField;
        try {
            currentObjectField = obj.getClass().getDeclaredField(idColumn.getJavaName());
            currentObjectField.setAccessible(true);
            currentObjectField.set(obj, idValue);
        } catch (IllegalAccessException ex) {
            throw new InternalAccessException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new InternalAccessException(ex.getMessage());
        } catch (NoSuchFieldException ex) {
            throw new InternalAccessException(ex.getMessage());
        } catch (SecurityException ex) {
            throw new InternalAccessException(ex.getMessage());
        }

    }

    public NamedQueries getNamedQueries() {
        return namedQueries;
    }

    public String getSQLLoad(Object obj) throws InternalAccessException {
        Collection<ColumnDefinition> objectDefinition = _fillObjectToColumnDefinition(obj);

        return this._createSelectSQL(objectDefinition);
    }

    public String getSQLDelete(Object obj) throws InternalAccessException {
        Collection<ColumnDefinition> objectDefinition = _fillObjectToColumnDefinition(obj);
        return this._createDeleteSQL(objectDefinition);
    }

    public String getSQLSave(Object obj) throws InternalAccessException {
        String returnValue = null;
        Collection<ColumnDefinition> objectDefinition = _fillObjectToColumnDefinition(obj);
        ColumnDefinition idColumn = getIDColumn();

        if (idColumn.getValue() == null
                || idColumn.getValue().toString().equals("")
                || idColumn.getValue().toString().equals("0")) {
            // it is an insert
            returnValue = _createInsertSQL(objectDefinition);


        } else {
            // it is an update
            returnValue = _createUpdateSQL(objectDefinition);
        }

        // first fill all values to the columns
        return returnValue;

    }

    /**
     * this function will fill the objects value to the column definitions this
     * way we will receive
     *
     * @param obj
     */
    private Collection<ColumnDefinition> _fillObjectToColumnDefinition(Object obj) throws InternalAccessException {
        Collection<ColumnDefinition> returnValue = columns.values();
        Iterator<ColumnDefinition> it = returnValue.iterator();

        // now fill the values of that stuff!
        while (it.hasNext()) {
            try {
                ColumnDefinition currentColumn = it.next();
                Field currentObjectField = obj.getClass().getDeclaredField(currentColumn.getJavaName());

                currentObjectField.setAccessible(true);
                currentColumn.setValue(currentObjectField.get(obj));
            } catch (IllegalArgumentException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (NoSuchFieldException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (SecurityException ex) {
                throw new InternalAccessException(ex.getMessage());
            }
        }

        return returnValue;
    }

    public ColumnDefinition getIDColumn() {
        return columns.get(idField);
    }

    public String getSQLAll() {
        return "SELECT * `" + tableName + "`";
    }

    /**
     * does load all the values of the resultset into the object
     *
     * @param obj
     * @param rset
     */
    public void loadValuesFromResultSet(Object obj, ResultSet rset) throws InternalAccessException, SQLException {
        Iterator<ColumnDefinition> it = columns.values().iterator();

        // now fill the values of that stuff!
        while (it.hasNext()) {
            try {
                ColumnDefinition currentColumn = it.next();
                Field currentObjectField = obj.getClass().getDeclaredField(currentColumn.getJavaName());
                Object currentObjectValue = null;
                boolean fieldFound = false;
                try {
                    currentObjectValue = rset.getObject(currentColumn.getDbName());
                    fieldFound = true;
                } catch (Exception e) {
                    // no field existing
                }

                if (fieldFound) {
                    // value upsize check:
                    if (currentObjectValue != null) {

                        // value upsize
                        if (currentObjectValue.getClass() == Integer.class
                                && currentColumn.getValueType() == Long.class) {
                            // convert integer to long
                            currentObjectValue = new Long(((Integer) currentObjectValue));
                        }

                        // value downsize (in general we need to do that, if we use
                        // functions like hyd.sum(..) where the CAST command will not
                        // be able to use a specific type
                        // Long => Integer
                        if (currentObjectValue.getClass() == Long.class
                                && currentColumn.getValueType() == Integer.class) {
                            currentObjectValue = ((Long) currentObjectValue).intValue();
                        }
                        // BigDecimal => Double
                        if (currentObjectValue.getClass() == java.math.BigDecimal.class
                                && currentColumn.getValueType() == Double.class) {
                            currentObjectValue = ((java.math.BigDecimal) currentObjectValue).doubleValue();
                        }


                    }
                    currentObjectField.setAccessible(true);
                    currentObjectField.set(obj, currentObjectValue);
                }
            } catch (IllegalArgumentException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (NoSuchFieldException ex) {
                throw new InternalAccessException(ex.getMessage());
            } catch (SecurityException ex) {
                throw new InternalAccessException(ex.getMessage());
            }
        }
    }

    /**
     * this function validates the structure of a given MetaData with the table
     * itself.
     *
     * it will check, if the fields are existing in the database and if the
     * database may have more then the current fields
     *
     * @param rsmd
     * @return
     */
    public String validate(ResultSetMetaData rsmd) throws SQLException {
        String returnValue = "";
        ColumnDefinition[] fields = new ColumnDefinition[columns.size() + 1];

        int i = 1;
        for (Map.Entry<String, ColumnDefinition> column : columns.entrySet()) {
            fields[i++] = new ColumnDefinition(column.getValue());
        }

        // now search for the MetaData in the DB
        for (int mdLoop = 1; mdLoop <= rsmd.getColumnCount(); mdLoop++) {
            boolean found = false;
            for (int cdLoop = 1; cdLoop < fields.length; cdLoop++) {
                if (rsmd.getColumnName(mdLoop).equals(fields[cdLoop].getDbName())) {
                    // we found that column in the fields
                    found = true;
                }
            }

            /*
             * if we did not find that field in the object, we need to report it
             */
            if (!found) {
                returnValue += "Warning: Field not in Java-Class: Table '" + this.tableName + "' DBField '" + rsmd.getColumnName(mdLoop) + "'\n";
            }
        }

        // and now, seach the columns in the java
        for (int cdLoop = 1; cdLoop < fields.length; cdLoop++) {

            boolean found = false;
            for (int mdLoop = 1; mdLoop <= rsmd.getColumnCount(); mdLoop++) {
                if (fields[cdLoop].getDbName().equals(rsmd.getColumnName(mdLoop))) {
                    // we found that column in the fields
                    found = true;
                }
            }

            /*
             * if we did not find that field in the object, we need to report it
             */
            if (!found) {
                returnValue += "Error: Field missing in Table: Table '" + this.tableName + "' requires DBField '" + fields[cdLoop].getDbName() + "'\n";
            }
        }
        return returnValue;
    }

    private String _createSelectSQL(Collection<ColumnDefinition> par) {
        String returnValue = null;
        Iterator<ColumnDefinition> it = par.iterator();

        while (it.hasNext()) {
            ColumnDefinition curColumn = it.next();

            if (curColumn.isKey()) {

                returnValue = "SELECT * FROM `" + this.tableName + "` WHERE `"
                        + curColumn.getDbName() + "`="
                        + curColumn.quotedValue();
            }
        }

        return returnValue;
    }

    public String getSQLDummy() {
        return "SELECT * FROM `" + this.tableName + "` WHERE 1=2";
    }

    private String _createDeleteSQL(Collection<ColumnDefinition> par) {
        String returnValue = null;
        Iterator<ColumnDefinition> it = par.iterator();

        while (it.hasNext()) {
            ColumnDefinition curColumn = it.next();

            if (curColumn.isKey()) {

                returnValue = "DELETE FROM `" + this.tableName + "` WHERE `"
                        + curColumn.getDbName() + "`="
                        + curColumn.quotedValue() + "";
            }
        }

        return returnValue;
    }

    private String _createInsertSQL(Collection<ColumnDefinition> par) {
        String returnValue = "";
        StringBuilder ValueIndex = new StringBuilder();
        StringBuilder ValueList = new StringBuilder();
        int elementCounter = 0;
        Iterator<ColumnDefinition> it = par.iterator();

        while (it.hasNext()) {
            ColumnDefinition curColumn = it.next();

            if (!curColumn.isKey()) {
                if (elementCounter > 0) {
                    ValueIndex.append(", ");
                    ValueList.append(", ");
                }

                ValueIndex.append("`").append(curColumn.getDbName()).append("`");
                ValueList.append(curColumn.quotedValue());
                elementCounter++;
            }
        }

        returnValue = "INSERT INTO `" + this.tableName + "` ( " + ValueIndex.toString() + " ) VALUES ( " + ValueList.toString() + " )";

        return returnValue;
    }

    private String _createUpdateSQL(Collection<ColumnDefinition> par) {
        String returnValue = null;
        String whereStatement = "";
        StringBuilder ValueList = new StringBuilder();
        Iterator<ColumnDefinition> it = par.iterator();
        int elementCount = 0;

        while (it.hasNext()) {
            ColumnDefinition curColumn = it.next();

            if (curColumn.isKey()) {

                whereStatement = " WHERE `" + curColumn.getDbName() + "` = " + curColumn.quotedValue() + "";

            } else {

                if (elementCount > 0) {
                    ValueList.append(", ");

                }
                ValueList.append("`").append(curColumn.getDbName()).append("` = ");
                ValueList.append(curColumn.quotedValue());
                elementCount++;
            }
        }

        returnValue = "UPDATE `" + tableName + "` SET " + ValueList.toString() + " " + whereStatement;

        return returnValue;
    }

    private void _loadTableHeader(Class obj) throws EntityAnnotationMissingException, TableAnnotationMissingException, NamedQueryNotUniqueException {
        // check the table header
        if (!obj.isAnnotationPresent(eu.hayde.box.persistence.annotation.Entity.class)) {
            throw new EntityAnnotationMissingException("@Entity Annotation expected right before the class definition for : " + obj.getCanonicalName());
        } else if (!obj.isAnnotationPresent(eu.hayde.box.persistence.annotation.Table.class)) {
            throw new TableAnnotationMissingException("@Table Annotation expected right before the class definition for : " + obj.getCanonicalName());
        }

        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0: {// get the table infos
                    eu.hayde.box.persistence.annotation.Table annotationTable = (eu.hayde.box.persistence.annotation.Table) obj.getAnnotation(eu.hayde.box.persistence.annotation.Table.class);
                    tableName = annotationTable.name();
                    schema = annotationTable.schema();
                    catalog = annotationTable.catalog();
                }
                break;

                case 1:
                    if (obj.isAnnotationPresent(eu.hayde.box.persistence.annotation.NamedQueries.class)) {
                        // named queries existing
                        eu.hayde.box.persistence.annotation.NamedQueries nQueries = (eu.hayde.box.persistence.annotation.NamedQueries) obj.getAnnotation(eu.hayde.box.persistence.annotation.NamedQueries.class);
                        eu.hayde.box.persistence.annotation.NamedQuery[] namedQuery = nQueries.value();

                        for (int j = 0; j < namedQuery.length; j++) {
                            this.namedQueries.put(this, namedQuery[j].name(), namedQuery[j].query());
                        }

                    }
                    break;

            }
        }
    }

    private void _loadColumns(Class obj) throws ColumnAnnotationExpectedException, ColumnAnnotationRequireDatabaseFieldNameException, VariableTypeHasNoMatchingSQLTypeException, MoreThanOneIDFieldInTableException, IdAnnotationMissingException {

        // load all fields
        Field[] fields = obj.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field currentField = fields[i];

            // if the column annotation or join column annotation is existing only!
            if (currentField.isAnnotationPresent(eu.hayde.box.persistence.annotation.Column.class)
                    || currentField.isAnnotationPresent(eu.hayde.box.persistence.annotation.JoinColumn.class)) {
                ColumnDefinition columnDefinition = null;

                try {
                    columnDefinition = new ColumnDefinition(currentField);
                } catch (ColumnAnnotationForFieldException ex) {
                    // already covered with the upper if clause!
                }

                if (columnDefinition != null) {
                    if (columnDefinition.isKey()) {
                        if (idField != null) {
                            // there is already a ID field definied!
                            throw new MoreThanOneIDFieldInTableException("The Class " + object.getCanonicalName() + " has annotated the @ID at least twice. At variable " + idField + " and " + columnDefinition.getJavaName() + ". @ID has to be unique!");
                        } else {
                            idField = columnDefinition.getJavaName();
                        }
                    }

                    // and now add it to the hashMap
                    columns.put(columnDefinition.getJavaName(), columnDefinition);
                }
            }
        }

        // check if there are columns at all?!
        if (columns.isEmpty()) {
            throw new ColumnAnnotationExpectedException("You need to bind the variables with the @Column Annotation to the database fields! (Class: " + object.getCanonicalName() + ")");

        } else if (idField == null) {
            // there is no id field, which is not allowed in this context
            throw new IdAnnotationMissingException("The Class " + object.getCanonicalName() + " has to define a @Id annotation to a variable, that is the corrosponding to the UniqueKey of the Table!");
        }
    }

    public String prepareQuery(String sqlStatement) {

        /**
         * here we will store the return of this function, which should be a
         * plain and standard sql query.
         */
        String returnValue = sqlStatement;

        /**
         * now check, if that is a persistence query or not.
         *
         * per definition we will say, that everything like : "SELECT a FROM
         * User a" is a hibernate sql query and "SELECT * FROM user" is already
         * a standard sql query.
         */
        boolean alreadySQL = false;

        // 1. replace the class name with the table name
        returnValue = returnValue.replaceAll(this.getEntityClass().getSimpleName() + "(\\s)(\\w)", "`" + this.tableName + "`");

        // 2. replace all sum( field ) with the constructor CAST( sum(field) AS fieldOutput ) as field
        returnValue = returnValue.replaceAll("([^\\w])hyd.sum\\(([^\\)]+)\\)([^\\w]|$)", "$1CAST( sum( $2 ) AS fieldtype:$2 ) AS $2$3");

        // 3. replace all field names with the dbField names
        {
            Iterator<ColumnDefinition> tmpColumns = columns.values().iterator();
            while (tmpColumns.hasNext()) {
                ColumnDefinition theCurrentColumn = tmpColumns.next();

                returnValue = returnValue.replaceAll("\\s+(AS|as|As)\\s+fieldtype:(\\w+\\." + theCurrentColumn.getJavaName() + ")([^\\w])", " AS " + theCurrentColumn.getCastType() + "$3");

                returnValue = returnValue.replaceAll("(\\w+)(\\.)(" + theCurrentColumn.getJavaName() + ")([^\\w]|$)",
                        "`" + theCurrentColumn.getDbName() + "`$4");

            }
        }

        // 3. put every :xx Parameter into quotes.
        returnValue = returnValue.replaceAll(":(\\w+)", "':$1'");

        // 4. finally we have to place the SELECT u FROM into a real
        //  SELECT * FROM
        returnValue = returnValue.replaceAll("(?i)SELECT(\\s+)(\\w+)(\\s+)FROM", "SELECT * FROM");


        return returnValue;
    }
}
