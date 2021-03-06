package com.github.atomsponge.skyblockmp.database;

import com.github.atomsponge.skyblockmp.database.transaction.Transaction;
import com.github.atomsponge.skyblockmp.database.transaction.TransactionCallback;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatabaseTransaction {
    private final DatabaseManager databaseManager;
    private final Transaction transaction;
    private final TransactionCallback callback;

    @Getter
    private final boolean autoClose;
    private boolean successful;

    public boolean wasSuccessful() {
        // Unfortunately, we can't rename lombok getters :(
        return successful;
    }

    public void fail() {
        this.successful = false;
    }

    void execute() {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            transaction.execute(this, connection);
            connection.commit();
            if (autoClose) {
                connection.close();
            }
        } catch (Throwable throwable) {
            fail();
            databaseManager.getMod().getLogger().log(Level.ERROR, "An error occurred while executing database transaction", throwable);

            if (connection != null) {
                databaseManager.getMod().getLogger().info("Trying to roll back changes...");
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    databaseManager.getMod().getLogger().log(Level.ERROR, "Failed to roll back changes", e1);
                }
            }

            if (callback != null) {
                callback.failure(throwable);
            }
        } finally {
            if (successful && callback != null) {
                callback.success();
            }
        }
    }
}
