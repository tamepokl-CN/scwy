package tamepokl.scwy.utils;

import com.mojang.brigadier.CommandDispatcher;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ScwyUtils {
    public static final MutableComponent scwy = Component.literal("[").append(Component.literal("scwy").withStyle(ChatFormatting.GOLD)).append(Component.literal("]"));
    public static void printMessage(Component component,Boolean asTitle){
        Minecraft.getInstance().execute(() -> {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(applyPrefix(component),asTitle);
        }

        });
    }
    public static void printTranslatableMessage(String translationKey,Boolean log,Object... args){
        printMessage(StringUtils.translateable(translationKey,args),log);
    }
    public static void printLiteralMessage(String message,Boolean asTitle){
        printMessage(Component.literal(message),asTitle);
    }
    public static MutableComponent applyPrefix(Component component){
        return scwy.copy().append( component);
    }


    public static CommandDispatcher<ClientSuggestionProvider> getCommands() {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null)
        {
            return networkHandler.getCommands();
        }
        return null;
    }

    public static void printTranslatableMessage(String s, boolean b) {
        printTranslatableMessage(s,b,new Object[0]);
    }

    public static String buildFileName(String s) {
        String name = removeV(s);
        return "scwy-%s.jar".formatted(name);
    }

    public static String removeV(@NonNull String s) {
        if (s.startsWith("v")) {
            return s.substring(1);
        }
        return s;
    }
}
