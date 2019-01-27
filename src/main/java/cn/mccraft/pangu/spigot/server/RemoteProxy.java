package cn.mccraft.pangu.spigot.server;

import cn.mccraft.pangu.spigot.PanguSpigot;
import cn.mccraft.pangu.spigot.client.Remote;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RemoteProxy implements InvocationHandler {
    private final String channel;

    public RemoteProxy(String channel) {
        this.channel = channel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Type[] types = method.getGenericParameterTypes();
        Collection<Player> players = new HashSet<>();
        boolean addAllPlayers = true;

        Remote remote = method.getAnnotation(Remote.class);
        if (remote == null) return null;

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
        if (addAllPlayers) {
            players = (Collection<Player>) Bukkit.getOnlinePlayers();
        } else {
            args = Arrays.copyOfRange(args, 1, args.length);
            types = Arrays.copyOfRange(types, 1, types.length);
        }

        byte[] bytes = ByteSerialization.serialize(args, types);

        MessageSender.out(players, channel, remote.value(), bytes);
        return null;
    }
}
