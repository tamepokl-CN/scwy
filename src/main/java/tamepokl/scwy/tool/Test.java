package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import tamepokl.scwy.config.ConfigTriggerHotkey;
import tamepokl.scwy.utils.HighlightBlockRenderer;
import tamepokl.scwy.utils.LitematicaUtils;

import java.util.HashSet;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class Test extends Tool {
    public static final Test INSTANCE = new Test("test");

    public final ConfigTriggerHotkey HOTKEY = config.ofTriggerHotkey("test", "");
    public final ConfigTriggerHotkey SIGHTTEST = config.ofTriggerHotkey("sight", "");

    static {

        addTool(INSTANCE);
    }

    {
        HighlightBlockRenderer.createHighlightBlockList("test", new ConfigColor("test", Color4f.WHITE) , true);
        HOTKEY.setCallback((action, key) -> {
            int missingCount = LitematicaUtils.getMissingBlocksCount();
            LitematicaUtils.VerifierStatus status = LitematicaUtils.getVerifierStatus();
            System.out.println("缺失方块: " + status.getMissingBlocks());
            System.out.println("总错误数: " + status.getTotalErrors());
            return true;
        });
        SIGHTTEST.setCallback((action, key) -> {
            BlockPos pos = LitematicaUtils.getSchematicBlockInSight(Minecraft.getInstance().player);
            if(pos!=null) {
                System.out.println(pos);

                HighlightBlockRenderer.setPos("test", new HashSet<>() {{
                    add(pos);
                }});
            }
            return true;
        });
    }

    public Test(String name) {
        super(name);
    }
}
