package tamepokl.scwy.tool.base;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;

public class GeneralTool extends ToolBase<ConfigBooleanHotkeyed> {
    public GeneralTool(String name) {
        super(name,(new ConfigBooleanHotkeyed(name , false , "")));
    }
}
