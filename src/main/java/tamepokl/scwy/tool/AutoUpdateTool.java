package tamepokl.scwy.tool;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


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
                        checkUpdate(true);

                    return 0;}))
                .then(ClientCommandManager.literal("now").executes(AutoUpdateTool::update))
                .then(ClientCommandManager.literal("ignore").executes(context -> {
                    INSTANCE.ignoreString.setValueFromString(data.tagName());
                    ScwyUtils.printTranslatableMessage("scwy.done", false);
                    ScwyUtils.printTranslatableMessage("scwy.update.unignoretip", false);
                return 0;
                }))
                .then(ClientCommandManager.literal("unignore").executes(context -> {
                    INSTANCE.ignoreString.setValueFromString("");
                    ScwyUtils.printTranslatableMessage("scwy.done", false);
                    return 0;
                }));

        ScwyClientCommands.addToScwy( update);

        ToolManager.addTool(INSTANCE);
        ClientLifecycleEvents.CLIENT_STARTED.register(c -> AutoUpdateTool.checkUpdate(false));
        ToolManager.onFirstTick(c -> {
            if(!INSTANCE.isEnabled())return;
            if (noticed) {
                return;
            }
            try {
                noticeUpdate();
            } catch (IOException ignored) {
            }
            noticed = true;
        });
    }

    private static int update(CommandContext<FabricClientCommandSource> ctx) {

        if (FileUtils.isDev()) {
            ctx.getSource().sendError(Component.translatable("scwy.update.dev"));
        }
        if (!INSTANCE.isEnabled() || updated){
            ctx.getSource().sendError(Component.translatable("scwy.update.disabled"));
        }

        CompletableFuture.runAsync(AutoUpdateTool::update);
        return 0;
    }
    private static volatile boolean isUpdating = false;

    private static void update() {
        if (updated || isUpdating || !INSTANCE.isEnabled() || FileUtils.isDev()) return;
        
        isUpdating = true;
        try {
            if (data == null) {
                throw new IOException("没有检查更新");
            }
            if (!isAvailable()) {
                throw new IOException("没有更新");
            }
            
            Asset releaseAsset = findReleaseAsset();
            if (releaseAsset == null) {
                throw new IOException("没有找到文件");
            }

            downloadUpdateFile(releaseAsset.browserDownloadUrl(), releaseAsset.name());
            ScwyUtils.printTranslatableMessage("scwy.update.success", false);
            updated = true;
        } catch (Exception e) {
            ScwyUtils.printTranslatableMessage("scwy.update.updatefailed", false, e.getMessage());
            Reference.LOGGER.error(e);
        } finally {
            isUpdating = false;
        }
    }

    public AutoUpdateTool(String name, boolean b) {
        super(name, b);
    }

    private static void checkUpdate(boolean notice) {
        CompletableFuture.runAsync(()->fetchUpdate(notice));

    }

    private static void fetchUpdate(boolean notice) {
        Reference.LOGGER.info("Checking Update...");

        try {
            data = check.getLatestRelease();
                if(INSTANCE.isEnabled()) {
                        if (isAvailable()) {
                            Reference.LOGGER.info("New update available: " + data.tagName());
                            if (notice) {
                                noticeUpdate();
                            }
                        } else {
                            if (notice) {
                                ScwyUtils.printTranslatableMessage("scwy.update.none", false);
                            }
                        }
                }
        } catch (IOException e) {
            Reference.LOGGER.warn("Failed to check for updates: " + e);
            if (notice) {
                ScwyUtils.printTranslatableMessage("scwy.update.failed", false, e);
            }
        }
    }

    private static Asset findReleaseAsset() {
        if (data == null) return null;
        String targetName = ScwyUtils.buildFileName(data.tagName());
        return data.assets().stream()
                .filter(asset -> asset.name().equals(targetName))
                .findFirst()
                .orElse(null);
    }

    private static boolean isAvailable() {
        return INSTANCE.isEnabled() 
                && !FileUtils.isDev()
                && data != null 
                && !isIgnore() 
                && !ScwyUtils.removeV(data.tagName()).equals(Reference.MOD_VERSION);
    }

    private static boolean isIgnore() {
        return data != null && INSTANCE.ignoreString.getStringValue().equals(data.tagName());
    }

    private static void noticeUpdate() throws IOException {
        if (!INSTANCE.isEnabled() || data == null || isIgnore()) return;

        String body = data.body();
        body = new String(body.getBytes(), StandardCharsets.UTF_8);
        if (Objects.equals(body, "")) body = "无";
        ScwyUtils.printTranslatableMessage("scwy.update.available", false,
                data.tagName(), data.publishedAt(), body);
        printClickMessage();
    }


    private static void printClickMessage() {
        if (!INSTANCE.isEnabled() || data == null) return;

        Component updateNow = Component.translatable("scwy.update.updateNow")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/scwy update now")));
        Component ignore = Component.translatable("scwy.update.ignore")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/scwy update ignore")));
        Component show = Component.translatable("scwy.update.show")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(data.htmlUrl()))));
        ScwyUtils.printMessage(updateNow, false);
        ScwyUtils.printMessage(ignore, false);
        ScwyUtils.printMessage(show, false);
    }

    public static void downloadUpdateFile(String fileUrl,String saveName) throws Exception{
        Path saveFolder = FileUtils.getModDirectory();

        if (saveFolder == null || !saveFolder.toFile().exists()) {
                throw new IOException("Mod文件夹错误" + saveFolder);
        }

        ScwyUtils.printTranslatableMessage("scwy.update.start", true);
        URL url = URI.create(fileUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");

        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP 响应码异常: " + responseCode);
        }
        Path filePath = saveFolder.resolve(saveName);

        // 获取文件总大小（可能为 -1，表示未知）
        int contentLength = conn.getContentLength();

        Reference.LOGGER.info("Downloading "+saveName + " to " + filePath.toAbsolutePath());

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(filePath.toFile())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            long lastPrintTime = System.currentTimeMillis();

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPrintTime >= 100) {
                    printProgress(totalRead, contentLength);
                    lastPrintTime = currentTime;
                }
            }
            printProgress(totalRead, contentLength);
            Reference.LOGGER.info("Downloaded: " + filePath.toAbsolutePath());
            removeModSelf();
        }
    }

    private static void printProgress(long downloaded, int total) {
        if (total > 0) {
            int percent = (int) (downloaded * 100 / total);
            ScwyUtils.printTranslatableMessage("scwy.update.downloading", true, percent);
        } else {
            ScwyUtils.printTranslatableMessage("scwy.update.downloading", true);
        }
    }

    public static void removeModSelf() {
        if(FileUtils.isDev()){
            return;
        }
        //实现有些困难，让玩家自己删

        Component showFile = Component.translatable("scwy.update.showfile").withStyle(Style.EMPTY.withClickEvent(
                new ClickEvent.OpenFile(Objects.requireNonNull(FileUtils.getModDirectory()).toAbsolutePath().toString())));
        ScwyUtils.printMessage(showFile,false);
    }
}
