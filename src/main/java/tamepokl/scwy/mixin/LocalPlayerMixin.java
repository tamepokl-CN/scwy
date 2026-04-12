package tamepokl.scwy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.autoFill.AutoFill;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "closeContainer", at = @At("HEAD"))
    public void closeContainer(CallbackInfo ci) {
        AbstractContainerMenu container = Minecraft.getInstance().player.containerMenu;

        if(AutoFill.INSTANCE.inGui && !(container instanceof InventoryMenu)){
            AutoFill.INSTANCE.updateMaterials(container, AutoFill.INSTANCE.lastClicked, false);
            AutoFill.INSTANCE.inGui = false;
        }
    }
}
