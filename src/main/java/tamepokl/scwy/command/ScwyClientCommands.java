package tamepokl.scwy.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import tamepokl.scwy.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ScwyClientCommands {
        public static final List<@NotNull LiteralArgumentBuilder<FabricClientCommandSource>> builders = new ArrayList<>();
        public static final Map<Class<? extends ArgumentType<?>> , Supplier<? extends ArgumentType<?>> > argumentTypes = new HashMap<>();
        static {
//            ConfigEditor.init();
            builders.add(ClientCommandManager.literal("scwy")
                    .executes(context -> {
                        context.getSource().sendFeedback(Component.literal("scwy v" + Reference.MOD_VERSION));
                        return 1;
                    })
//                    .then(ConfigEditor.builder)
            );

        }
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath("scwy", "tweakeroo_tweaks"), tweakerooTweaksArgumentType.class, SingletonArgumentInfo.contextFree(tweakerooTweaksArgumentType::new));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            builders.forEach(dispatcher::register);
        });
    }



    public static class tweakerooTweaksArgumentType implements ArgumentType<String> {
        public tweakerooTweaksArgumentType() {

        }

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            String string = reader.readString();
            boolean b = Arrays.stream(FeatureToggle.values())
                    .anyMatch(featureToggle -> featureToggle.getName().equals(string));
            if(!b) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            return string;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String remaining = builder.getRemaining();
            for (FeatureToggle value : FeatureToggle.values()) {
                if(value.getName().startsWith(remaining)) {
                    builder.suggest(value.getName());
                }
            }
            return builder.buildFuture();
        }
    }
}
