package cn.mccraft.pangu.spigot.server;

import cn.mccraft.pangu.spigot.PanguSpigot;
import com.trychen.bytedatastream.ByteSerialization;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageSender {
    public static void out(Player player, String channel,  int id, Object... objects) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeByte(id);
            byte[] bytes = ByteSerialization.serialize(objects);
            out.writeInt(bytes.length);
            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        player.sendPluginMessage(PanguSpigot.getInstance(), channel, b.toByteArray());
    }
}
