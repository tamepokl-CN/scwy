package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

public class FlyAbility extends ExpandableTool {
    public static final FlyAbility INSTANCE = new FlyAbility("flyAbility");
    static {
        ToolManager.addTool(INSTANCE);
    }
    public FlyAbility(String name) {
        super(name);
    }
    {
        this.config.setValueChangeCallback(this::setFlyAbility);
    }{
        ToolManager.onFirstTick(mc->this.setFlyAbility(this.config));
    }

    private void setFlyAbility(ConfigBoolean configBoolean) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.gameMode.getPlayerMode()!= GameType.CREATIVE) {
            if(configBoolean.getBooleanValue()) client.player.getAbilities().mayfly = configBoolean.getBooleanValue();
            client.player.onUpdateAbilities();
        }
    }
}
