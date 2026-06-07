package tamepokl.scwy.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import tamepokl.scwy.tool.FlyAbility;

public class FakeCreative {
    public static final LiteralArgumentBuilder<FabricClientCommandSource> creativeBuilder = ClientCommandManager.literal("fakeCreative").executes(context -> setCreativeGamemode(Minecraft.getInstance()));

    public static final LiteralArgumentBuilder<FabricClientCommandSource> survivalBuilder = ClientCommandManager.literal("fakeSurvival").executes(context -> setSurvivalGamemode(Minecraft.getInstance()));
    private static int setCreativeGamemode(Minecraft client) {
        if (client.player != null) {
            client.gameMode.setLocalMode(GameType.CREATIVE);
//                client.player.playerInfo.setGameMode(GameType.CREATIVE);
            Abilities abilities = client.player.getAbilities();
            abilities.instabuild = true;
            abilities.invulnerable = true;
            abilities.mayfly = true;
            client.player.onUpdateAbilities();
        }
        return 1;
    }
        private static int setSurvivalGamemode(Minecraft client) {
            if (client.player != null) {
                client.gameMode.setLocalMode(GameType.SURVIVAL);
//                client.player.playerInfo.setGameMode(GameType.CREATIVE);
                Abilities abilities = client.player.getAbilities();
                abilities.instabuild = false;
                abilities.invulnerable = false;
                abilities.mayfly = false;
                client.player.onUpdateAbilities();
            }

        return 1;
    }
}
