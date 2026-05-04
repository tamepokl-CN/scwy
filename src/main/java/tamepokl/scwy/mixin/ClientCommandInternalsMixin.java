package tamepokl.scwy.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientCommandInternals.class)
public class ClientCommandInternalsMixin {
//    @Inject(method = "addCommands", at = @At("RETURN"))
//    private static void onCommands(CommandDispatcher<FabricClientCommandSource> target, FabricClientCommandSource source, CallbackInfo ci){
//        CommandNode<FabricClientCommandSource> scwy = target.findNode(List.of("scwy"));
//        LiteralArgumentBuilder<FabricClientCommandSource> t = ClientCommandManager.literal("t");
//        target.getRoot().addChild(t.build());
//    }
}
