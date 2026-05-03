package tamepokl.scwy.event;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;
import tamepokl.scwy.config.Configs;
import tamepokl.scwy.gui.GuiConfigs;
import tamepokl.scwy.tool.base.ToolManager;

import static tamepokl.scwy.Reference.MOD_NAME;

public class InputHandler implements IKeybindProvider {
    public static final InputHandler INSTANCE = new InputHandler();
    private InputHandler() {
        Configs.Generic.OPEN_GUI_HOTKEY.getKeybind().setCallback(new OpenGuiCallback());

    }

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        //TODO：挪走OPENGUI
        manager.addKeybindToMap(Configs.Generic.OPEN_GUI_HOTKEY.getKeybind());

        ToolManager.addKeysToMap( manager);

    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(MOD_NAME,"scwy.hotkeys.category.general", ImmutableList.of(Configs.Generic.OPEN_GUI_HOTKEY));
        ToolManager.addHotkeys(manager);
    }



    private static class OpenGuiCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, fi.dy.masa.malilib.hotkeys.IKeybind key) {
            if (action != KeyAction.PRESS) {
                return false;
            }
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.setScreen(new GuiConfigs());
            }
            return true;
        }
    }
}
