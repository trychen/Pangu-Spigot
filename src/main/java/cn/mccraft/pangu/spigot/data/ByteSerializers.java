package cn.mccraft.pangu.spigot.data;

import cn.mccraft.pangu.spigot.data.builtin.ItemStackSerializer;
import cn.mccraft.pangu.spigot.data.builtin.NBTSerializer;
import cn.mccraft.pangu.spigot.data.builtin.TextComponentSerializer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.inventory.ItemStack;

public interface ByteSerializers {
    static void register() {
        ByteSerialization.register(ItemStack.class, ItemStackSerializer.INSTANCE, ItemStackSerializer.INSTANCE);
        ByteSerialization.register(NbtCompound.class, NBTSerializer.INSTANCE, NBTSerializer.INSTANCE);
        ByteSerialization.register(WrappedChatComponent.class, TextComponentSerializer.INSTANCE, TextComponentSerializer.INSTANCE);
    }
}
