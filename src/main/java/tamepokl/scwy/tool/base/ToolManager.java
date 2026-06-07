package tamepokl.scwy.tool.base;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import tamepokl.scwy.Reference;
import tamepokl.scwy.config.ToolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ToolManager {
    public static final List<ToolBase> TOOLS = new ArrayList<>();
    public static final String KEY_CATEGORY = "scwy.hotkeys.category.general";
    private static boolean hasInit = false;

    public static void init() {
        try {
            //所有需要被加载的tool
            Class.forName("tamepokl.scwy.tool.MaceKill");
            Class.forName("tamepokl.scwy.tool.CloseContainer");
            Class.forName("tamepokl.scwy.tool.FlyAbility");
//            Class.forName("tamepokl.scwy.tool.PacketRecorder");
            Class.forName("tamepokl.scwy.tool.HomeCommandRedirectTool");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        hasInit = true;
    }

    //获取配置列表
    public static List<IHotkeyTogglable> getConfigList() {
        if (!hasInit) {
            init();
        }
        return TOOLS.stream().map(ToolBase::getConfig).toList();
    }

    public static void addKeysToMap(IKeybindManager manager) {
        getKeybind().forEach(manager::addKeybindToMap);
    }

    public static void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME, KEY_CATEGORY, getHotkeys());
    }

    //获取热键
    private static List<IHotkey> getHotkeys() {
        List<IHotkey> hotkeys = new ArrayList<>();

        getConfigList().forEach(config -> {
            hotkeys.add(config);
            if (config instanceof ToolConfig tool) {
                tool.getChildren().forEach(child -> {
                    if (child instanceof IHotkey hotkey) hotkeys.add(hotkey);
                });
            }
        });
        return hotkeys;
    }

    //获取children
    public static List<IConfigBase> getChildrenConfig() {
        List<IConfigBase> children = new ArrayList<>(List.of());
        getConfigList().forEach(config -> {
            if (config instanceof ToolConfig tool) {
                children.addAll(tool.children);
            }
        });
        return children;
    }

    private static List<IKeybind> getKeybind() {
        return getHotkeys().stream().map(IHotkey::getKeybind).toList();
    }

    public static void addTool(ExpandableTool tool) {
        TOOLS.add(tool);
    }

    public static void initTools() {
        TOOLS.forEach(ToolBase::init);
    }

    //在第一个Tick执行
    public static boolean ticked = false;
    public static List<Consumer<Minecraft>> tickList = new ArrayList<>();

    static {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ticked = false;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ticked = false;
        });
        ClientTickEvents.START_WORLD_TICK.register((level) -> {
            if (ticked) return;
            for (Consumer<Minecraft> con : new ArrayList<>(tickList)) {
                con.accept(Minecraft.getInstance());
            }
            ticked = true;
        });
    }

    public static void onFirstTick(Consumer<Minecraft> consumer) {
        tickList.add(consumer);
    }


}
