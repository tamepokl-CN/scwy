package tamepokl.scwy.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import tamepokl.scwy.tool.PacketRecorder;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"),cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {

        PacketRecorder.INSTANCE.onClientO(packet);
    }
    @Inject(method = "genericsFtw", at = @At("HEAD"))
    private static void onReceive(Packet<?> packet, PacketListener packetListener, CallbackInfo ci) {
        PacketRecorder.INSTANCE.onClientI(packet);
    }
}
