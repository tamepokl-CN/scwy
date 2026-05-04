package tamepokl.scwy.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tamepokl.scwy.tool.CommandRedirect;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
    private String modifyCommandString(String string2) {
        return CommandRedirect.INSTANCE.modifyCommandString(string2);
    }
}
