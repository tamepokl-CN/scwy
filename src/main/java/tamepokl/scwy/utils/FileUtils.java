package tamepokl.scwy.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;
import tamepokl.scwy.Reference;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileUtils {
    static {

    }

    public static void init() {

    }
    public static Path getModDirectory() {
        if(isDev()) return null;
        Path path = getScwyModPath();
        Path current = path.getParent();
        while (current != null) {
            if ("mods".equals(current.getFileName().toString())) {
                return current;
            }
            current = current.getParent();
        }
        throw new RuntimeException("Could not find mod directory");
    }

    private static @NonNull Path getScwyModPath() {
        ModContainer container = FabricLoader.getInstance().getModContainer(Reference.MOD_ID).get();
        Path path = container.getOrigin().getPaths().stream().findFirst().get();
        return path;
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void downloadUpdateFile(String fileUrl,String saveName) throws Exception{
        if(isDev()) return;
        Path saveFolder = getModDirectory();

        if (!saveFolder.toFile().exists()) {
                throw new IOException("Mod文件夹错误" + saveFolder);
        }

        URL url = new URL(fileUrl);
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

        System.out.println("Downloading "+saveName + " to " + filePath.toAbsolutePath());

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
            System.out.println("Downloaded: " + filePath.toAbsolutePath());
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

    public static void removeModSelf() throws Exception {
        //实现有些困难，让玩家自己删
        Component showFile = Component.translatable("scwy.update.showfile").withStyle(Style.EMPTY.withClickEvent(
                new ClickEvent.OpenFile(getScwyModPath())));
        ScwyUtils.printMessage(showFile,false);
    }
}
