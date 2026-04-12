package tamepokl.scwy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.AutoCollect;
import tamepokl.scwy.tool.CloseContainer;
import tamepokl.scwy.tool.Tool1;
import tamepokl.scwy.tool.autoFill.AutoFill;

//从tweakermore借鉴
@Mixin(AbstractContainerMenu.class)
public class ContainerMixin {
    @Inject(method = "initializeContents", at = @At("TAIL"))
    private void containerProcessorProcess(CallbackInfo ci)
    {
        AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;
        if(menu instanceof MerchantMenu m){
            if(Tool1.INSTANCE.isEnabled()){
                Tool1.INSTANCE.process(m);
                return;
            }
        }
            AutoFill.INSTANCE.process((AbstractContainerMenu)(Object)this);
        if(CloseContainer.INSTANCE.isEnabled() && menu instanceof BrewingStandMenu) {
            Minecraft.getInstance().player.closeContainer();
            return;
        }
        if(AutoCollect.INSTANCE.isEnabled()){
            AutoCollect.INSTANCE.process((AbstractContainerMenu)(Object)this);
        }
    }
}
