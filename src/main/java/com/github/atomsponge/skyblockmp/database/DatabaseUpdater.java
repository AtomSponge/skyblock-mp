package com.github.atomsponge.skyblockmp.database;

import com.github.atomsponge.skyblockmp.database.transaction.Transaction;
import com.github.atomsponge.skyblockmp.database.transaction.TransactionCallback;
import com.github.atomsponge.skyblockmp.util.ConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor
class DatabaseUpdater {
    private static final int CURRENT_DATABASE_VERSION = 2;

    private final DatabaseManager databaseManager;

    void updateDatabase() throws Exception {
        File configFile = new File("skyblock", "database.conf");
        Config databaseConfig = ConfigUtils.load(getClass().getClassLoader(), "database.conf", configFile);

        int localVersion = databaseConfig.getInt("version");
        while (localVersion < CURRENT_DATABASE_VERSION) {
            databaseManager.getMod().getLogger().info("Updating database to version " + localVersion + 1);
            executeUpdateScript(localVersion + 1);
            localVersion++;
        }

        databaseConfig = databaseConfig.withValue("version", ConfigValueFactory.fromAnyRef(localVersion));
        ConfigUtils.save(databaseConfig, configFile);
    }

    private void executeUpdateScript(final int version) throws Exception {
        String path = String.format("/database-updates/version-%s.sql", version);
        List<String> lines;
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            lines = IOUtils.readLines(inputStream, Charsets.UTF_8);
        }

        boolean blockComment = false;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
            String line = iterator.next().trim();

            if (blockComment) {
                iterator.remove();
                if (line.endsWith("*/")) {
                    blockComment = false;
                }
                continue;
            }

            if (line.startsWith("/*")) {
                blockComment = true;
                iterator.remove();
            } else if (line.isEmpty() || line.startsWith("//") || line.startsWith("--")) {
                iterator.remove();
            }
        }

        final String[] statements = String.join(" ", lines).trim().replaceAll(" +", " ").replaceAll("\n", " ").split(";");

        databaseManager.createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (Statement statement = connection.createStatement()) {
                    for (String statementString : statements) {
                        statement.addBatch(statementString);
                    }
                    statement.executeBatch();
                }
            }
        }, new TransactionCallback() {
            @Override
            public void success() {
                databaseManager.getMod().getLogger().info("Successfully updated database to version " + version);
            }

            @Override
            public void failure(Throwable throwable) {
                throw new RuntimeException("Failed to update database to version " + version, throwable);
            }
        });
    }
}
