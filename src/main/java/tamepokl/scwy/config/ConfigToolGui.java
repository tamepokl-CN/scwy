package tamepokl.scwy.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.InfoUtils;

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
}
