package tamepokl.scwy.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import tamepokl.scwy.Reference;
import tamepokl.scwy.tool.base.ToolManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = "scwy.json";


    public static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "generic", Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "tools", Tools.OPTIONS);
                ConfigUtils.readConfigBase(root, "tweaks", Tweaks.OPTIONS);
                ConfigUtils.readConfigBase(root, "extra", Extra.OPTIONS);

            }
        } else {
            Reference.LOGGER.error("loadFromFile(): Failed to load config file '{}'.", configFile.toAbsolutePath());
        }
    }

    public static void saveToFile() {
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "generic", Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "tools", Tools.OPTIONS);
            ConfigUtils.writeConfigBase(root, "tweaks", Tweaks.OPTIONS);
            ConfigUtils.writeConfigBase(root, "extra", Extra.OPTIONS);

            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        } else {
            Reference.LOGGER.error("saveToFile(): Config Folder '{}' does not exist!", dir.toAbsolutePath());
        }
    }



    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }

    private static final Set<IConfigBase> CHILDREN = new HashSet<>();

    public static class Generic {
        public static final ConfigHotkey OPEN_GUI_HOTKEY =
                new ConfigHotkey("openGuiHotkey","B,C");
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(OPEN_GUI_HOTKEY);
    }
    public static class Tools {
        public static final ImmutableList<IConfigBase> OPTIONS;
        public static final ImmutableList<IConfigBase> TOP_LEVEL;
        public static final ImmutableList<IConfigBase> CHILDREN;

        static {
//            List<ToolConfig> tools = ToolManager.getToolConfigList();
//            Collection<IConfigBase> children = tools.stream().flatMap(toolConfig -> toolConfig.children.stream()).toList();
//           Collection<ToolGuiConfig> top = tools.stream().map(ToolConfig::getToolGuiConfig).toList();
//            Collection<ConfigToolGui> top = tools.stream().map(ToolConfig::getToolGuiConfig).toList();

            OPTIONS = ImmutableList.<IConfigBase>builder()
                    .addAll(ToolManager.getConfigList())
                    .addAll(ToolManager.getChildrenConfig())
                    .build();
            TOP_LEVEL = ImmutableList.copyOf(ToolManager.getConfigList());
            CHILDREN = ImmutableList.copyOf(ToolManager.getChildrenConfig());
        }
        public static boolean isChildConfig(IConfigBase config) {
            return CHILDREN.contains(config);
        }
    }
    public static class Tweaks {
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of();
        public static final ImmutableList<IConfigBase> TOP_LEVEL = ImmutableList.of();
        public static boolean isChildConfig(IConfigBase config) {
            return CHILDREN.contains(config);
        }
    }
    public static class Extra {
        public static final ImmutableList<IConfigBase> OPTIONS;
        public static final List<IConfigBase> CONFIG = new ArrayList<>();


        static {
            init();
            OPTIONS = ImmutableList.copyOf(CONFIG);
        }

        private static void init() {
        }
        public static void addExtra(IConfigBase base){
            CONFIG.add(base);
        }
    }
}