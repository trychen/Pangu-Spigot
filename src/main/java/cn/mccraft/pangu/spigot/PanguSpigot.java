package cn.mccraft.pangu.spigot;

import cn.mccraft.pangu.spigot.bridge.BridgeManager;
import cn.mccraft.pangu.spigot.data.ByteSerializers;
import cn.mccraft.pangu.spigot.data.JsonPersistence;
import org.bukkit.plugin.java.JavaPlugin;

public class PanguSpigot extends JavaPlugin {
    private static PanguSpigot instance;
    public static boolean DEBUG = false;

    public static PanguSpigot getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        ByteSerializers.register();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DEBUG = getConfig().getBoolean("debug", false);
        BridgeManager.INSTANCE.init();
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            JsonPersistence.INSTANCE.createGsonInstance();
        });
    }

    public static void debug(String message) {
        if (!DEBUG) return;
        getInstance().getLogger().info(message);
    }
}
