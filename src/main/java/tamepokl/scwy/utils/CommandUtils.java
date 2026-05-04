package tamepokl.scwy.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.Optional;

public class CommandUtils {
    public static final MutableComponent scwy = Component.literal("[").append(Component.literal("scwy").withStyle(ChatFormatting.GOLD)).append(Component.literal("]"));
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
    public static void sendChatMessage(MutableComponent component){
        Component formatted = scwy.copy().append(component);
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(formatted, true);
        }
    }
}
