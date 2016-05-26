package com.github.atomsponge.skyblockmp.dao;

import com.github.atomsponge.skyblockmp.dao.impl.DaoException;
import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;

import java.util.List;

/**
 * @author AtomSponge
 */
public interface IslandDao extends Dao {
    List<Island> findAll() throws DaoException;

    Position findLatestPosition() throws DaoException;

    boolean insertIsland(Island island) throws DaoException;

    boolean updateIsland(Island island) throws DaoException;

    // Members

    List<Player> findMembersByIsland(Island island) throws DaoException;

    boolean insertIslandMember(Island island, Player member) throws DaoException;

    boolean removeIslandMember(Island island, Player member) throws DaoException;
}
