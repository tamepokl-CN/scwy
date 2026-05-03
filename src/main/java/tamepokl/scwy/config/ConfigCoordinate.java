package tamepokl.scwy.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.util.StringUtils;
@Deprecated
public class ConfigCoordinate extends ConfigBase<ConfigCoordinate> implements IStringRepresentable {
    protected int x;
    protected int y;
    protected int z;
    protected int defaultValueX;
    protected int defaultValueY;
    protected int defaultValueZ;

    public ConfigCoordinate(String name) {
        this(name, 0, 0, 0);
    }

    public ConfigCoordinate(String name, int defaultX, int defaultY, int defaultZ) {
        super(ConfigType.INTEGER, name);
        this.defaultValueX = defaultX;
        this.defaultValueY = defaultY;
        this.defaultValueZ = defaultZ;
        this.x = defaultX;
        this.y = defaultY;
        this.z = defaultZ;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public void setX(int x) {
        if (this.x != x) {
            this.x = x;
            this.markDirty();
            this.onValueChanged();
        }
    }

    public void setY(int y) {
        if (this.y != y) {
            this.y = y;
            this.markDirty();
            this.onValueChanged();
        }
    }

    public void setZ(int z) {
        if (this.z != z) {
            this.z = z;
            this.markDirty();
            this.onValueChanged();
        }
    }

    public void setCoordinate(int x, int y, int z) {
        boolean changed = false;
        if (this.x != x) {
            this.x = x;
            changed = true;
        }
        if (this.y != y) {
            this.y = y;
            changed = true;
        }
        if (this.z != z) {
            this.z = z;
            changed = true;
        }
        
        if (changed) {
            this.markDirty();
            this.onValueChanged();
        }
    }

    @Override
    public String getStringValue() {
        return this.x + "," + this.y + "," + this.z;
    }

    @Override
    public String getDefaultStringValue() {
        return this.defaultValueX + "," + this.defaultValueY + "," + this.defaultValueZ;
    }

    @Override
    public void setValueFromString(String value) {
        String[] parts = value.split(",");
        if (parts.length >= 3) {
            try {
                int newX = Integer.parseInt(parts[0].trim());
                int newY = Integer.parseInt(parts[1].trim());
                int newZ = Integer.parseInt(parts[2].trim());
                this.setCoordinate(newX, newY, newZ);
            } catch (NumberFormatException ignored) {
                // Invalid format, keep current values
            }
        }
    }

    @Override
    public void resetToDefault() {
        this.setCoordinate(this.defaultValueX, this.defaultValueY, this.defaultValueZ);
    }

    @Override
    public boolean isModified() {
        return this.x != this.defaultValueX || this.y != this.defaultValueY || this.z != this.defaultValueZ;
    }

    @Override
    public String getConfigGuiDisplayName() {
        return StringUtils.getTranslatedOrFallback(this.getPrettyName(), this.getName());
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.setValueFromString(element.getAsString());
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(this.getStringValue());
    }

    @Override
    public boolean isModified(String newValue) {
        return !this.getDefaultStringValue().equals(newValue);
    }
}
