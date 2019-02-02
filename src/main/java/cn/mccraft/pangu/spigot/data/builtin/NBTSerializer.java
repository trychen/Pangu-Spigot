package cn.mccraft.pangu.spigot.data.builtin;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.trychen.bytedatastream.ByteDeserializer;
import com.trychen.bytedatastream.ByteSerializer;
import com.trychen.bytedatastream.DataInput;
import com.trychen.bytedatastream.DataOutput;

import java.io.IOException;

public enum NBTSerializer implements ByteSerializer<NbtCompound>, ByteDeserializer<NbtCompound> {
    INSTANCE;
    @Override
    public NbtCompound deserialize(DataInput in) throws IOException {
        NbtWrapper<Object> nbtWrapper = NbtBinarySerializer.DEFAULT.deserialize(in);
        return NbtFactory.fromNMSCompound(nbtWrapper.getHandle());
    }

    @Override
    public void serialize(DataOutput out, NbtCompound object) throws IOException {
        NbtBinarySerializer.DEFAULT.serialize(object, out);
    }
}
