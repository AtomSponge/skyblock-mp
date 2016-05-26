package com.github.atomsponge.skyblockmp.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.command.ICommandSender;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ChildCommand {
    @Getter
    private final String alias;
    @Getter
    private final String usage;
    @Getter
    private final String description;
    @Getter
    private final boolean opOnly;

    public abstract void process(ICommandSender commandSender, String[] arguments);

    public abstract boolean canCommandSenderUseCommand(ICommandSender commandSender);
}
