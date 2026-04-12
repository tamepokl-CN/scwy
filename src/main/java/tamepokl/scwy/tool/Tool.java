package tamepokl.scwy.tool;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;

import java.util.function.Predicate;

import static tamepokl.scwy.tool.ToolManager.REGISTER_EVENT;

public class Tool {
    public boolean lastEnabled = false;
    public ToolConfig getToolConfig() {
        return config;
    }
    public boolean isEnabled(){
        return config.getToolGuiConfig().getBooleanValue();
    }

    public void setToolConfig(ToolConfig toolConfig) {
        this.config = toolConfig;
    }

    public ToolConfig config;

    public Tool(ToolConfig toolConfig) {
        this.config = toolConfig;
    }

    public Tool(String name) {
        this.config = new ToolConfig(name);
    }

    protected <T> void addRegisterEvent(Event<T> event, T consumer) {
        REGISTER_EVENT.add(new EventRegisterEntry<>(event, consumer));
    }

    public void init(){};
    public void onTick(Minecraft mc){};


    public void onTick0(Minecraft mc) {
        if (mc.level != null && mc.player != null) {
            boolean current = isEnabled();

            if (current != lastEnabled) {
                if (current) {
                    onEnabled(mc);
                } else {
                    onDisabled(mc);
                }
            }
            lastEnabled = current;
            if (current) {
                onTick(mc);
            }
        }
    }

    public void onDisabled(Minecraft mc) {

    }

    public void onEnabled(Minecraft mc) {

    }


    public class EventRegisterEntry<T> {
        private final Event<T> event;
        private final T t;
        private final Predicate<Minecraft> isEnabled = mc -> mc!=null && mc.player!=null &&isEnabled();

        public EventRegisterEntry(Event<T> event, T consumer) {
            this.event = event;
            this.t = consumer;
        }

        public T getCallback() {
            return t;
        }



        public Event<T> getEvent() {
            return event;
        }
    }

}
