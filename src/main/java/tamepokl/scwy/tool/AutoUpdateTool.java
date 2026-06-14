package tamepokl.scwy.tool;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import ovh.neziw.checker.ReleaseCheck;
import ovh.neziw.checker.ReleaseCheckBuilder;
import ovh.neziw.checker.release.Asset;
import ovh.neziw.checker.release.ReleaseData;
import tamepokl.scwy.Reference;
import tamepokl.scwy.command.ScwyClientCommands;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;
import tamepokl.scwy.utils.FileUtils;
import tamepokl.scwy.utils.ScwyUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class AutoUpdateTool extends ExpandableTool {
    public static final ReleaseCheck check = ReleaseCheckBuilder.builder()
            .setRepositoryOwner("tamepokl-CN")
            .setRepositoryName("scwy")
            .build();
    public static boolean updated = false;
    public static final AutoUpdateTool INSTANCE = new AutoUpdateTool("autoUpdate", true);
    public static final LiteralArgumentBuilder<FabricClientCommandSource> update;
    public static ReleaseData data;
    public static boolean noticed = false;
    public  ConfigString ignoreString = config.ofString("ignore", "");

    static {
        update = ClientCommandManager.literal("update")
                .then(ClientCommandManager.literal("check")
                    .executes(context -> {
                        getUpdate(true);

                    return 0;}))
                .then(ClientCommandManager.literal("now").executes(AutoUpdateTool::update))
                .then(ClientCommandManager.literal("ignore").executes(context -> {
                    INSTANCE.ignoreString.setValueFromString(data.tagName());
                    ScwyUtils.printTranslatableMessage("scwy.done", false);
                return 0;
                }));
        ScwyClientCommands.addToScwy( update);

        ToolManager.addTool(INSTANCE);
        ClientLifecycleEvents.CLIENT_STARTED.register(c -> AutoUpdateTool.getUpdate(false));
        ToolManager.onFirstTick(c -> {
            if(!INSTANCE.isEnabled())return;
            if (noticed) {
                return;
            }
            try {
                noticeUpdate();
            } catch (IOException e) {
            }
            noticed = true;
        });
    }

    private static int update(CommandContext<FabricClientCommandSource> ctx) {
        if(!INSTANCE.isEnabled())return 0;
        if(updated)return 0;

        if(FileUtils.isDev()) ctx.getSource().sendError(Component.translatable("scwy.update.dev"));

        new Thread(AutoUpdateTool::update).start();
        return 0;
    }
    private static volatile boolean isUpdating = false;

    private static void update() {
        if(updated) return;
        if(isUpdating) return;
        isUpdating = true;
        if(!INSTANCE.isEnabled())return;

        try {
        if(data == null){
           throw new IOException("没有检查更新");
        }
            if (!check.isNewerVersionAvailable(Reference.MOD_VERSION)) {
                throw new IOException("没有更新");
            }
            Asset releaseAsset = null;
            for (Asset asset : data.assets()) {
                if(asset.name().equals(ScwyUtils.buildFileName(data.tagName()))){
                    releaseAsset = asset;
                    break;
                }
            }
            if(releaseAsset == null){
                throw new IOException("没有找到文件");
            }

            FileUtils.downloadUpdateFile(releaseAsset.browserDownloadUrl(),releaseAsset.name());

            ScwyUtils.printTranslatableMessage("scwy.update.success", false);
            updated = true;
        } catch (Exception e) {
            ScwyUtils.printTranslatableMessage("scwy.update.updatefailed", false,e.getMessage());
            Reference.LOGGER.error(e);
        }finally {
            isUpdating = false;
        }

    }

    public AutoUpdateTool(String name, boolean b) {
        super(name, b);
    }

    private static void getUpdate(boolean notice) {
        new Thread(() -> {
            if(!INSTANCE.isEnabled())return;

            try {
                data = check.getLatestRelease();
                check(notice);
            } catch (IOException e) {
                Reference.LOGGER.warn("Failed to check for updates: " + e);
                if (notice) {
                    ScwyUtils.printTranslatableMessage("scwy.update.failed", false, e);
                }
            }
        }).start();

    }

    private static void check(boolean notice) throws IOException {

        try {
            if(!INSTANCE.isEnabled())return;

            if (isIgnore()) return;
            if (check.isNewerVersionAvailable(Reference.MOD_VERSION)) {
                Reference.LOGGER.info("New update available: " + data.tagName());
                if (notice) {
                    noticeUpdate();
                }
            } else {
                if (notice) {
                    ScwyUtils.printTranslatableMessage("scwy.update.none", false);
                }
            }
        } catch (IOException e) {
            Reference.LOGGER.warn("Failed to check for updates: " + e);
        }
    }

    private static boolean isIgnore() throws IOException {
        return INSTANCE.ignoreString.getStringValue().equals(data.tagName());
    }

    private static void noticeUpdate() throws IOException {
        if(!INSTANCE.isEnabled())return;

        if (data == null) {
            return;
        }
        if(isIgnore())return;

        String body = data.body();
        if (Objects.equals(body, "")) body = "无";
        ScwyUtils.printTranslatableMessage("scwy.update.available", false,
                data.tagName(), data.publishedAt(),body);
        printClickMessage();
    }


    private static void printClickMessage() {
        if(!INSTANCE.isEnabled())return;

        Component updateNow = Component.translatable("scwy.update.updateNow").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/scwy update now")));
    Component ignore = Component.translatable("scwy.update.ignore").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/scwy update ignore")));
    Component show = Component.translatable("scwy.update.show").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(data.htmlUrl()))));
    ScwyUtils.printMessage(updateNow,false);
    ScwyUtils.printMessage(ignore,false);
    ScwyUtils.printMessage(show,false);
    }
}
