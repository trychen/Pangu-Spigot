package cn.mccraft.pangu.spigot.data;

import com.trychen.bytedatastream.ByteSerialization;

import java.io.IOException;
import java.lang.reflect.Type;

public enum ByteStreamPersistence implements Persistence {
    INSTANCE;

    @Override
    public byte[] serialize(String[] parameterNames, Object[] objects, Type[] types, boolean persistenceByParameterOrder) throws IOException {
        return ByteSerialization.serialize(objects, types);
    }

    @Override
    public Object[] deserialize(String[] parameterNames, byte[] bytes, Type[] types) throws IOException {
        return ByteSerialization.deserialize(bytes, types);
    }
}
