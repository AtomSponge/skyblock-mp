package com.github.atomsponge.skyblockmp;

import com.github.atomsponge.skyblockmp.command.IslandCommand;
import com.github.atomsponge.skyblockmp.dao.impl.DaoManager;
import com.github.atomsponge.skyblockmp.dao.impl.jdbc.IslandDaoJdbcImpl;
import com.github.atomsponge.skyblockmp.dao.impl.jdbc.PlayerDaoJdbcImpl;
import com.github.atomsponge.skyblockmp.database.DatabaseManager;
import com.github.atomsponge.skyblockmp.grid.GridManager;
import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.model.Island;
import com.github.atomsponge.skyblockmp.model.Player;
import com.github.atomsponge.skyblockmp.player.PlayerManager;
import com.github.atomsponge.skyblockmp.util.ConfigUtils;
import com.github.atomsponge.skyblockmp.world.SkyblockWorldType;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockEndWorldProvider;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockHellWorldProvider;
import com.github.atomsponge.skyblockmp.world.worldproviders.SkyblockSurfaceWorldProvider;
import com.typesafe.config.Config;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import lombok.Getter;
import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.common.DimensionManager;
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
    @Getter
    private DaoManager daoManager;
    @Getter
    private PlayerManager playerManager;
    @Getter
    private Scheduler scheduler;
    @Getter
    private GridManager gridManager;

    private SkyblockWorldType skyblockWorldType;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
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

        daoManager = new DaoManager(this);
        daoManager.registerDao(Player.class, new PlayerDaoJdbcImpl(this));
        daoManager.registerDao(Island.class, new IslandDaoJdbcImpl(this));

        playerManager = new PlayerManager(this);
        scheduler = new Scheduler();

        FMLCommonHandler.instance().bus().register(this);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        registerWorldProviders();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
        commandManager.registerCommand(new IslandCommand(this));
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        gridManager = new GridManager(this);
        gridManager.initialize();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
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
