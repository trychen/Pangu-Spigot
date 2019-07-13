package cn.mccraft.pangu.spigot.bridge;


import cn.mccraft.pangu.spigot.Bridge;
import cn.mccraft.pangu.spigot.PanguSpigot;
import cn.mccraft.pangu.spigot.Remote;
import cn.mccraft.pangu.spigot.client.RemoteMessage;
import cn.mccraft.pangu.spigot.data.ByteStreamPersistence;
import cn.mccraft.pangu.spigot.data.DataUtils;
import cn.mccraft.pangu.spigot.data.JsonPersistence;
import cn.mccraft.pangu.spigot.data.Persistence;
import cn.mccraft.pangu.spigot.server.MessageSender;
import cn.mccraft.pangu.spigot.server.RemoteProxy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum  BridgeManager implements PluginMessageListener {
    INSTANCE;

    private Map<String, Solution> solutions = new HashMap<>();
    private Map<Class<? extends Persistence>, Persistence> persistences = new HashMap<Class<? extends Persistence>, Persistence>() {{
        put(JsonPersistence.class, JsonPersistence.INSTANCE);
        put(ByteStreamPersistence.class, ByteStreamPersistence.INSTANCE);
    }};

    public Persistence getPersistence(Class<? extends Persistence> clazz) {
        return persistences.get(clazz);
    }

    public Map<Class<? extends Persistence>, Persistence> getPersistences() {
        return persistences;
    }

    public void init() {
        Bukkit.getMessenger().registerIncomingPluginChannel(PanguSpigot.getInstance(), "pangu", this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(PanguSpigot.getInstance(), "pangu");
    }

    public void register(Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Bridge.class)) continue;
            Bridge bridge = method.getAnnotation(Bridge.class);

            try {
                solutions.put(bridge.value(), new Solution(object, method, persistences.get(bridge.persistence())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(message));

            int id = input.readByte();

            byte[] keyBytes = new byte[DataUtils.readVarInt(input)];
            input.read(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            byte[] data = new byte[DataUtils.readVarInt(input)];
            input.read(data);

            Solution solution = solutions.get(key);
            if (solution != null) {
                solution.solve(player, data);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(Collection<Player> players, String key, byte[] bytes) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeByte(0x01);

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            DataUtils.writeVarInt(out, keyBytes.length);
            out.write(keyBytes);

            DataUtils.writeVarInt(out, bytes.length);
            out.write(bytes);

            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Player player : players) {
            player.sendPluginMessage(PanguSpigot.getInstance(), "pangu", b.toByteArray());
        }
    }

    public <T> T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(BridgeManager.class.getClassLoader(), new Class[]{clazz}, BridgeProxy.INSTANCE);
    }
}
