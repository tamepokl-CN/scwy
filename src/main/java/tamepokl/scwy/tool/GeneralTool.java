package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import tamepokl.scwy.utils.ConfigUtils;

public class GeneralTool extends ToolBase<ConfigBooleanHotkeyed> {
    public GeneralTool(String name) {
        super(name,(new ConfigBooleanHotkeyed(name , false , "")));
    }
}
