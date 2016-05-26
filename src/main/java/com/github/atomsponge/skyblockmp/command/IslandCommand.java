package com.github.atomsponge.skyblockmp.command;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.IslandDao;
import com.github.atomsponge.skyblockmp.dao.PlayerDao;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/**
 * @author AtomSponge
 */
public class IslandCommand extends ParentCommand {
    private final SkyblockMp mod;

    public IslandCommand(SkyblockMp mod) {
        super(null, "island");
        this.mod = mod;

        registerChildCommand(new TeleportCommand());
        registerChildCommand(new CreateCommand());
        registerChildCommand(new InviteCommand());
    }

    // ToDo: Make this less "meh" and add some more commands

    public class TeleportCommand extends ChildCommand {
        TeleportCommand() {
            super("teleport", null, "Teleports to your home island", false);
        }

        @Override
        public void process(ICommandSender commandSender, String[] arguments) {
            EntityPlayer executor = (EntityPlayer) commandSender;
            Player executorModel = mod.getPlayerManager().getPlayerByEntity(executor);
            Island homeIsland = mod.getGridManager().getHomeIsland(executorModel);

            if (homeIsland == null) {
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You have no home island"));
                return;
            }

            mod.getGridManager().teleportPlayerToIsland(executor, homeIsland);
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
            return commandSender instanceof EntityPlayer;
        }
    }

    public class CreateCommand extends ChildCommand {
        CreateCommand() {
            super("create", null, "Creates an island", false);
        }

        @Override
        public void process(final ICommandSender commandSender, String[] arguments) {
            EntityPlayer executor = (EntityPlayer) commandSender;
            Player executorModel = mod.getPlayerManager().getPlayerByEntity(executor);

            Island island = mod.getGridManager().getHomeIsland(executorModel);
            if (island != null) {
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You already have an island"));
                return;
            }

            mod.getGridManager().createIsland(executor, true);
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
            return commandSender instanceof EntityPlayer;
        }
    }

    public class InviteCommand extends ChildCommand {
        InviteCommand() {
            super("invite", "<player>", "Invites a player to your home island", false);
        }

        // ToDo: Confirm before adding the invited player to the island

        @Override
        public void process(final ICommandSender commandSender, String[] arguments) {
            if (arguments.length < 1) {
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Syntax: island invite <player>"));
                return;
            }

            final EntityPlayer executor = (EntityPlayer) commandSender;
            final EntityPlayer target = findPlayerByName(arguments[0]);

            if (target == null) {
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Specified player not found"));
                return;
            }

            mod.getDatabaseManager().getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    Player executorModel = mod.getPlayerManager().getPlayerByEntity(executor);
                    Player targetModel = mod.getPlayerManager().getPlayerByEntity(target);
                    Island homeIsland = mod.getGridManager().getHomeIsland(executorModel);

                    if (mod.getGridManager().getHomeIsland(targetModel) != null) {
                        commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Specified player already has an island"));
                        return;
                    }

                    IslandDao islandDao = (IslandDao) mod.getDaoManager().getDao(Island.class);
                    List<Player> members = islandDao.findMembersByIsland(homeIsland);
                    int maxPlayers = mod.getConfig().getInt("general.max-players-per-island");
                    if (members.size() >= maxPlayers) {
                        commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + String.format("Island already has %d out of %d members", maxPlayers, maxPlayers)));
                        return;
                    }
                    islandDao.insertIslandMember(homeIsland, targetModel);

                    PlayerDao playerDao = (PlayerDao) mod.getDaoManager().getDao(Player.class);
                    targetModel.setDefaultIsland(homeIsland.getId());
                    playerDao.updatePlayer(targetModel);

                    mod.getGridManager().teleportPlayerToIsland(target, homeIsland);
                }
            });
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
            return commandSender instanceof EntityPlayer;
        }
    }

    @SuppressWarnings("unchecked")
    private static EntityPlayer findPlayerByName(String name) {
        List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : players) {
            if (player.getDisplayName().equals(name)) {
                return player;
            }
        }
        return null;
    }
}
