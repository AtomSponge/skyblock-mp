package com.github.atomsponge.skyblockmp.player;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.PlayerDao;
import com.github.atomsponge.skyblockmp.grid.GridManager;
import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author AtomSponge
 */
public class PlayerManager {
    @Getter(AccessLevel.PACKAGE)
    private final SkyblockMp mod;
    private final Map<UUID, Player> players = new HashMap<>();
    private final PlayerDao playerDao;

    public PlayerManager(SkyblockMp mod) {
        this.mod = mod;
        playerDao = (PlayerDao) mod.getDaoManager().getDao(Player.class);
        FMLCommonHandler.instance().bus().register(this);
    }

    public Player getPlayerByUuid(UUID uuid) {
        return players.get(uuid);
    }

    public Player getPlayerByEntity(EntityPlayer entity) {
        return getPlayerByUuid(entity.getUniqueID());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        mod.getDatabaseManager().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                loadPlayer(event.player);
                checkDefaultIsland(event.player);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        players.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (mod.getConfig().getBoolean("general.respawn-on-home-island")) {
            Island homeIsland = mod.getGridManager().getHomeIsland(getPlayerByEntity(event.player));
            mod.getGridManager().teleportPlayerToIsland(event.player, homeIsland);
        }
    }

    private void loadPlayer(final EntityPlayer player) {
        List<Player> foundPlayers = playerDao.findByUuid(player.getUniqueID());

        if (foundPlayers.size() == 0) {
            Player playerModel = new Player(-1, player.getUniqueID(), player.getDisplayName(), -1);
            playerDao.insertPlayer(playerModel);
            players.put(player.getUniqueID(), playerModel);
        } else if (foundPlayers.size() > 0) {
            if (foundPlayers.size() != 1) {
                mod.getLogger().warn("Found more than one player in database for UUID " + player.getUniqueID().toString());
            }

            Player playerModel = foundPlayers.get(0);
            if (!playerModel.getLastUsername().equals(player.getDisplayName())) {
                mod.getLogger().info(String.format("Last username of player %s (%s) changed, updating database...", player.getDisplayName(), player.getUniqueID()));
                playerModel.setLastUsername(player.getDisplayName());
                playerDao.updatePlayer(playerModel);
            }
            players.put(player.getUniqueID(), playerModel);
        }
    }

    private void checkDefaultIsland(final EntityPlayer player) {
        Player playerModel = players.get(player.getUniqueID());
        if (mod.getConfig().getBoolean("general.create-island-on-join") && playerModel.getDefaultIsland() == -1) {
            mod.getScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    mod.getGridManager().createIsland(player, true);
                }
            }, 10);
        }
    }
}
