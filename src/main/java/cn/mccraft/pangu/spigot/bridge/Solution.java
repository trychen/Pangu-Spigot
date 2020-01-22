package cn.mccraft.pangu.spigot.bridge;

import cn.mccraft.pangu.spigot.PanguSpigot;
import cn.mccraft.pangu.spigot.data.Persistence;
import com.github.mouse0w0.fastreflection.FastReflection;
import com.github.mouse0w0.fastreflection.MethodAccessor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;

public class Solution {
    private Object instance;
    private Method method;
    private Persistence persistence;

    private boolean withPlayer;
    private Type[] actualParameterTypes;
    private String[] actualParameterNames;
    private MethodAccessor methodAccessor;

    public Solution(Object instance, Method method, Persistence persistence) throws Exception {
        this.instance = instance;
        this.method = method;
        this.persistence = persistence;
        this.withPlayer = method.getParameterCount() > 0 && Player.class.isAssignableFrom(method.getParameterTypes()[0]);
        this.actualParameterTypes = withPlayer?(Type[]) ArrayUtils.remove(method.getGenericParameterTypes(), 0):method.getGenericParameterTypes();
        this.actualParameterNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
        if (withPlayer) {
            this.actualParameterNames = (String[]) ArrayUtils.remove(actualParameterNames, 0);
        }

        this.methodAccessor = FastReflection.create(method);
    }

    public void solve(Player player, byte[] bytes) throws Exception {
        Object[] objects = persistence.deserialize(actualParameterNames, bytes, actualParameterTypes);
        if (withPlayer) {
            objects = ArrayUtils.add(objects, 0, player);
        }
        methodAccessor.invoke(instance, objects);
        PanguSpigot.debug("成功执行 @Bridge " + method.toGenericString());
    }

    public Method getMethod() {
        return method;
    }
}
