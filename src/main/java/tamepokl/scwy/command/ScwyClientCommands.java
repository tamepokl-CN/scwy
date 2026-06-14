package tamepokl.scwy.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import tamepokl.scwy.Reference;
import tamepokl.scwy.command.ConfigEditor.MasaConfigOptionList.IConfigOptionListArgumentType;
import tamepokl.scwy.command.ConfigEditor.MasaConfigStringList.MasaConfigStringListArgumentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ScwyClientCommands {
    public static final List<@NotNull LiteralArgumentBuilder<FabricClientCommandSource>> builders = new ArrayList<>();
    public static final Map<Class<? extends ArgumentType<?>> , Supplier<? extends ArgumentType<?>> > argumentTypes = new HashMap<>();
    private static boolean hasBuild = false;

    public static final LiteralArgumentBuilder<FabricClientCommandSource> SCWY = ClientCommandManager.literal("scwy");

    static {
            ConfigEditor.init();
            builders.add(SCWY
                    .executes(context -> {
                        context.getSource().sendFeedback(Component.literal("scwy v" + Reference.MOD_VERSION));
                        return 1;
                    })
            );
//            builders.add(HomeCommandRedirect.homebuilder);


        }
    public static void init() {
        try {
            Class.forName("tamepokl.scwy.command.HomeCommandRedirect");
            Class.forName("tamepokl.scwy.command.ConfigEditor");
            Class.forName("tamepokl.scwy.command.FakeCreative");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
//        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath("scwy", "i_option_list"), IConfigOptionListArgumentType.class, SingletonArgumentInfo.contextFree(IConfigOptionListArgumentType::new));
//        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath("scwy","string_list"), MasaConfigStringListArgumentType.class, SingletonArgumentInfo.contextFree(MasaConfigStringListArgumentType::new));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            builders.forEach(dispatcher::register);
            ScwyClientCommands.hasBuild = true;
        });
    }
    public static void addBuilder(LiteralArgumentBuilder<FabricClientCommandSource> builder){
        if(hasBuild){
            throw new RuntimeException("Command Registry has frozen");
        }
        builders.add(builder);
    }
    public static void addToScwy(LiteralArgumentBuilder<FabricClientCommandSource> builder){
        if(hasBuild){
            throw new RuntimeException("Command Registry has frozen");
        }
        SCWY.then(builder);
    }


}
