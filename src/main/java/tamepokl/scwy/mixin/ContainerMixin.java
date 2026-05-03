package tamepokl.scwy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.CloseContainer;

@Mixin(AbstractContainerMenu.class)
public class ContainerMixin {
    @Inject(method = "initializeContents", at = @At("TAIL"))
    private void containerProcessorProcess(CallbackInfo ci)
    {
        AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;
        CloseContainer.INSTANCE.onContainerOpen(ci , menu);
    }
}
