package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class MaceKill extends Tool {
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
}
