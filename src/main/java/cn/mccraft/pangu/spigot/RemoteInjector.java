package cn.mccraft.pangu.spigot;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public enum RemoteInjector {
    INSTANCE;

    private Set<RemoteMessage> remoteMessages = new HashSet<>();

    public void register(String messageChannel, Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (!method.isAnnotationPresent(Remote.class)) continue;

            Remote remote = method.getAnnotation(Remote.class);
        }
    }
}
