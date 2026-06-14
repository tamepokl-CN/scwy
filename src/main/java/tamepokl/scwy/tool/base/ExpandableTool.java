package tamepokl.scwy.tool.base;

import org.jspecify.annotations.NonNull;
import tamepokl.scwy.config.ToolConfig;
import tamepokl.scwy.utils.ConfigUtils;

public class ExpandableTool extends ToolBase<ToolConfig> {


    public ExpandableTool(String name) {
        super(name, createToolConfig(name,false));
    }
    public ExpandableTool(String name,boolean defaultValue) {
        super(name, createToolConfig(name,defaultValue));
    }

    private static @NonNull ToolConfig createToolConfig(String name,boolean defaultValue) {
        return (ToolConfig) ConfigUtils.apply(new ToolConfig(name, defaultValue, ""));
    }
}
