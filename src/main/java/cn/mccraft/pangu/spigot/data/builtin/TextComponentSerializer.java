package cn.mccraft.pangu.spigot.data.builtin;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.trychen.bytedatastream.ByteDeserializer;
import com.trychen.bytedatastream.ByteSerializer;
import com.trychen.bytedatastream.DataInput;
import com.trychen.bytedatastream.DataOutput;

import java.io.IOException;

public enum TextComponentSerializer implements ByteSerializer<WrappedChatComponent>, ByteDeserializer<WrappedChatComponent> {
    INSTANCE;
    @Override
    public WrappedChatComponent deserialize(DataInput in) throws IOException {
        return WrappedChatComponent.fromJson(in.readUTF());
    }

    @Override
    public void serialize(DataOutput out, WrappedChatComponent object) throws IOException {
        out.writeUTF(object.getJson());
    }
}
