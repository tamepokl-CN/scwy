package tamepokl.scwy.tool.base;

import fi.dy.masa.malilib.config.IHotkeyTogglable;

public class ToolBase<T extends IHotkeyTogglable> {

    public T config;
    public String name;
    public boolean isEnabled(){
        return getConfig().getBooleanValue();
    }

    public ToolBase(String name , T config){
        this.name = name;
        this.setConfig(config);
    }


    public void init(){};

    public T getConfig() {
        return config;
    }

    public void setConfig(T config) {
        this.config = config;
    }
}
