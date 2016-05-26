package com.github.atomsponge.skyblockmp.dao.impl.jdbc;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.IslandDao;
import com.github.atomsponge.skyblockmp.dao.impl.DaoException;
import com.github.atomsponge.skyblockmp.dao.impl.DaoImpl;
import com.github.atomsponge.skyblockmp.database.DatabaseTransaction;
import com.github.atomsponge.skyblockmp.database.transaction.Transaction;
import com.github.atomsponge.skyblockmp.database.transaction.TransactionCallback;
import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;
import com.github.atomsponge.skyblockmp.util.UuidUtils;
import com.sk89q.worldedit.Vector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AtomSponge
 */
public class IslandDaoJdbcImpl extends DaoImpl implements IslandDao {
    private static final String SQL_SELECT_ALL = "SELECT id, pos_x, pos_z, owner, offset_x, offset_y, offset_z FROM island";
    private static final String SQL_SELECT_LATEST_POS = "SELECT pos_x, pos_z FROM island ORDER BY id DESC LIMIT 1";
    private static final String SQL_UPDATE = "UPDATE island SET pos_x = ?, pos_z = ?, offset_x = ?, offset_y = ?, offset_z = ?, owner = ? WHERE id = ?";
    private static final String SQL_INSERT = "INSERT INTO island (owner, pos_x, pos_z, offset_x, offset_y, offset_z) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_MEMBERS_BY_ISLAND = "SELECT id, uuid, last_username, default_island FROM island_member JOIN player ON player.id = island_member.player_id WHERE island_id = ?";
    private static final String SQL_INSERT_MEMBER = "INSERT INTO island_member (island_id, player_id) VALUES (?, ?)";
    private static final String SQL_DELETE_MEMBER = "DELETE FROM island_member WHERE island_id = ? AND player_id = ?";

    public IslandDaoJdbcImpl(SkyblockMp mod) {
        super(mod);
    }

    @Override
    public List<Island> findAll() throws DaoException {
        final List<Island> islands = new ArrayList<>();
        getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Island island = new Island(
                                resultSet.getInt(1),
                                new Position(getMod().getGridManager(), resultSet.getInt(2), resultSet.getInt(3)),
                                resultSet.getInt(4),
                                new Vector(resultSet.getDouble(5), resultSet.getDouble(6), resultSet.getDouble(7))
                        );
                        islands.add(island);
                    }
                }
            }
        }, new TransactionCallback() {
            @Override
            public void failure(Throwable throwable) {
                throw new DaoException(throwable);
            }
        });
        return islands;
    }

    @Override
    public Position findLatestPosition() throws DaoException {
        final AtomicReference<Position> latestPosition = new AtomicReference<>();
        getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_LATEST_POS);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        latestPosition.set(new Position(getMod().getGridManager(), resultSet.getInt(1), resultSet.getInt(2)));
                    }
                }
            }
        }, new TransactionCallback() {
            @Override
            public void failure(Throwable throwable) {
                throw new DaoException(throwable);
            }
        });
        return latestPosition.get();
    }

    @Override
    public boolean insertIsland(final Island island) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, island.getOwner());
                    statement.setInt(2, island.getPosition().getX());
                    statement.setInt(3, island.getPosition().getZ());
                    statement.setDouble(4, island.getSpawnOffset().getX());
                    statement.setDouble(5, island.getSpawnOffset().getY());
                    statement.setDouble(6, island.getSpawnOffset().getZ());
                    statement.executeUpdate();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        generatedKeys.next();
                        island.setId(generatedKeys.getInt(1));
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
    public boolean updateIsland(final Island island) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
                    statement.setInt(1, island.getPosition().getX());
                    statement.setInt(2, island.getPosition().getZ());
                    statement.setDouble(3, island.getSpawnOffset().getX());
                    statement.setDouble(4, island.getSpawnOffset().getY());
                    statement.setDouble(5, island.getSpawnOffset().getZ());
                    statement.setInt(6, island.getOwner());
                    statement.setInt(7, island.getId());
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

    @Override
    public List<Player> findMembersByIsland(final Island island) throws DaoException {
        final List<Player> players = new ArrayList<>();
        getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_MEMBERS_BY_ISLAND)) {
                    statement.setInt(1, island.getId());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Player player = new Player(resultSet.getInt(1), UuidUtils.getUuidFromBytes(resultSet.getBytes(2)), resultSet.getString(3), resultSet.getInt(4));
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
    public boolean insertIslandMember(final Island island, final Player member) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MEMBER)) {
                    statement.setInt(1, island.getId());
                    statement.setInt(2, member.getId());
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

    @Override
    public boolean removeIslandMember(final Island island, final Player member) throws DaoException {
        DatabaseTransaction transaction = getDatabaseManager().createTransaction(new Transaction() {
            @Override
            public void execute(DatabaseTransaction context, Connection connection) throws SQLException {
                try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_MEMBER)) {
                    statement.setInt(1, island.getId());
                    statement.setInt(2, member.getId());
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
