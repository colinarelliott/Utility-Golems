package rehdpanda.utilitygolems;

import net.minecraft.text.Text;

public enum BuildPattern {
    NONE("None"),
    PLATFORM("Platform"),
    REPLACE("Replace Blocks"),
    TOWER("Large Tower"),
    SCHEMATIC("Schematic");

    private final String displayName;

    BuildPattern(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Text getText() {
        return Text.literal(displayName);
    }
}
