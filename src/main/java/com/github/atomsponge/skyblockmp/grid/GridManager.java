package com.github.atomsponge.skyblockmp.grid;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.IslandDao;
import com.github.atomsponge.skyblockmp.dao.PlayerDao;
import com.github.atomsponge.skyblockmp.dao.impl.DaoException;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AtomSponge
 */
public class GridManager {
    @Getter
    private final SkyblockMp mod;
    @Getter
    private int islandHeight; // final
    @Getter
    private int spacing; // final

    @Getter
    private final Map<Integer, Island> islands = new HashMap<>();

    private IslandDao islandDao; // final
    private PlayerDao playerDao; // final

    private World world;
    private ForgeWorld worldEditWorld;
    private ClipboardHolder islandClipboardHolder;

    public GridManager(SkyblockMp mod) {
        this.mod = mod;
        islandHeight = mod.getConfig().getInt("grid.island.height");
        spacing = mod.getConfig().getInt("grid.spacing");
        islandDao = (IslandDao) mod.getDaoManager().getDao(Island.class);
        playerDao = (PlayerDao) mod.getDaoManager().getDao(Player.class);
    }

    public void createIsland(EntityPlayer player, boolean setAsDefault) {
        Position position;
        try {
            position = getNextPosition();
        } catch (Exception e) {
            mod.getLogger().error("Failed to get next position for island", e.getCause());
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to create your island. Please contact an administrator."));
            return;
        }

        createIsland(position, player, setAsDefault);
    }

    public void createIsland(final Position position, final EntityPlayer player, boolean setAsDefault) {
        mod.getLogger().info(String.format("Creating island at position %d, %d (Block coordinates: %d, %d, %d)",
                position.getX(), position.getZ(), position.getBlockX(), islandHeight, position.getBlockZ()));
        // ToDo: Localize player messages
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Creating your island..."));

        loadChunksAroundPosition(position);
        pasteSchematic(position);

        String offsetConfigPrefix = "grid.island.spawn-offset.";
        Vector defaultSpawnOffset = new Vector(
                mod.getConfig().getDouble(offsetConfigPrefix + "x"),
                mod.getConfig().getDouble(offsetConfigPrefix + "y"),
                mod.getConfig().getDouble(offsetConfigPrefix + "z"));

        final Player playerModel = mod.getPlayerManager().getPlayerByEntity(player);
        final Island island = new Island(-1, position, playerModel.getId(), defaultSpawnOffset);

        try {
            islandDao.insertIsland(island);
            islandDao.insertIslandMember(island, playerModel);
        } catch (DaoException e) {
            mod.getLogger().error("Failed to create island", e.getCause());
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to create your island. Please contact an administrator."));
            return;
        }

        islands.put(island.getId(), island);
        if (setAsDefault) {
            playerModel.setDefaultIsland(island.getId());
            playerDao.updatePlayer(playerModel);
        }

        mod.getLogger().info("Successfully created island for player " + player.getDisplayName());
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Successfully created your island."));

        teleportPlayerToIsland(player, island);
    }

    public void teleportPlayerToIsland(final EntityPlayer player, final Island island) {
        mod.getScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Teleporting..."));
                player.setPositionAndUpdate(island.getSpawnPosition().getX(), island.getSpawnPosition().getY(), island.getSpawnPosition().getZ());
            }
        }, 5);
    }

    public Island getHomeIsland(Player player) {
        if (player.getDefaultIsland() != -1) {
            return islands.get(player.getDefaultIsland());
        } else {
            for (Island island : islands.values()) {
                if (island.getOwner() == player.getId()) {
                    return island;
                }
            }
            return null;
        }
    }

    public Position getNextPosition() throws Exception {
        Position lastPosition;
        try {
            lastPosition = islandDao.findLatestPosition();
        } catch (DaoException e) {
            throw new Exception("Failed to get latest position from database");
        }

        return getNextPosition(lastPosition);
    }

    public void initialize() {
        loadSchematic();
        loadIslands();
    }

    private void loadIslands() {
        mod.getLogger().info("Loading islands from database");
        try {
            List<Island> foundIslands = islandDao.findAll();
            for (Island foundIsland : foundIslands) {
                islands.put(foundIsland.getId(), foundIsland);
            }
            mod.getLogger().info(String.format("Successfully loaded %d island(s)", foundIslands.size()));
        } catch (DaoException e) {
            throw new RuntimeException("Failed to load islands from database", e.getCause());
        }
    }

    private void loadSchematic() {
        mod.getLogger().info("Loading island schematic");

        File file = new File(mod.getConfig().getString("grid.island.schematic-file"));
        if (!file.exists()) {
            throw new RuntimeException("Island schematic file does not exist");
        }

        ClipboardFormat format = ClipboardFormat.findByFile(file);
        if (format == null) {
            throw new RuntimeException("Unknown schematic format");
        }

        try (FileInputStream fileInputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            ClipboardReader clipboardReader = format.getReader(bufferedInputStream);
            world = MinecraftServer.getServer().getEntityWorld();
            worldEditWorld = ForgeWorldEdit.inst.getWorld(world);
            WorldData worldData = worldEditWorld.getWorldData();
            Clipboard clipboard = clipboardReader.read(worldData);
            islandClipboardHolder = new ClipboardHolder(clipboard, worldData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schematic", e);
        }
    }

    private void pasteSchematic(Position position) {
        Vector vector = new Vector(position.getBlockX(), islandHeight, position.getBlockZ());
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(worldEditWorld, -1);
        Operation operation = islandClipboardHolder
                .createPaste(editSession, worldEditWorld.getWorldData())
                .to(vector)
                .ignoreAirBlocks(true)
                .build();
        try {
            Operations.completeLegacy(operation);
        } catch (MaxChangedBlocksException e) {
            // This should never happen as we set no limit for the edit session
            mod.getLogger().error("Failed to paste island schematic", e);
        }
    }

    private void loadChunksAroundPosition(Position position) {
        WorldServer worldServer = (WorldServer) world;
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                worldServer.theChunkProviderServer.loadChunk(position.getX() + chunkX, position.getZ() + chunkZ);
            }
        }
    }

    private Position getNextPosition(Position lastPosition) throws Exception {
        if (lastPosition == null) {
            // No island has been generated yet, reserve the center position at 0,0
            // Expand into x positive (east) first
            return new Position(this, 1, 0);
        }

        // Imagine a two-dimensional Cartesian coordinate system

        int x = lastPosition.getX();
        int z = lastPosition.getZ();
        int absX = Math.abs(x);
        int absZ = Math.abs(z);

        int size = Math.max(absX, absZ);
        Preconditions.checkState(size > 0, "Grid size has to be positive");

        // First sequence: Along the right outer border, where z <= 0 ("below" the x-axis)
        if (x >= 0 && x == size && z <= 0) {
            // Did we reach the bottom-right corner yet?
            if (absZ < size) {
                // No we did not, move down
                return new Position(this, x, z - 1);
            } else if (absZ >= size) {
                // Yes we did, turn left
                return new Position(this, x - 1, z);
            }
        }

        // Second sequence: Along the bottom outer border
        if (z == size * -1) {
            // Did we reach the bottom-left corner yet?
            if (absX < size) {
                // No we did not, move left
                return new Position(this, x - 1, z);
            } else if (absX >= size) {
                // Yes we did, turn upwards
                return new Position(this, x, z + 1);
            }
        }

        // Third sequence: Along the left outer border
        if (x == size * -1) {
            // Did we reach the top-left corner yet?
            if (absZ < size) {
                // No we did not, move up
                return new Position(this, x, z + 1);
            } else if (absZ >= size) {
                // Yes we did, turn right
                return new Position(this, x + 1, z);
            }
        }

        // I had to swap the checks for the fourth and fifth sequence, as both conditions seem to be true sometimes (in the first "round")
        // Fifth sequence: Along the right outer border, where z > 0 ("above" the x-axis)
        if (x == size && z > 0) {
            // Did we reach the turn-point yet?
            if (z >= 2) {
                // No we did not, move down
                return new Position(this, x, z - 1);
            } else if (z == 1) {
                // Yes we did, move one unit right and one down
                return new Position(this, x + 1, z - 1);
            }
        }

        // Fourth sequence: Along the top outer border
        if (z == size) {
            // Did we reach the top-right corner yet?
            if (absX < size) {
                // No we did not, move right
                return new Position(this, x + 1, z);
            } else if (absX >= size) {
                // Yes we did, turn downwards
                return new Position(this, x, z - 1);
            }
        }

        throw new Exception("Failed to find next island position");
    }
}
