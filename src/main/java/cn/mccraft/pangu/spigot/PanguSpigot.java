package cn.mccraft.pangu.spigot;

import cn.mccraft.pangu.spigot.bridge.BridgeManager;
import cn.mccraft.pangu.spigot.data.ByteSerializers;
import cn.mccraft.pangu.spigot.data.JsonPersistence;
import org.bukkit.plugin.java.JavaPlugin;

public class PanguSpigot extends JavaPlugin {
    private static PanguSpigot instance;

    public static PanguSpigot getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        ByteSerializers.register();
        JsonPersistence.INSTANCE.createGsonInstance();
    }

    @Override
    public void onEnable() {
        BridgeManager.INSTANCE.init();
    }
}
