package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.tool.base.ExpandableTool;

import static tamepokl.scwy.tool.base.ToolManager.addTool;

public class MaceKill extends ExpandableTool {
    public static final MaceKill INSTANCE = new MaceKill("macekill");
    public final ConfigInteger PACKETCOUNT = config.ofInteger("packetcount", 17, 1, 18);
    public final ConfigInteger HEIGHT = config.ofInteger("height", 1790, 0, 2000);
    public boolean isAttacking = false;

    static {
        addTool(INSTANCE);
    }

    public MaceKill(String name) {
        super(name);
    }

    public void onAttack(Player player, Entity entity, CallbackInfo ci) {
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
            for (int i2 = 0; i2 < PACKETCOUNT.getIntegerValue(); i2++) {
                mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false,
                        mc.player.horizontalCollision));
            }


            ServerboundMovePlayerPacket.Pos home = new ServerboundMovePlayerPacket.Pos(previous.x, previous.y,
                    previous.z, false, mc.player.horizontalCollision);
            Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + HEIGHT.getIntegerValue(), entity.getZ());
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
}
