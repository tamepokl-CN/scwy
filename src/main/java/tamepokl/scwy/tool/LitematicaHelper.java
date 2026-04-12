package tamepokl.scwy.tool;

import fi.dy.masa.itemscroller.util.AccessorUtils;
import fi.dy.masa.itemscroller.util.InventoryUtils;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import me.fallenbreath.tweakermore.mixins.tweaks.features.autoCollectMaterialListItem.MaterialListHudRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tamepokl.scwy.tool.ToolManager.addTool;

/*
* 来源：https://github.com/zhaixianyu/wuhu-client/blob/1.X/src/main/java/com/zxy/wuhuclient/features_list/litematica_helper/LitematicaHelper.java
* 作者：zhaixianyu
* */
public class LitematicaHelper extends Tool {
    public static final LitematicaHelper INSTANCE = new LitematicaHelper("litematicaHelper");
    public LitematicaHelper(String name) {
        super(name);
    }
    static {
        addTool(INSTANCE);
    }

    public Map<ItemStack, Map<BlockPos,Integer>> inventoryList = new HashMap<>();

    public static void process(AbstractContainerMenu container) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        AbstractContainerScreen<?> containerScreen;
        if(client.screen instanceof AbstractContainerScreen){
            containerScreen = (AbstractContainerScreen<?>)client.screen;
        }else return;
        List<Slot> playerInvSlots = container.slots.stream().filter(slot -> slot.container instanceof Inventory).collect(Collectors.toList());
        List<Slot> containerInvSlots = container.slots.stream().filter(slot -> areSlotsInSameInventory(slot, container.slots.get(0),false)).collect(Collectors.toList());

        MaterialListBase materialList = DataManager.getMaterialList();
        if (materialList != null) {
            MaterialListHudRendererAccessor hudRendererAccessor = (MaterialListHudRendererAccessor) materialList.getHudRenderer();
            String guiTitle = containerScreen.getTitle().getString();

            // refresh before operation starts to make sure it's up-to-date
            MaterialListUtils.updateAvailableCounts(materialList.getMaterialsAll(), player);
            List<MaterialListEntry> missingOnly = materialList.getMaterialsMissingOnly(true);

            boolean takenSomething = false;
            for (MaterialListEntry entry : missingOnly) {
                int missing = entry.getCountMissing() * materialList.getMultiplier() - entry.getCountAvailable();
                ItemStack stack = entry.getStack();
                if (missing <= 0) {
                    continue;
                }
                int totalTaken = 0;
                for (Slot slot : containerInvSlots) {
                    if (InventoryUtils.areStacksEqual(stack, slot.getItem())) {
                        int stackAmount = slot.getItem().getCount();
                        moveToPlayerInventory(containerScreen, playerInvSlots, slot, Math.min(missing, stackAmount));
                        int moved = stackAmount - slot.getItem().getCount();
                        missing -= moved;
                        totalTaken += moved;
                        if (moved == 0) {
                            break;
                        }
                    }
                }
                if (totalTaken > 0) {

                    takenSomething = true;
                    log(Message.MessageType.INFO, "从%1$s中收集了:", guiTitle);

                    String missingColor = missing == 0 ? GuiBase.TXT_GREEN : GuiBase.TXT_GOLD;
                    String stackName = stack.getRarity().color() + stack.getHoverName().getString() + GuiBase.TXT_RST;

                    log(
                            Message.MessageType.INFO,
                            "- %1$s 个%2$s，仍需 %3$s",
                            GuiBase.TXT_GOLD + totalTaken + GuiBase.TXT_RST,
                            stackName,
                            missingColor + hudRendererAccessor.invokeGetFormattedCountString(missing, stack.getMaxStackSize()) + GuiBase.TXT_RST
                    );

                }
            }
            if (!takenSomething) log(Message.MessageType.INFO, "未在%1$s中收集任何材料列表中需要的物品", guiTitle);

            // refresh after operation ends
            hudRendererAccessor.setLastUpdateTime(-1);
        } else {
            log(Message.MessageType.WARNING, "没有生效的材料列表");
        }
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.closeContainer();
        }
    }

    public static void process0(AbstractContainerMenu container) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        AbstractContainerScreen<?> containerScreen;
        if(client.screen instanceof AbstractContainerScreen){
            containerScreen = (AbstractContainerScreen<?>)client.screen;
        }else return;
        List<Slot> playerInvSlots = container.slots.stream().filter(slot -> slot.container instanceof Inventory).collect(Collectors.toList());
        List<Slot> containerInvSlots = container.slots.stream().filter(slot -> areSlotsInSameInventory(slot, container.slots.get(0),false)).collect(Collectors.toList());

        MaterialListBase materialList = DataManager.getMaterialList();
        if (materialList != null) {
            MaterialListHudRendererAccessor hudRendererAccessor = (MaterialListHudRendererAccessor) materialList.getHudRenderer();
            String guiTitle = containerScreen.getTitle().getString();

            // refresh before operation starts to make sure it's up-to-date
            MaterialListUtils.updateAvailableCounts(materialList.getMaterialsAll(), player);
            List<MaterialListEntry> missingOnly = materialList.getMaterialsMissingOnly(true);

            boolean takenSomething = false;
            for (MaterialListEntry entry : missingOnly) {
                int missing = entry.getCountMissing() * materialList.getMultiplier() - entry.getCountAvailable();
                ItemStack stack = entry.getStack();
                if (missing <= 0) {
                    continue;
                }
                int totalTaken = 0;
                for (Slot slot : containerInvSlots) {
                    if (InventoryUtils.areStacksEqual(stack, slot.getItem())) {
                        int stackAmount = slot.getItem().getCount();
                        moveToPlayerInventory(containerScreen, playerInvSlots, slot, Math.min(missing, stackAmount));
                        int moved = stackAmount - slot.getItem().getCount();
                        missing -= moved;
                        totalTaken += moved;
                        if (moved == 0) {
                            break;
                        }
                    }
                }
                if (totalTaken > 0) {

                    takenSomething = true;
                    log(Message.MessageType.INFO, "从%1$s中收集了:", guiTitle);

                    String missingColor = missing == 0 ? GuiBase.TXT_GREEN : GuiBase.TXT_GOLD;
                    String stackName = stack.getRarity().color() + stack.getHoverName().getString() + GuiBase.TXT_RST;

                    log(
                            Message.MessageType.INFO,
                            "- %1$s 个%2$s，仍需 %3$s",
                            GuiBase.TXT_GOLD + totalTaken + GuiBase.TXT_RST,
                            stackName,
                            missingColor + hudRendererAccessor.invokeGetFormattedCountString(missing, stack.getMaxStackSize()) + GuiBase.TXT_RST
                    );

                }
            }
            if (!takenSomething) log(Message.MessageType.INFO, "未在%1$s中收集任何材料列表中需要的物品", guiTitle);

            // refresh after operation ends
            hudRendererAccessor.setLastUpdateTime(-1);
        } else {
            log(Message.MessageType.WARNING, "没有生效的材料列表");
        }
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.closeContainer();
        }
    }

    private static void moveToPlayerInventory(AbstractContainerScreen<?> containerScreen, List<Slot> playerInvSlots, Slot fromSlot, int amount) {
        ItemStack stack = fromSlot.getItem().copy();
        if (amount == stack.getCount()) {
            InventoryUtils.shiftClickSlot(containerScreen, fromSlot.index);
            return;
        } else if (amount > stack.getCount()) {
            return;
        }
        // ensure amount <= stack.getCount()

        InventoryUtils.leftClickSlot(containerScreen, fromSlot.index);
        // reversed iterating to match vanilla shift-click item putting order
        for (int idx = playerInvSlots.size() - 1; idx >= 0; idx--) {
            Slot slot = playerInvSlots.get(idx);
            int clickAmount = 0;
            if (slot.hasItem() && InventoryUtils.areStacksEqual(slot.getItem(), stack)) {
                ItemStack invStack = slot.getItem();
                clickAmount = Math.min(invStack.getMaxStackSize() - invStack.getCount(), amount);
            } else if (!slot.hasItem()) {
                clickAmount = amount;
            }
            for (int i = 0; i < clickAmount; i++) InventoryUtils.rightClickSlot(containerScreen, slot.index);
            amount -= clickAmount;
            if (amount == 0) {
                break;
            }
        }
        InventoryUtils.leftClickSlot(containerScreen, fromSlot.index);

    }

    private static void log(Message.MessageType type, String translationKey, Object... args) {
        InfoUtils.showGuiOrInGameMessage(type, translationKey, args);
    }

    private static boolean areSlotsInSameInventory(Slot slot1, Slot slot2, boolean treatHotbarAsDifferent) {
        if (slot1.container != slot2.container) {
            return false;
        } else if (treatHotbarAsDifferent && slot1.container instanceof Inventory) {
            int index1 = AccessorUtils.getSlotIndex(slot1);
            int index2 = AccessorUtils.getSlotIndex(slot2);
            return index1 == 40 || index2 == 40 || index1 < 9 == index2 < 9;
        } else {
            return true;
        }
    }


}
