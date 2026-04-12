package tamepokl.scwy.tool;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigLockedListType;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.util.data.Color4f;
import tamepokl.scwy.Reference;
import tamepokl.scwy.config.ConfigCoordinate;
import tamepokl.scwy.config.ConfigToolGui;
import tamepokl.scwy.config.ConfigTriggerHotkey;

import java.util.ArrayList;
import java.util.List;

public class ToolConfig {
    private static final String TOOL_KEY = Reference.MOD_ID + ".config.tool";
    public final List<IConfigBase> children = new ArrayList<>();
    public final String name;
    public ConfigToolGui guiConfig;

    public ToolConfig(String name) {
        this.name = name;

    }

    public void addChild(IConfigBase config) {
        if (config != null) {
            children.add(config);
        }
    }

    private ConfigToolGui createToolGuiConfig() {
        ConfigToolGui config = new ConfigToolGui(name, false);
        children.forEach(config::addChild);
        return (ConfigToolGui) config.apply(TOOL_KEY);
    }


    public ConfigToolGui getToolGuiConfig() {
        if (guiConfig == null) this.guiConfig = createToolGuiConfig();
        return guiConfig;

    }

    public IConfigBase ofBase(ConfigBase<? extends IConfigBase> base) {
        IConfigBase apply = base.apply("%s.%s".formatted(TOOL_KEY, name));
        addChild(apply);
        return apply;
    }

    public ConfigBoolean ofBoolean(String name) {
        return (ConfigBoolean) ofBase(new ConfigBoolean(name, false));
    }

    public ConfigBoolean ofBoolean(String name, Boolean defaultValue) {
        return (ConfigBoolean) ofBase(new ConfigBoolean(name, defaultValue));
    }

    public ConfigBooleanHotkeyed ofBooleanHotkeyed(String name, String defaultHotkey) {
        ConfigBooleanHotkeyed config = new ConfigBooleanHotkeyed(name, false, defaultHotkey);
        config.getKeybind().setCallback((action, key) -> {
            config.toggleBooleanValue();
            return true;
        });
        return (ConfigBooleanHotkeyed) ofBase(config);
    }

    public ConfigColor ofColor(String name, String defaultValue) {
        return (ConfigColor) ofBase(new ConfigColor(name, defaultValue));
    }

    public ConfigColorList ofColorList(String name, ImmutableList<Color4f> defaultValue) {
        return (ConfigColorList) ofBase(new ConfigColorList(name, defaultValue));
    }

    public ConfigDouble ofDouble(String name, double defaultValue, double minValue, double maxValue) {
        return (ConfigDouble) ofBase(new ConfigDouble(name, defaultValue, minValue, maxValue));
    }

    public ConfigFloat ofFloat(String name, float defaultValue, float minValue, float maxValue) {
        return (ConfigFloat) ofBase(new ConfigFloat(name, defaultValue, minValue, maxValue));
    }

    public ConfigHotkey ofHotkey(String name, String defaultHotkey) {
        return (ConfigHotkey) ofBase(new ConfigHotkey(name, defaultHotkey));
    }

    public ConfigInteger ofInteger(String name, int defaultValue, int minValue, int maxValue) {
        return (ConfigInteger) ofBase(new ConfigInteger(name, defaultValue, minValue, maxValue));
    }

    public ConfigLockedList ofLockedList(String name, IConfigLockedListType handler) {
        return (ConfigLockedList) ofBase(new ConfigLockedList(name, handler));
    }

    public ConfigOptionList ofOptionList(String name, IConfigOptionListEntry defaultValue) {
        return (ConfigOptionList) ofBase(new ConfigOptionList(name, defaultValue));
    }

    public ConfigString ofString(String name, String defaultValue) {
        return (ConfigString) ofBase(new ConfigString(name, defaultValue));
    }

    public ConfigStringList ofStringList(String name, ImmutableList<String> defaultValue) {
        return (ConfigStringList) ofBase(new ConfigStringList(name, defaultValue));
    }

    public ConfigTriggerHotkey ofTriggerHotkey(String name, String defaultHotkey) {
        return (ConfigTriggerHotkey) ofBase(new ConfigTriggerHotkey(name, defaultHotkey));
    }
    public ConfigCoordinate ofCoordinate(String name) {
        ConfigCoordinate coordinate = new ConfigCoordinate(name, 0, 0, 0);
        addChild(coordinate);
        return coordinate;
    }
}
