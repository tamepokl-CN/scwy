package tamepokl.scwy.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.Collections;
import java.util.Optional;

public class CommandUtils {
    //from LitematicaRemoveEntityCommandOverrider
    public static Optional<Boolean> isCommandValid(String command)
    {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null)
        {

            var node = networkHandler.getCommands().findNode(Collections.singleton(command));
            return Optional.of(node != null && node.canUse(networkHandler.getSuggestionsProvider()));
        }
        return Optional.empty();
    }

    public static boolean sendCommand(String command) {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null)
        {
            networkHandler.sendCommand(command);
            return true;

        }else {
            return false;
        }
    }
}
