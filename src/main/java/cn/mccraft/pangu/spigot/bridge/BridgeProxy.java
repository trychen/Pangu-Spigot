package cn.mccraft.pangu.spigot.bridge;

import cn.mccraft.pangu.spigot.Bridge;
import cn.mccraft.pangu.spigot.PanguSpigot;
import cn.mccraft.pangu.spigot.data.Persistence;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public enum BridgeProxy implements InvocationHandler {
    INSTANCE;

    @SuppressWarnings("Duplicates")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            PanguSpigot.getInstance().getLogger().log(Level.SEVERE, "default method isn't support in this version", new IllegalAccessException());
            return null;
        }

        Bridge bridge = method.getAnnotation(Bridge.class);

        if (bridge == null) return null;

        Type[] types = method.getGenericParameterTypes();
        Collection<Player> players = null;
        boolean addAllPlayers = true;

        if (args.length > 0) {
            if (args[0] instanceof Player) {
                players = Collections.singleton((Player) args[0]);
                addAllPlayers = false;
            } else if (args[0] instanceof Collection) {
                if (Player.class.isAssignableFrom((Class<?>) ((ParameterizedType)types[0]).getActualTypeArguments()[0])) {
                    players = (Collection<Player>) args[0];
                    addAllPlayers = false;
                }
            } else if (args[0].getClass().isArray() && Player.class.isAssignableFrom(args.getClass().getComponentType())) {
                players = Sets.newHashSet((Player[]) args[0]);
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

        if (players == null || players.isEmpty()) return null;

        Persistence persistence = BridgeManager.INSTANCE.getPersistence(bridge.persistence());
        byte[] bytes = persistence.serialize(names, args, types, true);

        BridgeManager.INSTANCE.send(players, bridge.value(), bytes);
        return null;
    }
}
