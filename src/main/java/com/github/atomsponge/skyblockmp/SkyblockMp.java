package com.github.atomsponge.skyblockmp;

import com.github.atomsponge.skyblockmp.database.DatabaseManager;
import com.github.atomsponge.skyblockmp.grid.Island;
import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.util.ConfigUtils;
import com.github.atomsponge.skyblockmp.world.SkyblockWorldType;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockEndWorldProvider;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockHellWorldProvider;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockSurfaceWorldProvider;
import com.typesafe.config.Config;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import lombok.Getter;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, acceptableRemoteVersions = "*")
public class SkyblockMp {
    @Getter
    @Mod.Instance
    private SkyblockMp instance;

    @Getter
    private Logger logger;
    @Getter
    private Config config;
    @Getter
    private DatabaseManager databaseManager;

    private SkyblockWorldType skyblockWorldType;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        try {
            logger.info("Loading config");
            config = ConfigUtils.load(getClass().getClassLoader(), "config.conf", event.getSuggestedConfigurationFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database manager", e);
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        registerWorldProviders();
    }

    @EventHandler
    public void stop(FMLServerStoppingEvent event) {
        databaseManager.shutdown();
    }

    private void registerWorldProviders() {
        logger.info("Registering world type");
        skyblockWorldType = new SkyblockWorldType();

        logger.info("Removing old world providers");
        for (int dimension : new int[]{-1, 0, 1}) {
            DimensionManager.unregisterProviderType(dimension);
        }

        logger.info("Registering custom world providers");
        DimensionManager.registerProviderType(-1, SkyblockHellWorldProvider.class, true);
        DimensionManager.registerProviderType(0, SkyblockSurfaceWorldProvider.class, true);
        DimensionManager.registerProviderType(1, SkyblockEndWorldProvider.class, true);
    }
}
