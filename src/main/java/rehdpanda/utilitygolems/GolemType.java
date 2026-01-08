package rehdpanda.utilitygolems;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

/// DESCRIBES THE VARIOUS TYPES OF GOLEM

public enum GolemType {
    LAPIS("lapis_golem", "Lapis Golem", "textures/entity/lapis_golem.png", createAttributes(), GolemAI::initLapisGoals),
    REDSTONE("redstone_golem", "Redstone Golem", "textures/entity/redstone_golem.png", createAttributes(), GolemAI::initRedstoneGoals),
    EMERALD("emerald_golem", "Emerald Golem", "textures/entity/emerald_golem.png", createAttributes(), GolemAI::initEmeraldGoals);

    private final String name;
    private final String friendlyName;
    private final String texturePath;
    private final DefaultAttributeContainer.Builder attributes;
    private final Consumer<UtilityGolem> aiInitializer;

    GolemType(String name, String friendlyName, String texturePath, DefaultAttributeContainer.Builder attributes, Consumer<UtilityGolem> aiInitializer) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.texturePath = texturePath;
        this.attributes = attributes;
        this.aiInitializer = aiInitializer;
    }

    private static DefaultAttributeContainer.Builder createAttributes() {
        return CopperGolemEntity.createCopperGolemAttributes()
                .add(EntityAttributes.TEMPT_RANGE, 10.0);
    }

    public DefaultAttributeContainer.Builder getAttributes() {
        return attributes;
    }

    public String getName() {
        return this.name;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public Identifier getTexture() {
        Identifier texture = Identifier.of("utility-golems", texturePath);
        return texture;
    }

    public void initGoals(UtilityGolem golem) {
        if (aiInitializer != null) {
            aiInitializer.accept(golem);
        }
    }
}
