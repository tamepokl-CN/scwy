package tamepokl.scwy.mixin.litematicahelper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.selection.SelectionManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import tamepokl.scwy.tool.LitematicaHelper;


import static fi.dy.masa.litematica.util.RayTraceUtils.*;
/*
 * 来源：https://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/mixin/masa/litematicahelper/SelectionManagerMixin.java
 * 作者：zhaixianyu
 * */
@Mixin(SelectionManager.class)
public class SelectionManagerMixin {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getTargetedPosition(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZ)Lnet/minecraft/core/BlockPos;"),method = "resetSelectionToClickedPosition")
    public BlockPos resetSelectionToClickedPosition(Level world, Entity player, double maxDistance, boolean sneakToOffset, Operation<BlockPos> original){
        return getPos(world,player,maxDistance,sneakToOffset,original);
    }
    @WrapOperation(at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getTargetedPosition(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZ)Lnet/minecraft/core/BlockPos;"),method = "growSelectionToContainClickedPosition")
    public BlockPos growSelectionToContainClickedPosition(Level world, Entity player, double maxDistance, boolean sneakToOffset, Operation<BlockPos> original){
        return getPos(world,player,maxDistance,sneakToOffset,original);
    }
    @WrapOperation(at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getTargetedPosition(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZ)Lnet/minecraft/core/BlockPos;"),method = "setPositionOfCurrentSelectionToRayTrace")
    public BlockPos setPositionOfCurrentSelectionToRayTrace(Level world, Entity player, double maxDistance, boolean sneakToOffset, Operation<BlockPos> original){
        return getPos(world,player,maxDistance,sneakToOffset,original);
    }

    @Unique
    private BlockPos getPos(Level world, Entity player, double maxDistance, boolean sneakToOffset, Operation<BlockPos> original){
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null || !LitematicaHelper.INSTANCE.isEnabled()) return original.call(world,player,maxDistance,sneakToOffset);

        BlockHitResult blockHitResult = traceToSchematicWorld(player, 200, true, true);
        if(blockHitResult == null || schematicWorld.getBlockState(blockHitResult.getBlockPos()).isAir()){
            return original.call(world, player, maxDistance, sneakToOffset);
        }
        BlockPos pos = blockHitResult.getBlockPos();
        if (sneakToOffset == player.isShiftKeyDown())
        {
            pos = pos.relative(blockHitResult.getDirection());
        }
        return pos;
    }
}