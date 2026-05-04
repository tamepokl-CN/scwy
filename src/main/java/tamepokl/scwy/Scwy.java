package tamepokl.scwy;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;
import tamepokl.scwy.command.ScwyClientCommands;

public class Scwy implements ModInitializer {
	@Override
	public void onInitialize() {
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
		init();
	}

    private static void init() {

		ScwyClientCommands.init();
    }
}