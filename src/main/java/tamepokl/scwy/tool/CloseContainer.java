package tamepokl.scwy.tool;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.util.tuples.Pair;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

import java.util.Map;
import java.util.Objects;

public class CloseContainer extends ExpandableTool {
    public final Map<Class<? extends AbstractContainerMenu>, ConfigBooleanHotkeyed> MENUS = Map.ofEntries(
            Map.entry(AnvilMenu.class, config.ofBooleanHotkeyed("AnvilMenu", "")),
            Map.entry(BeaconMenu.class, config.ofBooleanHotkeyed("BeaconMenu", "")),
            Map.entry(BlastFurnaceMenu.class, config.ofBooleanHotkeyed("BlastFurnaceMenu", "")),
            Map.entry(BrewingStandMenu.class, config.ofBooleanHotkeyed("BrewingStandMenu", "")),
            Map.entry(CartographyTableMenu.class, config.ofBooleanHotkeyed("CartographyTableMenu", "")),
            Map.entry(ChestMenu.class, config.ofBooleanHotkeyed("ChestMenu", "",true)),
            Map.entry(CrafterMenu.class, config.ofBooleanHotkeyed("CrafterMenu", "")),
            Map.entry(DispenserMenu.class, config.ofBooleanHotkeyed("DispenserMenu", "")),
            Map.entry(EnchantmentMenu.class, config.ofBooleanHotkeyed("EnchantmentMenu", "")),
            Map.entry(FurnaceMenu.class, config.ofBooleanHotkeyed("FurnaceMenu", "")),
            Map.entry(GrindstoneMenu.class, config.ofBooleanHotkeyed("GrindstoneMenu", "")),
            Map.entry(HopperMenu.class, config.ofBooleanHotkeyed("HopperMenu", "")),
            Map.entry(HorseInventoryMenu.class, config.ofBooleanHotkeyed("HorseInventoryMenu", "")),
            Map.entry(InventoryMenu.class, config.ofBooleanHotkeyed("InventoryMenu", "")),
            Map.entry(LecternMenu.class, config.ofBooleanHotkeyed("LecternMenu", "")),
            Map.entry(MerchantMenu.class, config.ofBooleanHotkeyed("MerchantMenu", "")),
            Map.entry(NautilusInventoryMenu.class, config.ofBooleanHotkeyed("NautilusInventoryMenu", "")),
            Map.entry(ShulkerBoxMenu.class, config.ofBooleanHotkeyed("ShulkerBoxMenu", "")),
            Map.entry(SmithingMenu.class, config.ofBooleanHotkeyed("SmithingMenu", "")),
            Map.entry(SmokerMenu.class, config.ofBooleanHotkeyed("SmokerMenu", "")),
            Map.entry(StonecutterMenu.class, config.ofBooleanHotkeyed("StonecutterMenu", ""))
    );

    public static final CloseContainer INSTANCE = new CloseContainer("closeContainer");
    static {
        ToolManager.addTool(INSTANCE);
    }
    public CloseContainer(String name) {
        super(name);
    }
    public void onContainerOpen(CallbackInfo ci, AbstractContainerMenu menu){
        if(isEnabled()){
            ConfigBooleanHotkeyed config = MENUS.get(menu.getClass());
            if(config != null && config.getBooleanValue()) {
                Objects.requireNonNull(Minecraft.getInstance().player).closeContainer();
            }

        }
    }
}
