package tamepokl.scwy.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
@Deprecated
public class ConfigEditor {
    public static final LiteralArgumentBuilder<FabricClientCommandSource> builder = literal("configEdit");
    public static final List<Mod> mods = List.of(new tweakeroo());
    public static void init() {
        for (Mod mod : mods) {
            builder.then(mod.builder);
        }

    }
    public static class tweakeroo extends Mod{
        public tweakeroo() {
            super("tweakeroo");
            builder=literal("tweakeroo")
                    .then(literal("tweaks")
                            .then(ClientCommandManager.argument("tweaks" , new ScwyClientCommands.tweakerooTweaksArgumentType())
                                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                            .executes(context -> {
                                                executeTweaks(context);
                                                return 1;
                                            }))));
        }

        private static void executeTweaks(CommandContext<FabricClientCommandSource> context) {
            boolean value = context.getArgument("value", boolean.class);
            String tweaks = context.getArgument("tweaks", String.class);
            Arrays.stream(FeatureToggle.values()).filter(featureToggle -> featureToggle.getName().equals(tweaks))
                            .findFirst()
                    .ifPresent(featureToggle -> featureToggle.setBooleanValue(value));

        }
    }
    public static class Mod{
        public Mod(String id) {
            this.id = id;
        }

        public String id;
        public LiteralArgumentBuilder<FabricClientCommandSource> builder;
    }
}
