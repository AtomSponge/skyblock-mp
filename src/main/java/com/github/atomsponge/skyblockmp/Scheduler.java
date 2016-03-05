package com.github.atomsponge.skyblockmp;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import lombok.AllArgsConstructor;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author AtomSponge
 */
public class Scheduler {
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    public Scheduler() {
        FMLCommonHandler.instance().bus().register(this);
    }

    public void schedule(Runnable runnable, int ticks) {
        scheduledTasks.add(new ScheduledTask(ticks, runnable));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (!scheduledTasks.isEmpty()) {
            for (Iterator<ScheduledTask> iterator = scheduledTasks.iterator(); iterator.hasNext();) {
                ScheduledTask task = iterator.next();

                task.remainingTicks--;
                if (task.remainingTicks <= 0) {
                    task.runnable.run();
                    iterator.remove();
                }
            }
        }
    }

    @AllArgsConstructor
    private class ScheduledTask {
        private int remainingTicks;
        private final Runnable runnable;
    }
}
