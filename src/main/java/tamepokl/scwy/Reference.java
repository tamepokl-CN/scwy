package tamepokl.scwy;

import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
    public static final String MOD_ID = "scwy";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final String MOD_NAME = "scwy";
    public static final String MOD_VERSION = StringUtils.getModVersionString(MOD_ID);
    public static final String MC_VERSION = SharedConstants.getCurrentVersion().id();
}
