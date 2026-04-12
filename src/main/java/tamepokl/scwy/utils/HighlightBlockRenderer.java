package tamepokl.scwy.utils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;


import tamepokl.scwy.config.Configs;


//ai写的
//也许参考了wuhuclient
public class HighlightBlockRenderer implements IRenderer {
    public static HighlightBlockRenderer instance = new HighlightBlockRenderer();
    private static final float LINE_WIDTH = 4.0f;

    public record HighlightTheProject(ConfigColor color4f, Set<BlockPos> pos, Map<BlockPos, Color4f> colorMap) {

        }
    public static Map<String, HighlightTheProject> highlightTheProjectMap = new ConcurrentHashMap<>();
    public static String threadName = "scwyRenderThread";
    public static boolean shaderIng = false;
    public static Map<String ,Boolean> soildMode = new ConcurrentHashMap<>();
    
    public static void createHighlightBlockList(String id, ConfigColor configColor , boolean renderSoild){
        if (highlightTheProjectMap.get(id) == null) {
            highlightTheProjectMap.put(id, new HighlightTheProject(
                configColor,
                new LinkedHashSet<>(), 
                new ConcurrentHashMap<>()
            ));
        }
        soildMode.put(id, renderSoild);
    }
    public static void createHighlightBlockList(String id, Color4f color4f , boolean renderSoild){
        if (highlightTheProjectMap.get(id) == null) {
            highlightTheProjectMap.put(id, new HighlightTheProject(
                    new ConfigColor(id , color4f),
                    new LinkedHashSet<>(),
                    new ConcurrentHashMap<>()
            ));
        }
        soildMode.put(id, renderSoild);
    }
    
    public static Set<BlockPos> getHighlightBlockPosList(String id){
        if(highlightTheProjectMap.get(id) != null){
            return highlightTheProjectMap.get(id).pos();
        }
        return null;
    }
    
    public static List<String> clearList = new CopyOnWriteArrayList<>();
    
    public static void clear(String id){
        if (!clearList.contains(id)) clearList.add(id);
    }
    
    public static Map<String, Set<BlockPos>> setMap = new HashMap<>();
    public static Map<String, Map<BlockPos, Color4f>> colorMapUpdates = new HashMap<>();

    
    public static void setPos(String id, Set<BlockPos> posSet){
        HighlightTheProject highlightTheProject = highlightTheProjectMap.get(id);
        if (highlightTheProject != null && posSet != null) {
            setMap.put(id, posSet);
        }
    }
    
    public static void setColors(String id, Map<BlockPos, Color4f> colorMap) {
        colorMapUpdates.put(id, colorMap);
    }



    // 渲染实心填充（优化：只渲染外露面）
    public void renderSolid(Color4f defaultColor, Set<BlockPos> posSet, Map<BlockPos, Color4f> colorMap){
        com.mojang.blaze3d.systems.RenderSystem.setShaderFog(com.mojang.blaze3d.systems.RenderSystem.getShaderFog());
        try (RenderContext ctx = new RenderContext(() -> threadName, MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_NO_DEPTH)) {
            BufferBuilder buffer = ctx.getBuilder();
            com.mojang.blaze3d.vertex.MeshData meshData;
            
            for (BlockPos pos : posSet) {
                Color4f color = colorMap != null && colorMap.containsKey(pos) ? colorMap.get(pos) : defaultColor;
                // 内部完全被包围的方块不渲染
                if (!isFullyEnclosed(pos, posSet)) {
                    RenderUtils.renderAreaSidesBatched(pos, pos, color, 0.002, buffer);
                }
            }

            if(buffer != null){
                meshData = buffer.build();
                ctx.upload(meshData , true);

                ctx.drawPost();
            }
        } catch (Exception e) {
            // 渲染错误，忽略
        }
        com.mojang.blaze3d.systems.RenderSystem.setShaderFog(com.mojang.blaze3d.systems.RenderSystem.getShaderFog());
    }

    private boolean isFullyEnclosed(BlockPos pos, Set<BlockPos> posSet) {
        return posSet.contains(pos.north()) && posSet.contains(pos.south()) &&
               posSet.contains(pos.east()) && posSet.contains(pos.west()) &&
               posSet.contains(pos.above()) && posSet.contains(pos.below());
    }

    // 渲染框线（每个方块的所有边）
    public void renderEdges(Color4f defaultColor, Set<BlockPos> posSet, Map<BlockPos, Color4f> colorMap){
        boolean renderThrough = true;
        
        for (BlockPos pos : posSet) {
            Color4f color = colorMap != null && colorMap.containsKey(pos) ? colorMap.get(pos) : defaultColor;
            RenderUtils.renderBlockOutline(pos, 0.002f, LINE_WIDTH, color, renderThrough);
        }
    }
    
    public static void init(){
        RenderEventHandler.getInstance().registerWorldLastRenderer(instance);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client1) -> {
            for (Map.Entry<String, HighlightTheProject> stringHighlightTheProjectEntry : highlightTheProjectMap.entrySet()) {
                stringHighlightTheProjectEntry.getValue().pos.clear();
            }
        });
    }

    @Override
    public void onRenderWorldLast(Matrix4f matrices, Matrix4f projMatrix){
        // 检查高亮功能是否启用
        // TODO: 检查高亮功能是否启用
        // if (!Configs.Features.HIGHLIGHT_SETTINGS.getBooleanValue()) {
        //     return;
        // }

        // 更新高亮位置和颜色
        setMap.forEach((k, v) -> {
            HighlightTheProject highlightTheProject = highlightTheProjectMap.get(k);
            if(highlightTheProject != null){
                highlightTheProject.pos.clear();
                highlightTheProject.pos.addAll(v);
                
                Map<BlockPos, Color4f> colorMap = colorMapUpdates.get(k);
                if (colorMap != null) {
                    highlightTheProject.colorMap().clear();
                    highlightTheProject.colorMap().putAll(colorMap);
                }
            }
        });
        setMap.clear();
        colorMapUpdates.clear();

        // 清除标记的位置
        for (String string : clearList) {
            HighlightTheProject highlightTheProject = highlightTheProjectMap.get(string);
            if (highlightTheProject != null) {
                highlightTheProject.pos.clear();
                highlightTheProject.colorMap().clear();
            }
        }
        clearList.clear();

        shaderIng = true;
        highlightTheProjectMap.forEach((key, value) -> {
            Color4f defaultColor = value.color4f.getColor();
            Set<BlockPos> filtered = filterByDistanceAndCount(value.pos);
            if (!filtered.isEmpty()) {
                if(soildMode.get(key)) {
                    renderSolid(defaultColor, filtered, value.colorMap());
                }else{
                    renderEdges(defaultColor, filtered, value.colorMap());
                }
            }
        });
        shaderIng = false;
    }

    private Set<BlockPos> filterByDistanceAndCount(Set<BlockPos> posSet) {
        if (posSet == null || posSet.isEmpty()) {
            return Set.of();
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) {
            return Set.of();
        }

        double maxRenderDistance = 64.0; // TODO: 从配置中获取
        int maxCount = 1000; // TODO: 从配置中获取

        double maxDistanceSq = maxRenderDistance * maxRenderDistance;
        var cameraPos = client.gameRenderer.getMainCamera().position();
        Set<BlockPos> filtered = new LinkedHashSet<>();
        
        for (BlockPos pos : posSet) {
            if (filtered.size() >= maxCount) break;
            
            double dx = pos.getX() + 0.5 - cameraPos.x;
            double dy = pos.getY() + 0.5 - cameraPos.y;
            double dz = pos.getZ() + 0.5 - cameraPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq <= maxDistanceSq) {
                filtered.add(pos);
            }
        }
        return filtered;
    }
}
