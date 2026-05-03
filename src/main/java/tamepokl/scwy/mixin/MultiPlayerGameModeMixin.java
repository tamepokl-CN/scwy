package tamepokl.scwy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.MaceKill;
/*
* 来源：https://github.com/etianl/Trouser-Streak/blob/main/src/main/java/pwn/noobs/trouserstreak/modules/MaceKill.java
* 原作者：etianl
* */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {


    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Player player, Entity entity, CallbackInfo ci) {
        MaceKill.INSTANCE.onAttack(player, entity, ci);
    }

}
