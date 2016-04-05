package com.github.atomsponge.skyblockmp;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author AtomSponge
 */
public class Scheduler {
    private final List<ScheduledTask> scheduledTasks = new CopyOnWriteArrayList<>();
    private final List<ScheduledTask> toRemove = new ArrayList<>();

    public Scheduler() {
        FMLCommonHandler.instance().bus().register(this);
    }

    public void schedule(Runnable runnable, int ticks) {
        scheduledTasks.add(new ScheduledTask(ticks, runnable));
    }

    // ToDo: Find a better way to execute things delayed on the main thread

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (!scheduledTasks.isEmpty()) {
            for (ScheduledTask task : scheduledTasks) {
                task.remainingTicks--;
                if (task.remainingTicks <= 0) {
                    task.runnable.run();
                    toRemove.add(task);
                }
            }

            if (!toRemove.isEmpty()) {
                scheduledTasks.removeAll(toRemove);
                toRemove.clear();
            }
        }
    }

    @AllArgsConstructor
    private class ScheduledTask {
        private int remainingTicks;
        private final Runnable runnable;
    }
}
