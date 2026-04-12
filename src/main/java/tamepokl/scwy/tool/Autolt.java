package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import tamepokl.scwy.utils.CommandUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static tamepokl.scwy.config.Configs.Extra.addExtra;
import static tamepokl.scwy.tool.ToolManager.addTool;
import static tamepokl.scwy.utils.CommandUtils.sendCommand;

public class Autolt extends Tool {
    public static final Autolt INSTANCE = new Autolt("autolt");
    public final ConfigString date = config.ofString("autolt_date", "");
    public final ConfigString command = config.ofString("autolt_command", "lt 1 random");
    public final ConfigBoolean bought = config.ofBoolean("bought", false);
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public LocalDate localDate;

    static {

        addTool(INSTANCE);
    }

    {
        addExtra(date);
        date.setValueChangeCallback(config1 ->
        {
            changeDate(true);
        });
        bought.setValueChangeCallback(config1 -> buy());
        addRegisterEvent(ClientLifecycleEvents.CLIENT_STARTED, client -> {
            if (localDate == null) {
                date.onValueChanged();
            }
        });
        addRegisterEvent(ClientPlayConnectionEvents.JOIN, (handler, sender, client) -> tickCounter = 6001);

    }

    private void changeDate(boolean buy) {
        String string = date.getStringValue().trim();
        if (string.isEmpty()) {
            date.setValueFromString(LocalDate.now().format(formatter));

        } else {
            try {
                localDate = LocalDate.parse(string, formatter);
                if(buy) {
                    buy();
                }
            } catch (Exception ignored) {
                date.resetToDefault();
            }
        }
    }

    private int tickCounter = 6001;

    @Override
    public void onTick(Minecraft mc) {
        if (isEnabled() && !Minecraft.getInstance().isSingleplayer()) {
            if (tickCounter >= 6000) {
                buy();
                tickCounter = 0;
            }
            tickCounter++;
        }
    }

    private void buy() {
        if(isEnabled()) {
            if (!isBoughtToday()) {
            if(CommandUtils.isCommandValid("lt").orElse(false)){
                boolean b = sendCommand(command.getStringValue());
                if (b) bought.setBooleanValue(true);
            }
            }
        }
    }


    public Autolt(String name) {
        super(name);
    }

    public boolean isBoughtToday() {
        changeDate(false);
        return LocalTime.now().isAfter(LocalTime.of(8,30)) || LocalDate.now().equals(localDate) && bought.getBooleanValue();
    }

}
