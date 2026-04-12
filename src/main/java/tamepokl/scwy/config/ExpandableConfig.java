package tamepokl.scwy.config;

import fi.dy.masa.malilib.config.IConfigBase;

import java.util.List;

public interface ExpandableConfig{
    boolean isExpanded();
    void setExpanded(boolean expanded);
    List<IConfigBase> getChildren();
}
