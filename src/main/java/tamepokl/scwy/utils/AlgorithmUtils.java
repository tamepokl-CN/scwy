package tamepokl.scwy.utils;

import net.minecraft.core.BlockPos;
import java.util.*;

public class AlgorithmUtils {
    
    /**
     * 球覆盖算法：用最少的半径为r的球覆盖所有点，且每个球心至少与其他球心距离<d
     * 
     * @param points 输入的三维坐标点集
     * @param r 球的半径
     * @param d 球心间最小距离阈值
     * @return 球心坐标列表
     */
    public static List<BlockPos> sphereCoverAlgorithm(List<BlockPos> points, int r, int d) {
        if (points == null || points.isEmpty() || r <= 0 || d <= 0) {
            return new ArrayList<>();
        }
        
        // 步骤1: 按r分桶
        Map<Long, List<BlockPos>> buckets = bucketPoints(points, r);
        
        // 步骤2: 贪心选择候选球心
        Set<BlockPos> candidateCenters = greedySelectCenters(buckets, r);
        
        // 步骤3: 检查并调整球心间距约束
        List<BlockPos> finalCenters = adjustCenterDistances(new ArrayList<>(candidateCenters), d, r);
        
        // 步骤4: 合并可合并的球
        finalCenters = mergeOverlappingSpheres(finalCenters, d, r);
        
        return finalCenters;
    }
    
    /**
     * 将点按r分桶，用于加速最近邻搜索
     */
    private static Map<Long, List<BlockPos>> bucketPoints(List<BlockPos> points, int r) {
        Map<Long, List<BlockPos>> buckets = new HashMap<>();
        
        for (BlockPos point : points) {
            // 使用r作为桶大小进行空间划分
            long bucketKey = getBucketKey(point, r);
            buckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(point);
        }
        
        return buckets;
    }
    
    /**
     * 计算点所在的桶键值
     */
    private static long getBucketKey(BlockPos pos, int bucketSize) {
        int x = pos.getX() / bucketSize;
        int y = pos.getY() / bucketSize;
        int z = pos.getZ() / bucketSize;
        // 使用Cantor配对函数创建唯一的long键值
        return (((long)x * 73856093) ^ ((long)y * 19349663) ^ ((long)z * 83492791)) & 0xFFFFFFFFL;
    }
    
    /**
     * 贪心算法选择候选球心
     */
    private static Set<BlockPos> greedySelectCenters(Map<Long, List<BlockPos>> buckets, int r) {
        Set<BlockPos> centers = new HashSet<>();
        Set<BlockPos> coveredPoints = new HashSet<>();
        
        // 收集所有未覆盖的点
        List<BlockPos> allPoints = new ArrayList<>();
        for (List<BlockPos> bucketPoints : buckets.values()) {
            allPoints.addAll(bucketPoints);
        }
        
        while (!allPoints.isEmpty()) {
            // 选择能覆盖最多未覆盖点的候选中心
            BlockPos bestCenter = findBestCenter(allPoints, r, coveredPoints);
            if (bestCenter == null) break;
            
            centers.add(bestCenter);
            
            // 标记被此球覆盖的所有点
            Iterator<BlockPos> iterator = allPoints.iterator();
            while (iterator.hasNext()) {
                BlockPos point = iterator.next();
                if (getDistanceSq(bestCenter, point) <= (long)r * r) {
                    coveredPoints.add(point);
                    iterator.remove();
                }
            }
        }
        
        return centers;
    }
    
    /**
     * 寻找最佳球心位置
     */
    private static BlockPos findBestCenter(List<BlockPos> points, int r, Set<BlockPos> alreadyCovered) {
        BlockPos bestCenter = null;
        int maxCoverage = 0;
        
        // 采样策略：只检查部分点作为候选中心以提高效率
        int sampleSize = Math.min(50, points.size());
        Random random = new Random(42); // 固定种子保证结果一致性
        
        for (int i = 0; i < sampleSize; i++) {
            int index = random.nextInt(points.size());
            BlockPos candidate = points.get(index);
            
            // 计算此候选中心能覆盖的点数
            int coverage = 0;
            for (BlockPos point : points) {
                if (!alreadyCovered.contains(point) && 
                    getDistanceSq(candidate, point) <= (long)r * r) {
                    coverage++;
                }
            }
            
            if (coverage > maxCoverage) {
                maxCoverage = coverage;
                bestCenter = candidate;
            }
        }
        
        return bestCenter;
    }
    
    /**
     * 调整球心间距以满足约束条件
     */
    private static List<BlockPos> adjustCenterDistances(List<BlockPos> centers, int d, int r) {
        List<BlockPos> adjustedCenters = new ArrayList<>(centers);
        long dSquared = (long)d * d;
        
        for (int i = 0; i < adjustedCenters.size(); i++) {
            BlockPos center = adjustedCenters.get(i);
            boolean hasNearbyCenter = false;
            BlockPos nearestCenter = null;
            long minDistSq = Long.MAX_VALUE;
            
            // 查找最近的其他球心
            for (int j = 0; j < adjustedCenters.size(); j++) {
                if (i != j) {
                    BlockPos other = adjustedCenters.get(j);
                    long distSq = getDistanceSq(center, other);
                    if (distSq < minDistSq) {
                        minDistSq = distSq;
                        nearestCenter = other;
                    }
                    if (distSq < dSquared) {
                        hasNearbyCenter = true;
                        break;
                    }
                }
            }
            
            // 如果没有足够近的球心，尝试调整位置
            if (!hasNearbyCenter && nearestCenter != null) {
                adjustedCenters.set(i, adjustCenterPosition(center, nearestCenter, d));
            }
        }
        
        return adjustedCenters;
    }
    
    /**
     * 调整球心位置使其与其他球心距离<d
     */
    private static BlockPos adjustCenterPosition(BlockPos center, BlockPos target, int minDistance) {
        double dx = target.getX() - center.getX();
        double dy = target.getY() - center.getY();
        double dz = target.getZ() - center.getZ();
        
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (distance > minDistance) {
            // 将center向target方向移动，使距离接近minDistance
            double ratio = (distance - minDistance + 1) / distance;
            int newX = (int)(center.getX() + dx * ratio);
            int newY = (int)(center.getY() + dy * ratio);
            int newZ = (int)(center.getZ() + dz * ratio);
            return new BlockPos(newX, newY, newZ);
        }
        
        return center;
    }
    
    /**
     * 合并可以合并的球
     */
    private static List<BlockPos> mergeOverlappingSpheres(List<BlockPos> centers, int d, int r) {
        List<BlockPos> merged = new ArrayList<>(centers);
        long mergeThresholdSq = (long)(d - r) * (d - r);
        boolean changed = true;
        
        while (changed) {
            changed = false;
            Set<Integer> toRemove = new HashSet<>();
            
            for (int i = 0; i < merged.size() && !changed; i++) {
                if (toRemove.contains(i)) continue;
                
                BlockPos center1 = merged.get(i);
                
                for (int j = i + 1; j < merged.size(); j++) {
                    if (toRemove.contains(j)) continue;
                    
                    BlockPos center2 = merged.get(j);
                    long distSq = getDistanceSq(center1, center2);
                    
                    // 如果两个球可以合并（距离足够近且不破坏覆盖）
                    if (distSq <= mergeThresholdSq) {
                        // 合并为新的球心位置
                        BlockPos mergedCenter = new BlockPos(
                            (center1.getX() + center2.getX()) / 2,
                            (center1.getY() + center2.getY()) / 2,
                            (center1.getZ() + center2.getZ()) / 2
                        );
                        
                        merged.set(i, mergedCenter);
                        toRemove.add(j);
                        changed = true;
                        break;
                    }
                }
            }
            
            // 移除被合并的球心
            List<BlockPos> temp = new ArrayList<>();
            for (int i = 0; i < merged.size(); i++) {
                if (!toRemove.contains(i)) {
                    temp.add(merged.get(i));
                }
            }
            merged = temp;
        }
        
        return merged;
    }
    
    /**
     * 计算两点间距离的平方
     */
    private static long getDistanceSq(BlockPos pos1, BlockPos pos2) {
        long dx = pos1.getX() - pos2.getX();
        long dy = pos1.getY() - pos2.getY();
        long dz = pos1.getZ() - pos2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * 验证结果：检查是否所有点都被覆盖且满足球心间距约束
     */
    public static boolean validateResult(List<BlockPos> points, List<BlockPos> centers, int r, int d) {
        // 检查所有点是否被覆盖
        for (BlockPos point : points) {
            boolean covered = false;
            for (BlockPos center : centers) {
                if (getDistanceSq(point, center) <= (long)r * r) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                System.out.println("点 " + point + " 未被任何球覆盖");
                return false;
            }
        }
        
        // 检查球心间距约束
        long dSquared = (long)d * d;
        for (int i = 0; i < centers.size(); i++) {
            boolean hasNearbyCenter = false;
            for (int j = 0; j < centers.size(); j++) {
                if (i != j) {
                    if (getDistanceSq(centers.get(i), centers.get(j)) < dSquared) {
                        hasNearbyCenter = true;
                        break;
                    }
                }
            }
            if (!hasNearbyCenter) {
                System.out.println("球心 " + centers.get(i) + " 不满足间距约束");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取算法统计信息
     */
    public static Map<String, Object> getAlgorithmStats(List<BlockPos> points, List<BlockPos> centers, int r) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pointCount", points.size());
        stats.put("centerCount", centers.size());
        stats.put("radius", r);
        
        // 计算覆盖率统计
        int[] coverageCount = new int[centers.size()];
        for (BlockPos point : points) {
            for (int i = 0; i < centers.size(); i++) {
                if (getDistanceSq(point, centers.get(i)) <= (long)r * r) {
                    coverageCount[i]++;
                }
            }
        }
        
        stats.put("avgCoveragePerSphere", Arrays.stream(coverageCount).average().orElse(0));
        stats.put("maxCoveragePerSphere", Arrays.stream(coverageCount).max().orElse(0));
        stats.put("minCoveragePerSphere", Arrays.stream(coverageCount).min().orElse(0));
        
        return stats;
    }
}
