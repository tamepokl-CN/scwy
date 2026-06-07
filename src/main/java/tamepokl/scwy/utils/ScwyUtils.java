package tamepokl.scwy.utils;

import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ScwyUtils {
    public static final MutableComponent scwy = Component.literal("[").append(Component.literal("scwy").withStyle(ChatFormatting.GOLD)).append(Component.literal("]"));
    public static void printMessage(Component component,Boolean log){
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(applyPrefix(component),log);
        }
    }
    public static void printTranslatableMessage(String translationKey,Object[]  args,Boolean log){
        printMessage(StringUtils.translateable(translationKey,args),log);
    }
    public static void printLiteralMessage(String message,Boolean log){
        printMessage(Component.literal(message),log);
    }
    public static MutableComponent applyPrefix(Component component){
        return scwy.copy().append( component);
    }
}
