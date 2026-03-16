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
            GolemAI::initLapisGoals,
            null),
    REDSTONE("redstone_golem", "Redstone Golem", "textures/entity/redstone_golem.png",
            GolemAI::initRedstoneGoals,
            null),
    EMERALD("emerald_golem", "Emerald Golem", "textures/entity/emerald_golem.png",
            GolemAI::initEmeraldGoals,
            null),
    GOLD("gold_golem", "Gold Golem", "textures/entity/gold_golem.png",
        GolemAI::initGoldGoals,
            null),
    AMETHYST("amethyst_golem", "Amethyst Golem", "textures/entity/amethyst_golem.png",
            GolemAI::initAmethystGoals,
            null),

    NETHERITE("netherite_golem", "Netherite Golem", "textures/entity/netherite_golem.png",
        GolemAI::initNetheriteGoals,
            null
        ),
    ANCIENT("ancient_golem", "Ancient Golem", "textures/entity/ancient_golem.png",
            GolemAI::initAncientGoals,
            null
    ),
    FURNACE("furnace_golem", "Furnace Golem", "textures/entity/furnace_golem.png",
            GolemAI::initFurnaceGoals,
            null
    ),
    BAMBOO("bamboo_golem", "Bamboo Golem", "textures/entity/bamboo_golem.png",
            GolemAI::initBambooGoals,
            null
    ),
    DIAMOND("diamond_golem", "Diamond Golem", "textures/entity/diamond_golem.png",
            GolemAI::initDiamondGoals,
            null
    ),
    SPONGE("sponge_golem", "Sponge Golem", "textures/entity/sponge_golem.png",
            GolemAI::initSpongeGoals,
            null
    ),
    DEEPSLATE("deepslate_golem", "Deepslate Golem", "textures/entity/deepslate_golem.png",
            GolemAI::initDeepslateGoals,
            null
    ),
    JUKEBOX("jukebox_golem", "Jukebox Golem", "textures/entity/jukebox_golem.png",
            GolemAI::initJukeboxGoals,
            null
    ),
    LAMP("lamp_golem", "Lamp Golem", "textures/entity/lamp_golem.png",
            GolemAI::initLampGoals,
            null
    ),
    NETHER_WART("nether_wart_golem", "Nether Wart Golem", "textures/entity/nether_wart_golem.png",
            GolemAI::initNetherWartGoals,
            null
    ),
    SMOKER("smoker_golem", "Smoker Golem", "textures/entity/smoker_golem.png",
            GolemAI::initSmokerGoals,
            null
    ),
    BLAST_FURNACE("blast_furnace_golem", "Blast Furnace Golem", "textures/entity/blast_furnace_golem.png",
            GolemAI::initBlastFurnaceGoals,
            null
    ),
    MEDIC("medic_golem", "Medic Golem", "textures/entity/medic_golem.png",
            GolemAI::initMedicGoals,
            null
    ),
    CACTUS("cactus_golem", "Cactus Golem", "textures/entity/cactus_golem.png",
            GolemAI::initCactusGoals,
            null
    ),
    HONEYCOMB("honeycomb_golem", "Honeycomb Golem", "textures/entity/honeycomb_golem.png",
            GolemAI::initHoneycombGoals,
            null
    ),
    HOPPER("hopper_golem", "Hopper Golem", "textures/entity/hopper_golem.png",
            GolemAI::initHopperGoals,
            null
    ),
    TINTED_GLASS("tinted_glass_golem", "Tinted Glass Golem", "textures/entity/tinted_glass_golem.png",
            GolemAI::initTintedGlassGoals,
            null
    );


    private final String name;
    private final String friendlyName;
    private final String texturePath;
    private final Consumer<UtilityGolem> aiInitializer;
    private final Block targetChestType;


    GolemType(String name, String friendlyName, String texturePath, Consumer<UtilityGolem> aiInitializer, Block targetChestType) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.texturePath = texturePath;
        this.aiInitializer = aiInitializer;
        this.targetChestType = targetChestType;
    }

    public DefaultAttributeContainer.Builder getAttributes() {
        ConfigManager.GolemStats stats = ConfigManager.getConfig().golems.get(this.name);
        return CopperGolemEntity.createCopperGolemAttributes()
                .add(EntityAttributes.TEMPT_RANGE, 10.0)
                .add(EntityAttributes.MAX_HEALTH, stats.maxHealth)
                .add(EntityAttributes.MOVEMENT_SPEED, stats.movementSpeed)
                .add(EntityAttributes.ATTACK_DAMAGE, stats.attackDamage)
                .add(EntityAttributes.FOLLOW_RANGE, stats.followRange)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, stats.knockbackResistance)
                .add(EntityAttributes.ARMOR, stats.armor)
                .add(EntityAttributes.ARMOR_TOUGHNESS, stats.armorToughness)
                .add(EntityAttributes.ATTACK_SPEED, stats.attackSpeed)
                .add(EntityAttributes.ATTACK_KNOCKBACK, stats.attackKnockback);
    }

    public String getName() {
        return this.name;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public Identifier getTexture() {
        return Identifier.of("utility-golems", texturePath);
    }

    public Block getChestBlock() {
        if (targetChestType != null) {
            return targetChestType;
        }
        if (this == LAMP || this == FURNACE || this == JUKEBOX || this == SMOKER || this == BLAST_FURNACE || this == MEDIC || this == CACTUS || this == TINTED_GLASS) return null;
        return UGBlocks.GOLEM_CHESTS.get(this);
    }

    public void initGoals(UtilityGolem golem) {
        if (aiInitializer != null) {
            aiInitializer.accept(golem);
        }
    }
}
