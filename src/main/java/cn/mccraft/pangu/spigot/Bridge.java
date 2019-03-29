package cn.mccraft.pangu.spigot;

import cn.mccraft.pangu.spigot.data.JsonPersistence;
import cn.mccraft.pangu.spigot.data.Persistence;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bridge {
    /**
     * message key
     */
    String value();

    Class<? extends Persistence> persistence() default JsonPersistence.class;
}