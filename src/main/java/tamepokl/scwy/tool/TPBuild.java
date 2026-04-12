package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigFloat;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.util.InfoUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import tamepokl.scwy.config.ConfigCoordinate;
import tamepokl.scwy.config.ConfigTriggerHotkey;
import tamepokl.scwy.utils.AlgorithmUtils;
import tamepokl.scwy.utils.HighlightBlockRenderer;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import tamepokl.scwy.utils.LitematicaUtils;

import java.util.*;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class TPBuild extends Tool{
    public static final TPBuild INSTANCE = new TPBuild(new ToolConfig("TPBuild"));
    public final ConfigString POS1 = config.ofString("pos1", "");
    public final ConfigString POS2 = config.ofString("pos2", "");
    public final ConfigTriggerHotkey NEXT = config.ofTriggerHotkey("next", "");
    public final ConfigTriggerHotkey PREV = config.ofTriggerHotkey("prev", "");
    public final ConfigTriggerHotkey REFRESH = config.ofTriggerHotkey("refresh", "");
    public final ConfigTriggerHotkey MARK = config.ofTriggerHotkey("mark", "");
    public final ConfigTriggerHotkey LITEMATICA = config.ofTriggerHotkey("litematica","");
    public final ConfigFloat DISTANCE = config.ofFloat("distance" , 20.0f , 0.0f , 100.0f);
    public final ConfigFloat RADIUS = config.ofFloat("radius" , 5.0f , 0.0f , 10.0f);
    public final ConfigColor SPHERE_COLOR = config.ofColor("sphereColor", "0xFF00FF00"); // 绿色
    public final ConfigColor CURRENT_SPHERE_COLOR = config.ofColor("currentSphereColor", "0xFFFF0000"); // 红色
    public final ConfigColor MARKED_SPHERE_COLOR = config.ofColor("markedSphereColor", "0xFFFFFF00"); // 黄色
    public final ConfigBoolean RENDER_THROUGH = config.ofBoolean("renderThrough", true); // 透视显示
    static {
        addTool(INSTANCE);
    }
    
    // 存储非空气方块的位置
    private List<BlockPos> nonAirBlocks = new ArrayList<>();
    // 存储球心位置
    private List<BlockPos> sphereCenters = new ArrayList<>();
    // 当前球心索引
    private int currentSphereIndex = 0;
    // 已标记的球心位置
    private Set<BlockPos> markedSpheres = new HashSet<>();
    // 高亮ID
    private static final String HIGHLIGHT_ID = "tpbuild_spheres";
    
    {
        LITEMATICA.setCallback((a,b)->{
            LitematicaUtils.BoxInfo boxInfo = LitematicaUtils.getCurrentSelectionInfo().get(0);
            POS1.setValueFromString(boxInfo.getPos1().toShortString());
            POS2.setValueFromString(boxInfo.getPos2().toShortString());
            ((KeybindMulti)REFRESH.getKeybind()).getCallback().onKeyAction(null,null);
            return true;
        });
        // 设置REFRESH回调
        REFRESH.setCallback(new IHotkeyCallback() {
            @Override
            public boolean onKeyAction(KeyAction action, fi.dy.masa.malilib.hotkeys.IKeybind key) {
                refreshBlocks(Minecraft.getInstance());
                return true;
            }
        });
        
        // 设置NEXT回调
        NEXT.setCallback(new IHotkeyCallback() {
            @Override
            public boolean onKeyAction(KeyAction action, fi.dy.masa.malilib.hotkeys.IKeybind key) {
                moveToNextSphere(Minecraft.getInstance());
                return true;
            }
        });
        
        // 设置PREV回调
        PREV.setCallback(new IHotkeyCallback() {
            @Override
            public boolean onKeyAction(KeyAction action, fi.dy.masa.malilib.hotkeys.IKeybind key) {
                moveToPrevSphere(Minecraft.getInstance());
                return true;
            }
        });
        
        // 设置MARK回调
        MARK.setCallback(new IHotkeyCallback() {
            @Override
            public boolean onKeyAction(KeyAction action, fi.dy.masa.malilib.hotkeys.IKeybind key) {
                markCurrentSphere(Minecraft.getInstance());
                return true;
            }
        });
    }

    public TPBuild(ToolConfig toolConfig) {
        super(toolConfig);
    }
    
    /**
     * 刷新方块数据：解析POS1和POS2，获取区间内非空气方块，运行球覆盖算法
     */
    private void refreshBlocks(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        
        try {
            // 解析POS1和POS2
            String pos1Str = POS1.getStringValue().trim();
            String pos2Str = POS2.getStringValue().trim();
            
            if (pos1Str.isEmpty() || pos2Str.isEmpty()) {
                System.out.println("§c请先设置POS1和POS2坐标");
                return;
            }
            
            BlockPos pos1 = parseBlockPos(pos1Str);
            BlockPos pos2 = parseBlockPos(pos2Str);
            
            if (pos1 == null || pos2 == null) {
                System.out.println("§c坐标格式错误，请使用 x,y,z 格式");
                return;
            }
            
            // 获取三维区间内的所有BlockPos
            nonAirBlocks.clear();
            BlockPos minPos = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
            );
            BlockPos maxPos = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
            );
            
            // 筛选非空气方块
            for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
                for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                    for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                        BlockPos currentPos = new BlockPos(x, y, z);
                        BlockState state = mc.level.getBlockState(currentPos);
                        if (!state.isAir() && state.getBlock() != Blocks.VOID_AIR) {
                            nonAirBlocks.add(currentPos);
                        }
                    }
                }
            }
            
            if (nonAirBlocks.isEmpty()) {
                System.out.println("§c指定区域内没有找到非空气方块");
                return;
            }
            System.out.println("非空气方块数量: " + nonAirBlocks.size());
            
            // 运行球覆盖算法
            int radius = Math.round(RADIUS.getFloatValue());
            int distance = Math.round(DISTANCE.getFloatValue());
            sphereCenters = AlgorithmUtils.sphereCoverAlgorithm(nonAirBlocks, radius, distance);
            
            if (sphereCenters.isEmpty()) {
                System.out.println("§c球覆盖算法未找到合适的球心");
                return;
            }
            
            currentSphereIndex = 0;
            markedSpheres.clear();
            
            // 高亮所有球心
            highlightAllSpheres();
            
            System.out.println("§a刷新完成！找到 %d 个球心，当前在第1个".formatted( sphereCenters.size()));
            
        } catch (Exception e) {
            System.out.println("§c刷新失败: " + e.getMessage());
        }
    }
    
    /**
     * 移动到下一个球心
     */
    private void moveToNextSphere(Minecraft mc) {
        if (mc.player == null || sphereCenters.isEmpty()) return;
        
        // 跳过已标记的球心
        int startIndex = currentSphereIndex;
        do {
            currentSphereIndex = (currentSphereIndex + 1) % sphereCenters.size();
            if (!markedSpheres.contains(sphereCenters.get(currentSphereIndex))) {
                break;
            }
        } while (currentSphereIndex != startIndex);
        
        BlockPos targetPos = sphereCenters.get(currentSphereIndex);
        moveTo(mc, targetPos.getX() + 0.5f, targetPos.getY(), targetPos.getZ() + 0.5f);
        
        // 更新当前球心高亮
        highlightCurrentSphere();
        
        System.out.println("§a移动到球心 %d/%d (x:%d y:%d z:%d)".formatted(
            currentSphereIndex + 1, sphereCenters.size(), 
            targetPos.getX(), targetPos.getY(), targetPos.getZ()));
    }
    
    /**
     * 移动到上一个球心
     */
    private void moveToPrevSphere(Minecraft mc) {
        if (mc.player == null || sphereCenters.isEmpty()) return;
        
        // 跳过已标记的球心
        int startIndex = currentSphereIndex;
        do {
            currentSphereIndex = (currentSphereIndex - 1 + sphereCenters.size()) % sphereCenters.size();
            if (!markedSpheres.contains(sphereCenters.get(currentSphereIndex))) {
                break;
            }
        } while (currentSphereIndex != startIndex);
        
        BlockPos targetPos = sphereCenters.get(currentSphereIndex);
        moveTo(mc, targetPos.getX() + 0.5f, targetPos.getY(), targetPos.getZ() + 0.5f);
        
        // 更新当前球心高亮
        highlightCurrentSphere();
        
        System.out.println("§a移动到球心 %d/%d (x:%d y:%d z:%d)".formatted(
            currentSphereIndex + 1, sphereCenters.size(), 
            targetPos.getX(), targetPos.getY(), targetPos.getZ()));
    }
    
    /**
     * 标记当前球心
     */
    private void markCurrentSphere(Minecraft mc) {
        if (sphereCenters.isEmpty()) return;
        
        BlockPos currentPos = sphereCenters.get(currentSphereIndex);
        if (markedSpheres.contains(currentPos)) {
            markedSpheres.remove(currentPos);
            System.out.println("§e取消标记球心 %d".formatted( currentSphereIndex + 1));
        } else {
            markedSpheres.add(currentPos);
            System.out.println("§a标记球心 %d，该位置将被跳过".formatted( currentSphereIndex + 1));
        }
        
        // 更新高亮显示
        highlightAllSpheres();
    }
    
    /**
     * 解析坐标字符串为BlockPos
     */
    private BlockPos parseBlockPos(String posStr) {
        try {
            String[] parts = posStr.split(",");
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                return new BlockPos(x, y, z);
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }
    
    /**
     * 高亮所有球心（普通球心为绿色，标记球心为黄色）
     */
    private void highlightAllSpheres() {
        if (sphereCenters.isEmpty()) {
            HighlightBlockRenderer.clear(HIGHLIGHT_ID);
            return;
        }
        
        HighlightBlockRenderer.createHighlightBlockList(HIGHLIGHT_ID, SPHERE_COLOR , false);
        
        // 设置透视显示选项
        // 注意：这里需要修改HighlightBlockRenderer来支持透视配置
        
        // 设置所有球心位置
        Set<BlockPos> allSpheres = new HashSet<>(sphereCenters);
        HighlightBlockRenderer.setPos(HIGHLIGHT_ID, allSpheres);
        
        // 为标记的球心设置黄色
        Map<BlockPos, Color4f> colorMap = new HashMap<>();
        Color4f markedColor = MARKED_SPHERE_COLOR.getColor();
        for (BlockPos pos : markedSpheres) {
            if (allSpheres.contains(pos)) {
                colorMap.put(pos, markedColor);
            }
        }
        
        if (!colorMap.isEmpty()) {
            HighlightBlockRenderer.setColors(HIGHLIGHT_ID, colorMap);
        }
    }
    
    /**
     * 高亮当前球心（红色）
     */
    private void highlightCurrentSphere() {
        if (sphereCenters.isEmpty()) return;
        
        BlockPos currentPos = sphereCenters.get(currentSphereIndex);
        Set<BlockPos> currentSet = Set.of(currentPos);
        
        // 创建单独的高亮用于当前球心
        String currentId = HIGHLIGHT_ID + "_current";
        HighlightBlockRenderer.createHighlightBlockList(currentId, CURRENT_SPHERE_COLOR , false);
        HighlightBlockRenderer.setPos(currentId, currentSet);
        
        // 设置透视显示选项
        // 注意：这里需要修改HighlightBlockRenderer来支持透视配置
    }
    
    /**
     * 智能传送方法：如果目标位置是方块，则传送到上方第一个空气位置
     * @param mc Minecraft实例
     * @param x 目标X坐标
     * @param y 目标Y坐标
     * @param z 目标Z坐标
     */
    public static void moveTo(Minecraft mc, float x, float y, float z) {
        if (mc.player == null || mc.level == null) return;
        
        BlockPos targetPos = new BlockPos((int)x, (int)y, (int)z);
        BlockPos adjustedPos = findSafeTeleportPosition(mc, targetPos);
        
        double finalX = adjustedPos.getX() + 0.5;
        double finalY = adjustedPos.getY();
        double finalZ = adjustedPos.getZ() + 0.5;
        
        mc.player.setPos(finalX, finalY, finalZ);
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(finalX, finalY, finalZ, false, mc.player.horizontalCollision));
    }
    
    /**
     * 寻找安全的传送位置
     * @param mc Minecraft实例
     * @param targetPos 目标位置
     * @return 安全的传送位置
     */
    private static BlockPos findSafeTeleportPosition(Minecraft mc, BlockPos targetPos) {
        if (mc.level == null) return targetPos;
        
        // 检查当前位置是否安全
        if (isPositionSafe(mc, targetPos)) {
            return targetPos;
        }
        
        // 向上搜索安全位置
        for (int yOffset = 1; yOffset <= 10; yOffset++) {
            BlockPos checkPos = targetPos.above(yOffset);
            if (isPositionSafe(mc, checkPos) && isPositionSafe(mc, checkPos.above())) {
                return checkPos;
            }
        }
        
        // 向下搜索安全位置
        for (int yOffset = 1; yOffset <= 10; yOffset++) {
            BlockPos checkPos = targetPos.below(yOffset);
            if (isPositionSafe(mc, checkPos) && isPositionSafe(mc, checkPos.above())) {
                return checkPos;
            }
        }
        
        // 如果都没找到，返回原位置
        return targetPos;
    }
    
    /**
     * 检查位置是否安全（脚下是固体方块，头部空间足够）
     * @param mc Minecraft实例
     * @param pos 要检查的位置
     * @return 是否安全
     */
    private static boolean isPositionSafe(Minecraft mc, BlockPos pos) {
//        if (mc.level == null) return false;
//
//        // 检查脚下位置是否为固体方块
//        BlockState footBlock = mc.level.getBlockState(pos.below());
//        if (footBlock.isAir() || !footBlock.blocksMotion()) {
//            return false;
//        }
//
        // 检查身体位置是否为空气
        BlockState bodyBlock = mc.level.getBlockState(pos);
        if (!bodyBlock.isAir()) {
            return false;
        }
//
//        // 检查头部位置是否为空气
//        BlockState headBlock = mc.level.getBlockState(pos.above());
//        if (!headBlock.isAir()) {
//            return false;
//        }
        
        return true;
    }
}
