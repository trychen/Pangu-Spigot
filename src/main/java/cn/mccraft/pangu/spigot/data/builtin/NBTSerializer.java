package cn.mccraft.pangu.spigot.data.builtin;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.gson.*;
import com.trychen.bytedatastream.ByteDeserializer;
import com.trychen.bytedatastream.ByteSerializer;
import com.trychen.bytedatastream.DataInput;
import com.trychen.bytedatastream.DataOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;

public enum NBTSerializer implements ByteSerializer<NbtCompound>, ByteDeserializer<NbtCompound>, JsonSerializer<NbtCompound>, JsonDeserializer<NbtCompound> {
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

    @Override
    public NbtCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(json.getAsString()));
        NbtWrapper<Object> nbtWrapper = NbtBinarySerializer.DEFAULT.deserialize(new DataInput(inputStream));
        return NbtFactory.fromNMSCompound(nbtWrapper.getHandle());
    }

    @Override
    public JsonElement serialize(NbtCompound src, Type typeOfSrc, JsonSerializationContext context) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        NbtBinarySerializer.DEFAULT.serialize(src, new DataOutputStream(stream));
        return context.serialize(Base64.getEncoder().encodeToString(stream.toByteArray()));
    }
}
