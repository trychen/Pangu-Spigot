package cn.mccraft.pangu.spigot.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface DataUtils {
    static void writeVarInt(DataOutputStream to, int toWrite) throws IOException {
        while ((toWrite & -128) != 0) {
            to.writeByte(toWrite & 127 | 128);
            toWrite >>>= 7;
        }
        to.writeByte(toWrite);
    }

    static int readVarInt(DataInputStream input) throws IOException {
        int i = 0;
        int j = 0;
        byte b0;

        do {
            b0 = input.readByte();
            i |= (b0 & 127) << j++ * 7;
        }
        while ((b0 & 128) == 128);

        return i;
    }

}
