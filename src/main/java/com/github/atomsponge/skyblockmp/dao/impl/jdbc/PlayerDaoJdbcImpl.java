package com.github.atomsponge.skyblockmp.dao.impl.jdbc;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.PlayerDao;
import com.github.atomsponge.skyblockmp.dao.impl.DaoException;
import com.github.atomsponge.skyblockmp.dao.impl.DaoImpl;
import com.github.atomsponge.skyblockmp.database.DatabaseTransaction;
import com.github.atomsponge.skyblockmp.database.transaction.Transaction;
import com.github.atomsponge.skyblockmp.database.transaction.TransactionCallback;
import com.github.atomsponge.skyblockmp.model.Player;
import com.github.atomsponge.skyblockmp.util.UuidUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author AtomSponge
 */
public class PlayerDaoJdbcImpl extends DaoImpl implements PlayerDao {
    private static final String SQL_SELECT_BY_UUID = "SELECT id, last_username, default_island FROM player WHERE uuid = ?";
    private static final String SQL_INSERT = "INSERT INTO player (uuid, last_username) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE player SET uuid = ?, last_username = ?, default_island = ? WHERE id = ?";

    public PlayerDaoJdbcImpl(SkyblockMp mod) {
        super(mod);
    }

    @Override
    public List<Player> findByUuid(final UUID uuid) throws DaoException {
        final List<Player> players = new ArrayList<>();
        getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_UUID)) {
                    statement.setBytes(1, UuidUtils.getBytesFromUuid(uuid));
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Player player = new Player(resultSet.getInt(1), uuid, resultSet.getString(2), resultSet.getInt(3));
                            players.add(player);
                        }
                    }
                }
            }
        }, new TransactionCallback() {
            @Override
            public void failure(Throwable throwable) {
                throw new DaoException(throwable);
            }
        });
        return players;
    }

    @Override
    public boolean insertPlayer(final Player player) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setBytes(1, UuidUtils.getBytesFromUuid(player.getUuid()));
                    statement.setString(2, player.getLastUsername());
                    statement.executeUpdate();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        generatedKeys.next();
                        player.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }, new TransactionCallback() {
            @Override
            public void failure(Throwable throwable) {
                throw new DaoException(throwable);
            }
        });
        return transaction.wasSuccessful();
    }

    @Override
    public boolean updatePlayer(final Player player) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
                    statement.setBytes(1, UuidUtils.getBytesFromUuid(player.getUuid()));
                    statement.setString(2, player.getLastUsername());
                    statement.setInt(3, player.getDefaultIsland());
                    statement.setInt(4, player.getId());
                    statement.executeUpdate();
                }
            }
        }, new TransactionCallback() {
            @Override
            public void failure(Throwable throwable) {
                throw new DaoException(throwable);
            }
        });
        return transaction.wasSuccessful();
    }
}
