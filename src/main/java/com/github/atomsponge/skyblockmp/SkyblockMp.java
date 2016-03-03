package com.github.atomsponge.skyblockmp;

import com.github.atomsponge.skyblockmp.util.ConfigUtils;
import com.typesafe.config.Config;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.Getter;
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

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        try {
            logger.info("Loading config");
            config = ConfigUtils.load(getClass().getClassLoader(), "config.conf", event.getSuggestedConfigurationFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }
}
