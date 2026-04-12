package tamepokl.scwy.mixin.litematicahelper;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.selection.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tamepokl.scwy.tool.LitematicaHelper;
/*
 * 来源：
 * 作者：zhaixianyuhttps://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/mixin/masa/litematicahelper/TaskCountBlocksPlacementMixin.java
 * */
@Mixin(TaskCountBlocksPlacement.class)
public class TaskCountBlocksPlacementMixin {
    @WrapOperation(method = "<init>(Lfi/dy/masa/litematica/schematic/placement/SchematicPlacement;Lfi/dy/masa/litematica/materials/IMaterialList;Z)V",at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/placement/SchematicPlacement;getSubRegionBoxes(Lfi/dy/masa/litematica/schematic/placement/SubRegionPlacement$RequiredEnabled;)Lcom/google/common/collect/ImmutableMap;"),remap = false)
    private ImmutableMap<String, Box> getSubRegionBoxes(SchematicPlacement instance, SubRegionPlacement.RequiredEnabled boxOriginAbsolute, Operation<ImmutableMap<String, Box>> original){
        if(LitematicaHelper.INSTANCE.isEnabled() && DataManager.getSelectionManager().getCurrentSelection() != null){
            return DataManager.getSelectionManager().getCurrentSelection().getAllSubRegions();
        }else return original.call(instance, boxOriginAbsolute);
    }
}