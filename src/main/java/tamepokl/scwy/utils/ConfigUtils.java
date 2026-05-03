package tamepokl.scwy.utils;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBase;
import tamepokl.scwy.Reference;

public class ConfigUtils {
    private static final String TOOL_KEY = Reference.MOD_ID + ".config.tool";
    public static <T extends IConfigBase> T apply(ConfigBase<T> base){
        return base.apply(TOOL_KEY);
    }
}
