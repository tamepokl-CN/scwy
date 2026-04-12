package tamepokl.scwy.utils;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListAreaAnalyzer;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListHudRenderer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.fallenbreath.tweakermore.mixins.tweaks.features.autoCollectMaterialListItem.MaterialListHudRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tamepokl.scwy.tool.autoFill.AutoFill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Litematica 工具类 - 获取投影缺失方块数据
 *
 * 使用方法：
 * 1. 确保已加载并选择了投影
 * 2. 调用 getMissingBlocksCount() 获取缺失方块数量
 * 3. 调用 getVerifierStatus() 获取详细状态信息
 */
public class LitematicaUtils {
    public static final double MAX_DISTANCE = 5.0d;

    /**
     * 获取当前选中的投影放置对象
     * @return 当前选中的SchematicPlacement，如果不存在则返回null
     */
    public static SchematicPlacement getSelectedSchematicPlacement() {
        return DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
    }

    /**
     * 检查是否有选中的投影
     * @return 如果有选中的投影返回true，否则返回false
     */
    public static boolean hasSelectedSchematicPlacement() {
        return getSelectedSchematicPlacement() != null;
    }

    /**
     * 启动或重启当前选中投影的验证器
     * @return 验证是否成功启动
     */
    public static boolean startSchematicVerification() {
        SchematicPlacement placement = getSelectedSchematicPlacement();
        if (placement == null) {
            return false;
        }

        SchematicVerifier verifier = placement.getSchematicVerifier();
        Minecraft mc = Minecraft.getInstance();
        ClientLevel worldClient = mc.level;
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldClient == null || worldSchematic == null) {
            return false;
        }

        // 重置并启动验证器
        verifier.reset();
        verifier.startVerification(worldClient, worldSchematic, placement, null);
        return true;
    }

    /**
     * 获取验证器的缺失方块统计信息
     * @return 缺失方块的数量
     */
    public static int getMissingBlocksCount() {
        SchematicPlacement placement = getSelectedSchematicPlacement();
        if (placement == null) {
            return 0;
        }

        SchematicVerifier verifier = placement.getSchematicVerifier();

        // 如果验证器未完成，启动验证但不等待
        if (!verifier.isFinished()) {
            startSchematicVerification();
        }

        return verifier.getMissingBlocks();
    }

    /**
     * 检查验证器是否已完成
     * @return 如果验证完成返回true，否则返回false
     */
    public static boolean isVerificationFinished() {
        SchematicPlacement placement = getSelectedSchematicPlacement();
        if (placement == null) {
            return true; // 没有投影视为"完成"
        }

        return placement.getSchematicVerifier().isFinished();
    }

    /**
     * 强制刷新验证器状态并获取最新数据
     * @return 最新的验证器状态
     */
    public static VerifierStatus refreshAndGetStatus() {
        SchematicPlacement placement = getSelectedSchematicPlacement();
        if (placement == null) {
            return new VerifierStatus(false, false, false, 0, 0, 0, 0, 0);
        }

        SchematicVerifier verifier = placement.getSchematicVerifier();
        // 触发状态更新
        verifier.getMissingBlocks(); // 调用此方法会触发内部状态计算

        return new VerifierStatus(
                verifier.isActive(),
                verifier.isPaused(),
                verifier.isFinished(),
                verifier.getMissingBlocks(),
                verifier.getExtraBlocks(),
                verifier.getMismatchedBlocks(),
                verifier.getMismatchedStates(),
                verifier.getTotalChunks()
        );
    }

    /**
     * 获取验证器状态信息
     * @return 验证器状态对象
     */
    public static VerifierStatus getVerifierStatus() {
        SchematicPlacement placement = getSelectedSchematicPlacement();
        if (placement == null) {
            return new VerifierStatus(false, false, false, 0, 0, 0, 0, 0);
        }

        SchematicVerifier verifier = placement.getSchematicVerifier();
        // 强制刷新状态以确保获取最新数据
        verifier.getMissingBlocks(); // 触发状态更新
        verifier.getExtraBlocks();
        verifier.getMismatchedBlocks();
        verifier.getMismatchedStates();
        //emm
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new VerifierStatus(
                verifier.isActive(),
                verifier.isPaused(),
                verifier.isFinished(),
                verifier.getMissingBlocks(),
                verifier.getExtraBlocks(),
                verifier.getMismatchedBlocks(),
                verifier.getMismatchedStates(),
                verifier.getTotalChunks()
        );
    }

    /**
     * 获取当前选区信息
     * @return 选区边界框列表
     */
    public static List<BoxInfo> getCurrentSelectionInfo() {
        AreaSelection selection = DataManager.getSelectionManager().getCurrentSelection();
        if (selection == null) {
            return Collections.emptyList();
        }

        List<BoxInfo> boxes = new ArrayList<>();
        List<Box> boxList = selection.getAllSubRegionBoxes();
        for (Box box : boxList) {
            boxes.add(new BoxInfo(
                    box.getName(),
                    box.getPos1(),
                    box.getPos2()
            ));
        }
        return boxes;
    }

    /**
     * 表示验证器状态的信息
     */
    public static class VerifierStatus {
        private final boolean active;
        private final boolean paused;
        private final boolean finished;
        private final int missingBlocks;
        private final int extraBlocks;
        private final int mismatchedBlocks;
        private final int mismatchedStates;
        private final int totalChunks;

        public VerifierStatus(boolean active, boolean paused, boolean finished,
                              int missingBlocks, int extraBlocks, int mismatchedBlocks,
                              int mismatchedStates, int totalChunks) {
            this.active = active;
            this.paused = paused;
            this.finished = finished;
            this.missingBlocks = missingBlocks;
            this.extraBlocks = extraBlocks;
            this.mismatchedBlocks = mismatchedBlocks;
            this.mismatchedStates = mismatchedStates;
            this.totalChunks = totalChunks;
        }

        // Getters
        public boolean isActive() {
            return active;
        }

        public boolean isPaused() {
            return paused;
        }

        public boolean isFinished() {
            return finished;
        }

        public int getMissingBlocks() {
            return missingBlocks;
        }

        public int getExtraBlocks() {
            return extraBlocks;
        }

        public int getMismatchedBlocks() {
            return mismatchedBlocks;
        }

        public int getMismatchedStates() {
            return mismatchedStates;
        }

        public int getTotalChunks() {
            return totalChunks;
        }

        public int getTotalErrors() {
            return missingBlocks + extraBlocks + mismatchedBlocks + mismatchedStates;
        }

        @Override
        public String toString() {
            return String.format("验证器状态: 活跃=%s, 暂停=%s, 完成=%s, " +
                            "缺失=%d, 多余=%d, 错误=%d, 状态错误=%d, 区块总数=%d",
                    active, paused, finished, missingBlocks, extraBlocks,
                    mismatchedBlocks, mismatchedStates, totalChunks);
        }
    }

    /**
     * 表示选区边界框的信息
     */
    public static class BoxInfo {
        private final String name;
        private final BlockPos pos1;
        private final BlockPos pos2;

        public BoxInfo(String name, BlockPos pos1, BlockPos pos2) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public String getName() {
            return name;
        }

        public BlockPos getPos1() {
            return pos1;
        }

        public BlockPos getPos2() {
            return pos2;
        }

        @Override
        public String toString() {
            return String.format("选区 '%s': %s 到 %s",
                    name, pos1.toShortString(), pos2.toShortString());
        }

        public ArrayList<BlockPos> getAllPositions() {
            if (this.pos1 == null || this.pos2 == null) {
                return new ArrayList<>();
            }

            // 计算范围
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            int sizeX = maxX - minX + 1;
            int sizeY = maxY - minY + 1;
            int sizeZ = maxZ - minZ + 1;
            int totalBlocks = sizeX * sizeY * sizeZ;

            ArrayList<BlockPos> positions = new ArrayList<>();

            int index = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }

            return positions;
        }
    }

    /**
     * 使用示例 - 异步处理方式
     */
    public static void demonstrateAsyncUsage() {
        System.out.println("=== LitematicaUtils 异步使用演示 ===");

        if (!hasSelectedSchematicPlacement()) {
            System.out.println("❌ 没有选中的投影！");
            return;
        }

        // 获取基本信息
        SchematicPlacement placement = getSelectedSchematicPlacement();
        System.out.println("✅ 当前投影: " + placement.getName());

        // 获取初始验证器状态
        VerifierStatus initialStatus = getVerifierStatus();
        System.out.println("📊 初始状态: " + initialStatus.toString());

        // 如果验证未完成，启动验证
        if (!initialStatus.isFinished()) {
            System.out.println("🔄 启动验证器...");
            startSchematicVerification();

            // 演示异步检查方式
            System.out.println("⏳ 验证进行中，可以通过以下方式检查进度：");
            System.out.println("   1. 调用 isVerificationFinished() 检查是否完成");
            System.out.println("   2. 调用 refreshAndGetStatus() 获取最新状态");
            System.out.println("   3. 定期调用 getVerifierStatus() 获取当前状态");

            // 演示异步检查方式（实际使用中应在游戏主循环中定期调用）
            System.out.println("   请在游戏主循环中定期调用以下方法检查进度：");
            System.out.println("   - isVerificationFinished() 检查是否完成");
            System.out.println("   - refreshAndGetStatus() 获取最新状态");
            System.out.println("   - getVerifierStatus() 获取当前状态");
        } else {
            System.out.println("✅ 验证已完成");
        }

        // 获取最终状态
        VerifierStatus finalStatus = refreshAndGetStatus();
        System.out.println("📊 最终状态: " + finalStatus.toString());
        System.out.println("🔍 缺失方块数量: " + finalStatus.getMissingBlocks());
        System.out.println("⚠️  总错误数: " + finalStatus.getTotalErrors());

        // 获取选区信息
        List<BoxInfo> boxes = getCurrentSelectionInfo();
        System.out.println("📍 当前选区数量: " + boxes.size());
        for (BoxInfo box : boxes) {
            System.out.println("  " + box.toString());
        }
    }

    /**
     * 简单使用示例 - 保持向后兼容
     */
    public static void demonstrateUsage() {
        System.out.println("=== LitematicaUtils 简单使用演示 ===");

        if (!hasSelectedSchematicPlacement()) {
            System.out.println("❌ 没有选中的投影！");
            return;
        }

        SchematicPlacement placement = getSelectedSchematicPlacement();
        System.out.println("✅ 当前投影: " + placement.getName());

        // 直接获取缺失方块数量（会自动启动验证但不等待）
        int missingCount = getMissingBlocksCount();
        System.out.println("🔍 当前缺失方块数量: " + missingCount);

        // 获取当前状态
        VerifierStatus status = getVerifierStatus();
        System.out.println("📊 验证器状态: " + status.toString());

        if (!status.isFinished()) {
            System.out.println("⏳ 验证正在进行中，请稍后再检查");
        } else {
            System.out.println("✅ 验证已完成");
        }

        // 获取选区信息
        List<BoxInfo> boxes = getCurrentSelectionInfo();
        System.out.println("📍 选区数量: " + boxes.size());
    }


    public static BlockPos getSchematicBlockInSight(Player player) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 look = player.getViewVector(1f).scale(MAX_DISTANCE);
        Vec3 endPos = eyePos.add(look);
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) return null;

        ClipContext clipContext = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                player);
        BlockHitResult result = schematicWorld.clip(clipContext);
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos();
        }

        return null;

    }

    public static BlockHitResult getLevelBlockResultInSight(Player player, Level level) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 look = player.getViewVector(1f).scale(MAX_DISTANCE);
        Vec3 endPos = eyePos.add(look);

        ClipContext clipContext = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                player);
        BlockHitResult result = level.clip(clipContext);
        if (result.getType() == HitResult.Type.BLOCK) {
            return result;
        }

        return null;

    }

    public static BlockState getSchematicBlockState(BlockPos pos) {
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null || pos == null) return null;
        return schematicWorld.getBlockState(pos);
    }

    public static List<ItemStack> getSchematicContainerContent(BlockPos pos) {
        List<ItemStack> contents = new ArrayList<>();
        BlockState state = LitematicaUtils.getSchematicBlockState(pos);
        if (state == null) return contents;
        if (!InventoryUtils.isContainer(state.getBlock())) return contents;
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        if (world == null) return contents;
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity == null) return contents;
        if (entity instanceof BaseContainerBlockEntity container) {
            contents = iterContainer(container, 0, container.getContainerSize());
        }
//    }
        return contents;

    }

    private static List<ItemStack> iterContainer(BaseContainerBlockEntity container, int start, int end) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ItemStack stack = container.getItem(i);
            Boolean replace = false;
            if(AutoFill.INSTANCE.replace.getBooleanValue()){
                Item item = AutoFill.INSTANCE.replaceItems.getOrDefault(stack.getItem(), stack.getItem());
                items.add(new ItemStack(item, stack.getCount()));

            }else {
                items.add(stack.copy());
            }
        }
        return items;
    }


    public static MaterialListBase createCustomMaterialList(List<MaterialListEntry> entries) {
        AreaSelection dummySelection = DataManager.getSelectionManager().getCurrentSelection();
        MaterialListAreaAnalyzer materialList = new MaterialListAreaAnalyzer(dummySelection != null ? dummySelection
                : new AreaSelection());
        materialList.setMaterialListEntries(entries);
        return materialList;
    }

    public static void setActiveMaterialList(MaterialListBase materialList) {
        if (materialList != null) {
            DataManager.setMaterialList(materialList);
        }
    }

    public static void refreshMaterialList() {
        MaterialListHudRendererAccessor hudRenderer =
                ((MaterialListHudRendererAccessor) DataManager.getMaterialList().getHudRenderer());
        hudRenderer.setLastUpdateTime(-1);
    }


}