package tamepokl.scwy.tool;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;
import tamepokl.scwy.utils.CommandUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Deprecated
public class CommandRedirect extends ExpandableTool {
    public static final CommandRedirect INSTANCE = new CommandRedirect("commandRedirect");
    public static final ImmutableList< String> defaultValue = ImmutableList.of(
            "scwy->s"
    );
    public Map<String,String> redirects = new HashMap<>();

    public final ConfigStringList commands = config.ofStringList("commands" ,ImmutableList.of());

    static {
        ToolManager.addTool(INSTANCE);
        ClientPlayConnectionEvents.JOIN.register((handler, sender,client) -> {
            INSTANCE.makeMap();
        });
    }


    {
        commands.setValueChangeCallback(this::makeMap);
    }

    public String modifyCommandString(String input){
        if(isEnabled()){
            for (Map.Entry<String, String> entry : redirects.entrySet()) {
                if(input.startsWith("/"+entry.getValue())){
                    return "/" + entry.getKey() + input.substring(entry.getValue().length() + 1);
                }
            }
        }
        return input;
    }

    public void onHandleCommands(ClientboundCommandsPacket packet ,CommandDispatcher<ClientSuggestionProvider> dispatcher){
        for (Map.Entry<String, String> entry : redirects.entrySet()) {
            String[] s = entry.getKey().split(" ");
            CommandNode<ClientSuggestionProvider> node = dispatcher.findNode(Arrays.stream(s).toList());
            if(node==null){
                CommandUtils.sendChatMessage(Component.literal("Node "+ entry.getKey()));
                break;
            }
            String[] s1 = entry.getValue().split(" ");
            if(s1.length==1) {
                LiteralArgumentBuilder<ClientSuggestionProvider> literal = LiteralArgumentBuilder.literal(s1[0]);
                LiteralCommandNode<ClientSuggestionProvider> build = literal.build();
                node.getChildren().forEach(build::addChild);
                dispatcher.getRoot().addChild(build);
            }else{
                CommandUtils.sendChatMessage(Component.literal("Redirect over 2"+ entry.getValue()));
                break;
            }
        }
    }

    public void makeMap(){
        makeMap(commands);
    }

    private void makeMap(ConfigStringList commands) {
        redirects.clear();
        commands.getStrings().forEach(s -> {
            String[] split = s.split("->");
            if(split.length == 2){
                String command = split[0].trim();
                String redirect = split[1].trim();
                redirects.put(command,redirect);


            }
        });
    }

    public CommandRedirect(String name) {
        super(name);
    }

}
