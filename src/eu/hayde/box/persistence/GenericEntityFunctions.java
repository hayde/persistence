/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.persistence;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericEntityFunctions {

    public <T> T save(StatelessSession session) {
        T returnValue = null;

        try {
            returnValue = (T) session.save(this);
        } catch (TableDefinition.InternalAccessException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceManager.ClassUnknownToPersistence ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.close();
        }

        return returnValue;
    }

    public <T> T load(StatelessSession session, long ID) {
        T returnValue = null;
        try {
            returnValue = (T) session.load(this.getClass(), ID);// load( theClass.getClass(), ID);
        } catch (TableDefinition.InternalAccessException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceManager.ClassUnknownToPersistence ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    public <T> List<T> list(StatelessSession session) {
        List<T> returnValue = null;
        try {

            // IMPORTANT
            // check your class for the following NamedQuery, which must be set manually!
            // {yourClassName}.findAll    SELECT x FROM {yourTableName} x
            Query query = session.getNamedQuery(this.getClass().getSimpleName() + ".findAll");
            returnValue = query.list();

        } catch (TableDefinition.InternalAccessException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (StatelessSession.NamedQueryUnknownException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    public <T> boolean delete(StatelessSession session) {
        boolean returnValue = false;
        try {
            session.delete(this);
            returnValue = true;

        } catch (TableDefinition.InternalAccessException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceManager.ClassUnknownToPersistence ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenericEntityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return returnValue;
    }

    public static <T> T loadNamedQuery(StatelessSession session, String queryName, HashMap<String, Object> nameValuePairs) {
        T returnValue = null;
        try {
            Query query = session.getNamedQuery(queryName);
            for (String key : nameValuePairs.keySet()) {
                query.setParameter(key, nameValuePairs.get(key));
            }

            returnValue = query.uniqueResult();
        } catch (Exception ex) {
            Logger.getLogger(queryName).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    public static <T> List<T> listNamedQuery(StatelessSession session, String queryName, HashMap<String, Object> nameValuePairs) {
        List<T> returnValue = null;
        try {
            Query query = session.getNamedQuery(queryName);
            for (String key : nameValuePairs.keySet()) {
                query.setParameter(key, nameValuePairs.get(key));
            }

            returnValue = query.list();
        } catch (Exception ex) {
            Logger.getLogger(queryName).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }
}
