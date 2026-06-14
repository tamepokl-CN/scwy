package tamepokl.scwy.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tamepokl.scwy.command.HomeCommandRedirect;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), argsOnly = true)
    private Component decodeChatMessage(Component message) {
        return HomeCommandRedirect.processChatMessage( message);
    }


}
