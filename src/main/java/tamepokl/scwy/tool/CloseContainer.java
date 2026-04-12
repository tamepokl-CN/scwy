package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class CloseContainer extends Tool{
    public static final CloseContainer INSTANCE = new CloseContainer("close_container");
    public CloseContainer(String name) {
        super(name);
    }
    static {
        addTool(INSTANCE);
    }
}
