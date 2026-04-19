package tamepokl.scwy.gui;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.util.StringUtils;
import tamepokl.scwy.config.ConfigCoordinate;
import tamepokl.scwy.config.ConfigTriggerHotkey;
import tamepokl.scwy.config.Configs;
import tamepokl.scwy.config.ExpandableConfig;

//这一部分是ai写的
//可能借鉴了LPCTools
//
public class WidgetConfigOptionScwy extends WidgetConfigOption {
    private final GuiConfigs gui;
    private static final int CHILD_INDENT = 22;
    public WidgetConfigOptionScwy(int x, int y, int width, int height, int labelWidth, int configWidth,
                                  GuiConfigsBase.ConfigOptionWrapper wrapper, int listIndex, GuiConfigsBase host,
                                  WidgetListConfigOptionsBase<?, ?> parent, GuiConfigs gui) {
        super(x, y, width, height, labelWidth, configWidth, wrapper, listIndex, host, parent);
        this.gui = gui;
    }



    @Override
    protected void addConfigOption(int x, int y,int labelWidth, int configWidth, IConfigBase config) {
        int indent = getIndentForConfig(config);
        int xAdj = x + indent;
        // 确保label宽度至少为1，避免label位置计算错误
        int labelWidthAdj = Math.max(1, labelWidth - indent);
        //TriggerHotkey
        if (config instanceof ConfigTriggerHotkey triggerConfig) {
            addTriggerHotKey(y, configWidth, config, triggerConfig, xAdj, labelWidthAdj);
            return;
        }

        //ExpandableConfig
        if (config instanceof ExpandableConfig expandable && config.getType() == ConfigType.BOOLEAN) {
            addExpandableConfig(y, configWidth, config, expandable, xAdj, labelWidthAdj);
            return;
        }
        if (config instanceof ConfigCoordinate) {
            addCoordinateConfig(y, configWidth, config, (ConfigCoordinate) config, xAdj, labelWidthAdj);
            return;
        }
        super.addConfigOption(xAdj, y, labelWidthAdj, configWidth, config);






    }

    //有问题
    private void addCoordinateConfig(int y, int configWidth, IConfigBase config, ConfigCoordinate coordinate, int xAdj, int labelWidthAdj) {
        y += 1;
        int configHeight = 20;
        
        String configName = config.getConfigGuiDisplayName();
        this.addLabel(xAdj, y + 7, labelWidthAdj, 8, 0xFFFFFFFF, configName);
        
        String comment = this.host.getHoverInfoProvider() != null ?
                this.host.getHoverInfoProvider().getHoverInfo(config) : config.getComment();
        if (comment != null) {
            this.addConfigComment(xAdj, y + 5, labelWidthAdj, 12, comment);
        }
        
        xAdj += labelWidthAdj + 10;
        
        // 计算每个输入框的宽度
        int textFieldWidth = (configWidth - 20) / 3; // 减去间距
        int spacing = 10;
        
        // 创建XYZ标签
        int labelWidth = 20;
        this.addLabel(xAdj, y + 7, labelWidth, 8, 0xFFFFFFFF, "X:");
        xAdj += labelWidth;
        
        // X坐标输入框
        GuiTextFieldGeneric xField = new GuiTextFieldGeneric(
                xAdj, y + 1, textFieldWidth, configHeight - 2, this.textRenderer);
        xField.setValue(String.valueOf(coordinate.getX()));
        xField.setMaxLength(10);
        xAdj += textFieldWidth + spacing;
        
        this.addLabel(xAdj, y + 7, labelWidth, 8, 0xFFFFFFFF, "Y:");
        xAdj += labelWidth;
        
        // Y坐标输入框
        GuiTextFieldGeneric yField = new GuiTextFieldGeneric(
                xAdj, y + 1, textFieldWidth, configHeight - 2, this.textRenderer);
        yField.setValue(String.valueOf(coordinate.getY()));
        yField.setMaxLength(10);
        xAdj += textFieldWidth + spacing;
        
        this.addLabel(xAdj, y + 7, labelWidth, 8, 0xFFFFFFFF, "Z:");
        xAdj += labelWidth;
        
        // Z坐标输入框
        GuiTextFieldGeneric zField = new GuiTextFieldGeneric(
                xAdj, y + 1, textFieldWidth, configHeight - 2, this.textRenderer);
        zField.setValue(String.valueOf(coordinate.getZ()));
        zField.setMaxLength(10);


        this.addTextField(xField, new CoordinateTextFieldListener(xField , coordinate, CoordinateType.X));
        this.addTextField(yField, new CoordinateTextFieldListener(yField , coordinate, CoordinateType.Y));
        this.addTextField(zField, new CoordinateTextFieldListener(zField , coordinate, CoordinateType.Z));

    }
    
    private enum CoordinateType {
        X, Y, Z
    }

    
    private static class CoordinateTextFieldListener extends ConfigOptionChangeListenerTextField {
        private final ConfigCoordinate coordinate;
        private final CoordinateType type;
        
        public CoordinateTextFieldListener(GuiTextFieldGeneric field , ConfigCoordinate coordinate, CoordinateType type) {
            super(coordinate , field ,null);
            this.coordinate = coordinate;
            this.type = type;
        }
        
        @Override
        public boolean onTextChange(GuiTextFieldGeneric field) {
            System.out.println("Changed!");
            String text = field.getValue();
            if (text.isEmpty()) {
                field.setValue("0");
            }
            
            try {
                int value = Integer.parseInt(text);
                switch (this.type) {
                    case X -> this.coordinate.setX(value);
                    case Y -> this.coordinate.setY(value);
                    case Z -> this.coordinate.setZ(value);
                }
            } catch (NumberFormatException ignored) {
                // 如果不是有效整数，保持当前值
                switch (this.type) {
                    case X -> field.setValue(String.valueOf(this.coordinate.getX()));
                    case Y -> field.setValue(String.valueOf(this.coordinate.getY()));
                    case Z -> field.setValue(String.valueOf(this.coordinate.getZ()));
                }
            }
            
            return false;
        }
    }

    private void addExpandableConfig(int y, int configWidth, IConfigBase config, ExpandableConfig expandable, int xAdj, int labelWidthAdj) {
        y += 1;
        int configHeight = 20;

        String configName = config.getConfigGuiDisplayName();
        this.addLabel(xAdj, y + 7, labelWidthAdj, 8, 0xFFFFFFFF, configName);

        String comment = this.host.getHoverInfoProvider() != null ?
                this.host.getHoverInfoProvider().getHoverInfo(config) : config.getComment();
        if (comment != null) {
            this.addConfigComment(xAdj, y + 5, labelWidthAdj, 12, comment);
        }

        xAdj += labelWidthAdj + 10;

        // Toggable
        if (config instanceof fi.dy.masa.malilib.config.IHotkeyTogglable hotkeyTogglable) {
            // 布尔值按钮（较窄）
            int booleanBtnWidth = 60;
            ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(xAdj, y, booleanBtnWidth, configHeight, (IConfigBoolean) config);
            xAdj += booleanBtnWidth + 2;

            // 快捷键按钮
            int keybindWidth = configWidth - booleanBtnWidth - 2 - 22;
            IKeybind keybind = hotkeyTogglable.getKeybind();
            ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(xAdj, y, keybindWidth, configHeight, keybind, this.host);
            xAdj += keybindWidth + 2;

            // 快捷键设置按钮
            this.addWidget(new WidgetKeybindSettings(xAdj, y, 20, configHeight, keybind, config.getName(), this.parent, this.host.getDialogHandler()));
            xAdj += 20 + 4;

            // 展开/折叠按钮（图标）
            MaLiLibIcons icon = expandable.isExpanded() ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN;
            int expandBtnY = y + (configHeight - icon.getHeight()) / 2;
            ButtonGeneric expandButton = new ButtonGeneric(xAdj, expandBtnY, icon);

            // 重置按钮
            ButtonGeneric resetButton = this.createResetButton(0, y, (IConfigResettable) config);

            // 设置监听器
            ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton((IConfigResettable) config, resetButton, null);
            HotkeyedBooleanResetListener resetListener =
                    new HotkeyedBooleanResetListener((IConfigResettable) config, booleanButton, keybindButton, resetButton, this.host);

            this.host.addKeybindChangeListener(resetListener::updateButtons);

            this.addButton(booleanButton, booleanChangeListener);
            this.addButton(keybindButton, this.host.getButtonPressListener());
            this.addButton(expandButton, new ExpandButtonListener(expandable, gui, this.host));
            this.addButton(resetButton, resetListener);
        } else {
            // 没有快捷键，只显示布尔值按钮
            ConfigButtonBoolean optionButton = new ConfigButtonBoolean(xAdj, y, configWidth, configHeight, (IConfigBoolean) config);
            xAdj += configWidth + 4;

            // 展开/折叠按钮（图标）
            MaLiLibIcons icon = expandable.isExpanded() ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN;
            int expandBtnY = y + (configHeight - icon.getHeight()) / 2;
            ButtonGeneric expandButton = new ButtonGeneric(xAdj, expandBtnY, icon);

            // 重置按钮
            ButtonGeneric resetButton = this.createResetButton(0, y, (IConfigResettable) config);

            ConfigOptionChangeListenerButton listenerChange = new ConfigOptionChangeListenerButton((IConfigResettable) config, resetButton, null);
            ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig((IConfigResettable) config, new ConfigOptionListenerResetConfig.ConfigResetterButton(optionButton), resetButton, null);

            this.addButton(optionButton, listenerChange);
            this.addButton(expandButton, new ExpandButtonListener(expandable, gui, this.host));
            this.addButton(resetButton, listenerReset);
        }
        return;
    }

    private void addTriggerHotKey(int y, int configWidth, IConfigBase config, ConfigTriggerHotkey triggerConfig, int xAdj, int labelWidthAdj) {
        y += 1;
        int configHeight = 20;

        String configName = config.getConfigGuiDisplayName();
        this.addLabel(xAdj, y + 7, labelWidthAdj, 8, 0xFFFFFFFF, configName);

        String comment = this.host.getHoverInfoProvider() != null ?
                this.host.getHoverInfoProvider().getHoverInfo(config) : config.getComment();
        if (comment != null) {
            this.addConfigComment(xAdj, y + 5, labelWidthAdj, 12, comment);
        }

        xAdj += labelWidthAdj + 10;

        // 左侧触发按钮
        int triggerBtnWidth = 50;
        ButtonGeneric triggerButton = new ButtonGeneric(xAdj, y, triggerBtnWidth, configHeight, "触发");
        xAdj += triggerBtnWidth + 2;

        // 右侧热键绑定
        int keybindWidth = configWidth - triggerBtnWidth - 2 - 22;
        IKeybind keybind = triggerConfig.getKeybind();
        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(xAdj, y, keybindWidth, configHeight, keybind, this.host);
        xAdj += keybindWidth + 2;

        // 热键设置按钮
        this.addWidget(new WidgetKeybindSettings(xAdj, y, 20, 20, keybind, config.getName(), this.parent, this.host.getDialogHandler()));

        // 重置按钮
        ButtonGeneric resetButton = this.createResetButton(0, y, triggerConfig);

        // 监听器
        this.addButton(triggerButton, (btn, mouseBtn) -> triggerConfig.trigger());
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, (btn, mouseBtn) -> {
            triggerConfig.resetToDefault();
            keybindButton.updateDisplayString();
            resetButton.setEnabled(triggerConfig.isModified());
        });
        return;
    }

    //只支持一层缩进
    private int getIndentForConfig(IConfigBase config) {
        // 只有当配置是ExpandableConfig的子配置（非ExpandableConfig本身）时才应用缩进
        if (config instanceof ExpandableConfig) {
            return 0; // ExpandableConfig本身不缩进
        }
        
        // 检查是否是子配置
        if (Configs.Tools.isChildConfig(config)) {
            return CHILD_INDENT;
        }
        return 0;
    }
    private int getAlignedResetX() {
        // Calculate reset button position based on auto-sized width
        String labelReset = StringUtils.translate("malilib.gui.button.reset.caps");
        int resetWidth = this.getStringWidth(labelReset) + 10; // Same padding as ButtonGeneric uses
        int rightEdge = this.getX() + this.getWidth() - 2;
        return Math.max(this.getX(), rightEdge - resetWidth);
    }
    protected ButtonGeneric createResetButton(int x, int y, IConfigResettable config) {
        // Use auto-width (-1) like default malilib, but right-aligned
        String labelReset = StringUtils.translate("malilib.gui.button.reset.caps");
        int resetWidth = this.getStringWidth(labelReset) + 10;
        int rightEdge = this.getX() + this.getWidth() - 2;
        int resetX = Math.max(this.getX(), rightEdge - resetWidth);
        ButtonGeneric resetButton = new ButtonGeneric(resetX, y, -1, 20, labelReset);
        resetButton.setEnabled(config.isModified());
        return resetButton;
    }
    private static class ExpandButtonListener implements IButtonActionListener {
        private final ExpandableConfig expandable;
        private final GuiConfigs gui;
        private final fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui host;

        public ExpandButtonListener(ExpandableConfig expandable, GuiConfigs gui,
                                    fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui host) {
            this.expandable = expandable;
            this.gui = gui;
            this.host = host;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            expandable.setExpanded(!expandable.isExpanded());
            if (gui != null) {
                gui.refreshList();
            } else if (host instanceof GuiConfigs configs) {
                configs.refreshList();
            }
        }
    }

    @Override
    protected void addConfigButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton) {
        ButtonGeneric resetButton = this.createResetButton(0, yReset, config);
        ConfigOptionChangeListenerButton listenerChange = new ConfigOptionChangeListenerButton(config, resetButton, null);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigOptionListenerResetConfig.ConfigResetterButton(optionButton), resetButton, null);
        this.addButton(optionButton, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    @Override
    protected void addConfigTextFieldEntry(int x, int y, int resetX, int configWidth, int configHeight, fi.dy.masa.malilib.config.IConfigValue config) {
        super.addConfigTextFieldEntry(x, y, getAlignedResetX(), configWidth, configHeight, config);
    }

    @Override
    protected void addConfigSliderEntry(int x, int y, int resetX, int configWidth, int configHeight, fi.dy.masa.malilib.config.IConfigSlider config) {
        super.addConfigSliderEntry(x, y, getAlignedResetX(), configWidth, configHeight, config);
    }

    @Override
    protected void addKeybindResetButton(int x, int y, fi.dy.masa.malilib.hotkeys.IKeybind keybind, fi.dy.masa.malilib.gui.button.ConfigButtonKeybind buttonHotkey) {
        super.addKeybindResetButton(getAlignedResetX(), y, keybind, buttonHotkey);
    }

    @Override
    protected void addBooleanAndHotkeyWidgets(int x, int y, int configWidth,
                                              IConfigResettable resettableConfig,
                                              IConfigBoolean booleanConfig,
                                              fi.dy.masa.malilib.hotkeys.IKeybind keybind) {
        int booleanBtnWidth = 60;
        ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, booleanConfig);
        x += booleanBtnWidth + 2;
        configWidth -= booleanBtnWidth + 2 + 22;

        fi.dy.masa.malilib.gui.button.ConfigButtonKeybind keybindButton =
                new fi.dy.masa.malilib.gui.button.ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings(
                x, y, 20, 20, keybind, booleanConfig.getName(), this.parent, this.host.getDialogHandler()));

        ButtonGeneric resetButton = this.createResetButton(0, y, resettableConfig);

        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(resettableConfig, resetButton, null);
        WidgetConfigOption.HotkeyedBooleanResetListener resetListener =
                new WidgetConfigOption.HotkeyedBooleanResetListener(resettableConfig, booleanButton, keybindButton, resetButton, this.host);

        this.host.addKeybindChangeListener(resetListener::updateButtons);

        this.addButton(booleanButton, booleanChangeListener);
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, resetListener);
    }
    
}
