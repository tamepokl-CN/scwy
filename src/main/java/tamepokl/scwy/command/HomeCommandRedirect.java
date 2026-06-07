package tamepokl.scwy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.Reference;
import tamepokl.scwy.tool.HomeCommandRedirectTool;
import tamepokl.scwy.tool.base.ToolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static tamepokl.scwy.tool.HomeCommandRedirectTool.enable;
import static tamepokl.scwy.utils.CommandUtils.sendCommand;

public class HomeCommandRedirect {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> homebuilder;

    public static LiteralArgumentBuilder<ClientSuggestionProvider> setbuilder;

    public static LiteralArgumentBuilder<ClientSuggestionProvider> delbuilder;

    private static List<String> homeList = new ArrayList<>();

    public static LiteralArgumentBuilder<ClientSuggestionProvider> listbuilder;

    static {
        homebuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("home")
                        .then(RequiredArgumentBuilder.argument("name", new HomeArgumentType()));
        delbuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("delhome")
                        .then(RequiredArgumentBuilder.argument("name", new HomeArgumentType()));
        setbuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("sethome")
                        .then(RequiredArgumentBuilder.argument("name", StringArgumentType.string()));
        listbuilder = LiteralArgumentBuilder.literal("listhome");
        ToolManager.onFirstTick(minecraft -> {
            if(!enable())return;
            if(hasCommand()){
                askHomeList();
            }
        });
    }


    public static void onSendCommand(String command, CallbackInfo info) {
        if(!enable())return;
        String[] s = command.split(" ");
        if( s.length==1 && s[0].equals("listhome")){
            info.cancel();
            sendCommand("home list");
            return;
        }
        if( s.length==2 ) {
            if(s[1].equals("list"))return;
            switch (s[0]) {
                case "home":
                    info.cancel();
                    sendCommand("home tp " + s[1]);
                    break;

                case "delhome":
                    info.cancel();
                    sendCommand("home delete " + s[1]);
                    askHomeList();
                    break;
                case "sethome":
                    info.cancel();
                    sendCommand("home set " + s[1]);
                    askHomeList();
                    break;
            }
        }
    }


    public static class HomeArgumentType implements ArgumentType<String> {

        public HomeArgumentType() {
        }


        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            String value = reader.readString();
            return value;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining();
            for (String home : homeList) {
                if (home.toLowerCase().startsWith(input)) {
                    builder.suggest(home);
                }
            }

            return builder.buildFuture();
        }
    }

    public static CommandDispatcher<ClientSuggestionProvider> getCommands() {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null)
        {
            CommandDispatcher<ClientSuggestionProvider> dispatcher = networkHandler.getCommands();
            return dispatcher;
        }
        return null;
    }
    public static boolean hasCommand(){
        return Objects.requireNonNull(getCommands()).findNode(List.of("home", "tp")) != null;
    }
    public static boolean waitForSuggestions = false;

    public static void onHandleSuggestions(ClientboundCommandSuggestionsPacket packet) {
        if(!enable())return;
        if(waitForSuggestions) {
            List<Suggestion> list = packet.toSuggestions().getList();
            homeList = list.stream().map(Suggestion::getText).toList();
            waitForSuggestions = false;
            Reference.LOGGER.info("Catch home list: "+homeList);
            reinjectHomeCommand();
        }

    }

    public static void askHomeList(){
        if(!enable())return;
        ServerboundCommandSuggestionPacket packet = new ServerboundCommandSuggestionPacket(0, "/home tp ");

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.getConnection().send(packet);
            waitForSuggestions = true;
        }
    }
    @SuppressWarnings("unchecked")
    public static void reinjectHomeCommand() {
        if(homeList.isEmpty()) return;
        CommandDispatcher<ClientSuggestionProvider> dispatcher = getCommands();
        if (dispatcher != null) {
            try {
                RootCommandNode<ClientSuggestionProvider> root = dispatcher.getRoot();
                try {
                    var childrenField = CommandNode.class.getDeclaredField("children");
                    childrenField.setAccessible(true);

                    var children =
                        (Map<String, CommandNode<ClientSuggestionProvider>>) childrenField.get(root);

                    var literalsField = CommandNode.class.getDeclaredField("literals");
                    literalsField.setAccessible(true);
                    var literals =
                            (Map<String, LiteralCommandNode<ClientSuggestionProvider>>)literalsField.get(root);


                    LiteralCommandNode<ClientSuggestionProvider> build = homebuilder.build();
                    children.replace("home", build);
                    literals.replace("home",build);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access RootCommandNode fields via reflection", e);
                }
                root.addChild(setbuilder.build());
                root.addChild(delbuilder.build());
                root.addChild(listbuilder.build());
                Reference.LOGGER.info("Re-injected home command");
            } catch (Exception e) {
                Reference.LOGGER.warn("Failed to re-inject home command: {}", e.getMessage());
            }
        }
    }
}
