package com.github.atomsponge.skyblockmp.database;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.database.transaction.Transaction;
import com.github.atomsponge.skyblockmp.database.transaction.TransactionCallback;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor
public class DatabaseManager {
    private static final int CURRENT_DATABASE_VERSION = 1;

    @Getter(AccessLevel.PACKAGE)
    private final SkyblockMp mod;
    @Getter
    private ExecutorService executorService;
    private HikariDataSource dataSource;
    @Getter
    private boolean updated = false;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public HikariDataSource getHandle() {
        return dataSource;
    }

    public DatabaseTransaction createTransaction(Transaction transaction) {
        return createTransaction(transaction, null);
    }

    public DatabaseTransaction createTransaction(Transaction transaction, TransactionCallback callback) {
        return createTransaction(transaction, callback, true);
    }

    public DatabaseTransaction createTransaction(Transaction transaction, TransactionCallback callback, boolean autoClose) {
        DatabaseTransaction context = new DatabaseTransaction(this, transaction, callback, autoClose);
        context.execute();
        return context;
    }

    public void initialize() throws Exception {
        mod.getLogger().info("Initializing database connection pool");
        if (mod.getConfig().getString("database.connection.password").trim().isEmpty()) {
            mod.getLogger().warn("Database password is empty!");
        }

        dataSource = new HikariDataSource(configure());
        executorService = Executors.newFixedThreadPool(mod.getConfig().getInt("database.executor-threads"), new ThreadFactoryBuilder().setNameFormat("SkyblockMp Database Thread #%1$d").build());

        DatabaseUpdater updater = new DatabaseUpdater(this);
        updater.updateDatabase();
    }

    public void shutdown() {
        mod.getLogger().info("Shutting down database connection pool");
        if (dataSource != null) {
            dataSource.shutdown();
        }
    }

    private HikariConfig configure() {
        Config config = mod.getConfig();
        HikariConfig hikariConfig = new HikariConfig();

        // General settings
        hikariConfig.setDriverClassName(config.getString("database.driver-class-name"));
        hikariConfig.setJdbcUrl(config.getString("database.connection.jdbc-url"));
        hikariConfig.setUsername(config.getString("database.connection.username"));
        hikariConfig.setPassword(config.getString("database.connection.password"));

        // Pool-related settings
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size"));
        hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle"));

        return hikariConfig;
    }
}
