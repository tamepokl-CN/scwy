package tamepokl.scwy.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import tamepokl.scwy.Reference;
import tamepokl.scwy.config.Configs;
import tamepokl.scwy.config.ExpandableConfig;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigs extends GuiConfigsBase {
    private static ConfigGuiTab currentTab = ConfigGuiTab.GENERAL;

    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, "scwy.gui.title.configs", String.format("%s", Reference.MOD_VERSION));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values()) {
            x += this.createButton(x, y, -1, tab) + 2;
        }
    }

    private int createButton(int x, int y, int width, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(currentTab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth();
    }

    @Override
    protected int getConfigWidth() {
        return 210;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<IConfigBase> topLevelConfigs = new ArrayList<>();
        switch (currentTab) {
            case GENERAL:
                topLevelConfigs.addAll(Configs.Generic.OPTIONS);
                break;

            case TOOLS:
                topLevelConfigs.addAll(Configs.Tools.TOP_LEVEL);
                break;

            case TWEAKS:
                topLevelConfigs.addAll(Configs.Tweaks.TOP_LEVEL);
                break;

        }
        List<ConfigOptionWrapper> wrappers = new ArrayList<>();
        for (IConfigBase config : topLevelConfigs) {
            wrappers.addAll(ConfigOptionWrapper.createFor(List.of(config)));
            addExpandedChildren(wrappers, config);
        }
        return wrappers;
    }
    private void addExpandedChildren(List<ConfigOptionWrapper> wrappers, IConfigBase config) {
        if (config instanceof ExpandableConfig expandable && expandable.isExpanded()) {
            for (IConfigBase child : expandable.getChildren()) {
                wrappers.addAll(ConfigOptionWrapper.createFor(List.of(child)));
                // Recursively add children of child expandables
                addExpandedChildren(wrappers, child);
            }
        }
    }

        private static class ButtonListener implements IButtonActionListener {
            private final GuiConfigs parent;
            private final ConfigGuiTab tab;

            public ButtonListener(ConfigGuiTab tab, GuiConfigs parent) {
                this.tab = tab;
                this.parent = parent;
            }

            @Override
            public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
                GuiConfigs.currentTab = this.tab;

                this.parent.reCreateListWidget();
                this.parent.getListWidget().resetScrollbarPosition();
                this.parent.initGui();
            }
        }

    @Override
    protected WidgetListConfigOptionScwy createListWidget(int listX, int listY) {
        return new WidgetListConfigOptionScwy(listX, listY,
                this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), 0.f, this.useKeybindSearch(), this);
    }


        public void refreshList () {
            this.reCreateListWidget();
            this.initGui();
        }


        public enum ConfigGuiTab {
            GENERAL("scwy.gui.button.config_gui.generic"),
            TWEAKS("scwy.gui.button.config_gui.tweaks"),
            TOOLS("scwy.gui.button.config_gui.tools"),

            ;
            private final String translationKey;

            ConfigGuiTab(String translationKey) {
                this.translationKey = translationKey;
            }

            public String getDisplayName() {
                return StringUtils.translate(this.translationKey);
            }
        }

    }






