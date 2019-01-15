package cn.mccraft.pangu.spigot;

import javafx.geometry.Side;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Remote {
    /**
     * Message ID
     */
    int value();
}