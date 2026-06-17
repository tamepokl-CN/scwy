package tamepokl.scwy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.seruco.encoding.base62.Base62;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamepokl.scwy.Reference;
import tamepokl.scwy.tool.base.ToolManager;
import tamepokl.scwy.utils.ScwyUtils;

import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static tamepokl.scwy.tool.HomeCommandRedirectTool.enable;
import static tamepokl.scwy.utils.CommandUtils.sendCommand;

public class HomeCommandRedirect {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> homebuilder;

    public static LiteralArgumentBuilder<ClientSuggestionProvider> setbuilder;

    public static LiteralArgumentBuilder<ClientSuggestionProvider> delbuilder;

    public static LiteralArgumentBuilder<ClientSuggestionProvider> listbuilder;

    public static LiteralArgumentBuilder<FabricClientCommandSource> askbuilder;

    static Comparator<String> c = Comparator.<String,Boolean>comparing(s -> !s.matches(".*[\\u4e00-\\u9fa5].*"))
            .thenComparing(s -> s, Collator.getInstance(Locale.CHINA));

    private static List<String> homeList = new ArrayList<>();

    //是否有home tp
    public static boolean hasCommand = false;
    static {
        homebuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("home")
                        .then(RequiredArgumentBuilder.argument("name", new HomeArgumentType()));
        delbuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("delhome")
                        .then(RequiredArgumentBuilder.argument("name", new HomeArgumentType()));
        setbuilder = LiteralArgumentBuilder.<ClientSuggestionProvider>literal("sethome")
                        .then(RequiredArgumentBuilder.argument("name", new ScwyStringArgumentType()));
        listbuilder = LiteralArgumentBuilder.literal("listhome");

        askbuilder = ClientCommandManager.literal("requesthomelist")
                        .executes(context -> {
                            askHomeList();
                            return 0;
                        });
        ScwyClientCommands.addToScwy(askbuilder);
        ToolManager.onFirstTick(minecraft -> {
            if(hasCommand()){
                askHomeList();
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((client, handler) -> {
            homeList.clear();
            hasCommand = false;
        });
    }


    //劫持sendCommand

    public static void onSendCommand(String command, CallbackInfo info) {
        if(!hasCommand())return;
        String[] s = command.split(" ");
        if( s.length==1 && s[0].equals("listhome")){
            info.cancel();
            sendCommand("home list");
            return;
        }
        if( s.length==2 ) {
            if(s[1].equals("list"))return;

            String encode = encodeIfNeeded(s[1]);
            switch (s[0]) {
                case "home":
                    info.cancel();
                    sendCommand("home tp " + encode);
                    break;

                case "delhome":
                    info.cancel();
                    sendCommand("home delete " + encode);
                    askHomeList();
                    break;
                case "sethome":
                    info.cancel();
                    sendCommand("home set " + encode);
                    askHomeList();
                    break;
            }
        }
    }

    public static boolean hasCommand(){
        if(!enable())return false;
        if(hasCommand) return true;
        CommandDispatcher<ClientSuggestionProvider> dispatcher = ScwyUtils.getCommands();
        if(dispatcher != null) {
            hasCommand = true;
            return dispatcher.findNode(List.of("home", "tp")) != null;
        }else {
            return false;
        }
    }

    public static boolean waitForSuggestions = false;
    private static final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "scwy-home-suggestions-timeout");
        t.setDaemon(true);
        return t;
    });
    public static void onHandleSuggestions(ClientboundCommandSuggestionsPacket packet) {
        if(!hasCommand())return;
        if(waitForSuggestions) {
            List<Suggestion> list = packet.toSuggestions().getList();
            homeList = list.stream().map(Suggestion::getText).map(HomeCommandRedirect::decodeIfNeeded).toList();
            waitForSuggestions = false;
            if(!homeList.isEmpty()) {
                Reference.LOGGER.info("Catch home list: " + homeList);
                reinjectHomeCommand();
            }else {
                Reference.LOGGER.info("Catch empty home list.");
                ScwyUtils.printTranslatableMessage("scwy.home.empty",false);
                noticeHomeList();
            }
        }

    }
    //提示玩家重新获取家列表
    public static void noticeHomeList() {
        MutableComponent component = Component.translatable("scwy.home.askHomeList")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/scwy requesthomelist")));
        ScwyUtils.printMessage(component,false);
    }
    private static final AtomicInteger requestIdCounter = new AtomicInteger(0);

    //请求家列表
    public static void askHomeList(){
        if(!hasCommand()) return;
        ServerboundCommandSuggestionPacket packet = new ServerboundCommandSuggestionPacket(0, "/home tp ");

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.getConnection().send(packet);
            waitForSuggestions = true;
        }
        final long requestId = requestIdCounter.incrementAndGet();
        timeoutScheduler.schedule(() -> {
            if (waitForSuggestions && requestIdCounter.get() == requestId) {
                Reference.LOGGER.warn("Home list request timed out");
                ScwyUtils.printTranslatableMessage("scwy.home.timeout",false);
                noticeHomeList();
                waitForSuggestions = false;
            }
        }, 5, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public static void reinjectHomeCommand() {
        if(homeList.isEmpty()) return;
        CommandDispatcher<ClientSuggestionProvider> dispatcher = ScwyUtils.getCommands();
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
                ScwyUtils.printTranslatableMessage("scwy.home.reinject.fail",false);
            }
        }
    }
    public static String readString(StringReader reader){
        StringBuilder result = new StringBuilder();

        while (reader.canRead()) {
            char c = reader.peek();
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                break;
            }
            result.append(c);
            reader.skip();
        }
        return result.toString();

    }


    //编码中文字符
    public static final String PREFIX = "+s";
    public static final String SUFFIX = "+e";

    public static boolean isAllowedInUnquotedString(final char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+';
    }

    public static String encodeIfNeeded(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        if (isEncodedString(str)) {
            return str;
        }

        if (needsEncoding(str)) {
            try {
                byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                Base62 base62 = Base62.createInstance();
                byte[] encoded = base62.encode(bytes);
                String encodedStr = new String(encoded, StandardCharsets.UTF_8);
                return PREFIX + encodedStr + SUFFIX;
            } catch (Exception e) {
                Reference.LOGGER.warn("Failed to encode string: {}", str, e);
                return str;
            }
        }

        return str;
    }

    public static String decodeIfNeeded(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        if (isEncodedString(str)) {
            try {
                String encoded = str.substring(PREFIX.length(), str.length() - SUFFIX.length());
                Base62 base62 = Base62.createInstance();
                byte[] bytes = base62.decode(encoded.getBytes(StandardCharsets.UTF_8));
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                Reference.LOGGER.warn("Failed to decode string: {}", str, e);
                return str;
            }
        }

        return str;
    }

    public static boolean isEncodedString(String in) {
        if (in == null || in.length() <= PREFIX.length() + SUFFIX.length()) {
            return false;
        }
        return in.startsWith(PREFIX) && in.endsWith(SUFFIX);
    }

    private static boolean needsEncoding(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isAllowedInUnquotedString(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @Unique
    public static Component processChatMessage(Component originalMessage) {
        if(!hasCommand) return originalMessage;
        String text = originalMessage.getString();

        if (!text.contains(PREFIX)) {
            return originalMessage;
        }
        if(!needProcess(text)){
            return originalMessage;
        }

        MutableComponent result = Component.empty();
        int lastIndex = 0;
        int currentIndex;

        while ((currentIndex = text.indexOf(PREFIX, lastIndex)) != -1) {
            int suffixIndex = text.indexOf(SUFFIX, currentIndex + PREFIX.length());

            if (suffixIndex == -1) {
                break;
            }

            int suffixEnd = suffixIndex + SUFFIX.length();

            if (lastIndex < currentIndex) {
                String beforeText = text.substring(lastIndex, currentIndex);
                result.append(Component.literal(beforeText).withStyle(originalMessage.getStyle()));
            }

            String encodedPart = text.substring(currentIndex, suffixEnd);
            String decodedText = decodeIfNeeded(encodedPart);

            MutableComponent decodedComponent = Component.literal(decodedText);
            decodedComponent.withStyle(originalMessage.getStyle());
            decodedComponent.withStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent.ShowText(Component.literal(encodedPart))
            ));

            result.append(decodedComponent);

            lastIndex = suffixEnd;
        }

        if (lastIndex < text.length()) {
            String remainingText = text.substring(lastIndex);
            result.append(Component.literal(remainingText).withStyle(originalMessage.getStyle()));
        }

        return result;
    }

    public static class HomeArgumentType implements ArgumentType<String> {

        public HomeArgumentType() {
        }


        @Override
        public String parse(StringReader reader) {
            return readString(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining();
            for (String home : homeList) {
                if (home.toLowerCase().startsWith(input)) {
                    builder.suggest(home);
                }
            }
            StringRange range = builder.build().getRange();
            Suggestions suggestions = new Suggestions(range, homeList.stream().sorted(c).map(s -> new Suggestion(range, s)).toList());
            return CompletableFuture.completedFuture(suggestions);

//            return builder.buildFuture();
        }

    }
    public static class ScwyStringArgumentType implements ArgumentType<String> {
        public ScwyStringArgumentType() {
        }

        @Override
        public String parse(StringReader reader) {
            // 没有校验长度是否超过，我想应该没必要
            return readString(reader);
        }

    }
    public static final String[] regex = new String[]{
            "Home '.*' set\\.",
            "Teleported to home: .* \\(.*\\)\\.",
            "Your current homes are: .*",
            "Home '.*' has been deleted\\."
    };
    public static boolean needProcess(String s){
        for (String string : regex) {
            if (s.matches(string)) {
                return true;
            }
        }
        return false;
    }


}
