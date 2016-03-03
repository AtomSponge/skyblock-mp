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
    @Getter
    private boolean successful;

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
        } catch (SQLException e) {
            fail();
            databaseManager.getMod().getLogger().log(Level.ERROR, "An error occurred while executing database transaction", e);

            if (connection != null) {
                databaseManager.getMod().getLogger().info("Trying to roll back changes...");
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    databaseManager.getMod().getLogger().log(Level.ERROR, "Failed to roll back changes", e1);
                }
            }

            if (callback != null) {
                callback.failure(e);
            }
        } finally {
            if (successful && callback != null) {
                callback.success();
            }
        }
    }

    public void fail() {
        this.successful = false;
    }
}
