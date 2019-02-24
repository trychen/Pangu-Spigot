package cn.mccraft.pangu.spigot.server;

import cn.mccraft.pangu.spigot.PanguSpigot;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface MessageSender {
    Map<String, RemoteProxy> PROXY_CACHE = new HashMap<>();

    static void out(Player player, String channel,  int id, Object... objects) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeByte(id);
            byte[] bytes = ByteSerialization.serialize(objects);
            out.writeInt(bytes.length);
            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        player.sendPluginMessage(PanguSpigot.getInstance(), channel, b.toByteArray());
    }

    static void out(Iterable<Player> players, String channel, int id, Object... objects) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeByte(id);
            byte[] bytes = ByteSerialization.serialize(objects);
            out.writeInt(bytes.length);
            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Player player : players) {
            player.sendPluginMessage(PanguSpigot.getInstance(), channel, b.toByteArray());
        }
    }

    static void out(Iterable<Player> players, String channel, int id, byte[] bytes) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeByte(id);
            out.writeInt(bytes.length);
            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Player player : players) {
            player.sendPluginMessage(PanguSpigot.getInstance(), channel, b.toByteArray());
        }
    }

    static <T> T createProxy(Class<T> clazz, String channel) {
        RemoteProxy proxy = PROXY_CACHE.get(channel);
        if (proxy == null) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(PanguSpigot.getInstance(), channel);
            PanguSpigot.getInstance().getLogger().info("成功注册对外通讯通道 " + channel);
            PROXY_CACHE.put(channel, proxy = new RemoteProxy(channel));
        }
        return (T) Proxy.newProxyInstance(MessageSender.class.getClassLoader(), new Class[]{clazz}, proxy);
    }
}
