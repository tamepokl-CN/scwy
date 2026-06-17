package tamepokl.scwy;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.minecraft.client.Minecraft;
import tamepokl.scwy.command.ScwyClientCommands;
import tamepokl.scwy.config.Configs;
import tamepokl.scwy.event.InputHandler;
import tamepokl.scwy.gui.GuiConfigs;
import tamepokl.scwy.tool.base.ToolManager;
import tamepokl.scwy.utils.FileUtils;
import tamepokl.scwy.utils.HighlightBlockRenderer;

import static tamepokl.scwy.Reference.*;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        Minecraft mc = Minecraft.getInstance();
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(MOD_ID, MOD_NAME, GuiConfigs::new)
        );
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        ToolManager.initTools();
        ConfigManager.getInstance().registerConfigHandler(MOD_ID, new Configs());
        HighlightBlockRenderer.init();



        LOGGER.debug("Scwy Init successfully :)");
    }
}
