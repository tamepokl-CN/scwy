package tamepokl.scwy;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;

public class Scwy implements ModInitializer {
	@Override
	public void onInitialize() {
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());

	}
}