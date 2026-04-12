package tamepokl.scwy.tool;

import fi.dy.masa.itemscroller.util.InventoryUtils;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tamepokl.scwy.utils.CommandUtils;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class AutoCollect extends Tool {
    public static final AutoCollect INSTANCE = new AutoCollect("autoCollect");

    public final ConfigInteger INDEX = config.ofInteger("index", 10, 0, 99);
    public final ConfigInteger MAXINDEX = config.ofInteger("maxIndex", 20, 0, 99);

    static {
        addTool(INSTANCE);
    }

    public AutoCollect(String name) {
        super(name);
    }

    @Override
    public void onTick(Minecraft mc) {
        boolean lose = hasDirt(mc);
        if (!lose && !(Minecraft.getInstance().screen instanceof InventoryScreen)) {
            open();
        }
    }

    private static boolean hasDirt(Minecraft mc) {
        boolean lose = false;
        for (ItemStack itemStack : mc.player.getInventory().getNonEquipmentItems()) {
            if (itemStack.is(Items.HEART_POTTERY_SHERD)) {
                lose = true;
                break;
            }
        }
        return lose;
    }

    private static boolean hasAir(Minecraft mc) {
        boolean b = false;
        for (ItemStack itemStack : mc.player.getInventory().getNonEquipmentItems()) {
            if (itemStack.is(Items.AIR) || itemStack.isEmpty()) {
                b = true;
                break;
            }
        }
        return b;
    }

    public void process(AbstractContainerMenu menu) {
        if(!(Minecraft.getInstance().screen instanceof InventoryScreen) && !hasDirt(Minecraft.getInstance()) && hasAir(Minecraft.getInstance())) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tamepokl.scwy.utils.InventoryUtils.fliterContainerInventory(menu.slots).forEach(slot -> {
                ItemStack itemStack = slot.getItem();
                if (itemStack.is(Items.HEART_POTTERY_SHERD)) {
                    InventoryUtils.shiftClickSlot((AbstractContainerScreen<? extends AbstractContainerMenu>) Minecraft.getInstance().screen, slot.index);
                }
            });
            tamepokl.scwy.utils.InventoryUtils.fliterPlayerInventory(menu.slots).forEach(s->{
                ItemStack item = s.getItem();
                if(item.is(Items.POTION)){
                    InventoryUtils.dropStack((AbstractContainerScreen<? extends AbstractContainerMenu>) Minecraft.getInstance().screen, s.index);
                }
            });
            Minecraft.getInstance().player.closeContainer();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (hasAir(Minecraft.getInstance())) {
                open();
            }else{
                Minecraft.getInstance().getConnection().sendChat("#resume");
            }
        }else{
            Minecraft.getInstance().player.closeContainer();
        }
    }

    public void open() {
        if (INDEX.getIntegerValue() < MAXINDEX.getIntegerValue()) {
            CommandUtils.sendCommand("pv " + INDEX.getIntegerValue());
            INDEX.setIntegerValue(INDEX.getIntegerValue() + 1);
        }else{
            AutoCollect.INSTANCE.getToolConfig().getToolGuiConfig().setBooleanValue(false);
        }

    }

}
