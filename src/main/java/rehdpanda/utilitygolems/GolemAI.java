package rehdpanda.utilitygolems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class GolemAI {

    // BOOLEAN FUNCTIONS
    public static boolean isIngredient(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.GLOWSTONE_DUST) || stack.is(net.minecraft.world.item.Items.REDSTONE)
                || stack.is(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE) || stack.is(net.minecraft.world.item.Items.MAGMA_CREAM) || stack.is(net.minecraft.world.item.Items.SUGAR)
                || stack.is(net.minecraft.world.item.Items.RABBIT_FOOT) || stack.is(net.minecraft.world.item.Items.GLISTERING_MELON_SLICE) || stack.is(net.minecraft.world.item.Items.SPIDER_EYE)
                || stack.is(net.minecraft.world.item.Items.PUFFERFISH) || stack.is(net.minecraft.world.item.Items.GOLDEN_CARROT) || stack.is(net.minecraft.world.item.Items.TURTLE_HELMET)
                || stack.is(net.minecraft.world.item.Items.PHANTOM_MEMBRANE) || stack.is(net.minecraft.world.item.Items.DRAGON_BREATH) || stack.is(net.minecraft.world.item.Items.GUNPOWDER);
    }
    public static boolean isSecondaryIngredient(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.GUNPOWDER) || stack.is(net.minecraft.world.item.Items.GLOWSTONE_DUST) || stack.is(net.minecraft.world.item.Items.REDSTONE) || stack.is(net.minecraft.world.item.Items.DRAGON_BREATH);
    }
    public static boolean isPrimaryIngredient(ItemStack stack) {
        return isIngredient(stack) && !isSecondaryIngredient(stack);
    }

    public static boolean isValidBreedingItem(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.WHEAT)
                || stack.is(net.minecraft.world.item.Items.CARROT)
                || stack.is(net.minecraft.world.item.Items.POTATO)
                || stack.is(net.minecraft.world.item.Items.BEETROOT)
                || stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS)
                || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS)
                || stack.is(net.minecraft.world.item.Items.MELON_SEEDS)
                || stack.is(net.minecraft.world.item.Items.GOLDEN_CARROT)
                || stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE)
                || stack.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(net.minecraft.world.item.Items.DANDELION)
                || stack.is(net.minecraft.world.item.Items.GLOW_BERRIES)
                || stack.is(net.minecraft.world.item.Items.SWEET_BERRIES)
                || stack.is(net.minecraft.world.item.Items.BEEF)
                || stack.is(net.minecraft.world.item.Items.CHICKEN)
                || stack.is(net.minecraft.world.item.Items.PORKCHOP)
                || stack.is(net.minecraft.world.item.Items.RABBIT)
                || stack.is(net.minecraft.world.item.Items.MUTTON)
                || stack.is(net.minecraft.world.item.Items.ROTTEN_FLESH)
                || stack.is(net.minecraft.world.item.Items.COOKED_BEEF)
                || stack.is(net.minecraft.world.item.Items.COOKED_CHICKEN)
                || stack.is(net.minecraft.world.item.Items.COOKED_PORKCHOP)
                || stack.is(net.minecraft.world.item.Items.COOKED_RABBIT)
                || stack.is(net.minecraft.world.item.Items.COOKED_MUTTON)
                || stack.is(net.minecraft.world.item.Items.COD)
                || stack.is(net.minecraft.world.item.Items.SALMON)
                || stack.is(net.minecraft.world.item.Items.TROPICAL_FISH_BUCKET)
                || stack.is(net.minecraft.world.item.Items.HAY_BLOCK)
                || stack.is(net.minecraft.world.item.Items.SEAGRASS)
                || stack.is(net.minecraft.world.item.Items.BAMBOO)
                || stack.is(net.minecraft.tags.ItemTags.FLOWERS)
                || stack.is(net.minecraft.world.item.Items.WARPED_FUNGUS)
                || stack.is(net.minecraft.world.item.Items.CRIMSON_FUNGUS)
                || stack.is(net.minecraft.world.item.Items.SLIME_BALL)
                || stack.is(net.minecraft.world.item.Items.CACTUS)
                || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS)
                || stack.is(net.minecraft.world.item.Items.SPIDER_EYE);
    }

    // INITIALIZE GOALS
    public static void initLapisGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.IRON_PICKAXE, net.minecraft.world.item.Items.DIAMOND_PICKAXE, net.minecraft.world.item.Items.NETHERITE_PICKAXE, net.minecraft.world.item.Items.GOLDEN_PICKAXE, net.minecraft.world.item.Items.NETHERITE_PICKAXE, net.minecraft.world.item.Items.STONE_PICKAXE, net.minecraft.world.item.Items.WOODEN_PICKAXE, net.minecraft.world.item.Items.COPPER_PICKAXE,
                net.minecraft.world.item.Items.IRON_SHOVEL, net.minecraft.world.item.Items.DIAMOND_SHOVEL, net.minecraft.world.item.Items.NETHERITE_SHOVEL, net.minecraft.world.item.Items.GOLDEN_SHOVEL, net.minecraft.world.item.Items.STONE_SHOVEL, net.minecraft.world.item.Items.WOODEN_SHOVEL, net.minecraft.world.item.Items.COPPER_SHOVEL
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new DigBlockGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.REDSTONE, net.minecraft.world.item.Items.REPEATER), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new ConnectRedstoneGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new TriggerRedstoneGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.EMERALD), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new TradeWithVillagerGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new CraftEmeraldsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(7, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initGoldGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT, net.minecraft.world.item.Items.GOLD_NUGGET), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new TradeWithPiglinGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initAmethystGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.WHEAT, net.minecraft.world.item.Items.CARROT, net.minecraft.world.item.Items.POTATO, net.minecraft.world.item.Items.BEETROOT, net.minecraft.world.item.Items.WHEAT_SEEDS, net.minecraft.world.item.Items.GOLDEN_APPLE, net.minecraft.world.item.Items.GOLDEN_CARROT), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new BreedAnimalsGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initNetheriteGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new MeleeAttackGoal(golem, 1.2D, false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.NETHERITE_SWORD, net.minecraft.world.item.Items.DIAMOND_SWORD, net.minecraft.world.item.Items.IRON_SWORD, net.minecraft.world.item.Items.GOLDEN_SWORD, net.minecraft.world.item.Items.STONE_SWORD, net.minecraft.world.item.Items.WOODEN_SWORD, net.minecraft.world.item.Items.COPPER_SWORD,
                net.minecraft.world.item.Items.NETHERITE_AXE, net.minecraft.world.item.Items.DIAMOND_AXE, net.minecraft.world.item.Items.IRON_AXE, net.minecraft.world.item.Items.GOLDEN_AXE, net.minecraft.world.item.Items.STONE_AXE, net.minecraft.world.item.Items.WOODEN_AXE, net.minecraft.world.item.Items.COPPER_AXE,
                net.minecraft.world.item.Items.NETHERITE_PICKAXE, net.minecraft.world.item.Items.DIAMOND_PICKAXE, net.minecraft.world.item.Items.IRON_PICKAXE, net.minecraft.world.item.Items.GOLDEN_PICKAXE, net.minecraft.world.item.Items.STONE_PICKAXE, net.minecraft.world.item.Items.WOODEN_PICKAXE, net.minecraft.world.item.Items.COPPER_PICKAXE,
                net.minecraft.world.item.Items.NETHERITE_SHOVEL, net.minecraft.world.item.Items.DIAMOND_SHOVEL, net.minecraft.world.item.Items.IRON_SHOVEL, net.minecraft.world.item.Items.GOLDEN_SHOVEL, net.minecraft.world.item.Items.STONE_SHOVEL, net.minecraft.world.item.Items.WOODEN_SHOVEL, net.minecraft.world.item.Items.COPPER_SHOVEL,
                net.minecraft.world.item.Items.NETHERITE_HOE, net.minecraft.world.item.Items.DIAMOND_HOE, net.minecraft.world.item.Items.IRON_HOE, net.minecraft.world.item.Items.GOLDEN_HOE, net.minecraft.world.item.Items.STONE_HOE, net.minecraft.world.item.Items.WOODEN_HOE, net.minecraft.world.item.Items.COPPER_HOE,
                net.minecraft.world.item.Items.BOW, net.minecraft.world.item.Items.CROSSBOW, net.minecraft.world.item.Items.TRIDENT, net.minecraft.world.item.Items.SHIELD, net.minecraft.world.item.Items.MACE,
                net.minecraft.world.item.Items.WOODEN_SWORD, net.minecraft.world.item.Items.STONE_SWORD, net.minecraft.world.item.Items.IRON_SWORD, net.minecraft.world.item.Items.DIAMOND_SWORD, net.minecraft.world.item.Items.NETHERITE_SWORD, net.minecraft.world.item.Items.GOLDEN_SWORD,
                net.minecraft.world.item.Items.WOODEN_AXE, net.minecraft.world.item.Items.STONE_AXE, net.minecraft.world.item.Items.IRON_AXE, net.minecraft.world.item.Items.DIAMOND_AXE, net.minecraft.world.item.Items.NETHERITE_AXE, net.minecraft.world.item.Items.GOLDEN_AXE
        ), false)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new StayNearChestGoal(golem, 1.2D, 32.0F)));
        golem.getGoalSelector().addGoal(7, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
        golem.getTargetSelector().addGoal(1, new HurtByTargetGoal(golem).setAlertOthers());
        golem.getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(golem, Monster.class, true));
    }
    public static void initAncientGoals(UtilityGolem golem) {
        initNetheriteGoals(golem);
    }
    public static void initFurnaceGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL, net.minecraft.world.item.Items.BLAZE_ROD, net.minecraft.world.item.Items.LAVA_BUCKET), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initSmokerGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL, net.minecraft.world.item.Items.BLAZE_ROD, net.minecraft.world.item.Items.LAVA_BUCKET), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initBlastFurnaceGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL, net.minecraft.world.item.Items.BLAZE_ROD, net.minecraft.world.item.Items.LAVA_BUCKET), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initBambooGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.NETHERITE_HOE, net.minecraft.world.item.Items.DIAMOND_HOE, net.minecraft.world.item.Items.IRON_HOE, net.minecraft.world.item.Items.GOLDEN_HOE, net.minecraft.world.item.Items.STONE_HOE, net.minecraft.world.item.Items.WOODEN_HOE, net.minecraft.world.item.Items.COPPER_HOE,
                net.minecraft.world.item.Items.WHEAT_SEEDS, net.minecraft.world.item.Items.CARROT, net.minecraft.world.item.Items.POTATO, net.minecraft.world.item.Items.BEETROOT_SEEDS, net.minecraft.world.item.Items.WATER_BUCKET, net.minecraft.world.item.Items.BUCKET
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new FarmGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new RefillBucketGoal(golem)));
        golem.getGoalSelector().addGoal(7, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initDiamondGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.DIAMOND), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new PlaceBlockGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initSpongeGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(net.minecraft.world.item.Items.FISHING_ROD), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new FishGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initDeepslateGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.NETHERITE_AXE, net.minecraft.world.item.Items.DIAMOND_AXE, net.minecraft.world.item.Items.IRON_AXE, net.minecraft.world.item.Items.GOLDEN_AXE, net.minecraft.world.item.Items.STONE_AXE, net.minecraft.world.item.Items.WOODEN_AXE, net.minecraft.world.item.Items.COPPER_AXE, net.minecraft.world.item.Items.SHEARS
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new ChopTreeGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new ReplantSaplingGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(7, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initJukeboxGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.MUSIC_DISC_13, net.minecraft.world.item.Items.MUSIC_DISC_CAT, net.minecraft.world.item.Items.MUSIC_DISC_BLOCKS, net.minecraft.world.item.Items.MUSIC_DISC_CHIRP, net.minecraft.world.item.Items.MUSIC_DISC_FAR,
                net.minecraft.world.item.Items.MUSIC_DISC_MALL, net.minecraft.world.item.Items.MUSIC_DISC_MELLOHI, net.minecraft.world.item.Items.MUSIC_DISC_STAL, net.minecraft.world.item.Items.MUSIC_DISC_STRAD, net.minecraft.world.item.Items.MUSIC_DISC_WARD,
                net.minecraft.world.item.Items.MUSIC_DISC_11, net.minecraft.world.item.Items.MUSIC_DISC_WAIT, net.minecraft.world.item.Items.MUSIC_DISC_OTHERSIDE, net.minecraft.world.item.Items.MUSIC_DISC_5, net.minecraft.world.item.Items.MUSIC_DISC_PIGSTEP,
                net.minecraft.world.item.Items.MUSIC_DISC_CREATOR_MUSIC_BOX, net.minecraft.world.item.Items.MUSIC_DISC_CREATOR, net.minecraft.world.item.Items.MUSIC_DISC_PRECIPICE
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new PlayRecordGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initLampGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new FollowGolemGoal(golem, GolemType.LAPIS, 1.2D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.TORCH, net.minecraft.world.item.Items.SOUL_TORCH, net.minecraft.world.item.Items.REDSTONE_TORCH, net.minecraft.world.item.Items.COPPER_TORCH, net.minecraft.world.item.Items.LANTERN, net.minecraft.world.item.Items.SOUL_LANTERN
        ), false)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new PlaceTorchGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initNetherWartGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.GLASS_BOTTLE, net.minecraft.world.item.Items.NETHER_WART
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new PlaceBrewingStandGoal(golem)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new FillBottleGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new BrewingGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }
    public static void initMedicGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                UGItems.WRENCH_ITEM
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new HealGolemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new HoldWrenchGoal(golem)));
    }

    public static void initCactusGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                UGItems.WRENCH_ITEM
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new DeleteBlacklistedItemsGoal(golem, 1.1D, 16)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }

    public static void initHoneycombGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.GLASS_BOTTLE, net.minecraft.world.item.Items.SHEARS
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new HoneyBabysitterGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().addGoal(6, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }

    public static void initHopperGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                UGItems.WRENCH_ITEM
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new DepositInHopperChestGoal(golem, 1.1D)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new CollectItemsFromInventoriesGoal(golem, 1.1D, 16)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().addGoal(5, new DebugGoalWrapper(golem, new ReturnToChestGoal(golem)));
    }

    public static void initTintedGlassGoals(UtilityGolem golem) {
        golem.getGoalSelector().addGoal(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.of(
                net.minecraft.world.item.Items.GLASS_BOTTLE
        ), false)));
        golem.getGoalSelector().addGoal(2, new DebugGoalWrapper(golem, new CollectXPGoal(golem, 1.2D, 12)));
        golem.getGoalSelector().addGoal(3, new DebugGoalWrapper(golem, new BottleXPGoal(golem)));
        golem.getGoalSelector().addGoal(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
    }

    public static class CollectXPGoal extends Goal {
        private final UtilityGolem golem;
        private final double speed;
        private final int range;
        private ExperienceOrb targetOrb;

        public CollectXPGoal(UtilityGolem golem, double speed, int range) {
            this.golem = golem;
            this.speed = speed;
            this.range = range;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.TINTED_GLASS) return false;
            targetOrb = findNearbyXPOrb();
            return targetOrb != null;
        }

        @Override
        public boolean canContinueToUse() {
            return targetOrb != null && targetOrb.isAlive() && golem.distanceToSqr(targetOrb) < range * range;
        }

        @Override
        public void start() {
            if (targetOrb != null) {
                golem.getNavigation().moveTo(targetOrb, speed);
            }
            updateHeldItem();
        }

        private void updateHeldItem() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) {
                    golem.setHeldItem(stack.copyWithCount(1));
                    return;
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void stop() {
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void tick() {
            if (targetOrb == null || !targetOrb.isAlive()) {
                targetOrb = findNearbyXPOrb();
                if (targetOrb != null) {
                    golem.getNavigation().moveTo(targetOrb, speed);
                }
                updateHeldItem();
                return;
            }

            if (golem.distanceToSqr(targetOrb) < 4.0) { // Increased distance slightly to help collection near players
                // We'll use a fixed value of 1 XP per orb as a fallback if the method name is obfuscated or unknown
                // ExperienceOrbEntity usually gives 1-3 XP or so for small ones.
                golem.incrementXpScore(1);
                targetOrb.discard();
                targetOrb = findNearbyXPOrb();
                if (targetOrb != null) {
                    golem.getNavigation().moveTo(targetOrb, speed);
                }
                updateHeldItem();
            } else if (golem.getNavigation().isDone()) {
                golem.getNavigation().moveTo(targetOrb, speed);
            }
        }

        private ExperienceOrb findNearbyXPOrb() {
            List<ExperienceOrb> orbs = golem.level().getEntitiesOfClass(ExperienceOrb.class, golem.getBoundingBox().inflate(range), orb -> orb.isAlive());
            // In 1.21.1, XP orbs can have a target player. We should still target them.
            return orbs.stream().min(Comparator.comparingDouble(golem::distanceToSqr)).orElse(null);
        }
    }

    public static class BottleXPGoal extends Goal {
        private final UtilityGolem golem;
        private int bottleCooldown = 0;

        public BottleXPGoal(UtilityGolem golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.TINTED_GLASS) return false;
            return golem.getXpScore() >= 7 && hasGlassBottle();
        }

        private boolean hasGlassBottle() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(net.minecraft.world.item.Items.GLASS_BOTTLE)) return true;
            }
            return false;
        }

        @Override
        public void tick() {
            if (bottleCooldown > 0) {
                bottleCooldown--;
                if (bottleCooldown == 0) {
                    golem.setHeldItem(ItemStack.EMPTY);
                }
                return;
            }

            if (golem.getXpScore() >= 7) {
                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    ItemStack stack = golem.getInventory().getItem(i);
                    if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) {
                        stack.shrink(1);
                        ItemStack bottleOEnchanting = new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE);
                        if (!golem.getInventory().addItem(bottleOEnchanting).isEmpty()) {
                            net.minecraft.world.level.block.Block.popResource(golem.level(), golem.blockPosition(), bottleOEnchanting);
                        }
                        golem.incrementXpScore(-7);
                        golem.setAnimation(GolemAnimation.BREWING, 40);
                        golem.setHeldItem(bottleOEnchanting.copyWithCount(1));
                        bottleCooldown = 40;
                        golem.level().playSound(null, golem.blockPosition(), net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }

        @Override
        public void stop() {
            golem.setHeldItem(ItemStack.EMPTY);
            bottleCooldown = 0;
        }
    }

    public static class CollectItemsFromInventoriesGoal extends Goal {
        private final UtilityGolem golem;
        private final double speed;
        private final int range;
        private BlockPos targetChestPos;
        private int transferCooldown = 0;

        public CollectItemsFromInventoriesGoal(UtilityGolem golem, double speed, int range) {
            this.golem = golem;
            this.speed = speed;
            this.range = range;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.HOPPER) return false;
            if (isInventoryMostlyFull()) return false;
            targetChestPos = findChestWithTargetItems();
            return targetChestPos != null;
        }

        private boolean isInventoryMostlyFull() {
            int emptySlots = 0;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).isEmpty()) {
                    emptySlots++;
                }
            }
            // If only 2 slots left, or less than 25% empty, consider it mostly full
            return emptySlots < 2;
        }

        private boolean isInventoryFull() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return targetChestPos != null && !isInventoryFull() && golem.level().getBlockEntity(targetChestPos) instanceof Container;
        }

        @Override
        public void start() {
            if (targetChestPos != null) {
                golem.getNavigation().moveTo(targetChestPos.getX(), targetChestPos.getY(), targetChestPos.getZ(), speed);
            }
            updateHeldItem();
        }

        private void updateHeldItem() {
            Container inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getCount() > 1) {
                    ItemStack held = stack.copy();
                    held.setCount(1);
                    golem.setHeldItem(held);
                    return;
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void tick() {
            if (targetChestPos == null) return;

            double dx = golem.getX() - (targetChestPos.getX() + 0.5);
            double dy = Math.abs(golem.getY() - (targetChestPos.getY() + 0.5));
            double dz = golem.getZ() - (targetChestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;

            if (horizontalDistSq < 4.0 && dy < 8.0 && canSee(targetChestPos)) {
                if (transferCooldown <= 0) {
                    golem.setAnimation(GolemAnimation.WITHDRAWING, 60);
                    transferCooldown = 60;
                } else if (transferCooldown == 1) {
                    collectItemsFromChest(targetChestPos);
                    updateHeldItem();
                    transferCooldown = 0;
                    targetChestPos = findChestWithTargetItems();
                    if (targetChestPos != null) {
                        golem.getNavigation().moveTo(targetChestPos.getX() + 0.5, targetChestPos.getY(), targetChestPos.getZ() + 0.5, speed);
                    }
                } else {
                    transferCooldown--;
                }
            } else {
                golem.getNavigation().moveTo(targetChestPos.getX() + 0.5, targetChestPos.getY(), targetChestPos.getZ() + 0.5, speed);
                transferCooldown = 0;
            }
        }

        private boolean canSee(BlockPos pos) {
            Vec3 start = golem.getEyePosition();
            Vec3 end = net.minecraft.world.phys.Vec3.atCenterOf(pos);
            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.VISUAL,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    golem
            );
            net.minecraft.world.phys.BlockHitResult result = golem.level().clip(context);
            return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS || result.getBlockPos().equals(pos);
        }

        private BlockPos findChestWithTargetItems() {
            BlockPos pos = golem.blockPosition();
            BlockPos chestPos = golem.getChestPos();
            
            for (int x = -range; x <= range; x++) {
                for (int y = -range / 2; y <= range / 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        if (checkPos.equals(chestPos)) continue;
                        
                        // Blacklist adjacent blocks to the golem's chest
                        if (chestPos != null) {
                            if (isAdjacent(checkPos, chestPos)) continue;
                            
                            // InteractionHandle double chests
                            BlockState chestState = golem.level().getBlockState(chestPos);
                            if (chestState.getBlock() instanceof GolemChestBlock) {
                                net.minecraft.world.level.block.state.properties.ChestType chestType = chestState.getValue(GolemChestBlock.CHEST_TYPE);
                                if (chestType != net.minecraft.world.level.block.state.properties.ChestType.SINGLE) {
                                    BlockPos otherChestPos = chestPos.relative(GolemChestBlock.getFacing(chestState));
                                    if (isAdjacent(checkPos, otherChestPos) || checkPos.equals(otherChestPos)) continue;
                                }
                            }
                        }

                        BlockEntity be = golem.level().getBlockEntity(checkPos);
                        if (be instanceof Container inv && !(be instanceof GolemChestBlockEntity)) {
                            if (hasTargetItems(inv)) return checkPos;
                        }
                    }
                }
            }
            return null;
        }

        private boolean isAdjacent(BlockPos pos1, BlockPos pos2) {
            return pos1.distManhattan(pos2) == 1;
        }

        private boolean hasTargetItems(Container inv) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && matchesFilter(stack)) return true;
            }
            return false;
        }

        private boolean matchesFilter(ItemStack stack) {
            Container filterInv = golem.getInventory();
            boolean anyFilter = false;
            for (int i = 0; i < filterInv.getContainerSize(); i++) {
                ItemStack filterStack = filterInv.getItem(i);
                if (!filterStack.isEmpty()) {
                    anyFilter = true;
                    if (golem.getGolemType() == GolemType.HOPPER) {
                        if (ItemStack.isSameItemSameComponents(stack, filterStack)) return true;
                    } else {
                        if (ItemStack.isSameItemSameComponents(stack, filterStack)) return true;
                    }
                }
            }
            return false; // If no filter, don't collect anything (Hopper Golem fix)
        }

        private void collectItemsFromChest(BlockPos pos) {
            BlockEntity be = golem.level().getBlockEntity(pos);
            if (be instanceof Container inv) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && matchesFilter(stack)) {
                        // For Hopper Golem, don't collect if it would create a duplicate slot
                        if (golem.getGolemType() == GolemType.HOPPER && isAlreadyInInventory(stack)) {
                            // Only allow if we can merge into an existing stack
                            if (!canMergeIntoExistingStack(stack)) {
                                continue;
                            }
                        }
                        
                        ItemStack remaining = transferStackToGolem(stack);
                        inv.setItem(i, remaining);
                        if (isInventoryFull()) break;
                    }
                }
            }
        }

        private boolean isAlreadyInInventory(ItemStack stack) {
            Container golemInv = golem.getInventory();
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (!golemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, golemStack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean canMergeIntoExistingStack(ItemStack stack) {
            Container golemInv = golem.getInventory();
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (!golemStack.isEmpty()) {
                    boolean match;
                    if (golem.getGolemType() == GolemType.HOPPER) {
                        match = ItemStack.isSameItemSameComponents(stack, golemStack);
                    } else {
                        match = ItemStack.isSameItemSameComponents(stack, golemStack);
                    }
                    if (match && golemStack.getCount() < golemStack.getMaxStackSize()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private ItemStack transferStackToGolem(ItemStack stack) {
            Container golemInv = golem.getInventory();
            // First try to merge into existing stacks
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (!golemStack.isEmpty()) {
                    boolean match;
                    if (golem.getGolemType() == GolemType.HOPPER) {
                        match = ItemStack.isSameItemSameComponents(stack, golemStack);
                    } else {
                        match = ItemStack.isSameItemSameComponents(stack, golemStack);
                    }

                    if (match) {
                        int transferAmount = Math.min(stack.getCount(), golemStack.getMaxStackSize() - golemStack.getCount());
                        if (transferAmount > 0) {
                            golemStack.grow(transferAmount);
                            stack.shrink(transferAmount);
                        }
                        // Hopper Golem: Once we've found a match, we don't want to create another stack
                        if (golem.getGolemType() == GolemType.HOPPER) {
                            return stack;
                        }
                    }
                }
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
            
            // For Hopper Golem, if we're here it means we didn't find a matching stack.
            // Check if this item is already in ANY slot (though the loop above should have found it).
            // But we also need to ensure we don't add it if it's already there but full.
            if (golem.getGolemType() == GolemType.HOPPER && isAlreadyInInventory(stack)) {
                return stack;
            }

            // Then try to put into empty slots
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (golemStack.isEmpty()) {
                    golemInv.setItem(i, stack.copy());
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        }
    }

    public static class DepositInHopperChestGoal extends Goal {
        private final UtilityGolem golem;
        private final double speed;
        private int transferCooldown = 0;

        public DepositInHopperChestGoal(UtilityGolem golem, double speed) {
            this.golem = golem;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.HOPPER) return false;
            return (hasItemsToDeposit() || isInventoryNearlyFull()) && golem.getChestPos() != null;
        }

        private boolean isInventoryNearlyFull() {
            int emptySlots = 0;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).isEmpty()) {
                    emptySlots++;
                }
            }
            return emptySlots < 4; // Start depositing if 3 or fewer slots are empty
        }

        private boolean hasItemsToDeposit() {
            Container inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                // Also deposit if we have any item that isn't the filter (but Hopper Golem currently only collects filter items)
                // The main thing is to deposit if we have more than 1 of something OR if we are getting full
                if (!stack.isEmpty() && stack.getCount() > 1) return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return (hasItemsToDeposit() || isInventoryNearlyFull()) && golem.getChestPos() != null;
        }

        @Override
        public void start() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos != null) {
                golem.getNavigation().moveTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), speed);
            }
            updateHeldItem();
        }

        private void updateHeldItem() {
            Container inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getCount() > 1) {
                    ItemStack held = stack.copy();
                    held.setCount(1);
                    golem.setHeldItem(held);
                    return;
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void tick() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return;

            double dx = golem.getX() - (chestPos.getX() + 0.5);
            double dy = Math.abs(golem.getY() - (chestPos.getY() + 0.5));
            double dz = golem.getZ() - (chestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;

            if (horizontalDistSq < 4.0 && dy < 8.0 && canSee(chestPos)) {
                if (transferCooldown <= 0) {
                    golem.setAnimation(GolemAnimation.DEPOSITING, 60);
                    transferCooldown = 60;
                } else if (transferCooldown == 1) {
                    depositItems();
                    updateHeldItem();
                    transferCooldown = 0;
                } else {
                    transferCooldown--;
                }
            } else {
                golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, speed);
                transferCooldown = 0;
            }
        }

        private boolean canSee(BlockPos pos) {
            Vec3 start = golem.getEyePosition();
            Vec3 end = net.minecraft.world.phys.Vec3.atCenterOf(pos);
            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.VISUAL,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    golem
            );
            net.minecraft.world.phys.BlockHitResult result = golem.level().clip(context);
            return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS || result.getBlockPos().equals(pos);
        }

        private void depositItems() {
            BlockPos chestPos = golem.getChestPos();
            BlockEntity be = golem.level().getBlockEntity(chestPos);
            if (be instanceof Container inv) {
                Container golemInv = golem.getInventory();
                for (int i = 0; i < golemInv.getContainerSize(); i++) {
                    ItemStack stack = golemInv.getItem(i);
                    if (!stack.isEmpty() && stack.getCount() > 1) {
                        ItemStack toDeposit = stack.copy();
                        toDeposit.setCount(stack.getCount() - 1);
                        ItemStack remaining = transferStack(toDeposit, inv);
                        stack.setCount(remaining.getCount() + 1);
                        golemInv.setItem(i, stack);
                    }
                }
            }
        }

        private ItemStack transferStack(ItemStack stack, Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack containerStack = container.getItem(i);
                if (containerStack.isEmpty()) {
                    container.setItem(i, stack.copy());
                    return ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameComponents(stack, containerStack)) {
                    int transferAmount = Math.min(stack.getCount(), containerStack.getMaxStackSize() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.grow(transferAmount);
                        stack.shrink(transferAmount);
                    }
                }
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
            return stack;
        }
    }
    
    // DEBUG WRAPPER
    public static class DebugGoalWrapper extends Goal {
        private final UtilityGolem golem;
        private final Goal innerGoal;
        private final String goalName;

        public DebugGoalWrapper(UtilityGolem golem, Goal innerGoal) {
            this.golem = golem;
            this.innerGoal = innerGoal;
            String name = innerGoal.getClass().getSimpleName();
            if (name.isEmpty()) {
                name = innerGoal.getClass().getName();
                if (name.contains("$")) {
                    name = name.substring(name.lastIndexOf('$') + 1);
                } else if (name.contains(".")) {
                    name = name.substring(name.lastIndexOf('.') + 1);
                }
            }
            this.goalName = name;

            this.setFlags(innerGoal.getFlags());
        }

        @Override
        public boolean canUse() {
            return innerGoal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            boolean result = innerGoal.canContinueToUse();
            if (!result) {
                golem.debugLog(goalName + " shouldContinue returned false");
            } else {
                // Extra check for Netherite/Ancient combat goals
                if (innerGoal instanceof net.minecraft.world.entity.ai.goal.MeleeAttackGoal) {
                    net.minecraft.world.entity.LivingEntity target = golem.getTarget();
                    if (target == null || target.isDeadOrDying() || target.isRemoved()) {
                        golem.debugLog(goalName + " forced stop: target invalid");
                        return false;
                    }
                }
            }
            return result;
        }

        @Override
        public boolean isInterruptable() {
            return innerGoal.isInterruptable();
        }

        @Override
        public void start() {
            golem.debugLog(goalName + " starting");
            if (innerGoal instanceof net.minecraft.world.entity.ai.goal.MeleeAttackGoal || innerGoal instanceof net.minecraft.world.entity.ai.goal.TemptGoal) {
                golem.setAnimation(GolemAnimation.ATTACKING, 20);
            }
            innerGoal.start();
        }

        @Override
        public void stop() {
            golem.debugLog(goalName + " stopping");
            golem.setDebugTarget(null);
            innerGoal.stop();
        }

        @Override
        public void tick() {
            innerGoal.tick();
            if (innerGoal instanceof net.minecraft.world.entity.ai.goal.MeleeAttackGoal || innerGoal instanceof net.minecraft.world.entity.ai.goal.TemptGoal) {
                if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                    golem.setAnimation(GolemAnimation.ATTACKING, 20);
                }
            }
            updateDebugTarget();
        }

        private void updateDebugTarget() {
            try {
                java.lang.reflect.Field[] fields = innerGoal.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(innerGoal);
                    if (value instanceof BlockPos pos) {
                        golem.setDebugTarget(pos);
                        return;
                    }
                    if (value instanceof net.minecraft.world.entity.Entity entity) {
                        golem.setDebugTarget(entity.blockPosition());
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        @Override
        public String toString() {
            return "DebugWrapper[" + innerGoal.toString() + "]";
        }
    }

    // STAY NEAR CHEST GOAL
    public static class DeleteBlacklistedItemsGoal extends Goal {
        private final UtilityGolem golem;
        private final double speed;
        private final int range;
        private BlockPos targetChest;
        private int searchCooldown;
        private int deleteCooldown;

        public DeleteBlacklistedItemsGoal(UtilityGolem golem, double speed, int range) {
            this.golem = golem;
            this.speed = speed;
            this.range = range;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (searchCooldown-- > 0) return false;
            searchCooldown = 40 + golem.getRandom().nextInt(40);

            if (golem.getInventory().isEmpty()) return false;

            targetChest = findChestWithBlacklistedItems();
            return targetChest != null;
        }

        @Override
        public boolean canContinueToUse() {
            return (targetChest != null || deleteCooldown > 0) && !golem.getInventory().isEmpty() && (targetChest == null || golem.blockPosition().distSqr(targetChest) < range * range);
        }

        @Override
        public void start() {
            if (targetChest != null) {
                golem.getNavigation().moveTo(targetChest.getX(), targetChest.getY(), targetChest.getZ(), speed);
            }
            deleteCooldown = 0;
        }

        @Override
        public void stop() {
            targetChest = null;
            deleteCooldown = 0;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetChest == null && deleteCooldown <= 0) return;

            if (deleteCooldown > 0) {
                deleteCooldown--;
                if (targetChest != null) {
                    golem.getLookControl().setLookAt(targetChest.getX() + 0.5, targetChest.getY() + 0.5, targetChest.getZ() + 0.5);
                }
                
                if (deleteCooldown == 10) {
                    // This is when we actually delete the items, after the WITHDRAWING animation finishes
                    if (targetChest != null) {
                        deleteItemsFromChest(targetChest);
                    }
                }
                
                if (deleteCooldown <= 0) {
                    targetChest = null;
                }
                return;
            }

            if (golem.blockPosition().distToLowCornerSqr(targetChest.getX() + 0.5, targetChest.getY() + 0.5, targetChest.getZ() + 0.5) < 4.0) {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetChest.getX() + 0.5, targetChest.getY() + 0.5, targetChest.getZ() + 0.5);
                
                // Start the animation sequence
                golem.setAnimation(GolemAnimation.WITHDRAWING, 40);
                deleteCooldown = 50; // 40 ticks for withdrawing, then 10 for attacking/deleting
            } else {
                golem.getNavigation().moveTo(targetChest.getX(), targetChest.getY(), targetChest.getZ(), speed);
            }
        }

        private BlockPos findChestWithBlacklistedItems() {
            BlockPos pos = golem.blockPosition();
            for (int x = -range; x <= range; x++) {
                for (int y = -range / 2; y <= range / 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        BlockEntity be = golem.level().getBlockEntity(checkPos);
                        if (be instanceof Container inv) {
                            if (hasBlacklistedItems(inv)) {
                                return checkPos;
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean hasBlacklistedItems(Container inv) {
            SimpleContainer blacklist = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                for (int j = 0; j < blacklist.getContainerSize(); j++) {
                    ItemStack blacklisted = blacklist.getItem(j);
                    if (!blacklisted.isEmpty() && stack.is(blacklisted.getItem())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void deleteItemsFromChest(BlockPos pos) {
            BlockEntity be = golem.level().getBlockEntity(pos);
            if (be instanceof Container inv) {
                SimpleContainer blacklist = golem.getInventory();
                boolean changed = false;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.isEmpty()) continue;
                    for (int j = 0; j < blacklist.getContainerSize(); j++) {
                        ItemStack blacklisted = blacklist.getItem(j);
                        if (!blacklisted.isEmpty() && stack.is(blacklisted.getItem())) {
                            int count = stack.getCount();
                            inv.setItem(i, ItemStack.EMPTY);
                            golem.incrementDeletedItemsCount(count);
                            changed = true;
                            break;
                        }
                    }
                }
                if (changed) {
                    inv.setChanged();
                    golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0f, 1.0f);
                    golem.setAnimation(GolemAnimation.ATTACKING, 10);

                    // Add some cactus/smoke particles
                    if (golem.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
                        serverLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.CACTUS.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
                    }
                }
            }
        }
    }

    public static class StayNearChestGoal extends Goal {
        private final UtilityGolem golem;
        private final double speed;
        private final float maxDistance;
        private int searchCooldown;

        public StayNearChestGoal(UtilityGolem golem, double speed, float maxDistance) {
            this.golem = golem;
            this.speed = speed;
            this.maxDistance = maxDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }

            BlockPos chestPos = golem.findNearbyChest();
            if (chestPos == null) {
                searchCooldown = 40;
                return false;
            }

            double distSq = golem.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5);
            if (distSq > maxDistance * maxDistance) {
                return true;
            }

            searchCooldown = 20;
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return false;

            double distSq = golem.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5);
            // Stop when we get within 8 blocks
            return distSq > 64;
        }

        @Override
        public void start() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos != null) {
                golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, speed);
            }
        }

        @Override
        public void stop() {
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos != null) {
                golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, speed);
            }
        }
    }

    // ADVANCED GOAL LOGIC
    public static class PlaceTorchGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int placeActionTime;
        private static final int MAX_PLACE_ACTION_TIME = 20;
        private int cooldown = 0;

        public PlaceTorchGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (golem.getGolemType() != GolemType.LAMP) return false;
            if (!golem.isLampOn()) return false;

            targetPos = findDarkSpot();
            if (targetPos != null && hasTorch()) {
                golem.debugLog("PlaceTorchGoal: Found dark spot at " + targetPos.toShortString());
                return true;
            }
            return false;
        }

        private boolean hasTorch() {
            if (isTorch(golem.getHeldItem())) return true;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (isTorch(golem.getInventory().getItem(i))) return true;
            }
            return false;
        }

        private boolean isTorch(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.TORCH) || stack.is(net.minecraft.world.item.Items.SOUL_TORCH) || stack.is(net.minecraft.world.item.Items.REDSTONE_TORCH) || stack.is(net.minecraft.world.item.Items.COPPER_TORCH);
        }

        private BlockPos findDarkSpot() {
            Level world = golem.level();
            BlockPos pos = golem.blockPosition();
            int range = 8;
            BlockPos bestPos = null;
            double bestDistSq = Double.MAX_VALUE;

            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (isDarkEnough(world, p) && canPlaceTorchAt(p)) {
                            double distSq = pos.distSqr(p);
                            if (distSq < bestDistSq) {
                                bestDistSq = distSq;
                                bestPos = p.immutable();
                            }
                        }
                    }
                }
            }
            return bestPos;
        }

        private boolean isDarkEnough(Level world, BlockPos pos) {
            // Check light level while ignoring the golem's own light block if it's nearby
            int blockLight = world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
            if (blockLight == 0) return true;
            
            // If light level is 12 or less, it might be from the golem itself
            if (blockLight <= 12) {
                // Check if this golem has an active light block
                BlockPos golemLightPos = golem.getLastLightPos();
                if (golemLightPos != null && world.getBlockState(golemLightPos).is(UGBlocks.LIGHT_BLOCK)) {
                    // This golem has a light block. We need to know if this block is the ONLY thing lighting 'pos'.
                    // Since we can't easily re-calculate light without it, we check if 'pos' is close to it.
                    // Light level 12 reaches 12 blocks, but intensity drops by 1 per block.
                    // If blockLight is exactly what we'd expect from the golem's light, and no other sources are obvious.
                    
                    double dist = Math.sqrt(pos.distSqr(golemLightPos));
                    int expectedLight = 12 - (int)Math.floor(dist);
                    if (expectedLight < 0) expectedLight = 0;
                    
                    // If the current light is exactly what we expect from the golem, 
                    // and expectedLight > 0, we can be reasonably sure that without the golem it would be 0.
                    // If blockLight > expectedLight, there's definitely another light source.
                    // If blockLight < expectedLight, something is blocking the golem's light, but still not 0.
                    if (blockLight == expectedLight && expectedLight > 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean canPlaceTorchAt(BlockPos pos) {
            Level world = golem.level();
            if (!world.getBlockState(pos).canBeReplaced()) return false;

            // Don't place torches where mining golems are likely to work
            List<UtilityGolem> miningGolems = golem.level().getEntitiesOfClass(UtilityGolem.class, new net.minecraft.world.phys.AABB(pos).inflate(5.0),
                g -> g.getGolemType() == GolemType.LAPIS || g.getGolemType() == GolemType.DEEPSLATE || g.getGolemType() == GolemType.BAMBOO);
            
            for (UtilityGolem g : miningGolems) {
                // If the golem is currently mining this block or its neighbors
                BlockPos miningTarget = g.getFarmTarget(); // Shared field for some golems, or we check goal target if possible
                if (miningTarget != null && miningTarget.distSqr(pos) < 4.0) return false;

                // For Lapis Golems specifically, don't block their staircase path
                if (g.getGolemType() == GolemType.LAPIS) {
                    BlockPos chestPos = g.getChestPos();
                    Direction miningDir = g.getMiningDirection();
                    if (chestPos != null && miningDir != null) {
                        // Check if pos is on the mining line
                        boolean onLine = (miningDir.getAxis() == net.minecraft.core.Direction.Axis.Z) ? pos.getX() == chestPos.getX() : pos.getZ() == chestPos.getZ();
                        if (onLine) return false;
                    }
                }
            }

            BlockState below = world.getBlockState(pos.below());
            return below.isFaceSturdy(world, pos.below(), Direction.UP) || below.is(net.minecraft.tags.BlockTags.FENCES);
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && hasTorch() && golem.isLampOn() && isDarkEnough(golem.level(), targetPos);
        }

        @Override
        public void start() {
            placeActionTime = 0;
            golem.setDebugTarget(targetPos);
            golem.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.2D);
            golem.setAnimation(GolemAnimation.LIGHTING, 20);
        }

        @Override
        public void stop() {
            targetPos = null;
            golem.setDebugTarget(null);
            golem.getNavigation().stop();
            cooldown = 40; // Add 2 second cooldown after finishing or being interrupted
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            golem.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);
            double distSq = golem.distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);

            if (distSq > 2.0D * 2.0D) {
                if (golem.getNavigation().isDone()) {
                    golem.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.2D);
                }
                placeActionTime = 0;
            } else {
                golem.getNavigation().stop();
                placeActionTime++;
                if (placeActionTime >= MAX_PLACE_ACTION_TIME) {
                    placeTorch();
                    targetPos = null;
                }
            }
        }

        private void placeTorch() {
            ItemStack torchStack = ItemStack.EMPTY;
            int slot = -1;

            if (isTorch(golem.getHeldItem())) {
                torchStack = golem.getHeldItem();
            } else {
                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    if (isTorch(golem.getInventory().getItem(i))) {
                        torchStack = golem.getInventory().getItem(i);
                        slot = i;
                        break;
                    }
                }
            }

            if (!torchStack.isEmpty()) {
                golem.debugLog("PlaceTorchGoal: Placing torch (" + torchStack.getHoverName().getString() + ") at " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ());
                Level world = golem.level();
                Block torchBlock = net.minecraft.world.level.block.Blocks.TORCH;
                if (torchStack.is(net.minecraft.world.item.Items.SOUL_TORCH)) torchBlock = net.minecraft.world.level.block.Blocks.SOUL_TORCH;
                else if (torchStack.is(net.minecraft.world.item.Items.REDSTONE_TORCH)) torchBlock = net.minecraft.world.level.block.Blocks.REDSTONE_TORCH;
                else if (torchStack.is(net.minecraft.world.item.Items.COPPER_TORCH)) torchBlock = net.minecraft.world.level.block.Blocks.COPPER_TORCH;

                if (world.getBlockState(targetPos).canBeReplaced()) {
                    world.setBlock(targetPos, torchBlock.defaultBlockState(), 3);
                    world.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    torchStack.shrink(1);
                    if (torchStack.isEmpty() && slot != -1) {
                        golem.getInventory().setItem(slot, ItemStack.EMPTY);
                    }
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
        }
    }
    public static class ClimbLadderGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos ladderPos;

        public ClimbLadderGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            ladderPos = findNearbyLadder();
            if (ladderPos == null) return false;

            // If we are already on a ladder, we can start/continue
            if (golem.level().getBlockState(golem.blockPosition()).is(net.minecraft.tags.BlockTags.CLIMBABLE)) {
                return true;
            }

            // Otherwise, only start if we have a target above us
            BlockPos target = getTargetPos();
            if (target != null && target.getY() > golem.getY()) return true;

            // Or if we are already moving and stuck vertically near a ladder
            if (!golem.getNavigation().isDone() && target != null) {
                double dy = target.getY() - golem.getY();
                if (dy > 1.0) return true;
            }

            return false;
        }

        private BlockPos getTargetPos() {
            if (golem.getNavigation().getPath() != null) {
                return golem.getNavigation().getPath().getTarget();
            }
            return null;
        }

        private BlockPos findNearbyLadder() {
            BlockPos pos = golem.blockPosition();
            // Check current block and immediate neighbors
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
                if (golem.level().getBlockState(p).is(net.minecraft.tags.BlockTags.CLIMBABLE)) {
                    return p.immutable();
                }
            }
            return null;
        }

        @Override
        public boolean canContinueToUse() {
            if (ladderPos == null) return false;

            // Check if we are still on or near a climbable block
            boolean onClimbable = false;
            BlockPos pos = golem.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
                if (golem.level().getBlockState(p).is(net.minecraft.tags.BlockTags.CLIMBABLE)) {
                    onClimbable = true;
                    ladderPos = p.immutable();
                    break;
                }
            }

            if (!onClimbable) return false;

            // If we've reached the target height or target moved below us, we can stop
            BlockPos target = getTargetPos();
            if (target != null && target.getY() <= golem.getY()) {
                return false;
            }

            return true;
        }

        @Override
        public void start() {
            golem.getNavigation().stop();
        }

        @Override
        public void stop() {
            ladderPos = null;
            golem.setDeltaMovement(golem.getDeltaMovement().x, 0.0, golem.getDeltaMovement().z);
        }

        @Override
        public void tick() {
            if (ladderPos == null) return;

            BlockPos target = getTargetPos();

            // Move towards ladder horizontal center
            double centerX = ladderPos.getX() + 0.5;
            double centerZ = ladderPos.getZ() + 0.5;

            if (golem.distanceToSqr(centerX, golem.getY(), centerZ) > 0.05) {
                golem.getNavigation().moveTo(centerX, golem.getY(), centerZ, 1.0);
            } else {
                golem.getNavigation().stop();
                // Snap to center to avoid sliding off
                golem.snapTo(centerX, golem.getY(), centerZ, golem.getYRot(), golem.getXRot());
            }

            // If we are close enough to the ladder horizontally, climb
            if (Math.abs(golem.getX() - centerX) < 0.5 && Math.abs(golem.getZ() - centerZ) < 0.5) {
                if (target == null || target.getY() > golem.getY()) {
                    // Only apply upward velocity if there is a climbable block at our feet or slightly above
                    BlockPos currentPos = golem.blockPosition();
                    if (golem.level().getBlockState(currentPos).is(net.minecraft.tags.BlockTags.CLIMBABLE) ||
                            golem.level().getBlockState(currentPos.above()).is(net.minecraft.tags.BlockTags.CLIMBABLE)) {
                        golem.setDeltaMovement(golem.getDeltaMovement().x, 0.2, golem.getDeltaMovement().z);
                    } else {
                        // We are at the top or no longer on a ladder
                        golem.setDeltaMovement(golem.getDeltaMovement().x, 0.0, golem.getDeltaMovement().z);
                    }
                }
            }
        }
    }
    public static class FollowGolemGoal extends Goal {
        private final UtilityGolem golem;
        private final GolemType targetType;
        private UtilityGolem targetGolem;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;

        public FollowGolemGoal(UtilityGolem golem, GolemType targetType, double speed, float minDistance, float maxDistance) {
            this.golem = golem;
            this.targetType = targetType;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            List<UtilityGolem> golems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(maxDistance), g -> g.getGolemType() == targetType && g.isAlive());
            if (golems.isEmpty()) return false;

            // Find closest golem of target type
            targetGolem = golems.stream()
                    .min(Comparator.comparingDouble(g -> g.distanceToSqr(golem.position())))
                    .orElse(null);

            return targetGolem != null && golem.distanceToSqr(targetGolem) > (double)(minDistance * minDistance);
        }

        @Override
        public boolean canContinueToUse() {
            return targetGolem != null && targetGolem.isAlive() && golem.distanceToSqr(targetGolem) < (double)(maxDistance * maxDistance * 2);
        }

        @Override
        public void start() {
            golem.clearBlacklist();
        }

        @Override
        public void stop() {
            targetGolem = null;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetGolem == null) return;
            golem.getLookControl().setLookAt(targetGolem, 30.0F, 30.0F);
            
            double distSq = golem.distanceToSqr(targetGolem);
            if (distSq > 144.0D) { // Teleport if more than 12 blocks away
                teleportToTarget();
            } else if (distSq > (double)(minDistance * minDistance)) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetGolem, speed);
                }
            }
        }

        private void teleportToTarget() {
            Level world = golem.level();
            for (int i = 0; i < 10; ++i) {
                int x = (int)targetGolem.getX() + golem.getRandom().nextInt(3) - 1;
                int y = (int)targetGolem.getY();
                int z = (int)targetGolem.getZ() + golem.getRandom().nextInt(3) - 1;
                
                if (isSafe(new BlockPos(x, y, z))) {
                    golem.teleportTo(x + 0.5, y, z + 0.5);
                    golem.getNavigation().stop();
                    return;
                }
            }
            // Fallback: exact target position
            golem.teleportTo(targetGolem.getX(), targetGolem.getY(), targetGolem.getZ());
            golem.getNavigation().stop();
        }

        private boolean isSafe(BlockPos pos) {
            Level world = golem.level();
            return world.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.AIR) && world.getBlockState(pos.above()).is(net.minecraft.world.level.block.Blocks.AIR) && !world.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.AIR);
        }
    }

    public static class FollowPlayerGoal extends Goal {
        private final UtilityGolem golem;
        private Player targetPlayer;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;

        public FollowPlayerGoal(UtilityGolem golem, double speed, float minDistance, float maxDistance) {
            this.golem = golem;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() == GolemType.LAMP && !golem.isLampOn()) {
                return false;
            }

            // Check if we should even try following a player.
            // For lamp golems, if we're following a Lapis Golem that's alive, we don't start following the player.
            // But if the Lapis Golem is gone, we should start following the player.
            if (golem.getGolemType() == GolemType.LAMP) {
                List<UtilityGolem> lapisGolems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(16.0), g -> g.getGolemType() == GolemType.LAPIS && g.isAlive());
                if (!lapisGolems.isEmpty()) {
                    return false;
                }
            }

            float searchRange = maxDistance;
            if (golem.getGolemType() == GolemType.LAMP && golem.isLampOn()) {
                searchRange = 128.0F; // Large search range for lamp golems
            }

            List<Player> players = golem.level().getEntitiesOfClass(Player.class, golem.getBoundingBox().inflate(searchRange), player -> true);
            if (players.isEmpty()) return false;
            
            // Find closest player
            targetPlayer = players.stream()
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(golem.position())))
                .orElse(null);
                
            return targetPlayer != null && golem.distanceToSqr(targetPlayer) > (double)(minDistance * minDistance);
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.getGolemType() == GolemType.LAMP) {
                if (!golem.isLampOn()) return false;
                
                // If a Lapis Golem is nearby, stop following the player
                List<UtilityGolem> lapisGolems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(16.0), g -> g.getGolemType() == GolemType.LAPIS && g.isAlive());
                if (!lapisGolems.isEmpty()) {
                    return false;
                }
                
                return targetPlayer != null && targetPlayer.isAlive() && targetPlayer.level() == golem.level();
            }
            return targetPlayer != null && targetPlayer.isAlive() && golem.distanceToSqr(targetPlayer) < (double)(maxDistance * maxDistance * 2);
        }

        @Override
        public void start() {
            golem.clearBlacklist();
        }

        @Override
        public void stop() {
            targetPlayer = null;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetPlayer == null) return;
            golem.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
            if (golem.distanceToSqr(targetPlayer) > (double)(minDistance * minDistance)) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetPlayer, speed);
                }
            }
        }
    }
    public static class TradeWithVillagerGoal extends Goal {
        private final UtilityGolem golem;
        private Villager targetVillager;
        private int tradeDelay;

        public TradeWithVillagerGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            targetVillager = findVillagerWithTrade();
            // Record trades periodically to keep the "buy list" updated
            if (targetVillager == null || golem.getRandom().nextInt(20) == 0) {
                recordNearbyTrades();
            }
            return targetVillager != null;
        }

        private void recordNearbyTrades() {
            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), villager -> true);
            for (Villager villager : villagers) {
                MerchantOffers offers = villager.getOffers();
                for (MerchantOffer offer : offers) {
                    if (offer.isOutOfStock()) continue;
                    if (!offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                        golem.addDiscoveredTrade(offer.getResult());
                    }
                }
            }
        }

        private Villager findVillagerWithTrade() {
            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), villager -> true);
            for (Villager villager : villagers) {
                if (canTradeWith(villager)) {
                    return villager;
                }
            }
            return null;
        }

        private boolean canTradeWith(Villager villager) {
            MerchantOffers offers = villager.getOffers();
            SimpleContainer inventory = golem.getInventory();
            ItemStack selectedBuy = golem.getSelectedBuyItem();

            for (MerchantOffer offer : offers) {
                if (offer.isOutOfStock()) continue;
                
                // Existing selling logic
                if (offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                    ItemCost buyItem1 = offer.getItemCostA();
                    Optional<ItemCost> buyItem2 = offer.getItemCostB();

                    if (hasStack(golem.getInventory(), buyItem1) && (buyItem2.isEmpty() || hasStack(golem.getInventory(), buyItem2.get()))) {
                        return true;
                    }
                }
                
                // New buying logic
                if (!selectedBuy.isEmpty() && ItemStack.isSameItemSameComponents(offer.getResult(), selectedBuy)) {
                    ItemCost buyItem1 = offer.getItemCostA();
                    Optional<ItemCost> buyItem2 = offer.getItemCostB();

                    if (hasStack(golem.getInventory(), buyItem1) && (buyItem2.isEmpty() || hasStack(golem.getInventory(), buyItem2.get()))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasStack(Container inventory, ItemCost target) {
            int count = 0;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack invStack = inventory.getItem(i);
                if (target.test(invStack)) {
                    count += invStack.getCount();
                }
            }
            return count >= target.count();
        }

        @Override
        public boolean canContinueToUse() {
            if (targetVillager == null || !targetVillager.isAlive()) return false;
            
            // Re-check if the villager still has trades periodically or when they level up
            // This ensures we "lose track" correctly if they are no longer tradeable,
            // or keep tracking if they still have other valid trades.
            return canTradeWith(targetVillager);
        }

        @Override
        public void start() {
            tradeDelay = 0;
            golem.setAnimation(GolemAnimation.NODDING, 40);
        }

        @Override
        public void stop() {
            targetVillager = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            if (targetVillager == null) return;

            // Periodically check for new trades from ALL nearby villagers
            if (golem.getRandom().nextInt(100) == 0) {
                recordNearbyTrades();
                
                // If our current target no longer has valid trades, we should stop
                // so canStart can find a better target (possibly the same villager with new trades)
                if (!canTradeWith(targetVillager)) {
                    targetVillager = null;
                    golem.setAnimation(GolemAnimation.IDLE, 0);
                    golem.getNavigation().stop();
                    return;
                }
            }

            // Ensure animation is active while trading
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.TRADING, 40);
            }

            double dist = golem.distanceToSqr(targetVillager);
            if (dist > 4.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetVillager, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetVillager, 30.0F, 30.0F);

                if (++tradeDelay % 20 == 0) {
                    performTrade();
                }
            }
        }

        private void performTrade() {
            MerchantOffers offers = targetVillager.getOffers();
            SimpleContainer inventory = golem.getInventory();
            ItemStack selectedBuy = golem.getSelectedBuyItem();

            // First, record all available trades from this villager
            for (MerchantOffer offer : offers) {
                if (!offer.isOutOfStock() && !offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                    golem.addDiscoveredTrade(offer.getResult());
                }
            }

            for (MerchantOffer offer : offers) {
                if (offer.isOutOfStock()) continue;

                boolean isSellingToVillager = offer.getResult().is(net.minecraft.world.item.Items.EMERALD);
                boolean isBuyingFromVillager = !selectedBuy.isEmpty() && ItemStack.isSameItemSameComponents(offer.getResult(), selectedBuy);

                if (isSellingToVillager || isBuyingFromVillager) {
                    ItemCost buyItem1 = offer.getItemCostA();
                    Optional<ItemCost> buyItem2 = offer.getItemCostB();

                    if (hasStack(golem.getInventory(), buyItem1) && (buyItem2.isEmpty() || hasStack(golem.getInventory(), buyItem2.get()))) {
                        // Consume items
                        consumeItems(golem.getInventory(), buyItem1);
                        buyItem2.ifPresent(tradedItem -> consumeItems(golem.getInventory(), tradedItem));

                        // Add reward
                        ItemStack reward = offer.getResult().copy();
                        ItemStack remaining = inventory.addItem(reward);
                        if (!remaining.isEmpty()) {
                            golem.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(golem.level(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                        }

                        // Notify villager of trade and decrement stock/uses
                        targetVillager.notifyTrade(offer);
                        // Ensure the offer usage/stock is decremented even when not using the trading UI
                        offer.increaseUses();
                        // Let the villager process post-trade effects (restock timers, XP, sounds)
                        try {
                            targetVillager.notifyTradeUpdated(reward.copy());
                        } catch (Throwable ignored) {
                            // Some mappings may not expose onSellingItem on VillagerEntity; ignore if unavailable
                        }
                        break;
                    }
                }
            }
        }

        private void consumeItems(Container inventory, ItemCost target) {
            int toConsume = target.count();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack invStack = inventory.getItem(i);
                if (target.test(invStack)) {
                    int amount = Math.min(toConsume, invStack.getCount());
                    invStack.shrink(amount);
                    toConsume -= amount;
                    if (toConsume <= 0) break;
                }
            }
        }
    }
    public static class FishGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos waterPos;
        private BlockPos chestPos;
        private int fishingTime;
        private int maxFishingTime;
        private int castingTicks;

        public FishGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ItemStack rod = golem.getHeldItem();
            if (rod.isEmpty() || !UtilityGolem.isFishingRod(rod)) {
                return false;
            }
            if (isInventoryFull()) {
                return false;
            }
            chestPos = findNearbyChest();
            
            waterPos = findNearbyWater(chestPos != null ? chestPos : golem.blockPosition());
            if (waterPos != null) {
                if (chestPos != null) {
                    golem.debugLog("FishGoal: Found water at " + waterPos.toShortString() + " near chest " + chestPos.toShortString());
                } else {
                    golem.debugLog("FishGoal: Found water at " + waterPos.toShortString() + " near golem");
                }
                return true;
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private BlockPos findNearbyWater(BlockPos center) {
            Level world = golem.level();
            
            // Collect all water positions currently being fished by other golems
            List<BlockPos> occupiedWater = new ArrayList<>();
            List<UtilityGolem> golems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(32.0), g -> g != golem && g.getGolemType() == GolemType.SPONGE);
            for (UtilityGolem other : golems) {
                BlockPos target = other.getFishingTarget();
                if (target != null) {
                    occupiedWater.add(target);
                }
            }

            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = center.offset(x, y, z);
                        
                        // Skip if this water block is already being used
                        if (occupiedWater.contains(p)) continue;

                        BlockState state = world.getBlockState(p);
                        if (!state.is(net.minecraft.world.level.block.Blocks.WATER)) continue;
                        if (!world.getBlockState(p.above()).is(net.minecraft.world.level.block.Blocks.AIR)) continue;
                        // compute score
                        double dx = p.getX() - center.getX();
                        double dz = p.getZ() - center.getZ();
                        double horizDistSq = dx * dx + dz * dz;
                        int minLandDist = 99;
                        boolean shallow = !world.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.WATER);
                        for (int lx = -4; lx <= 4; lx++) {
                            for (int lz = -4; lz <= 4; lz++) {
                                if (lx == 0 && lz == 0) continue;
                                BlockPos landPos = p.offset(lx, 0, lz);
                                BlockState ls = world.getBlockState(landPos);
                                if (ls.getFluidState().isEmpty() && !ls.is(net.minecraft.world.level.block.Blocks.AIR)) {
                                    int dist = Math.max(Math.abs(lx), Math.abs(lz));
                                    if (dist < minLandDist) {
                                        minLandDist = dist;
                                    }
                                }
                            }
                        }
                        double score = horizDistSq + minLandDist * 25.0;
                        if (shallow) score -= 10.0;
                        if (score < bestScore) {
                            bestScore = score;
                            best = p;
                        }
                    }
                }
            }
            if (best != null) {
                double dx = best.getX() - center.getX();
                double dz = best.getZ() - center.getZ();
                double horizDistSq = dx * dx + dz * dz;
                boolean shallow = !world.getBlockState(best.below()).is(net.minecraft.world.level.block.Blocks.WATER);
                int minLandDist = 99;
                for (int lx = -4; lx <= 4; lx++) {
                    for (int lz = -4; lz <= 4; lz++) {
                        if (lx == 0 && lz == 0) continue;
                        BlockPos landPos = best.offset(lx, 0, lz);
                        BlockState ls = world.getBlockState(landPos);
                        if (ls.getFluidState().isEmpty() && !ls.is(net.minecraft.world.level.block.Blocks.AIR)) {
                            int dist = Math.max(Math.abs(lx), Math.abs(lz));
                            if (dist < minLandDist) {
                                minLandDist = dist;
                            }
                        }
                    }
                }
                double score = horizDistSq + minLandDist * 25.0;
                if (shallow) score -= 10.0;
                golem.debugLog("FishGoal: Best water " + best.toShortString() + " | score: " + score + " | horizDistSq: " + horizDistSq + " | minLandDist: " + minLandDist + " | shallow: " + shallow);
            }
            return best;
        }

        @Override
        public void start() {
            fishingTime = 0;
            castingTicks = 20;
            maxFishingTime = 100 + golem.getRandom().nextInt(200); // 5-15 seconds
            golem.setAnimation(GolemAnimation.WITHDRAWING, castingTicks);
            golem.setFishingTarget(waterPos);
        }

        @Override
        public boolean canContinueToUse() {
            ItemStack rod = golem.getHeldItem();
            BlockState chestState = chestPos != null ? golem.level().getBlockState(chestPos) : null;
            
            // Basic validity checks
            if (waterPos == null || !golem.level().getBlockState(waterPos).is(net.minecraft.world.level.block.Blocks.WATER) ||
                rod.isEmpty() || !UtilityGolem.isFishingRod(rod) ||
                isInventoryFull()) {
                return false;
            }

            if (chestPos != null) {
                if (chestState == null || chestState.getBlock() != golem.getGolemType().getChestBlock()) {
                    return false;
                }
                if (golem.blockPosition().distToLowCornerSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) >= 1024) {
                    return false;
                }
            }

            // Distance checks
            if (golem.blockPosition().distToLowCornerSqr(waterPos.getX(), waterPos.getY(), waterPos.getZ()) >= 400) {
                return false;
            }

            return fishingTime < maxFishingTime;
        }

        private boolean isInventoryFull() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public void stop() {
            waterPos = null;
            chestPos = null;
            golem.setFishingTarget(null);
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            if (waterPos == null) return;

            if (castingTicks > 0) {
                castingTicks--;
                golem.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);
                golem.setFishingTarget(waterPos);
                if (castingTicks == 0) {
                    golem.setAnimation(GolemAnimation.FISHING, maxFishingTime);
                }
                return;
            }

            double dx = golem.getX() - (waterPos.getX() + 0.5);
            double dy = golem.getY() - (waterPos.getY() + 0.5);
            double dz = golem.getZ() - (waterPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 5.0D || verticalDist > 15.0D) {
                // If it's far vertically, target our current height
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().moveTo(waterPos.getX(), golem.getY(), waterPos.getZ(), 1.1D);
                    } else {
                        golem.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.1D);
                    }
                }
                fishingTime = 0;
                // Keep the target even if moving to it
                golem.setFishingTarget(waterPos);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);
                golem.setFishingTarget(waterPos);

                if (fishingTime % 20 == 0) {
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }

                fishingTime++;

                if (fishingTime >= maxFishingTime) {
                    catchFish();
                }
            }
        }

        private void catchFish() {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;

            // Simplified fishing loot - 85% fish, 10% junk, 5% treasure
            ItemStack loot;
            int chance = golem.getRandom().nextInt(100);
            if (chance < 85) {
                int fishType = golem.getRandom().nextInt(4);
                loot = switch (fishType) {
                    case 1 -> new ItemStack(net.minecraft.world.item.Items.SALMON);
                    case 2 -> new ItemStack(net.minecraft.world.item.Items.TROPICAL_FISH);
                    case 3 -> new ItemStack(net.minecraft.world.item.Items.PUFFERFISH);
                    default -> new ItemStack(net.minecraft.world.item.Items.COD);
                };
                golem.setAnimation(GolemAnimation.CATCHING_FISH, 20);
            } else if (chance < 95) {
                loot = new ItemStack(net.minecraft.world.item.Items.SADDLE); // Simplified junk/treasure for now
                golem.setAnimation(GolemAnimation.CATCHING_FISH, 20);
            } else {
                loot = net.minecraft.world.item.Items.ENCHANTED_BOOK.getDefaultInstance();
                if (serverLevel.registryAccess() != null) {
                    var enchantmentRegistry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                    var optionalEnchantment = enchantmentRegistry.getRandom(golem.getRandom());
                    if (optionalEnchantment.isPresent()) {
                        loot.enchant(optionalEnchantment.get(), net.minecraft.util.Mth.nextInt(golem.getRandom(), 1, 3));
                    }
                }
                golem.setAnimation(GolemAnimation.CATCHING_RARE_FISH, 20);
            }

            ItemStack remaining = golem.getInventory().addItem(loot);
            golem.debugLog("FishGoal: Caught " + loot.getItem().getDescriptionId() + "!");
            if (!remaining.isEmpty()) {
                Block.popResource(serverLevel, golem.blockPosition(), remaining);
            }

            // Damage the fishing rod
            ItemStack rod = golem.getHeldItem();
            if (!rod.isEmpty() && UtilityGolem.isFishingRod(rod)) {
                // Apply Lure and Luck of the Sea if possible
                if (serverLevel.registryAccess() != null) {
                    var registry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                    int lureLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LURE), rod);
                    int luckLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LUCK_OF_THE_SEA), rod);
                    
                    // Lure reduces wait time by 5 seconds (100 ticks) per level
                    maxFishingTime = Math.max(20, maxFishingTime - (lureLevel * 100));
                    
                    // Luck of the Sea increases treasure chance (simplified)
                    if (luckLevel > 0 && chance >= 85) {
                        // If we already rolled junk/treasure, make it even better
                        if (golem.getRandom().nextInt(10) < luckLevel) {
                            loot = net.minecraft.world.item.Items.ENCHANTED_BOOK.getDefaultInstance();
                            var enchantmentRegistry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                            var optionalEnchantment = enchantmentRegistry.getRandom(golem.getRandom());
                            if (optionalEnchantment.isPresent()) {
                                loot.enchant(optionalEnchantment.get(), net.minecraft.util.Mth.nextInt(golem.getRandom(), 2, 4));
                            }
                            golem.setAnimation(GolemAnimation.CATCHING_RARE_FISH, 20);
                        }
                    }
                }
                
                rod.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
            }
            
            fishingTime = 0;
            castingTicks = 20;
            maxFishingTime = 100 + golem.getRandom().nextInt(200);
            golem.setAnimation(GolemAnimation.WITHDRAWING, castingTicks);
        }
    }
    public static class PlaceBrewingStandGoal extends Goal {
        private final UtilityGolem golem;
        private int cooldown = 0;

        public PlaceBrewingStandGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (golem.getGolemType() != GolemType.NETHER_WART) return false;
            if (findBrewingStand() != -1) {
                // If we don't have a brewing stand nearby, we should place one if we have it
                BlockPos nearbyStand = findNearbyBrewingStand();
                if (nearbyStand == null) {
                    return true;
                }
            }
            return false;
        }

        private int findBrewingStand() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(net.minecraft.world.item.Items.BREWING_STAND)) return i;
            }
            return -1;
        }

        private BlockPos findNearbyBrewingStand() {
            BlockPos pos = golem.blockPosition();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.BREWING_STAND)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void tick() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) {
                // Find a nearby chest to place the stand next to
                chestPos = findNearbyChest();
            }

            if (chestPos != null) {
                BlockPos placePos = findPlacePos(chestPos);
                if (placePos != null) {
                    if (golem.blockPosition().distSqr(placePos) > 4.0D) {
                        golem.getNavigation().moveTo(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5, 1.0D);
                    } else {
                        placeStand(placePos);
                    }
                }
            }
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private BlockPos findPlacePos(BlockPos chestPos) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos p = chestPos.relative(dir);
                if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.AIR) && golem.level().getBlockState(p.below()).isRedstoneConductor(golem.level(), p.below())) {
                    return p;
                }
            }
            return chestPos.above(); // Fallback to on top of chest
        }

        private void placeStand(BlockPos pos) {
            int slot = findBrewingStand();
            if (slot != -1) {
                ItemStack stack = golem.getInventory().getItem(slot);
                golem.level().setBlock(pos, net.minecraft.world.level.block.Blocks.BREWING_STAND.defaultBlockState(), 3);
                stack.shrink(1);
                golem.getInventory().setChanged();
                golem.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                cooldown = 100;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return findBrewingStand() != -1 && findNearbyBrewingStand() == null;
        }
    }

    public static class BrewingGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int actionTimer;
        private ItemStack pendingActionStack = ItemStack.EMPTY;
        private int searchCooldown = 0;

        public BrewingGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (golem.getInventory().isEmpty() && !golem.getHeldItem().is(net.minecraft.world.item.Items.BLAZE_POWDER)) return false;
            
            targetPos = findBrewingStand();
            if (targetPos == null) {
                searchCooldown = 40 + golem.getRandom().nextInt(40); // 2-4s cooldown if none found
                return false;
            }

            // Only start brewing if we have something to do at the stand
            boolean hasWork = hasWorkAtStand();
            if (hasWork) {
                golem.debugLog("BrewingGoal: Found work at brewing stand " + targetPos.toShortString());
                return true;
            } else {
                searchCooldown = 20 + golem.getRandom().nextInt(20); // 1-2s cooldown if no work
            }
            return false;
        }

        private boolean hasWorkAtStand() {
            if (targetPos == null) return false;
            BlockPos chestPos = golem.findNearbyChest();
            if (chestPos == null) return false;
            Container chestInv = golem.getChestInventory(chestPos);
            if (chestInv == null) return false;

            BlockEntity be = golem.level().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity stand) {
                // 1. Can collect finished potions?
                for (int i = 0; i < 3; i++) {
                    if (!stand.getItem(i).isEmpty() && isFullyFinished(stand.getItem(i))) return true;
                }

                // 2. Can refill fuel?
                ItemStack fuelStack = stand.getItem(4);
                if ((fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxStackSize()) && findItemInInventory(net.minecraft.world.item.Items.BLAZE_POWDER) != -1) {
                    return true;
                }

                // Check if stand HAS any bottles/potions to brew with
                boolean standHasBottles = false;
                for (int i = 0; i < 3; i++) {
                    if (!stand.getItem(i).isEmpty()) {
                        standHasBottles = true;
                        break;
                    }
                }

                // 3. Can add water bottles?
                boolean hasWaterInInv = findWaterBottleInInventory() != -1;
                if (hasWaterInInv) {
                    for (int i = 0; i < 3; i++) {
                        if (stand.getItem(i).isEmpty()) return true;
                    }
                }

                // 4. Can add ingredient?
                if (standHasBottles && stand.getItem(3).isEmpty() && findBestIngredientForStand(stand) != -1) {
                    return true;
                }
            }
            return false;
        }

        private BlockPos findBrewingStand() {
            BlockPos pos = golem.blockPosition();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.BREWING_STAND)) {
                            // Check if any other golem is already targeting this brewing stand
                            boolean alreadyTargeted = false;
                            for (UtilityGolem other : golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(32.0D), g -> g != golem)) {
                                if (p.equals(other.getDebugTarget())) {
                                    alreadyTargeted = true;
                                    break;
                                }
                            }
                            if (!alreadyTargeted) {
                                golem.debugLog("BrewingGoal: Found available brewing stand at " + p.toShortString());
                                return p;
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void start() {
            actionTimer = 0;
            golem.setDebugTarget(targetPos);
            updateHeldItem();
            // golem.setAnimation(GolemAnimation.BREWING, 200); // Typical action duration
        }

        @Override
        public void stop() {
            targetPos = null;
            golem.setDebugTarget(null);
            golem.setHeldItem(ItemStack.EMPTY);
            pendingActionStack = ItemStack.EMPTY;
            actionTimer = 0;
            searchCooldown = 20 + golem.getRandom().nextInt(20); // Add cooldown after stopping
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        private void updateHeldItem() {
            if (targetPos == null) return;
            BlockEntity be = golem.level().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity stand) {
                // If we are at the stand and have a pending action stack, hold it
                if (!pendingActionStack.isEmpty()) {
                    golem.setHeldItem(pendingActionStack);
                    return;
                }

                // Otherwise, hold what we are about to insert
                // 1. Fuel
                ItemStack fuelStack = stand.getItem(4);
                if (fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxStackSize()) {
                    int slot = findItemInInventory(net.minecraft.world.item.Items.BLAZE_POWDER);
                    if (slot != -1) {
                        golem.setHeldItem(golem.getInventory().getItem(slot).copyWithCount(1));
                        return;
                    }
                }

                // 2. Ingredients
                if (stand.getItem(3).isEmpty()) {
                    int slot = findBestIngredientForStand(stand);
                    if (slot != -1) {
                        golem.setHeldItem(golem.getInventory().getItem(slot).copyWithCount(1));
                        return;
                    }
                }

                // 3. Water Bottles
                for (int i = 0; i < 3; i++) {
                    if (stand.getItem(i).isEmpty()) {
                        int waterSlot = findWaterBottleInInventory();
                        if (waterSlot != -1) {
                            golem.setHeldItem(golem.getInventory().getItem(waterSlot).copyWithCount(1));
                            return;
                        }
                    }
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void tick() {
            if (targetPos == null) return;
            
            // Ensure animation is active while brewing
            // if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
            //     golem.setAnimation(GolemAnimation.BREWING, 40);
            // }

            // Only search for brewing stand if the current target is no longer a brewing stand
            if (golem.getRandom().nextInt(40) == 0 && !golem.level().getBlockState(targetPos).is(net.minecraft.world.level.block.Blocks.BREWING_STAND)) {
                targetPos = findBrewingStand();
                if (targetPos == null) return;
            }

            if (golem.getNavigation().isDone() || golem.getRandom().nextInt(20) == 0) {
                golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            }
            golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

            if (golem.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) < 4.0D) {
                golem.getNavigation().stop(); // Stop moving once close enough
                actionTimer++;
                if (actionTimer >= 20) {
                    interactWithBrewingStand();
                    actionTimer = 0;
                    updateHeldItem();
                }
            } else {
                // Periodically update held item while moving
                if (golem.getRandom().nextInt(20) == 0) {
                    updateHeldItem();
                }
            }
        }

        private void interactWithBrewingStand() {
            BlockEntity be = golem.level().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity brewingStand) {
                // 1. Collect finished potions from slots 0, 1, 2
                for (int i = 0; i < 3; i++) {
                    ItemStack stack = brewingStand.getItem(i);
                    if (!stack.isEmpty() && isFullyFinished(stack)) {
                        pendingActionStack = stack.copy();
                        golem.setHeldItem(pendingActionStack);
                        
                        ItemStack remaining = golem.getInventory().addItem(stack);
                        if (!remaining.isEmpty()) {
                            // Inventory full, try to deposit in chest if nearby
                            BlockPos chestPos = golem.getChestPos();
                            if (chestPos == null) {
                                // Simple search for nearby chest if not set
                                chestPos = findNearbyChest();
                            }
                            if (chestPos != null && golem.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5) < 16.0D) {
                                Container chestInv = golem.getChestInventory(chestPos);
                                if (chestInv != null) {
                                    BlockState chestState = golem.level().getBlockState(chestPos);
                                    if (chestState.getBlock() == golem.getGolemType().getChestBlock()) {
                                        remaining = transferStackToChest(remaining, chestInv);
                                    } else if (golem.getGolemType() == GolemType.NETHER_WART && isFullyFinished(remaining)) {
                                        // Nether Wart Golems can deposit completed potions in normal chests
                                        remaining = transferStackToChest(remaining, chestInv);
                                    }
                                }
                            }
                        }
                        brewingStand.setItem(i, remaining);
                        brewingStand.setChanged();
                        pendingActionStack = ItemStack.EMPTY; // Item is now in golem.getInventory() or chest
                        return; // One action per interaction
                    }
                }

                // 2. Refill Blaze Powder fuel (slot 4)
                ItemStack fuelStack = brewingStand.getItem(4);
                if (fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxStackSize()) {
                    int slot = findItemInInventory(net.minecraft.world.item.Items.BLAZE_POWDER);
                    if (slot != -1) {
                        ItemStack powder = golem.getInventory().getItem(slot);
                        int amountToTransfer = Math.min(powder.getCount(), net.minecraft.world.item.Items.BLAZE_POWDER.getDefaultMaxStackSize() - fuelStack.getCount());
                        ItemStack toTransfer = golem.getInventory().removeItem(slot, amountToTransfer);
                        if (fuelStack.isEmpty()) {
                            brewingStand.setItem(4, toTransfer);
                        } else {
                            fuelStack.grow(amountToTransfer);
                        }
                        brewingStand.setChanged();
                        return;
                    }
                }

                // 3. Add Water Bottles to empty potion slots (0, 1, 2)
                for (int i = 0; i < 3; i++) {
                    if (brewingStand.getItem(i).isEmpty()) {
                        int waterSlot = findWaterBottleInInventory();
                        if (waterSlot != -1) {
                            ItemStack waterBottle = golem.getInventory().removeItem(waterSlot, 1);
                            brewingStand.setItem(i, waterBottle);
                            brewingStand.setChanged();
                            // Also try to fill other empty slots if we have more water bottles
                            for (int j = i + 1; j < 3; j++) {
                                if (brewingStand.getItem(j).isEmpty()) {
                                    int nextWaterSlot = findWaterBottleInInventory();
                                    if (nextWaterSlot != -1) {
                                        brewingStand.setItem(j, golem.getInventory().removeItem(nextWaterSlot, 1));
                                    }
                                }
                            }
                            return;
                        }
                    }
                }

                // 4. Add ingredient (slot 3) if empty
                if (brewingStand.getItem(3).isEmpty()) {
                    int ingredientSlot = findBestIngredientForStand(brewingStand);
                    if (ingredientSlot != -1) {
                        ItemStack ingredient = golem.getInventory().getItem(ingredientSlot);
                        brewingStand.setItem(3, golem.getInventory().removeItem(ingredientSlot, 1));
                        brewingStand.setChanged();
                        return;
                    }
                }
            }
        }

        private int findBestIngredientForStand(net.minecraft.world.level.block.entity.BrewingStandBlockEntity stand) {
            boolean hasWaterBottle = false;
            boolean hasAwkwardPotion = false;
            boolean hasRegularPotion = false;

            for (int i = 0; i < 3; i++) {
                ItemStack stack = stand.getItem(i);
                if (stack.isEmpty()) continue;
                if (isWaterBottle(stack)) hasWaterBottle = true;
                else if (isAwkwardPotion(stack)) hasAwkwardPotion = true;
                else if (isRegularPotion(stack)) hasRegularPotion = true;
            }

            // Priority 1: If there are water bottles, we NEED Nether Wart first.
            // But only if there aren't already Awkward potions in the stand.
            if (hasWaterBottle && !hasAwkwardPotion) {
                int nw = findItemInInventory(net.minecraft.world.item.Items.NETHER_WART);
                if (nw != -1) return nw;
            }

            // Priority 2: If we have awkward potions, use a primary ingredient (not nether wart, not secondary)
            if (hasAwkwardPotion) {
                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    ItemStack stack = golem.getInventory().getItem(i);
                    if (isPrimaryIngredient(stack) && !stack.is(net.minecraft.world.item.Items.NETHER_WART)) {
                        return i;
                    }
                }
            }

            // Priority 1.5: If we STILL have water bottles and no better options, then use nether wart
            if (hasWaterBottle) {
                int nw = findItemInInventory(net.minecraft.world.item.Items.NETHER_WART);
                if (nw != -1) return nw;
            }

            // Priority 3: If we have regular potions, use secondary ingredients (Gunpowder, etc.)
            if (hasRegularPotion) {
                // Check for fermented spider eye specifically for recipes like Night Vision -> Invisibility
                int fse = findItemInInventory(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE);
                if (fse != -1) return fse;

                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    ItemStack stack = golem.getInventory().getItem(i);
                    if (isSecondaryIngredient(stack)) {
                        // Check if the stand already has this ingredient or if it can be used
                        ItemStack standIngredient = stand.getItem(3);
                        if (standIngredient.isEmpty()) return i;
                    }
                }
            }

            return -1;
        }

        private boolean isWaterBottle(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER);
            }
            return false;
        }

        private boolean isAwkwardPotion(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        private boolean isRegularPotion(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                Holder<net.minecraft.world.item.alchemy.Potion> p = potion.potion().get();
                return !p.is(net.minecraft.world.item.alchemy.Potions.WATER) && !p.is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private ItemStack transferStackToChest(ItemStack stack, Container container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack containerStack = container.getItem(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxStackSize() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).isEmpty()) {
                    container.setItem(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.isSameItemSameComponents(stack, other) && other.getCount() < other.getMaxStackSize();
        }

        private boolean isFullyFinished(ItemStack stack) {
            return BrewingGoal.isFullyFinished(golem, stack);
        }

        public static boolean isFullyFinished(UtilityGolem golem, ItemStack stack) {
            // A potion is fully finished if it's a splash/lingering potion OR it has been enhanced and we have no more secondary ingredients to add
            // For simplicity, let's say if it's not a water bottle and not awkward, it's "finished" enough to be collected if we don't have secondary ingredients.
            // But the user said: "If it has gunpowder, it will make splash potions, same with glowstone / redstone."
            // So it should only collect if it CANNOT improve it further with what it has in golem.getInventory().
            
            if (!isRegularPotionStatic(stack)) return false;

            // If we have secondary ingredients, we should probably keep it in the stand to process further
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (isSecondaryIngredient(golem.getInventory().getItem(i))) {
                    // But only if it's not already splash/lingering (unless it's dragon breath)
                    if (stack.is(net.minecraft.world.item.Items.POTION)) return false; // Can still add gunpowder/redstone/glowstone
                    if (stack.is(net.minecraft.world.item.Items.SPLASH_POTION) && findItemInInventoryStatic(golem, net.minecraft.world.item.Items.DRAGON_BREATH) != -1) return false;
                }
            }
            
            return true;
        }

        public static boolean isRegularPotionStatic(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                Holder<net.minecraft.world.item.alchemy.Potion> p = potion.potion().get();
                return !p.is(net.minecraft.world.item.alchemy.Potions.WATER) && !p.is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        public static boolean isWaterBottleStatic(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER);
            }
            return false;
        }

        public static boolean isAwkwardPotionStatic(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        public static int findItemInInventoryStatic(UtilityGolem golem, Item item) {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(item)) return i;
            }
            return -1;
        }

        private boolean isFinishedPotion(ItemStack stack) {
            // Keep for compatibility if used elsewhere, but we updated interactWithBrewingStand to use isFullyFinished
            if (stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && !potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER);
            }
            return false;
        }

        private int findItemInInventory(Item item) {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(item)) return i;
            }
            return -1;
        }

        private int findWaterBottleInInventory() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (stack.is(net.minecraft.world.item.Items.POTION)) {
                    net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        private int findIngredientInInventory() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (isIngredient(stack)) return i;
            }
            return -1;
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && golem.level().getBlockState(targetPos).is(net.minecraft.world.level.block.Blocks.BREWING_STAND) && hasWorkAtStand();
        }

        private boolean isInventoryFull() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).isEmpty()) return false;
            }
            return true;
        }
    }
    public static class FillBottleGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos waterPos;
        private int actionTimer;
        private int cooldown;
        private int searchCooldown = 0;

        public FillBottleGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (isInventoryFull()) return false;
            if (findItemInInventory(net.minecraft.world.item.Items.GLASS_BOTTLE) == -1) return false;
            
            // Priority: Don't fill more water bottles if we already have 3 or more (water + finished potions)
            // Leave space for ingredients!
            if (countWaterBottles() + countPotions() >= 3) return false;

            // Also don't fill bottles if there's an active brewing process that needs our attention
            // (e.g. we have ingredients but the stand is empty or waiting for them)
            if (shouldPrioritizeBrewing()) {
                searchCooldown = 20 + golem.getRandom().nextInt(20);
                return false;
            }

            waterPos = findNearbyWater();
            if (waterPos == null) {
                searchCooldown = 40 + golem.getRandom().nextInt(40);
                return false;
            }
            return true;
        }

        private boolean shouldPrioritizeBrewing() {
            // Use cached targetPos from BrewingGoal if possible? 
            // For now just optimization: don't call findBrewingStand every tick
            BlockPos standPos = findBrewingStand();
            if (standPos == null) return false;
            
            BlockEntity be = golem.level().getBlockEntity(standPos);
            if (be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity stand) {
                // If we have ingredients and stand can take them, prioritize brewing
                if (findIngredientInInventory() != -1 && stand.getItem(3).isEmpty()) {
                    // But only if there are actually potions/bottles to brew with
                    boolean standHasBottles = false;
                    for (int i = 0; i < 3; i++) {
                        if (!stand.getItem(i).isEmpty()) {
                            standHasBottles = true;
                            break;
                        }
                    }
                    if (standHasBottles) return true;
                }
                
                // If we have water bottles and stand has empty slots, prioritize brewing
                if (findWaterBottleInInventory() != -1) {
                    for (int i = 0; i < 3; i++) {
                        if (stand.getItem(i).isEmpty()) return true;
                    }
                }

                // If stand has finished potions we can collect
                for (int i = 0; i < 3; i++) {
                    if (!stand.getItem(i).isEmpty() && BrewingGoal.isFullyFinished(golem, stand.getItem(i))) return true;
                }
            }
            return false;
        }

        private BlockPos findBrewingStand() {
            BlockPos pos = golem.blockPosition();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.BREWING_STAND)) {
                            golem.debugLog("FillBottleGoal: Found brewing stand at " + p.toShortString());
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private int findWaterBottleInInventory() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (isWaterBottle(stack)) return i;
            }
            return -1;
        }

        private int findIngredientInInventory() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (isIngredient(stack)) return i;
            }
            return -1;
        }

        private int countWaterBottles() {
            int count = 0;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (isWaterBottle(golem.getInventory().getItem(i))) count++;
            }
            return count;
        }

        private int countPotions() {
            int count = 0;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                    if (!isWaterBottle(stack)) count++;
                }
            }
            return count;
        }

        private boolean isInventoryEmpty() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (!golem.getInventory().getItem(i).isEmpty()) return false;
            }
            return true;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private ItemStack transferStackToChest(ItemStack stack, Container container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack containerStack = container.getItem(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxStackSize() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).isEmpty()) {
                    container.setItem(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.isSameItemSameComponents(stack, other) && other.getCount() < other.getMaxStackSize();
        }

        private boolean isWaterBottle(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER);
            }
            return false;
        }

        private boolean isAwkwardPotion(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        private boolean isRegularPotion(ItemStack stack) {
            if (stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                Holder<net.minecraft.world.item.alchemy.Potion> p = potion.potion().get();
                return !p.is(net.minecraft.world.item.alchemy.Potions.WATER) && !p.is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }
            return false;
        }

        private BlockPos findNearbyWater() {
            BlockPos pos = golem.blockPosition();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)) {
                            return p;
                        }
                        BlockState state = golem.level().getBlockState(p);
                        if (state.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON) || state.is(net.minecraft.world.level.block.Blocks.CAULDRON)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void start() {
            actionTimer = 0;
            golem.setDebugTarget(waterPos);
            golem.setHeldItem(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
        }

        @Override
        public void stop() {
            waterPos = null;
            golem.setDebugTarget(null);
            golem.setHeldItem(ItemStack.EMPTY);
            cooldown = 30 + golem.getRandom().nextInt(10); // 1.5 - 2s cooldown
        }

        @Override
        public void tick() {
            if (waterPos == null) return;

            // Only search for water if the current target is no longer water or a cauldron
            if (golem.getRandom().nextInt(40) == 0) {
                BlockState state = golem.level().getBlockState(waterPos);
                boolean isWater = golem.level().getFluidState(waterPos).is(net.minecraft.tags.FluidTags.WATER) ||
                                 state.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON) || state.is(net.minecraft.world.level.block.Blocks.CAULDRON);
                if (!isWater) {
                    waterPos = findNearbyWater();
                    if (waterPos == null) return;
                }
            }

            if (golem.getNavigation().isDone() || golem.getRandom().nextInt(20) == 0) {
                golem.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.0D);
            }
            golem.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);

            if (golem.distanceToSqr(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5) < 4.0D) {
                golem.getNavigation().stop(); // Stop moving once close enough
                actionTimer++;
                // Fill first bottle after 20 ticks (1s), then subsequent bottles every 10 ticks (0.5s)
                if (actionTimer >= 20 && actionTimer % 10 == 0) {
                    fillBottle();
                    if (isInventoryFull()) {
                        waterPos = null;
                        golem.setHeldItem(ItemStack.EMPTY);
                    } else if (findItemInInventory(net.minecraft.world.item.Items.GLASS_BOTTLE) != -1) {
                        golem.setHeldItem(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
                    }
                }
            }
        }

        private void fillBottle() {
            int slot = findItemInInventory(net.minecraft.world.item.Items.GLASS_BOTTLE);
            if (slot != -1) {
                BlockState state = golem.level().getBlockState(waterPos);
                boolean canFill = false;
                if (golem.level().getFluidState(waterPos).is(net.minecraft.tags.FluidTags.WATER)) {
                    canFill = true;
                } else if (state.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)) {
                    int level = state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
                    if (level > 0) {
                        if (level == 1) {
                            golem.level().setBlock(waterPos, net.minecraft.world.level.block.Blocks.CAULDRON.defaultBlockState(), 3);
                        } else {
                            golem.level().setBlock(waterPos, state.setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, level - 1), 3);
                        }
                        canFill = true;
                    }
                }

                if (!canFill) return;

                golem.getInventory().removeItem(slot, 1);
                ItemStack waterBottle = new ItemStack(net.minecraft.world.item.Items.POTION);
                waterBottle.set(DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.WATER));
                
                golem.setHeldItem(waterBottle.copy());

                ItemStack remaining = golem.getInventory().addItem(waterBottle);
                if (!remaining.isEmpty()) {
                    BlockPos chestPos = golem.getChestPos();
                    if (chestPos != null && golem.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5) < 16.0D) {
                        Container chestInv = golem.getChestInventory(chestPos);
                        if (chestInv != null) {
                            BlockState chestState = golem.level().getBlockState(chestPos);
                            if (chestState.getBlock() == golem.getGolemType().getChestBlock()) {
                                remaining = transferStackToChest(remaining, chestInv);
                            } else if (golem.getGolemType() == GolemType.NETHER_WART && BrewingGoal.isRegularPotionStatic(remaining)) {
                                // Nether Wart Golems can deposit completed potions in normal chests
                                remaining = transferStackToChest(remaining, chestInv);
                            }
                        }
                    }
                    if (!remaining.isEmpty()) {
                        Block.popResource(golem.level(), golem.blockPosition(), remaining);
                    }
                }
                golem.level().playSound(null, golem.blockPosition(), net.minecraft.sounds.SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }

        private int findItemInInventory(Item item) {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(item)) return i;
            }
            return -1;
        }

        @Override
        public boolean canContinueToUse() {
            return waterPos != null && findItemInInventory(net.minecraft.world.item.Items.GLASS_BOTTLE) != -1 && !isInventoryFull();
        }

        private boolean isInventoryFull() {
            int emptySlots = 0;
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).isEmpty()) {
                    emptySlots++;
                }
            }
            // If we have no empty slots, we consider it full for the purpose of filling more bottles
            return emptySlots == 0;
        }
    }
    public static class DepositItemsGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos chestPos;
        private int delay;
        private int searchCooldown = 0;

        public DepositItemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        public boolean hasItemsToDeposit() {
            if (golem.getGolemType() == GolemType.EMERALD) return hasEmeraldsOrBuyListItems();
            if (golem.getGolemType() == GolemType.BAMBOO) return hasCropsToDeposit();
            if (golem.getGolemType() == GolemType.DEEPSLATE) return hasDeepslateItemsToDeposit();
            if (golem.getGolemType() == GolemType.SPONGE) return hasSpongeItemsToDeposit();
            if (golem.getGolemType() == GolemType.GOLD) return hasGoldGolemsItemsToDeposit();
            if (golem.getGolemType() == GolemType.JUKEBOX) return hasJukeboxItemsToDeposit();
            if (golem.getGolemType() == GolemType.DIAMOND) return hasDiamondItemsToDeposit();
            if (golem.getGolemType() == GolemType.NETHER_WART) return hasNetherWartItemsToDeposit();
            if (golem.getGolemType() == GolemType.HONEYCOMB) return hasHoneycombItemsToDeposit();
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) return hasNetheriteItemsToDeposit();
            if (golem.getGolemType() == GolemType.LAPIS) return hasLapisItemsToDeposit();
            return hasFullStack() || (isInventoryFull() && hasAnythingToDeposit());
        }

        private boolean hasHoneycombItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) continue;
                return true;
            }
            return false;
        }

        private boolean hasAnythingToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                return true;
            }
            return false;
        }

        private boolean hasNetheriteItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.is(UGItems.GOLEM_SPAWN_EGGS.get(golem.getGolemType()))) continue;
                return true;
            }
            return false;
        }

        private boolean hasNetherWartItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) continue;
                if (isIngredient(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.BLAZE_POWDER)) continue;
                if (stack.is(net.minecraft.world.item.Items.POTION)) {
                    net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent()) {
                         Holder<net.minecraft.world.item.alchemy.Potion> p = potion.potion().get();
                         if (p.is(net.minecraft.world.item.alchemy.Potions.WATER) || p.is(net.minecraft.world.item.alchemy.Potions.AWKWARD)) {
                             continue;
                         }
                    }
                }
                return true;
            }
            return false;
        }

        private boolean hasEmeraldsOrBuyListItems() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (stack.is(net.minecraft.world.item.Items.EMERALD)) return true;
                
                // If it's a sellable item, we should deposit it if we don't have a villager nearby who wants it.
                // This handles the case where we switched villagers.
                List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), v -> true);
                boolean isWantedNearby = false;
                for (Villager villager : villagers) {
                    for (MerchantOffer offer : villager.getOffers()) {
                        if (!offer.isOutOfStock() && offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                            if (offer.getItemCostA().test(stack) || (offer.getItemCostB().isPresent() && offer.getItemCostB().get().test(stack))) {
                                isWantedNearby = true;
                                break;
                            }
                        }
                    }
                    if (isWantedNearby) break;
                }
                
                // If it's something that SOME villager once bought (discovered), but no one nearby wants it now,
                // or if it's just something we happened to pick up that is in our discovered list,
                // and it's NOT wanted by a nearby villager, we should deposit it.
                // WAIT, if it IS wanted nearby, we SHOULD NOT deposit it (we want to trade it).
                // If it is NOT wanted nearby, but IS a discovered trade item, then we definitely have no use for it right now.
                
                for (ItemStack buyItem : golem.getDiscoveredTrades()) {
                    if (ItemStack.isSameItemSameComponents(stack, buyItem)) {
                        if (!isWantedNearby) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasLapisItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                
                // Deposit any gathered blocks or ores
                return true;
            }
            
            return false;
        }

        private boolean hasSpongeItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            int itemCount = 0;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (!UtilityGolem.isTool(stack)) {
                    itemCount += stack.getCount();
                }
            }
            if (itemCount >= 16) return true;
            if (isInventoryFull() && itemCount > 0) return true;
            return false;
        }

        private boolean hasDiamondItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem)) return true;
            }
            return false;
        }

        private boolean hasJukeboxItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (stack.get(DataComponents.JUKEBOX_PLAYABLE) != null) continue;
                return true;
            }
            return false;
        }

        private boolean hasGoldGolemsItemsToDeposit() {
            if (golem.getGoldTradeCount() >= 8) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (!stack.is(net.minecraft.world.item.Items.GOLD_INGOT) && !stack.is(net.minecraft.world.item.Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private boolean hasDeepslateItemsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue; // Common check in depositItems
                
                if (stack.is(net.minecraft.world.item.Items.CHORUS_FRUIT)) return true; // Always deposit fruit
                if (isSapling(stack)) {
                    if (getSaplingCount(stack.getItem()) > 8) return true;
                    continue;
                }
                if (stack.is(net.minecraft.world.item.Items.STICK)) return true; // Always deposit sticks
                return true; // Anything else should be deposited
            }
            return false;
        }

        private int getSaplingCount(Item item) {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(item)) {
                    count += inv.getItem(i).getCount();
                }
            }
            return count;
        }

        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (hasItemsToDeposit()) {
                chestPos = findNearbyChest();
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return true;
            }
            return false;
        }


        private boolean hasCropsToDeposit() {
            SimpleContainer inv = golem.getInventory();
            
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (isCrop(stack)) {
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (stack.is(net.minecraft.world.item.Items.MELON_SLICE)) return true; // Always deposit melon slices
                        if (isSeed(stack) || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO)) {
                            // Only deposit seeds/carrots/potatoes if we have more than 16 (keep some for planting)
                            // However, if the golem.getInventory() is full, we should still deposit them to make room.
                            if (getSeedCount(stack.getItem()) > 16 || isInventoryFull()) return true;
                            continue;
                        }
                    }
                    return true;
                }
            }
            
            if (isInventoryFull()) {
                // If golem.getInventory() is full, we must deposit SOMETHING that isn't a tool or bucket
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.isEmpty()) continue;
                    if (stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET) || UtilityGolem.isTool(stack)) {
                        continue;
                    }
                    return true;
                }
            }
            
            return false;
        }

        private int getSeedCount(Item item) {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(item)) {
                    count += stack.getCount();
                }
            }
            return count;
        }

        private boolean isCrop(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.WHEAT) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT)
                    || stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)
                    || stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS)
                    || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS)
                    || stack.is(net.minecraft.world.item.Items.PUMPKIN) || stack.is(net.minecraft.world.item.Items.MELON) || stack.is(net.minecraft.world.item.Items.MELON_SLICE);
        }

        private boolean hasFullStack() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                // MAKE SURE THE STACK IS NOT A TOOL THAT SHOULD BE KEPT
                if (!stack.isEmpty() && stack.getCount() >= stack.getMaxStackSize()) {
                    if (UtilityGolem.isTool(stack)) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }

        private boolean isInventoryFull() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        @Override
        public void start() {
            delay = 0;
            if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                updateHeldItem();
            }
        }

        private void updateHeldItem() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) continue;
                if (isIngredient(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.BLAZE_POWDER)) continue;
                if (stack.is(net.minecraft.world.item.Items.POTION)) {
                    net.minecraft.world.item.alchemy.PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent()) {
                         Holder<net.minecraft.world.item.alchemy.Potion> p = potion.potion().get();
                         if (p.is(net.minecraft.world.item.alchemy.Potions.WATER) || p.is(net.minecraft.world.item.alchemy.Potions.AWKWARD)) {
                             continue;
                         }
                    }
                }
                golem.setHeldItem(stack.copyWithCount(1));
                return;
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public boolean canContinueToUse() {
            return chestPos != null && hasItemsToDeposit() && golem.getChestInventory(chestPos) != null;
        }

        private boolean isSeed(ItemStack stack) {
            if (stack.isEmpty()) return false;
            return stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)
                    || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS)
                    || stack.is(net.minecraft.world.item.Items.PITCHER_POD) || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS);
        }

        private boolean isSapling(ItemStack stack) {
            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                return stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER);
            }
            return stack.is(net.minecraft.tags.ItemTags.SAPLINGS);
        }

        private boolean isApple(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.APPLE);
        }

        private int getSaplingCount() {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isSapling(inv.getItem(i))) {
                    count += inv.getItem(i).getCount();
                }
            }
            return count;
        }

        @Override
        public void stop() {
            if (chestPos != null) {
                golem.level().blockEvent(chestPos, golem.level().getBlockState(chestPos).getBlock(), 1, 0);
            }
            golem.setSearching(false);
            chestPos = null;
            if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                golem.setHeldItem(ItemStack.EMPTY);
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                golem.resetGoldTradeCount();
            }
            searchCooldown = 20 + golem.getRandom().nextInt(20);
        }

        private int stuckTicks = 0;
        private Vec3 lastPos = net.minecraft.world.phys.Vec3.ZERO;

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dx = golem.getX() - (chestPos.getX() + 0.5);
            double dy = golem.getY() - (chestPos.getY() + 0.5);
            double dz = golem.getZ() - (chestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                // stuck check
                Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.distanceToSqr(lastPos) < 0.001) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastPos = currentPos;

                if (stuckTicks > 100) {
                    golem.blacklistPosition(chestPos);
                    stop();
                    return;
                }

                // If it's far vertically, target our current height
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    BlockPos targetPos = findStandablePosNear(chestPos);
                    boolean possible;
                    // Lapis golems often have to travel significant vertical distances to return to their chest
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    }

                    if (!possible) {
                        golem.blacklistPosition(chestPos);
                        stop();
                        return;
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (delay > 0) {
                    delay--;
                    if (delay == 0) {
                        if (!hasItemsToDeposit()) {
                            stop();
                        }
                    }
                    return;
                }

                if (golem.getRandom().nextInt(10) == 0) {
                    golem.level().blockEvent(chestPos, golem.level().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    golem.setAnimation(GolemAnimation.DEPOSITING, 100);
                    depositItems();
                    if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                        updateHeldItem();
                    }
                    delay = 100; // Wait for animation
                }
            }
        }

        private BlockPos findStandablePosNear(BlockPos pos) {
            Level world = golem.level();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos p = pos.relative(dir);
                if (world.getBlockState(p).is(net.minecraft.world.level.block.Blocks.AIR) && world.getBlockState(p.above()).is(net.minecraft.world.level.block.Blocks.AIR) && !world.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.AIR)) {
                    return p;
                }
            }
            return pos;
        }

        private void depositItems() {
            Container container = golem.getChestInventory(chestPos);
            if (container != null) {
                SimpleContainer golemInv = golem.getInventory();
                for (int i = 0; i < golemInv.getContainerSize(); i++) {
                    ItemStack stack = golemInv.getItem(i);
                    if (!stack.isEmpty() && !UtilityGolem.isTool(stack)) {
                        if (golem.getGolemType() == GolemType.LAPIS) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.EMERALD) {
                            boolean isWantedNearby = false;
                            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), v -> true);
                            for (Villager villager : villagers) {
                                for (MerchantOffer offer : villager.getOffers()) {
                                    if (!offer.isOutOfStock() && offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                                        if (offer.getItemCostA().test(stack) || (offer.getItemCostB().isPresent() && offer.getItemCostB().get().test(stack))) {
                                            isWantedNearby = true;
                                            break;
                                        }
                                    }
                                }
                                if (isWantedNearby) break;
                            }

                            if (stack.is(net.minecraft.world.item.Items.EMERALD)) {
                                ItemStack remaining_stack = transferStack(stack, container);
                                golemInv.setItem(i, remaining_stack);
                                continue;
                            }

                            // If it's a discovered trade item but NOT wanted nearby, deposit it.
                            boolean isDiscovered = false;
                            for (ItemStack buyItem : golem.getDiscoveredTrades()) {
                                if (ItemStack.isSameItemSameComponents(stack, buyItem)) {
                                    isDiscovered = true;
                                    break;
                                }
                            }

                            if (isDiscovered && !isWantedNearby) {
                                // Deposit this stack
                                ItemStack remaining_stack = transferStack(stack, container);
                                golemInv.setItem(i, remaining_stack);
                                continue;
                            } else if (isDiscovered && isWantedNearby) {
                                continue;
                            }
                        }
            if (golem.getGolemType() == GolemType.BAMBOO) {
                if (stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET) || UtilityGolem.isTool(stack)) {
                    continue;
                }
                if (stack.is(net.minecraft.world.item.Items.MELON_SLICE) || stack.is(net.minecraft.world.item.Items.PUMPKIN) || stack.is(net.minecraft.world.item.Items.MELON) || stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS)) {
                    ItemStack remaining_stack = transferStack(stack, container);
                    golemInv.setItem(i, remaining_stack);
                    continue;
                }
                if (isSeed(stack) || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO)) {
                                int seedCount = getSeedCount(stack.getItem());
                                if (seedCount <= 16 && !isInventoryFull()) continue;
                                
                                // Transfer only the excess, unless golem.getInventory() is full then transfer as much as needed to get down to 16 or clear the slot
                                int toKeep = 16;
                                if (seedCount <= 16 && isInventoryFull()) {
                                    // If we have <= 16 but golem.getInventory() is full, we still want to keep some if possible, 
                                    // but if we need to clear space, we might have to deposit some.
                                    // Let's keep it simple: if full, we can deposit even below 16 if it's the only way to clear a slot,
                                    // but usually we just deposit what's over 16.
                                    // If total is 16 and we are full, we should deposit SOME to make room.
                                    toKeep = 8; 
                                }
                                
                                int toTransfer = seedCount - toKeep;
                                if (toTransfer <= 0) continue;
                                
                                ItemStack toDeposit = stack.copyWithCount(Math.min(stack.getCount(), toTransfer));
                                ItemStack remaining_stack = transferStack(toDeposit, container);
                                stack.setCount(stack.getCount() - (toDeposit.getCount() - remaining_stack.getCount()));
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            if (stack.is(net.minecraft.world.item.Items.CHORUS_FRUIT)) {
                                ItemStack remaining_stack = transferStack(stack, container);
                                golemInv.setItem(i, remaining_stack);
                                continue;
                            }
                            if (isSapling(stack) && getSaplingCount(stack.getItem()) <= 8) {
                                continue;
                            }
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.GOLD) {
                            if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.GOLD_NUGGET)) {
                                continue;
                            }
                            // Also don't deposit items the golem is currently "holding" for a purpose (though gold golems usually hold gold)
                            if (ItemStack.isSameItemSameComponents(stack, golem.getHeldItem())) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.JUKEBOX) {
                            if (stack.get(DataComponents.JUKEBOX_PLAYABLE) != null) {
                                continue;
                            }
                            if (ItemStack.isSameItemSameComponents(stack, golem.getHeldItem())) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.DIAMOND) {
                            if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                            if (stack.is(UGItems.GOLEM_SPAWN_EGGS.get(golem.getGolemType()))) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.SPONGE) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.NETHER_WART) {
                            // Don't deposit glass bottles, ingredients, blaze powder, brewing stands, or water/awkward potions
                            if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || isIngredient(stack) || stack.is(net.minecraft.world.item.Items.BLAZE_POWDER) || stack.is(net.minecraft.world.item.Items.BREWING_STAND)) {
                                continue;
                            }
                            if (BrewingGoal.isWaterBottleStatic(stack) || BrewingGoal.isAwkwardPotionStatic(stack)) {
                                continue;
                            }
                            
                            // If this isn't a Nether Wart Golem Chest, only deposit completed potions
                            if (golem.level().getBlockState(chestPos).getBlock() != golem.getGolemType().getChestBlock()) {
                                if (!BrewingGoal.isRegularPotionStatic(stack)) {
                                    continue;
                                }
                            }
                        }
                        if (golem.getGolemType() == GolemType.HONEYCOMB) {
                            if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || stack.is(net.minecraft.world.item.Items.SHEARS)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.GOLD) {
                            if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.GOLD_NUGGET)) {
                                continue;
                            }
                        }
                        ItemStack remaining = transferStack(stack, container);
                        golemInv.setItem(i, remaining);
                    }
                }
                golemInv.setChanged();
                container.setChanged();
            }
        }

        private ItemStack transferStack(ItemStack stack, Container container) {
            ItemStack remaining = stack.copy();
            // Try to add to existing stacks first
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack containerStack = container.getItem(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxStackSize() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            // Try to find an empty slot
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).isEmpty()) {
                    container.setItem(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.isSameItemSameComponents(stack, other) && other.getCount() < other.getMaxStackSize();
        }
    }
    public static class WithdrawItemsGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos chestPos;
        private int delay;
        private int searchCooldown = 0;

        public WithdrawItemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (golem.getGolemType() == GolemType.EMERALD) {
                // If we are set to buy something, we need emeralds.
                // If we are NOT set to buy something, we are selling, so we need sellable items.
                ItemStack selectedBuy = golem.getSelectedBuyItem();
                if (selectedBuy != null && !selectedBuy.isEmpty()) {
                    if (hasEmeralds() || isInventoryFull()) {
                        return false;
                    }
                } else {
                    if (isInventoryFull() || hasTradeItems()) {
                        return false;
                    }
                }

                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.LAPIS) {
                // Lapis golems only need to withdraw if they are completely missing a pickaxe or shovel.
                // This prevents them from returning to the chest just because they have one tool but not both,
                // or if they have a damaged tool but it hasn't broken yet.
                // UPDATED: Lapis golems can now mine toolless, so they only withdraw if they find a tool in chest.
                // If they are already near a target, they should prefer mining over withdrawing.
                if (hasPickaxe() || hasShovel()) {
                    return false;
                }
                
                // If we already have a target to mine (toolless), don't go back for a tool yet.
                if (new DigBlockGoal(golem).canUse()) {
                    return false;
                }
                
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                if (hasAxe() && hasShears() && hasEnoughSaplings()) {
                    return false;
                }
                if (isInventoryFull()) {
                    return false;
                }
                
                // If we don't have enough saplings, ONLY return to chest if there are NO trees to chop.
                // This prevents looping when we are in the middle of a forest.
                if (!hasEnoughSaplings()) {
                    ChopTreeGoal chopGoal = new ChopTreeGoal(golem);
                    if (chopGoal.canUse()) {
                        return false;
                    }
                }
                
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isTool(golem.getHeldItem())) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.BAMBOO) {
                // If we have EVERYTHING, we don't need to withdraw
                if (!hasAnythingNeeded()) {
                    return false;
                }

                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }

                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }

                // If we can already farm something (harvesting), prioritize that over withdrawing tools
                if (new FarmGoal(golem).canUse()) {
                    return false;
                }

                return hasNeededItemsInChest(chestPos);
            }
            
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                return false;
            }
            if (golem.getGolemType() == GolemType.AMETHYST) {
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.HONEYCOMB) {
                if (hasGlassBottles() && hasShears()) {
                    return false;
                }
                if (isInventoryFull()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.REDSTONE) {
                if (hasRedstoneDust() && hasRepeater()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.GOLD) {
                if (hasGold()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.JUKEBOX) {
                if (hasMusicDisc()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.FURNACE) {
                if (hasFuel()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.SPONGE) {
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isFishingRod(golem.getHeldItem())) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.DIAMOND) {
                if (hasBlocks()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.EMERALD) {
                if (hasTradeItems()) {
                    return false;
                }
                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 40 + golem.getRandom().nextInt(40);
                    return false;
                }
                return hasTradeItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.NETHER_WART) {
                if (hasIngredients() && hasSecondaryIngredients() && hasGlassBottles() && hasBlazePowder() && hasItem(net.minecraft.world.item.Items.BREWING_STAND)) {
                    // Check if we have at least 1 stack of each. 
                    // Actually, if we have them, we might still want more if we have room in our 6 reserved slots.
                    int supplySlotsUsed = 0;
                    for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                        ItemStack s = golem.getInventory().getItem(i);
                        if (!s.isEmpty() && (isIngredient(s) || s.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || s.is(net.minecraft.world.item.Items.BLAZE_POWDER) || s.is(net.minecraft.world.item.Items.BREWING_STAND))) {
                            supplySlotsUsed++;
                        }
                    }
                    if (supplySlotsUsed >= 6) {
                         searchCooldown = 100 + golem.getRandom().nextInt(100); // Wait 5-10s
                         return false;
                    }
                }
                
                // Don't withdraw if golem.getInventory() is full (regardless of supply slots)
                if (isInventoryFull()) {
                     searchCooldown = 100 + golem.getRandom().nextInt(100);
                     return false;
                }

                chestPos = golem.getChestPos();
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                if (chestPos == null) {
                    searchCooldown = 100 + golem.getRandom().nextInt(100);
                    return false;
                }
                boolean hasNeeded = hasNeededItemsInChest(chestPos);
                if (!hasNeeded) {
                    searchCooldown = 100 + golem.getRandom().nextInt(100);
                }
                return hasNeeded;
            }

            return false;
        }

        private boolean hasIngredients() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isPrimaryIngredient(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasSecondaryIngredients() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isSecondaryIngredient(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasGlassBottles() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.GLASS_BOTTLE)) return true;
            }
            return false;
        }

        private boolean hasBlazePowder() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.BLAZE_POWDER)) return true;
            }
            return false;
        }

        private boolean hasTradeItems() {
            List<MerchantOffers> allOffers = findNearbyVillagerOffers();
            if (allOffers.isEmpty()) return false;
            SimpleContainer inventory = golem.getInventory();
            for (MerchantOffers offers : allOffers) {
                for (MerchantOffer offer : offers) {
                    if (offer.isOutOfStock()) continue;
                    if (offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                        ItemCost buyItem1 = offer.getItemCostA();
                        Optional<ItemCost> buyItem2 = offer.getItemCostB();
                        if (hasStack(golem.getInventory(), buyItem1) && (buyItem2.isEmpty() || hasStack(golem.getInventory(), buyItem2.get()))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private List<MerchantOffers> findNearbyVillagerOffers() {
            List<MerchantOffers> allOffers = new ArrayList<>();
            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), villager -> true);
            for (Villager villager : villagers) {
                MerchantOffers offers = villager.getOffers();
                for (MerchantOffer offer : offers) {
                    if (!offer.isOutOfStock() && offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                        allOffers.add(offers);
                        break;
                    }
                }
            }
            return allOffers;
        }

        private boolean hasTradeItemsInChest(BlockPos pos) {
            Container container = golem.getChestInventory(pos);
            if (container != null) {
                List<MerchantOffers> allOffers = findNearbyVillagerOffers();
                if (allOffers.isEmpty()) return false;
                
                for (MerchantOffers offers : allOffers) {
                    for (MerchantOffer offer : offers) {
                        if (offer.isOutOfStock()) continue;
                        if (offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                            ItemCost buyItem1 = offer.getItemCostA();
                            Optional<ItemCost> buyItem2 = offer.getItemCostB();
                            
                            boolean has1 = hasStackInInventoryOrChest(buyItem1, container);
                            boolean has2 = buyItem2.isEmpty() || hasStackInInventoryOrChest(buyItem2.get(), container);
                            
                            if (has1 && has2) return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasStackInInventoryOrChest(ItemCost target, Container chest) {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (target.test(stack)) count += stack.getCount();
            }
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (target.test(stack)) count += stack.getCount();
            }
            return count >= target.count();
        }

        private boolean hasStack(Container inventory, ItemCost target) {
            int count = 0;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack invStack = inventory.getItem(i);
                if (target.test(invStack)) {
                    count += invStack.getCount();
                }
            }
            return count >= target.count();
        }

        private boolean hasPickaxe() {
            if (UtilityGolem.isPickaxe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isPickaxe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShovel(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasAxe() {
            if (UtilityGolem.isAxe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isAxe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasShears() {
            if (UtilityGolem.isShears(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShears(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasEnoughSaplings() {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER)) {
                    count += stack.getCount();
                }
            }
            return count >= 8;
        }

        private int getSaplingCount() {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER)) {
                    count += stack.getCount();
                }
            }
            return count;
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private boolean hasEmptyBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.BUCKET)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.BUCKET)) return true;
            }
            return false;
        }

        private boolean hasSeeds() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isSeed(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean isSeed(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)
                    || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS)
                    || stack.is(net.minecraft.world.item.Items.PITCHER_POD) || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS);
        }

        private boolean hasFuel() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isFuel(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean isFuel(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.COAL) || stack.is(net.minecraft.world.item.Items.CHARCOAL) || stack.is(net.minecraft.world.item.Items.BLAZE_ROD) || stack.is(net.minecraft.world.item.Items.LAVA_BUCKET);
        }

        private boolean hasBlocks() {
            SimpleContainer inventory = golem.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (inventory.getItem(i).getItem() instanceof net.minecraft.world.item.BlockItem) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasMusicDisc() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).get(DataComponents.JUKEBOX_PLAYABLE) != null) return true;
            }
            return false;
        }

        private boolean hasEmeralds() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.EMERALD)) return true;
            }
            return false;
        }

        private boolean hasItem(Item item) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(item)) return true;
            }
            return false;
        }

        private boolean hasGold() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private boolean hasRedstone() {
            SimpleContainer inv = golem.getInventory();
            boolean hasDust = false;
            boolean hasRepeater = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.REDSTONE)) hasDust = true;
                if (inv.getItem(i).is(net.minecraft.world.item.Items.REPEATER)) hasRepeater = true;
            }
            return hasDust && hasRepeater;
        }

        private boolean hasRedstoneDust() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.REDSTONE)) return true;
            }
            return false;
        }

        private boolean hasRepeater() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.REPEATER)) return true;
            }
            return false;
        }

        private boolean hasEnoughBreedingItems() {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isValidBreedingItem(inv.getItem(i))) {
                    count += inv.getItem(i).getCount();
                }
            }
            return count >= 8;
        }

        private boolean isInventoryFull() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        private boolean hasNeededItemsInChest(BlockPos pos) {
            Container container = golem.getChestInventory(pos);
            if (container != null) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.isEmpty()) continue;
                    if (golem.getGolemType() == GolemType.DIAMOND) {
                        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) return true;
                    }
                    if (golem.getGolemType() == GolemType.SPONGE) {
                        if (UtilityGolem.isFishingRod(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        if (UtilityGolem.isTool(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                        if (UtilityGolem.isTool(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.DEEPSLATE) {
                        if (UtilityGolem.isAxe(stack) && !hasAxe()) return true;
                        if (UtilityGolem.isShears(stack) && !hasShears()) return true;
                        if (!hasEnoughSaplings() && stack.is(net.minecraft.tags.ItemTags.SAPLINGS)) return true;
                    }
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (new FarmGoal(golem).canUse()) return false;
                        
                        if (!hasHoe() && UtilityGolem.isHoe(stack)) return true;
                        if (!hasAxe() && UtilityGolem.isAxe(stack)) return true;
                        if (!hasWaterBucket() && !hasEmptyBucket() && (stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET))) return true;
                        if (!hasSeeds() && isSeed(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.AMETHYST) {
                        if (isValidBreedingItem(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.REDSTONE) {
                        if (stack.is(net.minecraft.world.item.Items.REDSTONE) || stack.is(net.minecraft.world.item.Items.REPEATER)) return true;
                    }
                    if (golem.getGolemType() == GolemType.JUKEBOX) {
                        if (stack.get(DataComponents.JUKEBOX_PLAYABLE) != null) return true;
                    }
                    if (golem.getGolemType() == GolemType.FURNACE) {
                        if (isFuel(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.SPONGE) {
                        if (UtilityGolem.isFishingRod(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.GOLD) {
                        if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.GOLD_NUGGET)) return true;
                    }
                    if (golem.getGolemType() == GolemType.EMERALD) {
                        // Only withdraw emeralds if we have a buy item selected and we don't already have emeralds
                        if (stack.is(net.minecraft.world.item.Items.EMERALD)) {
                            return !golem.getSelectedBuyItem().isEmpty() && !hasEmeralds();
                        }
                        
                        // Check if it's a sellable item
                        // Only withdraw sellable items if we don't have a buy item selected and we don't already have sellable items
                        if (golem.getSelectedBuyItem().isEmpty() && !hasTradeItems()) {
                            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), v -> true);
                            for (Villager villager : villagers) {
                                for (MerchantOffer offer : villager.getOffers()) {
                                    if (!offer.isOutOfStock() && offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                                        if (offer.getItemCostA().test(stack) || (offer.getItemCostB().isPresent() && offer.getItemCostB().get().test(stack))) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (golem.getGolemType() == GolemType.HONEYCOMB) {
                        if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) && !hasGlassBottles()) return true;
                        if (stack.is(net.minecraft.world.item.Items.SHEARS) && !hasShears()) return true;
                    }
                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) && !hasGlassBottles()) return true;
                        if (isIngredient(stack) && !hasItem(stack.getItem())) return true;
                        if (stack.is(net.minecraft.world.item.Items.BLAZE_POWDER) && !hasBlazePowder()) return true;
                        if (stack.is(net.minecraft.world.item.Items.BREWING_STAND) && !hasItem(net.minecraft.world.item.Items.BREWING_STAND)) return true;
                        if (BrewingGoal.isWaterBottleStatic(stack) || BrewingGoal.isAwkwardPotionStatic(stack)) {
                             // Only withdraw potions/water bottles if we have space in our 3 reserved water/potion slots
                             int potionSlotsUsed = 0;
                             for (int j = 0; j < golem.getInventory().getContainerSize(); j++) {
                                 ItemStack s = golem.getInventory().getItem(j);
                                 if (BrewingGoal.isWaterBottleStatic(s) || BrewingGoal.isRegularPotionStatic(s) || BrewingGoal.isAwkwardPotionStatic(s)) {
                                     potionSlotsUsed++;
                                 }
                             }
                             if (potionSlotsUsed < 3) {
                                 return true;
                             }
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasHoe() {
            if (UtilityGolem.isHoe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isHoe(inv.getItem(i))) return true;
            }
            return false;
        }

        public boolean hasAnythingNeeded() {
            if (golem.getGolemType() == GolemType.BAMBOO) {
                // If there is harvesting tasks (like mature nether wart), prioritize it over getting tools.
                // This prevents the golem from rocking back and forth near nether wart if it's missing a hoe/axe.
                if (new FarmGoal(golem).canUse()) return false;

                boolean needsHoe = !hasHoe();
                boolean needsAxe = !hasAxe();
                boolean needsWater = !hasWaterBucket() && !hasEmptyBucket();
                boolean needsSeeds = !hasSeeds();
                return needsHoe || needsAxe || needsWater || needsSeeds;
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                return !hasGoldIngot() || !hasGoldNugget();
            }
            return true;
        }

        private boolean hasGoldIngot() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.GOLD_INGOT)) return true;
            }
            return false;
        }

        private boolean hasGoldNugget() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        @Override
        public void start() {
            delay = 0;
            if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                updateHeldItem();
            }
        }

        private void updateHeldItem() {
            if (chestPos == null) return;
            Container container = golem.getChestInventory(chestPos);
            if (container != null) {
                // Determine what we are about to withdraw and hold it
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.isEmpty()) continue;
                    
                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (stack.is(net.minecraft.world.item.Items.BLAZE_POWDER) && !hasBlazePowder()) {
                            golem.setHeldItem(stack.copyWithCount(1));
                            return;
                        }
                        if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) && !hasGlassBottles()) {
                            golem.setHeldItem(stack.copyWithCount(1));
                            return;
                        }
                        if (isIngredient(stack) && !hasIngredients()) {
                            golem.setHeldItem(stack.copyWithCount(1));
                            return;
                        }
                        if (isSecondaryIngredient(stack) && !hasSecondaryIngredients()) {
                            golem.setHeldItem(stack.copyWithCount(1));
                            return;
                        }
                    } else if (golem.getGolemType() == GolemType.HONEYCOMB) {
                        if (stack.is(net.minecraft.world.item.Items.SHEARS) && !hasShears()) {
                             golem.setHeldItem(stack.copyWithCount(1));
                             return;
                        }
                        if (stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) && !hasGlassBottles()) {
                             golem.setHeldItem(stack.copyWithCount(1));
                             return;
                        }
                    }
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public boolean canContinueToUse() {
            if (chestPos == null) return false;
            Container container = golem.getChestInventory(chestPos);
            if (container == null) return false;

            if (golem.getGolemType() == GolemType.NETHER_WART) {
                // If golem.getInventory() is "full" for supplies (6 slots used), stop.
                int supplySlotsUsed = 0;
                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    ItemStack s = golem.getInventory().getItem(i);
                    if (!s.isEmpty() && (isIngredient(s) || s.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || s.is(net.minecraft.world.item.Items.BLAZE_POWDER))) {
                        supplySlotsUsed++;
                    }
                }
                if (supplySlotsUsed >= 6) {
                    return false;
                }

                // If no more needed items in chest, stop.
                if (chestPos == null || !hasNeededItemsInChest(chestPos)) {
                    return false;
                }
                
                return true;
            }

            if (golem.getGolemType() == GolemType.LAPIS) {
                return chestPos != null && (!hasPickaxe() || !hasShovel()) && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                return chestPos != null && (!hasAxe() || !hasShears() || !hasEnoughSaplings()) && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isSword(golem.getHeldItem())) && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.BAMBOO) {
                // Should continue if we still need something AND there's a chest to get it from
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                return chestPos != null && hasAnythingNeeded() && !isInventoryFull() && 
                       golem.getChestInventory(chestPos) != null &&
                       hasNeededItemsInChest(chestPos) &&
                       !new FarmGoal(golem).canUse(); // Interrupt if there's farming to do
            }
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                return false;
            }
            if (golem.getGolemType() == GolemType.AMETHYST) {
                return chestPos != null && !hasEnoughBreedingItems() && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.REDSTONE) {
                return chestPos != null && !hasRedstone() && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                return chestPos != null && (!hasGoldIngot() || !hasGoldNugget()) && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.JUKEBOX) {
                return chestPos != null && !hasMusicDisc() && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.FURNACE) {
                return chestPos != null && !hasFuel() && !isInventoryFull() && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.DIAMOND) {
                return chestPos != null && !hasBlocks() && !isInventoryFull() && 
                       golem.getChestInventory(chestPos) != null &&
                       hasNeededItemsInChest(chestPos);
            }
            if (golem.getGolemType() == GolemType.SPONGE) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isFishingRod(golem.getHeldItem())) && golem.getChestInventory(chestPos) != null;
            }
            if (golem.getGolemType() == GolemType.EMERALD) {
                boolean needsEmeraldsForBuying = !golem.getSelectedBuyItem().isEmpty() && !hasEmeralds();
                boolean needsItemsForSelling = findNearbyVillagerOffers() != null && !hasTradeItems() && hasTradeItemsInChest(chestPos);
                
                return chestPos != null && (needsEmeraldsForBuying || needsItemsForSelling) && !isInventoryFull() &&
                       golem.getChestInventory(chestPos) != null;
            }
            return false;
        }

        @Override
        public void stop() {
            if (chestPos != null) {
                golem.level().blockEvent(chestPos, golem.level().getBlockState(chestPos).getBlock(), 1, 0);
            }
            golem.setSearching(false);
            chestPos = null;
            if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                golem.setHeldItem(ItemStack.EMPTY);
            }
        }

        private int stuckTicks = 0;
        private Vec3 lastPos = net.minecraft.world.phys.Vec3.ZERO;

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dx = golem.getX() - (chestPos.getX() + 0.5);
            double dy = golem.getY() - (chestPos.getY() + 0.5);
            double dz = golem.getZ() - (chestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 1.5D || verticalDist > 1.5D) {
                // stuck check
                Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.distanceToSqr(lastPos) < 0.001) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastPos = currentPos;
                
                if (stuckTicks > 100) {
                    golem.blacklistPosition(chestPos);
                    stop();
                    return;
                }

                // If it's far vertically, target our current height
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // Lapis golems often have to travel significant vertical distances to return to their chest
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().moveTo(chestPos.getX() + 0.5, golem.getY(), chestPos.getZ() + 0.5, 1.2D);
                    } else {
                        possible = golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, 1.2D);
                    }

                    if (!possible) {
                        golem.blacklistPosition(chestPos);
                        stop();
                        return;
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (delay > 0) {
                    delay--;
                    if (delay == 0) {
                        withdrawItems();
                        if (golem.getGolemType() == GolemType.NETHER_WART || golem.getGolemType() == GolemType.HONEYCOMB) {
                            updateHeldItem();
                        }
                        if (golem.getGolemType() == GolemType.NETHER_WART) {
                            if (!canContinueToUse()) {
                                stop();
                            }
                        }
                    }
                    return;
                }

                if (golem.getRandom().nextInt(10) == 0) {
                    golem.level().blockEvent(chestPos, golem.level().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    golem.setAnimation(GolemAnimation.WITHDRAWING, 60);
                    delay = 60; // Wait for animation
                }
            }
        }

        private boolean withdrawItems() {
            Container container = golem.getChestInventory(chestPos);
            if (container != null) {
                golem.debugLog("WithdrawItemsGoal: Withdrawing from chest at " + chestPos.toShortString());
                SimpleContainer golemInv = golem.getInventory();
                boolean withdrawnSomething = false;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack containerStack = container.getItem(i);
                    if (containerStack.isEmpty()) continue;

                    if (golem.getGolemType() == GolemType.EMERALD) {
                        if (containerStack.is(net.minecraft.world.item.Items.EMERALD)) {
                            // Only withdraw emeralds if we have a buy item selected and we don't already have emeralds
                            if (!golem.getSelectedBuyItem().isEmpty() && !hasEmeralds()) {
                                ItemStack toWithdraw = containerStack.split(Math.min(containerStack.getCount(), containerStack.getMaxStackSize()));
                                golem.getInventory().addItem(toWithdraw);
                                withdrawnSomething = true;
                            }
                        } else if (golem.getSelectedBuyItem().isEmpty() && !hasTradeItems()) {
                            // Check if it's a sellable item
                            List<Villager> villagers = golem.level().getEntitiesOfClass(Villager.class, golem.getBoundingBox().inflate(16.0), v -> true);
                            boolean isSellable = false;
                            for (Villager villager : villagers) {
                                for (MerchantOffer offer : villager.getOffers()) {
                                    if (!offer.isOutOfStock() && offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                                        if (offer.getItemCostA().test(containerStack) || (offer.getItemCostB().isPresent() && offer.getItemCostB().get().test(containerStack))) {
                                            isSellable = true;
                                            break;
                                        }
                                    }
                                }
                                if (isSellable) break;
                            }

                            if (isSellable) {
                                ItemStack toWithdraw = containerStack.split(Math.min(containerStack.getCount(), containerStack.getMaxStackSize()));
                                golem.getInventory().addItem(toWithdraw);
                                withdrawnSomething = true;
                            }
                        }
                    }

                    if (golem.getGolemType() == GolemType.LAPIS) {
                        if (UtilityGolem.isPickaxe(containerStack) && !hasPickaxe()) {
                            ItemStack toWithdraw = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(toWithdraw);
                            } else {
                                golemInv.addItem(toWithdraw);
                            }
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        if (UtilityGolem.isShovel(containerStack) && !hasShovel()) {
                            ItemStack toWithdraw = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(toWithdraw);
                            } else {
                                golemInv.addItem(toWithdraw);
                            }
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                    }

                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (isIngredient(containerStack) || containerStack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || containerStack.is(net.minecraft.world.item.Items.BLAZE_POWDER) || containerStack.is(net.minecraft.world.item.Items.BREWING_STAND) || BrewingGoal.isWaterBottleStatic(containerStack) || BrewingGoal.isAwkwardPotionStatic(containerStack)) {
                            // Nether Wart Golem has a slot reservation system.
                            // 6 slots for ingredients/supplies, 3 slots reserved for water/potions.
                            
                            boolean isPotionOrWater = BrewingGoal.isWaterBottleStatic(containerStack) || BrewingGoal.isAwkwardPotionStatic(containerStack);
                            
                            int supplySlotsUsed = 0;
                            int potionSlotsUsed = 0;
                            SimpleContainer golemInv_count = golem.getInventory();
                            for (int j = 0; j < golemInv_count.getContainerSize(); j++) {
                                ItemStack s = golemInv_count.getItem(j);
                                if (!s.isEmpty()) {
                                    if (isIngredient(s) || s.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || s.is(net.minecraft.world.item.Items.BLAZE_POWDER) || s.is(net.minecraft.world.item.Items.BREWING_STAND)) {
                                        supplySlotsUsed++;
                                    } else if (BrewingGoal.isWaterBottleStatic(s) || BrewingGoal.isRegularPotionStatic(s) || BrewingGoal.isAwkwardPotionStatic(s)) {
                                        potionSlotsUsed++;
                                    }
                                }
                            }
                            
                            if (isPotionOrWater) {
                                if (potionSlotsUsed < 3) {
                                    ItemStack toWithdraw = containerStack.split(1);
                                    golemInv.addItem(toWithdraw);
                                    golemInv.setChanged();
                                    container.setChanged();
                                    return true;
                                }
                            } else if (supplySlotsUsed < 6) {
                                // Only withdraw if we don't already have a stack of this specific item
                                boolean alreadyHasItem = false;
                                for (int j = 0; j < golemInv.getContainerSize(); j++) {
                                    if (golemInv.getItem(j).is(containerStack.getItem())) {
                                        alreadyHasItem = true;
                                        break;
                                    }
                                }
                                
                                if (!alreadyHasItem) {
                                    int maxToWithdraw = 8;
                                    if (containerStack.is(net.minecraft.world.item.Items.BLAZE_POWDER) || containerStack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) {
                                        maxToWithdraw = 16;
                                    }
                                    if (containerStack.is(net.minecraft.world.item.Items.BREWING_STAND)) {
                                        maxToWithdraw = 1;
                                    }
                                    
                                    ItemStack toWithdraw = containerStack.split(Math.min(containerStack.getCount(), Math.min(containerStack.getMaxStackSize(), maxToWithdraw)));
                                    golemInv.addItem(toWithdraw);
                                    withdrawnSomething = true;
                                    
                                    golemInv.setChanged();
                                    container.setChanged();
                                    // Stop after one successful withdrawal to allow re-evaluation in tick/shouldContinue
                                    return true;
                                }
                            }
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.AMETHYST && isValidBreedingItem(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setItem(i, remaining);
                        withdrawnSomething = true;
                        if (hasEnoughBreedingItems() || isInventoryFull()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.REDSTONE && containerStack.is(net.minecraft.world.item.Items.REDSTONE)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setItem(i, remaining);
                        withdrawnSomething = true;
                        if (hasRedstone() || isInventoryFull()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.GOLD && containerStack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setItem(i, remaining);
                        withdrawnSomething = true;
                        if (hasGold() || isInventoryFull()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.JUKEBOX && containerStack.get(DataComponents.JUKEBOX_PLAYABLE) != null) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setItem(i, remaining);
                        withdrawnSomething = true;
                        if (hasMusicDisc() || isInventoryFull()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.FURNACE && isFuel(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setItem(i, remaining);
                        withdrawnSomething = true;
                        if (hasFuel() || isInventoryFull()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (UtilityGolem.isHoe(containerStack)) {
                            ItemStack hoe = containerStack.split(1);
                            golem.setHeldItem(hoe);
                            withdrawnSomething = true;
                            if ((hasWaterBucket() || hasEmptyBucket()) && hasSeeds()) {
                                golemInv.setChanged();
                                container.setChanged();
                                return true;
                            }
                        } else if (containerStack.is(net.minecraft.world.item.Items.BUCKET) || containerStack.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
                            if (!hasWaterBucket() && !hasEmptyBucket()) {
                                ItemStack bucket = containerStack.split(1);
                                golem.getInventory().addItem(bucket);
                                withdrawnSomething = true;
                                if (UtilityGolem.isHoe(golem.getHeldItem()) && hasSeeds()) {
                                    golemInv.setChanged();
                                    container.setChanged();
                                    return true;
                                }
                            }
                        } else if (isSeed(containerStack)) {
                             if (!hasSeeds()) {
                                 ItemStack seeds = containerStack.split(Math.min(containerStack.getCount(), 64));
                                 golem.getInventory().addItem(seeds);
                                 withdrawnSomething = true;
                                 if (UtilityGolem.isHoe(golem.getHeldItem()) && (hasWaterBucket() || hasEmptyBucket())) {
                                     golemInv.setChanged();
                                     container.setChanged();
                                     return true;
                                 }
                             }
                        }
                        if (!hasAnythingNeeded()) {
                            golemInv.setChanged();
                            container.setChanged();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.SPONGE && UtilityGolem.isFishingRod(containerStack)) {
                        ItemStack rod = containerStack.split(1);
                        golem.setHeldItem(rod);
                        golemInv.setChanged();
                        container.setChanged();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.DIAMOND && containerStack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                        ItemStack blocks = containerStack.split(Math.min(containerStack.getCount(), 64));
                        golemInv.addItem(blocks);
                        withdrawnSomething = true;
                        golemInv.setChanged();
                        container.setChanged();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.DEEPSLATE) {
                        if (UtilityGolem.isAxe(containerStack) && !hasAxe()) {
                            if (golem.getHeldItem().isEmpty()) {
                                ItemStack tool = containerStack.split(1);
                                golem.setHeldItem(tool);
                                withdrawnSomething = true;
                                golemInv.setChanged();
                                container.setChanged();
                                golem.debugLog("WithdrawItemsGoal: Withdrew axe into hand");
                                return true;
                            } else {
                                // Try adding to golem.getInventory() instead
                                int slot = -1;
                                for (int j = 0; j < golemInv.getContainerSize(); j++) {
                                    if (golemInv.getItem(j).isEmpty()) {
                                        slot = j;
                                        break;
                                    }
                                }
                                if (slot != -1) {
                                    ItemStack tool = containerStack.split(1);
                                    golemInv.setItem(slot, tool);
                                    withdrawnSomething = true;
                                    golemInv.setChanged();
                                    container.setChanged();
                                    golem.debugLog("WithdrawItemsGoal: Withdrew axe into golem.getInventory() slot " + slot);
                                    return true;
                                }
                            }
                        } else if (UtilityGolem.isShears(containerStack) && !hasShears()) {
                            if (golem.getHeldItem().isEmpty()) {
                                ItemStack tool = containerStack.split(1);
                                golem.setHeldItem(tool);
                                withdrawnSomething = true;
                                golemInv.setChanged();
                                container.setChanged();
                                golem.debugLog("WithdrawItemsGoal: Withdrew shears into hand");
                                return true;
                            } else {
                                // Try adding to golem.getInventory() instead
                                int slot = -1;
                                for (int j = 0; j < golemInv.getContainerSize(); j++) {
                                    if (golemInv.getItem(j).isEmpty()) {
                                        slot = j;
                                        break;
                                    }
                                }
                                if (slot != -1) {
                                    ItemStack tool = containerStack.split(1);
                                    golemInv.setItem(slot, tool);
                                    withdrawnSomething = true;
                                    golemInv.setChanged();
                                    container.setChanged();
                                    golem.debugLog("WithdrawItemsGoal: Withdrew shears into golem.getInventory() slot " + slot);
                                    return true;
                                }
                            }
                        } else if (!hasEnoughSaplings() && containerStack.is(net.minecraft.tags.ItemTags.SAPLINGS)) {
                            int currentSaplings = getSaplingCount();
                            int needed = 8 - currentSaplings;
                            if (needed > 0) {
                                ItemStack toWithdraw = containerStack.copy();
                                toWithdraw.setCount(Math.min(needed, containerStack.getCount()));
                                ItemStack remaining = golemInv.addItem(toWithdraw);
                                int withdrawnCount = toWithdraw.getCount() - remaining.getCount();
                                if (withdrawnCount > 0) {
                                    containerStack.shrink(withdrawnCount);
                                    withdrawnSomething = true;
                                    golemInv.setChanged();
                                    container.setChanged();
                                    golem.debugLog("WithdrawItemsGoal: Withdrew " + withdrawnCount + " saplings");
                                    return true;
                                }
                            }
                        }
                        
                        if (hasAxe() && hasShears() && hasEnoughSaplings()) {
                            golem.debugLog("WithdrawItemsGoal: Deepslate golem has all needed items.");
                            return true;
                        }
                        continue;
                    }

                    if ((golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) && UtilityGolem.isSword(containerStack)) {
                        ItemStack sword = containerStack.split(1);
                        golem.setHeldItem(sword);
                        golemInv.setChanged();
                        container.setChanged();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (containerStack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || isIngredient(containerStack) || containerStack.is(net.minecraft.world.item.Items.BLAZE_POWDER) || containerStack.get(DataComponents.POTION_CONTENTS) != null) {
                            // Only withdraw if we have room AND we need it
                            // Try to keep 3 slots for water bottles (total 9 slots in UtilityGolem golem.getInventory())
                            int filledSlots = 0;
                            for (int j = 0; j < golemInv.getContainerSize(); j++) {
                                if (!golemInv.getItem(j).isEmpty()) filledSlots++;
                            }
                            
                            if (filledSlots < 6) { // Max 6 slots for ingredients/powder/bottles
                                ItemStack remaining = transferStack(containerStack, golemInv);
                                container.setItem(i, remaining);
                                withdrawnSomething = true;
                                if (isInventoryFull()) {
                                    golemInv.setChanged();
                                    container.setChanged();
                                    return true;
                                }
                            }
                            continue;
                        }
                    }

                    if (golem.getGolemType() == GolemType.EMERALD) {
                        List<MerchantOffers> allOffers = findNearbyVillagerOffers();
                        if (!allOffers.isEmpty()) {
                            boolean withdrawnSomethingInThisItem = false;
                            for (MerchantOffers offers : allOffers) {
                                for (MerchantOffer offer : offers) {
                                    if (offer.isOutOfStock()) continue;
                                    if (offer.getResult().is(net.minecraft.world.item.Items.EMERALD)) {
                                        ItemCost buyItem1 = offer.getItemCostA();
                                        Optional<ItemCost> buyItem2 = offer.getItemCostB();

                                        if (buyItem1.test(containerStack)) {
                                            int countInInv = getCountInInventory(buyItem1);
                                            int needed = buyItem1.count() - countInInv;
                                            if (needed > 0) {
                                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                                ItemStack remaining = golem.getInventory().addItem(toWithdraw);
                                                if (!remaining.isEmpty()) {
                                                    containerStack.grow(remaining.getCount());
                                                }
                                                withdrawnSomething = true;
                                                withdrawnSomethingInThisItem = true;
                                                if (hasTradeItems() || isInventoryFull()) {
                                                    golemInv.setChanged();
                                                    container.setChanged();
                                                    return true;
                                                }
                                                break;
                                            }
                                        } else if (buyItem2.isPresent() && buyItem2.get().test(containerStack)) {
                                            int countInInv = getCountInInventory(buyItem2.get());
                                            int needed = buyItem2.get().count() - countInInv;
                                            if (needed > 0) {
                                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                                ItemStack remaining = golem.getInventory().addItem(toWithdraw);
                                                if (!remaining.isEmpty()) {
                                                    containerStack.grow(remaining.getCount());
                                                }
                                                withdrawnSomething = true;
                                                withdrawnSomethingInThisItem = true;
                                                if (hasTradeItems() || isInventoryFull()) {
                                                    golemInv.setChanged();
                                                    container.setChanged();
                                                    return true;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (withdrawnSomethingInThisItem) break;
                            }
                            if (withdrawnSomethingInThisItem) continue;
                        }
                        continue;
                    }
                }
                if (withdrawnSomething) {
                    golemInv.setChanged();
                    container.setChanged();
                    return true;
                }
            }
            return false;
        }

        private int getCountInInventory(ItemCost target) {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (target.test(stack)) count += stack.getCount();
            }
            return count;
        }

        private boolean isValidBreedingItem(ItemStack stack) {
            return GolemAI.isValidBreedingItem(stack);
        }

        private ItemStack transferStack(ItemStack stack, Container container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack containerStack = container.getItem(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxStackSize() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).isEmpty()) {
                    container.setItem(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.isSameItemSameComponents(stack, other) && other.getCount() < other.getMaxStackSize();
        }
    }
    public static class DigBlockGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int breakingTime;
        private int maxBreakingTime;
        private int searchCooldown = 0;
        private boolean isAirTarget = false;
        private final List<Direction> failedDirections = new ArrayList<>();

        public DigBlockGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            targetPos = findTargetBlock();
            if (targetPos != null) {
                BlockState state = golem.level().getBlockState(targetPos);
                if (state.is(net.minecraft.world.level.block.Blocks.AIR)) {
                    // Only log if we are far enough to need navigation
                    double distSq = golem.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    if (distSq > 0.1) {
                        golem.debugLog("DigBlockGoal: Navigating to air target at " + targetPos.toShortString());
                    }
                    this.maxBreakingTime = 1;
                    this.isAirTarget = true;
                    return true;
                }
                
                this.isAirTarget = false;
                golem.debugLog("DigBlockGoal: Found target block " + state.getBlock().getDescriptionId() + " at " + targetPos.toShortString());
                ItemStack tool = golem.getHeldItem();
                // Ensure we have the right tool held or can swap to it
                boolean needsPickaxe = state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER)
                        || state.is(net.minecraft.tags.BlockTags.COAL_ORES) || state.is(net.minecraft.tags.BlockTags.IRON_ORES) || state.is(net.minecraft.tags.BlockTags.COPPER_ORES)
                        || state.is(net.minecraft.tags.BlockTags.GOLD_ORES) || state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES) || state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)
                        || state.is(net.minecraft.tags.BlockTags.LAPIS_ORES) || state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES);
                boolean needsShovel = state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.SAND) || state.is(net.minecraft.world.level.block.Blocks.GRAVEL);

            if (needsPickaxe && !UtilityGolem.isPickaxe(tool)) {
                // Try to swap immediately if possible
                if (hasPickaxe()) {
                    // We'll recalculate the actual breaking time in tick() after the swap.
                    this.maxBreakingTime = calculateBreakingTime(getBestAvailableTool(UtilityGolem::isPickaxe), targetPos);
                    return true;
                }
                
                // Lapis golems can dig common blocks even without tools
                if (golem.getGolemType() == GolemType.LAPIS && canDig(targetPos)) {
                    this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                    return true;
                }
            } else if (needsShovel && !UtilityGolem.isShovel(tool)) {
                if (hasShovel()) {
                    this.maxBreakingTime = calculateBreakingTime(getBestAvailableTool(UtilityGolem::isShovel), targetPos);
                    return true;
                }

                // Lapis golems can dig common blocks even without tools
                if (golem.getGolemType() == GolemType.LAPIS && canDig(targetPos)) {
                    this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                    return true;
                }
            } else if (!tool.isEmpty() && (UtilityGolem.isPickaxe(tool) || UtilityGolem.isShovel(tool))) {
                // If we already have a tool, make sure it's the BEST one we have
                if (UtilityGolem.isPickaxe(tool)) {
                    ItemStack best = getBestAvailableTool(UtilityGolem::isPickaxe);
                    this.maxBreakingTime = calculateBreakingTime(best, targetPos);
                } else if (UtilityGolem.isShovel(tool)) {
                    ItemStack best = getBestAvailableTool(UtilityGolem::isShovel);
                    this.maxBreakingTime = calculateBreakingTime(best, targetPos);
                } else {
                    this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                }
                return true;
            } else if (golem.getGolemType() == GolemType.LAPIS && canDig(targetPos)) {
                // Fallback for lapis golem for any other block it's allowed to dig
                this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                return true;
            }
            } else {
                searchCooldown = 20;
            }
            return false;
        }

        private int calculateBreakingTime(ItemStack tool, BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            float hardness = state.getDestroySpeed(golem.level(), pos);
            if (hardness < 0) return 200; // Unbreakable

            float speed = 1.0f;
            if (tool != null && !tool.isEmpty()) {
                speed = tool.getDestroySpeed(state);
                
                // If the tool is efficient against this block, apply efficiency enchantment
                if (speed > 1.0f && golem.level() instanceof ServerLevel serverLevel) {
                    int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), tool);
                    if (efficiencyLevel > 0) {
                        speed += (float)(efficiencyLevel * efficiencyLevel + 1);
                    }
                }
            }

            // Player mining formula is roughly (hardness * 30) / speed if using correct tool
            // We want it slower than player, so let's use a higher multiplier
            return Math.max(20, (int) (hardness * 60 / speed));
        }


        private BlockPos findTargetBlock() {
            BlockPos pos = golem.blockPosition();
            BlockPos chestPos = golem.getChestPos();
            Level world = golem.level();

            // 1. Prioritize Visible Ores
            int oreRange = 12;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            double minOreDistSq = Double.MAX_VALUE;
            BlockPos bestOre = null;

            for (int x = -oreRange; x <= oreRange; x++) {
                for (int y = -oreRange; y <= oreRange; y++) {
                    for (int z = -oreRange; z <= oreRange; z++) {
                        mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (golem.isBlacklisted(mutable)) continue;
                        
                        boolean isOre = isOre(mutable);
                        if (isOre && canDig(mutable)) {
                            // Lapis golems should only prioritize ores that are not too far above them during staircase mining
                            if (golem.getGolemType() == GolemType.LAPIS && mutable.getY() > pos.getY() + 3) continue;

                            if (chestPos == null || mutable.distToLowCornerSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
                                // Prevent digging under feet
                                if (golem.getGolemType() == GolemType.LAPIS && mutable.getY() == pos.getY() - 1 && mutable.getX() == pos.getX() && mutable.getZ() == pos.getZ()) continue;
                                
                                double distSq = mutable.distSqr(pos);
                                if (distSq < minOreDistSq) {
                                    minOreDistSq = distSq;
                                    bestOre = mutable.immutable();
                                }
                            }
                        } else if (golem.getGolemType() == GolemType.LAPIS && isOre) {
                            // If it's an ore but we can't dig it (because it's hidden), check if we can dig the block above it
                            BlockPos above = mutable.above();
                            if (canDig(above)) {
                                double distSq = above.distSqr(pos);
                                if (distSq < minOreDistSq) {
                                    minOreDistSq = distSq;
                                    bestOre = above.immutable();
                                }
                            }
                        }
                    }
                }
            }

            if (golem.getGolemType() == GolemType.LAPIS && bestOre != null) {
                // Visibility check for ores is a bit expensive, but we want to make sure it's actually mineable
                if (canDig(bestOre)) {
                    golem.debugLog("DigBlockGoal: Found visible ore at " + bestOre.toShortString());
                    return bestOre;
                }
            }

            // 2. Staircase/Tunnel Logic for Lapis
            if (golem.getGolemType() == GolemType.LAPIS) {
                BlockPos target = findStaircaseOrTunnelBlock();
                if (target != null) {
                    BlockState targetState = world.getBlockState(target);
                    // If target is stone but we can't dig it, it might be too far
                    if (!targetState.is(net.minecraft.world.level.block.Blocks.AIR) && !canDig(target)) {
                        golem.debugLog("DigBlockGoal: Lapis cannot dig staircase target " + targetState.getBlock().getDescriptionId() + " at " + target.toShortString() + " (missing tool?)");
                    } else {
                        golem.debugLog("DigBlockGoal: Lapis staircase/tunnel target " + target.toShortString());
                    }
                }
                return target;
            }

            // 3. General digging for other types
            int range = 15;
            double minTargetDistSq = Double.MAX_VALUE;
            BlockPos bestTarget = null;

            for (int x = -8; x <= 8; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -8; z <= 8; z++) {
                        mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (golem.isBlacklisted(mutable)) continue;
                        if (canDig(mutable)) {
                            // Non-lapis golems (or lapis in general mode) should also stay within a reasonable height relative to feet
                            if (mutable.getY() > pos.getY() + 2) continue;

                            if (chestPos == null || mutable.distToLowCornerSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                double distSq = mutable.distSqr(pos);
                                if (distSq < minTargetDistSq) {
                                    minTargetDistSq = distSq;
                                    bestTarget = mutable.immutable();
                                }
                            }
                        }
                    }
                }
            }

            return bestTarget;
        }

        private boolean isOre(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            if (!(state.is(net.minecraft.tags.BlockTags.COAL_ORES) || state.is(net.minecraft.tags.BlockTags.IRON_ORES) || state.is(net.minecraft.tags.BlockTags.COPPER_ORES)
                    || state.is(net.minecraft.tags.BlockTags.GOLD_ORES) || state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES) || state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)
                    || state.is(net.minecraft.tags.BlockTags.LAPIS_ORES) || state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES)
                    || state.is(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE)
                    || state.is(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS))) {
                return false;
            }

            // Visibility check: is it adjacent to an air block or a non-opaque block?
            // Optimization: check common directions first
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutable.set(pos.relative(direction));
                BlockState neighborState = golem.level().getBlockState(mutable);
                if (neighborState.is(net.minecraft.world.level.block.Blocks.AIR) || neighborState.is(net.minecraft.tags.BlockTags.REPLACEABLE) || !neighborState.isCollisionShapeFullBlock(golem.level(), mutable)) {
                    return true;
                }
            }
            return false;
        }

        private BlockPos findStaircaseOrTunnelBlock() {
            BlockPos pos = golem.blockPosition();
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) {
                chestPos = findNearbyChest();
            }
            if (chestPos == null) {
                golem.debugLog("Lapis: No chest assigned, cannot staircase");
                return null;
            }

            // Use a consistent direction for the staircase based on the chest position
            Direction facing = golem.getMiningDirection();
            if (facing == null) {
                if (Math.abs(pos.getX() - chestPos.getX()) > Math.abs(pos.getZ() - chestPos.getZ())) {
                    facing = pos.getX() > chestPos.getX() ? Direction.EAST : Direction.WEST;
                } else {
                    facing = pos.getZ() > chestPos.getZ() ? Direction.SOUTH : Direction.NORTH;
                }
                golem.setMiningDirection(facing);
                golem.debugLog("Lapis: Locking mining direction to " + facing);
            }

            // Locked coordinate to keep it straight (the axis perpendicular to mining direction)
            int lockedCoord = (facing.getAxis() == net.minecraft.core.Direction.Axis.Z) ? chestPos.getX() : chestPos.getZ();

            // The staircase starts ONE block away from the chest.
            BlockPos startPos = chestPos.relative(facing);

            // Distance from startPos along the mining direction
            int directionalDist = switch (facing) {
                case EAST -> pos.getX() - startPos.getX();
                case WEST -> startPos.getX() - pos.getX();
                case SOUTH -> pos.getZ() - startPos.getZ();
                case NORTH -> startPos.getZ() - pos.getZ();
                default -> 0;
            };

            // Check if any block in our path is fluid (water/lava)
            for (int d = 0; d <= directionalDist + 2; d++) {
                BlockPos pathMiddle = (facing.getAxis() == net.minecraft.core.Direction.Axis.X)
                        ? new BlockPos(startPos.getX() + facing.getStepX() * d, chestPos.getY() - d, lockedCoord)
                        : new BlockPos(lockedCoord, chestPos.getY() - d, startPos.getZ() + facing.getStepZ() * d);
                
                for (int yOffset = 0; yOffset <= 2; yOffset++) {
                    BlockPos p = pathMiddle.above(yOffset);
                    if (p.getY() < -64) continue;
                    BlockState state = golem.level().getBlockState(p);
                    if (!state.getFluidState().isEmpty()) {
                        golem.debugLog("Lapis: Path blocked by fluid at " + p.toShortString() + ". Retrying in new direction.");
                        failedDirections.add(facing);
                        Direction newFacing = null;
                        for (Direction dir : Direction.values()) {
                            if (!failedDirections.contains(dir) && dir != facing.getOpposite()) {
                                newFacing = dir;
                                break;
                            }
                        }
                        if (newFacing == null) {
                            // If all directions failed, at least try another one or reset
                            failedDirections.clear();
                            newFacing = facing.getClockWise();
                        }
                        golem.setMiningDirection(newFacing);
                        return null; // Stop this goal, it will restart in new direction next tick
                    }
                }
            }

            // Target depth for the tunnel
            int targetDepthY = -54;

            // Calculate the intended Y level for the golem's current position.
            // At directionalDist = 0 (startPos), intendedY = chestPos.getY().
            // For every 1 block forward, we drop 1 block in Y.
            int intendedY = chestPos.getY() - Math.max(0, directionalDist);
            if (intendedY < targetDepthY) intendedY = targetDepthY;

            // Check horizontal alignment (are we on the locked line?)
            double distToLine = (facing.getAxis() == net.minecraft.core.Direction.Axis.Z)
                    ? Math.abs(golem.getX() - (lockedCoord + 0.5))
                    : Math.abs(golem.getZ() - (lockedCoord + 0.5));

            // Only align horizontally to the locked line; do NOT dig straight down to match intendedY.
            if (distToLine > 0.4) {
                BlockPos targetOnLine = (facing.getAxis() == net.minecraft.core.Direction.Axis.Z)
                        ? new BlockPos(lockedCoord, pos.getY(), pos.getZ())
                        : new BlockPos(pos.getX(), pos.getY(), lockedCoord);

                for (int yOffset = 2; yOffset >= 0; yOffset--) {
                    BlockPos p = targetOnLine.above(yOffset);
                    BlockState state = golem.level().getBlockState(p);
                    if (canDig(p) && !state.is(net.minecraft.world.level.block.Blocks.AIR)) {
                        if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                            continue;
                        }
                        // Prioritize ores even if they are part of the staircase/tunnel alignment
                        if (isOre(p)) {
                            golem.debugLog("Lapis: Found ore during alignment at " + p.toShortString());
                            return p;
                        }
                        golem.debugLog("Lapis: Aligning horizontally to staircase line, digging at " + p.toShortString());
                        return p;
                    }
                }

                if (distToLine > 0.1) {
                    golem.debugLog("Lapis: Aligning horizontally to staircase line, moving to " + targetOnLine.toShortString());
                    return targetOnLine;
                }
            }

            // We are aligned and at the correct height. Now find the next block(s) to dig.
            // If we are at target depth, dig a tunnel ahead.
            if (pos.getY() <= targetDepthY) {
                BlockPos ahead = pos.relative(facing);
                for (int yOffset = 2; yOffset >= 0; yOffset--) {
                    BlockPos p = ahead.above(yOffset);
                    BlockState state = golem.level().getBlockState(p);
                    if (canDig(p) && !state.is(net.minecraft.world.level.block.Blocks.AIR)) {
                        if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                            continue;
                        }
                        // Prioritize ores in the tunnel
                        if (isOre(p)) {
                            golem.debugLog("Lapis: Found ore in tunnel at " + p.toShortString());
                            return p;
                        }
                        golem.debugLog("Lapis: Tunnel digging at " + p.toShortString());
                        return p;
                    }
                }
                if (distToLine > 0.2 || true) {
                    return ahead;
                }
                return null;
            }

            // Otherwise, dig the next step down in the staircase.
            // Choose a forward distance so the next step is at most 1 block below our current height.
            int baseDist = Math.max(0, directionalDist);
            int nextDist = baseDist + 1;

            // If we are high above the staircase height for nextDist, we should dig down at our current location
            // to reach the staircase slope, rather than jumping ahead.
            if (pos.getY() > chestPos.getY() - baseDist) {
                // Check if we are at surface and trying to find the entrance
                // If we are more than 2 blocks above the intended staircase height,
                // try to find the entrance (the point where the staircase actually starts dropping)
                if (pos.getY() > chestPos.getY() - baseDist + 2) {
                    for (int d = 0; d <= 256; d++) {
                        BlockPos p = startPos.relative(facing, d);
                        int intendedYForD = chestPos.getY() - d;
                        if (intendedYForD < targetDepthY) intendedYForD = targetDepthY;
                        
                        // Check if the staircase exists at this distance (at least 2 blocks high air/replaceable)
                        BlockPos pIntended = p.atY(intendedYForD);
                        if (golem.level().getBlockState(pIntended).is(net.minecraft.world.level.block.Blocks.AIR) && 
                            golem.level().getBlockState(pIntended.above()).is(net.minecraft.world.level.block.Blocks.AIR)) {
                            golem.debugLog("Lapis: Found existing staircase entrance at distance " + d + ", moving to " + pIntended.toShortString());
                            return pIntended;
                        }
                        
                        // If we hit a solid block where the staircase should be, stop searching
                        if (!golem.level().getBlockState(pIntended).is(net.minecraft.world.level.block.Blocks.AIR) && d > directionalDist) {
                           break;
                        }
                    }
                }
                
                // Target a block directly below or one step ahead at posY - 1
                nextDist = baseDist;
            }

            // Limit nextDist so we don't target something too far away
            if (nextDist > baseDist + 2) {
                nextDist = baseDist + 1;
            }

            // If we are already close enough to nextDist, move one further
            if (directionalDist > nextDist - 0.5) {
                nextDist++;
            }

            int nextY = Math.max(targetDepthY, chestPos.getY() - nextDist);
            BlockPos nextStepMiddle = (facing.getAxis() == net.minecraft.core.Direction.Axis.X)
                    ? new BlockPos(startPos.getX() + facing.getStepX() * nextDist, nextY, lockedCoord)
                    : new BlockPos(lockedCoord, nextY, startPos.getZ() + facing.getStepZ() * nextDist);

            // Reconstruct the floor of the staircase if it's missing or if it's a non-solid block (like a torch)
            // that shouldn't be here. This ensures pathfinding works.
            BlockPos floorPos = nextStepMiddle.below();
            BlockState floorState = golem.level().getBlockState(floorPos);
            if (!floorState.is(net.minecraft.world.level.block.Blocks.AIR) && !floorState.canBeReplaced() && !floorState.isCollisionShapeFullBlock(golem.level(), floorPos)) {
                // If there's something like a slab or stair, we should replace it with a full block
                golem.debugLog("Lapis: Non-full block at floor " + floorPos.toShortString() + ", clearing it to reconstruct");
                return floorPos;
            }

            if (floorState.is(net.minecraft.world.level.block.Blocks.AIR) || floorState.canBeReplaced()) {
                 // Prioritize clearing the space above it first, but if it's already clear enough to see the floor...
                 if (golem.level().getBlockState(nextStepMiddle).is(net.minecraft.world.level.block.Blocks.AIR) && 
                     golem.level().getBlockState(nextStepMiddle.above()).is(net.minecraft.world.level.block.Blocks.AIR)) {
                     golem.debugLog("Lapis: Staircase floor broken at " + floorPos.toShortString() + ", attempting to reconstruct");
                     this.isAirTarget = true;
                     return floorPos;
                 }
            }

            // Clear the 3-high path for the next step.
            for (int yOffset = 2; yOffset >= 0; yOffset--) {
                BlockPos p = nextStepMiddle.above(yOffset);
                BlockState state = golem.level().getBlockState(p);
                if (canDig(p) && !state.is(net.minecraft.world.level.block.Blocks.AIR)) {
                    // Skip light sources
                    if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                        continue;
                    }
                    // Prioritize ores in the staircase
                    if (isOre(p)) {
                        golem.debugLog("Lapis: Found ore in staircase at " + p.toShortString());
                        return p;
                    }
                    golem.debugLog("Lapis: Staircase digging next step at " + p.toShortString());
                    return p;
                }
            }

            // If next step is already clear, return it to navigate there.
            if (nextDist > directionalDist + 0.4 || distToLine > 0.2) {
                return nextStepMiddle;
            }
            
            // We'll increment directionalDist slightly to find the next actual block in the next AI tick
            return null;
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private boolean tryPlaceStepUp() {
            if (golem.getInventory().isEmpty()) {
                golem.debugLog("Lapis: Cannot place step up - golem.getInventory() empty");
                return false;
            }
            
            BlockPos pos = golem.blockPosition();
            Level world = golem.level();
            
            // If we are in a 1x1 hole or a corner (surrounded by at least 2 blocks) and the target is above us
            if (targetPos != null && targetPos.getY() > pos.getY()) {
                // Check if we are partially surrounded horizontally
                int blockedSides = 0;
                for (Direction dir : Direction.values()) {
                    BlockPos p = pos.relative(dir);
                    BlockState s = world.getBlockState(p);
                    if (!s.is(net.minecraft.world.level.block.Blocks.AIR) && !s.is(net.minecraft.tags.BlockTags.REPLACEABLE) && s.isCollisionShapeFullBlock(world, p)) {
                        blockedSides++;
                    }
                }

                // If at least 2 sides are blocked, and the space above us is clear, we can try to tower up
                if (blockedSides >= 2) {
                    BlockState state = world.getBlockState(pos);
                    BlockState stateUp = world.getBlockState(pos.above(2)); // Head space (assuming 2 blocks high)
                    if ((state.is(net.minecraft.world.level.block.Blocks.AIR) || state.canBeReplaced()) && (stateUp.is(net.minecraft.world.level.block.Blocks.AIR) || stateUp.canBeReplaced())) {
                        golem.debugLog("Lapis: In a confined space (" + blockedSides + " sides blocked), towering up at " + pos.toShortString());
                        return placeBlockFromInventory(pos);
                    }
                }
            }

            // Lapis golem staircase reconstruction logic:
            // If we're a lapis golem and are 'stuck' trying to reach a target,
            // check if the block below us is missing relative to where it should be.
            if (golem.getGolemType() == GolemType.LAPIS) {
                BlockPos under = pos.below();
                BlockState underState = world.getBlockState(under);
                if (underState.is(net.minecraft.world.level.block.Blocks.AIR) || underState.canBeReplaced() || !underState.isCollisionShapeFullBlock(world, under)) {
                    golem.debugLog("Lapis: Staircase broken below feet (" + underState.getBlock().getDescriptionId() + "), attempting to reconstruct at " + under.toShortString());
                    return placeBlockFromInventory(under);
                }
                
                // Also check if we're just stuck in front of a 1-block gap we can't jump
                Direction facing = golem.getMiningDirection();
                if (facing != null) {
                    BlockPos ahead = pos.relative(facing);
                    BlockPos aheadUnder = ahead.below();
                    BlockState aheadUnderState = world.getBlockState(aheadUnder);
                    if (aheadUnderState.is(net.minecraft.world.level.block.Blocks.AIR) || aheadUnderState.canBeReplaced()) {
                        golem.debugLog("Lapis: Gap detected ahead (" + aheadUnder.toShortString() + "), attempting to fill");
                        return placeBlockFromInventory(aheadUnder);
                    }
                }
            }

            return false;
        }

        private boolean placeBlockFromInventory(BlockPos pos) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    // Prefer dirt or cobblestone-like blocks
                    if (stack.is(net.minecraft.tags.ItemTags.DIRT) || stack.is(net.minecraft.world.item.Items.COBBLESTONE) || stack.is(net.minecraft.world.item.Items.COBBLED_DEEPSLATE) || stack.is(net.minecraft.world.item.Items.STONE) || stack.is(net.minecraft.world.item.Items.DEEPSLATE)) {
                        
                        // Hold the block before placing
                        ItemStack currentHeld = golem.getHeldItem();
                        ItemStack toHold = inv.removeItem(i, 1);
                        golem.setHeldItem(toHold);

                        if (golem.level().setBlock(pos, block.defaultBlockState(), 3)) {
                            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                            golem.level().playSound(null, pos, block.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            
                            // Swap back to tool if we had one
                            if (!currentHeld.isEmpty()) {
                                golem.setHeldItem(currentHeld);
                            } else {
                                golem.setHeldItem(ItemStack.EMPTY);
                            }

                            // Teleport golem slightly up if we placed under feet
                            if (pos.equals(golem.blockPosition())) {
                                golem.setPos(golem.getX(), golem.getY() + 1.1, golem.getZ());
                                golem.getJumpControl().jump();
                                golem.setDeltaMovement(golem.getDeltaMovement().add(0, 0.2, 0));
                            }
                            
                            // Reset navigation so it recalculates its path
                            golem.getNavigation().stop();
                            
                            return true;
                        } else {
                            // Failed to place, put it back or drop it
                            ItemStack remaining = inv.addItem(toHold);
                            if (!remaining.isEmpty()) {
                                golem.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(golem.level(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                            }
                            golem.setHeldItem(currentHeld);
                        }
                    }
                }
            }
            return false;
        }

        private boolean canDig(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            if (!state.getFluidState().isEmpty()) return false;
            
            boolean isOre = isOre(pos);

            if (golem.getGolemType() == GolemType.LAPIS) {
                if (UtilityGolem.isLightSource(state)) return false;
                if (state.is(net.minecraft.world.level.block.Blocks.AIR) || state.is(net.minecraft.tags.BlockTags.REPLACEABLE)) return true;
                
                // Lapis golems can dig common blocks even without tools, but it's slower
                if (state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER)
                        || state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.SAND) || state.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                        || state.is(net.minecraft.world.level.block.Blocks.NETHERRACK) || state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND) || state.is(net.minecraft.world.level.block.Blocks.SOUL_SOIL)) {
                    return true;
                }
                
                // Lapis golems can dig ores if they have a pickaxe that is SUFFICIENT for the ore
                if (isOre) {
                    return hasSufficientPickaxe(state);
                }
            }
            if (state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER)
                    || isOre
                    || state.is(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE)
                    || state.is(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS)) {
                return hasSufficientPickaxe(state);
            }
            if (state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.SAND) || state.is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
                return hasShovel();
            }
            // Add general check for very soft blocks like grass
            if (state.getDestroySpeed(golem.level(), pos) <= 0.2f) return true;
            
            return false;
        }

        private boolean hasSufficientPickaxe(BlockState state) {
            ItemStack held = golem.getHeldItem();
            if (UtilityGolem.isPickaxe(held) && held.isCorrectToolForDrops(state)) return true;
            
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (UtilityGolem.isPickaxe(stack) && stack.isCorrectToolForDrops(state)) return true;
            }
            return false;
        }

        private boolean isShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShovel(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasPickaxe() {
            if (UtilityGolem.isPickaxe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isPickaxe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShovel(inv.getItem(i))) return true;
            }
            return false;
        }

        @Override
        public void start() {
            breakingTime = 0;
            if (isAirTarget) {
                golem.setAnimation(GolemAnimation.IDLE, 0);
            } else {
                golem.setAnimation(GolemAnimation.DIGGING, Math.min(100, Math.max(40, this.maxBreakingTime)));
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && canDig(targetPos) &&
                    breakingTime < maxBreakingTime && golem.blockPosition().distSqr(targetPos) < 400;
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.level().destroyBlockProgress(golem.getId(), targetPos, -1);
            }
            targetPos = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        private int stuckTicks = 0;
        private int loopCounter = 0;
        private Vec3 lastPos = net.minecraft.world.phys.Vec3.ZERO;
        private BlockPos lastTargetPos = null;

        private BlockPos findSafePosAround(BlockPos chestPos) {
            Level world = golem.level();
            for (int i = 0; i < 10; ++i) {
                int x = chestPos.getX() + golem.getRandom().nextInt(5) - 2;
                int y = chestPos.getY();
                int z = chestPos.getZ() + golem.getRandom().nextInt(5) - 2;
                BlockPos p = new BlockPos(x, y, z);
                if (world.getBlockState(p).is(net.minecraft.world.level.block.Blocks.AIR) && world.getBlockState(p.above()).is(net.minecraft.world.level.block.Blocks.AIR)) {
                    return p;
                }
            }
            return chestPos.above();
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            BlockState targetState = golem.level().getBlockState(targetPos);

            // Ensure animation is active while digging
            if (!isAirTarget && (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1)) {
                golem.setAnimation(GolemAnimation.DIGGING, 40);
            }

            // stuck check
            Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
            if (currentPos.distanceToSqr(lastPos) < 0.001) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }
            lastPos = currentPos;

            if (stuckTicks > 80) { // Slightly more aggressive than 100
                golem.debugLog("DigBlockGoal: Stuck at " + golem.blockPosition().toShortString() + " trying to reach " + targetPos.toShortString());
                
                // If we've been stuck for a very long time, try to teleport back to the chest or clear the goal
                if (stuckTicks > 300) {
                    BlockPos chestPos = golem.getChestPos();
                    if (chestPos != null) {
                        // Try to find a safe spot for teleport
                        golem.debugLog("DigBlockGoal: Extremely stuck, teleporting to chest.");
                        BlockPos safePos = findSafePosAround(chestPos);
                        golem.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                        
                        if (golem.getGolemType() == GolemType.LAPIS) {
                            if (targetPos.equals(lastTargetPos)) {
                                loopCounter++;
                                golem.debugLog("Lapis: Stuck on same target " + loopCounter + " times");
                            } else {
                                lastTargetPos = targetPos;
                                loopCounter = 1;
                            }
                            
                            if (loopCounter >= 3) {
                                golem.debugLog("Lapis: Stuck loop detected, forcing new direction");
                                Direction current = golem.getMiningDirection();
                                if (current != null) {
                                    failedDirections.add(current);
                                    Direction newFacing = null;
                                    for (Direction dir : Direction.values()) {
                                        if (!failedDirections.contains(dir) && dir != current.getOpposite()) {
                                            newFacing = dir;
                                            break;
                                        }
                                    }
                                    if (newFacing == null) {
                                        failedDirections.clear();
                                        newFacing = current.getClockWise();
                                    }
                                    golem.setMiningDirection(newFacing);
                                    loopCounter = 0;
                                }
                            }
                        }
                    }
                    stuckTicks = 0;
                    targetPos = null;
                    return;
                }

                if (golem.getGolemType() == GolemType.LAPIS) {
                    if (tryPlaceStepUp()) {
                        golem.debugLog("Lapis: Attempting to place step up");
                        stuckTicks = 0;
                        return;
                    }
                    
                    // Try to dig our way out if stuck in a cave
                    BlockPos above = golem.blockPosition().above(2);
                    if (canDig(above)) {
                        BlockState state = golem.level().getBlockState(above);
                        if (!state.is(net.minecraft.world.level.block.Blocks.AIR) && !UtilityGolem.isLightSource(state)) {
                            golem.debugLog("Lapis: Stuck in cave? Digging out at " + above.toShortString());
                            targetPos = above;
                            maxBreakingTime = calculateBreakingTime(golem.getHeldItem(), targetPos);
                            breakingTime = 0;
                            stuckTicks = 0;
                            return;
                        }
                    }
                }
                
                if (stuckTicks > 120) { // If still stuck after trying to place
                    golem.debugLog("DigBlockGoal: Blacklisting unreachable block at " + targetPos.toShortString());
                    golem.blacklistPosition(targetPos);
                    stop();
                    return;
                }
            }

            // Auto-switch tool
            ItemStack currentHeld = golem.getHeldItem();
            boolean needsPickaxe = targetState.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE);
            boolean needsShovel = targetState.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL);

            if (needsPickaxe && !UtilityGolem.isPickaxe(currentHeld)) {
                swapTool(UtilityGolem::isPickaxe);
            } else if (needsShovel && !UtilityGolem.isShovel(currentHeld)) {
                swapTool(UtilityGolem::isShovel);
            }

            double dx = golem.getX() - (targetPos.getX() + 0.5);
            double dy = golem.getY() - (targetPos.getY() + 0.5);
            double dz = golem.getZ() - (targetPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            double reachDistSq = golem.getGolemType() == GolemType.LAPIS ? 16.0D : 9.0D;
            double reachVertical = golem.getGolemType() == GolemType.LAPIS ? 4.0D : 2.0D;

            if (horizontalDistSq > reachDistSq || verticalDist > reachVertical) {
                // If it's high up or far below, move to the XZ position at our current height
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // Lapis golems always try to move to the exact targetPos for better staircase navigation
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    }

                    if (!possible) {
                        golem.blacklistPosition(targetPos);
                        stop();
                        return;
                    }
                }
                breakingTime = 0;
            } else if (isAirTarget) {
                // If it's air and we're close, we've "reached" it
                // For navigation targets, 0.2 is the threshold used in findTargetBlock
                Direction facing = golem.getMiningDirection();
                double alignmentDist = (facing != null && facing.getAxis() == net.minecraft.core.Direction.Axis.X)
                        ? Math.abs(golem.getZ() - (targetPos.getZ() + 0.5))
                        : Math.abs(golem.getX() - (targetPos.getX() + 0.5));
                
                double directionalDist = (facing != null)
                        ? (facing.getAxis() == net.minecraft.core.Direction.Axis.X)
                                ? Math.abs(golem.getX() - (targetPos.getX() + 0.5))
                                : Math.abs(golem.getZ() - (targetPos.getZ() + 0.5))
                        : 0;

                // If it's the exact same block we're in, we've definitely reached it
                // We also check if we are aligned AND close enough to the target on the mining axis
                if (alignmentDist <= 0.2 && directionalDist <= 0.4 || targetPos.equals(golem.blockPosition())) {
                    golem.debugLog("DigBlockGoal: Reached air alignment target " + targetPos.toShortString() + " (align: " + alignmentDist + ", dir: " + directionalDist + ")");
                    stop();
                    return;
                }
                
                // Keep moving to it
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                // Swing arm every 5 ticks
                if (!isAirTarget && breakingTime % 5 == 0) {
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }

                breakingTime++;
                int progress = (int) ((float) breakingTime / (float) maxBreakingTime * 10.0F);
                golem.level().destroyBlockProgress(golem.getId(), targetPos, progress);

                if (breakingTime >= maxBreakingTime) {
                    if (golem.level().getBlockState(targetPos).is(net.minecraft.world.level.block.Blocks.AIR)) {
                        stop();
                    } else {
                        breakBlock();
                    }
                }
            }
        }

        private int getToolScore(ItemStack stack, BlockPos target) {
            if (stack.isEmpty()) return 0;
            int score = 0;
            if (golem.level() instanceof ServerLevel serverLevel) {
                var registry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                
                // Efficiency is always good
                score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), stack) * 10;
                
                BlockState state = serverLevel.getBlockState(target);
                boolean isOre = state.is(net.minecraft.tags.BlockTags.COAL_ORES) || state.is(net.minecraft.tags.BlockTags.IRON_ORES) || state.is(net.minecraft.tags.BlockTags.COPPER_ORES)
                        || state.is(net.minecraft.tags.BlockTags.GOLD_ORES) || state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES) || state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)
                        || state.is(net.minecraft.tags.BlockTags.LAPIS_ORES) || state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES)
                        || state.is(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE) || state.is(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS);
                
                if (isOre) {
                    // Fortune is great for ores
                    score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE), stack) * 50;
                    // Silk Touch is also good for some ores (e.g. coal, diamond if you want the block)
                    score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), stack) * 30;
                } else {
                    // For stone/dirt, Silk Touch might be preferred (e.g. Grass block)
                    if (state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                        score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), stack) * 50;
                    }
                }
            }
            
            // Material score
            if (stack.is(net.minecraft.world.item.Items.NETHERITE_PICKAXE) || stack.is(net.minecraft.world.item.Items.NETHERITE_SHOVEL)) score += 500;
            else if (stack.is(net.minecraft.world.item.Items.DIAMOND_PICKAXE) || stack.is(net.minecraft.world.item.Items.DIAMOND_SHOVEL)) score += 400;
            else if (stack.is(net.minecraft.world.item.Items.IRON_PICKAXE) || stack.is(net.minecraft.world.item.Items.IRON_SHOVEL)) score += 300;
            else if (stack.is(net.minecraft.world.item.Items.GOLDEN_PICKAXE) || stack.is(net.minecraft.world.item.Items.GOLDEN_SHOVEL)) score += 600; // Gold is fast
            else if (stack.is(net.minecraft.world.item.Items.STONE_PICKAXE) || stack.is(net.minecraft.world.item.Items.STONE_SHOVEL)) score += 200;
            else if (stack.is(net.minecraft.world.item.Items.WOODEN_PICKAXE) || stack.is(net.minecraft.world.item.Items.WOODEN_SHOVEL)) score += 100;
            else if (stack.is(net.minecraft.world.item.Items.COPPER_PICKAXE) || stack.is(net.minecraft.world.item.Items.COPPER_SHOVEL)) score += 250;
            
            return score;
        }

        private ItemStack getBestAvailableTool(java.util.function.Predicate<ItemStack> toolPredicate) {
            ItemStack best = golem.getHeldItem();
            int bestScore = toolPredicate.test(best) ? getToolScore(best, targetPos) : -1;
            
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (toolPredicate.test(stack)) {
                    int score = getToolScore(stack, targetPos);
                    if (score > bestScore) {
                        bestScore = score;
                        best = stack;
                    }
                }
            }
            return best;
        }

        private void swapTool(java.util.function.Predicate<ItemStack> toolPredicate) {
            SimpleContainer inv = golem.getInventory();
            ItemStack currentHeld = golem.getHeldItem();
            BlockState targetState = golem.level().getBlockState(targetPos);
            
            int bestSlot = -1;
            int bestScore = (toolPredicate.test(currentHeld) && currentHeld.isCorrectToolForDrops(targetState)) ? getToolScore(currentHeld, targetPos) : -1;
            
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (toolPredicate.test(stack) && stack.isCorrectToolForDrops(targetState)) {
                    int score = getToolScore(stack, targetPos);
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = i;
                    }
                }
            }
            
            if (bestSlot != -1) {
                ItemStack newTool = inv.removeItem(bestSlot, 1);
                if (!currentHeld.isEmpty()) {
                    ItemStack remaining = inv.addItem(currentHeld);
                    if (!remaining.isEmpty()) {
                        golem.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(golem.level(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                    }
                }
                golem.setHeldItem(newTool);
                // Recalculate maxBreakingTime for the new tool
                this.maxBreakingTime = calculateBreakingTime(newTool, targetPos);
            }
        }

        private void breakBlock() {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;

            BlockState state = serverLevel.getBlockState(targetPos);
            if (canDig(targetPos)) {
                ItemStack tool = golem.getHeldItem();
                
                net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(targetPos))
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, tool)
                        .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, golem);

                serverLevel.destroyBlock(targetPos, false, golem, 512);

                List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, targetPos, null, golem, tool);
                for (ItemStack drop : drops) {
                    ItemStack remaining = golem.getInventory().addItem(drop);
                    if (!remaining.isEmpty()) {
                        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(serverLevel, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, remaining);
                        itemEntity.setDefaultPickUpDelay();
                        serverLevel.addFreshEntity(itemEntity);
                    }
                }

                if (!tool.isEmpty()) {
                    if (UtilityGolem.isPickaxe(tool) || UtilityGolem.isShovel(tool)) {
                        tool.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                    }
                }
            }
            targetPos = null;
        }
    }
    public static class FarmGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int farmActionTime;
        private static final int MAX_FARM_ACTION_TIME = 20;

        public FarmGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.BAMBOO) return false;
            
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) {
                // If we don't have a chest, try to find one.
                // This allows the golem to start farming as soon as a chest is placed.
                chestPos = findNearbyChest();
            }
            if (chestPos == null) return false;

            targetPos = findTargetPos();
            if (targetPos != null) {
                golem.setFarmTarget(targetPos);
                return true;
            }
            return false;
        }

        private boolean canSee(BlockPos pos) {
            Vec3 start = golem.getEyePosition();
            Vec3 end = net.minecraft.world.phys.Vec3.atCenterOf(pos);
            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.VISUAL,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    golem
            );
            net.minecraft.world.phys.BlockHitResult result = golem.level().clip(context);
            return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS || result.getBlockPos().equals(pos);
        }

        private BlockPos findNearbyChest() {
            return golem.findNearbyChest();
        }

        private BlockPos findTargetPos() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return null;

            // Search radius: 32 blocks (covers a large area).
            // A standard 9x9 field has 81 blocks.
            // We search for tasks within the 32x32 area around the chest.
            List<BlockPos> otherGolemsTargets = getOtherGolemsTargets();
            
            // Priority 1: Harvest mature crops
            for (int y = -3; y <= 3; y++) {
                for (int x = -16; x <= 16; x++) {
                    for (int z = -16; z <= 16; z++) {
                        BlockPos p = chestPos.offset(x, y, z);
                        if (p.equals(chestPos) || golem.isBlacklisted(p) || otherGolemsTargets.contains(p)) continue;
                        if (shouldHarvest(p, null)) {
                            // Check line of sight
                            if (canSee(p)) return p;
                        }
                    }
                }
            }

            // Priority 2: Pick up dropped items in the field
            List<net.minecraft.world.entity.item.ItemEntity> items = golem.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(chestPos).inflate(32.0),
                    item -> !item.hasPickUpDelay() && isFamiliarItem(item.getItem()) && canSee(item.blockPosition())
            );
            if (!items.isEmpty()) {
                net.minecraft.world.entity.item.ItemEntity closest = items.stream()
                        .filter(item -> item.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(chestPos)) <= 32.0 * 32.0)
                        .min(Comparator.comparingDouble(golem::distanceToSqr))
                        .orElse(null);
                if (closest != null) {
                    return closest.blockPosition();
                }
            }

            // Priority 2.5: Nether Wart Farm Construction (if we have shovel, soul sand, and nether wart)
            if (hasNetherWart() && hasItem(net.minecraft.world.item.Items.SOUL_SAND) && hasShovel()) {
                for (int x = -4; x <= 4; x++) {
                    for (int z = -4; z <= 4; z++) {
                        for (int y = -1; y <= 1; y++) {
                            BlockPos p = chestPos.offset(x, y, z);
                            if (p.equals(chestPos) || golem.isBlacklisted(p) || otherGolemsTargets.contains(p)) continue;
                            if (shouldPlant(p, null)) {
                                if (canSee(p)) return p;
                            }
                        }
                    }
                }
            }

            // Priority 3: Water
            BlockPos waterPos = findWaterCenter(chestPos);
            if (waterPos == null && hasWaterBucket()) {
                BlockPos waterSpot = findPlaceForWater(chestPos);
                if (waterSpot != null && !otherGolemsTargets.contains(waterSpot)) {
                    if (canSee(waterSpot)) return waterSpot;
                }
            }

            // Priority 4: Tilling and Planting (requires water)
            if (waterPos != null) {
                // Focus on the 33x33 area around this water source (multiple fields if they are close)
                for (int y = -1; y <= 1; y++) {
                    for (int x = -16; x <= 16; x++) {
                        for (int z = -16; z <= 16; z++) {
                            BlockPos p = waterPos.offset(x, y, z);
                            if (p.equals(waterPos) || golem.isBlacklisted(p) || otherGolemsTargets.contains(p)) continue;

                            // Checkered pattern logic for Stripped Bamboo Golems (Pumpkin/Melon mode)
                            if (golem.isStripped()) {
                                if ((Math.abs(p.getX() - waterPos.getX()) + Math.abs(p.getZ() - waterPos.getZ())) % 2 == 0) {
                                    // This is a seed spot
                                    if (shouldTill(p, waterPos) || shouldPlant(p, waterPos)) {
                                        if (canSee(p)) return p;
                                    }
                                } else {
                                    // This is a growth spot. Golem should only till it if it's not farmland/dirt/grass (e.g. if it was harvested)
                                    // Actually, pumpkins/melons grow on dirt, grass, or farmland. 
                                    // If it's a growth spot, we don't want to plant there. 
                                    // If it's currently tillable and we are stripped, we should TILL it to ensure pumpkins can grow.
                                    // BUT, wait, pumpkins don't NEED farmland. They can grow on dirt. 
                                    // The issue description says: "Doesnt use hoe for blocks that were under pumpkin/melons."
                                    // This suggests the golem SHOULD till those spots.
                                    if (shouldTill(p, waterPos)) {
                                        if (canSee(p)) return p;
                                    }
                                }
                            } else {
                                if (shouldTill(p, waterPos) || shouldPlant(p, waterPos)) {
                                    if (canSee(p)) return p;
                                }
                            }
                        }
                    }
                }
            } else if (!hasWaterBucket()) {
                // Fallback: search around chest if no water bucket is available (e.g. user-made field)
                for (int y = -3; y <= 3; y++) {
                    for (int x = -16; x <= 16; x++) {
                        for (int z = -16; z <= 16; z++) {
                            BlockPos p = chestPos.offset(x, y, z);
                            if (p.equals(chestPos) || golem.isBlacklisted(p) || otherGolemsTargets.contains(p)) continue;

                            if (golem.isStripped()) {
                                if ((Math.abs(p.getX() - chestPos.getX()) + Math.abs(p.getZ() - chestPos.getZ())) % 2 == 0) {
                                    if (shouldTill(p, null) || shouldPlant(p, null)) {
                                        if (canSee(p)) return p;
                                    }
                                } else {
                                    if (shouldTill(p, null)) {
                                        if (canSee(p)) return p;
                                    }
                                }
                            } else {
                                if (shouldTill(p, null) || shouldPlant(p, null)) {
                                    if (canSee(p)) return p;
                                }
                            }
                        }
                    }
                }
            }

            return null;
        }

        private boolean isFamiliarItem(ItemStack stack) {
            boolean isHoe = UtilityGolem.isHoe(stack);
            boolean isAxe = UtilityGolem.isAxe(stack);
            boolean isShovel = UtilityGolem.isShovel(stack);
            boolean isCrop = stack.is(net.minecraft.world.item.Items.WHEAT) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT) ||
                            stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS) || stack.is(net.minecraft.world.item.Items.PUMPKIN) || stack.is(net.minecraft.world.item.Items.MELON) || stack.is(net.minecraft.world.item.Items.MELON_SLICE);
            boolean isSeed = stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS) || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS) || stack.is(net.minecraft.world.item.Items.PITCHER_POD);
            return isHoe || isAxe || isShovel || isCrop || isSeed || stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET) || stack.is(net.minecraft.world.item.Items.SOUL_SAND);
        }

        private List<BlockPos> getOtherGolemsTargets() {
            List<BlockPos> targets = new ArrayList<>();
            List<UtilityGolem> golems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(32.0), g -> g != golem && g.getGolemType() == GolemType.BAMBOO);
            for (UtilityGolem g : golems) {
                BlockPos target = g.getFarmTarget();
                if (target != null) {
                    targets.add(target);
                }
            }
            return targets;
        }

        private boolean isWater(BlockPos pos) {
            return golem.level().getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
        }

        private BlockPos findWaterCenter(BlockPos chestPos) {
            int range = 10;
            // Search for existing water. We want the one closest to the chest if there are multiple,
            // but ideally there is only one.
            BlockPos bestWater = null;
            double minDist = Double.MAX_VALUE;

            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = chestPos.offset(x, y, z);
                        if (isWater(p)) {
                            double dist = p.distSqr(chestPos);
                            if (dist < minDist) {
                                minDist = dist;
                                bestWater = p;
                            }
                        }
                    }
                }
            }
            return bestWater;
        }

        private BlockPos findPlaceForWater(BlockPos chestPos) {
            // Find a spot near the chest. We want it to be consistently the same spot
            // if we are searching multiple times.
            int range = 5;
            // Iterate Y from 1 down to -1 to find a spot at surface level or slightly below
            for (int y = 1; y >= -1; y--) {
                for (int x = -range; x <= range; x++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = chestPos.offset(x, y, z);
                        if (canPlaceWater(p)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean canPlaceWater(BlockPos pos) {
            Level world = golem.level();
            BlockState state = world.getBlockState(pos);
            // Don't place water if there is already water nearby (prevents rows)
            if (findWaterNearby(pos, 4) != null) return false;

            // Target must be replaceable (like air, grass) or a dirt block we can dig out
            if (!state.canBeReplaced() && !state.is(net.minecraft.tags.BlockTags.DIRT)) return false;
            
            // The block ABOVE must be air or replaceable (not water, not solid)
            BlockState above = world.getBlockState(pos.above());
            if (!above.is(net.minecraft.world.level.block.Blocks.AIR) && !above.canBeReplaced()) return false;
            if (above.is(net.minecraft.world.level.block.Blocks.WATER)) return false;

            // Check if surrounded by dirt/grass/farmland to ensure it's a good farm spot
            int dirtCount = 0;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockState s = world.getBlockState(pos.offset(x, 0, z));
                    if (s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.world.level.block.Blocks.FARMLAND) || s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                        dirtCount++;
                    }
                }
            }
            // Need at least 4 surrounding dirt-like blocks to consider it a "center"
            return dirtCount >= 4;
        }

        private BlockPos findWaterNearby(BlockPos pos, int range) {
            for (int x = -range; x <= range; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.WATER)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean shouldHarvest(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            BlockState state = golem.level().getBlockState(pos);
            Block block = state.getBlock();
            
            // If stripped, ONLY harvest pumpkins and melons
            if (golem.isStripped()) {
                if (state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) {
                    return true;
                }
                return false;
            }
            
            if (block instanceof CropBlock crop) {
                return crop.isMaxAge(state);
            }
            if (block instanceof NetherWartBlock wart) {
                return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3;
            }
            if (block instanceof CocoaBlock cocoa) {
                return state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE) >= 2;
            }
            if (state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) {
                return true;
            }
            return false;
        }

        private boolean shouldTill(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            if (!hasHoe()) return false;
            BlockState state = golem.level().getBlockState(pos);
            // Must be tillable
            boolean isTillable = state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.DIRT) || state.is(net.minecraft.world.level.block.Blocks.DIRT_PATH);
            BlockState aboveState = golem.level().getBlockState(pos.above());
            // Above must be air or replaceable (we will break replaceable things)
            boolean isAboveSafe = aboveState.is(net.minecraft.world.level.block.Blocks.AIR) || (aboveState.canBeReplaced() && !aboveState.is(net.minecraft.world.level.block.Blocks.WATER));
            return isTillable && isAboveSafe;
        }

        private boolean hasHoe() {
            if (UtilityGolem.isHoe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isHoe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasAxe() {
            if (UtilityGolem.isAxe(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isAxe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean shouldPlant(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            BlockState state = golem.level().getBlockState(pos);
            BlockState aboveState = golem.level().getBlockState(pos.above());
            boolean isAboveSafe = aboveState.is(net.minecraft.world.level.block.Blocks.AIR) || (aboveState.canBeReplaced() && !aboveState.is(net.minecraft.world.level.block.Blocks.WATER));
            
            // Checkered pattern logic for Stripped Bamboo Golems
            if (golem.isStripped()) {
                BlockPos referencePos = waterPos != null ? waterPos : golem.getChestPos();
                if (referencePos != null) {
                    if ((Math.abs(pos.getX() - referencePos.getX()) + Math.abs(pos.getZ() - referencePos.getZ())) % 2 != 0) {
                        return false; // Not a planting spot
                    }
                }
            }

            // Standard crops
            if (state.is(net.minecraft.world.level.block.Blocks.FARMLAND) && isAboveSafe && hasSeeds()) return true;

            // Carrots and Potatoes on Farmland
            if (state.is(net.minecraft.world.level.block.Blocks.FARMLAND) && isAboveSafe && (hasItem(net.minecraft.world.item.Items.CARROT) || hasItem(net.minecraft.world.item.Items.POTATO))) return true;

            // Nether wart
            if (state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND) && isAboveSafe && hasNetherWart()) return true;

            // Cocoa beans
            if (state.is(net.minecraft.world.level.block.Blocks.AIR) && hasCocoaBeans() && findJungleLogNearby(pos) != null) return true;

            // Digging for Nether Wart farm (9x9 plot)
            if (hasNetherWart() && hasItem(net.minecraft.world.item.Items.SOUL_SAND) && hasShovel() && !state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND) && isAboveSafe) {
                BlockPos chestPos = golem.getChestPos();
                if (chestPos != null) {
                    if (Math.abs(pos.getX() - chestPos.getX()) <= 4 && Math.abs(pos.getZ() - chestPos.getZ()) <= 4) {
                        // Check if it's already a planting spot for something else or if it's a water spot
                        if (waterPos != null && pos.equals(waterPos)) return false;
                        // Avoid digging up established farmland if it's already hydrated or has crops
                        if (state.is(net.minecraft.world.level.block.Blocks.FARMLAND)) return false;
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean hasShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShovel(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasItem(Item item) {
            if (golem.getHeldItem().is(item)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(item)) return true;
            }
            return false;
        }

        private boolean hasNetherWart() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.NETHER_WART)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.NETHER_WART)) return true;
            }
            return false;
        }

        private boolean hasCocoaBeans() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.COCOA_BEANS)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.COCOA_BEANS)) return true;
            }
            return false;
        }

        private boolean hasSeeds() {
            if (isSeed(golem.getHeldItem())) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isSeed(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean isSeed(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)
                    || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS)
                    || stack.is(net.minecraft.world.item.Items.PITCHER_POD) || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS);
        }


        @Override
        public boolean canContinueToUse() {
            if (targetPos == null) return false;
            if (golem.getGolemType() != GolemType.BAMBOO) return false;
            
            // Check if target is still valid
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return false;
            BlockPos waterPos = findWaterCenter(chestPos);
            
            // If it's an item, it's always valid as long as it exists (tick will handle it)
            // For other actions, verify they still need to be done
            return true; 
        }

        @Override
        public void start() {
            farmActionTime = 0;
            golem.setAnimation(GolemAnimation.FARMING, MAX_FARM_ACTION_TIME);
        }

        private int stuckTicks = 0;
        private Vec3 lastPos = net.minecraft.world.phys.Vec3.ZERO;

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Ensure animation is active while farming
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.FARMING, 40);
            }

            // Ensure we are holding the right tool for the job
            ensureCorrectTool();

            double dist = golem.blockPosition().distSqr(targetPos);
            if (dist > 4.0) {
                // stuck check
                Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.distanceToSqr(lastPos) < 0.001) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastPos = currentPos;

                if (stuckTicks > 100) {
                    golem.blacklistPosition(targetPos);
                    targetPos = null;
                    golem.setFarmTarget(null);
                    return;
                }

                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible = golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                    if (!possible) {
                        golem.blacklistPosition(targetPos);
                        targetPos = null;
                        golem.setFarmTarget(null);
                        return;
                    }
                }
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);
                
                farmActionTime++;
                if (farmActionTime % 5 == 0) {
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND); // might not work?
                }

                if (farmActionTime >= MAX_FARM_ACTION_TIME) {
                    performFarmAction();
                    farmActionTime = 0;
                    targetPos = findTargetPos();
                    golem.setFarmTarget(targetPos);
                }
            }
        }

        @Override
        public void stop() {
            golem.setFarmTarget(null);
        }

        private void ensureCorrectTool() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return;
            BlockPos waterPos = findWaterCenter(chestPos);

            // If we are heading to place water, we should hold the water bucket
            boolean isWaterTarget = false;
            if (targetPos.equals(waterPos)) {
                if (!isWater(waterPos)) isWaterTarget = true;
            } else if (waterPos == null && targetPos.equals(findPlaceForWater(chestPos))) {
                isWaterTarget = true;
            }

            if (isWaterTarget) {
                if (!golem.getHeldItem().is(net.minecraft.world.item.Items.WATER_BUCKET)) {
                    swapToItem(net.minecraft.world.item.Items.WATER_BUCKET);
                }
            } else if (shouldHarvest(targetPos, waterPos)) {
                // If we are heading to harvest pumpkin or melon, we should hold an axe
                BlockState state = golem.level().getBlockState(targetPos);
                if ((state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) && hasAxe()) {
                    if (!UtilityGolem.isAxe(golem.getHeldItem())) {
                        swapToAxe();
                    }
                } else if (!golem.getHeldItem().isEmpty()) {
                    // For regular crops, empty hand is fine, but we don't necessarily need to swap to empty
                    // However, we should definitely NOT be holding a tool that isn't needed.
                    // If we're holding a Hoe or Axe but don't need it, we could swap to seeds if we'll plant next,
                    // or just leave it. The current logic is okay for harvesting.
                }
            } else if (shouldPlant(targetPos, waterPos)) {
                // If we are heading to plant, we should hold the seeds
                ItemStack seeds = getSeedsForTarget(targetPos);
                if (!seeds.isEmpty() && !golem.getHeldItem().is(seeds.getItem())) {
                    swapToItem(seeds.getItem());
                }
            } else if (shouldTill(targetPos, waterPos)) {
                // If we are heading to till, we should hold a hoe
                if (!UtilityGolem.isHoe(golem.getHeldItem())) {
                    swapToHoe();
                }
            }
        }

        private void swapToItem(net.minecraft.world.item.Item itemType) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(itemType)) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(stack.copy());
                    inv.setItem(i, currentHeld);
                    break;
                }
            }
        }

        private void swapToHoe() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isHoe(inv.getItem(i))) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(inv.getItem(i).copy());
                    inv.setItem(i, currentHeld);
                    break;
                }
            }
        }

        private void swapToAxe() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isAxe(inv.getItem(i))) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(inv.getItem(i).copy());
                    inv.setItem(i, currentHeld);
                    break;
                }
            }
        }

        private void swapToShovel() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShovel(inv.getItem(i))) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(inv.getItem(i).copy());
                    inv.setItem(i, currentHeld);
                    break;
                }
            }
        }

        private void performFarmAction() {
            Level world = golem.level();
            BlockState state = world.getBlockState(targetPos);
            BlockPos waterPos = findWaterCenter(golem.getChestPos());
            
            // 0. Ensure we have the right tool if we are about to act
            if (shouldHarvest(targetPos, waterPos)) {
                if ((state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) && hasAxe()) {
                    if (!UtilityGolem.isAxe(golem.getHeldItem())) {
                        swapToAxe();
                    }
                }
            } else if (shouldTill(targetPos, waterPos)) {
                if (!UtilityGolem.isHoe(golem.getHeldItem())) {
                    swapToHoe();
                }
            } else if (shouldPlant(targetPos, waterPos)) {
                // If we need to dig for nether wart farm
                if (!state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND) && hasItem(net.minecraft.world.item.Items.SOUL_SAND) && hasNetherWart()) {
                    if (!UtilityGolem.isShovel(golem.getHeldItem()) && hasShovel()) {
                        swapToShovel();
                    }
                }
            }

            // 1. Pickup items
            List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(targetPos).inflate(1.5), item -> true);
            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    ItemStack stack = item.getItem();
                    ItemStack remaining = golem.getInventory().addItem(stack);
                    if (remaining.isEmpty()) {
                        item.discard();
                    } else {
                        item.setItem(remaining);
                    }
                }
                targetPos = null;
                golem.setFarmTarget(null);
                return;
            }

            // 2. Water
            if ((waterPos != null && targetPos.equals(waterPos)) || (waterPos == null && targetPos.equals(findPlaceForWater(golem.getChestPos())))) {
                if (!isWater(targetPos)) {
                    ItemStack waterBucket = golem.getHeldItem();
                    if (!waterBucket.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
                        waterBucket = getWaterBucket();
                    }
                    
                    if (!waterBucket.isEmpty()) {
                        world.setBlock(targetPos, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(), 3);
                        useWaterBucket(waterBucket);

                        // After placing water, if we have a hoe in golem.getInventory(), swap back to it
                        if (hasHoe() && !UtilityGolem.isHoe(golem.getHeldItem())) {
                            swapToHoe();
                        }
                    }
                }
                targetPos = null;
                golem.setFarmTarget(null);
                return;
            }
            
            // If waterPos is NOT null, and we are NOT at waterPos, and waterPos is NOT water,
            // then we should NOT be doing other farm actions yet.
            if (waterPos != null && !isWater(waterPos)) {
                targetPos = null;
                golem.setFarmTarget(null);
                return;
            }

            // 3. Harvest
            if (shouldHarvest(targetPos, waterPos)) {
                if (world instanceof ServerLevel serverLevel) {
                    // Ensure axe if pumpkin/melon
                    if ((state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) && hasAxe()) {
                        if (!UtilityGolem.isAxe(golem.getHeldItem())) {
                            swapToAxe();
                        }
                    } else if (UtilityGolem.isAxe(golem.getHeldItem()) && !(state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON))) {
                        // If we are holding an axe but it's not pumpkin/melon, swap to hoe or seeds if we have them
                        if (hasHoe()) swapToHoe();
                    } else if (state.is(net.minecraft.world.level.block.Blocks.NETHER_WART)) {
                        // Nether wart doesn't need tools.
                    }

                    // Check if we can actually harvest this (e.g. if we need a tool for pumpkin/melon but don't have one)
                    boolean needsAxe = state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON);
                    if (needsAxe && !hasAxe()) {
                        targetPos = null;
                        golem.setFarmTarget(null);
                        return;
                    }

                    net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(targetPos))
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, golem.getHeldItem())
                            .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, golem);

                    ItemStack tool = golem.getHeldItem();
                    List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, targetPos, null, golem, tool);
                    serverLevel.destroyBlock(targetPos, false, golem, 512);
                    for (ItemStack drop : drops) {
                        ItemStack remaining = golem.getInventory().addItem(drop);
                        if (!remaining.isEmpty()) {
                            net.minecraft.world.level.block.Block.popResource(serverLevel, targetPos, remaining);
                        }
                    }

                    // Damage axe if used for pumpkin/melon
                    if ((state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.MELON)) && UtilityGolem.isAxe(tool)) {
                        tool.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                    }
                    
                    // Swing hand to show action
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
                targetPos = null;
                golem.setFarmTarget(null);
                return;
            }

            // 4. Till
            if (shouldTill(targetPos, waterPos)) {
                // Ensure we have a hoe
                if (!UtilityGolem.isHoe(golem.getHeldItem())) {
                    swapToHoe();
                }

                // Break things like grass or flowers above first
                BlockPos abovePos = targetPos.above();
                BlockState aboveState = world.getBlockState(abovePos);
                if (aboveState.canBeReplaced() && !aboveState.is(net.minecraft.world.level.block.Blocks.AIR) && !aboveState.is(net.minecraft.world.level.block.Blocks.WATER)) {
                    world.destroyBlock(abovePos, true, golem, 512);
                }

                world.setBlock(targetPos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
                world.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                
                // Damage hoe
                ItemStack hoe = golem.getHeldItem();
                if (UtilityGolem.isHoe(hoe)) {
                    if (world instanceof ServerLevel serverLevel) {
                        hoe.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                    }
                }
                
                // Swing hand to show action
                golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                
                targetPos = null; // Reset target after action
                golem.setFarmTarget(null);
                return;
            }

            // 5. Plant and Nether Wart Farm
            if (shouldPlant(targetPos, waterPos)) {
                // Nether Wart Farm Construction: Dig and place Soul Sand
                if (!state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND) && hasItem(net.minecraft.world.item.Items.SOUL_SAND) && hasNetherWart() && hasShovel()) {
                    if (!UtilityGolem.isShovel(golem.getHeldItem())) {
                        swapToShovel();
                    }
                    if (UtilityGolem.isShovel(golem.getHeldItem())) {
                        world.destroyBlock(targetPos, true, golem, 512);
                        ItemStack soulSand = ItemStack.EMPTY;
                        if (golem.getHeldItem().is(net.minecraft.world.item.Items.SOUL_SAND)) {
                            soulSand = golem.getHeldItem();
                        } else {
                            SimpleContainer inv = golem.getInventory();
                            for (int i = 0; i < inv.getContainerSize(); i++) {
                                if (inv.getItem(i).is(net.minecraft.world.item.Items.SOUL_SAND)) {
                                    soulSand = inv.getItem(i);
                                    break;
                                }
                            }
                        }
                        
                        if (!soulSand.isEmpty()) {
                            world.setBlock(targetPos, net.minecraft.world.level.block.Blocks.SOUL_SAND.defaultBlockState(), 3);
                            soulSand.shrink(1);
                            if (soulSand.isEmpty() && golem.getHeldItem() == soulSand) {
                                golem.setHeldItem(ItemStack.EMPTY);
                            }
                            world.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.SOUL_SAND_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }

                        // Damage shovel
                        ItemStack shovel = golem.getHeldItem();
                        if (UtilityGolem.isShovel(shovel)) {
                            if (world instanceof ServerLevel serverLevel) {
                                shovel.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                            }
                        }
                        
                        golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        targetPos = null;
                        golem.setFarmTarget(null);
                        return;
                    }
                }

                ItemStack seeds = golem.getHeldItem();
                if (!isSeed(seeds)) {
                    seeds = getSeeds();
                }
                
                if (!seeds.isEmpty()) {
                    BlockPos plantPos = targetPos.above();
                    Block seedBlock = getSeedBlock(seeds);
                        if (seedBlock != null) {
                            // Specialized planting for certain crops
                            if (seedBlock == net.minecraft.world.level.block.Blocks.COCOA) {
                                // Find jungle log to plant on
                                BlockPos logPos = findJungleLogNearby(targetPos);
                                if (logPos != null) {
                                    Direction dir = getDirectionToPlantCocoa(targetPos, logPos);
                                    world.setBlock(targetPos, seedBlock.defaultBlockState().setValue(net.minecraft.world.level.block.CocoaBlock.FACING, dir), 3);
                                    seeds.shrink(1);
                                    if (seeds.isEmpty() && golem.getHeldItem() == seeds) {
                                        golem.setHeldItem(ItemStack.EMPTY);
                                    }
                                    world.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.GRASS_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            } else {
                                world.setBlock(plantPos, seedBlock.defaultBlockState(), 3);
                                seeds.shrink(1);
                                if (seeds.isEmpty() && golem.getHeldItem() == seeds) {
                                    golem.setHeldItem(ItemStack.EMPTY);
                                }
                                world.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.GRASS_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }

                            // Swing hand to show action
                            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

                            // After planting, if we have a hoe in golem.getInventory(), swap back to it
                            if (hasHoe() && !UtilityGolem.isHoe(golem.getHeldItem())) {
                                swapToHoe();
                            }
                        }
                }
                targetPos = null; // Reset target after action
                golem.setFarmTarget(null);
            }
        }

        private ItemStack getWaterBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.WATER_BUCKET)) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.WATER_BUCKET)) return inv.getItem(i);
            }
            return ItemStack.EMPTY;
        }

        private void useWaterBucket(ItemStack waterBucket) {
            if (waterBucket.getCount() == 1) {
                // If it's in the hand, we can just replace it.
                // If it's in the golem.getInventory(), it's more complex because it becomes an empty bucket.
                if (golem.getHeldItem() == waterBucket) {
                    golem.setHeldItem(new ItemStack(net.minecraft.world.item.Items.BUCKET));
                } else {
                    // It's in the golem.getInventory(). We need to find its slot.
                    SimpleContainer inv = golem.getInventory();
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        if (inv.getItem(i) == waterBucket) {
                            inv.setItem(i, new ItemStack(net.minecraft.world.item.Items.BUCKET));
                            break;
                        }
                    }
                }
            } else {
                waterBucket.shrink(1);
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                ItemStack remaining = golem.getInventory().addItem(emptyBucket);
                if (!remaining.isEmpty()) {
                    net.minecraft.world.level.block.Block.popResource(golem.level(), golem.blockPosition(), remaining);
                }
            }
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private ItemStack getSeeds() {
            if (isSeed(golem.getHeldItem())) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isSeed(inv.getItem(i))) return inv.getItem(i);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getSeedsForTarget(BlockPos pos) {
            Level world = golem.level();
            BlockState state = world.getBlockState(pos);
            if (state.is(net.minecraft.world.level.block.Blocks.FARMLAND)) return getStandardSeeds();
            if (state.is(net.minecraft.world.level.block.Blocks.SOUL_SAND)) return getNetherWart();
            if (findJungleLogNearby(pos) != null) return getCocoaBeans();
            return getSeeds();
        }

        private ItemStack getStandardSeeds() {
            if (isSeed(golem.getHeldItem()) && !golem.getHeldItem().is(net.minecraft.world.item.Items.NETHER_WART) && !golem.getHeldItem().is(net.minecraft.world.item.Items.COCOA_BEANS)) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (isSeed(stack) && !stack.is(net.minecraft.world.item.Items.NETHER_WART) && !stack.is(net.minecraft.world.item.Items.COCOA_BEANS)) return stack;
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getNetherWart() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.NETHER_WART)) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.NETHER_WART)) return inv.getItem(i);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getCocoaBeans() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.COCOA_BEANS)) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.COCOA_BEANS)) return inv.getItem(i);
            }
            return ItemStack.EMPTY;
        }

        private Block getSeedBlock(ItemStack seeds) {
            if (seeds.is(net.minecraft.world.item.Items.WHEAT_SEEDS)) return net.minecraft.world.level.block.Blocks.WHEAT;
            if (seeds.is(net.minecraft.world.item.Items.CARROT)) return net.minecraft.world.level.block.Blocks.CARROTS;
            if (seeds.is(net.minecraft.world.item.Items.POTATO)) return net.minecraft.world.level.block.Blocks.POTATOES;
            if (seeds.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)) return net.minecraft.world.level.block.Blocks.BEETROOTS;
            if (seeds.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS)) return net.minecraft.world.level.block.Blocks.PUMPKIN_STEM;
            if (seeds.is(net.minecraft.world.item.Items.MELON_SEEDS)) return net.minecraft.world.level.block.Blocks.MELON_STEM;
            if (seeds.is(net.minecraft.world.item.Items.NETHER_WART)) return net.minecraft.world.level.block.Blocks.NETHER_WART;
            if (seeds.is(net.minecraft.world.item.Items.COCOA_BEANS)) return net.minecraft.world.level.block.Blocks.COCOA;
            return null;
        }

        private BlockPos findJungleLogNearby(BlockPos pos) {
            for (Direction dir : Direction.values()) {
                BlockPos p = pos.relative(dir);
                if (golem.level().getBlockState(p).is(net.minecraft.tags.BlockTags.JUNGLE_LOGS)) {
                    return p;
                }
            }
            return null;
        }

        private Direction getDirectionToPlantCocoa(BlockPos pos, BlockPos logPos) {
            int dx = logPos.getX() - pos.getX();
            int dz = logPos.getZ() - pos.getZ();
            if (dx > 0) return Direction.EAST;
            if (dx < 0) return Direction.WEST;
            if (dz > 0) return Direction.SOUTH;
            if (dz < 0) return Direction.NORTH;
            return Direction.NORTH;
        }
    }
    public static class RefillBucketGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos waterSource;
        private int delay;

        public RefillBucketGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.BAMBOO) return false;
            if (hasWaterBucket()) return false;
            if (!hasEmptyBucket()) return false;
            
            waterSource = findNearbyWater();
            return waterSource != null;
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private boolean hasEmptyBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.BUCKET)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.BUCKET)) return true;
            }
            return false;
        }

        private BlockPos findNearbyWater() {
            BlockPos pos = golem.blockPosition();
            for (int x = -16; x <= 16; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -16; z <= 16; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.WATER)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void start() {
            delay = 0;
        }

        @Override
        public void tick() {
            if (waterSource == null) return;

            double dx = golem.getX() - (waterSource.getX() + 0.5);
            double dy = golem.getY() - (waterSource.getY() + 0.5);
            double dz = golem.getZ() - (waterSource.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(waterSource.getX(), waterSource.getY(), waterSource.getZ(), 1.0);
                }
                golem.getLookControl().setLookAt(waterSource.getX() + 0.5, waterSource.getY() + 0.5, waterSource.getZ() + 0.5, 30.0F, 30.0F);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(waterSource.getX() + 0.5, waterSource.getY() + 0.5, waterSource.getZ() + 0.5, 30.0F, 30.0F);
                
                if (++delay % 20 == 0) {
                    fillBucket();
                }
            }
        }

        private void fillBucket() {
            ItemStack emptyBucket = getEmptyBucket();
            if (!emptyBucket.isEmpty()) {
                if (emptyBucket.getCount() == 1) {
                    if (golem.getHeldItem() == emptyBucket) {
                        golem.setHeldItem(new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                    } else {
                        SimpleContainer inv = golem.getInventory();
                        for (int i = 0; i < inv.getContainerSize(); i++) {
                            if (inv.getItem(i) == emptyBucket) {
                                inv.setItem(i, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                                break;
                            }
                        }
                    }
                } else {
                    emptyBucket.shrink(1);
                    ItemStack waterBucket = new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
                    ItemStack remaining = golem.getInventory().addItem(waterBucket);
                    if (!remaining.isEmpty()) {
                        net.minecraft.world.level.block.Block.popResource(golem.level(), golem.blockPosition(), remaining);
                    }
                }
                golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                waterSource = null;
            }
        }

        private ItemStack getEmptyBucket() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.BUCKET)) return golem.getHeldItem();
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.BUCKET)) return inv.getItem(i);
            }
            return ItemStack.EMPTY;
        }
    }
    public static class TriggerRedstoneGoal extends Goal {
        private final UtilityGolem golem;
        private int interactionDelay;

        public TriggerRedstoneGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return golem.isRedstoneProgramStarted() && !golem.getRedstoneProgram().isEmpty();
        }

        @Override
        public void start() {
            interactionDelay = 0;
        }

        @Override
        public void tick() {
            List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
            if (program.isEmpty() || !golem.isRedstoneProgramStarted()) {
                return;
            }

            int index = golem.getCurrentInteractionIndex();
            if (index >= program.size()) {
                golem.setCurrentInteractionIndex(0);
                index = 0;
            }

            UtilityGolem.RedstoneInteraction interaction = program.get(index);
            BlockPos target = interaction.pos();
            
            // Check if block is still there
            BlockState state = golem.level().getBlockState(target);
            if (!(state.getBlock() instanceof ButtonBlock || state.getBlock() instanceof LeverBlock || 
                  state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock ||
                  state.getBlock() instanceof FenceGateBlock || state.getBlock() == net.minecraft.world.level.block.Blocks.TNT || 
                  state.getBlock() == net.minecraft.world.level.block.Blocks.REDSTONE_LAMP)) {
                // If the block is gone, skip it? Or just try anyway? Let's skip it to avoid getting stuck.
                golem.setCurrentInteractionIndex((index + 1) % program.size());
                golem.setRedstoneTickCounter(0);
                return;
            }

            double distSq = golem.distanceToSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

            if (distSq > 4.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

                int counter = golem.getRedstoneTickCounter();
                if (counter >= interaction.interval()) {
                    interactWithComponent(target, state);
                    golem.setCurrentInteractionIndex((index + 1) % program.size());
                    golem.setRedstoneTickCounter(0);
                } else {
                    golem.setRedstoneTickCounter(counter + 1);
                }
            }
        }

        private void interactWithComponent(BlockPos pos, BlockState state) {
            Block block = state.getBlock();
            golem.setAnimation(GolemAnimation.PRESSING_BUTTON, 20);
            if (block instanceof net.minecraft.world.level.block.ButtonBlock button) {
                golem.level().setBlock(pos, state.setValue(net.minecraft.world.level.block.ButtonBlock.POWERED, true), 3);
                golem.level().scheduleTick(pos, block, 20);
                golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
            } else if (block instanceof net.minecraft.world.level.block.LeverBlock lever) {
                golem.level().setBlock(pos, state.cycle(net.minecraft.world.level.block.LeverBlock.POWERED), 3);
                golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, 0.6f);
            } else if (block instanceof net.minecraft.world.level.block.DoorBlock || block instanceof net.minecraft.world.level.block.TrapDoorBlock || block instanceof net.minecraft.world.level.block.FenceGateBlock) {
                // Find the property representing whether it is open
                if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)) {
                    golem.level().setBlock(pos, state.cycle(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN), 3);
                    golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            } else if (block == net.minecraft.world.level.block.Blocks.TNT) {
                net.minecraft.world.level.block.TntBlock.prime(golem.level(), pos);
                golem.level().removeBlock(pos, false);
            }
        }
    }
    public static class ConnectRedstoneGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos startPos;
        private BlockPos endPos;
        private List<BlockPos> path;
        private int delay;

        public ConnectRedstoneGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.REDSTONE) return false;
            SimpleContainer inv = golem.getInventory();
            boolean hasRedstone = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.REDSTONE)) {
                    hasRedstone = true;
                    break;
                }
            }
            if (!hasRedstone) return false;

            findComponentsToConnect();
            return startPos != null && endPos != null;
        }

        private void findComponentsToConnect() {
            BlockPos pos = golem.blockPosition();
            int range = 16;
            List<BlockPos> components = new ArrayList<>();
            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        BlockState bs = golem.level().getBlockState(p);
                        if (isRedstoneComponent(bs)) {
                            components.add(p);
                        }
                    }
                }
            }

            if (components.size() >= 2) {
                // Find two components not already connected (simplistic check)
                for (int i = 0; i < components.size(); i++) {
                    for (int j = i + 1; j < components.size(); j++) {
                        BlockPos p1 = components.get(i);
                        BlockPos p2 = components.get(j);
                        if (p1.distSqr(p2) > 4 && p1.distSqr(p2) < 256) {
                            if (!areConnected(p1, p2)) {
                                startPos = p1;
                                endPos = p2;
                                return;
                            }
                        }
                    }
                }
            }
        }

        private boolean isRedstoneComponent(BlockState state) {
            Block b = state.getBlock();
            return b instanceof ButtonBlock || b instanceof LeverBlock || b instanceof PressurePlateBlock || b == net.minecraft.world.level.block.Blocks.TNT || b == net.minecraft.world.level.block.Blocks.REDSTONE_LAMP || b instanceof net.minecraft.world.level.block.DoorBlock;
        }

        private boolean areConnected(BlockPos p1, BlockPos p2) {
            // Very basic check: is there redstone dust near p1 that leads towards p2?
            // For now, let's just assume they are not connected if there is no dust immediately adjacent
            for (Direction dir : Direction.values()) {
                if (golem.level().getBlockState(p1.relative(dir)).is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)) return true;
                if (golem.level().getBlockState(p2.relative(dir)).is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)) return true;
            }
            return false;
        }

        @Override
        public void start() {
            delay = 0;
            path = calculatePath(startPos, endPos);
            golem.setAnimation(GolemAnimation.CONNECTING, 100);
        }

        private List<BlockPos> calculatePath(BlockPos start, BlockPos end) {
            List<BlockPos> p = new ArrayList<>();
            int x1 = start.getX();
            int y1 = start.getY();
            int z1 = start.getZ();
            int x2 = end.getX();
            int y2 = end.getY();
            int z2 = end.getZ();

            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int dz = Math.abs(z2 - z1);
            int sx = x1 < x2 ? 1 : -1;
            int sy = y1 < y2 ? 1 : -1;
            int sz = z1 < z2 ? 1 : -1;

            // Simple Manhattan path
            int currX = x1;
            int currY = y1;
            int currZ = z1;

            // Move one step away from start to not overwrite component
            if (dx > dz) currX += sx; else currZ += sz;

            while (currX != x2 || currZ != z2) {
                BlockPos target = new BlockPos(currX, currY, currZ);
                if (golem.level().getBlockState(target).canBeReplaced()) {
                    p.add(target);
                } else if (golem.level().getBlockState(target.above()).canBeReplaced()) {
                    currY++;
                    p.add(target.above());
                } else if (golem.level().getBlockState(target.below()).canBeReplaced()) {
                    currY--;
                    p.add(target.below());
                }

                if (currX != x2 && (currZ == z2 || Math.abs(x2 - currX) > Math.abs(z2 - currZ))) {
                    currX += sx;
                } else if (currZ != z2) {
                    currZ += sz;
                }
                
                if (p.size() > 32) break; // Limit path length
            }
            return p;
        }

        @Override
        public void tick() {
            if (path == null || path.isEmpty()) {
                stop();
                return;
            }

            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.CONNECTING, 40);
            }

            BlockPos target = path.get(0);
            double distSq = golem.blockPosition().distSqr(target);

            if (distSq > 4) {
                golem.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2D);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
                if (++delay % 10 == 0) {
                    placeRedstone(target);
                    path.remove(0);
                    // If path is long, we might need a repeater
                    if (path.size() > 0 && (path.size() % 14 == 0)) {
                        if (!path.isEmpty()) {
                            placeRepeater(path.remove(0));
                        }
                    }
                }
            }
        }

        private void placeRedstone(BlockPos pos) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.world.item.Items.REDSTONE)) {
                    if (golem.level().getBlockState(pos).canBeReplaced()) {
                        golem.level().setBlock(pos, net.minecraft.world.level.block.Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
                        stack.shrink(1);
                        golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }

        private void placeRepeater(BlockPos pos) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.world.item.Items.REPEATER)) {
                    if (golem.level().getBlockState(pos).canBeReplaced()) {
                        // Direction should be facing towards endPos
                        Direction facing = net.minecraft.core.Direction.fromYRot(golem.getYRot());
                        golem.level().setBlock(pos, net.minecraft.world.level.block.Blocks.REPEATER.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing), 3);
                        stack.shrink(1);
                        golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }
    }
    public static class BreedAnimalsGoal extends Goal {
        private UtilityGolem golem;
        private Animal animalA;
        private Animal animalB;
        private int delay;

        public BreedAnimalsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        private boolean isCompatibleMate(Animal a, Animal b) {
            if (a == b) return false;
            if (a.getClass() == b.getClass()) return true;

            // InteractionHandle Horse/Donkey crossbreeding
            if (a instanceof AbstractHorse && b instanceof AbstractHorse) {
                if (a instanceof net.minecraft.world.entity.animal.equine.Horse || a instanceof net.minecraft.world.entity.animal.equine.Donkey) {
                    if (b instanceof net.minecraft.world.entity.animal.equine.Horse || b instanceof net.minecraft.world.entity.animal.equine.Donkey) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean canUse() {
            ItemStack food = getAnyBreedingItem();
            if (food.isEmpty()) return false;

            BlockPos chestPos = golem.getChestPos();
            List<Animal> animals = golem.level()
                    .getEntitiesOfClass(
                            Animal.class,
                            golem.getBoundingBox().inflate(16.0),
                            a -> {
                                boolean ok = a.isAlive() && a.getAge() == 0 && !a.isInLove() && a.canFallInLove() && isReadyForBreeding(a);
                                if (!ok && golem.getRandom().nextInt(100) == 0) {
                                    golem.debugLog("BreedAnimalsGoal: Animal " + a.getType().getDescriptionId() + " at " + a.blockPosition().toShortString() + " NOT READY: alive=" + a.isAlive() + ", tickCount=" + a.getAge() + ", inLove=" + a.isInLove() + ", canEat=" + a.canFallInLove() + ", isReady=" + isReadyForBreeding(a));
                                }
                                return ok;
                            }
                    );

            for (int i = 0; i < animals.size(); i++) {
                for (int j = i + 1; j < animals.size(); j++) {
                    Animal a = animals.get(i);
                    Animal b = animals.get(j);

                    if (isCompatibleMate(a, b)) {
                        // Check if we have food for THIS specific animal type
                        ItemStack specificFood = getBreedingItemFor(a);
                        if (specificFood.isEmpty()) {
                            if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: No food for animal " + a.getType().getDescription().getString());
                            continue;
                        }

                        if (specificFood.getCount() < 2) {
                            if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Not enough food (" + specificFood.getCount() + ") for " + a.getType().getDescription().getString());
                            continue;
                        }

                        // RELAXED: Removed chest distance check if we have enough food.
                        // Golems should be able to breed animals anywhere if they have the food.
                        // They only need the chest to WITHDRAW more food.

                        animalA = a;
                        animalB = b;
                        golem.debugLog("BreedAnimalsGoal: Selected " + a.getType().getDescription().getString() + " pair for breeding");
                        return true;
                    }
                }
            }
            if (!animals.isEmpty()) {
                golem.debugLog("BreedAnimalsGoal: Found " + animals.size() + " animals, but no matching pair could be bred.");
            }
            return false;
        }

        private boolean isReadyForBreeding(Animal animal) {
            if (animal instanceof TamableAnimal tameable) {
                if (!tameable.isTame()) {
                    if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Animal not tamed.");
                    return false;
                }
                if (animal instanceof Wolf || animal instanceof Cat) {
                    if (animal.getHealth() < animal.getMaxHealth()) {
                        if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Wolf/Cat not at full health.");
                        return false;
                    }
                }
            }
            if (animal instanceof AbstractHorse horse) {
                if (!horse.isTamed()) {
                    if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Horse not tamed.");
                    return false;
                }
            }
            if (animal instanceof Llama llama) {
                if (!llama.isTamed()) {
                    if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Llama not tamed.");
                    return false;
                }
            }
            if (animal instanceof Panda panda) {
                if (!hasEnoughBambooNearby(panda)) {
                    if (golem.getRandom().nextInt(100) == 0) golem.debugLog("BreedAnimalsGoal: Panda needs more bamboo.");
                    return false;
                }
            }
            return true;
        }

        private boolean hasEnoughBambooNearby(Panda panda) {
            BlockPos pos = panda.blockPosition();
            int bambooCount = 0;
            for (BlockPos p : net.minecraft.core.BlockPos.betweenClosed(pos.offset(-5, -2, -5), pos.offset(5, 2, 5))) {
                if (golem.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.BAMBOO)) {
                    if (++bambooCount >= 8) return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return animalA != null && animalB != null
                    && animalA.isAlive()
                    && animalB.isAlive()
                    && animalA.getAge() == 0
                    && animalB.getAge() == 0
                    && !animalA.isInLove()
                    && !animalB.isInLove();
        }

        @Override
        public void start() {
            delay = 0;
            golem.setAnimation(GolemAnimation.BREEDING, 40);
        }

        @Override
        public void stop() {
            animalA = null;
            animalB = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            // Ensure animation is active while breeding
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.BREEDING, 40);
            }

            if (animalA == null || animalB == null) return;

            double ax = animalA.getX();
            double ay = animalA.getY();
            double az = animalA.getZ();
            double bx = animalB.getX();
            double by = animalB.getY();
            double bz = animalB.getZ();
            Vec3 center = new Vec3((ax + bx) * 0.5, (ay + by) * 0.5, (az + bz) * 0.5);
            if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                golem.getNavigation().moveTo(center.x, center.y, center.z, 1.1D);
            }
            golem.getLookControl().setLookAt(center.x, center.y, center.z);

            if (++delay >= 40) {
                breed();
                // Don't call stop() here, let shouldContinue handle it so we can potentially chain if I refactored it more, 
                // but for now it will just finish this pair and canStart will find next pair immediately since lastBreedTime is gone.
            }
        }

        private void breed() {
            ItemStack food = getBreedingItemFor(animalA);
            if (food.isEmpty() || food.getCount() < 2) return;

            // Consume 2 items (one for each parent)
            food.shrink(2);

            // Amethyst golem assists with breeding: trigger vanilla love on parents
            if (golem.level() instanceof ServerLevel serverLevel) {
                animalA.setInLove(null);
                animalB.setInLove(null);
                // Visuals
                Vec3 center = new Vec3((animalA.getX() + animalB.getX()) * 0.5, (animalA.getY() + animalB.getY()) * 0.5, (animalA.getZ() + animalB.getZ()) * 0.5);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, center.x, center.y + 0.5, center.z, 7, 0.5, 0.5, 0.5, 0.05);
                serverLevel.playSound(null, center.x, center.y, center.z, net.minecraft.sounds.SoundEvents.CHORUS_FLOWER_GROW, SoundSource.NEUTRAL, 0.7F, 1.2F);
            }

            // Short cooldown before attempting another breed action to avoid spamming
            this.delay = 0;
        }

        private ItemStack getAnyBreedingItem() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && isValidBreedingItem(stack)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getBreedingItemFor(Animal animal) {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    // Check standard animal isBreedingItem
                    if (animal.isFood(stack)) {
                        return stack;
                    }
                    // Special case for some animals that might not use isBreedingItem correctly in some versions/mods
                    if (animal instanceof net.minecraft.world.entity.animal.cow.Cow || animal instanceof net.minecraft.world.entity.animal.sheep.Sheep) {
                        if (stack.is(net.minecraft.world.item.Items.WHEAT)) return stack;
                    }
                    if (animal instanceof net.minecraft.world.entity.animal.pig.Pig) {
                        if (stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT)) return stack;
                    }
                    if (animal instanceof net.minecraft.world.entity.animal.chicken.Chicken) {
                        if (stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS)) return stack;
                    }
                    if (animal instanceof net.minecraft.world.entity.animal.equine.AbstractHorse) {
                        if (stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE) || stack.is(net.minecraft.world.item.Items.GOLDEN_CARROT) || stack.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)) return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        private boolean isValidBreedingItem(ItemStack stack) {
            return GolemAI.isValidBreedingItem(stack);
        }
    }
    public static class ChopTreeGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private BlockPos currentTreePos;
        private int breakingTime;
        private int maxBreakingTime;
        private int stuckTicks;
        private Vec3 lastPos;
        private final java.util.Map<BlockPos, Long> failedBlocks = new java.util.HashMap<>();

        public ChopTreeGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ItemStack tool = golem.getHeldItem();
            if (tool.isEmpty() || (!UtilityGolem.isAxe(tool) && !UtilityGolem.isShears(tool))) {
                // Check if we have an axe or shears in golem.getInventory()
                if (hasAxeInInventory() || hasShearsInInventory()) {
                    // We can start, but we'll need to swap in tick()
                } else {
                    return false;
                }
            }
            if (shouldStopToDeposit()) {
                return false;
            }

            // Cleanup failed blocks older than 30 seconds
            long now = golem.level().getGameTime();
            failedBlocks.entrySet().removeIf(entry -> now - entry.getValue() > 600);

            // If it's a deepslate golem, we should always allow chopping if there's a tool and golem.getInventory() space.
            // Previously there was a check that stopped chopping if the golem had enough saplings, 
            // but this made the golem idle when it should be clearing trees.
            
            targetPos = findTargetBlock();
            if (targetPos != null) {
                golem.debugLog("ChopTreeGoal: canStart - found target at " + targetPos.toShortString());
                this.maxBreakingTime = calculateBreakingTime(golem.getHeldItem(), targetPos);
                return true;
            } else {
                // golem.debugLog("ChopTreeGoal: canStart - no target found");
            }
            return false;
        }

        private boolean hasAxeInInventory() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isAxe(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean hasEnoughSaplings() {
            int count = 0;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER)) {
                    count += stack.getCount();
                }
            }
            return count >= 8;
        }

        private boolean shouldStopToDeposit() {
            if (isInventoryFull()) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.STICK)) return true; // Always deposit sticks
                if (stack.getCount() >= stack.getMaxStackSize()) return true; // Deposit full stacks
            }
            return false;
        }

        private boolean isInventoryFull() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        private boolean findNearbySaplingsOnGround() {
            List<net.minecraft.world.entity.item.ItemEntity> items = golem.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    golem.getBoundingBox().inflate(16.0, 4.0, 16.0),
                    item -> item.getItem().is(net.minecraft.tags.ItemTags.SAPLINGS)
            );
            return !items.isEmpty();
        }

        private int calculateBreakingTime(ItemStack tool, BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            float hardness = state.getDestroySpeed(golem.level(), pos);
            if (hardness < 0) return 200;

            float speed = 1.0f;
            if (tool != null && !tool.isEmpty()) {
                speed = tool.getDestroySpeed(state);
                
                // If the tool is efficient against this block, apply efficiency enchantment
                if (speed > 1.0f && golem.level() instanceof ServerLevel serverLevel) {
                    int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), tool);
                    if (efficiencyLevel > 0) {
                        speed += (float)(efficiencyLevel * efficiencyLevel + 1);
                    }
                }
            }

            return Math.max(1, (int) (hardness * 30 / speed));
        }

        private BlockPos findTargetBlock() {
            BlockPos chestPos = golem.getChestPos();
            
            // Priority 1: Find connected log/leaf to current tree
            if (currentTreePos != null) {
                // If the current tree is way too far from the golem (e.g. golem was teleported),
                // or too far from the chest, reset it.
                if (currentTreePos.distSqr(golem.blockPosition()) > 1024 ||
                    (chestPos != null && currentTreePos.distSqr(chestPos) > 1024)) {
                    currentTreePos = null;
                } else {
                    BlockPos connected = findConnectedTarget(currentTreePos);
                    if (connected != null) {
                        return connected;
                    }
                    // If no more connected blocks found for this tree, but we were just chopping a tree,
                    // we might have finished the logs but not the leaves, or there might be a gap.
                    // However, findConnectedTarget with range 5 should catch most things.
                    currentTreePos = null; 
                }
            }

            // Only look for a NEW tree if we don't have a current one
            if (currentTreePos != null) {
                return null; // Should have been handled above
            }

            BlockPos pos = golem.blockPosition();
            BlockPos closest = null;
            double minDistSq = Double.MAX_VALUE;
            int range = 15;

            // Detect if any nearby log is part of a 2x2 tree
            BlockPos treeBase = find2x2TreeBase(pos, range);

            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (canChop(p) && !failedBlocks.containsKey(p) && !golem.isBlacklisted(p)) {
                            // Only chop if within 32 blocks of chest (if chest is known)
                            if (chestPos == null || p.distSqr(chestPos) < 1024) {
                                double distSq = p.distSqr(golem.blockPosition());
                                // Heavy bias towards logs to prioritize them over leaves
                                double leafPenalty = hasShearsInInventory() ? 10.0 : 1000.0;
                                // Add a Y penalty to favor lower blocks (like the base of the tree)
                                double yPenalty = (p.getY() - golem.getY()) * 4.0;
                                double chorusFlowerBonus = isChorusFlower(p) ? -1000.0 : 0.0;
                                double bottomChorusPenalty = isBottomChorus(p) ? 500.0 : 0.0;
                                
                                double score = distSq + (isLog(p) ? 0 : leafPenalty) + Math.max(0, yPenalty) + chorusFlowerBonus + bottomChorusPenalty;

                                // If it's a 2x2 tree, we want to leave a spiral staircase
                                if (treeBase != null && isPartOf2x2(p, treeBase)) {
                                    if (isStaircaseBlock(p, treeBase)) {
                                        score += 10000.0; // Very low priority
                                    }
                                }

                                if (score < minDistSq) {
                                    minDistSq = score;
                                    closest = p;
                                }
                            }
                        }
                    }
                }
            }
            
            // If the closest block is not at the ground level of the tree, try to find the base of that same tree
            if (closest != null && isLog(closest) && !isChorus(closest)) {
                // If it's part of a 2x2, we already handled priority. 
                // If it's NOT part of 2x2, we still want to go to the base.
                if (treeBase == null || !isPartOf2x2(closest, treeBase)) {
                    BlockPos base = closest;
                    while (isLog(base.below()) && base.getY() > golem.level().getMinY()) {
                        base = base.below();
                    }
                    closest = base;
                }
            }
            
            if (closest != null) {
                currentTreePos = closest;
            }
            return closest;
        }

        private BlockPos find2x2TreeBase(BlockPos pos, int range) {
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (isLog(p) && isLog(p.east()) && isLog(p.south()) && isLog(p.east().south())) {
                            // Found a 2x2 base (lowest found in this search)
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean isPartOf2x2(BlockPos p, BlockPos base) {
            return p.getX() >= base.getX() && p.getX() <= base.getX() + 1 &&
                   p.getZ() >= base.getZ() && p.getZ() <= base.getZ() + 1;
        }

        private boolean isStaircaseBlock(BlockPos p, BlockPos base) {
            int dy = p.getY() - base.getY();
            if (dy < 0) return false;
            // Spiral: (0,0) -> (1,0) -> (1,1) -> (0,1)
            int[][] pattern = {{0,0}, {1,0}, {1,1}, {0,1}};
            int[] pos = pattern[dy % 4];
            return p.getX() == base.getX() + pos[0] && p.getZ() == base.getZ() + pos[1];
        }

        private BlockPos findConnectedTarget(BlockPos startPos) {
            BlockPos chestPos = golem.getChestPos();
            int searchRange = 5; // Increased range to better catch all blocks in large/gapped trees
            BlockPos closest = null;
            double minDistSq = Double.MAX_VALUE;

            // Detect if we are in a 2x2 tree
            BlockPos treeBase = find2x2TreeBase(startPos, searchRange);

            for (int x = -searchRange; x <= searchRange; x++) {
                for (int y = -searchRange; y <= searchRange; y++) {
                    for (int z = -searchRange; z <= searchRange; z++) {
                        BlockPos p = startPos.offset(x, y, z);
                        if (canChop(p) && !failedBlocks.containsKey(p) && !golem.isBlacklisted(p)) {
                            // Only chop if within 32 blocks of chest (if chest is known)
                            if (chestPos == null || p.distSqr(chestPos) < 1024) {
                                double distSq = p.distSqr(golem.blockPosition());
                                // Heavy bias towards logs to prioritize them over leaves
                                double leafPenalty = hasShearsInInventory() ? 10.0 : 1000.0;
                                double chorusFlowerBonus = isChorusFlower(p) ? -100.0 : 0.0;
                                double bottomChorusPenalty = isBottomChorus(p) ? 50.0 : 0.0;
                                double score = distSq + (isLog(p) ? 0 : leafPenalty) + chorusFlowerBonus + bottomChorusPenalty;

                                // If it's a 2x2 tree, we want to leave a spiral staircase
                                if (treeBase != null && isPartOf2x2(p, treeBase)) {
                                    if (isStaircaseBlock(p, treeBase)) {
                                        score += 10000.0; // Very low priority
                                    }
                                }

                                if (score < minDistSq) {
                                    minDistSq = score;
                                    closest = p;
                                }
                            }
                        }
                    }
                }
            }
            return closest;
        }

        private boolean canChop(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            if (state.is(net.minecraft.tags.BlockTags.LOGS)) return true;
            if (state.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER)) {
                if (!golem.isChorusReady(pos)) return false;
                // If it's a flower on end stone, it's a newly planted one.
                // We should only break it once it has grown into a stem (becoming a CHORUS_PLANT)
                // and new flowers have grown above/around it.
                return !golem.level().getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.END_STONE);
            }
            if (state.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT)) return true;
            if (state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                // Deepslate golems with shears should always be allowed to collect leaves.
                if (golem.getGolemType() == GolemType.DEEPSLATE && hasShearsInInventory()) {
                    return true;
                }

                // If using an axe, only break leaves if we don't have shears in golem.getInventory()
                // AND it's a "necessary" leaf (in this case, we'll allow it if there are no logs left)
                // But the scoring already prioritizes logs.
                // To truly only break "necessary" leaves with an axe, we can restrict it further.
                ItemStack tool = golem.getHeldItem();
                if (UtilityGolem.isAxe(tool)) {
                    // If we have shears in golem.getInventory(), we can eventually swap to them and chop this leaf.
                    if (hasShearsInInventory()) return true;
                    
                    // If we ONLY have an axe, we should only chop leaves if we are already in the "tree-chopping"
                    // phase (meaning currentTreePos is not null and it was a log tree).
                    // This prevents the golem from wandering off to chop random leaves.
                    return currentTreePos != null && isLog(currentTreePos); 
                }
                return true; // Shears can always break leaves
            }
            return false;
        }

        private boolean hasShearsInInventory() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (UtilityGolem.isShears(inv.getItem(i))) return true;
            }
            return false;
        }

        private boolean isChorus(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            return state.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT) || state.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER);
        }

        private boolean isChorusFlower(BlockPos pos) {
            return golem.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER);
        }

        private boolean isBottomChorus(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            if (state.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER)) {
                // A flower directly on end stone is newly planted.
                return golem.level().getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.END_STONE);
            }
            // A stem on end stone means the flower has changed into a stem (grown).
            return state.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT) && golem.level().getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.END_STONE);
        }

        private boolean isLog(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            return state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT) || state.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER);
        }

        @Override
        public void start() {
            breakingTime = 0;
            stuckTicks = 0;
            lastPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
            golem.setAnimation(GolemAnimation.CHOPPING, Math.min(100, Math.max(40, this.maxBreakingTime)));
        }

        @Override
        public boolean canContinueToUse() {
            ItemStack tool = golem.getHeldItem();
            return targetPos != null && canChop(targetPos) && !tool.isEmpty() && (UtilityGolem.isAxe(tool) || UtilityGolem.isShears(tool)) &&
                    breakingTime < maxBreakingTime && golem.blockPosition().distSqr(targetPos) < 400 &&
                    stuckTicks < 100 && !shouldStopToDeposit();
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.level().destroyBlockProgress(golem.getId(), targetPos, -1);
                if (stuckTicks >= 100) {
                    failedBlocks.put(targetPos, golem.level().getGameTime());
                }
            }
            targetPos = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Ensure animation is active while chopping
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.CHOPPING, 40);
            }

            // Stuck detection
            Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
            if (lastPos != null && currentPos.distanceToSqr(lastPos) < 0.01 * 0.01) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }
            lastPos = currentPos;

            // Deepslate Golem tool switching logic
            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                boolean targetIsLog = isLog(targetPos);
                ItemStack currentHeld = golem.getHeldItem();
                if (targetIsLog && !UtilityGolem.isAxe(currentHeld)) {
                    swapTool(UtilityGolem::isAxe);
                } else if (!targetIsLog && !UtilityGolem.isShears(currentHeld)) {
                    swapTool(UtilityGolem::isShears);
                }
            }

            double dx = golem.getX() - (targetPos.getX() + 0.5);
            double dy = golem.getY() - (targetPos.getY() + 0.5);
            double dz = golem.getZ() - (targetPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);
            double totalDistSq = horizontalDistSq + dy * dy;

            // If it's too far (more than ~4.5 blocks away horizontally OR 15 blocks vertically)
            if (horizontalDistSq > 20.0D || verticalDist > 15.0D) {
                if (stuckTicks % 20 == 0) {
                    golem.debugLog("ChopTreeGoal: Too far - horizontalDistSq: " + horizontalDistSq + ", verticalDist: " + verticalDist + ", stuck: " + stuckTicks);
                }
                if (stuckTicks > 100) {
                    golem.blacklistPosition(targetPos);
                    golem.debugLog("ChopTreeGoal: Blacklisting position " + targetPos.toShortString() + " due to stuckTicks");
                    targetPos = null;
                    return;
                }
                // If it's high up, move to the base of it
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // If it's high up, move to the base of it, BUT try to move to target height if possible
                    if (verticalDist > 2.0D) {
                        // First try moving to the target's height (climbing)
                        possible = golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.2D);
                        // If that fails, move to the base at ground level
                        if (!possible) {
                            possible = golem.getNavigation().moveTo(targetPos.getX() + 0.5, golem.getY(), targetPos.getZ() + 0.5, 1.2D);
                        }
                    } else {
                        possible = golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.2D);
                    }
                    if (!possible) {
                        golem.blacklistPosition(targetPos);
                        golem.debugLog("ChopTreeGoal: Pathfinding impossible to " + targetPos.toShortString());
                        targetPos = null;
                        return;
                    }
                }
                breakingTime = 0;
            } else {
                if (!golem.getNavigation().isDone()) {
                    golem.getNavigation().stop();
                }
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

                if (breakingTime % 5 == 0) {
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }

                breakingTime++;
                if (breakingTime % 20 == 0) {
                    // golem.debugLog("ChopTreeGoal: Breaking... " + breakingTime + "/" + maxBreakingTime);
                }
                int progress = (int) ((float) breakingTime / (float) maxBreakingTime * 10.0F);
                golem.level().destroyBlockProgress(golem.getId(), targetPos, progress);

                if (breakingTime >= maxBreakingTime) {
                    golem.debugLog("ChopTreeGoal: Breaking block at " + targetPos.toShortString());
                    breakBlock();
                }
            }
        }

        private int getToolScore(ItemStack stack, BlockPos target) {
            if (stack.isEmpty()) return -1;
            int score = 0;
            BlockState state = golem.level().getBlockState(target);
            
            // Prioritize correct tool for the block type
            if (state.is(net.minecraft.tags.BlockTags.LOGS) && UtilityGolem.isAxe(stack)) {
                score += 100;
            } else if (state.is(net.minecraft.tags.BlockTags.LEAVES) && UtilityGolem.isShears(stack)) {
                score += 100;
            }

            if (golem.level() instanceof ServerLevel serverLevel) {
                var registry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), stack) * 10;
                
                if (state.is(net.minecraft.tags.BlockTags.LOGS)) {
                    // Silk touch on logs?
                    score += EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), stack) * 20;
                }
            }
            
            if (stack.is(net.minecraft.world.item.Items.NETHERITE_AXE)) score += 5;
            else if (stack.is(net.minecraft.world.item.Items.DIAMOND_AXE)) score += 4;
            else if (stack.is(net.minecraft.world.item.Items.IRON_AXE)) score += 3;
            else if (stack.is(net.minecraft.world.item.Items.GOLDEN_AXE)) score += 6;
            
            return score;
        }

        private void swapTool(java.util.function.Predicate<ItemStack> toolPredicate) {
            SimpleContainer inv = golem.getInventory();
            ItemStack currentHeld = golem.getHeldItem();
            
            int bestSlot = -1;
            int bestScore = getToolScore(currentHeld, targetPos);
            
            // If we are holding a tool that doesn't match the predicate, we should definitely swap if we have one that does
            boolean currentMatches = toolPredicate.test(currentHeld);

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (toolPredicate.test(stack)) {
                    int score = getToolScore(stack, targetPos);
                    if (!currentMatches || score > bestScore) {
                        bestScore = score;
                        bestSlot = i;
                        currentMatches = true; // Now we have at least one matching tool
                    }
                }
            }
            
            if (bestSlot != -1) {
                ItemStack newTool = inv.removeItem(bestSlot, 1);
                if (!currentHeld.isEmpty()) {
                    ItemStack remaining = inv.addItem(currentHeld);
                    if (!remaining.isEmpty()) {
                        golem.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(golem.level(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                    }
                }
                golem.setHeldItem(newTool);
                // Recalculate maxBreakingTime for the new tool
                this.maxBreakingTime = calculateBreakingTime(newTool, targetPos);
            }
        }

        private void breakBlock() {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;

            BlockState state = serverLevel.getBlockState(targetPos);
            if (canChop(targetPos)) {
                ItemStack tool = golem.getHeldItem();
                
                net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(targetPos))
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, tool)
                        .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, golem);

                serverLevel.destroyBlock(targetPos, false, golem, 512);

                List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, targetPos, null, golem, tool);
                for (ItemStack drop : drops) {
                    ItemStack remaining = golem.getInventory().addItem(drop);
                    if (!remaining.isEmpty()) {
                        net.minecraft.world.level.block.Block.popResource(serverLevel, targetPos, remaining);
                    }
                }

                if (!tool.isEmpty()) {
                    if (UtilityGolem.isAxe(tool) || UtilityGolem.isShears(tool)) {
                        tool.hurtAndBreak(1, serverLevel, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                    }
                }
                
                // Update current tree position to the one we just broke to find its neighbors
                currentTreePos = targetPos;
            }
            targetPos = null;
        }
    }
    public static class ReplantSaplingGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int stuckTicks;
        private Vec3 lastPos;
        private final java.util.Map<BlockPos, Long> failedPositions = new java.util.HashMap<>();

        public ReplantSaplingGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (getSaplingFromInventory().isEmpty()) {
                return false;
            }
            if (shouldStopToDeposit()) {
                return false;
            }

            // Cleanup failed positions older than 30 seconds
            long now = golem.level().getGameTime();
            failedPositions.entrySet().removeIf(entry -> now - entry.getValue() > 600);

            targetPos = findPlantingPos();
            return targetPos != null;
        }

        private boolean shouldStopToDeposit() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.is(net.minecraft.world.item.Items.STICK)) return true; // Always deposit sticks
                if (stack.getCount() >= stack.getMaxStackSize()) return true; // Deposit full stacks
            }
            return false;
        }

        private ItemStack getSaplingFromInventory() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && (stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER))) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private BlockPos findPlantingPos() {
            BlockPos pos = golem.blockPosition();
            BlockPos chestPos = golem.getChestPos();
            int range = 8;
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    for (int y = -2; y <= 2; y++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (canPlantAt(p) && !failedPositions.containsKey(p)) {
                            // Sparse pattern check: No other saplings within 3 blocks
                            if (isSparse(p)) {
                                // Only plant if within 32 blocks of chest (if chest is known)
                                if (chestPos == null || p.distSqr(chestPos) < 1024) {
                                    return p;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean canPlantAt(BlockPos pos) {
            net.minecraft.world.level.Level world = golem.level();
            BlockState state = world.getBlockState(pos);
            BlockState floor = world.getBlockState(pos.below());
            ItemStack sapling = getSaplingFromInventory();
            if (sapling.is(net.minecraft.world.item.Items.CHORUS_FLOWER)) {
                return state.is(net.minecraft.world.level.block.Blocks.AIR) && floor.is(net.minecraft.world.level.block.Blocks.END_STONE);
            }
            // Can plant on dirt, grass, moss, or podzol
            return state.is(net.minecraft.world.level.block.Blocks.AIR) && (floor.is(net.minecraft.tags.BlockTags.DIRT) || floor.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || floor.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK) || floor.is(net.minecraft.world.level.block.Blocks.PODZOL));
        }

        private boolean isSparse(BlockPos pos) {
            net.minecraft.world.level.Level world = golem.level();
            ItemStack sapling = getSaplingFromInventory();
            if (sapling.is(net.minecraft.world.item.Items.CHORUS_FLOWER)) {
                // Chorus can be planted closer together than trees if desired, 
                // but let's stick to a similar sparse check for consistency.
                int sparseRange = 2;
                for (int x = -sparseRange; x <= sparseRange; x++) {
                    for (int z = -sparseRange; z <= sparseRange; z++) {
                        if (x == 0 && z == 0) continue;
                        BlockPos p = pos.offset(x, 0, z);
                        BlockState s = world.getBlockState(p);
                        if (s.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT) || s.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            int sparseRange = 3;
            for (int x = -sparseRange; x <= sparseRange; x++) {
                for (int z = -sparseRange; z <= sparseRange; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos p = pos.offset(x, 0, z);
                    BlockState s = world.getBlockState(p);
                    // Check if it's a sapling or a log (tree already there)
                    if (s.is(net.minecraft.tags.BlockTags.SAPLINGS) || s.is(net.minecraft.tags.BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public void start() {
            stuckTicks = 0;
            lastPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
        }

        @Override
        public void stop() {
            if (stuckTicks >= 100 && targetPos != null) {
                failedPositions.put(targetPos, golem.level().getGameTime());
            }
            targetPos = null;
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && canPlantAt(targetPos) && !getSaplingFromInventory().isEmpty() && stuckTicks < 100 && !shouldStopToDeposit();
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Stuck detection
            Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
            if (lastPos != null && currentPos.distanceToSqr(lastPos) < 0.01 * 0.01) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }
            lastPos = currentPos;

            double dx = golem.getX() - (targetPos.getX() + 0.5);
            double dy = golem.getY() - (targetPos.getY() + 0.5);
            double dz = golem.getZ() - (targetPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 2.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                ItemStack saplingStack = getSaplingFromInventory();
                if (!saplingStack.isEmpty()) {
                    Block saplingBlock = net.minecraft.world.level.block.Block.byItem(saplingStack.getItem());
                    if (saplingBlock != net.minecraft.world.level.block.Blocks.AIR) {
                        golem.level().setBlock(targetPos, saplingBlock.defaultBlockState(), 3);
                        if (saplingBlock == net.minecraft.world.level.block.Blocks.CHORUS_FLOWER) {
                            golem.recordChorusPlanting(targetPos);
                        }
                        saplingStack.shrink(1);
                        golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        targetPos = null;
                    }
                }
            }
        }
    }
    public static class TradeWithPiglinGoal extends Goal {
        private final UtilityGolem golem;
        private net.minecraft.world.entity.monster.piglin.Piglin targetPiglin;
        private int tradeDelay;
        private boolean waitingForPiglin;
        private net.minecraft.world.entity.item.ItemEntity suspectedTradedItem;
        private static final java.util.Map<net.minecraft.world.entity.monster.piglin.Piglin, UtilityGolem> CLAIMED_PIGLINS = new java.util.HashMap<>();

        public TradeWithPiglinGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getGolemType() != GolemType.GOLD) return false;
            // Only start if we have gold and aren't holding something that's NOT gold (unless it's empty)
            if (!hasGoldIngot()) return false;
            
            // Limit trades to 8 before depositing
            if (golem.getGoldTradeCount() >= 8) return false;

            // If we are holding something that isn't gold, maybe we should finish depositing it first?
            // Actually, if we have gold in golem.getInventory() but holding a trade result, we should probably finish that.
            if (!golem.getHeldItem().isEmpty() && !golem.getHeldItem().is(net.minecraft.world.item.Items.GOLD_INGOT)) return false;

            targetPiglin = findNearbyPiglin();
            if (targetPiglin != null) {
                CLAIMED_PIGLINS.put(targetPiglin, golem);
                return true;
            }
            return false;
        }

        private boolean hasGoldIngot() {
            if (golem.getHeldItem().is(net.minecraft.world.item.Items.GOLD_INGOT)) return true;
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.GOLD_INGOT)) return true;
            }
            return false;
        }

        private net.minecraft.world.entity.monster.piglin.Piglin findNearbyPiglin() {
            List<net.minecraft.world.entity.monster.piglin.Piglin> piglins = golem.level().getEntitiesOfClass(
                    net.minecraft.world.entity.monster.piglin.Piglin.class,
                    golem.getBoundingBox().inflate(16.0),
                    piglin -> piglin.isAlive() && !piglin.isBaby() && (!CLAIMED_PIGLINS.containsKey(piglin) || CLAIMED_PIGLINS.get(piglin) == golem)
            );
            return piglins.stream()
                    .min(Comparator.comparingDouble(golem::distanceToSqr))
                    .orElse(null);
        }

        @Override
        public boolean canContinueToUse() {
            if (targetPiglin == null || !targetPiglin.isAlive() || golem.distanceToSqr(targetPiglin) > 256) return false;
            
            if (CLAIMED_PIGLINS.get(targetPiglin) != golem) return false;

            if (waitingForPiglin) {
                // If we are waiting, we don't necessarily need gold ingot in golem.getInventory() right now (we just dropped it)
                return true;
            }
            
            return hasGoldIngot();
        }

        @Override
        public void start() {
            tradeDelay = 0;
            waitingForPiglin = false;
            suspectedTradedItem = null;
            golem.setAnimation(GolemAnimation.TRADING, 40);
        }

        @Override
        public void stop() {
            if (targetPiglin != null && CLAIMED_PIGLINS.get(targetPiglin) == golem) {
                CLAIMED_PIGLINS.remove(targetPiglin);
            }
            targetPiglin = null;
            waitingForPiglin = false;
            suspectedTradedItem = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        @Override
        public void tick() {
            if (targetPiglin == null) return;

            // Ensure animation is active while trading
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.TRADING, 40);
            }

            golem.getLookControl().setLookAt(targetPiglin, 30.0F, 30.0F);
            double distSq = golem.distanceToSqr(targetPiglin);

            if (distSq > 4.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().moveTo(targetPiglin, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                
                if (waitingForPiglin) {
                    // Check if piglin dropped something
                    if (suspectedTradedItem == null || !suspectedTradedItem.isAlive()) {
                        suspectedTradedItem = findNearbyDroppedItem();
                    }

                    if (suspectedTradedItem != null && suspectedTradedItem.isAlive()) {
                        if (golem.distanceToSqr(suspectedTradedItem) < 2.0D) {
                            pickupTradedItem(suspectedTradedItem);
                            waitingForPiglin = false;
                            suspectedTradedItem = null;
                            tradeDelay = 0;
                        } else {
                            // Move to item
                            golem.getNavigation().moveTo(suspectedTradedItem, 1.2D);
                        }
                    } else {
                        // Still waiting for piglin to finish admiring and drop
                        if (++tradeDelay > 100) { // Shortened timeout from 10s to 5s
                            waitingForPiglin = false;
                            tradeDelay = 0;
                        }
                    }
                    return;
                }

                // Ensure golem is holding gold ingot before trading
                if (!golem.getHeldItem().is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                    SimpleContainer inv = golem.getInventory();
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                            ItemStack held = golem.getHeldItem();
                            // If we were holding something else, try to put it in golem.getInventory()
                            if (!held.isEmpty()) {
                                ItemStack remaining = inv.addItem(held);
                                if (!remaining.isEmpty()) {
                                    // No space? Just drop it
                                    net.minecraft.world.level.block.Block.popResource(golem.level(), golem.blockPosition(), remaining);
                                }
                            }
                            ItemStack toHold = stack.copy();
                            toHold.setCount(1);
                            golem.setHeldItem(toHold);
                            stack.shrink(1);
                            break;
                        }
                    }
                }

                if (++tradeDelay % 10 == 0) {
                    if (isPiglinReady(targetPiglin) && golem.getHeldItem().is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                        dropGoldIngot();
                        waitingForPiglin = true;
                        tradeDelay = 0;
                    }
                }
            }
        }

        private net.minecraft.world.entity.item.ItemEntity findNearbyDroppedItem() {
            // Cactus Golems should NOT pick up items off the ground.
            if (golem.getGolemType() == GolemType.CACTUS) return null;
            List<net.minecraft.world.entity.item.ItemEntity> items = golem.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    golem.getBoundingBox().inflate(4.0),
                    item -> !item.hasPickUpDelay() && !item.getItem().is(net.minecraft.world.item.Items.GOLD_INGOT)
            );
            return items.stream()
                    .min(Comparator.comparingDouble(golem::distanceToSqr))
                    .orElse(null);
        }

        private void pickupTradedItem(net.minecraft.world.entity.item.ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack remaining = golem.getInventory().addItem(stack.copy());
            if (!remaining.isEmpty()) {
                golem.setHeldItem(remaining);
            } else {
                golem.setHeldItem(ItemStack.EMPTY);
            }
            itemEntity.discard();
            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            golem.incrementGoldTradeCount();
        }

        private boolean isPiglinReady(net.minecraft.world.entity.monster.piglin.Piglin piglin) {
            // Piglin is ready if it's not currently admiring a gold ingot or holding one to admire.
            // When admiring, they hold the item in their main hand and have a specific NBT or AI state.
            // However, simply checking if they are holding gold is a good start.
            ItemStack mainInteractionHand = piglin.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            ItemStack offInteractionHand = piglin.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND);
            return !mainInteractionHand.is(net.minecraft.world.item.Items.GOLD_INGOT) && !offInteractionHand.is(net.minecraft.world.item.Items.GOLD_INGOT);
        }

        private void dropGoldIngot() {
            ItemStack goldIngot = golem.getHeldItem();

            if (goldIngot.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                ItemStack toDrop = goldIngot.copy();
                toDrop.setCount(1);
                
                goldIngot.shrink(1);
                if (goldIngot.isEmpty()) {
                    golem.setHeldItem(ItemStack.EMPTY);
                }
                
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        golem.level(),
                        golem.getX(), golem.getY() + 1.0, golem.getZ(),
                        toDrop
                );
                itemEntity.setPickUpDelay(20);
                Vec3 targetPos = new Vec3(targetPiglin.getX(), targetPiglin.getY(), targetPiglin.getZ());
                Vec3 golemPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
                Vec3 velocity = targetPos.subtract(golemPos).normalize().scale(0.3);
                itemEntity.setDeltaMovement(velocity);
                golem.level().addFreshEntity(itemEntity);
                golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        }
    }
    public static class CraftEmeraldsGoal extends Goal {
        private final UtilityGolem golem;
        private int cooldown;

        public CraftEmeraldsGoal(UtilityGolem golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            return hasEmeraldBlocks();
        }

        private boolean hasEmeraldBlocks() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(net.minecraft.world.item.Items.EMERALD_BLOCK)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            craft();
            cooldown = 20;
        }

        private void craft() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(net.minecraft.world.item.Items.EMERALD_BLOCK)) {
                    int count = stack.getCount();
                    stack.shrink(count);
                    
                    ItemStack emeralds = new ItemStack(net.minecraft.world.item.Items.EMERALD, count * 9);
                    ItemStack remaining = inv.addItem(emeralds);
                    
                    if (!remaining.isEmpty()) {
                        net.minecraft.world.level.block.Block.popResource(golem.level(), golem.blockPosition(), remaining);
                    }
                    
                    golem.debugLog("CraftEmeraldsGoal: Crafted " + (count * 9) + " emeralds from " + count + " blocks.");
                    golem.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

    public static class PickupItemGoal extends Goal {
        private final UtilityGolem golem;
        private net.minecraft.world.entity.item.ItemEntity targetItem;
        private int cooldown;
        private int stuckTicks;
        private Vec3 lastPos;

        private final java.util.Map<net.minecraft.world.entity.item.ItemEntity, Long> blacklistedItems = new java.util.HashMap<>();

        public PickupItemGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (isInventoryFull()) {
                cooldown = 40;
                return false;
            }

            // Cleanup blacklisted items older than 30 seconds
            long now = golem.level().getGameTime();
            blacklistedItems.entrySet().removeIf(entry -> now - entry.getValue() > 600 || !entry.getKey().isAlive());

            targetItem = findNearbyItem();
            if (targetItem == null) {
                cooldown = 20;
            } else {
                golem.debugLog("PickupItemGoal: Found item " + targetItem.getItem().getItem().getDescriptionId() + " at " + targetItem.blockPosition().toShortString());
            }
            return targetItem != null;
        }

        private boolean isInventoryFull() {
            SimpleContainer inv = golem.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        private net.minecraft.world.entity.item.ItemEntity findNearbyItem() {
            // Cactus Golems should NOT pick up items off the ground.
            if (golem.getGolemType() == GolemType.CACTUS) return null;
            // Bamboo Golems should prioritize harvesting over picking up items
            if (golem.getGolemType() == GolemType.BAMBOO) {
                if (hasMatureCropsNearby()) {
                    return null;
                }
            }

            // If golem.getInventory() is already full, don't look for items to pick up
            if (isInventoryFull()) return null;

            BlockPos chestPos = golem.getChestPos();
            
            // Amethyst Golems only pick up items within the range of their crop field (home chest)
            if (golem.getGolemType() == GolemType.AMETHYST) {
                if (chestPos == null) return null;
                
                List<net.minecraft.world.entity.item.ItemEntity> items = golem.level().getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(chestPos).inflate(16.0),
                        item -> {
                            if (item.hasPickUpDelay()) return false;
                            if (blacklistedItems.containsKey(item)) return false;
                            if (item.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(chestPos)) > 16.0 * 16.0) return false;
                            ItemStack stack = item.getItem();
                            return GolemAI.isValidBreedingItem(stack);
                        }
                );
                
                return items.stream()
                        .min(Comparator.comparingDouble(golem::distanceToSqr))
                        .orElse(null);
            }

            List<net.minecraft.world.entity.item.ItemEntity> items = golem.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    golem.getBoundingBox().inflate(16.0, 4.0, 16.0),
                    item -> {
                        if (item.hasPickUpDelay()) return false;
                        if (blacklistedItems.containsKey(item)) return false;
                        ItemStack stack = item.getItem();
                        
                        boolean isFamiliar = false;
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            boolean isAxe = UtilityGolem.isAxe(stack);
                            boolean isSapling = stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.world.item.Items.CHORUS_FLOWER);
                            boolean isChorusFruit = stack.is(net.minecraft.world.item.Items.CHORUS_FRUIT);
                            boolean isApple = stack.is(net.minecraft.world.item.Items.APPLE);
                            boolean isStick = stack.is(net.minecraft.world.item.Items.STICK);
                            isFamiliar = isAxe || isSapling || isChorusFruit || isApple || isStick;
                        } else if (golem.getGolemType() == GolemType.BAMBOO) {
                            boolean isHoe = UtilityGolem.isHoe(stack);
                            boolean isCrop = stack.is(net.minecraft.world.item.Items.WHEAT) || stack.is(net.minecraft.world.item.Items.CARROT) || stack.is(net.minecraft.world.item.Items.POTATO) || stack.is(net.minecraft.world.item.Items.BEETROOT) ||
                                            stack.is(net.minecraft.world.item.Items.NETHER_WART) || stack.is(net.minecraft.world.item.Items.COCOA_BEANS) || stack.is(net.minecraft.world.item.Items.PUMPKIN) || stack.is(net.minecraft.world.item.Items.MELON);
                            boolean isSeed = stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) || stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS) || stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) || stack.is(net.minecraft.world.item.Items.MELON_SEEDS) || stack.is(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS) || stack.is(net.minecraft.world.item.Items.PITCHER_POD);
                            isFamiliar = isHoe || isCrop || isSeed || stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET);
                        } else if (golem.getGolemType() == GolemType.DIAMOND) {
                            // User requested only blocks. Some "seeds" (like Wheat Seeds) are BlockItems but 
                            // user might not want them. For now, let's keep it to BlockItem but exclude common "seed/non-blocky" items if they cause confusion.
                            // But usually, Diamond Golem is for building.
                            isFamiliar = stack.getItem() instanceof net.minecraft.world.item.BlockItem && !stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) && !stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS) && !stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) && !stack.is(net.minecraft.world.item.Items.MELON_SEEDS);
                        } else if (golem.getGolemType() == GolemType.SPONGE) {
                            boolean isFishingRod = UtilityGolem.isFishingRod(stack);
                            isFamiliar = isFishingRod || stack.is(net.minecraft.world.item.Items.COD) || stack.is(net.minecraft.world.item.Items.SALMON) || stack.is(net.minecraft.world.item.Items.PUFFERFISH) || stack.is(net.minecraft.world.item.Items.TROPICAL_FISH) ||
                                            stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || stack.is(net.minecraft.world.item.Items.NAME_TAG) || stack.is(net.minecraft.world.item.Items.SADDLE) || stack.is(net.minecraft.world.item.Items.BOW);
                        } else if (golem.getGolemType() == GolemType.JUKEBOX) {
                            isFamiliar = stack.get(DataComponents.JUKEBOX_PLAYABLE) != null;
                        } else if (golem.getGolemType() == GolemType.GOLD) {
                            // Gold golem wants to pick up gold AND anything else (traded items)
                            isFamiliar = true; 
                        } else if (golem.getGolemType() == GolemType.LAMP) {
                            isFamiliar = UtilityGolem.isTorch(stack);
                        } else if (golem.getGolemType() == GolemType.EMERALD) {
                            boolean isEmerald = stack.is(net.minecraft.world.item.Items.EMERALD) || stack.is(net.minecraft.world.item.Items.EMERALD_BLOCK);
                            boolean isOnSellingList = golem.getDiscoveredTrades().stream().anyMatch(tradeStack -> ItemStack.isSameItemSameComponents(tradeStack, stack));
                            isFamiliar = isEmerald || isOnSellingList;
                        } else if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                            // Netherite golems pick up anything (especially from mobs they killed)
                            isFamiliar = true;
                        } else if (golem.getGolemType() == GolemType.NETHER_WART) {
                            boolean isIngredient = isIngredient(stack);
                            boolean isSupply = stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || stack.is(net.minecraft.world.item.Items.BLAZE_POWDER) || stack.is(net.minecraft.world.item.Items.BREWING_STAND);
                            boolean isPotionOrWater = BrewingGoal.isWaterBottleStatic(stack) || stack.is(net.minecraft.world.item.Items.POTION) || stack.is(net.minecraft.world.item.Items.SPLASH_POTION) || stack.is(net.minecraft.world.item.Items.LINGERING_POTION);
                            isFamiliar = isIngredient || isSupply || isPotionOrWater;
                        } else if (golem.getGolemType() == GolemType.HONEYCOMB) {
                            isFamiliar = stack.is(net.minecraft.world.item.Items.HONEYCOMB) || stack.is(net.minecraft.world.item.Items.HONEY_BOTTLE) || stack.is(net.minecraft.world.item.Items.SHEARS) || stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE);
                        } else if (golem.getGolemType() == GolemType.LAPIS) {
                            // Lapis golems pick up ores, raw ores, and mining-related blocks/tools
                            isFamiliar = stack.is(net.minecraft.tags.ItemTags.COAL_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.IRON_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.COPPER_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.GOLD_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.DIAMOND_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.EMERALD_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.LAPIS_ORES)
                                    || stack.is(net.minecraft.tags.ItemTags.REDSTONE_ORES)
                                    || stack.is(net.minecraft.world.item.Items.QUARTZ)
                                    || stack.is(net.minecraft.world.item.Items.RAW_IRON) || stack.is(net.minecraft.world.item.Items.RAW_GOLD) || stack.is(net.minecraft.world.item.Items.RAW_COPPER)
                                    || stack.is(net.minecraft.world.item.Items.COAL) || stack.is(net.minecraft.world.item.Items.DIAMOND) || stack.is(net.minecraft.world.item.Items.EMERALD)
                                    || stack.is(net.minecraft.world.item.Items.LAPIS_LAZULI) || stack.is(net.minecraft.world.item.Items.REDSTONE)
                                    || stack.is(net.minecraft.world.item.Items.ANCIENT_DEBRIS)
                                    || stack.is(net.minecraft.tags.ItemTags.PICKAXES)
                                    || stack.is(net.minecraft.tags.ItemTags.SHOVELS)
                                    || stack.is(net.minecraft.world.item.Items.COBBLESTONE) || stack.is(net.minecraft.world.item.Items.COBBLED_DEEPSLATE)
                                    || stack.is(net.minecraft.world.item.Items.DIRT) || stack.is(net.minecraft.world.item.Items.GRAVEL) || stack.is(net.minecraft.world.item.Items.SAND);
                        } else {
                            // Default: only pick up blocks to avoid cluttering golem.getInventory() with junk
                            isFamiliar = stack.getItem() instanceof net.minecraft.world.item.BlockItem && !stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS) && !stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS) && !stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) && !stack.is(net.minecraft.world.item.Items.MELON_SEEDS);
                        }
                        
                        if (!isFamiliar) return false;

                        // For Hopper Golem, don't pick up if it would create a duplicate slot
                        if (golem.getGolemType() == GolemType.HOPPER) {
                            if (isAlreadyInInventory(stack)) {
                                if (!canMergeIntoExistingStack(stack)) {
                                    return false;
                                }
                            }
                        }
                        
                        // Only pickup if within 32 blocks of chest (if chest is known)
                        if (chestPos != null) {
                            return item.distanceToSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024;
                        }
                        return true;
                    }
            );

            return items.stream()
                    .min(Comparator.comparingDouble(golem::distanceToSqr))
                    .orElse(null);
        }

        private boolean hasMatureCropsNearby() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return false;

            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    for (int y = -3; y <= 3; y++) {
                        BlockPos p = chestPos.offset(x, y, z);
                        if (p.equals(chestPos) || golem.isBlacklisted(p)) continue;
                        if (isMatureCrop(p)) return true;
                    }
                }
            }
            return false;
        }

        private boolean isMatureCrop(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof CropBlock crop) return crop.isMaxAge(state);
            if (block instanceof NetherWartBlock) return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3;
            if (block instanceof CocoaBlock) return state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE) >= 2;
            return false;
        }

        @Override
        public void start() {
            stuckTicks = 0;
            lastPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
        }

        @Override
        public boolean canContinueToUse() {
            return targetItem != null && targetItem.isAlive() && !isInventoryFull() &&
                    golem.distanceToSqr(targetItem) < 256 && stuckTicks < 60;
        }

        @Override
        public void stop() {
            if (stuckTicks >= 60 && targetItem != null) {
                blacklistedItems.put(targetItem, golem.level().getGameTime());
                golem.debugLog("PickupItemGoal: Blacklisting item " + targetItem.getItem().getItem().getDescriptionId() + " due to being stuck");
            }
            targetItem = null;
            if (stuckTicks >= 60) {
                cooldown = 100; // Longer cooldown if we got stuck
            }
        }

        @Override
        public void tick() {
            if (targetItem == null) return;

            // Stuck detection: if we are not moving much
            Vec3 currentPos = new Vec3(golem.getX(), golem.getY(), golem.getZ());
            if (currentPos.distanceToSqr(lastPos) < 0.005 * 0.005) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }
            lastPos = currentPos;

            double distSq = golem.distanceToSqr(targetItem);
            if (distSq > 4.0D) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(5) == 0) {
                    golem.getNavigation().moveTo(targetItem, 1.2D);
                }
                golem.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
            } else {
                pickup();
            }
        }

        private boolean isAlreadyInInventory(ItemStack stack) {
            SimpleContainer golemInv = golem.getInventory();
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (!golemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, golemStack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean canMergeIntoExistingStack(ItemStack stack) {
            SimpleContainer golemInv = golem.getInventory();
            for (int i = 0; i < golemInv.getContainerSize(); i++) {
                ItemStack golemStack = golemInv.getItem(i);
                if (!golemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, golemStack)) {
                    if (golemStack.getCount() < golemStack.getMaxStackSize()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void pickup() {
            ItemStack stack = targetItem.getItem();
            golem.debugLog("PickupItemGoal: Attempting pickup of " + stack.getItem().getDescriptionId());
            ItemStack remaining = golem.getInventory().addItem(stack);
            if (remaining.isEmpty()) {
                golem.debugLog("PickupItemGoal: Success");
                targetItem.discard();
            } else {
                golem.debugLog("PickupItemGoal: Partial/Fail, remaining: " + remaining.getCount());
                targetItem.setItem(remaining);
            }
            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            targetItem = null;
        }
    }
    public static class PlayRecordGoal extends Goal {
        private final UtilityGolem golem;

        public PlayRecordGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.noneOf(net.minecraft.world.entity.ai.goal.Goal.Flag.class));
        }

        @Override
        public boolean canUse() {
            return isRecord();
        }

        @Override
        public void start() {
            // Music is handled by UtilityGolem.tickJukebox() and world events
        }

        @Override
        public void stop() {
            // Music stop is handled by UtilityGolem.tick()
            golem.setSearching(false);
        }

        @Override
        public boolean canContinueToUse() {
            return isRecord();
        }

        public boolean isRecord() {
            return golem.getHeldItem().get(DataComponents.JUKEBOX_PLAYABLE) != null;
        }
    }
    public static class HoneyBabysitterGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int actionCooldown = 0;
        private int smokeCooldown = 0;

        public HoneyBabysitterGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public void start() {
            updateHeldItem();
        }

        @Override
        public boolean canUse() {
            if (actionCooldown > 0) {
                actionCooldown--;
                return false;
            }
            targetPos = findTargetHive();
            return targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (targetPos == null) return false;
            BlockState state = golem.level().getBlockState(targetPos);
            return isHive(state) && getHoneyLevel(state) >= 5;
        }

        @Override
        public void stop() {
            targetPos = null;
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

            double dist = golem.blockPosition().distSqr(targetPos);
            if (dist > 9.0) { // 3 blocks
                golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.1D);
            } else {
                golem.getNavigation().stop();
                
                updateHeldItem();
                
                // Smoke logic - purely visual particles
                if (golem.level() instanceof ServerLevel serverLevel) {
                    if (golem.level().getRandom().nextInt(5) == 0) {
                        double px = targetPos.getX() + 0.5 + (golem.level().getRandom().nextDouble() - 0.5);
                        double py = targetPos.getY() + 0.5 + (golem.level().getRandom().nextDouble() - 0.5);
                        double pz = targetPos.getZ() + 0.5 + (golem.level().getRandom().nextDouble() - 0.5);
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 1, 0, 0.07, 0, 0.0);
                    }
                }

                if (smokeCooldown > 0) {
                    smokeCooldown--;
                } else if (smokeCooldown == 0) {
                    smokeCooldown = 40; // Initial "smoking" time
                } else {
                    harvestHive();
                    actionCooldown = 40;
                }

                // If we've been smoking for a while, harvest
                if (smokeCooldown > 0) {
                   smokeCooldown--;
                   if (smokeCooldown == 1) {
                       harvestHive();
                       actionCooldown = 40;
                   }
                }
            }
        }

        private void updateHeldItem() {
            int shearsIdx = findItem(net.minecraft.world.item.Items.SHEARS);
            if (shearsIdx != -1) {
                golem.setHeldItem(golem.getInventory().getItem(shearsIdx).copyWithCount(1));
                return;
            }

            int bottleIdx = findItem(net.minecraft.world.item.Items.GLASS_BOTTLE);
            if (bottleIdx != -1) {
                golem.setHeldItem(golem.getInventory().getItem(bottleIdx).copyWithCount(1));
                return;
            }
        }

        private BlockPos findTargetHive() {
            BlockPos pos = golem.blockPosition();
            Level world = golem.level();
            for (BlockPos testPos : net.minecraft.core.BlockPos.betweenClosed(pos.offset(-8, -4, -8), pos.offset(8, 4, 8))) {
                BlockState state = world.getBlockState(testPos);
                if (isHive(state) && getHoneyLevel(state) >= 5) {
                    return testPos;
                }
            }
            return null;
        }

        private boolean isHive(BlockState state) {
            return state.is(net.minecraft.tags.BlockTags.BEEHIVES);
        }

        private int getHoneyLevel(BlockState state) {
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY)) {
                return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY);
            }
            return 0;
        }

        private void harvestHive() {
            Level world = golem.level();
            BlockState state = world.getBlockState(targetPos);
            
            // Try harvest honeycomb first (shears)
            int shearsIdx = findItem(net.minecraft.world.item.Items.SHEARS);
            if (shearsIdx != -1) {
                harvestHoneycomb(world, targetPos, state, shearsIdx);
                return;
            }

            // Try harvest honey (glass bottle)
            int bottleIdx = findItem(net.minecraft.world.item.Items.GLASS_BOTTLE);
            if (bottleIdx != -1) {
                harvestHoney(world, targetPos, state, bottleIdx);
                return;
            }
        }

        private void harvestHoneycomb(Level world, BlockPos pos, BlockState state, int slot) {
            if (golem.level().isClientSide()) return;
            
            golem.setAnimation(GolemAnimation.WITHDRAWING, 20);
            world.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Reset honey level without angering bees (simulate silk touch/smoke behavior)
            if (world instanceof ServerLevel serverLevel) {
                // In vanilla, bees are calmed by smoke. We simulate this by directly setting state
                // and ensuring no angry bees are spawned.
                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY, 0), 3);
                
                ItemStack honeycombs = new ItemStack(net.minecraft.world.item.Items.HONEYCOMB, 3);
                if (!golem.getInventory().addItem(honeycombs).isEmpty()) {
                    net.minecraft.world.level.block.Block.popResource(world, pos, honeycombs);
                }
                golem.setHeldItem(honeycombs.copyWithCount(1));
                
                // Damage shears
                ItemStack shears = golem.getInventory().getItem(slot);
                shears.hurtAndBreak(1, serverLevel, null, (item) -> {});
            }
        }

        private void harvestHoney(Level world, BlockPos pos, BlockState state, int slot) {
            if (golem.level().isClientSide()) return;

            golem.setAnimation(GolemAnimation.WITHDRAWING, 20);
            world.playSound(null, pos, net.minecraft.sounds.SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1.0F, 1.0F);

            // Reset honey level
            world.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY, 0), 3);

            // Consume bottle
            golem.getInventory().getItem(slot).shrink(1);

            // Add honey bottle
            ItemStack honeyBottle = new ItemStack(net.minecraft.world.item.Items.HONEY_BOTTLE);
            if (!golem.getInventory().addItem(honeyBottle).isEmpty()) {
                net.minecraft.world.level.block.Block.popResource(world, pos, honeyBottle);
            }
            golem.setHeldItem(honeyBottle.copy());
        }

        private int findItem(Item item) {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(item)) return i;
            }
            return -1;
        }
    }
    public static class PlaceBlockGoal extends Goal {
        private final UtilityGolem golem;
        private int patternProgress = 0;
        private BlockPos startPos = null;
        private int cooldown = 0;
        private BuildPattern lastPattern = BuildPattern.NONE;
        private Direction startFacing = null;
        private rehdpanda.utilitygolems.schematic.SchematicData schematic = null;
        private int schematicTotal = 0;
        private BlockPos schematicOrigin = null;

        public PlaceBlockGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.getBuildPattern() == BuildPattern.NONE) return false;
            if (!golem.isBuildingStarted()) return false;
            
            if (cooldown > 0) {
                return false;
            }
            
            // Reset progress if pattern changed
            if (golem.getBuildPattern() != lastPattern) {
                patternProgress = 0;
                lastPattern = golem.getBuildPattern();
            }

            // Allow schematic mode to start so that we can load and compute total in start()
            if (golem.getBuildPattern() == BuildPattern.SCHEMATIC) {
                return true;
            }

            if (golem.getBuildPattern() == BuildPattern.REPLACE) {
                // Replace mode needs blocks in golem.getInventory() to work
                return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
            }

            if (golem.getBuildPattern() == BuildPattern.TOWER) {
                // Tower mode needs blocks; ladders no longer required
                return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
            }

            return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
        }

        private boolean hasLadders() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                if (golem.getInventory().getItem(i).is(net.minecraft.world.item.Items.LADDER)) return true;
            }
            return false;
        }

        private int findBlockInInventory() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void start() {
            startPos = golem.blockPosition();
            startFacing = golem.getDirection();
            golem.debugLog("PlaceBlockGoal started. Mode: " + golem.getBuildPattern() + " | Pos: " + startPos.toShortString() + " | Facing: " + startFacing);
            if (golem.getBuildPattern() == BuildPattern.SCHEMATIC) {
                schematic = null;
                schematicTotal = 0;
                schematicOrigin = null;
                String name = golem.getSchematicName();
                golem.debugLog("Schematic Mode: file name = '" + name + "'");
                if (name != null && !name.isEmpty()) {
                    java.nio.file.Path dir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("utility-golems").resolve("schematics");
                    try { java.nio.file.Files.createDirectories(dir); } catch (Exception ignored) {}
                    golem.debugLog("Loading schematic from: " + dir.resolve(name));
                    schematic = rehdpanda.utilitygolems.schematic.SchematicLoader.load(dir, name);
                    if (schematic != null) {
                        schematicOrigin = startPos.relative(startFacing, 2);
                        golem.debugLog("Schematic loaded. Size: " + schematic.width + "x" + schematic.height + "x" + schematic.length + " | Origin: " + schematicOrigin.toShortString());
                        // Count non-air blocks
                        int total = schematic.getTotalBlocks();
                        int count = 0;
                        for (int i = 0; i < total; i++) {
                            net.minecraft.world.level.block.Block b = schematic.getBlockAtIndex(i);
                            if (b != null && b != net.minecraft.world.level.block.Blocks.AIR) count++;
                        }
                        schematicTotal = count;
                        golem.debugLog("Total buildable blocks in schematic: " + schematicTotal);
                        if (schematicTotal == 0) {
                            golem.debugLog("Warning: schematicTotal is 0. Golem might have nothing to build.");
                        }
                    } else {
                        golem.debugLog("Failed to load schematic. (SchematicLoader returned null)");
                    }
                } else {
                    golem.debugLog("No schematic name selected.");
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.getBuildPattern() == BuildPattern.NONE || !golem.isBuildingStarted() || golem.getBuildPattern() != lastPattern || startPos == null || patternProgress >= getMaxProgress()) {
                return false;
            }

            if (golem.getBuildPattern() == BuildPattern.TOWER) {
                return findBlockInInventory() != -1;
            }

            return findBlockInInventory() != -1;
        }

        private int getMaxProgress() {
            switch (golem.getBuildPattern()) {
                case PLATFORM:
                    int width = golem.getWallWidth();
                    int length = golem.getWallLength();
                    if (width <= 0 || length <= 0) return 0;
                    return width * length; // Single-layer solid platform
                case REPLACE: return 25; // Scan 5x5 area around start
                case TOWER: return 1000; // As many as it has (up to 1000)
                case SCHEMATIC: return schematicTotal;
                default: return 0;
            }
        }

        @Override
        public void tick() {
            if (cooldown > 0) {
                cooldown--;
                return;
            }
            if (patternProgress >= getMaxProgress()) {
                golem.debugLog("Pattern finished. Progress: " + patternProgress + "/" + getMaxProgress());
                patternProgress = 0;
                cooldown = 100;
                golem.setBuildingStarted(false);
                golem.getNavigation().stop();
                return;
            }

            BlockPos targetPos = getTargetPos();
            if (targetPos == null) {
                if (golem.getBuildPattern() == BuildPattern.SCHEMATIC && schematic != null) {
                     golem.debugLog("Schematic targetPos is null at progress " + patternProgress + "/" + schematicTotal);
                }
                return;
            }

            double distSq = golem.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            if (distSq > 4.0) {
                if (golem.getNavigation().isDone() || golem.getRandom().nextInt(10) == 0) {
                    golem.debugLog("Moving to target: " + targetPos.toShortString() + " | dist: " + Math.sqrt(distSq));
                    golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                if (golem.getBuildPattern() == BuildPattern.REPLACE) {
                    replaceBlock(targetPos);
                } else if (golem.getBuildPattern() == BuildPattern.TOWER) {
                    placeTowerStep(targetPos);
                } else if (golem.getBuildPattern() == BuildPattern.SCHEMATIC) {
                    placeSchematicBlock(targetPos);
                } else {
                    placeBlock(targetPos);
                }
            }
        }

        private BlockPos getTargetPos() {
            Direction facing = golem.getDirection();
            switch (golem.getBuildPattern()) {
                case PLATFORM:
                    Direction side = facing.getClockWise();
                    int width = golem.getWallWidth();
                    int length = golem.getWallLength();
                    if (width <= 0 || length <= 0) return null;
                    int dx = patternProgress % width;
                    int dz = patternProgress / width;
                    return startPos.relative(facing, 2 + dz).relative(side, dx - width/2);
                case REPLACE:
                    int rx = (patternProgress % 5) - 2;
                    int rz = (patternProgress / 5) - 2;
                    return startPos.offset(rx, 0, rz);
                case TOWER:
                    // Build directly under the golem
                    return golem.blockPosition().below();
                case SCHEMATIC:
                    if (schematic == null || schematicOrigin == null) return null;
                    // Iterate indices until find next non-air and not already correct block
                    int total = schematic.getTotalBlocks();
                    int seen = 0;
                    for (int i = 0; i < total; i++) {
                        net.minecraft.world.level.block.Block block = schematic.getBlockAtIndex(i);
                        if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) continue;
                        // Map linear index to x,y,z
                        int sx = schematic.indexToX(i);
                        int sy = schematic.indexToY(i);
                        int sz = schematic.indexToZ(i);
                        BlockPos rel = translateByFacing(sx, sy, sz, facing);
                        BlockPos worldPos = schematicOrigin.offset(rel.getX(), rel.getY(), rel.getZ());
                        // Check if already correct
                        if (golem.level().getBlockState(worldPos).is(block)) continue;
                        if (seen == patternProgress) {
                            return worldPos;
                        }
                        seen++;
                    }
                    return null;
                default:
                    return null;
            }
        }

        private void replaceBlock(BlockPos pos) {
            ItemStack filterStack = golem.getHeldItem();
            if (filterStack.isEmpty() || !(filterStack.getItem() instanceof net.minecraft.world.item.BlockItem)) {
                patternProgress++; // Skip if no filter
                return;
            }
            
            Block filterBlock = ((net.minecraft.world.item.BlockItem) filterStack.getItem()).getBlock();
            BlockState currentState = golem.level().getBlockState(pos);
            
            if (currentState.is(filterBlock)) {
                int slot = findBlockInInventory();
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getItem(slot);
                    Block newBlock = ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock();
                    
                    if (newBlock != filterBlock) {
                        boolean success = golem.level().setBlock(pos, newBlock.defaultBlockState(), 3, 3);
                        if (success) {
                            stack.shrink(1);
                            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                            golem.level().playSound(null, pos, newBlock.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            cooldown = 8; // 2.5 blocks per second
                        }
                    }
                }
            }
            patternProgress++;
        }

        private void placeTowerStep(BlockPos pos) {
            // Place a block directly beneath the golem and jump up
            if (golem.level().getBlockState(pos).canBeReplaced()) {
                int blockSlot = findBlockInInventory();
                if (blockSlot != -1) {
                    ItemStack stack = golem.getInventory().getItem(blockSlot);
                    Block block = ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock();
                    if (golem.level().setBlock(pos, block.defaultBlockState(), 3, 3)) {
                        stack.shrink(1);
                        golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        golem.level().playSound(null, pos, block.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        // Apply an upward velocity to simulate jumping
                        golem.setDeltaMovement(golem.getDeltaMovement().x, 0.5, golem.getDeltaMovement().z);
                        patternProgress++;
                        cooldown = 8; // 2.5 blocks per second
                        return;
                    }
                }
            }
            // If we fail to place (space not replaceable or no blocks), DON'T advance if TOWER mode
            // unless we really want to skip (but for tower, skipping doesn't make sense if we are standing on it)
            // However, we must ensure we don't stall forever if something is blocking.
            // If it's NOT replaceable, it means there's a block there.
            // If we are at the same level or higher than that block, we should move up.
            if (!golem.level().getBlockState(pos).canBeReplaced()) {
                 // There's already a block here. If we are still below where we need to be to place the NEXT one,
                 // we might need to jump.
                 if (golem.getY() < pos.getY() + 1.5) {
                     golem.setDeltaMovement(golem.getDeltaMovement().x, 0.3, golem.getDeltaMovement().z);
                 }
            }
        }

        private void placeSchematicBlock(BlockPos pos) {
            if (schematic == null) { 
                golem.debugLog("Schematic is null in placeSchematicBlock");
                patternProgress++; 
                return; 
            }
            // Determine required block at this target by reverse-mapping pos to schematic index
            Direction facing = golem.getDirection();
            // Find index that maps to this pos; iterate through as in getTargetPos to keep order
            int total = schematic.getTotalBlocks();
            int seen = 0;
            net.minecraft.world.level.block.Block needed = null;
            for (int i = 0; i < total; i++) {
                net.minecraft.world.level.block.Block block = schematic.getBlockAtIndex(i);
                if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) continue;
                int sx = schematic.indexToX(i);
                int sy = schematic.indexToY(i);
                int sz = schematic.indexToZ(i);
                BlockPos rel = translateByFacing(sx, sy, sz, facing);
                BlockPos worldPos = schematicOrigin.offset(rel.getX(), rel.getY(), rel.getZ());
                if (golem.level().getBlockState(worldPos).is(block)) continue;
                if (seen == patternProgress) {
                    needed = block;
                    break;
                }
                seen++;
            }
            if (needed == null) { 
                golem.debugLog("Could not find needed block at progress " + patternProgress);
                patternProgress++; 
                return; 
            }

            if (golem.level().getBlockState(pos).canBeReplaced()) {
                int slot = findBlockInInventory(needed);
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getItem(slot);
                    Block block = ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock();
                    golem.debugLog("Placing " + block.getDescriptionId() + " at " + pos.toShortString());
                    boolean success = golem.level().setBlock(pos, block.defaultBlockState(), 3, 3);
                    if (success) {
                        stack.shrink(1);
                        golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        golem.level().playSound(null, pos, block.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        patternProgress++;
                        cooldown = 8; // 2.5 blocks per second
                        if (patternProgress >= getMaxProgress()) {
                            golem.debugLog("Schematic build completed!");
                            patternProgress = 0;
                            cooldown = 100;
                            golem.setBuildingStarted(false);
                            golem.getNavigation().stop();
                        }
                        return;
                    } else {
                        golem.debugLog("setBlockState failed at " + pos.toShortString());
                    }
                } else {
                    golem.debugLog("Missing block in golem.getInventory(): " + needed.getDescriptionId());
                }
            } else {
                golem.debugLog("Block at " + pos.toShortString() + " is not replaceable: " + golem.level().getBlockState(pos).getBlock().getDescriptionId());
            }
            patternProgress++;
            if (patternProgress >= getMaxProgress()) {
                golem.debugLog("Schematic build finished (some blocks might have been skipped)");
                patternProgress = 0;
                cooldown = 100;
                golem.setBuildingStarted(false);
                golem.getNavigation().stop();
            }
        }

        private int findBlockInInventory(net.minecraft.world.level.block.Block needed) {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                    if (blockItem.getBlock() == needed) return i;
                }
            }
            return -1;
        }

        private BlockPos translateByFacing(int sx, int sy, int sz, Direction facing) {
            // Origin at schematicOrigin; X goes to right (rotateYClockwise), Z goes forward (facing), Y is up
            Direction right = facing.getClockWise();
            return schematicOrigin == null ? new BlockPos(0,0,0) : new BlockPos(
                    right.getStepX() * sx + facing.getStepX() * sz,
                    sy,
                    right.getStepZ() * sx + facing.getStepZ() * sz
            );
        }

        private void placeBlock(BlockPos pos) {
            if (golem.level().getBlockState(pos).canBeReplaced()) {
                int slot = findBlockInInventory();
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getItem(slot);
                    Block block = ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock();
                    
                    // Use setBlockState with flags to ensure updates
                    boolean success = golem.level().setBlock(pos, block.defaultBlockState(), 3, 3);
                    if (success) {
                        stack.shrink(1);
                        golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        golem.level().playSound(null, pos, block.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        
                        patternProgress++;
                        cooldown = 8; // 2.5 blocks per second
                        if (patternProgress >= getMaxProgress()) {
                            patternProgress = 0; // Reset for next time this goal starts
                            cooldown = 100;
                            golem.setBuildingStarted(false);
                            golem.getNavigation().stop();
                        }
                        return; // Successfully placed a block
                    }
                }
            }
            
            // If we couldn't place a block at this position (it's not replaceable or setBlockState failed), 
            // we skip this position and increment progress to avoid getting stuck.
            patternProgress++;
            if (patternProgress >= getMaxProgress()) {
                patternProgress = 0; // Reset for next time this goal starts
                cooldown = 100;
                golem.setBuildingStarted(false);
                golem.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            startPos = null;
            startFacing = null;
            // Removed patternProgress = 0; to allow the golem to resume if it gets interrupted.
            // Progress is only reset in canStart() if the pattern is changed.
        }
    }
    // HOLD WRENCH GOAL
    public static class HoldWrenchGoal extends Goal {
        private final UtilityGolem golem;

        public HoldWrenchGoal(UtilityGolem golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            ItemStack mainInteractionHand = golem.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!mainInteractionHand.isEmpty() && mainInteractionHand.is(UGItems.WRENCH_ITEM)) {
                return false;
            }
            return hasWrench();
        }

        private boolean hasWrench() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(UGItems.WRENCH_ITEM)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(UGItems.WRENCH_ITEM)) {
                    golem.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    break;
                }
            }
        }
    }

    // HEAL GOLEMS GOAL
    public static class HealGolemsGoal extends Goal {
        private final UtilityGolem golem;
        private UtilityGolem targetGolem;
        private int healCooldown = 0;
        private int scanCooldown = 0;

        public HealGolemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (healCooldown > 0) {
                healCooldown--;
                return false;
            }
            if (!hasWrench()) return false;
            
            targetGolem = findDamagedGolem();
            return targetGolem != null;
        }

        private boolean hasWrench() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(UGItems.WRENCH_ITEM)) {
                    return true;
                }
            }
            return false;
        }

        private UtilityGolem findDamagedGolem() {
            List<UtilityGolem> golems = golem.level().getEntitiesOfClass(UtilityGolem.class, golem.getBoundingBox().inflate(10.0D), 
                e -> e.getHealth() < e.getMaxHealth());
            if (golems.isEmpty()) return null;
            golems.sort(Comparator.comparingDouble(golem::distanceToSqr));
            return golems.get(0);
        }

        @Override
        public boolean canContinueToUse() {
            return targetGolem != null && targetGolem.isAlive() && targetGolem.getHealth() < targetGolem.getMaxHealth() && hasWrench() && golem.distanceToSqr(targetGolem) < 144.0D;
        }

        @Override
        public void start() {
            if (targetGolem != null) {
                if (targetGolem != golem) {
                    golem.getNavigation().moveTo(targetGolem, 1.2D);
                }
                
                // Equip wrench to main hand when starting to heal
                for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                    ItemStack stack = golem.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.is(UGItems.WRENCH_ITEM)) {
                        golem.setItemSlot(EquipmentSlot.MAINHAND, stack);
                        break;
                    }
                }
            }
        }

        @Override
        public void stop() {
            golem.getNavigation().stop();
            targetGolem = null;
            // Medic golems should always hold the wrench now, managed by HoldWrenchGoal and start()
        }

        @Override
        public void tick() {
            if (targetGolem == null) return;

            // Periodically check for new golems or if target is still best
            if (scanCooldown-- <= 0) {
                scanCooldown = 20;
                UtilityGolem bestTarget = findDamagedGolem();
                if (bestTarget != null && bestTarget != targetGolem) {
                    targetGolem = bestTarget;
                    if (targetGolem != golem) {
                        golem.getNavigation().moveTo(targetGolem, 1.2D);
                    } else {
                        golem.getNavigation().stop();
                    }
                }
            }

            if (targetGolem != golem) {
                golem.getLookControl().setLookAt(targetGolem, 30.0F, 30.0F);
            }
            double distSq = golem.distanceToSqr(targetGolem);

            if (distSq < 4.0D || targetGolem == golem) {
                golem.getNavigation().stop();
                if (healCooldown <= 0) {
                    healTarget();
                    healCooldown = 20; // Heal once per second
                }
            } else {
                if (golem.getNavigation().isDone()) {
                    golem.getNavigation().moveTo(targetGolem, 1.2D);
                }
            }

            if (healCooldown > 0) {
                healCooldown--;
            }
        }

        private void healTarget() {
            for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
                ItemStack stack = golem.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(UGItems.WRENCH_ITEM)) {
                    float healAmount = 2.0F; // Base 2.0 heal

                    // Efficiency enchantment increases amount of health that's healed
                    if (golem.level() instanceof ServerLevel serverLevel) {
                        int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), stack);
                        if (efficiencyLevel > 0) {
                            healAmount += (efficiencyLevel * 1.0F); // Add 1.0 (half heart) per efficiency level
                        }
                    }

                    targetGolem.heal(healAmount);
                    golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    targetGolem.level().playSound(null, targetGolem.blockPosition(), net.minecraft.sounds.SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 0.5F, 1.5F);
                    
                    if (golem.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, targetGolem.getX(), targetGolem.getY() + 1.0, targetGolem.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                        
                        // Using the version of damage that handles Unbreaking/Mending
                        stack.hurtAndBreak(1, serverLevel, null, item -> {});
                    } else {
                        stack.hurtAndBreak(1, (ServerLevel) golem.level(), null, item -> {});
                    }

                    if (stack.isEmpty()) {
                        golem.getInventory().setItem(i, ItemStack.EMPTY);
                        golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        // Update hand in case it was a new stack (unlikely but good for consistency)
                        golem.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    }
                    return;
                }
            }
        }
    }
    public static class ReturnToChestGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos chestPos;

        public ReturnToChestGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Low priority: only start if no other goals are active.
            // But we need to find the chest first.
            chestPos = golem.findNearbyChest();
            if (chestPos == null) return false;
            
            // Only return if we are not already near the chest
            return golem.blockPosition().distSqr(chestPos) > 25.0D;
        }

        @Override
        public boolean canContinueToUse() {
            if (chestPos == null) return false;
            // Interrupt if some other task becomes available (this is low priority anyway)
            return golem.blockPosition().distSqr(chestPos) > 9.0D &&
                   !golem.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (chestPos != null) {
                golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, 1.0D);
            }
        }

        @Override
        public void stop() {
            golem.getNavigation().stop();
            chestPos = null;
        }

        @Override
        public void tick() {
            if (chestPos != null) {
                if (golem.getNavigation().isDone()) {
                    golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, 1.0D);
                }
                golem.getLookControl().setLookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);
            }
        }
    }
}
