package tamepokl.scwy.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.CommandRedirect;

import java.util.Collection;
import java.util.List;

@Mixin(value = ClientPacketListener.class,priority = 2000)
public abstract class ClientPacketListenerMixin {
    @Shadow
    private CommandDispatcher<ClientSuggestionProvider> commands;

    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void onHandleCommands(ClientboundCommandsPacket packet, CallbackInfo ci) {
//        Collection<CommandNode<ClientSuggestionProvider>> scwy = this.commands.findNode(List.of("scwy")).getChildren();
//        LiteralArgumentBuilder<ClientSuggestionProvider> test = LiteralArgumentBuilder.literal("t");
//        LiteralCommandNode<ClientSuggestionProvider> build = test.build();
//        scwy.forEach(build::addChild);
//        this.commands.getRoot().addChild(build);
        CommandRedirect.INSTANCE.onHandleCommands(packet,commands);
    }

}
