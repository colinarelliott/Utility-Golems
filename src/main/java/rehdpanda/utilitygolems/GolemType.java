package rehdpanda.utilitygolems;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.util.Identifier;

/// DESCRIBES THE VARIOUS TYPES OF GOLEM

public enum GolemType {
    LAPIS("lapis_golem", "textures/entity/lapis_golem.png", CopperGolemEntity.createCopperGolemAttributes()),
    REDSTONE("redstone_golem", "textures/entity/redstone_golem.png", CopperGolemEntity.createCopperGolemAttributes()),
    EMERALD("emerald_golem", "textures/entity/emerald_golem.png", CopperGolemEntity.createCopperGolemAttributes());

    private final String name;
    private final String texturePath;
    private final DefaultAttributeContainer.Builder attributes;

    GolemType(String name, String texturePath, DefaultAttributeContainer.Builder attributes) {
        this.name = name;
        this.texturePath = texturePath;
        this.attributes = attributes;
    }

    public DefaultAttributeContainer.Builder getAttributes() {
        return attributes;
    }

    public String getName() {
        return this.name;
    }

    public Identifier getTexture() {
        Identifier texture = Identifier.of("utility-golems", texturePath);
        return texture;
    }
}
