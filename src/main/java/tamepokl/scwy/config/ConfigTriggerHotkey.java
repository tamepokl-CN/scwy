package tamepokl.scwy.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public class ConfigTriggerHotkey extends ConfigBase<ConfigTriggerHotkey> implements IHotkeyTogglable {
    private final IKeybind keybind;
    private Runnable triggerAction;
    public ConfigTriggerHotkey(String name, String defaultHotkey) {
        super(ConfigType.HOTKEY, name, name + " Trigger");
        this.keybind = KeybindMulti.fromStorageString(defaultHotkey, KeybindSettings.DEFAULT);
    }

    public ConfigTriggerHotkey setTriggerAction(Runnable action) {
        this.triggerAction = action;
        return this;
    }
    public void trigger() {
        if (triggerAction != null) {
            triggerAction.run();
        }
    }

    public ConfigTriggerHotkey setCallback(IHotkeyCallback callback) {
        this.keybind.setCallback(callback);

        this.triggerAction = ()->callback.onKeyAction(KeyAction.PRESS , KeybindMulti.fromStorageString("",KeybindSettings.DEFAULT));
        return this;
    }


    @Override
    public IKeybind getKeybind() {
        return this.keybind;
    }

    @Override
    public String getStringValue() {
        return this.keybind.getStringValue();
    }

    @Override
    public String getDefaultStringValue() {
        return this.keybind.getDefaultStringValue();
    }

    @Override
    public void setValueFromString(String value) {
        this.keybind.setValueFromString(value);
    }

    @Override
    public boolean isModified() {
        return this.keybind.isModified();
    }

    @Override
    public boolean isModified(String newValue) {
        return this.keybind.isModified(newValue);
    }

    @Override
    public void resetToDefault() {
        this.keybind.resetToDefault();
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        try {
            if (element.isJsonPrimitive()) {
                this.keybind.setValueFromString(element.getAsString());
            }
        } catch (Exception ignored) {}
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(this.keybind.getStringValue());
    }

    // IHotkeyTogglable 需要的方法 - 这里我们只用hotkey部分
    @Override
    public boolean getBooleanValue() {
        return true;
    }

    @Override
    public boolean getDefaultBooleanValue() {
        return true;
    }

    @Override
    public void setBooleanValue(boolean value) {
        // 不需要开关功能
    }
}
