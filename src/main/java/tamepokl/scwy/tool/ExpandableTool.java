package tamepokl.scwy.tool;

import tamepokl.scwy.config.ConfigToolGui;

public class ExpandableTool extends ToolBase<ConfigToolGui> {


    public ExpandableTool(String name) {
        super(name,new ConfigToolGui(name,false , ""));
    }
}
