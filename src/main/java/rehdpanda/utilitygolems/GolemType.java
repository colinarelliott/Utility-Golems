package rehdpanda.utilitygolems;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.util.Identifier;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.util.function.Consumer;

import net.minecraft.block.Block;

/// DESCRIBES THE VARIOUS TYPES OF GOLEM

public enum GolemType {
    LAPIS("lapis_golem", "Lapis Golem", "textures/entity/lapis_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 20.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.25),
            GolemAI::initLapisGoals,
            null),
    REDSTONE("redstone_golem", "Redstone Golem", "textures/entity/redstone_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 5.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3),
            GolemAI::initRedstoneGoals,
            null),
    EMERALD("emerald_golem", "Emerald Golem", "textures/entity/emerald_golem.png",
            baseAttributes()
                    //POSSIBLE ATTRIBUTES
                    .add(EntityAttributes.MAX_HEALTH, 30.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 2.0)
                    .add(EntityAttributes.FOLLOW_RANGE, 16.0)
                    .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.5)
                    .add(EntityAttributes.ARMOR, 2.0)
                    .add(EntityAttributes.ARMOR_TOUGHNESS, 1.0)
                    .add(EntityAttributes.ATTACK_SPEED, 1.0)
                    .add(EntityAttributes.ATTACK_KNOCKBACK, 0.2),
            GolemAI::initEmeraldGoals,
            null),
    GOLD("gold_golem", "Gold Golem", "textures/entity/gold_golem.png",
         baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 20.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3),
        GolemAI::initGoldGoals,
            null),
    AMETHYST("amethyst_golem", "Amethyst Golem", "textures/entity/amethyst_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 15.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3),
            GolemAI::initAmethystGoals,
            null),

    NETHERITE("netherite_golem", "Netherite Golem", "textures/entity/netherite_golem.png",
    baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 80.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
        GolemAI::initNetheriteGoals,
            null
        ),
    FURNACE("furnace_golem", "Furnace Golem", "textures/entity/furnace_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initFurnaceGoals,
            null
    ),
    BAMBOO("bamboo_golem", "Bamboo Golem", "textures/entity/bamboo_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initBambooGoals,
            null
    ),
    DIAMOND("diamond_golem", "Diamond Golem", "textures/entity/diamond_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.2)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initDiamondGoals,
            null
    ),
    SPONGE("sponge_golem", "Sponge Golem", "textures/entity/sponge_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initSpongeGoals,
            null
    ),
    DEEPSLATE("deepslate_golem", "Deepslate Golem", "textures/entity/deepslate_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initDeepslateGoals,
            null
    ),
    JUKEBOX("jukebox_golem", "Jukebox Golem", "textures/entity/jukebox_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initJukeboxGoals,
            null
    ),
    LAMP("lamp_golem", "Lamp Golem", "textures/entity/lamp_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initLampGoals,
            null
    ),
    NETHER_WART("nether_wart_golem", "Nether Wart Golem", "textures/entity/nether_wart_golem.png",
            baseAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 40.0)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5),
            GolemAI::initNetherWartGoals,
            null
    );


    private static Object BlockEntityType;
    private final String name;
    private final String friendlyName;
    private final String texturePath;
    private final DefaultAttributeContainer.Builder attributes;
    private final Consumer<UtilityGolem> aiInitializer;
    private final Block targetChestType;


    GolemType(String name, String friendlyName, String texturePath, DefaultAttributeContainer.Builder attributes, Consumer<UtilityGolem> aiInitializer, Block targetChestType) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.texturePath = texturePath;
        this.attributes = attributes;
        this.aiInitializer = aiInitializer;
        this.targetChestType = targetChestType;
    }

    private static DefaultAttributeContainer.Builder baseAttributes() {
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

    public Block getChestBlock() {
        if (targetChestType != null) {
            return targetChestType;
        }
        if (this == LAMP) return null;
        return UGBlocks.GOLEM_CHESTS.get(this);
    }

    public void initGoals(UtilityGolem golem) {
        if (aiInitializer != null) {
            aiInitializer.accept(golem);
        }
    }
}
