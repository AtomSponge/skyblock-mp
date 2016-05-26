package com.github.atomsponge.skyblockmp.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AtomSponge
 */
public abstract class ParentCommand implements ICommand {
    private final Map<String, ChildCommand> childCommands = new HashMap<>();
    private final List<String> aliases;
    private final String usage;

    protected ParentCommand(String usage, String... aliases) {
        assert aliases.length > 0;
        this.aliases = Arrays.asList(aliases);
        this.usage = usage;
    }

    protected void registerChildCommand(ChildCommand childCommand) {
        childCommands.put(childCommand.getAlias().toLowerCase(), childCommand);
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] arguments) {
        if (arguments.length == 0) {
            sendHelp(commandSender);
            return;
        }

        String childAlias = arguments[0].toLowerCase();
        if (childCommands.containsKey(childAlias)) {
            ChildCommand childCommand = childCommands.get(childAlias);
            if (!childCommand.canCommandSenderUseCommand(commandSender)) {
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You cannot use this command"));
                return;
            }
            if (childCommand.isOpOnly() && !isOp(commandSender)) {
                System.out.println("Command is op only, but sender is not opped");
                commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You do not have permission to use this command"));
                return;
            }

            childCommand.process(commandSender, Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            sendHelp(commandSender);
        }
    }

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public List getCommandAliases() {
        return aliases;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return usage;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] arguments) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    private boolean isOp(ICommandSender commandSender) {
        return commandSender instanceof MinecraftServer ||
                commandSender instanceof CommandBlockLogic ||
                (commandSender instanceof EntityPlayer && MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer) commandSender).getGameProfile()));
    }

    private void sendHelp(ICommandSender commandSender) {
        // Send list of all child commands
        commandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Unknown child command. Possible commands:"));
        for (ChildCommand childCommand : childCommands.values()) {
            commandSender.addChatMessage(new ChatComponentText(String.format(" %s%s%s: %s", EnumChatFormatting.YELLOW, childCommand.getAlias(), EnumChatFormatting.GRAY, childCommand.getDescription())));
        }
    }
}
