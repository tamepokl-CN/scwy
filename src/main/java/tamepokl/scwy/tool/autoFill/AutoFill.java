package tamepokl.scwy.tool.autoFill;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import tamepokl.scwy.Reference;
import tamepokl.scwy.config.ConfigTriggerHotkey;
import tamepokl.scwy.tool.LitematicaHelper;
import tamepokl.scwy.tool.Tool;
import tamepokl.scwy.utils.HighlightBlockRenderer;
import tamepokl.scwy.utils.InventoryUtils;
import tamepokl.scwy.utils.LitematicaUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.CHEST_TYPE;
import static tamepokl.scwy.tool.ToolManager.addTool;
//TODO:重构
public class AutoFill extends Tool {
    public static final AutoFill INSTANCE = new AutoFill("autoFill");
    public final ConfigBooleanHotkeyed addTarget = config.ofBooleanHotkeyed("addTarget", "");
    public final ConfigString addTargetTool = config.ofString("addTargetTool", "minecraft:golden_axe");
    public final ConfigTriggerHotkey refresh = config.ofTriggerHotkey("refreshMaterialList", "");
    public final ConfigBooleanHotkeyed enabled = config.ofBooleanHotkeyed("autoFillEnabled", "");
    public final ConfigTriggerHotkey litematica = config.ofTriggerHotkey("litematica", "");
    public final ConfigTriggerHotkey toggleUncheckedVisible = config.ofTriggerHotkey("toggleUncheckedVisible", "");
    public final ConfigBooleanHotkeyed replace = config.ofBooleanHotkeyed("replace", "");
    public final ConfigStringList replaceMap = config.ofStringList("replaceMap", ImmutableList.of("minecraft:barrier," +
            "minecraft:command_block=>minecraft:iron_nugget"));
    public final ConfigTriggerHotkey clearEveryThing = config.ofTriggerHotkey("clearEveryThing", "");

    //    public final ConfigTriggerHotkey autoUpdate = config.ofTriggerHotkey("autoUpdate", "");
    public ConfigColor notCheckConfig = null;
    public Item addTargetToolItem = Items.GOLDEN_AXE;
    public final Set<PositionKey> targets = new HashSet<>();
    public final Map<PositionKey, ItemEntry> materials = new HashMap<>();
    public BlockPos lastTarget = new BlockPos(0, 0, 0);
    public Set<PositionKey> notChecked = new HashSet<>();
    public boolean clearedSight = false;
    public BlockPos lastClicked = new BlockPos(0, 0, 0);
    public List<CompletableFuture<Void>> fillFutures = new ArrayList<>();
    public List<PositionKey> targetCache = new ArrayList<>();
    public List<PositionKey> previousTarget = new ArrayList<>();
    public Map<Item, Item> replaceItems = new HashMap<>();


    static {
        addTool(INSTANCE);
    }

    private boolean waitForClose;
    public boolean inGui = false;


    {
        litematica.setCallback((keyAction, keybind) -> {
            for (LitematicaUtils.BoxInfo boxInfo : LitematicaUtils.getCurrentSelectionInfo()) {
                for (BlockPos pos : boxInfo.getAllPositions()) {
                    if (InventoryUtils.isContainer(LitematicaUtils.getSchematicBlockState(pos).getBlock())) {
                        addTarget(pos);
                    }
                }
            }
            return true;

        });
        toggleUncheckedVisible.setCallback((keyAction, keybind) -> {
//            notCheckConfig.setIntegerValue();
            notCheckConfig.setIntegerValue(notCheckConfig.getIntegerValue() == 2147418112 ? 16711680 : 2147418112);
            return true;
        });

        notCheckConfig = new ConfigColor("autoFillNotCheck", new Color4f(255, 0, 0, 0.5f));
        //TODO:写一个BlockDetector
        HighlightBlockRenderer.createHighlightBlockList("autoFillSight", new Color4f(255, 0, 0, 0.5f), false);
        HighlightBlockRenderer.createHighlightBlockList("autoFillTarget", new Color4f(0, 255, 0, 0.5f), false);
        HighlightBlockRenderer.createHighlightBlockList("autoFillAble", new Color4f(0, 0, 255, 0.5f), true);
        HighlightBlockRenderer.createHighlightBlockList("autoFillNotCheck", notCheckConfig, true);
        addTargetTool.setValueChangeCallback(this::parseToolItem);
        addTarget.setValueChangeCallback(config -> {
            if (addTarget.getBooleanValue()) {
                InfoUtils.printActionbarMessage("scwy.info.addTarget.onEnabled",
                        addTargetToolItem.getName().getString(),
                        addTarget.getKeybind().getKeysDisplayString());
                enabled.setBooleanValue(false);
            } else {
                InfoUtils.printActionbarMessage("scwy.info.addTarget.onDisabled", targets.size());
                HighlightBlockRenderer.clear("autoFillSight");
                refresh();
            }
        });
        replaceMap.setValueChangeCallback(this::parseReplaceMap);
        replace.setValueChangeCallback(config -> parseReplaceMap(replaceMap));
        addRegisterEvent(ClientLifecycleEvents.CLIENT_STOPPING, client -> {
            this.addTarget.setBooleanValue(false);
            this.enabled.setBooleanValue(false);
            notCheckConfig.setIntegerValue(2147418112);
        });
        addRegisterEvent(ClientPlayConnectionEvents.JOIN,
                (handler, sender, client) -> {
                    this.parseToolItem(addTargetTool);
                    this.parseReplaceMap(replaceMap);
                });

        addRegisterEvent(UseItemCallback.EVENT, (player, world, hand) -> {
            if (isEnabled() && checkItemInHand() & addTarget.getBooleanValue()) {
                handleTargetSelection(player, world);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        addRegisterEvent(UseBlockCallback.EVENT, (player, world, hand, hitResult) -> {
            lastClicked = hitResult.getBlockPos();
            //我不知道为什么点击方块会触发3次Callback
            //不过这样能用就是了
            if (hitResult.getBlockPos() instanceof BlockPos.MutableBlockPos) {
                if (isEnabled() && checkItemInHand() && addTarget.getBooleanValue()) {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockState schematicState = LitematicaUtils.getSchematicBlockState(pos);

                    if (schematicState != null && InventoryUtils.isContainer(schematicState.getBlock())) {
                        handleTargetSelection(pos);
                        BlockState worldState = world.getBlockState(pos);
                        boolean isMatch = worldState.getBlock().equals(schematicState.getBlock());

                        if (isMatch) {
                            lastTarget = pos;
                            return InteractionResult.SUCCESS;
                        } else {
                            return InteractionResult.FAIL;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        refresh.setCallback((action, key) -> {
            refresh();
            return true;
        });
        enabled.setValueChangeCallback(config -> {
            targetCache.clear();
            if (enabled.getBooleanValue()) {
                InfoUtils.printActionbarMessage("scwy.info.autoFill.onEnabled");
                addTarget.setBooleanValue(false);
                runCheck();
            } else {
                InfoUtils.printActionbarMessage("scwy.info.autoFill.onDisabled");
            }
        });
        clearEveryThing.setCallback((action, key) -> {
            clearEveryThing();
            return true;
        });
//        autoUpdate.setCallback((action, key) -> {
//            addTarget.setBooleanValue(false);
//            enabled.setBooleanValue(false);
//            return true;
//        });

    }

    private void parseReplaceMap(ConfigStringList config1) {
        replaceItems.clear();
        for (String text : config1.getStrings()) {
            if (Objects.equals(text, "")) continue;
            String entry = text.replaceAll("\\s+", "");
            try {
                String[] parts = entry.split("=>");
                if (parts.length != 2) {
                    parseReplaceMapInvalid(entry, "Too Many Parts");
                }

                String[] sourceItems = parts[0].split(",");
                List<Item> sourceItemList = new ArrayList<>();
                for (String itemId : sourceItems) {
                    Identifier identifier = Identifier.tryParse(itemId.trim());
                    if (identifier == null) {
                        parseReplaceMapInvalid(entry, "Parse Source Identifier Failed");
                    }
                    BuiltInRegistries.ITEM.get(identifier).ifPresentOrElse(
                            item -> sourceItemList.add(item.value()),
                            () -> {
                                parseReplaceMapInvalid(entry, "Invalid Source Item Identifier");
                            }
                    );
                }

                Identifier targetIdentifier = Identifier.tryParse(parts[1].trim());
                if (targetIdentifier == null) {
                    parseReplaceMapInvalid(entry, "Parse Target Identifier Failed");
                }
                Item targetItem = BuiltInRegistries.ITEM.get(targetIdentifier)
                        .map(Holder.Reference::value)
                        .orElse(null);
                if (targetItem == null) {
                    parseReplaceMapInvalid(entry, "Invalid Target Item Identifier");
                }
                for (Item item : sourceItemList) {
                    if (replaceItems.containsKey(item)) {
                        parseReplaceMapInvalid(entry, "Duplicate Source Item");
                    }
                    replaceItems.put(item, targetItem);
                }


            } catch (RuntimeException e) {
                InfoUtils.showGuiAndInGameMessage(Message.MessageType.ERROR, "scwy.error.replaceMap.invalid", entry,
                        e.getMessage());
                replaceItems.clear();
            }
        }
    }

    private void parseReplaceMapInvalid(String entry, String error) throws RuntimeException {
        throw new RuntimeException(error);
    }

    private void refresh() {
        try {
            List<MaterialListEntry> list = recreate();
            MaterialListBase materlist = LitematicaUtils.createCustomMaterialList(list);
            LitematicaUtils.setActiveMaterialList(materlist);
            MaterialListUtils.updateAvailableCounts(list, Minecraft.getInstance().player);
        } catch (RuntimeException e) {
            catchException(e);
        }
    }

    private List<MaterialListEntry> recreate() {
        Map<Item, MutableMaterialListEntry> map = new HashMap<>();
        materials.values().forEach(itemEntry -> {
            for (ItemStack totalItem : itemEntry.totalItems) {
                if (totalItem.getItem() != Items.AIR) {

                    MutableMaterialListEntry entry = map.getOrDefault(totalItem.getItem(),
                            new MutableMaterialListEntry(totalItem));
                    entry.countTotal += totalItem.getCount();
                    map.put(totalItem.getItem(), entry);
                }
            }
            for (ItemStack availableItem : itemEntry.availableItems) {
                if (availableItem.getItem() != Items.AIR) {
                    assert map.containsKey(availableItem.getItem());
                    MutableMaterialListEntry entry = map.getOrDefault(availableItem.getItem(),
                            new MutableMaterialListEntry(availableItem));
                    entry.countAvailable += availableItem.getCount();
                    map.put(availableItem.getItem(), entry);
                }
            }
        });
        map.values().forEach(entry -> {
            entry.countMissing = entry.countTotal - entry.countAvailable;
        });
        return map.values().stream().map(MutableMaterialListEntry::toMaterialListEntry).toList();
    }

    private int tickCounter = 0;

    @Override
    public void onTick(Minecraft mc) {
        try {
            if (addTarget.getBooleanValue() && checkItemInHand()) {
                clearedSight = false;
                handleSchematicBlockInSight(mc);
            } else {
                HighlightBlockRenderer.clear("autoFillSight");
                clearedSight = true;
            }

            if (enabled.getBooleanValue()) {
                tickCounter++;
                if (tickCounter >= 10) {
                    runCheck();
                    tickCounter = 0;
                }
                if (waitForClose && !fillFutures.isEmpty()) {
                    boolean allCompleted = fillFutures.stream().allMatch(CompletableFuture::isDone);
                    if (allCompleted) {
                        fillFutures.removeIf(CompletableFuture::isDone);
                        if (fillFutures.isEmpty()) {
                            waitForClose = false;
                            refresh();
                            Minecraft.getInstance().player.closeContainer();
                        }
                    }
                }
            } else {
                tickCounter = 0;
                fillFutures.clear();
                waitForClose = false;
            }
        } catch (RuntimeException e) {
            catchException(e);
        }
    }
    //处理视线方块

    private void handleSchematicBlockInSight(Minecraft mc) {
        BlockPos pos = LitematicaUtils.getSchematicBlockInSight(Objects.requireNonNull(mc.player));
        if (pos != null) {
            BlockState state = LitematicaUtils.getSchematicBlockState(pos);
            if (state != null && InventoryUtils.isContainer(state.getBlock())) {
                Set<BlockPos> sightTargets = new HashSet<>();
                if (state.getBlock().equals(Blocks.CHEST)) {
                    handleChestSight(pos, state, sightTargets);
                } else {
                    sightTargets.add(pos);
                }
                HighlightBlockRenderer.setPos("autoFillSight", sightTargets);
            }
        } else {
            HighlightBlockRenderer.clear("autoFillSight");
        }
    }

    @Override
    public void onEnabled(Minecraft mc) {
    }

    @Override
    public void onDisabled(Minecraft mc) {
        clearEveryThing();

    }

    private void clearEveryThing() {
        targets.clear();
        materials.clear();
        targetCache.clear();
        fillFutures.clear();
        notChecked.clear();
        previousTarget.clear();
        HighlightBlockRenderer.clear("autoFillSight");
        HighlightBlockRenderer.clear("autoFillTarget");
        HighlightBlockRenderer.clear("autoFillAble");
        HighlightBlockRenderer.clear("autoFillNotCheck");
        addTarget.setBooleanValue(false);
        enabled.setBooleanValue(false);
    }
    //处理工具添加

    public void parseToolItem(ConfigString configString) {
        Identifier identifier = Identifier.tryParse(configString.getStringValue());
        if (identifier == null) {
            InfoUtils.showGuiAndInGameMessage(Message.MessageType.ERROR, "scwy.info.addTargetTool.invalid");
            configString.resetToDefault();
            addTargetToolItem = Items.GOLDEN_AXE;
            return;
        }
        BuiltInRegistries.ITEM.get(identifier).ifPresentOrElse(
                item -> {
                    addTargetToolItem = item.value();
                },
                () -> {
                    InfoUtils.showGuiAndInGameMessage(Message.MessageType.ERROR, "scwy.info.addTargetTool.invalid");
                    configString.resetToDefault();
                    addTargetToolItem = Items.GOLDEN_AXE;
                }
        );
    }


    public AutoFill(String name) {
        super(name);
    }

    //添加目标
    private void handleTargetSelection(Player player, Level level) {
        try {
            BlockPos pos = LitematicaUtils.getSchematicBlockInSight(player);
            if (pos != null) {
                handleTargetSelection(pos);
            }
        } catch (RuntimeException e) {
            catchException(e);
        }
    }

    private void handleTargetSelection(BlockPos pos) {
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        if (state != null && InventoryUtils.isContainer(state.getBlock())) {
            if (state.getBlock().equals(Blocks.CHEST)) {
                handleChestTarget(pos, state);
            } else {
                toggleTarget(pos);
            }


        }
    }

    private void handleChestTarget(BlockPos pos, BlockState state) {
        Collection<Property<?>> properties = state.getProperties();
        if (properties.contains(CHEST_TYPE)) {
            var chestType = state.getValue(CHEST_TYPE);
            switch (chestType) {
                case SINGLE:
                    toggleTarget(pos);
                    break;
                case LEFT:
                    BlockPos rightPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
                    toggleLinkedTargets(pos, rightPos);
                    break;
                case RIGHT:
                    BlockPos leftPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise());
                    toggleLinkedTargets(pos, leftPos);
                    break;
            }
        }
    }

    private void handleChestSight(BlockPos pos, BlockState state, Set<BlockPos> sightTargets) {
        Collection<Property<?>> properties = state.getProperties();
        if (properties.contains(CHEST_TYPE)) {
            var chestType = state.getValue(CHEST_TYPE);
            switch (chestType) {
                case SINGLE:
                    sightTargets.add(pos);
                    break;
                case LEFT:
                    BlockPos rightPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
                    sightTargets.add(pos);
                    sightTargets.add(rightPos);
                    break;
                case RIGHT:
                    BlockPos leftPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise());
                    sightTargets.add(pos);
                    sightTargets.add(leftPos);
                    break;
            }
        }
    }


    private void toggleTarget(BlockPos pos) {
        if (targetsContains(pos)) {
            removeTarget(pos);
            previousTarget = new ArrayList<>() {{
                add(new PositionKey(pos));
            }};
        } else {
            addTarget(pos);
        }
    }

    private void toggleLinkedTargets(BlockPos pos1, BlockPos pos2) {
        if (targetsContains(pos1) || targetsContains(pos2)) {
            removeTarget(pos1);
            removeTarget(pos2);
            previousTarget = new ArrayList<>() {{
                add(new PositionKey(pos1));
                add(new PositionKey(pos2));
            }};
        } else {
            addTarget(pos1);
            addTarget(pos2);

        }
    }

    private void addTarget(BlockPos pos) {
        targets.add(new PositionKey(pos));
        updateMaterials(pos);
        HighlightBlockRenderer.setPos("autoFillTarget", toBlockSet(targets));

        notChecked.add(new PositionKey(pos));
        HighlightBlockRenderer.setPos("autoFillNotCheck", toBlockSet(notChecked));
    }

    public static final class ItemEntry {
        public List<ItemStack> totalItems;
        public List<ItemStack> availableItems;

        public ItemEntry(List<ItemStack> totalItems, List<ItemStack> availableItems) {
            this.totalItems = new ArrayList<>(totalItems);
            this.availableItems = new ArrayList<>(availableItems);
        }


        @Override
        public int hashCode() {
            return Objects.hash(totalItems, availableItems);
        }

        @Override
        public String toString() {
            return "ItemEntry[" +
                    "totalItems=" + totalItems + ", " +
                    "availableItems=" + availableItems + ']';
        }

        public List<ItemStack> getMissingItems() {
            ArrayList<ItemStack> stacks = new ArrayList<>();
            assert totalItems.size() == availableItems.size();
            for (int i = 0; i < totalItems.size(); i++) {
                assert totalItems.get(i).getItem() == availableItems.get(i).getItem();
                int minus = totalItems.get(i).getCount() - availableItems.get(i).getCount();
                if (minus == 0) {
                    stacks.add(i, new ItemStack(Items.AIR, 0));
                } else {
                    stacks.add(i, new ItemStack(totalItems.get(i).getItem(), minus));
                }
            }
            return stacks;
        }

    }


    //处理物品
    //更新materials，然后refresh
    public void process(AbstractContainerMenu container) {
        try {
            if (isEnabled()) {

                if (enabled.getBooleanValue()) {
                    if (targetCache.contains(new PositionKey(lastClicked))) {
                        InfoUtils.showInGameMessage(Message.MessageType.INFO, "scwy.info.addTarget.already_success",
                                lastClicked.toShortString());
                        Minecraft.getInstance().player.closeContainer();
                    }
                    if (!isTarget(lastClicked)) {
                        LitematicaHelper.process0(container);
                        runCheck();
                    } else {
                        if (!isSameChestType(lastClicked)) {
                            InfoUtils.showInGameMessage(Message.MessageType.ERROR, "scwy.autoFill.error.isNotSameType");
                        } else {
                            fillProcess(container, lastClicked);
                        }
                    }
                    return;
                } else if (addTarget.getBooleanValue()) {


                    if (targetsContains(lastTarget)) {
                        if (isSameChestType(lastTarget)) {
                            updateMaterials(container, lastTarget, false);
                        } else {
                            InfoUtils.showInGameMessage(Message.MessageType.ERROR, "scwy.autoFill.error.isNotSameType");
                            return;
                        }
                    }
                    if (targetsContains(lastTarget) || previousTarget.contains(new PositionKey(lastClicked))) {
                        if (checkItemInHand()) {
                            Objects.requireNonNull(Minecraft.getInstance().player).closeContainer();
                            return;
                        }
                    }
                    if (targetsContains(lastClicked) && isSameChestType(lastClicked)) {
                        inGui = true;
                    }
                    return;
                }
                //            if (updating && isSameChestType(lastClicked)) {
                //                updateMaterials(container, lastClicked, false);
                //                Minecraft.getInstance().player.closeContainer();
                //                return;
                //            }
            }
        } catch (RuntimeException e) {
            catchException(e);
        }

    }

    private boolean isSameChestType(BlockPos pos) {
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        BlockState levelState = Minecraft.getInstance().level.getBlockState(pos);
        Collection<Property<?>> levelProperties = levelState.getProperties();
        Collection<Property<?>> properties = state.getProperties();
        if (properties.contains(CHEST_TYPE) && properties.contains(CHEST_TYPE)) {
            return state.getValue(CHEST_TYPE) == levelState.getValue(CHEST_TYPE);
        } else {
            if (state.getBlock() != levelState.getBlock()) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean checkItemInHand() {
        return Objects.requireNonNull(Minecraft.getInstance().player).getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(addTargetToolItem);
    }

    public void updateMaterials(AbstractContainerMenu container, BlockPos pos, boolean skipChest) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        BlockState levelState = Objects.requireNonNull(level).getBlockState(pos);
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        List<ItemStack> contents = LitematicaUtils.getSchematicContainerContent(pos);
        List<ItemStack> slots =
                InventoryUtils.fliterContainerInventory(container.slots).stream().map(Slot::getItem).toList();
        if (state != null && InventoryUtils.isContainer(state.getBlock())) {
            if (!skipChest) {
                if (state.getBlock().equals(Blocks.CHEST)) {
                    Collection<Property<?>> properties = state.getProperties();
                    if (properties.contains(CHEST_TYPE)) {
                        List<ItemStack> left;
                        List<ItemStack> right;
                        var chestType = state.getValue(CHEST_TYPE);
                        switch (chestType) {
                            case SINGLE:
                                updateMaterials(pos, contents, slots);
                                break;
                            case LEFT:
                                BlockPos rightPos =
                                        pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
                                right = slots.subList(0, 27);
                                left = slots.subList(27, 54);
                                List<ItemStack> rightContent = LitematicaUtils.getSchematicContainerContent(rightPos);

                                updateMaterials(pos, contents, left);
                                updateMaterials(rightPos, rightContent, right);


                                break;
                            case RIGHT:
                                BlockPos leftPos =
                                        pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise());
                                right = slots.subList(0, 27);
                                left = slots.subList(27, 54);
                                List<ItemStack> leftContent = LitematicaUtils.getSchematicContainerContent(leftPos);
                                updateMaterials(pos, contents, right);
                                updateMaterials(leftPos, leftContent, left);
                                break;
                        }
                    }

                } else {
                    updateMaterials(pos, contents, slots);
                }
            } else {
                updateMaterials(pos, contents, slots);
            }

        }
//        LitematicaUtils.getSchematicContainerContent()
    }

    private void updateMaterials(BlockPos pos, List<ItemStack> contents, List<ItemStack> slots) {
        ItemEntry entry = new ItemEntry(contents, slots);
        Map<Integer, Boolean> index = getMismatchIndex(contents, slots);
        if (!index.isEmpty()) {
            InfoUtils.showInGameMessage(Message.MessageType.WARNING, "scwy.info.addTarget.mismatch");
            index.forEach((integer, value) -> {
                if (value) {
                    entry.availableItems.remove((int) integer);
                } else {
                    entry.availableItems.get(integer).setCount(contents.get(integer).getCount());
                }
            });

        } else if (InventoryUtils.equals(entry.totalItems, entry.availableItems)) {
            InfoUtils.showInGameMessage(Message.MessageType.INFO, "scwy.info.addTarget.already_success",
                    pos.toShortString());
            removeTarget(pos);
            return;
        }
        materials.put(new PositionKey(pos), entry);
        if (notChecked.contains(new PositionKey(pos))) {
            notChecked.remove(new PositionKey(pos));
            HighlightBlockRenderer.setPos("autoFillNotCheck", toBlockSet(notChecked));
        }
    }

    private @NotNull Set<BlockPos> toBlockSet(Set<PositionKey> notChecked) {
        return notChecked.stream().map(PositionKey::toBlockPos).collect(Collectors.toSet());
    }

    private void updateMaterials(BlockPos pos) {
        List<ItemStack> contents = LitematicaUtils.getSchematicContainerContent(pos);
        List<ItemStack> list = Stream.generate(() -> new ItemStack(Items.AIR, 0))
                .limit(contents.size())
                .toList();
        ItemEntry entry = new ItemEntry(contents, list);
        materials.put(new PositionKey(pos), entry);
    }

    private void removeTarget(BlockPos pos) {

        materials.remove(new PositionKey(pos));
        targets.remove(new PositionKey(pos));
        HighlightBlockRenderer.setPos("autoFillTarget", toBlockSet(targets));
    }

    public Map<Integer, Boolean> getMismatchIndex(List<ItemStack> total, List<ItemStack> available) {
        Map<Integer, Boolean> indexs = new LinkedHashMap<>();
        for (int i = 0; i < available.size(); i++) {
            ItemStack a = available.get(i);
            ItemStack t = total.get(i);
            boolean isDifferentItem = a.getItem() != Items.AIR && a.getItem() != t.getItem();
            if ((isDifferentItem) || a.getCount() > t.getCount()) {
                indexs.put(i, isDifferentItem);
            }
        }
        return indexs;
    }

    public static class PositionKey {
        private final int x, y, z;

        public PositionKey(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        public PositionKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PositionKey other)) return false;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }

    public static final class MutableMaterialListEntry {
        public ItemStack item;
        public int countTotal;
        public int countMissing;
        public int countMismatched;
        public int countAvailable;

        public MutableMaterialListEntry(
                ItemStack item,
                int countTotal,
                int countMissing,
                int countMismatched,
                int countAvailable
        ) {
            this.item = item;
            this.countTotal = countTotal;
            this.countMissing = countMissing;
            this.countMismatched = countMismatched;
            this.countAvailable = countAvailable;
        }

        public MutableMaterialListEntry(ItemStack item) {
            this.item = item;
            this.countAvailable = 0;
            this.countMismatched = 0;
            this.countMissing = 0;
            this.countTotal = 0;
        }

        public MaterialListEntry toMaterialListEntry() {
            return new MaterialListEntry(item, countTotal, countMissing, countMismatched, countAvailable);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MutableMaterialListEntry) obj;
            return Objects.equals(this.item, that.item) &&
                    this.countTotal == that.countTotal &&
                    this.countMissing == that.countMissing &&
                    this.countMismatched == that.countMismatched &&
                    this.countAvailable == that.countAvailable;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, countTotal, countMissing, countMismatched, countAvailable);
        }

        @Override
        public String toString() {
            return "MutableMaterialListEntry[" +
                    "item=" + item + ", " +
                    "countTotal=" + countTotal + ", " +
                    "countMissing=" + countMissing + ", " +
                    "countMismatched=" + countMismatched + ", " +
                    "countAvailable=" + countAvailable + ']';
        }

    }


    public void runCheck() {
        try {
            Set<Item> items =
                    Minecraft.getInstance().player.getInventory().getNonEquipmentItems().stream().map(ItemStack::getItem).collect(Collectors.toSet());
            Set<BlockPos> able = new HashSet<>();
            for (Map.Entry<PositionKey, ItemEntry> entry : materials.entrySet()) {
                PositionKey pos = entry.getKey();
                ItemEntry value = entry.getValue();

                for (ItemStack item : value.getMissingItems()) {
                    if (item.getItem() != Items.AIR && items.contains(item.getItem())) {
                        able.add(pos.toBlockPos());
                        break;

                    }
                }
            }
            HighlightBlockRenderer.setPos("autoFillAble", able);
        } catch (RuntimeException e) {
            catchException(e);
        }
    }

    public void fillProcess(AbstractContainerMenu container, BlockPos pos) {
        ItemEntry entry = materials.get(new PositionKey(pos));
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        Collection<Property<?>> properties = state.getProperties();
        List<ItemStack> items = getItems(container);
        if (properties.contains(CHEST_TYPE)) {
            List<ItemStack> left;
            List<ItemStack> right;
            var chestType = state.getValue(CHEST_TYPE);
            int i;
            switch (chestType) {
                case SINGLE:
                    i = InventoryUtils.processInvOpen(container, entry.totalItems);
                    fillCheck(i, pos, entry.totalItems, () -> new ArrayList<>(getItems(container)));
                    break;
                case LEFT:
                    BlockPos rightPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
                    if (!targetsContains(pos)) {
                        //只有right
                        assert targetsContains(rightPos);
                        left = LitematicaUtils.getSchematicContainerContent(pos);
                        right = materials.get(new PositionKey(rightPos)).totalItems;
                        i = InventoryUtils.processInvOpen(container, Stream.concat(right.stream(),
                                left.stream()).toList());
                        fillCheck(i, rightPos, right, () -> new ArrayList<>(getItems(container)).subList(0, 27));
                        break;
                    } else {
                        if (!targetsContains(rightPos)) {
                            //只有left
                            left = materials.get(new PositionKey(pos)).totalItems;
                            right = LitematicaUtils.getSchematicContainerContent(rightPos);
                            i = InventoryUtils.processInvOpen(container, Stream.concat(right.stream(),
                                    left.stream()).toList());
                            fillCheck(i, pos, left, () -> new ArrayList<>(getItems(container)).subList(27, 54));
                            break;
                        }
                    }
                    //都有
                    ItemEntry rightEntry = materials.get(new PositionKey(rightPos));
                    left = entry.totalItems;
                    right = rightEntry.totalItems;
                    i = InventoryUtils.processInvOpen(container, Stream.concat(right.stream(),
                            left.stream()).toList());
                    fillCheck(i, pos, left, () -> new ArrayList<>(getItems(container)).subList(27, 54));
                    fillCheck(i, rightPos, right, () -> new ArrayList<>(getItems(container)).subList(0, 27));

                    break;
                case RIGHT:
                    BlockPos leftPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise());
                    if (!targetsContains(pos)) {
                        //只有left
                        left = materials.get(new PositionKey(leftPos)).totalItems;
                        right = LitematicaUtils.getSchematicContainerContent(pos);
                        i = InventoryUtils.processInvOpen(container, Stream.concat(right.stream(),
                                left.stream()).toList());
                        fillCheck(i, leftPos, left, () -> new ArrayList<>(getItems(container)).subList(27, 54));
                        break;
                    } else {
                        if (!targetsContains(leftPos)) {
                            //只有right
                            left = LitematicaUtils.getSchematicContainerContent(leftPos);
                            right = materials.get(new PositionKey(pos)).totalItems;
                            i = InventoryUtils.processInvOpen(container,
                                    Stream.concat(right.stream(), left.stream()).toList());
                            fillCheck(i, pos, right, () -> new ArrayList<>(getItems(container)).subList(0, 27));
                        } else {
                            //都有
                            left = materials.get(new PositionKey(leftPos)).totalItems;
                            right = entry.totalItems;
                            i = InventoryUtils.processInvOpen(container, Stream.concat(right.stream(),
                                    left.stream()).toList());
                            fillCheck(i, leftPos, left, () -> new ArrayList<>(getItems(container)).subList(27, 54));
                            fillCheck(i, pos, right, () -> new ArrayList<>(getItems(container)).subList(0, 27));
                            break;
                        }
                    }
                    break;
            }
        } else {
            int i = InventoryUtils.processInvOpen(container, entry.totalItems);
            fillCheck(i, pos, entry.totalItems, () -> new ArrayList<>(getItems(container)));

        }
        if (!fillFutures.isEmpty()) {
            waitForClose = true;
        }

    }

    private static @NotNull List<ItemStack> getItems(AbstractContainerMenu container) {
        List<ItemStack> items =
                InventoryUtils.fliterContainerInventory(container.slots).stream().map(Slot::getItem).toList();
        return items;
    }

    public boolean isTarget(BlockPos pos) {
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        Collection<Property<?>> properties = state.getProperties();
        if (properties.contains(CHEST_TYPE)) {
            var chestType = state.getValue(CHEST_TYPE);
            switch (chestType) {
                case SINGLE:
                    return targetsContains(pos);


                case LEFT:
                    BlockPos rightPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
                    return targetsContains(pos) || targetsContains(rightPos);
                case RIGHT:
                    BlockPos leftPos =
                            pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise());
                    return targetsContains(pos) || targetsContains(leftPos);

            }
        }
        return targetsContains(pos);
    }

    public void fillCheck(int result, BlockPos pos, List<ItemStack> contents, Supplier<List<ItemStack>> available) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        fillFutures.add(future);
        waitForClose = true;
        fillCheck(result, pos, contents, available, future);
    }

    public void fillCheck(int result, BlockPos pos, List<ItemStack> contents, Supplier<List<ItemStack>> available,
                          CompletableFuture<Void> completionFuture) {
        CompletableFuture.runAsync(() -> {
            try {
                //延时1tick,不然物品刷新不了
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).thenRunAsync(() -> {
            InfoUtils.showInGameMessage(Message.MessageType.INFO, "scwy.info.autoFill.move", pos.toShortString(),
                    result);
            if (InventoryUtils.equals(contents, available.get())) {
                removeTarget(pos);
                InfoUtils.showInGameMessage(Message.MessageType.SUCCESS, "scwy.info.autoFill.success",
                        pos.toShortString());
                targetCache.add(new PositionKey(pos));
            } else {
                updateMaterials(pos, contents, available.get());
            }

            if (completionFuture != null) {
                completionFuture.complete(null);
            }
        }, Minecraft.getInstance());
    }

    public boolean targetsContains(BlockPos pos) {
        return targets.contains(new PositionKey(pos));
    }


    public void catchException(RuntimeException e) {
        InfoUtils.showGuiAndInGameMessage(Message.MessageType.ERROR, "scwy.info.autoFill.exception", e);

        clearEveryThing();
        this.config.getToolGuiConfig().setBooleanValue(false);
        Reference.LOGGER.error(e);
    }

}
