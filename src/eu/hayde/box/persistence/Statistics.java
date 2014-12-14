/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author can
 */
public class Statistics {

    private long CloseStatementCount = 0;
    private long ConnectCount = 0;					//          Get the global number of connections asked by the sessions (the actual number of connections used may be much smaller depending whether you use a connection pool or not)
    private long EntityDeleteCount = 0;				//          Get global number of entity deletes
    private long EntityFetchCount = 0;				//         Get global number of entity fetchs
    private long EntityInsertCount = 0;				//          Get global number of entity inserts
    private long EntityLoadCount = 0;				//          Get global number of entity loads
    private long EntityUpdateCount = 0;				//          Get global number of entity updates
    private long FlushCount = 0;					//          Get the global number of flush executed by sessions (either implicit or explicit)
    private long OptimisticFailureCount = 0;		//          The number of StaleObjectStateExceptions that occurred
    private long PrepareStatementCount = 0;			//          The number of prepared statements that were acquired
    private long QueryCacheHitCount = 0;			//          Get the global number of cached queries successfully retrieved from cache
    private long QueryCacheMissCount = 0;			//          Get the global number of cached queries *not* found in cache
    private long QueryCachePutCount = 0;			//          Get the global number of cacheable queries put in cache
    private long QueryExecutionCount = 0;			//          Get global number of executed queries
    private long QueryExecutionMaxTime = 0;			//          Get the time in milliseconds of the slowest query.
    private long SessionCloseCount = 0;				//          Global number of sessions closed
    private long SessionOpenCount = 0;				//          Global number of sessions opened
    private long StartTime = 0;
    private long SuccessfulTransactionCount = 0;	//          The number of transactions we know to have been successful
    private long TransactionCount = 0;				//          The number of transactions we know to have completed
    private long UpdateTimestampsCacheHitCount = 0;	//          Get the global number of timestamps successfully retrieved from cache
    private long UpdateTimestampsCacheMissCount = 0;//          Get the global number of tables for which no update timestamps was *not* found in cache
    private long UpdateTimestampsCachePutCount = 0;	//          Get the global number of timestamps put in cache
    private boolean StatisticsEnabled = false;		//          Are statistics logged
    private String SQLLogFile = null;				//			if statistics is enabled and this value points to a valid location, the each sql statment
    //			will be placed into that file.

    void logSummary() {
//          log in info level the main statistics
    }

    public void setStatisticsEnabled(boolean b) {
        // Enable statistics logs (this is a dynamic parameter)
        StatisticsEnabled = b;
    }

    public void clear() {
        // not implemented now.
    }

    public String[] getEntityNames() {
        return null;
    }

    private String[] getQueries() {
        return null;
    }

    String getQueryExecutionMaxTimeQueryString() {
        //          Get the query string for the slowest query.
        return null;
    }

    public void writeToLogFile(String text) {
        if (StatisticsEnabled) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                        SQLLogFile + "_" + getLogDate() + ".log"), true));
                bw.write(text);
                bw.newLine();
                bw.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean isStatisticsEnabled() {
        return StatisticsEnabled;
    }

    private String getLogDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }

    //<editor-fold defaultstate="collapsed" desc="incrementors!">
    public void incCloseStatementCount() {
        this.CloseStatementCount++;
    }

    public void incConnectCount() {
        this.ConnectCount++;
    }

    public void incEntityDeleteCount() {
        this.EntityDeleteCount++;
    }

    public void incEntityFetchCount() {
        this.EntityFetchCount++;
    }

    public void incEntityInsertCount() {
        this.EntityInsertCount++;
    }

    public void incEntityLoadCount() {
        this.EntityLoadCount++;
    }

    public void incEntityUpdateCount() {
        this.EntityUpdateCount++;
    }

    public void incFlushCount() {
        this.FlushCount++;
    }

    public void incOptimisticFailureCount() {
        this.OptimisticFailureCount++;
    }

    public void incPrepareStatementCount() {
        this.PrepareStatementCount++;
    }

    public void incQueryCacheHitCount() {
        this.QueryCacheHitCount++;
    }

    public void incQueryCacheMissCount() {
        this.QueryCacheMissCount++;
    }

    public void incQueryCachePutCount() {
        this.QueryCachePutCount++;
    }

    public void incQueryExecutionCount() {
        this.QueryExecutionCount++;
    }

    public void incSessionCloseCount() {
        this.SessionCloseCount++;
    }

    public void incSessionOpenCount() {
        this.SessionOpenCount++;
    }

    public void incSuccessfulTransactionCount() {
        this.SuccessfulTransactionCount++;
    }

    public void incTransactionCount() {
        this.TransactionCount++;
    }

    public void incUpdateTimestampsCacheHitCount() {
        this.UpdateTimestampsCacheHitCount++;
    }

    public void incUpdateTimestampsCacheMissCount() {
        this.UpdateTimestampsCacheMissCount++;
    }

    public void incUpdateTimestampsCachePutCount() {
        this.UpdateTimestampsCachePutCount++;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="setters">
    public void setQueryExecutionMaxTime(long QueryExecutionMaxTime) {
        this.QueryExecutionMaxTime = QueryExecutionMaxTime;
    }

    public void setSQLLogFile(String SQLLogFile) {
        this.SQLLogFile = SQLLogFile;

        // clear the *.log stuff to enable the date adding
        if (this.SQLLogFile.endsWith(".log")
                || this.SQLLogFile.endsWith(".LOG")
                || this.SQLLogFile.endsWith(".Log")) {
            this.SQLLogFile = this.SQLLogFile.substring(0, this.SQLLogFile.length() - 4);

        }
    }

    public void setStartTime(long StartTime) {
        this.StartTime = StartTime;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Getters">
    public long getCloseStatementCount() {
        return CloseStatementCount;
    }

    public long getConnectCount() {
        return ConnectCount;
    }

    public long getEntityDeleteCount() {
        return EntityDeleteCount;
    }

    public long getEntityFetchCount() {
        return EntityFetchCount;
    }

    public long getEntityInsertCount() {
        return EntityInsertCount;
    }

    public long getEntityLoadCount() {
        return EntityLoadCount;
    }

    public long getEntityUpdateCount() {
        return EntityUpdateCount;
    }

    public long getFlushCount() {
        return FlushCount;
    }

    public long getOptimisticFailureCount() {
        return OptimisticFailureCount;
    }

    public long getPrepareStatementCount() {
        return PrepareStatementCount;
    }

    public long getQueryCacheHitCount() {
        return QueryCacheHitCount;
    }

    public long getQueryCacheMissCount() {
        return QueryCacheMissCount;
    }

    public long getQueryCachePutCount() {
        return QueryCachePutCount;
    }

    public long getQueryExecutionCount() {
        return QueryExecutionCount;
    }

    public long getQueryExecutionMaxTime() {
        return QueryExecutionMaxTime;
    }

    public String getSQLLogFile() {
        return SQLLogFile;
    }

    public long getSessionCloseCount() {
        return SessionCloseCount;
    }

    public long getSessionOpenCount() {
        return SessionOpenCount;
    }

    public long getStartTime() {
        return StartTime;
    }

    public long getSuccessfulTransactionCount() {
        return SuccessfulTransactionCount;
    }

    public long getTransactionCount() {
        return TransactionCount;
    }

    public long getUpdateTimestampsCacheHitCount() {
        return UpdateTimestampsCacheHitCount;
    }

    public long getUpdateTimestampsCacheMissCount() {
        return UpdateTimestampsCacheMissCount;
    }

    public long getUpdateTimestampsCachePutCount() {
        return UpdateTimestampsCachePutCount;
    }
    //</editor-fold>
}
