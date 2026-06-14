package tamepokl.scwy.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tamepokl.scwy.command.HomeCommandRedirect;
import tamepokl.scwy.tool.NoCommandVerify;

@Mixin(value = ClientPacketListener.class,priority = 2000)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void onHandleCommands(ClientboundCommandsPacket packet, CallbackInfo ci) {
        HomeCommandRedirect.reinjectHomeCommand();
    }
    @Inject(method = "handleCommandSuggestions", at = @At("RETURN"))
    private void onHandleCommandSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
        HomeCommandRedirect.onHandleSuggestions(packet);
    }
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String command, CallbackInfo info) {
        HomeCommandRedirect.onSendCommand(command,info);
    }
    @Inject(method = "verifyCommand", at = @At("RETURN"), cancellable = true)
    private void onVerify(String string, CallbackInfoReturnable<ClientPacketListener.CommandCheckResult> cir) {
        NoCommandVerify.onVerify(string,cir);
    }
    //ClientboundContainerSetContentPacket
}
