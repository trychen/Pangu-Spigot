package cn.mccraft.pangu.spigot.bridge;

import cn.mccraft.pangu.spigot.Bridge;
import cn.mccraft.pangu.spigot.Remote;
import cn.mccraft.pangu.spigot.data.Persistence;
import cn.mccraft.pangu.spigot.server.MessageSender;
import com.trychen.bytedatastream.ByteSerialization;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public enum BridgeProxy implements InvocationHandler {
    INSTANCE;

    @SuppressWarnings("Duplicates")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Type[] types = method.getGenericParameterTypes();
        Collection<Player> players = new HashSet<>();
        boolean addAllPlayers = true;

        Bridge bridge = method.getAnnotation(Bridge.class);
        if (bridge == null) return null;

        if (args.length > 0) {
            if (args[0] instanceof Player) {
                players.add((Player) args[0]);
                addAllPlayers = false;
            } else if (args[0] instanceof Collection) {
                if (Player.class.isAssignableFrom((Class<?>) ((ParameterizedType)types[0]).getActualTypeArguments()[0])) {
                    players.addAll((Collection<? extends Player>) args[0]);
                    addAllPlayers = false;
                }
            } else if (args[0].getClass().isArray() && Player.class.isAssignableFrom(args.getClass().getComponentType())) {
                Collections.addAll(players, (Player[]) args[0]);
                addAllPlayers = false;
            }
        }
        String[] names = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);

        if (addAllPlayers) {
            players = (Collection<Player>) Bukkit.getOnlinePlayers();
        } else {
            args = ArrayUtils.remove(args, 0);
            types = (Type[]) ArrayUtils.remove(types, 0);
            names = (String[]) ArrayUtils.remove(names, 0);
        }

        Persistence persistence = BridgeManager.INSTANCE.getPersistence(bridge.persistence());
        byte[] bytes = persistence.serialize(names, args, types, true);

        BridgeManager.INSTANCE.send(players, bridge.value(), bytes);
        return null;
    }
}
