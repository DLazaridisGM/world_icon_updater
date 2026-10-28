package net.x7bbbbbbb.world_icon_updater.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.x7bbbbbbb.world_icon_updater.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLEnvironment.isProduction();
    }
}