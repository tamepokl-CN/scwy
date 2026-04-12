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
        if (MaceKill.INSTANCE.isEnabled()) {

            Minecraft mc = Minecraft.getInstance();
            if (MaceKill.INSTANCE.isAttacking) return;
            if (player == null) return;
            if (player.getInventory().getSelectedItem().getItem() != Items.MACE) return;
            if (!entity.isAlive()) return;
            ci.cancel();
            Vec3 previous = player.position();
            ServerboundInteractPacket attack = ServerboundInteractPacket.createAttackPacket(entity,
                    player.isShiftKeyDown());
            for (int i2 = 0; i2 < getPaperpackets(); i2++) {
                mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false,
                        mc.player.horizontalCollision));
            }


            ServerboundMovePlayerPacket.Pos home = new ServerboundMovePlayerPacket.Pos(previous.x, previous.y,
                    previous.z, false, mc.player.horizontalCollision);
            Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + getHeight(), entity.getZ());
            ServerboundMovePlayerPacket.Pos target = new ServerboundMovePlayerPacket.Pos(targetPos.x, targetPos.y
                    , targetPos.z, false, mc.player.horizontalCollision);


            mc.player.connection.send(target);
            mc.player.setPos(previous);

            mc.player.connection.send(home);
            mc.player.setPos(previous);
            MaceKill.INSTANCE.isAttacking = true;
            mc.player.connection.send(attack);
            mc.player.connection.send(home);
            MaceKill.INSTANCE.isAttacking = false;


        }
    }



    public int getHeight() {
        return MaceKill.INSTANCE.HEIGHT.getIntegerValue();
    }

    public int getPaperpackets() {
        return MaceKill.INSTANCE.PACKETCOUNT.getIntegerValue();
    }
}
