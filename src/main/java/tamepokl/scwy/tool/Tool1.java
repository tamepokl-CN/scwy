package tamepokl.scwy.tool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import tamepokl.scwy.config.ConfigTriggerHotkey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static tamepokl.scwy.tool.ToolManager.addTool;

public class Tool1 extends Tool {
    public Tool1(String name) {
        super(name);
    }

    private List<Entity> processed = new ArrayList<>();
    private List<Entity> kill = new ArrayList<>();

    public static final Tool1 INSTANCE = new Tool1("tool1");
    public final ConfigTriggerHotkey triggerHotkey = new ConfigTriggerHotkey("x", "key.scwy.tool1");

    static {
        addTool(INSTANCE);
    }
    {
        triggerHotkey.setCallback((a,keybind) -> {
            processed.clear();
            kill.clear();
            return true;
        });
    }

    private int tickCounter = 0;
    private int tickCounter2 = 0;
    private boolean isChecking = false;
    private WanderingTrader lastEntity;

    @Override
    public void onTick(Minecraft mc) {
        tickCounter++;
        if (tickCounter >= 200) {
            check();
            tickCounter = 0;
        }
        tickCounter2++;
        if (tickCounter2 >= 10) {
            kill();
            tickCounter2 = 0;
        }

    }

    private void kill() {
        CompletableFuture.runAsync(() -> {
            List<Entity> toRemove = new ArrayList<>(); // 创建临时列表存储需要移除的实体
            for (Entity entity : kill) {
                if (entity.isAlive()) {
                    System.out.println("kill");
                    Minecraft.getInstance().gameMode.attack(Minecraft.getInstance().player, entity);
                    try {
                        Thread.sleep(6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    toRemove.add(entity); // 将需要移除的实体添加到临时列表
                }
            }
            kill.removeAll(toRemove); // 统一移除所有标记的实体
        });
    }

    private void check() {
        List<WanderingTrader> entities = getNearbyEntities(10);
        CompletableFuture.runAsync(() -> {
            for (WanderingTrader entity : entities) {
                isChecking = true;
                lastEntity = entity;

                // 使用 CompletableFuture 异步执行交互逻辑

                Minecraft.getInstance().gameMode.interact(Minecraft.getInstance().player, entity,
                        InteractionHand.MAIN_HAND);
                System.out.println("Opening");
                long startTime = System.currentTimeMillis();
                while (isChecking) {
                    try {
                        Thread.sleep(10);
                        if (System.currentTimeMillis() - startTime > 3000) {
                            isChecking = false;
                            System.out.println("checkFail");
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

    }

    public void process(MerchantMenu menu) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean has = false;
        System.out.println("process");
        for (MerchantOffer offer : menu.getOffers()) {
            if (offer.getResult().is(Items.SMALL_DRIPLEAF)) {
                has = true;
                break;
            }
        }
        if (has) {
            processed.add(lastEntity);
        } else {
            kill.add(lastEntity);
        }
        Minecraft.getInstance().player.closeContainer();
        System.out.println("processend");
        isChecking = false;


    }

    public List<WanderingTrader> getNearbyEntities(double radius) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientLevel world = client.level;

        if (player == null || world == null) {
            return List.of();
        }

        // 创建包围盒进行搜索
        AABB searchBox = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        // 使用world.getOtherEntities方法获取实体
        return world.getEntitiesOfClass(WanderingTrader.class, searchBox, e -> !processed.contains(e.getUUID()));

    }
}
