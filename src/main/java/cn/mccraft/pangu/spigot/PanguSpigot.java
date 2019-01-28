package cn.mccraft.pangu.spigot;

import cn.mccraft.pangu.spigot.data.ByteSerializers;
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
    }

    @Override
    public void onEnable() {
    }
}
