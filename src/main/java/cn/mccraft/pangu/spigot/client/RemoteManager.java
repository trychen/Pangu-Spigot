package cn.mccraft.pangu.spigot.client;

import cn.mccraft.pangu.spigot.PanguSpigot;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public enum RemoteManager implements PluginMessageListener {
    INSTANCE;

    private Set<String> registeredChannel = new HashSet<>();
    private Set<RemoteMessage> remoteMessages = new HashSet<>();

    public void register(String messageChannel, Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && object == null) continue;
            if (!method.isAnnotationPresent(Remote.class)) continue;

            Remote remote = method.getAnnotation(Remote.class);
            try {
                registerMessageChannel(messageChannel);
                remoteMessages.add(new RemoteMessage(messageChannel, remote.value(), method, object));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public RemoteMessage get(String channel, int id) {
        for (RemoteMessage remoteMessage : remoteMessages) {
            if (remoteMessage.getChannel().equals(channel) && remoteMessage.getId() == id) return remoteMessage;
        }
        return null;
    }

    public void registerMessageChannel(String channel) {
        if (registeredChannel.contains(channel)) return;
        Bukkit.getMessenger().registerIncomingPluginChannel(PanguSpigot.getInstance(), channel, this);
        registeredChannel.add(channel);
        PanguSpigot.getInstance().getLogger().info("成功注册对内通讯通道 " + channel);
    }

    public void injectMessage(RemoteMessage message, Player player, byte[] in) {
        try {
            Object[] objects;
            if (message.isWithPlayer()) {
                objects = new Object[message.getTypes().length + 1];
                Object[] deserialize = ByteSerialization.deserialize(in, message.getTypes());
                for (int i = 0; i < deserialize.length; i++) {
                    objects[i + 1] = deserialize[i];
                }
                objects[0] = player;
            } else objects = ByteSerialization.deserialize(in, message.getTypes());

            message.getMethodAccessor().invoke(message.getInstance(), objects);
        } catch (Exception e) {
            PanguSpigot.getInstance().getLogger().log(Level.SEVERE, "", e);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (message.length == 0) return;
        int id = message[0];
        RemoteMessage remoteMessage = get(channel, id);
        if (remoteMessage == null) {
            PanguSpigot.getInstance().getLogger().warning("Received unknown message from channel " + channel + ":" + id + " from " + player.getName());
            return;
        }
        byte[] data = Arrays.copyOfRange(message, 5, message.length);
        injectMessage(remoteMessage, player, data);
    }
}
