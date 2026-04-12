package tamepokl.scwy.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import tamepokl.scwy.tool.autoFill.AutoFill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.inventory.ClickType.PICKUP;
import static net.minecraft.world.inventory.ClickType.THROW;

public class InventoryUtils {
    public static boolean isContainer(Block block) {
        List<Block> container = List.of(
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.BARREL,
                Blocks.SHULKER_BOX,
                Blocks.DROPPER,
                Blocks.DISPENSER,
                Blocks.HOPPER
        );
        return container.contains(block);
    }
    public static boolean isContainerType(BlockEntity blockEntity){
        List<BlockEntityType<? extends RandomizableContainerBlockEntity>> container = List.of(
                BlockEntityType.CHEST,
                BlockEntityType.TRAPPED_CHEST,
                BlockEntityType.BARREL,
                BlockEntityType.SHULKER_BOX,
                BlockEntityType.DROPPER,
                BlockEntityType.DISPENSER,
                BlockEntityType.HOPPER
        );
        return container.contains(blockEntity.getType());
    }
    public static Map<Integer, ItemStack> scanContainerContents(AbstractContainerMenu container) {
        Map<Integer, ItemStack> contents = new HashMap<>();
        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.getSlot(i);
            if (slot.hasItem()) {
                contents.put(i, slot.getItem().copy());
            }
        }
        return contents;
    }
    public static List<Slot> fliterPlayerInventory(List<Slot> slots){
        return slots.stream().filter(slot -> slot.container instanceof Inventory).toList();
    }
    public static List<Slot> fliterContainerInventory(List<Slot> slots){
        return slots.stream().filter(slot -> !(slot.container instanceof Inventory)).toList();
    }
    //from wuhu-client
    public static int processInvOpen(AbstractContainerMenu sc, List<ItemStack> targetBlockInv){
        Minecraft client = Minecraft.getInstance();
        int size = Math.min(targetBlockInv.size(), sc.slots.get(0).container.getContainerSize());

        int times = 0;
        for (int i = 0; i < size; i++) {
            ItemStack item1 = sc.slots.get(i).getItem();
            ItemStack item2 = targetBlockInv.get(i).copy();
            int currNum = item1.getCount();
            int tarNum = item2.getCount();
            boolean same = equalsItem(item1, item2.copy()) && !item1.isEmpty();
            if (equalsItem(item1, item2) && currNum == tarNum) continue;
            //不和背包交互
            if (same) {
                //有多
                while (currNum > tarNum) {
                    client.gameMode.handleInventoryMouseClick(sc.containerId, i, 0, THROW, client.player);
                    currNum--;
                }
            } else {
                //不同直接扔出
                client.gameMode.handleInventoryMouseClick(sc.containerId, i, 1, THROW, client.player);
                times++;
            }
            boolean thereAreItems = false;
            //背包交互
            for (int i1 = size; i1 < sc.slots.size(); i1++) {
                ItemStack stack = sc.slots.get(i1).getItem();
                ItemStack currStack = sc.slots.get(i).getItem();
                currNum = currStack.getCount();
                boolean same2 = thereAreItems = equalsItem(item2, stack);
                if (same2 && !stack.isEmpty()) {
                    int i2 = stack.getCount();
                    client.gameMode.handleInventoryMouseClick(sc.containerId, i1, 0, PICKUP, client.player);
                    for (; currNum < tarNum && i2 > 0; i2--) {
                        client.gameMode.handleInventoryMouseClick(sc.containerId, i, 1,PICKUP, client.player);
                        currNum++;
                    }
                    client.gameMode.handleInventoryMouseClick(sc.containerId, i1, 0, PICKUP, client.player);
                }
                //这里判断没啥用，因为一个游戏刻操作背包太多次.getStack().getCount()获取的数量不准确 下次一定优化，
                if (currNum != tarNum) times++;
            }
            if (!thereAreItems) times++;
        }
        return times;
    }
    public static boolean equalsItem(ItemStack itemStack1,ItemStack itemStack2){
        //notice
        return ItemStack.isSameItemSameComponents(itemStack1, itemStack2);
//        AutoFill.INSTANCE.replaceMap.getStrings().contains()

    }
    public static boolean equalsItemStrict(ItemStack itemStack1,ItemStack itemStack2){

        return ItemStack.isSameItem(itemStack1, itemStack2);

    }
    public static boolean equalsItemandCount(ItemStack itemStack1,ItemStack itemStack2){
        return equalsItem(itemStack1, itemStack2) && itemStack1.getCount() == itemStack2.getCount();

    }


    public static boolean equals(List<ItemStack> list1,List<ItemStack> list2) {
        if(list1.size()!=list2.size())return false;
        boolean equals = true;
        for (int i = 0; i < list1.size(); i++) {

            ItemStack itemStack1 = list1.get(i);
            ItemStack itemStack2 = list2.get(i);
            if(!equalsItemandCount(itemStack1, itemStack2)){
                equals = false;
                break;
            }
        }
        return equals;
    }
}
