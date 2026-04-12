package tamepokl.scwy.gui;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;

public class WidgetListConfigOptionScwy extends WidgetListConfigOptions {
    private final GuiConfigs gui;

    public WidgetListConfigOptionScwy(int x, int y, int width, int height, int configWidth, float zLevel, boolean useKeybindSearch, GuiConfigs gui) {
        super(x, y, width, height, configWidth, zLevel, useKeybindSearch, gui);
        this.gui = gui;
    }

    @Override
    protected WidgetConfigOption createListEntryWidget(int x, int y, int listIndex, boolean isOdd, GuiConfigsBase.ConfigOptionWrapper wrapper) {
        return new WidgetConfigOptionScwy(x, y, this.browserEntryWidth, this.browserEntryHeight,
                this.maxLabelWidth, this.configWidth, wrapper, listIndex, this.parent, this, gui);
    }
}