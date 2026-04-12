package tamepokl.scwy.mixin.litematicahelper;

import fi.dy.masa.litematica.materials.MaterialListHudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
/*
 * 来源：https://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/mixin/masa/litematicahelper/MaterialListHudRendererAccessor.java
 * 作者：zhaixianyu
 * */
@Mixin(MaterialListHudRenderer.class)
public interface MaterialListHudRendererAccessor
{
    @Accessor(remap = false)
    void setLastUpdateTime(long value);

    @Invoker(remap = false)
    String invokeGetFormattedCountString(int count, int maxStackSize);
}