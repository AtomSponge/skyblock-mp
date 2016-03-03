package com.github.atomsponge.skyblockmp.database.transaction;

import com.github.atomsponge.skyblockmp.database.DatabaseTransaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author AtomSponge
 */
public interface Transaction {
    void execute(DatabaseTransaction context, Connection connection) throws SQLException;
}
