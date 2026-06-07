package tamepokl.scwy.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigDouble;
import fi.dy.masa.malilib.config.IConfigFloat;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigFloat;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.gui.GuiConfigs;
import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.config.TweakerMoreOption;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jspecify.annotations.NonNull;
import tamepokl.scwy.config.ConfigTriggerHotkey;
import tamepokl.scwy.config.ExpandableConfig;
import tamepokl.scwy.config.ToolConfig;
import tamepokl.scwy.tool.base.ToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
public class ConfigEditor {
    public static LiteralArgumentBuilder<FabricClientCommandSource> builder = literal("configedit");
    public static final List<Mod> mods = new ArrayList<>();
    public static void init() {
        for (Mod mod : mods) {
            builder.then(mod.builder);
        }

    }
    static {
        mods.add(new Mod("scwy")
                .addTab(new Tab("tool")
                        .addConfigList(ToolManager.getConfigList())));
        mods.add(new Mod("tweakeroo")
                .addTab(new Tab("tweaks")
                        .addConfigList(GuiConfigs.TWEAK_LIST))
                .addTab(new Tab("generic")
                        .addConfigList(Configs.Generic.OPTIONS))
                .addTab(new Tab("fixes")
                        .addConfigList(Configs.Fixes.OPTIONS))
                .addTab(new Tab("disable")
                        .addConfigList(Configs.Disable.OPTIONS))
                .addTab(new Tab("internal")
                        .addConfigList(Configs.Internal.OPTIONS))
                .addTab(new Tab("lists")
                        .addConfigList(Configs.Lists.OPTIONS)));
        mods.add(new Mod("litematica")
                .addTab(new Tab("colors")
                        .addConfigList(fi.dy.masa.litematica.config.Configs.Colors.OPTIONS))
                .addTab(new Tab("generic")
                        .addConfigList(fi.dy.masa.litematica.config.Configs.Generic.OPTIONS))
                .addTab(new Tab("visuals")
                        .addConfigList(fi.dy.masa.litematica.config.Configs.Visuals.OPTIONS))
                .addTab(new Tab("infoOverlays")
                        .addConfigList(fi.dy.masa.litematica.config.Configs.InfoOverlays.OPTIONS)));
        mods.add(new Mod("itemscroller")
                .addTab(new Tab("generic")
                        .addConfigList(fi.dy.masa.itemscroller.config.Configs.Generic.OPTIONS))
                .addTab(new Tab("toggles")
                        .addConfigList(fi.dy.masa.itemscroller.config.Configs.Toggles.OPTIONS)));
        mods.add(new Mod("minuhud")
                .addTab(new Tab("generic")
                        .addConfigList(fi.dy.masa.minihud.config.Configs.Generic.OPTIONS))
                .addTab(new Tab("colors")
                        .addConfigList(fi.dy.masa.minihud.config.Configs.Colors.OPTIONS)));
//        mods.add(new Mod("tweakermore")
//                .addTab(new Tab("option")
//                        .addConfigList(TweakerMoreConfigs.getAllOptions().stream().map(TweakerMoreOption::getConfig).toList())));
    }
    public static class Mod{
        public Mod(String id) {
            this.id = id;
            builder = literal(this.id);
        }

        public String id;
        public List<Tab> tabs = new ArrayList<>();
        public LiteralArgumentBuilder<FabricClientCommandSource> builder;
        public Mod addTab(Tab tab) {
            this.tabs.add(tab);
            this.builder.then(tab.builder);
            return this;
        }
    }
    public static class Tab{
        private final String id;
        public List<MasaConfig<?>> configs = new ArrayList<>();
        public LiteralArgumentBuilder<FabricClientCommandSource> builder;
        public Tab addConfig(MasaConfig<?> config) {
            if(config == null) return null;
            configs.add(config);
            this.builder.then(config.getFinalBuilder());
            return this;
        }

        public Tab(String id) {
            this.id = id;
            this.builder = literal(this.id);
        }
        public Tab addConfigList(List<? extends IConfigBase> list){
            for (IConfigBase base : list) {
                this.addConfig(MasaConfig.parseConfig(base));
            }
            return this;
        }
    }
    public abstract static class MasaProcessor{
        public abstract boolean test(IConfigBase base);

        public abstract <C extends IConfigBase> LiteralArgumentBuilder<FabricClientCommandSource> process(LiteralArgumentBuilder<FabricClientCommandSource> builder, C config);
    }
    public abstract static class MasaConfig<T extends IConfigBase>{
        private static final Map<Class<?>, Function<IConfigBase, MasaConfig<?>>> CONFIG_MAP = new HashMap<>();
        public static final List<MasaProcessor> PROCESSORS = new ArrayList<>();
        static {
            CONFIG_MAP.put(ConfigBoolean.class , MasaConfig::wrapBoolean);
            CONFIG_MAP.put(ConfigBooleanHotkeyed.class,MasaConfig::wrapBoolean);
            CONFIG_MAP.put(FeatureToggle.class , MasaConfig::wrapBoolean);
            CONFIG_MAP.put(ConfigInteger.class , MasaConfig::wrapInteger);
            CONFIG_MAP.put(ConfigDouble.class , MasaConfig::wrapDouble);

            CONFIG_MAP.put(ConfigFloat.class , MasaConfig::wrapFloat);
            CONFIG_MAP.put(ConfigString.class , MasaConfig::wrapString);
            CONFIG_MAP.put(ConfigOptionList.class, MasaConfig::wrapOptionList);
            CONFIG_MAP.put(ConfigStringList.class, MasaConfig::wrapStringList);
            CONFIG_MAP.put(ToolConfig.class,MasaConfig::wrapToolConfig);
            CONFIG_MAP.put(ConfigTriggerHotkey.class , MasaConfig::wrapTriggerHotkey);
            CONFIG_MAP.put(ConfigColor.class, MasaConfig::wrapColor);

            PROCESSORS.add(new MasaProcessor() {
                @Override
                public boolean test(IConfigBase base) {
                    return base instanceof IConfigResettable;
                }

                @Override
                public <C extends IConfigBase> LiteralArgumentBuilder<FabricClientCommandSource> process(LiteralArgumentBuilder<FabricClientCommandSource> builder, C config) {

                    return builder.then(literal("reset")
                            .executes(ctx->this.reset(ctx,config)));
                }

                private int reset(CommandContext<FabricClientCommandSource> ctx,IConfigBase config) {
                    IConfigResettable res = (IConfigResettable) config;
                    res.resetToDefault();
                    ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.reset",config.getName(),config.getTranslatedName()));
                    return 1;
                }

            });

        }

        private static MasaConfig<?> wrapColor(IConfigBase config) {
            return new MasaConfigColor(config.getName(), (ConfigColor) config);
        }


        public final String name;

        public final T config;

        public MasaConfig(String name, T config) {
            this.name = name;
            this.config = config;
        }
        public abstract LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() ;

        //从IConfigBase获得MasaConfig

        public static MasaConfig<?> parseConfig(@NonNull IConfigBase base) {
            Class<?> clazz = base.getClass();
            while (clazz != null && clazz != Object.class) {
                Function<IConfigBase, MasaConfig<?>> factory = CONFIG_MAP.get(clazz);
                if (factory != null) {
                    MasaConfig<?> apply = factory.apply(base);
                    return apply;
                }
                clazz = clazz.getSuperclass();
            }
            System.out.println("Unsupported config type: " + base.getClass().getName());
            return null;

        }
        protected LiteralArgumentBuilder<FabricClientCommandSource> postProcessBuilder(
                LiteralArgumentBuilder<FabricClientCommandSource> builder) {
            for (MasaProcessor processor : PROCESSORS) {
                if (processor.test(config)) {
                    builder = processor.process(builder, config);
                }
            }
            return builder;
        }
        public ArgumentBuilder<FabricClientCommandSource, ?> getFinalBuilder() {
            return postProcessBuilder(getBuilder());
        }
        private static MasaConfig<?> wrapBoolean(IConfigBase config) {
            return new MasaIConfigBoolean(config.getName(), (IConfigBoolean)config);
        }
        private static MasaConfig<?> wrapTriggerHotkey(IConfigBase config) {
            return new ScwyTriggerHotKey(config.getName(), (ConfigTriggerHotkey) config);
        }

        private static MasaConfig<?> wrapToolConfig(IConfigBase config) {
            return new ScwyToolConfig(config.getName(),(ToolConfig) config);
        }
        private static MasaConfig<?> wrapOptionList(IConfigBase config) {
            return new MasaConfigOptionList(config.getName(), (ConfigOptionList)config);
        }
        private static MasaConfig<?> wrapStringList(IConfigBase config) {
            return new MasaConfigStringList(config.getName(), (ConfigStringList)config);
        }
        private static MasaConfig<?> wrapInteger(IConfigBase config) {
            return new MasaIConfigInteger(config.getName(), (IConfigInteger)config);
        }
        private static MasaConfig<?> wrapDouble(IConfigBase config) {
            return new MasaIConfigDouble(config.getName(), (IConfigDouble)config);
        }
        private static MasaConfig<?> wrapFloat(IConfigBase config) {
            return new MasaIConfigFloat(config.getName(), (IConfigFloat) config);
        }

        private static MasaConfig<?> wrapString(IConfigBase config) {
            return new MasaConfigString(config.getName(), (ConfigString)config);
        }
    }
    public abstract static class MasaIConfigValue<T extends IConfigValue, V> extends MasaConfig<T> {
        private final Supplier<ArgumentType<V>> argumentTypeSupplier;
        private final Class<V> argumentValueClass;
        public final ValueGetter<T,V> getter;
        private final ValueSetter<T,V> setter;
        private final String typeName;

        @FunctionalInterface
        public interface ValueGetter<T,V> {
            V get(T config);
        }

        @FunctionalInterface
        public interface ValueSetter<T,V> {
            void set(T config, V value);
        }

        public MasaIConfigValue(String name, T config, Supplier<ArgumentType<V>> argumentTypeSupplier, Class<V> argumentValueClass,
                                ValueGetter<T, V> getter, ValueSetter<T, V> setter, String typeName) {
            super(name, config);
            this.argumentTypeSupplier = argumentTypeSupplier;
            this.argumentValueClass = argumentValueClass;
            this.getter = getter;
            this.setter = setter;
            this.typeName = typeName;
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return literal(name)
                    .then(literal("value")
                            .then(argument("value", argumentTypeSupplier.get())
                                    .executes(this::changeValueWithArg)));
        }

        protected int changeValueWithArg(CommandContext<FabricClientCommandSource> ctx) {
            V prev = getter.get(config);
            V newValue = ctx.getArgument("value", argumentValueClass);
            changeValueTo(ctx, newValue, prev);
            return 1;
        }

        public void changeValueTo(CommandContext<FabricClientCommandSource> ctx, V newValue, V prev) {
            setter.set(config, newValue);
            ctx.getSource().sendFeedback(
                    StringUtils.translateable("scwy.message.configEdit.edit." + typeName, config.getName(),config.getTranslatedName(), prev, newValue)
            );
        }
    }
    public static class MasaIConfigInteger extends MasaIConfigValue<IConfigInteger, Integer> {
        public MasaIConfigInteger(String name, IConfigInteger config) {
            super(name, config, IntegerArgumentType::integer, int.class, IConfigInteger::getIntegerValue, IConfigInteger::setIntegerValue, "IInteger");
        }
    }
    public static class MasaConfigString extends MasaIConfigValue<ConfigString , String>{
        public MasaConfigString(String name, ConfigString config) {
            super(name, config, StringArgumentType::string, String.class, ConfigString::getStringValue, ConfigString::setValueFromString, "String");
        }
    }
    public static class MasaConfigColor extends MasaIConfigValue<ConfigColor, Integer> {
        public MasaConfigColor(String name, ConfigColor config) {
            super(name, config, IntegerArgumentType::integer, int.class, ConfigColor::getIntegerValue, ConfigColor::setIntegerValue, "Color");
        }
    }
    public static class MasaIConfigDouble extends MasaIConfigValue<IConfigDouble, Double> {
        public MasaIConfigDouble(String name, IConfigDouble config) {
            super(name, config, DoubleArgumentType::doubleArg, double.class, IConfigDouble::getDoubleValue, IConfigDouble::setDoubleValue, "IDouble");
        }
    }
    public static class MasaIConfigFloat extends MasaIConfigValue<IConfigFloat, Float> {
        public MasaIConfigFloat(String name, IConfigFloat config) {
            super(name, config, FloatArgumentType::floatArg, float.class, IConfigFloat::getFloatValue, IConfigFloat::setFloatValue, "IFloat");
        }
    }
    public static class MasaIConfigBoolean extends MasaIConfigValue<IConfigBoolean , Boolean> {
        public MasaIConfigBoolean(String name, IConfigBoolean config) {
            super(name, config, BoolArgumentType::bool, boolean.class, IConfigBoolean::getBooleanValue, IConfigBoolean::setBooleanValue, "IBoolean");
        }
        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return super.getBuilder()
                    .then(literal("toggle")
                            .executes(this::changeValue));
        }

        private int changeValue(CommandContext<FabricClientCommandSource> ctx) {
            this.changeValueTo(ctx, !getter.get(config), getter.get(config));
            return 1;
        }

    }
    public static class ScwyToolConfig extends MasaIConfigValue<ToolConfig,Boolean>{
        public ScwyToolConfig(String name, ToolConfig config) {
            super(name, config, BoolArgumentType::bool, boolean.class, IConfigBoolean::getBooleanValue, IConfigBoolean::setBooleanValue, "IBoolean");
        }
        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return super.getBuilder()
                    .then(literal("toggle")
                            .executes(this::changeValue))
                            .then(this.getChildrenBuilder());
        }

        private ArgumentBuilder<FabricClientCommandSource, ?> getChildrenBuilder() {
            LiteralArgumentBuilder<FabricClientCommandSource> root = literal("child");
            for (IConfigBase child : config.getChildren()) {
                MasaConfig<?> masa = MasaConfig.parseConfig(child);
                if (masa != null) {
                    root.then(masa.getFinalBuilder());
                }
            }
            return root;
        }

        private int changeValue(CommandContext<FabricClientCommandSource> ctx) {
            this.changeValueTo(ctx, !getter.get(config), getter.get(config));
            return 1;
        }
    }

    public static class MasaConfigOptionList extends MasaConfig<ConfigOptionList> {
        private final Class<? extends IConfigOptionListEntry> enumClass;

        public MasaConfigOptionList(String name, ConfigOptionList config) {
            super(name, config);
            this.enumClass = config.getOptionListValue().getClass();
        }
        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return literal(name)
                    .then(literal("set")
                            .then(argument("value", new IConfigOptionListArgumentType(enumClass))
                                    .executes(this::setValue)))
                    .then(literal("next")
                            .executes(this::next))
                    .then(literal("prev")
                            .executes(this::prev));
        }

        private int prev(CommandContext<FabricClientCommandSource> ctx) {
            IConfigOptionListEntry prev = config.getOptionListValue();
            return cycle(prev.cycle(false), ctx, prev);
        }

        private int next(CommandContext<FabricClientCommandSource> ctx) {
            IConfigOptionListEntry prev = config.getOptionListValue();
            return cycle(prev.cycle(true), ctx, prev);
        }

        private int cycle(IConfigOptionListEntry newValue, CommandContext<FabricClientCommandSource> ctx, IConfigOptionListEntry prev1) {
            config.setOptionListValue(newValue);
            ctx.getSource().sendFeedback(
                    StringUtils.translateable("scwy.message.configEdit.edit.IOptionList", config.getName(),config.getTranslatedName(), prev1.getStringValue(), newValue.getStringValue())
            );
            return 1;
        }
        private int setValue(CommandContext<FabricClientCommandSource> ctx) {
            String value = ctx.getArgument("value", String.class);
            IConfigOptionListEntry prev = config.getOptionListValue();
            IConfigOptionListEntry newValue = prev.fromString(value);
            config.setOptionListValue(newValue);
            ctx.getSource().sendFeedback(
                    StringUtils.translateable("scwy.message.configEdit.edit.IOptionList", config.getName(),config.getTranslatedName(), prev.getStringValue(), newValue.getStringValue())
            );
            return 1;
        }

        public static class IConfigOptionListArgumentType implements ArgumentType<String> {
            public void setEnumClass(Class<? extends IConfigOptionListEntry> enumClass) {
                this.enumClass = enumClass;
            }

            public IConfigOptionListArgumentType() {
            }

            public Class<? extends IConfigOptionListEntry> enumClass;

            public IConfigOptionListArgumentType(Class<? extends IConfigOptionListEntry> enumClass) {
                this.enumClass = enumClass;
            }

            @Override
            public String parse(StringReader reader) throws CommandSyntaxException {
                String value = reader.readString();
                if(!isValid(value))throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Invalid Type");
                return value;
            }

            private boolean isValid(String value) {
                if(enumClass==null){
                    return false;
                }
                try {
                    for (IConfigOptionListEntry constant : enumClass.getEnumConstants()) {
                        if(constant instanceof IConfigOptionListEntry entry){
                            if(entry.getStringValue().equals(value))return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
                if(enumClass==null)return builder.buildFuture();
                try {
                    for (Object constant : enumClass.getEnumConstants()) {
                        if (constant instanceof IConfigOptionListEntry entry) {
                            builder.suggest(entry.getStringValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return builder.buildFuture();
            }
        }
    }
    public static class ScwyTriggerHotKey extends MasaConfig<ConfigTriggerHotkey> {
        public ScwyTriggerHotKey(String name, ConfigTriggerHotkey config) {
            super(name, config);
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return literal("trigger")
                    .executes(this::trigger);
        }

        private int trigger(CommandContext<FabricClientCommandSource> ctx) {

            config.trigger();
            ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.configEdit.trigger.triggered", config.getName(),config.getTranslatedName()));
            return 1;
        }
    }
    public static class MasaConfigStringList extends MasaConfig<ConfigStringList> {
        public MasaConfigStringList(String name, ConfigStringList config) {
            super(name, config);
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
            return literal(name)
                    .then(literal("add")
                            .then(argument("value",StringArgumentType.string())
                                    .executes(this::add)))
                    .then(literal("remove")
                            .then(argument("value",new MasaConfigStringListArgumentType(config))
                                    .executes(this::remove)))
                    .then(literal("replace")
                            .then(argument("value",new MasaConfigStringListArgumentType(config))
                                    .then(argument("replace",StringArgumentType.string())
                                            .executes(this::replace))));
        }

        private int replace(CommandContext<FabricClientCommandSource> ctx) {

            List<String> strings = config.getStrings();
            String value = ctx.getArgument("value", String.class);
            if(!strings.contains(value)){
                ctx.getSource().sendError(StringUtils.translateable("scwy.message.configEdit.replace.failed.StringList",config.getName(),config.getTranslatedName(),value));
                return 0;
            }
            String replace = ctx.getArgument("replace", String.class);
            strings.set(strings.indexOf(value),replace);
            ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.configEdit.replace.successful.StringList",config.getName(),config.getTranslatedName(),value,replace));
            return 1;
        }

        private int add(CommandContext<FabricClientCommandSource> ctx) {
            List<String> strings = config.getStrings();
            String value = ctx.getArgument("value", String.class);
            strings.add(value);
            ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.configEdit.add.successful.StringList",config.getName(),config.getTranslatedName(),value));
            return 1;
        }

        private int remove(CommandContext<FabricClientCommandSource> ctx) {
            List<String> strings = config.getStrings();
            String value = ctx.getArgument("value", String.class);
            boolean result = strings.remove(value);
            if(result){
                ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.configEdit.remove.successful.StringList",config.getName(),config.getTranslatedName(),value));
                return 1;
            }else {

                ctx.getSource().sendFeedback(StringUtils.translateable("scwy.message.configEdit.remove.failed.StringList",config.getName(),config.getTranslatedName(),value));
                return 0;
            }
        }
        public static class MasaConfigStringListArgumentType implements ArgumentType<String> {
            public ConfigStringList config;

            public MasaConfigStringListArgumentType(ConfigStringList config) {
                this.config = config;
            }

            public MasaConfigStringListArgumentType() {
            }

            public void setConfig(ConfigStringList config) {
                this.config = config;
            }

            @Override
            public String parse(StringReader reader) throws CommandSyntaxException {
//                String s = reader.readString();//BUG:parse result is not the same as the value(minecraft:xxx->minecraft)
                String s = readUntilSpace(reader);
                if(config!=null&&config.getStrings().contains(s))return s;
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Invalid Element" + s);
            }

            @Override
            public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
                if(config == null) return builder.buildFuture();
                for (String s : config.getStrings()) {
                    builder.suggest(s);
                }
                return builder.buildFuture();
            }
        }
    }
    public static String readUntilSpace(StringReader reader) throws CommandSyntaxException {
        if(StringReader.isQuotedStringStart(reader.peek())){
            return reader.readQuotedString();
        }

        final int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }
}



