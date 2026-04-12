package tamepokl.scwy.mixin.litematicahelper;


import fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksArea;
import fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksPlacement;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.LitematicaHelper;
import tamepokl.scwy.utils.ScwyUtils;
/*
 * 来源：https://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/mixin/masa/litematicahelper/TaskCountBlocksAreaMixin.java
 * 作者：zhaixianyu
 * */
@Mixin(value = {TaskCountBlocksArea.class, TaskCountBlocksPlacement.class})
public class TaskCountBlocksAreaMixin {
    @Inject(at = @At(value = "HEAD"), method = "countAtPosition", cancellable = true)
    private void countAtPosition(BlockPos pos, CallbackInfo ci) {
        if(LitematicaHelper.INSTANCE.isEnabled() && !ScwyUtils.TempData.xuanQuFanWeiNei_p(pos)) ci.cancel();
    }
}