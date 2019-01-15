package cn.mccraft.pangu.spigot;

import com.github.mouse0w0.fastreflection.FastReflection;
import com.github.mouse0w0.fastreflection.MethodAccessor;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;

public class RemoteMessage {
    private String channel;
    private int id;
    private Class[] types;
    private boolean withPlayer;
    private Object instance;
    private MethodAccessor methodAccessor;

    public RemoteMessage(String channel, int id, Method method) throws Exception {
        this.channel = channel;
        this.id = id;
        if (method.getParameterCount() > 0 || method.getParameterTypes()[0] == Player.class) {
            withPlayer = true;
            types = Arrays.copyOfRange(method.getParameterTypes(), 1, method.getParameterTypes().length);
        } else {
            withPlayer = false;
            types = method.getParameterTypes();
        }

        methodAccessor = FastReflection.create(method);
    }

    public String getChannel() {
        return channel;
    }

    public int getId() {
        return id;
    }

    public Class[] getTypes() {
        return types;
    }

    public boolean isWithPlayer() {
        return withPlayer;
    }

    public Object getInstance() {
        return instance;
    }

    public MethodAccessor getMethodAccessor() {
        return methodAccessor;
    }

    public void invoke(Player player, byte[] bytes) throws Exception {
        Object[] objects = ByteSerialization.deserialize(bytes, types);
        if (withPlayer) {
            Object[] newObjects = new Object[objects.length + 1];
            System.arraycopy(objects, 0, newObjects, 1, objects.length);
            newObjects[0] = player;
            objects = newObjects;
        }
        methodAccessor.invoke(instance, objects);
    }
}
