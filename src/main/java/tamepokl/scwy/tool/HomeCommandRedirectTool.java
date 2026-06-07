package tamepokl.scwy.tool;

import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

public class HomeCommandRedirectTool extends ExpandableTool {
    public static final HomeCommandRedirectTool INSTANCE = new HomeCommandRedirectTool("homeCommandRedirect");
    public HomeCommandRedirectTool(String name) {
        super(name);
    }
    static {
        ToolManager.addTool(INSTANCE);
    }
    public static boolean enable() {
        return INSTANCE.isEnabled();
    }
}
