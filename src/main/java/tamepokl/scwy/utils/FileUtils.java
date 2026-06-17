package tamepokl.scwy.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jspecify.annotations.NonNull;
import tamepokl.scwy.Reference;

import java.nio.file.Path;

public class FileUtils {


    public static Path getModDirectory() {
        if (isDev()) return null;
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

    public static @NonNull Path getScwyModPath() {
        return FabricLoader.getInstance()
                .getModContainer(Reference.MOD_ID)
                .map(ModContainer::getOrigin)
                .map(ModOrigin::getPaths)
                .flatMap(paths -> paths.stream().findFirst())
                .orElseThrow(() -> new RuntimeException("Could not find mod path"));
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }


}
