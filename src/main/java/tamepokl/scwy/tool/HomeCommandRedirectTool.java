package tamepokl.scwy.tool;

import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

public class HomeCommandRedirectTool extends ExpandableTool {
    public static final HomeCommandRedirectTool INSTANCE = new HomeCommandRedirectTool("homeCommandRedirect",true);
    public HomeCommandRedirectTool(String name, boolean b) {
        super(name);
    }
    static {
        ToolManager.addTool(INSTANCE);
    }
    public static boolean enable() {
        return INSTANCE.isEnabled();
    }
}
