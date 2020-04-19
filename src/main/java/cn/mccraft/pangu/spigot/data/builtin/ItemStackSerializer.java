package cn.mccraft.pangu.spigot.data.builtin;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.nbt.*;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.comphenix.protocol.wrappers.nbt.io.NbtTextSerializer;
import com.google.gson.*;
import com.trychen.bytedatastream.ByteDeserializer;
import com.trychen.bytedatastream.ByteSerializer;
import com.trychen.bytedatastream.DataInput;
import com.trychen.bytedatastream.DataOutput;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public enum ItemStackSerializer implements ByteSerializer<ItemStack>, ByteDeserializer<ItemStack>, JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    INSTANCE;

    @Override
    public ItemStack deserialize(DataInput in) throws IOException {
        short type = in.readShort();
        if (type < 0) return new ItemStack(Material.AIR);
        byte amount = in.readByte();
        short data = in.readShort();
        ItemStack itemStack = new ItemStack(type, amount, data);

        NbtWrapper<Object> nbtWrapper = NbtBinarySerializer.DEFAULT.deserialize(in);

        if (nbtWrapper.getType() != NbtType.TAG_COMPOUND) return itemStack;
        getStackModifier(itemStack).write(0, nbtWrapper);

        return itemStack;
    }

    @Override
    public void serialize(DataOutput out, ItemStack item) throws IOException {
        if (item != null && item.getAmount() != 0 && item.getType() != null && item.getType() != Material.AIR) {
            out.writeShort(item.getType().getId());
            out.writeByte(item.getAmount());
            out.writeShort(item.getDurability());
            NbtBase<?> nbtBase = getStackModifier(item).read(0);
            if (nbtBase == null) nbtBase = NbtFactory.ofCompound("");
            NbtBinarySerializer.DEFAULT.serialize(nbtBase, out);
        } else {
            out.writeShort(-1);
        }
    }


    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject element = json.getAsJsonObject();

        short id = element.get("id").getAsShort();
        if (id <= 0) {
            return null;
        }
        int amount = element.get("amount").getAsByte();
        short meta = element.get("meta").getAsShort();
        ItemStack itemStack = new ItemStack(id, amount, meta);

        if (element.has("nbt")) {
            NbtWrapper<Object> nbtWrapper = context.deserialize(element.get("nbt"), NbtCompound.class);

            if (nbtWrapper.getType() != NbtType.TAG_COMPOUND) return itemStack;
            getStackModifier(itemStack).write(0, nbtWrapper);
        }

        return itemStack;
    }

    @Override
    public JsonElement serialize(ItemStack src, Type type, JsonSerializationContext context) {
        Map<String, Object> map = new HashMap<>();
        if (src == null || src.getType() == Material.AIR) {
            map.put("id", -1);
        } else {
            map.put("id", src.getTypeId());
            map.put("amount", src.getAmount());
            map.put("meta", src.getDurability());

            NbtBase<?> nbtBase = getStackModifier(src).read(0);
            if (nbtBase != null) {
                map.put("nbt", context.serialize(nbtBase, NbtCompound.class));
            }
        }
        return context.serialize(map);
    }

    private static StructureModifier<Object> itemStackModifier;

    public static StructureModifier<NbtBase<?>> getStackModifier(ItemStack stack) {
        Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
        if (itemStackModifier == null) {
            itemStackModifier = new StructureModifier(nmsStack.getClass(), Object.class, false);
        }

        return itemStackModifier.withTarget(nmsStack).withType(MinecraftReflection.getNBTBaseClass(), BukkitConverters.getNbtConverter());
    }
}
