package tamepokl.scwy.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigLockedListType;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import tamepokl.scwy.Reference;
import tamepokl.scwy.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigToolGui extends ConfigBoolean implements ExpandableConfig , IHotkeyTogglable {

    public final List<IConfigBase> children = new ArrayList<>();
    private boolean expanded;
    private final IKeybind keybind;

    public ConfigToolGui(String name, boolean defaultValue) {
        this(name, defaultValue, "");
    }

    public ConfigToolGui(String name, boolean defaultValue, String defaultHotKey) {
        super(name, defaultValue);
        this.keybind = KeybindMulti.fromStorageString(defaultHotKey, KeybindSettings.DEFAULT);
        this.keybind.setCallback(this::onToggle);
    }
    @Override
    public IKeybind getKeybind() {
        return this.keybind;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public List<IConfigBase> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public void addChild(IConfigBase config) {
        if (config != null) {
            this.children.add(config);
        }
    }

    public boolean onToggle(KeyAction action, IKeybind keybind) {
        this.setBooleanValue(!this.getBooleanValue());
        InfoUtils.printBooleanConfigToggleMessage(this.getName(), this.getBooleanValue());

        return true;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("enabled")) {
                    this.setBooleanValue(obj.get("enabled").getAsBoolean());
                }

                if (obj.has("hotkey")) {
                    this.keybind.setValueFromString(obj.get("hotkey").getAsString());
                }
            } else if (element.isJsonPrimitive()) {
                // 兼容旧格式（只有布尔值）
                this.setBooleanValue(element.getAsBoolean());
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", this.getBooleanValue());
        obj.addProperty("hotkey", this.keybind.getStringValue());
        return obj;
    }

    //便捷方法，用于快速创建配置

    public IConfigBase ofBase(ConfigBase<? extends IConfigBase> base) {
        IConfigBase apply = ConfigUtils.apply(base);
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
}
