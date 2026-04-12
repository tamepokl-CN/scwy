package tamepokl.scwy.tool;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import tamepokl.scwy.Reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ToolManager {


    public static final List<Tool.EventRegisterEntry<?>> REGISTER_EVENT = new ArrayList<>();
    public static final List<Tool> TOOLS = new ArrayList<>();
    public static final String KEY_CATEGORY = "scwy.hotkeys.category.general";

    public static void init() {
        try {
            Class.forName("tamepokl.scwy.tool.MaceKill");
//          Class.forName("tamepokl.scwy.tool.Test");
//          Class.forName("tamepokl.scwy.tool.TPBuild");
            Class.forName("tamepokl.scwy.tool.autoFill.AutoFill");
            Class.forName("tamepokl.scwy.tool.LitematicaHelper");
            Class.forName("tamepokl.scwy.tool.Autolt");
            Class.forName("tamepokl.scwy.tool.AutoCollect");
            Class.forName("tamepokl.scwy.tool.CloseContainer");
            Class.forName("tamepokl.scwy.tool.Tool1");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ToolConfig> getToolConfigList() {
        init();
        return TOOLS.stream().map(Tool::getToolConfig).toList();
    }

    public static void addKeysToMap(IKeybindManager manager) {
        getKeybind().forEach(manager::addKeybindToMap);
    }

    public static void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME, KEY_CATEGORY, getHotkeys());
    }

    private static List<IHotkey> getHotkeys() {
        List<IHotkey> hotkeys = new ArrayList<>();
        getToolConfigList().forEach(toolConfig -> {
            hotkeys.add(toolConfig.getToolGuiConfig());
            toolConfig.children.forEach(base -> {
                if (base instanceof IHotkey) hotkeys.add((IHotkey) base);
            });
        });
        return hotkeys;
    }

    private static List<IKeybind> getKeybind() {
        return getHotkeys().stream().map(IHotkey::getKeybind).toList();
    }

    public static void addTool(Tool tool) {
        TOOLS.add(tool);
    }

    public static void initTools() {
        registerEvent();
        TOOLS.forEach(Tool::init);
    }

    public static void registerEvent() {
        for (Tool.EventRegisterEntry entry : REGISTER_EVENT) {
            entry.getEvent().register(entry.getCallback());
        }
        for (Tool tool : TOOLS) {
            ClientTickEvents.END_CLIENT_TICK.register(tool::onTick0);
        }
    }


}
