package tamepokl.scwy.mixin.litematicahelper;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.LitematicaHelper;

import static tamepokl.scwy.tool.LitematicaHelper.process;
/*
* 来源：https://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/mixin/masa/litematicahelper/ScreenHandlerMixin.java
* 作者：zhaixianyu
* */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Inject(method = "initializeContents", at = @At("TAIL"))
    private void tweakerMoreAutoContainerProcessorProcess(CallbackInfo ci)
    {
        if(LitematicaHelper.INSTANCE.isEnabled()) process((AbstractContainerMenu)(Object)this);
    }
}