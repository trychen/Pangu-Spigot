package cn.mccraft.pangu.spigot.server;

import cn.mccraft.pangu.spigot.PanguSpigot;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public interface MessageSender {
    Set<String> REGISTERED_CHANNEL = new HashSet<>();

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
        if (!REGISTERED_CHANNEL.contains(channel)) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(PanguSpigot.getInstance(), channel);
            REGISTERED_CHANNEL.add(channel);
            PanguSpigot.getInstance().getLogger().info("成功注册对外通讯通道 " + channel);
        }
        return (T) Proxy.newProxyInstance(MessageSender.class.getClassLoader(), new Class[]{clazz}, new RemoteProxy(channel));
    }
}
