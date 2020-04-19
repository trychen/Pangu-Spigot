package cn.mccraft.pangu.spigot.data;

import cn.mccraft.pangu.spigot.data.builtin.ItemStackSerializer;
import cn.mccraft.pangu.spigot.data.builtin.NBTSerializer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum JsonPersistence implements Persistence {
    INSTANCE;

    private Gson gson;
    private Charset charset = StandardCharsets.UTF_8;

    public void createGsonInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ItemStack.class, ItemStackSerializer.INSTANCE);
        builder.registerTypeAdapter(MinecraftReflection.getCraftItemStackClass(), ItemStackSerializer.INSTANCE);
        builder.registerTypeAdapter(MinecraftReflection.getItemStackClass(), ItemStackSerializer.INSTANCE);
        builder.registerTypeAdapter(NbtCompound.class, NBTSerializer.INSTANCE);

        Bukkit.getPluginManager().callEvent(new GsonCreateEvent(builder));
        this.gson = builder.create();
    }

    @Override
    public byte[] serialize(String[] parameterNames, Object[] objects, Type[] types, boolean persistenceByParameterOrder) throws IOException {
        assert parameterNames.length == objects.length && objects.length == types.length : "Argument's length must be the same";

        JsonObject jsonObject = new JsonObject();

        if (persistenceByParameterOrder) {
            jsonObject.add("__persistence_by_parameter_order__", new JsonPrimitive(true));
        }

        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i]; if (object == null) continue;
            String name = persistenceByParameterOrder? "arg" + i : parameterNames[i];
            Type type = types[i];

            JsonElement json = gson.toJsonTree(object, type);
            jsonObject.add(name, json);
        }

        return jsonObject.toString().getBytes(charset);
    }

    @Override
    public Object[] deserialize(String[] parameterNames, byte[] bytes, Type[] types) throws IOException {
        String json = new String(bytes, charset);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        JsonElement jsonParameterOrder = jsonObject.get("__persistence_by_parameter_order__");
        boolean byOrder = jsonParameterOrder != null && !jsonParameterOrder.isJsonNull()?jsonParameterOrder.getAsBoolean():false;

        Object[] objects = new Object[types.length];
        for (int i = 0; i < parameterNames.length; i++) {
            JsonElement element;

            if (byOrder) {
                element = jsonObject.get("arg" + i);
            } else {
                String name = parameterNames[i];
                element = jsonObject.get(name);
            }

            if (element != null && !element.isJsonNull()) {
                objects[i] = gson.fromJson(element, types[i]);
            }
        }
        return objects;
    }

    public static class GsonCreateEvent extends Event {
        private GsonBuilder builder;

        @java.beans.ConstructorProperties({"builder"})
        public GsonCreateEvent(GsonBuilder builder) {
            this.builder = builder;
        }

        public GsonBuilder getBuilder() {
            return this.builder;
        }

        public void setBuilder(GsonBuilder builder) {
            this.builder = builder;
        }

        private static HandlerList handlerList = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static void setHandlerList(HandlerList handlerList) {
            GsonCreateEvent.handlerList = handlerList;
        }
    }
}
