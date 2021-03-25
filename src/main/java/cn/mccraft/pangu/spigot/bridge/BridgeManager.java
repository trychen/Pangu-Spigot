package cn.mccraft.pangu.spigot.bridge;


import cn.mccraft.pangu.spigot.Bridge;
import cn.mccraft.pangu.spigot.PanguSpigot;
import cn.mccraft.pangu.spigot.Remote;
import cn.mccraft.pangu.spigot.client.RemoteMessage;
import cn.mccraft.pangu.spigot.data.*;
import cn.mccraft.pangu.spigot.server.MessageSender;
import cn.mccraft.pangu.spigot.server.RemoteProxy;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

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
                PanguSpigot.debug("Registered @Bridge Receiver with key " + bridge.value() + " for " + method.toGenericString());
            } catch (Exception e) {
                PanguSpigot.getInstance().getLogger().log(Level.SEVERE, "Error while register @Bridge for " + method.toGenericString(), e);
            }
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(message));

            int id = input.readByte();

            // Basic Packet
            if (id == 0 || id == 4) {
                byte[] keyBytes = new byte[DataUtils.readVarInt(input)];
                input.read(keyBytes);
                String key = new String(keyBytes, StandardCharsets.UTF_8);

                byte[] data = new byte[DataUtils.readVarInt(input)];
                input.read(data);

                if (id == 4) data = GZIPUtils.uncompress(data);

                Solution solution = solutions.get(key);
                if (solution != null) {
                    PanguSpigot.debug("收到 @Bridge 信息 " + key + "，解决方案 " + solution.getMethod().toGenericString());
                    solution.solve(player, data);
                } else {
                    PanguSpigot.debug("收到 @Bridge 信息 " + key + "，找不到对应的解决方案!");
                }
            }
        } catch (Exception e){
            PanguSpigot.getInstance().getLogger().log(Level.SEVERE, "Error while solving @Bridge for Player " + player.getName(), e);
        }
    }

    public void send(Collection<Player> players, String key, byte[] bytes) {
        send(players, key, bytes, bytes.length > 2000);
    }


    public void send(Collection<Player> players, String key, byte[] bytes, boolean compress) {
        if (bytes.length > 30000) {
            sendMultiPart(players, key, bytes);
            return;
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeByte(compress ? 0x05 : 0x01);

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            DataUtils.writeVarInt(out, keyBytes.length);
            out.write(keyBytes);

            byte[] write = compress ? GZIPUtils.compress(bytes) : bytes;
            DataUtils.writeVarInt(out, write.length);
            out.write(write);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Player player : players) {
            player.sendPluginMessage(PanguSpigot.getInstance(), "pangu", b.toByteArray());
        }
    }

    public void sendMultiPart(Collection<Player> players, String key, byte[] bytes) {
        short total = (short) ((bytes.length / 30000) + (bytes.length % 30000 > 0 ? 1 : 0));
        UUID id = UUID.randomUUID();

        for (short i = 0; i < total; i++) try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeByte(0x03);

            out.writeLong(id.getMostSignificantBits());
            out.writeLong(id.getLeastSignificantBits());
            out.writeShort(total);
            out.writeShort(i);

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            DataUtils.writeVarInt(out, keyBytes.length);
            out.write(keyBytes);

            byte[] sub = ArrayUtils.subarray(bytes, i * 30000, Math.min(bytes.length, (i + 1) * 30000));
            DataUtils.writeVarInt(out, sub.length);
            out.write(sub);

            for (Player player : players) {
                player.sendPluginMessage(PanguSpigot.getInstance(), "pangu", b.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public <T> T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(BridgeManager.class.getClassLoader(), new Class[]{clazz}, BridgeProxy.INSTANCE);
    }
}
