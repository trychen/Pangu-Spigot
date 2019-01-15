package cn.mccraft.pangu.spigot;

import com.trychen.bytedatastream.DataInput;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public enum RemoteInjector implements PluginMessageListener {
    INSTANCE;

    private Set<RemoteMessage> remoteMessages = new HashSet<>();

    public void register(String messageChannel, Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && object == null) continue;
            if (!method.isAnnotationPresent(Remote.class)) continue;

            Remote remote = method.getAnnotation(Remote.class);
            try {
                remoteMessages.add(new RemoteMessage(messageChannel, remote.value(), method));
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

    public void injectMessage(String channel, int id, Player player) {

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            DataInput in = new DataInput(message);
            int id = in.readInt();
            injectMessage(channel, id, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
