package rehdpanda.utilitygolems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class GolemAI {

    /// BOOLEAN FUNCTIONS
    public static boolean isIngredient(ItemStack stack) {
        return stack.isOf(Items.NETHER_WART) || stack.isOf(Items.GLOWSTONE_DUST) || stack.isOf(Items.REDSTONE)
                || stack.isOf(Items.FERMENTED_SPIDER_EYE) || stack.isOf(Items.MAGMA_CREAM) || stack.isOf(Items.SUGAR)
                || stack.isOf(Items.RABBIT_FOOT) || stack.isOf(Items.GLISTERING_MELON_SLICE) || stack.isOf(Items.SPIDER_EYE)
                || stack.isOf(Items.PUFFERFISH) || stack.isOf(Items.GOLDEN_CARROT) || stack.isOf(Items.TURTLE_HELMET)
                || stack.isOf(Items.PHANTOM_MEMBRANE) || stack.isOf(Items.DRAGON_BREATH) || stack.isOf(Items.GUNPOWDER);
    }
    public static boolean isSecondaryIngredient(ItemStack stack) {
        return stack.isOf(Items.GUNPOWDER) || stack.isOf(Items.GLOWSTONE_DUST) || stack.isOf(Items.REDSTONE) || stack.isOf(Items.DRAGON_BREATH);
    }
    public static boolean isPrimaryIngredient(ItemStack stack) {
        return isIngredient(stack) && !isSecondaryIngredient(stack);
    }

    /// INITIALIZE GOALS
    public static void initLapisGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.COPPER_PICKAXE,
                Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL, Items.GOLDEN_SHOVEL, Items.STONE_SHOVEL, Items.WOODEN_SHOVEL, Items.COPPER_SHOVEL
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new DigBlockGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.REDSTONE, Items.REPEATER), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new ConnectRedstoneGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new TriggerRedstoneGoal(golem)));
    }
    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.EMERALD), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new TradeWithVillagerGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new CraftEmeraldsGoal(golem)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    public static void initGoldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.GOLD_INGOT, Items.GOLD_NUGGET), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new TradeWithPiglinGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    public static void initAmethystGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.WHEAT_SEEDS), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new BreedAnimalsGoal(golem)));
    }
    public static void initNetheriteGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD, Items.WOODEN_SWORD, Items.COPPER_SWORD,
                Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.WOODEN_AXE, Items.COPPER_AXE,
                Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.COPPER_PICKAXE,
                Items.NETHERITE_SHOVEL, Items.DIAMOND_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.STONE_SHOVEL, Items.WOODEN_SHOVEL, Items.COPPER_SHOVEL,
                Items.NETHERITE_HOE, Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.STONE_HOE, Items.WOODEN_HOE, Items.COPPER_HOE,
                Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new MeleeAttackGoal(golem, 1.2D, false)));
        golem.getTargetSelector().add(1, new DebugGoalWrapper(golem, new RevengeGoal(golem).setGroupRevenge()));
        golem.getTargetSelector().add(2, new DebugGoalWrapper(golem, new ActiveTargetGoal<>(golem, HostileEntity.class, true)));
    }
    public static void initAncientGoals(UtilityGolem golem) {
        initNetheriteGoals(golem);
    }
    public static void initFurnaceGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
    }
    public static void initBambooGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_HOE, Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.STONE_HOE, Items.WOODEN_HOE, Items.COPPER_HOE,
                Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS, Items.WATER_BUCKET, Items.BUCKET
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new FarmGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new RefillBucketGoal(golem)));
    }
    public static void initDiamondGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.DIAMOND), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new PlaceBlockGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
    }
    public static void initSpongeGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.FISHING_ROD), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new FishGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    public static void initDeepslateGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.WOODEN_AXE, Items.COPPER_AXE, Items.SHEARS
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new ChopTreeGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new ReplantSaplingGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    public static void initJukeboxGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
                Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_PRECIPICE
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new PlayRecordGoal(golem)));
    }
    public static void initLampGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.TORCH, Items.SOUL_TORCH, Items.REDSTONE_TORCH, Items.LANTERN, Items.SOUL_LANTERN
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new PlaceTorchGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
    }
    public static void initNetherWartGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.GLASS_BOTTLE, Items.NETHER_WART
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new PlaceBrewingStandGoal(golem)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new FillBottleGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new BrewingGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }
    
    /// DEBUG WRAPPER
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

            this.setControls(innerGoal.getControls());
        }

        @Override
        public boolean canStart() {
            return innerGoal.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return innerGoal.shouldContinue();
        }

        @Override
        public boolean canStop() {
            return innerGoal.canStop();
        }

        @Override
        public void start() {
            golem.debugLog(goalName + " starting");
            if (innerGoal instanceof net.minecraft.entity.ai.goal.MeleeAttackGoal) {
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
            if (innerGoal instanceof net.minecraft.entity.ai.goal.MeleeAttackGoal) {
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
                    if (value instanceof net.minecraft.entity.Entity entity) {
                        golem.setDebugTarget(entity.getBlockPos());
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

    /// ADVANCED GOAL LOGIC
    public static class PlaceTorchGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int placeActionTime;
        private static final int MAX_PLACE_ACTION_TIME = 20;
        private int cooldown = 0;

        public PlaceTorchGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
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
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (isTorch(golem.getInventory().getStack(i))) return true;
            }
            return false;
        }

        private boolean isTorch(ItemStack stack) {
            return stack.isOf(Items.TORCH) || stack.isOf(Items.SOUL_TORCH) || stack.isOf(Items.REDSTONE_TORCH);
        }

        private BlockPos findDarkSpot() {
            World world = golem.getEntityWorld();
            BlockPos pos = golem.getBlockPos();
            int range = 8;
            BlockPos bestPos = null;
            double bestDistSq = Double.MAX_VALUE;

            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (isDarkEnough(world, p) && canPlaceTorchAt(p)) {
                            double distSq = pos.getSquaredDistance(p);
                            if (distSq < bestDistSq) {
                                bestDistSq = distSq;
                                bestPos = p.toImmutable();
                            }
                        }
                    }
                }
            }
            return bestPos;
        }

        private boolean isDarkEnough(World world, BlockPos pos) {
            // Check light level while ignoring the golem's own light block if it's nearby
            int blockLight = world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos);
            if (blockLight == 0) return true;
            
            // If light level is 12 or less, it might be from the golem itself
            if (blockLight <= 12) {
                // Check if this golem has an active light block
                BlockPos golemLightPos = golem.getLastLightPos();
                if (golemLightPos != null && world.getBlockState(golemLightPos).isOf(UGBlocks.LIGHT_BLOCK)) {
                    // This golem has a light block. We need to know if this block is the ONLY thing lighting 'pos'.
                    // Since we can't easily re-calculate light without it, we check if 'pos' is close to it.
                    // Light level 12 reaches 12 blocks, but intensity drops by 1 per block.
                    // If blockLight is exactly what we'd expect from the golem's light, and no other sources are obvious.
                    
                    double dist = Math.sqrt(pos.getSquaredDistance(golemLightPos));
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
            World world = golem.getEntityWorld();
            if (!world.getBlockState(pos).isReplaceable()) return false;
            BlockState below = world.getBlockState(pos.down());
            return below.isSideSolidFullSquare(world, pos.down(), Direction.UP) || below.isIn(BlockTags.FENCES);
        }

        @Override
        public boolean shouldContinue() {
            return targetPos != null && hasTorch() && golem.isLampOn() && isDarkEnough(golem.getEntityWorld(), targetPos);
        }

        @Override
        public void start() {
            placeActionTime = 0;
            golem.setDebugTarget(targetPos);
            golem.getNavigation().startMovingTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.2D);
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

            golem.getLookControl().lookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);
            double distSq = golem.squaredDistanceTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);

            if (distSq > 2.0D * 2.0D) {
                if (golem.getNavigation().isIdle()) {
                    golem.getNavigation().startMovingTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.2D);
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
                for (int i = 0; i < golem.getInventory().size(); i++) {
                    if (isTorch(golem.getInventory().getStack(i))) {
                        torchStack = golem.getInventory().getStack(i);
                        slot = i;
                        break;
                    }
                }
            }

            if (!torchStack.isEmpty()) {
                golem.debugLog("PlaceTorchGoal: Placing torch (" + torchStack.getItem().getName().getString() + ") at " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ());
                World world = golem.getEntityWorld();
                Block torchBlock = Blocks.TORCH;
                if (torchStack.isOf(Items.SOUL_TORCH)) torchBlock = Blocks.SOUL_TORCH;
                else if (torchStack.isOf(Items.REDSTONE_TORCH)) torchBlock = Blocks.REDSTONE_TORCH;

                if (world.getBlockState(targetPos).isReplaceable()) {
                    world.setBlockState(targetPos, torchBlock.getDefaultState());
                    world.playSound(null, targetPos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    torchStack.decrement(1);
                    if (torchStack.isEmpty() && slot != -1) {
                        golem.getInventory().setStack(slot, ItemStack.EMPTY);
                    }
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }
            }
        }
    }
    public static class ClimbLadderGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos ladderPos;

        public ClimbLadderGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            ladderPos = findNearbyLadder();
            if (ladderPos == null) return false;

            // If we are already on a ladder, we can start/continue
            if (golem.getEntityWorld().getBlockState(golem.getBlockPos()).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }

            // Otherwise, only start if we have a target above us
            BlockPos target = getTargetPos();
            if (target != null && target.getY() > golem.getY()) return true;

            // Or if we are already moving and stuck vertically near a ladder
            if (!golem.getNavigation().isIdle() && target != null) {
                double dy = target.getY() - golem.getY();
                if (dy > 1.0) return true;
            }

            return false;
        }

        private BlockPos getTargetPos() {
            if (golem.getNavigation().getCurrentPath() != null) {
                return golem.getNavigation().getCurrentPath().getTarget();
            }
            return null;
        }

        private BlockPos findNearbyLadder() {
            BlockPos pos = golem.getBlockPos();
            // Check current block and immediate neighbors
            for (BlockPos p : BlockPos.iterate(pos.add(-1, 0, -1), pos.add(1, 1, 1))) {
                if (golem.getEntityWorld().getBlockState(p).isIn(BlockTags.CLIMBABLE)) {
                    return p.toImmutable();
                }
            }
            return null;
        }

        @Override
        public boolean shouldContinue() {
            if (ladderPos == null) return false;

            // Check if we are still on or near a climbable block
            boolean onClimbable = false;
            BlockPos pos = golem.getBlockPos();
            for (BlockPos p : BlockPos.iterate(pos.add(-1, 0, -1), pos.add(1, 1, 1))) {
                if (golem.getEntityWorld().getBlockState(p).isIn(BlockTags.CLIMBABLE)) {
                    onClimbable = true;
                    ladderPos = p.toImmutable();
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
            golem.setVelocity(golem.getVelocity().x, 0.0, golem.getVelocity().z);
        }

        @Override
        public void tick() {
            if (ladderPos == null) return;

            BlockPos target = getTargetPos();

            // Move towards ladder horizontal center
            double centerX = ladderPos.getX() + 0.5;
            double centerZ = ladderPos.getZ() + 0.5;

            if (golem.squaredDistanceTo(centerX, golem.getY(), centerZ) > 0.05) {
                golem.getNavigation().startMovingTo(centerX, golem.getY(), centerZ, 1.0);
            } else {
                golem.getNavigation().stop();
                // Snap to center to avoid sliding off
                golem.refreshPositionAndAngles(centerX, golem.getY(), centerZ, golem.getYaw(), golem.getPitch());
            }

            // If we are close enough to the ladder horizontally, climb
            if (Math.abs(golem.getX() - centerX) < 0.5 && Math.abs(golem.getZ() - centerZ) < 0.5) {
                if (target == null || target.getY() > golem.getY()) {
                    // Only apply upward velocity if there is a climbable block at our feet or slightly above
                    BlockPos currentPos = golem.getBlockPos();
                    if (golem.getEntityWorld().getBlockState(currentPos).isIn(BlockTags.CLIMBABLE) ||
                            golem.getEntityWorld().getBlockState(currentPos.up()).isIn(BlockTags.CLIMBABLE)) {
                        golem.setVelocity(golem.getVelocity().x, 0.2, golem.getVelocity().z);
                    } else {
                        // We are at the top or no longer on a ladder
                        golem.setVelocity(golem.getVelocity().x, 0.0, golem.getVelocity().z);
                    }
                }
            }
        }
    }
    public static class FollowPlayerGoal extends Goal {
        private final UtilityGolem golem;
        private PlayerEntity targetPlayer;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;

        public FollowPlayerGoal(UtilityGolem golem, double speed, float minDistance, float maxDistance) {
            this.golem = golem;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            List<PlayerEntity> players = golem.getEntityWorld().getEntitiesByClass(PlayerEntity.class, golem.getBoundingBox().expand(maxDistance), player -> true);
            if (players.isEmpty()) return false;
            
            // Find closest player
            targetPlayer = players.stream()
                .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(golem)))
                .orElse(null);
                
            return targetPlayer != null && golem.squaredDistanceTo(targetPlayer) > (double)(minDistance * minDistance);
        }

        @Override
        public boolean shouldContinue() {
            return targetPlayer != null && targetPlayer.isAlive() && golem.squaredDistanceTo(targetPlayer) < (double)(maxDistance * maxDistance * 2);
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
            golem.getLookControl().lookAt(targetPlayer, 30.0F, 30.0F);
            if (golem.squaredDistanceTo(targetPlayer) > (double)(minDistance * minDistance)) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPlayer, speed);
                }
            }
        }
    }
    public static class TradeWithVillagerGoal extends Goal {
        private final UtilityGolem golem;
        private VillagerEntity targetVillager;
        private int tradeDelay;

        public TradeWithVillagerGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            targetVillager = findVillagerWithTrade();
            // Record trades even if we can't trade right now
            if (targetVillager == null) {
                recordNearbyTrades();
            }
            return targetVillager != null;
        }

        private void recordNearbyTrades() {
            List<VillagerEntity> villagers = golem.getEntityWorld().getEntitiesByClass(VillagerEntity.class, golem.getBoundingBox().expand(8.0), villager -> true);
            for (VillagerEntity villager : villagers) {
                TradeOfferList offers = villager.getOffers();
                for (TradeOffer offer : offers) {
                    if (offer.isDisabled()) continue;
                    if (!offer.getSellItem().isOf(Items.EMERALD)) {
                        golem.addDiscoveredTrade(offer.getSellItem());
                    }
                }
            }
        }

        private VillagerEntity findVillagerWithTrade() {
            List<VillagerEntity> villagers = golem.getEntityWorld().getEntitiesByClass(VillagerEntity.class, golem.getBoundingBox().expand(16.0), villager -> true);
            for (VillagerEntity villager : villagers) {
                if (canTradeWith(villager)) {
                    return villager;
                }
            }
            return null;
        }

        private boolean canTradeWith(VillagerEntity villager) {
            TradeOfferList offers = villager.getOffers();
            SimpleInventory inventory = golem.getInventory();
            ItemStack selectedBuy = golem.getSelectedBuyItem();

            for (TradeOffer offer : offers) {
                if (offer.isDisabled()) continue;
                
                // Existing selling logic
                if (offer.getSellItem().isOf(Items.EMERALD)) {
                    TradedItem buyItem1 = offer.getFirstBuyItem();
                    Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();

                    if (hasStack(inventory, buyItem1) && (buyItem2.isEmpty() || hasStack(inventory, buyItem2.get()))) {
                        return true;
                    }
                }
                
                // New buying logic
                if (!selectedBuy.isEmpty() && ItemStack.areItemsEqual(offer.getSellItem(), selectedBuy)) {
                    TradedItem buyItem1 = offer.getFirstBuyItem();
                    Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();

                    if (hasStack(inventory, buyItem1) && (buyItem2.isEmpty() || hasStack(inventory, buyItem2.get()))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasStack(Inventory inventory, TradedItem target) {
            int count = 0;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack invStack = inventory.getStack(i);
                if (target.matches(invStack)) {
                    count += invStack.getCount();
                }
            }
            return count >= target.count();
        }

        @Override
        public boolean shouldContinue() {
            return targetVillager != null && targetVillager.isAlive() && canTradeWith(targetVillager);
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

            // Ensure animation is active while trading
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.TRADING, 40);
            }

            double dist = golem.squaredDistanceTo(targetVillager);
            if (dist > 4.0D) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetVillager, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetVillager, 30.0F, 30.0F);

                if (++tradeDelay % 20 == 0) {
                    performTrade();
                }
            }
        }

        private void performTrade() {
            TradeOfferList offers = targetVillager.getOffers();
            SimpleInventory inventory = golem.getInventory();
            ItemStack selectedBuy = golem.getSelectedBuyItem();

            // First, record all available trades from this villager
            for (TradeOffer offer : offers) {
                if (!offer.isDisabled() && !offer.getSellItem().isOf(Items.EMERALD)) {
                    golem.addDiscoveredTrade(offer.getSellItem());
                }
            }

            for (TradeOffer offer : offers) {
                if (offer.isDisabled()) continue;

                boolean isSellingToVillager = offer.getSellItem().isOf(Items.EMERALD);
                boolean isBuyingFromVillager = !selectedBuy.isEmpty() && ItemStack.areItemsEqual(offer.getSellItem(), selectedBuy);

                if (isSellingToVillager || isBuyingFromVillager) {
                    TradedItem buyItem1 = offer.getFirstBuyItem();
                    Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();

                    if (hasStack(inventory, buyItem1) && (buyItem2.isEmpty() || hasStack(inventory, buyItem2.get()))) {
                        // Consume items
                        consumeItems(inventory, buyItem1);
                        buyItem2.ifPresent(tradedItem -> consumeItems(inventory, tradedItem));

                        // Add reward
                        ItemStack reward = offer.getSellItem().copy();
                        ItemStack remaining = inventory.addStack(reward);
                        if (!remaining.isEmpty()) {
                            golem.getEntityWorld().spawnEntity(new net.minecraft.entity.ItemEntity(golem.getEntityWorld(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                        }

                        // Notify villager of trade and decrement stock/uses
                        targetVillager.trade(offer);
                        // Ensure the offer usage/stock is decremented even when not using the trading UI
                        offer.use();
                        // Let the villager process post-trade effects (restock timers, XP, sounds)
                        try {
                            targetVillager.onSellingItem(reward.copy());
                        } catch (Throwable ignored) {
                            // Some mappings may not expose onSellingItem on VillagerEntity; ignore if unavailable
                        }
                        break;
                    }
                }
            }
        }

        private void consumeItems(Inventory inventory, TradedItem target) {
            int toConsume = target.count();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack invStack = inventory.getStack(i);
                if (target.matches(invStack)) {
                    int amount = Math.min(toConsume, invStack.getCount());
                    invStack.decrement(amount);
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

        public FishGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            ItemStack rod = golem.getHeldItem();
            if (rod.isEmpty() || !UtilityGolem.isFishingRod(rod)) {
                return false;
            }
            if (isInventoryFull()) {
                return false;
            }
            chestPos = findNearbyChest();
            if (chestPos == null) {
                return false;
            }
            waterPos = findNearbyWater(chestPos);
            if (waterPos != null) {
                golem.debugLog("FishGoal: Found water at " + waterPos.toShortString() + " near chest " + chestPos.toShortString());
                return true;
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            if (golem.getChestPos() != null) {
                if (golem.isBlacklisted(golem.getChestPos())) {
                    golem.setChestPos(null);
                } else {
                    BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                    if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                        return golem.getChestPos();
                    }
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.isBlacklisted(p)) continue;
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        Block targetChest = golem.getGolemType().getChestBlock();
                        if (be instanceof Inventory && block == targetChest) {
                            golem.debugLog("FishGoal: Found chest at " + p.toShortString());
                            golem.setChestPos(p);
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private BlockPos findNearbyWater(BlockPos center) {
            World world = golem.getEntityWorld();
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = center.add(x, y, z);
                        BlockState state = world.getBlockState(p);
                        if (!state.isOf(Blocks.WATER)) continue;
                        if (!world.getBlockState(p.up()).isAir()) continue;
                        // compute score
                        double dx = p.getX() - center.getX();
                        double dz = p.getZ() - center.getZ();
                        double horizDistSq = dx * dx + dz * dz;
                        int minLandDist = 99;
                        boolean shallow = !world.getBlockState(p.down()).isOf(Blocks.WATER);
                        for (int lx = -4; lx <= 4; lx++) {
                            for (int lz = -4; lz <= 4; lz++) {
                                if (lx == 0 && lz == 0) continue;
                                BlockPos landPos = p.add(lx, 0, lz);
                                BlockState ls = world.getBlockState(landPos);
                                if (ls.getFluidState().isEmpty() && !ls.isAir()) {
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
                boolean shallow = !world.getBlockState(best.down()).isOf(Blocks.WATER);
                int minLandDist = 99;
                for (int lx = -4; lx <= 4; lx++) {
                    for (int lz = -4; lz <= 4; lz++) {
                        if (lx == 0 && lz == 0) continue;
                        BlockPos landPos = best.add(lx, 0, lz);
                        BlockState ls = world.getBlockState(landPos);
                        if (ls.getFluidState().isEmpty() && !ls.isAir()) {
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
            maxFishingTime = 100 + golem.getRandom().nextInt(200); // 5-15 seconds
            golem.setAnimation(GolemAnimation.FISHING, maxFishingTime);
        }

        @Override
        public boolean shouldContinue() {
            ItemStack rod = golem.getHeldItem();
            return waterPos != null && golem.getEntityWorld().getBlockState(waterPos).isOf(Blocks.WATER) &&
                    chestPos != null && golem.getEntityWorld().getBlockState(chestPos).getBlock() == golem.getGolemType().getChestBlock() &&
                    !rod.isEmpty() && UtilityGolem.isFishingRod(rod) &&
                    fishingTime < maxFishingTime && !isInventoryFull() &&
                    golem.getBlockPos().getSquaredDistance(waterPos.getX(), waterPos.getY(), waterPos.getZ()) < 400 &&
                    golem.getBlockPos().getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024; // within 32 blocks of chest
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
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

            double dx = golem.getX() - (waterPos.getX() + 0.5);
            double dy = golem.getY() - (waterPos.getY() + 0.5);
            double dz = golem.getZ() - (waterPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 5.0D || verticalDist > 15.0D) {
                // If it's far vertically, target our current height
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().startMovingTo(waterPos.getX(), golem.getY(), waterPos.getZ(), 1.1D);
                    } else {
                        golem.getNavigation().startMovingTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.1D);
                    }
                }
                fishingTime = 0;
                golem.setFishingTarget(null);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);
                golem.setFishingTarget(waterPos);

                if (fishingTime % 20 == 0) {
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }

                fishingTime++;

                if (fishingTime >= maxFishingTime) {
                    catchFish();
                }
            }
        }

        private void catchFish() {
            if (!(golem.getEntityWorld() instanceof ServerWorld serverWorld)) return;

            // Simplified fishing loot - 85% fish, 10% junk, 5% treasure
            ItemStack loot;
            int chance = golem.getRandom().nextInt(100);
            if (chance < 85) {
                int fishType = golem.getRandom().nextInt(4);
                loot = switch (fishType) {
                    case 1 -> new ItemStack(Items.SALMON);
                    case 2 -> new ItemStack(Items.TROPICAL_FISH);
                    case 3 -> new ItemStack(Items.PUFFERFISH);
                    default -> new ItemStack(Items.COD);
                };
                golem.setAnimation(GolemAnimation.CATCHING_FISH, 20);
            } else if (chance < 95) {
                loot = new ItemStack(Items.SADDLE); // Simplified junk/treasure for now
                golem.setAnimation(GolemAnimation.CATCHING_FISH, 20);
            } else {
                loot = Items.ENCHANTED_BOOK.getDefaultStack();
                if (serverWorld.getRegistryManager() != null) {
                    var enchantmentRegistry = serverWorld.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
                    var optionalEnchantment = enchantmentRegistry.getRandom(golem.getRandom());
                    if (optionalEnchantment.isPresent()) {
                        loot.addEnchantment(optionalEnchantment.get(), net.minecraft.util.math.MathHelper.nextInt(golem.getRandom(), 1, 3));
                    }
                }
                golem.setAnimation(GolemAnimation.CATCHING_RARE_FISH, 20);
            }

            ItemStack remaining = golem.getInventory().addStack(loot);
            golem.debugLog("FishGoal: Caught " + loot.getItem().getName().getString() + "!");
            if (!remaining.isEmpty()) {
                Block.dropStack(serverWorld, golem.getBlockPos(), remaining);
            }

            // Damage the fishing rod
            ItemStack rod = golem.getHeldItem();
            if (!rod.isEmpty() && UtilityGolem.isFishingRod(rod)) {
                rod.damage(1, serverWorld, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
            }
            
            fishingTime = 0;
            maxFishingTime = 100 + golem.getRandom().nextInt(200);
        }
    }
    public static class PlaceBrewingStandGoal extends Goal {
        private final UtilityGolem golem;
        private int cooldown = 0;

        public PlaceBrewingStandGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
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
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isOf(Items.BREWING_STAND)) return i;
            }
            return -1;
        }

        private BlockPos findNearbyBrewingStand() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getBlockState(p).isOf(Blocks.BREWING_STAND)) {
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
                    if (golem.getBlockPos().getSquaredDistance(placePos) > 4.0D) {
                        golem.getNavigation().startMovingTo(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5, 1.0D);
                    } else {
                        placeStand(placePos);
                    }
                }
            }
        }

        private BlockPos findNearbyChest() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockState state = golem.getEntityWorld().getBlockState(p);
                        if (state.isOf(Blocks.CHEST) || state.isOf(UGBlocks.GOLEM_CHESTS.get(GolemType.NETHER_WART))) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private BlockPos findPlacePos(BlockPos chestPos) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos p = chestPos.offset(dir);
                if (golem.getEntityWorld().getBlockState(p).isAir() && golem.getEntityWorld().getBlockState(p.down()).isSolidBlock(golem.getEntityWorld(), p.down())) {
                    return p;
                }
            }
            return chestPos.up(); // Fallback to on top of chest
        }

        private void placeStand(BlockPos pos) {
            int slot = findBrewingStand();
            if (slot != -1) {
                ItemStack stack = golem.getInventory().getStack(slot);
                golem.getEntityWorld().setBlockState(pos, Blocks.BREWING_STAND.getDefaultState());
                stack.decrement(1);
                golem.getInventory().markDirty();
                golem.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
                cooldown = 100;
            }
        }

        @Override
        public boolean shouldContinue() {
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (golem.getInventory().isEmpty() && !golem.getHeldItem().isOf(Items.BLAZE_POWDER)) return false;
            
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
            BlockEntity be = golem.getEntityWorld().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.block.entity.BrewingStandBlockEntity stand) {
                // 1. Can collect finished potions?
                for (int i = 0; i < 3; i++) {
                    if (!stand.getStack(i).isEmpty() && isFullyFinished(stand.getStack(i))) return true;
                }

                // 2. Can refill fuel?
                ItemStack fuelStack = stand.getStack(4);
                if ((fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxCount()) && findItemInInventory(Items.BLAZE_POWDER) != -1) {
                    return true;
                }

                // Check if stand HAS any bottles/potions to brew with
                boolean standHasBottles = false;
                for (int i = 0; i < 3; i++) {
                    if (!stand.getStack(i).isEmpty()) {
                        standHasBottles = true;
                        break;
                    }
                }

                // 3. Can add water bottles?
                boolean hasWaterInInv = findWaterBottleInInventory() != -1;
                if (hasWaterInInv) {
                    for (int i = 0; i < 3; i++) {
                        if (stand.getStack(i).isEmpty()) return true;
                    }
                }

                // 4. Can add ingredient?
                if (standHasBottles && stand.getStack(3).isEmpty() && findBestIngredientForStand(stand) != -1) {
                    return true;
                }
            }
            return false;
        }

        private BlockPos findBrewingStand() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getBlockState(p).isOf(Blocks.BREWING_STAND)) {
                            golem.debugLog("BrewingGoal: Found brewing stand at " + p.toShortString());
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
            BlockEntity be = golem.getEntityWorld().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.block.entity.BrewingStandBlockEntity stand) {
                // If we are at the stand and have a pending action stack, hold it
                if (!pendingActionStack.isEmpty()) {
                    golem.setHeldItem(pendingActionStack);
                    return;
                }

                // Otherwise, hold what we are about to insert
                // 1. Fuel
                ItemStack fuelStack = stand.getStack(4);
                if (fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxCount()) {
                    int slot = findItemInInventory(Items.BLAZE_POWDER);
                    if (slot != -1) {
                        golem.setHeldItem(golem.getInventory().getStack(slot).copyWithCount(1));
                        return;
                    }
                }

                // 2. Ingredients
                if (stand.getStack(3).isEmpty()) {
                    int slot = findBestIngredientForStand(stand);
                    if (slot != -1) {
                        golem.setHeldItem(golem.getInventory().getStack(slot).copyWithCount(1));
                        return;
                    }
                }

                // 3. Water Bottles
                for (int i = 0; i < 3; i++) {
                    if (stand.getStack(i).isEmpty()) {
                        int waterSlot = findWaterBottleInInventory();
                        if (waterSlot != -1) {
                            golem.setHeldItem(golem.getInventory().getStack(waterSlot).copyWithCount(1));
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
            if (golem.getRandom().nextInt(40) == 0 && !golem.getEntityWorld().getBlockState(targetPos).isOf(Blocks.BREWING_STAND)) {
                targetPos = findBrewingStand();
                if (targetPos == null) return;
            }

            if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(20) == 0) {
                golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            }
            golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

            if (golem.squaredDistanceTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) < 4.0D) {
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
            BlockEntity be = golem.getEntityWorld().getBlockEntity(targetPos);
            if (be instanceof net.minecraft.block.entity.BrewingStandBlockEntity brewingStand) {
                // 1. Collect finished potions from slots 0, 1, 2
                for (int i = 0; i < 3; i++) {
                    ItemStack stack = brewingStand.getStack(i);
                    if (!stack.isEmpty() && isFullyFinished(stack)) {
                        pendingActionStack = stack.copy();
                        golem.setHeldItem(pendingActionStack);
                        
                        ItemStack remaining = golem.getInventory().addStack(stack);
                        if (!remaining.isEmpty()) {
                            // Inventory full, try to deposit in chest if nearby
                            BlockPos chestPos = golem.getChestPos();
                            if (chestPos == null) {
                                // Simple search for nearby chest if not set
                                chestPos = findNearbyChest();
                            }
                            if (chestPos != null && golem.squaredDistanceTo(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5) < 16.0D) {
                                BlockEntity chestBe = golem.getEntityWorld().getBlockEntity(chestPos);
                                if (chestBe instanceof Inventory chestInv) {
                                    remaining = transferStackToChest(remaining, chestInv);
                                }
                            }
                        }
                        brewingStand.setStack(i, remaining);
                        brewingStand.markDirty();
                        pendingActionStack = ItemStack.EMPTY; // Item is now in inventory or chest
                        return; // One action per interaction
                    }
                }

                // 2. Refill Blaze Powder fuel (slot 4)
                ItemStack fuelStack = brewingStand.getStack(4);
                if (fuelStack.isEmpty() || fuelStack.getCount() < fuelStack.getMaxCount()) {
                    int slot = findItemInInventory(Items.BLAZE_POWDER);
                    if (slot != -1) {
                        ItemStack powder = golem.getInventory().getStack(slot);
                        int amountToTransfer = Math.min(powder.getCount(), Items.BLAZE_POWDER.getMaxCount() - fuelStack.getCount());
                        ItemStack toTransfer = golem.getInventory().removeStack(slot, amountToTransfer);
                        if (fuelStack.isEmpty()) {
                            brewingStand.setStack(4, toTransfer);
                        } else {
                            fuelStack.increment(amountToTransfer);
                        }
                        brewingStand.markDirty();
                        return;
                    }
                }

                // 3. Add Water Bottles to empty potion slots (0, 1, 2)
                for (int i = 0; i < 3; i++) {
                    if (brewingStand.getStack(i).isEmpty()) {
                        int waterSlot = findWaterBottleInInventory();
                        if (waterSlot != -1) {
                            ItemStack waterBottle = golem.getInventory().removeStack(waterSlot, 1);
                            brewingStand.setStack(i, waterBottle);
                            brewingStand.markDirty();
                            return;
                        }
                    }
                }

                // 4. Add ingredient (slot 3) if empty
                if (brewingStand.getStack(3).isEmpty()) {
                    int ingredientSlot = findBestIngredientForStand(brewingStand);
                    if (ingredientSlot != -1) {
                        ItemStack ingredient = golem.getInventory().getStack(ingredientSlot);
                        brewingStand.setStack(3, golem.getInventory().removeStack(ingredientSlot, 1));
                        brewingStand.markDirty();
                        return;
                    }
                }
            }
        }

        private int findBestIngredientForStand(net.minecraft.block.entity.BrewingStandBlockEntity stand) {
            boolean hasWaterBottle = false;
            boolean hasAwkwardPotion = false;
            boolean hasRegularPotion = false;

            for (int i = 0; i < 3; i++) {
                ItemStack stack = stand.getStack(i);
                if (stack.isEmpty()) continue;
                if (isWaterBottle(stack)) hasWaterBottle = true;
                else if (isAwkwardPotion(stack)) hasAwkwardPotion = true;
                else if (isRegularPotion(stack)) hasRegularPotion = true;
            }

            // Priority 1: If there are water bottles, we NEED Nether Wart first.
            if (hasWaterBottle) {
                int nw = findItemInInventory(Items.NETHER_WART);
                if (nw != -1) return nw;
            }

            // Priority 2: If we have awkward potions, use a primary ingredient (not nether wart, not secondary)
            if (hasAwkwardPotion) {
                for (int i = 0; i < golem.getInventory().size(); i++) {
                    ItemStack stack = golem.getInventory().getStack(i);
                    if (isPrimaryIngredient(stack) && !stack.isOf(Items.NETHER_WART)) return i;
                }
                
                // If we don't have primary ingredients, but we HAVE awkward potions, maybe we can use secondary?
                // For example, making splash awkward potions (though rare) or if the stand has a mix of awkward and regular potions.
                for (int i = 0; i < golem.getInventory().size(); i++) {
                    ItemStack stack = golem.getInventory().getStack(i);
                    if (isSecondaryIngredient(stack)) {
                        ItemStack standIngredient = stand.getStack(3);
                        if (standIngredient.isEmpty()) return i;
                    }
                }
            }

            // Priority 3: If we have regular potions, use secondary ingredients (Gunpowder, etc.)
            if (hasRegularPotion) {
                for (int i = 0; i < golem.getInventory().size(); i++) {
                    ItemStack stack = golem.getInventory().getStack(i);
                    if (isSecondaryIngredient(stack)) {
                        // Check if the stand already has this ingredient or if it can be used
                        ItemStack standIngredient = stand.getStack(3);
                        if (standIngredient.isEmpty()) return i;
                    }
                }
            }

            return -1;
        }

        private boolean isWaterBottle(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.WATER);
            }
            return false;
        }

        private boolean isAwkwardPotion(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        private boolean isRegularPotion(ItemStack stack) {
            if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                RegistryEntry<net.minecraft.potion.Potion> p = potion.potion().get();
                return !p.matches(net.minecraft.potion.Potions.WATER) && !p.matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        if (bs.isOf(UGBlocks.GOLEM_CHESTS.get(GolemType.NETHER_WART))) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private ItemStack transferStackToChest(ItemStack stack, Inventory container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.size(); i++) {
                ItemStack containerStack = container.getStack(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxCount() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.increment(transferAmount);
                        remaining.decrement(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.size(); i++) {
                if (container.getStack(i).isEmpty()) {
                    container.setStack(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, other) && other.getCount() < other.getMaxCount();
        }

        private boolean isFullyFinished(ItemStack stack) {
            return BrewingGoal.isFullyFinished(golem, stack);
        }

        public static boolean isFullyFinished(UtilityGolem golem, ItemStack stack) {
            // A potion is fully finished if it's a splash/lingering potion OR it has been enhanced and we have no more secondary ingredients to add
            // For simplicity, let's say if it's not a water bottle and not awkward, it's "finished" enough to be collected if we don't have secondary ingredients.
            // But the user said: "If it has gunpowder, it will make splash potions, same with glowstone / redstone."
            // So it should only collect if it CANNOT improve it further with what it has in inventory.
            
            if (!isRegularPotionStatic(stack)) return false;

            // If we have secondary ingredients, we should probably keep it in the stand to process further
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (isSecondaryIngredient(golem.getInventory().getStack(i))) {
                    // But only if it's not already splash/lingering (unless it's dragon breath)
                    if (stack.isOf(Items.POTION)) return false; // Can still add gunpowder/redstone/glowstone
                    if (stack.isOf(Items.SPLASH_POTION) && findItemInInventoryStatic(golem, Items.DRAGON_BREATH) != -1) return false;
                }
            }
            
            return true;
        }

        public static boolean isRegularPotionStatic(ItemStack stack) {
            if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                RegistryEntry<net.minecraft.potion.Potion> p = potion.potion().get();
                return !p.matches(net.minecraft.potion.Potions.WATER) && !p.matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        public static boolean isWaterBottleStatic(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.WATER);
            }
            return false;
        }

        public static boolean isAwkwardPotionStatic(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        public static int findItemInInventoryStatic(UtilityGolem golem, Item item) {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isOf(item)) return i;
            }
            return -1;
        }

        private boolean isFinishedPotion(ItemStack stack) {
            // Keep for compatibility if used elsewhere, but we updated interactWithBrewingStand to use isFullyFinished
            if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && !potion.potion().get().matches(net.minecraft.potion.Potions.WATER);
            }
            return false;
        }

        private int findItemInInventory(Item item) {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isOf(item)) return i;
            }
            return -1;
        }

        private int findWaterBottleInInventory() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (stack.isOf(Items.POTION)) {
                    net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.WATER)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        private int findIngredientInInventory() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                if (isIngredient(stack)) return i;
            }
            return -1;
        }

        @Override
        public boolean shouldContinue() {
            return targetPos != null && golem.getEntityWorld().getBlockState(targetPos).isOf(Blocks.BREWING_STAND) && hasWorkAtStand();
        }

        private boolean isInventoryFull() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isEmpty()) return false;
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (isInventoryFull()) return false;
            if (findItemInInventory(Items.GLASS_BOTTLE) == -1) return false;
            
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
            
            BlockEntity be = golem.getEntityWorld().getBlockEntity(standPos);
            if (be instanceof net.minecraft.block.entity.BrewingStandBlockEntity stand) {
                // If we have ingredients and stand can take them, prioritize brewing
                if (findIngredientInInventory() != -1 && stand.getStack(3).isEmpty()) {
                    // But only if there are actually potions/bottles to brew with
                    boolean standHasBottles = false;
                    for (int i = 0; i < 3; i++) {
                        if (!stand.getStack(i).isEmpty()) {
                            standHasBottles = true;
                            break;
                        }
                    }
                    if (standHasBottles) return true;
                }
                
                // If we have water bottles and stand has empty slots, prioritize brewing
                if (findWaterBottleInInventory() != -1) {
                    for (int i = 0; i < 3; i++) {
                        if (stand.getStack(i).isEmpty()) return true;
                    }
                }

                // If stand has finished potions we can collect
                for (int i = 0; i < 3; i++) {
                    if (!stand.getStack(i).isEmpty() && BrewingGoal.isFullyFinished(golem, stand.getStack(i))) return true;
                }
            }
            return false;
        }

        private BlockPos findBrewingStand() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getBlockState(p).isOf(Blocks.BREWING_STAND)) {
                            golem.debugLog("FillBottleGoal: Found brewing stand at " + p.toShortString());
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private int findWaterBottleInInventory() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (isWaterBottle(stack)) return i;
            }
            return -1;
        }

        private int findIngredientInInventory() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                if (isIngredient(stack)) return i;
            }
            return -1;
        }

        private int countWaterBottles() {
            int count = 0;
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (isWaterBottle(golem.getInventory().getStack(i))) count++;
            }
            return count;
        }

        private int countPotions() {
            int count = 0;
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                    if (!isWaterBottle(stack)) count++;
                }
            }
            return count;
        }

        private boolean isInventoryEmpty() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (!golem.getInventory().getStack(i).isEmpty()) return false;
            }
            return true;
        }

        private BlockPos findNearbyChest() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        if (bs.isOf(UGBlocks.GOLEM_CHESTS.get(GolemType.NETHER_WART))) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private ItemStack transferStackToChest(ItemStack stack, Inventory container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.size(); i++) {
                ItemStack containerStack = container.getStack(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxCount() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.increment(transferAmount);
                        remaining.decrement(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.size(); i++) {
                if (container.getStack(i).isEmpty()) {
                    container.setStack(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, other) && other.getCount() < other.getMaxCount();
        }

        private boolean isWaterBottle(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.WATER);
            }
            return false;
        }

        private boolean isAwkwardPotion(ItemStack stack) {
            if (stack.isOf(Items.POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                return potion != null && potion.potion().isPresent() && potion.potion().get().matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        private boolean isRegularPotion(ItemStack stack) {
            if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potion == null || !potion.potion().isPresent()) return false;
                RegistryEntry<net.minecraft.potion.Potion> p = potion.potion().get();
                return !p.matches(net.minecraft.potion.Potions.WATER) && !p.matches(net.minecraft.potion.Potions.AWKWARD);
            }
            return false;
        }

        private BlockPos findNearbyWater() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getFluidState(p).isIn(net.minecraft.registry.tag.FluidTags.WATER)) {
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
            golem.setHeldItem(new ItemStack(Items.GLASS_BOTTLE));
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

            // Only search for water if the current target is no longer water
            if (golem.getRandom().nextInt(40) == 0 && !golem.getEntityWorld().getFluidState(waterPos).isIn(net.minecraft.registry.tag.FluidTags.WATER)) {
                waterPos = findNearbyWater();
                if (waterPos == null) return;
            }

            if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(20) == 0) {
                golem.getNavigation().startMovingTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.0D);
            }
            golem.getLookControl().lookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);

            if (golem.squaredDistanceTo(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5) < 4.0D) {
                golem.getNavigation().stop(); // Stop moving once close enough
                actionTimer++;
                if (actionTimer >= 20) {
                    fillBottle();
                    actionTimer = 0;
                    if (isInventoryFull()) {
                        waterPos = null;
                        golem.setHeldItem(ItemStack.EMPTY);
                    } else if (findItemInInventory(Items.GLASS_BOTTLE) != -1) {
                        golem.setHeldItem(new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            }
        }

        private void fillBottle() {
            int slot = findItemInInventory(Items.GLASS_BOTTLE);
            if (slot != -1) {
                golem.getInventory().removeStack(slot, 1);
                ItemStack waterBottle = new ItemStack(Items.POTION);
                waterBottle.set(DataComponentTypes.POTION_CONTENTS, new net.minecraft.component.type.PotionContentsComponent(net.minecraft.potion.Potions.WATER));
                
                golem.setHeldItem(waterBottle.copy());

                ItemStack remaining = golem.getInventory().addStack(waterBottle);
                if (!remaining.isEmpty()) {
                    Block.dropStack(golem.getEntityWorld(), golem.getBlockPos(), remaining);
                }
                golem.getEntityWorld().playSound(null, golem.getBlockPos(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }

        private int findItemInInventory(Item item) {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isOf(item)) return i;
            }
            return -1;
        }

        @Override
        public boolean shouldContinue() {
            return waterPos != null && findItemInInventory(Items.GLASS_BOTTLE) != -1 && !isInventoryFull();
        }

        private boolean isInventoryFull() {
            int emptySlots = 0;
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isEmpty()) {
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
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
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) return hasNetheriteItemsToDeposit();
            if (golem.getGolemType() == GolemType.LAPIS) return hasLapisItemsToDeposit();
            return hasFullStack() || isInventoryFull();
        }

        private boolean hasNetheriteItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue;
                if (stack.isOf(UGItems.GOLEM_SPAWN_EGGS.get(golem.getGolemType()))) continue;
                return true;
            }
            return false;
        }

        private boolean hasNetherWartItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (stack.isOf(Items.GLASS_BOTTLE)) continue;
                if (isIngredient(stack)) continue;
                if (stack.isOf(Items.BLAZE_POWDER)) continue;
                if (stack.isOf(Items.POTION)) {
                    net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent()) {
                         RegistryEntry<net.minecraft.potion.Potion> p = potion.potion().get();
                         if (p.matches(net.minecraft.potion.Potions.WATER) || p.matches(net.minecraft.potion.Potions.AWKWARD)) {
                             continue;
                         }
                    }
                }
                return true;
            }
            return false;
        }

        private boolean hasEmeraldsOrBuyListItems() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (stack.isOf(Items.EMERALD)) return true;
                for (ItemStack buyItem : golem.getDiscoveredTrades()) {
                    if (ItemStack.areItemsEqual(stack, buyItem)) return true;
                }
            }
            return false;
        }

        private boolean hasLapisItemsToDeposit() {
            // Deposit only when inventory is full or almost full.
            // This keeps the golem mining longer and reduces unnecessary trips to the surface.
            if (isInventoryFull()) return true;
            
            int empty = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) {
                    empty++;
                    continue;
                }
                // Check for tools that should never be deposited
                if (UtilityGolem.isTool(stack)) continue;
            }
            // Only return to deposit if we have 1 or fewer empty slots remaining
            if (empty <= 1) return true;
            
            return false;
        }

        private boolean hasSpongeItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (!UtilityGolem.isTool(stack)) return true;
            }
            return false;
        }

        private boolean hasDiamondItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (!(stack.getItem() instanceof net.minecraft.item.BlockItem)) return true;
            }
            return false;
        }

        private boolean hasJukeboxItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (stack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) continue;
                return true;
            }
            return false;
        }

        private boolean hasGoldGolemsItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (!stack.isOf(Items.GOLD_INGOT) && !stack.isOf(Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private boolean hasDeepslateItemsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (UtilityGolem.isTool(stack)) continue; // Common check in depositItems
                
                if (isSapling(stack)) {
                    if (getSaplingCount() > 8) return true;
                    continue;
                }
                return true; // Anything else should be deposited
            }
            return false;
        }

        @Override
        public boolean canStart() {
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
            SimpleInventory inv = golem.getInventory();
            
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (isCrop(stack)) {
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (isSeed(stack) || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS)) {
                            // Only deposit seeds if we have more than 16 (keep some for planting)
                            if (getSeedCount(stack.getItem()) > 16) return true;
                            continue;
                        }
                    }
                    return true;
                }
            }
            
            return false;
        }

        private int getSeedCount(Item item) {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(item)) {
                    count += stack.getCount();
                }
            }
            return count;
        }

        private boolean isCrop(ItemStack stack) {
            return stack.isOf(Items.WHEAT) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT)
                    || stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.BEETROOT_SEEDS)
                    || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS)
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS)
                    || stack.isOf(Items.PUMPKIN) || stack.isOf(Items.MELON);
        }

        //checks if stack is greater than 8 (PICKAXES COUNT AS FULL STACK BUG)
        private boolean hasFullStack() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                /// MAKE SURE THE STACK IS NOT A TOOL THAT SHOULD BE KEPT
                if (!stack.isEmpty() && stack.getCount() >= stack.getMaxCount()/8) {
                    if (UtilityGolem.isTool(stack)) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
            }
            return true;
        }

        private BlockPos findNearbyChest() {
            if (golem.getChestPos() != null) {
                if (golem.isBlacklisted(golem.getChestPos())) {
                    golem.setChestPos(null);
                } else {
                    BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                    if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                        return golem.getChestPos();
                    }
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.isBlacklisted(p)) continue;
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        Block targetChest = golem.getGolemType().getChestBlock();
                        if (be instanceof Inventory && block == targetChest) {
                            golem.setChestPos(p);
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
            if (golem.getGolemType() == GolemType.NETHER_WART) {
                updateHeldItem();
            }
        }

        private void updateHeldItem() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (stack.isOf(Items.GLASS_BOTTLE)) continue;
                if (isIngredient(stack)) continue;
                if (stack.isOf(Items.BLAZE_POWDER)) continue;
                if (stack.isOf(Items.POTION)) {
                    net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                    if (potion != null && potion.potion().isPresent()) {
                         RegistryEntry<net.minecraft.potion.Potion> p = potion.potion().get();
                         if (p.matches(net.minecraft.potion.Potions.WATER) || p.matches(net.minecraft.potion.Potions.AWKWARD)) {
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
        public boolean shouldContinue() {
            return chestPos != null && hasItemsToDeposit() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
        }

        private boolean isSeed(ItemStack stack) {
            if (stack.isEmpty()) return false;
            return stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT_SEEDS)
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS)
                    || stack.isOf(Items.PITCHER_POD) || stack.isOf(Items.TORCHFLOWER_SEEDS);
        }

        private boolean isSapling(ItemStack stack) {
            return stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS);
        }

        private boolean isApple(ItemStack stack) {
            return stack.isOf(Items.APPLE);
        }

        private int getSaplingCount() {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isSapling(inv.getStack(i))) {
                    count += inv.getStack(i).getCount();
                }
            }
            return count;
        }

        @Override
        public void stop() {
            if (chestPos != null) {
                golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 0);
            }
            golem.setSearching(false);
            chestPos = null;
            if (golem.getGolemType() == GolemType.NETHER_WART) {
                golem.setHeldItem(ItemStack.EMPTY);
            }
            searchCooldown = 20 + golem.getRandom().nextInt(20);
        }

        private int stuckTicks = 0;
        private Vec3d lastPos = Vec3d.ZERO;

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
                Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.squaredDistanceTo(lastPos) < 0.001) {
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
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    BlockPos targetPos = findStandablePosNear(chestPos);
                    boolean possible;
                    // Lapis golems often have to travel significant vertical distances to return to their chest
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    }

                    if (!possible) {
                        golem.blacklistPosition(chestPos);
                        stop();
                        return;
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

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
                    golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    golem.setAnimation(GolemAnimation.DEPOSITING, 60);
                    depositItems();
                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        updateHeldItem();
                    }
                    delay = 60; // Wait for animation
                }
            }
        }

        private BlockPos findStandablePosNear(BlockPos pos) {
            World world = golem.getEntityWorld();
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos p = pos.offset(dir);
                if (world.getBlockState(p).isAir() && world.getBlockState(p.up()).isAir() && !world.getBlockState(p.down()).isAir()) {
                    return p;
                }
            }
            return pos;
        }

        private void depositItems() {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                SimpleInventory golemInv = golem.getInventory();
                for (int i = 0; i < golemInv.size(); i++) {
                    ItemStack stack = golemInv.getStack(i);
                    if (!stack.isEmpty() && !UtilityGolem.isTool(stack)) {
                        // Never deposit the item the golem is currently holding in hand
                        if (ItemStack.areItemsAndComponentsEqual(stack, golem.getHeldItem())) {
                            continue;
                        }
                        if (golem.getGolemType() == GolemType.LAPIS) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.EMERALD) {
                            boolean isBuyListItem = false;
                            for (ItemStack buyItem : golem.getDiscoveredTrades()) {
                                if (ItemStack.areItemsEqual(stack, buyItem)) {
                                    isBuyListItem = true;
                                    break;
                                }
                            }
                            if (!stack.isOf(Items.EMERALD) && !isBuyListItem) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.BAMBOO) {
                            if (stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET) || UtilityGolem.isTool(stack)) {
                                continue;
                            }
                            if (isSeed(stack) || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS)) {
                                if (getSeedCount(stack.getItem()) <= 16) continue;
                                // Transfer only the excess
                                int toTransfer = getSeedCount(stack.getItem()) - 16;
                                if (toTransfer <= 0) continue;
                                ItemStack toDeposit = stack.copyWithCount(Math.min(stack.getCount(), toTransfer));
                                ItemStack remaining = transferStack(toDeposit, container);
                                stack.setCount(stack.getCount() - (toDeposit.getCount() - remaining.getCount()));
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            if (isSapling(stack) && getSaplingCount() <= 8) {
                                continue;
                            }
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.GOLD) {
                            if (stack.isOf(Items.GOLD_INGOT) || stack.isOf(Items.GOLD_NUGGET)) {
                                continue;
                            }
                            // Also don't deposit items the golem is currently "holding" for a purpose (though gold golems usually hold gold)
                            if (ItemStack.areItemsAndComponentsEqual(stack, golem.getHeldItem())) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.JUKEBOX) {
                            if (stack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) {
                                continue;
                            }
                            if (ItemStack.areItemsAndComponentsEqual(stack, golem.getHeldItem())) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.DIAMOND) {
                            if (stack.getItem() instanceof net.minecraft.item.BlockItem) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.SPONGE) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                            if (UtilityGolem.isTool(stack)) {
                                continue;
                            }
                            if (stack.isOf(UGItems.GOLEM_SPAWN_EGGS.get(golem.getGolemType()))) {
                                continue;
                            }
                        }
                        ItemStack remaining = transferStack(stack, container);
                        golemInv.setStack(i, remaining);
                    }
                }
                golemInv.markDirty();
                container.markDirty();
            }
        }

        private ItemStack transferStack(ItemStack stack, Inventory container) {
            ItemStack remaining = stack.copy();
            // Try to add to existing stacks first
            for (int i = 0; i < container.size(); i++) {
                ItemStack containerStack = container.getStack(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxCount() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.increment(transferAmount);
                        remaining.decrement(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            // Try to find an empty slot
            for (int i = 0; i < container.size(); i++) {
                if (container.getStack(i).isEmpty()) {
                    container.setStack(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, other) && other.getCount() < other.getMaxCount();
        }
    }
    public static class WithdrawItemsGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos chestPos;
        private int delay;
        private int searchCooldown = 0;

        public WithdrawItemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (golem.getGolemType() == GolemType.EMERALD) {
                if (hasEmeralds() || (golem.getSelectedBuyItem() != null && golem.getSelectedBuyItem().isEmpty())) {
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

            if (golem.getGolemType() == GolemType.LAPIS) {
                // Lapis golems only need to withdraw if they are completely missing a pickaxe or shovel.
                // This prevents them from returning to the chest just because they have one tool but not both,
                // or if they have a damaged tool but it hasn't broken yet.
                if (hasPickaxe() || hasShovel()) {
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
                if ((hasAxe() || hasShears()) && hasEnoughSaplings()) {
                    return false;
                }
                chestPos = golem.getChestPos();
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
                if (new FarmGoal(golem).canStart()) {
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
                if (hasIngredients() && hasSecondaryIngredients() && hasGlassBottles() && hasBlazePowder() && hasItem(Items.BREWING_STAND)) {
                    // Check if we have at least 1 stack of each. 
                    // Actually, if we have them, we might still want more if we have room in our 6 reserved slots.
                    int supplySlotsUsed = 0;
                    for (int i = 0; i < golem.getInventory().size(); i++) {
                        ItemStack s = golem.getInventory().getStack(i);
                        if (!s.isEmpty() && (isIngredient(s) || s.isOf(Items.GLASS_BOTTLE) || s.isOf(Items.BLAZE_POWDER) || s.isOf(Items.BREWING_STAND))) {
                            supplySlotsUsed++;
                        }
                    }
                    if (supplySlotsUsed >= 6) return false;
                }
                
                // Don't withdraw if inventory is full (regardless of supply slots)
                if (isInventoryFull()) return false;

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

            return false;
        }

        private boolean hasIngredients() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isPrimaryIngredient(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasSecondaryIngredients() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isSecondaryIngredient(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasGlassBottles() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.GLASS_BOTTLE)) return true;
            }
            return false;
        }

        private boolean hasBlazePowder() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.BLAZE_POWDER)) return true;
            }
            return false;
        }

        private boolean hasTradeItems() {
            List<TradeOfferList> allOffers = findNearbyVillagerOffers();
            if (allOffers.isEmpty()) return false;
            SimpleInventory inventory = golem.getInventory();
            for (TradeOfferList offers : allOffers) {
                for (TradeOffer offer : offers) {
                    if (offer.isDisabled()) continue;
                    if (offer.getSellItem().isOf(Items.EMERALD)) {
                        TradedItem buyItem1 = offer.getFirstBuyItem();
                        Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();
                        if (hasStack(inventory, buyItem1) && (buyItem2.isEmpty() || hasStack(inventory, buyItem2.get()))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private List<TradeOfferList> findNearbyVillagerOffers() {
            List<TradeOfferList> allOffers = new ArrayList<>();
            List<VillagerEntity> villagers = golem.getEntityWorld().getEntitiesByClass(VillagerEntity.class, golem.getBoundingBox().expand(16.0), villager -> true);
            for (VillagerEntity villager : villagers) {
                TradeOfferList offers = villager.getOffers();
                for (TradeOffer offer : offers) {
                    if (!offer.isDisabled() && offer.getSellItem().isOf(Items.EMERALD)) {
                        allOffers.add(offers);
                        break;
                    }
                }
            }
            return allOffers;
        }

        private boolean hasTradeItemsInChest(BlockPos pos) {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(pos);
            if (be instanceof Inventory container) {
                List<TradeOfferList> allOffers = findNearbyVillagerOffers();
                if (allOffers.isEmpty()) return false;
                
                for (TradeOfferList offers : allOffers) {
                    for (TradeOffer offer : offers) {
                        if (offer.isDisabled()) continue;
                        if (offer.getSellItem().isOf(Items.EMERALD)) {
                            TradedItem buyItem1 = offer.getFirstBuyItem();
                            Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();
                            
                            boolean has1 = hasStackInInventoryOrChest(buyItem1, container);
                            boolean has2 = buyItem2.isEmpty() || hasStackInInventoryOrChest(buyItem2.get(), container);
                            
                            if (has1 && has2) return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasStackInInventoryOrChest(TradedItem target, Inventory chest) {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (target.matches(stack)) count += stack.getCount();
            }
            for (int i = 0; i < chest.size(); i++) {
                ItemStack stack = chest.getStack(i);
                if (target.matches(stack)) count += stack.getCount();
            }
            return count >= target.count();
        }

        private boolean hasStack(Inventory inventory, TradedItem target) {
            int count = 0;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack invStack = inventory.getStack(i);
                if (target.matches(invStack)) {
                    count += invStack.getCount();
                }
            }
            return count >= target.count();
        }

        private boolean hasPickaxe() {
            if (UtilityGolem.isPickaxe(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isPickaxe(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isShovel(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasAxe() {
            if (UtilityGolem.isAxe(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isAxe(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasShears() {
            if (UtilityGolem.isShears(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isShears(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasEnoughSaplings() {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                    count += inv.getStack(i).getCount();
                }
            }
            return count >= 8;
        }

        private int getSaplingCount() {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                    count += inv.getStack(i).getCount();
                }
            }
            return count;
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().isOf(Items.WATER_BUCKET)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private boolean hasEmptyBucket() {
            if (golem.getHeldItem().isOf(Items.BUCKET)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.BUCKET)) return true;
            }
            return false;
        }

        private boolean hasSeeds() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isSeed(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean isSeed(ItemStack stack) {
            return stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT_SEEDS)
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS)
                    || stack.isOf(Items.PITCHER_POD) || stack.isOf(Items.TORCHFLOWER_SEEDS);
        }

        private boolean hasFuel() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isFuel(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean isFuel(ItemStack stack) {
            return stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL) || stack.isOf(Items.BLAZE_ROD) || stack.isOf(Items.LAVA_BUCKET);
        }

        private boolean hasBlocks() {
            SimpleInventory inventory = golem.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem() instanceof net.minecraft.item.BlockItem) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasMusicDisc() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) return true;
            }
            return false;
        }

        private boolean hasEmeralds() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.EMERALD)) return true;
            }
            return false;
        }

        private boolean hasItem(Item item) {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(item)) return true;
            }
            return false;
        }

        private boolean hasGold() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.GOLD_INGOT) || stack.isOf(Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private boolean hasRedstone() {
            SimpleInventory inv = golem.getInventory();
            boolean hasDust = false;
            boolean hasRepeater = false;
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.REDSTONE)) hasDust = true;
                if (inv.getStack(i).isOf(Items.REPEATER)) hasRepeater = true;
            }
            return hasDust && hasRepeater;
        }

        private boolean hasRedstoneDust() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.REDSTONE)) return true;
            }
            return false;
        }

        private boolean hasRepeater() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.REPEATER)) return true;
            }
            return false;
        }

        private boolean hasEnoughBreedingItems() {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isValidBreedingItem(inv.getStack(i))) {
                    count += inv.getStack(i).getCount();
                }
            }
            return count >= 8;
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
            }
            return true;
        }

        private boolean hasNeededItemsInChest(BlockPos pos) {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(pos);
            if (be instanceof Inventory container) {
                for (int i = 0; i < container.size(); i++) {
                    ItemStack stack = container.getStack(i);
                    if (stack.isEmpty()) continue;
                    if (golem.getGolemType() == GolemType.DIAMOND) {
                        if (stack.getItem() instanceof net.minecraft.item.BlockItem) return true;
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
                        if (!hasAxe() && UtilityGolem.isTool(stack)) return true;
                        if (!hasShears() && UtilityGolem.isTool(stack)) return true;
                        if (!hasEnoughSaplings() && stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) return true;
                    }
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (!hasHoe() && UtilityGolem.isTool(stack)) return true;
                        if (!hasWaterBucket() && !hasEmptyBucket() && (stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET))) return true;
                        if (!hasSeeds() && isSeed(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.AMETHYST) {
                        if (isValidBreedingItem(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.REDSTONE) {
                        if (stack.isOf(Items.REDSTONE) || stack.isOf(Items.REPEATER)) return true;
                    }
                    if (golem.getGolemType() == GolemType.JUKEBOX) {
                        if (stack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) return true;
                    }
                    if (golem.getGolemType() == GolemType.FURNACE) {
                        if (isFuel(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.SPONGE) {
                        if (UtilityGolem.isFishingRod(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.GOLD) {
                        if (stack.isOf(Items.GOLD_INGOT) || stack.isOf(Items.GOLD_NUGGET)) return true;
                    }
                    if (golem.getGolemType() == GolemType.EMERALD) {
                        if (stack.isOf(Items.EMERALD)) return true;
                    }
                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (stack.isOf(Items.GLASS_BOTTLE) && !hasGlassBottles()) return true;
                        if (isIngredient(stack) && !hasItem(stack.getItem())) return true;
                        if (stack.isOf(Items.BLAZE_POWDER) && !hasBlazePowder()) return true;
                        if (stack.isOf(Items.BREWING_STAND) && !hasItem(Items.BREWING_STAND)) return true;
                        if (BrewingGoal.isWaterBottleStatic(stack) || BrewingGoal.isAwkwardPotionStatic(stack)) {
                             // Only withdraw potions/water bottles if we have space in our 3 reserved water/potion slots
                             int potionSlotsUsed = 0;
                             for (int j = 0; j < golem.getInventory().size(); j++) {
                                 ItemStack s = golem.getInventory().getStack(j);
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
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isHoe(inv.getStack(i))) return true;
            }
            return false;
        }

        public boolean hasAnythingNeeded() {
            if (golem.getGolemType() == GolemType.BAMBOO) {
                // We always WANT these things, but we don't NEED them to start farming if there's harvesting to do.
                // However, this method is used by WithdrawItemsGoal.canStart().
                // If we return true here, it will try to withdraw.
                boolean needsHoe = !hasHoe();
                boolean needsWater = !hasWaterBucket() && !hasEmptyBucket();
                boolean needsSeeds = !hasSeeds();
                return needsHoe || needsWater || needsSeeds;
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                return !hasGoldIngot() || !hasGoldNugget();
            }
            return true;
        }

        private boolean hasGoldIngot() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.GOLD_INGOT)) return true;
            }
            return false;
        }

        private boolean hasGoldNugget() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.GOLD_NUGGET)) return true;
            }
            return false;
        }

        private BlockPos findNearbyChest() {
            if (golem.getChestPos() != null) {
                if (golem.isBlacklisted(golem.getChestPos())) {
                    golem.setChestPos(null);
                } else {
                    BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                    if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                        return golem.getChestPos();
                    }
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.isBlacklisted(p)) continue;
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        Block targetChest = golem.getGolemType().getChestBlock();
                        if (be instanceof Inventory && block == targetChest) {
                            golem.setChestPos(p);
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
            if (golem.getGolemType() == GolemType.NETHER_WART) {
                updateHeldItem();
            }
        }

        private void updateHeldItem() {
            if (chestPos == null) return;
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                // Determine what we are about to withdraw and hold it
                for (int i = 0; i < container.size(); i++) {
                    ItemStack stack = container.getStack(i);
                    if (stack.isEmpty()) continue;
                    
                    if (stack.isOf(Items.BLAZE_POWDER) && !hasBlazePowder()) {
                        golem.setHeldItem(stack.copyWithCount(1));
                        return;
                    }
                    if (stack.isOf(Items.GLASS_BOTTLE) && !hasGlassBottles()) {
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
                }
            }
            golem.setHeldItem(ItemStack.EMPTY);
        }

        @Override
        public boolean shouldContinue() {
            if (chestPos == null || !(golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory)) return false;

            if (golem.getGolemType() == GolemType.NETHER_WART) {
                // If inventory is "full" for supplies (6 slots used), stop.
                int supplySlotsUsed = 0;
                for (int i = 0; i < golem.getInventory().size(); i++) {
                    ItemStack s = golem.getInventory().getStack(i);
                    if (!s.isEmpty() && (isIngredient(s) || s.isOf(Items.GLASS_BOTTLE) || s.isOf(Items.BLAZE_POWDER))) {
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
                return chestPos != null && (!hasPickaxe() || !hasShovel()) && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                return chestPos != null && (!(hasAxe() || hasShears()) || !hasEnoughSaplings()) && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isSword(golem.getHeldItem())) && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.BAMBOO) {
                // Should continue if we still need something AND there's a chest to get it from
                if (chestPos == null) {
                    chestPos = findNearbyChest();
                }
                return chestPos != null && hasAnythingNeeded() && !isInventoryFull() && 
                       golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory &&
                       hasNeededItemsInChest(chestPos) &&
                       !new FarmGoal(golem).canStart(); // Interrupt if there's farming to do
            }
            if (golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) {
                return false;
            }
            if (golem.getGolemType() == GolemType.AMETHYST) {
                return chestPos != null && !hasEnoughBreedingItems() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.REDSTONE) {
                return chestPos != null && !hasRedstone() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                return chestPos != null && (!hasGoldIngot() || !hasGoldNugget()) && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.JUKEBOX) {
                return chestPos != null && !hasMusicDisc() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.FURNACE) {
                return chestPos != null && !hasFuel() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.DIAMOND) {
                return chestPos != null && !hasBlocks() && !isInventoryFull() && 
                       golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory &&
                       hasNeededItemsInChest(chestPos);
            }
            if (golem.getGolemType() == GolemType.SPONGE) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isFishingRod(golem.getHeldItem())) && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.EMERALD) {
                boolean needsEmeraldsForBuying = !golem.getSelectedBuyItem().isEmpty() && !hasEmeralds();
                boolean needsItemsForSelling = findNearbyVillagerOffers() != null && !hasTradeItems() && hasTradeItemsInChest(chestPos);
                
                return chestPos != null && (needsEmeraldsForBuying || needsItemsForSelling) && !isInventoryFull() &&
                       golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            return false;
        }

        @Override
        public void stop() {
            if (chestPos != null) {
                golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 0);
            }
            golem.setSearching(false);
            chestPos = null;
            if (golem.getGolemType() == GolemType.NETHER_WART) {
                golem.setHeldItem(ItemStack.EMPTY);
            }
        }

        private int stuckTicks = 0;
        private Vec3d lastPos = Vec3d.ZERO;

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
                Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.squaredDistanceTo(lastPos) < 0.001) {
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
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // Lapis golems often have to travel significant vertical distances to return to their chest
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().startMovingTo(chestPos.getX(), golem.getY(), chestPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
                    }

                    if (!possible) {
                        golem.blacklistPosition(chestPos);
                        stop();
                        return;
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (delay > 0) {
                    delay--;
                    if (delay == 0) {
                        if (golem.getGolemType() == GolemType.NETHER_WART) {
                            if (!shouldContinue()) {
                                stop();
                            }
                        }
                    }
                    return;
                }

                if (golem.getRandom().nextInt(10) == 0) {
                    golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    golem.setAnimation(GolemAnimation.WITHDRAWING, 60);
                    withdrawItems();
                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        updateHeldItem();
                    }
                    delay = 60; // Wait for animation
                }
            }
        }

        private boolean withdrawItems() {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                SimpleInventory golemInv = golem.getInventory();
                boolean withdrawnSomething = false;
                for (int i = 0; i < container.size(); i++) {
                    ItemStack containerStack = container.getStack(i);
                    if (containerStack.isEmpty()) continue;

                    if (golem.getGolemType() == GolemType.EMERALD) {
                        if (containerStack.isOf(Items.EMERALD)) {
                            ItemStack toWithdraw = containerStack.split(Math.min(containerStack.getCount(), containerStack.getMaxCount()));
                            golem.getInventory().addStack(toWithdraw);
                            withdrawnSomething = true;
                        }
                    }

                    if (golem.getGolemType() == GolemType.LAPIS) {
                        if (UtilityGolem.isPickaxe(containerStack) && !hasPickaxe()) {
                            ItemStack toWithdraw = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(toWithdraw);
                            } else {
                                golemInv.addStack(toWithdraw);
                            }
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        if (UtilityGolem.isShovel(containerStack) && !hasShovel()) {
                            ItemStack toWithdraw = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(toWithdraw);
                            } else {
                                golemInv.addStack(toWithdraw);
                            }
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                    }

                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (isIngredient(containerStack) || containerStack.isOf(Items.GLASS_BOTTLE) || containerStack.isOf(Items.BLAZE_POWDER) || containerStack.isOf(Items.BREWING_STAND) || BrewingGoal.isWaterBottleStatic(containerStack) || BrewingGoal.isAwkwardPotionStatic(containerStack)) {
                            // Nether Wart Golem has a slot reservation system.
                            // 6 slots for ingredients/supplies, 3 slots reserved for water/potions.
                            
                            boolean isPotionOrWater = BrewingGoal.isWaterBottleStatic(containerStack) || BrewingGoal.isAwkwardPotionStatic(containerStack);
                            
                            int supplySlotsUsed = 0;
                            int potionSlotsUsed = 0;
                            SimpleInventory golemInv_count = golem.getInventory();
                            for (int j = 0; j < golemInv_count.size(); j++) {
                                ItemStack s = golemInv_count.getStack(j);
                                if (!s.isEmpty()) {
                                    if (isIngredient(s) || s.isOf(Items.GLASS_BOTTLE) || s.isOf(Items.BLAZE_POWDER) || s.isOf(Items.BREWING_STAND)) {
                                        supplySlotsUsed++;
                                    } else if (BrewingGoal.isWaterBottleStatic(s) || BrewingGoal.isRegularPotionStatic(s) || BrewingGoal.isAwkwardPotionStatic(s)) {
                                        potionSlotsUsed++;
                                    }
                                }
                            }
                            
                            if (isPotionOrWater) {
                                if (potionSlotsUsed < 3) {
                                    ItemStack toWithdraw = containerStack.split(1);
                                    golemInv.addStack(toWithdraw);
                                    golemInv.markDirty();
                                    container.markDirty();
                                    return true;
                                }
                            } else if (supplySlotsUsed < 6) {
                                // Only withdraw if we don't already have a stack of this specific item
                                boolean alreadyHasItem = false;
                                for (int j = 0; j < golemInv.size(); j++) {
                                    if (golemInv.getStack(j).isOf(containerStack.getItem())) {
                                        alreadyHasItem = true;
                                        break;
                                    }
                                }
                                
                                if (!alreadyHasItem) {
                                    ItemStack toWithdraw = containerStack.split(Math.min(containerStack.getCount(), containerStack.getMaxCount()));
                                    golemInv.addStack(toWithdraw);
                                    withdrawnSomething = true;
                                    
                                    golemInv.markDirty();
                                    container.markDirty();
                                    // Stop after one successful withdrawal to allow re-evaluation in tick/shouldContinue
                                    return true;
                                }
                            }
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.AMETHYST && isValidBreedingItem(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        withdrawnSomething = true;
                        if (hasEnoughBreedingItems() || isInventoryFull()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.REDSTONE && containerStack.isOf(Items.REDSTONE)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        withdrawnSomething = true;
                        if (hasRedstone() || isInventoryFull()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.GOLD && containerStack.isOf(Items.GOLD_INGOT)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        withdrawnSomething = true;
                        if (hasGold() || isInventoryFull()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.JUKEBOX && containerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        withdrawnSomething = true;
                        if (hasMusicDisc() || isInventoryFull()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.FURNACE && isFuel(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        withdrawnSomething = true;
                        if (hasFuel() || isInventoryFull()) {
                            golemInv.markDirty();
                            container.markDirty();
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
                                golemInv.markDirty();
                                container.markDirty();
                                return true;
                            }
                        } else if (containerStack.isOf(Items.BUCKET) || containerStack.isOf(Items.WATER_BUCKET)) {
                            if (!hasWaterBucket() && !hasEmptyBucket()) {
                                ItemStack bucket = containerStack.split(1);
                                golem.getInventory().addStack(bucket);
                                withdrawnSomething = true;
                                if (UtilityGolem.isHoe(golem.getHeldItem()) && hasSeeds()) {
                                    golemInv.markDirty();
                                    container.markDirty();
                                    return true;
                                }
                            }
                        } else if (isSeed(containerStack)) {
                             if (!hasSeeds()) {
                                 ItemStack seeds = containerStack.split(Math.min(containerStack.getCount(), 64));
                                 golem.getInventory().addStack(seeds);
                                 withdrawnSomething = true;
                                 if (UtilityGolem.isHoe(golem.getHeldItem()) && (hasWaterBucket() || hasEmptyBucket())) {
                                     golemInv.markDirty();
                                     container.markDirty();
                                     return true;
                                 }
                             }
                        }
                        if (!hasAnythingNeeded()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.SPONGE && UtilityGolem.isFishingRod(containerStack)) {
                        ItemStack rod = containerStack.split(1);
                        golem.setHeldItem(rod);
                        golemInv.markDirty();
                        container.markDirty();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.DIAMOND && containerStack.getItem() instanceof net.minecraft.item.BlockItem) {
                        ItemStack blocks = containerStack.split(Math.min(containerStack.getCount(), 64));
                        golemInv.addStack(blocks);
                        withdrawnSomething = true;
                        golemInv.markDirty();
                        container.markDirty();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.DEEPSLATE) {
                        if (!hasAxe() && UtilityGolem.isAxe(containerStack)) {
                            ItemStack tool = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(tool);
                            } else {
                                golem.getInventory().addStack(tool);
                            }
                            withdrawnSomething = true;
                        } else if (!hasShears() && UtilityGolem.isShears(containerStack)) {
                            ItemStack tool = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(tool);
                            } else {
                                golem.getInventory().addStack(tool);
                            }
                            withdrawnSomething = true;
                        } else if (!hasEnoughSaplings() && containerStack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                            int needed = 8 - getSaplingCount();
                            if (needed > 0) {
                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                golem.getInventory().addStack(toWithdraw);
                                withdrawnSomething = true;
                            }
                        }
                        
                        if ((hasAxe() || hasShears()) && hasEnoughSaplings()) {
                            golemInv.markDirty();
                            container.markDirty();
                            return true;
                        }
                        withdrawnSomething = true;
                        continue;
                    }

                    if ((golem.getGolemType() == GolemType.NETHERITE || golem.getGolemType() == GolemType.ANCIENT) && UtilityGolem.isSword(containerStack)) {
                        ItemStack sword = containerStack.split(1);
                        golem.setHeldItem(sword);
                        golemInv.markDirty();
                        container.markDirty();
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.NETHER_WART) {
                        if (containerStack.isOf(Items.GLASS_BOTTLE) || isIngredient(containerStack) || containerStack.isOf(Items.BLAZE_POWDER) || containerStack.get(DataComponentTypes.POTION_CONTENTS) != null) {
                            // Only withdraw if we have room AND we need it
                            // Try to keep 3 slots for water bottles (total 9 slots in UtilityGolem inventory)
                            int filledSlots = 0;
                            for (int j = 0; j < golemInv.size(); j++) {
                                if (!golemInv.getStack(j).isEmpty()) filledSlots++;
                            }
                            
                            if (filledSlots < 6) { // Max 6 slots for ingredients/powder/bottles
                                ItemStack remaining = transferStack(containerStack, golemInv);
                                container.setStack(i, remaining);
                                withdrawnSomething = true;
                                if (isInventoryFull()) {
                                    golemInv.markDirty();
                                    container.markDirty();
                                    return true;
                                }
                            }
                            continue;
                        }
                    }

                    if (golem.getGolemType() == GolemType.EMERALD) {
                        List<TradeOfferList> allOffers = findNearbyVillagerOffers();
                        if (!allOffers.isEmpty()) {
                            boolean withdrawnSomethingInThisItem = false;
                            for (TradeOfferList offers : allOffers) {
                                for (TradeOffer offer : offers) {
                                    if (offer.isDisabled()) continue;
                                    if (offer.getSellItem().isOf(Items.EMERALD)) {
                                        TradedItem buyItem1 = offer.getFirstBuyItem();
                                        Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();

                                        if (buyItem1.matches(containerStack)) {
                                            int countInInv = getCountInInventory(buyItem1);
                                            int needed = buyItem1.count() - countInInv;
                                            if (needed > 0) {
                                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                                ItemStack remaining = golem.getInventory().addStack(toWithdraw);
                                                if (!remaining.isEmpty()) {
                                                    containerStack.increment(remaining.getCount());
                                                }
                                                withdrawnSomething = true;
                                                withdrawnSomethingInThisItem = true;
                                                if (hasTradeItems() || isInventoryFull()) {
                                                    golemInv.markDirty();
                                                    container.markDirty();
                                                    return true;
                                                }
                                                break;
                                            }
                                        } else if (buyItem2.isPresent() && buyItem2.get().matches(containerStack)) {
                                            int countInInv = getCountInInventory(buyItem2.get());
                                            int needed = buyItem2.get().count() - countInInv;
                                            if (needed > 0) {
                                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                                ItemStack remaining = golem.getInventory().addStack(toWithdraw);
                                                if (!remaining.isEmpty()) {
                                                    containerStack.increment(remaining.getCount());
                                                }
                                                withdrawnSomething = true;
                                                withdrawnSomethingInThisItem = true;
                                                if (hasTradeItems() || isInventoryFull()) {
                                                    golemInv.markDirty();
                                                    container.markDirty();
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
                    golemInv.markDirty();
                    container.markDirty();
                    return true;
                }
            }
            return false;
        }

        private int getCountInInventory(TradedItem target) {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (target.matches(stack)) count += stack.getCount();
            }
            return count;
        }

        private boolean isValidBreedingItem(ItemStack stack) {
            return stack.isOf(Items.WHEAT) || stack.isOf(Items.CARROT) ||
                    stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT) ||
                    stack.isOf(Items.WHEAT_SEEDS);
        }

        private ItemStack transferStack(ItemStack stack, Inventory container) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < container.size(); i++) {
                ItemStack containerStack = container.getStack(i);
                if (canCombine(remaining, containerStack)) {
                    int transferAmount = Math.min(remaining.getCount(), containerStack.getMaxCount() - containerStack.getCount());
                    if (transferAmount > 0) {
                        containerStack.increment(transferAmount);
                        remaining.decrement(transferAmount);
                    }
                }
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
            for (int i = 0; i < container.size(); i++) {
                if (container.getStack(i).isEmpty()) {
                    container.setStack(i, remaining);
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }

        private boolean canCombine(ItemStack stack, ItemStack other) {
            return !other.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, other) && other.getCount() < other.getMaxCount();
        }
    }
    public static class DigBlockGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private int breakingTime;
        private int maxBreakingTime;
        private int searchCooldown = 0;
        private boolean isAirTarget = false;

        public DigBlockGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            targetPos = findTargetBlock();
            if (targetPos != null) {
                BlockState state = golem.getEntityWorld().getBlockState(targetPos);
                if (state.isAir()) {
                    // Only log if we are far enough to need navigation
                    double distSq = golem.squaredDistanceTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    if (distSq > 0.1) {
                        golem.debugLog("DigBlockGoal: Navigating to air target at " + targetPos.toShortString());
                    }
                    this.maxBreakingTime = 1;
                    this.isAirTarget = true;
                    return true;
                }
                
                this.isAirTarget = false;
                golem.debugLog("DigBlockGoal: Found target block " + state.getBlock().getName().getString() + " at " + targetPos.toShortString());
                ItemStack tool = golem.getHeldItem();
                // Ensure we have the right tool held or can swap to it
                boolean needsPickaxe = state.isIn(BlockTags.BASE_STONE_OVERWORLD) || state.isIn(BlockTags.BASE_STONE_NETHER)
                        || state.isIn(BlockTags.COAL_ORES) || state.isIn(BlockTags.IRON_ORES) || state.isIn(BlockTags.COPPER_ORES)
                        || state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES)
                        || state.isIn(BlockTags.LAPIS_ORES) || state.isIn(BlockTags.REDSTONE_ORES);
                boolean needsShovel = state.isIn(BlockTags.SHOVEL_MINEABLE) || state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.SAND) || state.isOf(Blocks.GRAVEL);

                if (needsPickaxe && !UtilityGolem.isPickaxe(tool)) {
                    // Try to swap immediately if possible, or at least confirm we have one
                    if (hasPickaxe()) {
                        this.maxBreakingTime = 200; // Placeholder, will be recalculated in tick after swap
                        return true;
                    }
                } else if (needsShovel && !UtilityGolem.isShovel(tool)) {
                    if (hasShovel()) {
                        this.maxBreakingTime = 200; // Placeholder
                        return true;
                    }
                } else if (!tool.isEmpty() && (UtilityGolem.isPickaxe(tool) || UtilityGolem.isShovel(tool))) {
                    this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                    return true;
                }
            } else {
                searchCooldown = 20;
            }
            return false;
        }

        private int calculateBreakingTime(ItemStack tool, BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            float hardness = state.getHardness(golem.getEntityWorld(), pos);
            if (hardness < 0) return 200; // Unbreakable

            float speed = 1.0f;
            if (UtilityGolem.isPickaxe(tool)) {
                if (tool.isOf(Items.GOLDEN_PICKAXE)) speed = 9.0f;
                else if (tool.isOf(Items.NETHERITE_PICKAXE)) speed = 12.0f;
                else if (tool.isOf(Items.DIAMOND_PICKAXE)) speed = 12.0f;
                else if (tool.isOf(Items.IRON_PICKAXE)) speed = 6.0f;
                else if (tool.isOf(Items.STONE_PICKAXE)) speed = 4.0f;
                else if (tool.isOf(Items.WOODEN_PICKAXE)) speed = 2.0f;
                else if (tool.isOf(Items.COPPER_PICKAXE)) speed = 5.0f;
            } else if (UtilityGolem.isShovel(tool)) {
                if (tool.isOf(Items.GOLDEN_SHOVEL)) speed = 9.0f;
                else if (tool.isOf(Items.NETHERITE_SHOVEL)) speed = 12.0f;
                else if (tool.isOf(Items.DIAMOND_SHOVEL)) speed = 12.0f;
                else if (tool.isOf(Items.IRON_SHOVEL)) speed = 6.0f;
                else if (tool.isOf(Items.STONE_SHOVEL)) speed = 4.0f;
                else if (tool.isOf(Items.WOODEN_SHOVEL)) speed = 2.0f;
                else if (tool.isOf(Items.COPPER_SHOVEL)) speed = 5.0f;
            }

            // Player mining formula is roughly (hardness * 30) / speed if using correct tool
            // We want it slower than player, so let's use a higher multiplier
            return Math.max(20, (int) (hardness * 60 / speed));
        }


        private BlockPos findTargetBlock() {
            BlockPos pos = golem.getBlockPos();
            BlockPos chestPos = golem.getChestPos();
            World world = golem.getEntityWorld();

            // 1. Prioritize Visible Ores
            int oreRange = 8;
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            double minOreDistSq = Double.MAX_VALUE;
            BlockPos bestOre = null;

            for (int x = -oreRange; x <= oreRange; x++) {
                for (int y = -oreRange; y <= oreRange; y++) {
                    for (int z = -oreRange; z <= oreRange; z++) {
                        mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (golem.isBlacklisted(mutable)) continue;
                        if (isOre(mutable) && canDig(mutable)) {
                            // Lapis golems should only prioritize ores that are not too far above them during staircase mining
                            if (golem.getGolemType() == GolemType.LAPIS && mutable.getY() > pos.getY() + 2) continue;

                            if (chestPos == null || mutable.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                // Prevent digging under feet
                                if (golem.getGolemType() == GolemType.LAPIS && mutable.getY() == pos.getY() - 1 && mutable.getX() == pos.getX() && mutable.getZ() == pos.getZ()) continue;
                                
                                double distSq = mutable.getSquaredDistance(pos);
                                if (distSq < minOreDistSq) {
                                    minOreDistSq = distSq;
                                    bestOre = mutable.toImmutable();
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
                    if (!targetState.isAir() && !canDig(target)) {
                        golem.debugLog("DigBlockGoal: Lapis cannot dig staircase target " + targetState.getBlock().getName().getString() + " at " + target.toShortString() + " (missing tool?)");
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

                            if (chestPos == null || mutable.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                double distSq = mutable.getSquaredDistance(pos);
                                if (distSq < minTargetDistSq) {
                                    minTargetDistSq = distSq;
                                    bestTarget = mutable.toImmutable();
                                }
                            }
                        }
                    }
                }
            }

            return bestTarget;
        }

        private boolean isOre(BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            if (!(state.isIn(BlockTags.COAL_ORES) || state.isIn(BlockTags.IRON_ORES) || state.isIn(BlockTags.COPPER_ORES)
                    || state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES)
                    || state.isIn(BlockTags.LAPIS_ORES) || state.isIn(BlockTags.REDSTONE_ORES))) {
                return false;
            }

            // Visibility check: is it adjacent to an air block or a non-opaque block?
            // Optimization: check common directions first
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (Direction direction : Direction.values()) {
                mutable.set(pos, direction);
                BlockState neighborState = golem.getEntityWorld().getBlockState(mutable);
                if (neighborState.isAir() || !neighborState.isFullCube(golem.getEntityWorld(), mutable)) {
                    return true;
                }
            }
            return false;
        }

        private BlockPos findStaircaseOrTunnelBlock() {
            BlockPos pos = golem.getBlockPos();
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
            int lockedCoord = (facing.getAxis() == Direction.Axis.Z) ? chestPos.getX() : chestPos.getZ();

            // The staircase starts ONE block away from the chest.
            BlockPos startPos = chestPos.offset(facing);

            // Distance from startPos along the mining direction
            int directionalDist = switch (facing) {
                case EAST -> pos.getX() - startPos.getX();
                case WEST -> startPos.getX() - pos.getX();
                case SOUTH -> pos.getZ() - startPos.getZ();
                case NORTH -> startPos.getZ() - pos.getZ();
                default -> 0;
            };

            // Target depth for the tunnel
            int targetDepthY = -54;

            // Calculate the intended Y level for the golem's current position.
            // At directionalDist = 0 (startPos), intendedY = chestPos.getY().
            // For every 1 block forward, we drop 1 block in Y.
            int intendedY = chestPos.getY() - Math.max(0, directionalDist);
            if (intendedY < targetDepthY) intendedY = targetDepthY;

            // Check horizontal alignment (are we on the locked line?)
            double distToLine = (facing.getAxis() == Direction.Axis.Z)
                    ? Math.abs(golem.getX() - (lockedCoord + 0.5))
                    : Math.abs(golem.getZ() - (lockedCoord + 0.5));

            // Only align horizontally to the locked line; do NOT dig straight down to match intendedY.
            if (distToLine > 0.4) {
                BlockPos targetOnLine = (facing.getAxis() == Direction.Axis.Z)
                        ? new BlockPos(lockedCoord, pos.getY(), pos.getZ())
                        : new BlockPos(pos.getX(), pos.getY(), lockedCoord);

                for (int yOffset = 2; yOffset >= 0; yOffset--) {
                    BlockPos p = targetOnLine.up(yOffset);
                    BlockState state = golem.getEntityWorld().getBlockState(p);
                    if (canDig(p) && !state.isAir()) {
                        if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                            continue;
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
                BlockPos ahead = pos.offset(facing);
                for (int yOffset = 2; yOffset >= 0; yOffset--) {
                    BlockPos p = ahead.up(yOffset);
                    BlockState state = golem.getEntityWorld().getBlockState(p);
                    if (canDig(p) && !state.isAir()) {
                        if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                            continue;
                        }
                        golem.debugLog("Lapis: Tunnel digging at " + p.toShortString());
                        return p;
                    }
                }
                return ahead;
            }

            // Otherwise, dig the next step down in the staircase.
            // Choose a forward distance so the next step is at most 1 block below our current height.
            int baseDist = Math.max(0, directionalDist);
            int nextDist = baseDist + 1;

            // If we are high above the staircase height for nextDist, advance further so nextY == posY - 1
            if (pos.getY() > chestPos.getY() - nextDist + 1) {
                int distForOneDown = chestPos.getY() - (pos.getY() - 1);
                if (distForOneDown < nextDist) distForOneDown = nextDist;
                nextDist = distForOneDown;
            }

            int nextY = Math.max(targetDepthY, chestPos.getY() - nextDist);
            BlockPos nextStepMiddle = (facing.getAxis() == Direction.Axis.X)
                    ? new BlockPos(startPos.getX() + facing.getOffsetX() * nextDist, nextY, lockedCoord)
                    : new BlockPos(lockedCoord, nextY, startPos.getZ() + facing.getOffsetZ() * nextDist);

            // Clear the 3-high path for the next step.
            for (int yOffset = 2; yOffset >= 0; yOffset--) {
                BlockPos p = nextStepMiddle.up(yOffset);
                BlockState state = golem.getEntityWorld().getBlockState(p);
                if (canDig(p) && !state.isAir()) {
                    // Skip light sources
                    if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isLightSource(state)) {
                        continue;
                    }
                    golem.debugLog("Lapis: Staircase digging next step at " + p.toShortString());
                    return p;
                }
            }

            // If next step is already clear, return it to navigate there.
            return nextStepMiddle;
        }

        private BlockPos findNearbyChest() {
            if (golem.getChestPos() != null) {
                if (golem.isBlacklisted(golem.getChestPos())) {
                    golem.setChestPos(null);
                } else {
                    BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                    if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                        return golem.getChestPos();
                    }
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.isBlacklisted(p)) continue;
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        Block targetChest = golem.getGolemType().getChestBlock();
                        if (be instanceof Inventory && block == targetChest) {
                            golem.debugLog("DigBlockGoal: Found chest at " + p.toShortString());
                            golem.setChestPos(p);
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean tryPlaceStepUp() {
            if (golem.getInventory().isEmpty()) return false;
            
            BlockPos pos = golem.getBlockPos();
            BlockState underState = golem.getEntityWorld().getBlockState(pos.down());
            
            // If we are in a hole (surrounded by blocks) and the target is above us
            if (targetPos != null && targetPos.getY() > pos.getY()) {
                // Look for a place to put a block to step up
                // Simplest: place under feet if we're in a 1x1 hole
                if (golem.getEntityWorld().getBlockState(pos).isAir() || golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                    return placeBlockFromInventory(pos);
                }
            }
            return false;
        }

        private boolean placeBlockFromInventory(BlockPos pos) {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    // Prefer dirt or cobblestone-like blocks
                    if (stack.isIn(net.minecraft.registry.tag.ItemTags.DIRT) || stack.isOf(Items.COBBLESTONE) || stack.isOf(Items.COBBLED_DEEPSLATE)) {
                        if (golem.getEntityWorld().setBlockState(pos, block.getDefaultState())) {
                            stack.decrement(1);
                            golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                            golem.getEntityWorld().playSound(null, pos, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                            // Teleport golem slightly up if we placed under feet
                            if (pos.equals(golem.getBlockPos())) {
                                golem.setPosition(golem.getX(), golem.getY() + 1.0, golem.getZ());
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean canDig(BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            if (!state.getFluidState().isEmpty()) return false;
            if (golem.getGolemType() == GolemType.LAPIS) {
                if (UtilityGolem.isLightSource(state)) return false;
                if (state.isAir() || state.isIn(BlockTags.REPLACEABLE)) return true;
            }
            if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) || state.isIn(BlockTags.BASE_STONE_NETHER)
                    || state.isIn(BlockTags.COAL_ORES) || state.isIn(BlockTags.IRON_ORES) || state.isIn(BlockTags.COPPER_ORES)
                    || state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES)
                    || state.isIn(BlockTags.LAPIS_ORES) || state.isIn(BlockTags.REDSTONE_ORES)) {
                return hasPickaxe();
            }
            if (state.isIn(BlockTags.SHOVEL_MINEABLE) || state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.SAND) || state.isOf(Blocks.GRAVEL)) {
                return hasShovel();
            }
            // Add general check for very soft blocks like grass
            if (state.getHardness(golem.getEntityWorld(), pos) == 0.0f) return true;
            
            return false;
        }

        private boolean isShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isShovel(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasPickaxe() {
            if (UtilityGolem.isPickaxe(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isPickaxe(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean hasShovel() {
            if (UtilityGolem.isShovel(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isShovel(inv.getStack(i))) return true;
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
        public boolean shouldContinue() {
            return targetPos != null && canDig(targetPos) &&
                    breakingTime < maxBreakingTime && golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 400;
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, -1);
            }
            targetPos = null;
            golem.setAnimation(GolemAnimation.IDLE, 0);
        }

        private int stuckTicks = 0;
        private Vec3d lastPos = Vec3d.ZERO;

        @Override
        public void tick() {
            if (targetPos == null) return;

            BlockState targetState = golem.getEntityWorld().getBlockState(targetPos);

            // Ensure animation is active while digging
            if (!isAirTarget && (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1)) {
                golem.setAnimation(GolemAnimation.DIGGING, 40);
            }

            // Auto-switch tool
            ItemStack currentHeld = golem.getHeldItem();
            boolean needsPickaxe = targetState.isIn(BlockTags.BASE_STONE_OVERWORLD) || targetState.isIn(BlockTags.BASE_STONE_NETHER)
                    || targetState.isIn(BlockTags.COAL_ORES) || targetState.isIn(BlockTags.IRON_ORES) || targetState.isIn(BlockTags.COPPER_ORES)
                    || targetState.isIn(BlockTags.GOLD_ORES) || targetState.isIn(BlockTags.DIAMOND_ORES) || targetState.isIn(BlockTags.EMERALD_ORES)
                    || targetState.isIn(BlockTags.LAPIS_ORES) || targetState.isIn(BlockTags.REDSTONE_ORES);
            boolean needsShovel = targetState.isIn(BlockTags.SHOVEL_MINEABLE) || targetState.isIn(BlockTags.DIRT) || targetState.isIn(BlockTags.SAND) || targetState.isOf(Blocks.GRAVEL);

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

            if (horizontalDistSq > 9.0D || verticalDist > 2.0D) {
                // stuck check
                Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.squaredDistanceTo(lastPos) < 0.001) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastPos = currentPos;

                if (stuckTicks > 100) {
                    golem.debugLog("DigBlockGoal: Stuck at " + golem.getBlockPos().toShortString() + " trying to reach " + targetPos.toShortString());
                    if (golem.getGolemType() == GolemType.LAPIS && tryPlaceStepUp()) {
                        golem.debugLog("Lapis: Attempting to place step up");
                        stuckTicks = 0;
                        return;
                    }
                    golem.debugLog("DigBlockGoal: Blacklisting unreachable block at " + targetPos.toShortString());
                    golem.blacklistPosition(targetPos);
                    stop();
                    return;
                }

                // If it's high up or far below, move to the XZ position at our current height
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // Lapis golems always try to move to the exact targetPos for better staircase navigation
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    } else if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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
                double alignmentDist = (golem.getMiningDirection() != null && golem.getMiningDirection().getAxis() == Direction.Axis.X)
                        ? Math.abs(golem.getZ() - (targetPos.getZ() + 0.5))
                        : Math.abs(golem.getX() - (targetPos.getX() + 0.5));
                
                // If it's the exact same block we're in, we've definitely reached it
                if (alignmentDist <= 0.1 || targetPos.equals(golem.getBlockPos())) {
                    golem.debugLog("DigBlockGoal: Reached air alignment target " + targetPos.toShortString());
                    stop();
                    return;
                }
                
                // Keep moving to it
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                // Swing arm every 5 ticks
                if (!isAirTarget && breakingTime % 5 == 0) {
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }

                breakingTime++;
                int progress = (int) ((float) breakingTime / (float) maxBreakingTime * 10.0F);
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, progress);

                if (breakingTime >= maxBreakingTime) {
                    if (golem.getEntityWorld().getBlockState(targetPos).isAir()) {
                        stop();
                    } else {
                        breakBlock();
                    }
                }
            }
        }

        private void swapTool(java.util.function.Predicate<ItemStack> toolPredicate) {
            SimpleInventory inv = golem.getInventory();
            ItemStack currentHeld = golem.getHeldItem();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (toolPredicate.test(stack)) {
                    ItemStack newTool = inv.removeStack(i, 1);
                    if (!currentHeld.isEmpty()) {
                        ItemStack remaining = inv.addStack(currentHeld);
                        if (!remaining.isEmpty()) {
                            golem.getEntityWorld().spawnEntity(new net.minecraft.entity.ItemEntity(golem.getEntityWorld(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                        }
                    }
                    golem.setHeldItem(newTool);
                    // Recalculate maxBreakingTime for the new tool
                    this.maxBreakingTime = calculateBreakingTime(newTool, targetPos);
                    break;
                }
            }
        }

        private void breakBlock() {
            if (!(golem.getEntityWorld() instanceof ServerWorld serverWorld)) return;

            BlockState state = serverWorld.getBlockState(targetPos);
            if (canDig(targetPos)) {
                ItemStack tool = golem.getHeldItem();
                
                LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                        .add(LootContextParameters.TOOL, tool)
                        .addOptional(LootContextParameters.THIS_ENTITY, golem);

                serverWorld.breakBlock(targetPos, false, golem);

                List<ItemStack> drops = state.getDroppedStacks(builder);
                for (ItemStack drop : drops) {
                    ItemStack remaining = golem.getInventory().addStack(drop);
                    if (!remaining.isEmpty()) {
                        Block.dropStack(serverWorld, targetPos, remaining);
                    }
                }
                
                // Damage the tool
                if (!tool.isEmpty()) {
                    if (UtilityGolem.isPickaxe(tool) || UtilityGolem.isShovel(tool)) {
                        tool.damage(1, serverWorld, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() != GolemType.BAMBOO) return false;
            
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) {
                // If we don't have a chest, try to find one.
                // This allows the golem to start farming as soon as a chest is placed.
                chestPos = findNearbyChest();
            }
            if (chestPos == null) return false;

            targetPos = findTargetPos();
            return targetPos != null;
        }

        private BlockPos findNearbyChest() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.isBlacklisted(p)) continue;
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        Block targetChest = golem.getGolemType().getChestBlock();
                        if (be instanceof Inventory && block == targetChest) {
                            golem.setChestPos(p);
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private BlockPos findTargetPos() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return null;

            // Search the whole 10x10 area around the chest for tasks
            // This is more robust than only searching around water centers
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    for (int y = -3; y <= 3; y++) {
                        BlockPos p = chestPos.add(x, y, z);
                        if (p.equals(chestPos) || golem.isBlacklisted(p)) {
                            continue;
                        }

                        // Use null waterPos to relax distance checks during the initial scan
                        if (shouldHarvest(p, null)) {
                            return p;
                        }
                    }
                }
            }

            // If no harvesting found, try to find a water source if we have a bucket
            BlockPos waterPos = findWaterCenter(chestPos);
            if (waterPos == null && hasWaterBucket()) {
                BlockPos waterSpot = findPlaceForWater(chestPos);
                if (waterSpot != null) return waterSpot;
            }

            // If we have water, search for tilling and planting
            if (waterPos != null) {
                for (int x = -4; x <= 4; x++) {
                    for (int z = -4; z <= 4; z++) {
                        for (int y = -1; y <= 1; y++) {
                            BlockPos p = waterPos.add(x, y, z);
                            if (p.equals(waterPos) || golem.isBlacklisted(p)) {
                                continue;
                            }
                            if (shouldTill(p, waterPos) || shouldPlant(p, waterPos)) {
                                return p;
                            }
                        }
                    }
                }
            } else if (!hasWaterBucket()) {
                // Fallback: search around chest if no water exists and we can't make it
                for (int x = -10; x <= 10; x++) {
                    for (int z = -10; z <= 10; z++) {
                        for (int y = -3; y <= 3; y++) {
                            BlockPos p = chestPos.add(x, y, z);
                            if (p.equals(chestPos) || golem.isBlacklisted(p)) continue;
                            if (shouldTill(p, null) || shouldPlant(p, null)) return p;
                        }
                    }
                }
            }

            return null;
        }

        private boolean isWater(BlockPos pos) {
            return golem.getEntityWorld().getFluidState(pos).isIn(net.minecraft.registry.tag.FluidTags.WATER);
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
                        BlockPos p = chestPos.add(x, y, z);
                        if (isWater(p)) {
                            double dist = p.getSquaredDistance(chestPos);
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
                        BlockPos p = chestPos.add(x, y, z);
                        if (canPlaceWater(p)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean canPlaceWater(BlockPos pos) {
            World world = golem.getEntityWorld();
            BlockState state = world.getBlockState(pos);
            // Don't place water if there is already water nearby (prevents rows)
            if (findWaterNearby(pos, 4) != null) return false;

            // Target must be replaceable (like air, grass) or a dirt block we can dig out
            if (!state.isReplaceable() && !state.isIn(BlockTags.DIRT)) return false;
            
            // The block ABOVE must be air or replaceable (not water, not solid)
            BlockState above = world.getBlockState(pos.up());
            if (!above.isAir() && !above.isReplaceable()) return false;
            if (above.isOf(Blocks.WATER)) return false;

            // Check if surrounded by dirt/grass/farmland to ensure it's a good farm spot
            int dirtCount = 0;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockState s = world.getBlockState(pos.add(x, 0, z));
                    if (s.isIn(BlockTags.DIRT) || s.isOf(Blocks.FARMLAND) || s.isOf(Blocks.GRASS_BLOCK)) {
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
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getBlockState(p).isOf(Blocks.WATER)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private boolean shouldHarvest(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof CropBlock crop) {
                return crop.isMature(state);
            }
            if (block instanceof NetherWartBlock wart) {
                return state.get(NetherWartBlock.AGE) >= 3;
            }
            if (block instanceof CocoaBlock cocoa) {
                return state.get(CocoaBlock.AGE) >= 2;
            }
            return false;
        }

        private boolean shouldTill(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            if (!hasHoe()) return false;
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            // Must be tillable AND have air or replaceable block above it (like short grass)
            // But NOT water! We don't want to override water.
            boolean isTillable = state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH);
            BlockState aboveState = golem.getEntityWorld().getBlockState(pos.up());
            boolean isAboveSafe = aboveState.isAir() || (aboveState.isReplaceable() && !aboveState.isOf(Blocks.WATER));
            return isTillable && isAboveSafe;
        }

        private boolean hasHoe() {
            if (UtilityGolem.isHoe(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isHoe(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean shouldPlant(BlockPos pos, BlockPos waterPos) {
            if (waterPos != null && pos.equals(waterPos)) return false;
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            BlockState aboveState = golem.getEntityWorld().getBlockState(pos.up());
            boolean isAboveSafe = aboveState.isAir() || (aboveState.isReplaceable() && !aboveState.isOf(Blocks.WATER));
            
            // Standard crops
            if (state.isOf(Blocks.FARMLAND) && isAboveSafe && hasSeeds()) return true;

            // Carrots and Potatoes on Farmland
            if (state.isOf(Blocks.FARMLAND) && isAboveSafe && (hasItem(Items.CARROT) || hasItem(Items.POTATO))) return true;

            // Nether wart
            if (state.isOf(Blocks.SOUL_SAND) && isAboveSafe && hasNetherWart()) return true;

            // Cocoa beans
            if (state.isAir() && hasCocoaBeans() && findJungleLogNearby(pos) != null) return true;

            return false;
        }

        private boolean hasItem(Item item) {
            if (golem.getHeldItem().isOf(item)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(item)) return true;
            }
            return false;
        }

        private boolean hasNetherWart() {
            if (golem.getHeldItem().isOf(Items.NETHER_WART)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.NETHER_WART)) return true;
            }
            return false;
        }

        private boolean hasCocoaBeans() {
            if (golem.getHeldItem().isOf(Items.COCOA_BEANS)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.COCOA_BEANS)) return true;
            }
            return false;
        }

        private boolean hasSeeds() {
            if (isSeed(golem.getHeldItem())) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isSeed(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean isSeed(ItemStack stack) {
            return stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT_SEEDS)
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS)
                    || stack.isOf(Items.PITCHER_POD) || stack.isOf(Items.TORCHFLOWER_SEEDS);
        }


        @Override
        public boolean shouldContinue() {
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
        private Vec3d lastPos = Vec3d.ZERO;

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Ensure animation is active while farming
            if (golem.getAnimation() == GolemAnimation.IDLE || golem.getAnimationTicks() <= 1) {
                golem.setAnimation(GolemAnimation.FARMING, 40);
            }

            // Ensure we are holding the right tool for the job
            ensureCorrectTool();

            double dist = golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if (dist > 4.0) {
                // stuck check
                Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
                if (currentPos.squaredDistanceTo(lastPos) < 0.001) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastPos = currentPos;

                if (stuckTicks > 100) {
                    golem.blacklistPosition(targetPos);
                    targetPos = null;
                    return;
                }

                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                    if (!possible) {
                        golem.blacklistPosition(targetPos);
                        targetPos = null;
                        return;
                    }
                }
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30.0F, 30.0F);
                
                farmActionTime++;
                if (farmActionTime % 5 == 0) {
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND); // might not work?
                }

                if (farmActionTime >= MAX_FARM_ACTION_TIME) {
                    performFarmAction();
                    farmActionTime = 0;
                    targetPos = findTargetPos();
                }
            }
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
                if (!golem.getHeldItem().isOf(Items.WATER_BUCKET)) {
                    swapToItem(Items.WATER_BUCKET);
                }
            } else if (shouldPlant(targetPos, waterPos)) {
                // If we are heading to plant, we should hold the seeds
                ItemStack seeds = getSeedsForTarget(targetPos);
                if (!seeds.isEmpty() && !golem.getHeldItem().isOf(seeds.getItem())) {
                    swapToItem(seeds.getItem());
                }
            } else if (shouldTill(targetPos, waterPos)) {
                // If we are heading to till, we should hold a hoe
                if (!UtilityGolem.isHoe(golem.getHeldItem())) {
                    swapToHoe();
                }
            }
        }

        private void swapToItem(net.minecraft.item.Item itemType) {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(itemType)) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(stack.copy());
                    inv.setStack(i, currentHeld);
                    break;
                }
            }
        }

        private void swapToHoe() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (UtilityGolem.isHoe(stack)) {
                    ItemStack currentHeld = golem.getHeldItem();
                    golem.setHeldItem(stack.copy());
                    inv.setStack(i, currentHeld);
                    break;
                }
            }
        }

        private void performFarmAction() {
            World world = golem.getEntityWorld();
            BlockState state = world.getBlockState(targetPos);
            BlockPos waterPos = findWaterCenter(golem.getChestPos());
            
            // 1. Pickup items
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, new net.minecraft.util.math.Box(targetPos).expand(1.5), item -> true);
            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    ItemStack stack = item.getStack();
                    ItemStack remaining = golem.getInventory().addStack(stack);
                    if (remaining.isEmpty()) {
                        item.discard();
                    } else {
                        item.setStack(remaining);
                    }
                }
                targetPos = null;
                return;
            }

            // 2. Water
            if ((waterPos != null && targetPos.equals(waterPos)) || (waterPos == null && targetPos.equals(findPlaceForWater(golem.getChestPos())))) {
                if (!isWater(targetPos)) {
                    ItemStack waterBucket = golem.getHeldItem();
                    if (!waterBucket.isOf(Items.WATER_BUCKET)) {
                        waterBucket = getWaterBucket();
                    }
                    
                    if (!waterBucket.isEmpty()) {
                        world.setBlockState(targetPos, Blocks.WATER.getDefaultState());
                        useWaterBucket(waterBucket);

                        // After placing water, if we have a hoe in inventory, swap back to it
                        if (hasHoe() && !UtilityGolem.isHoe(golem.getHeldItem())) {
                            swapToHoe();
                        }
                    }
                }
                targetPos = null;
                return;
            }
            
            // If waterPos is NOT null, and we are NOT at waterPos, and waterPos is NOT water,
            // then we should NOT be doing other farm actions yet.
            if (waterPos != null && !isWater(waterPos)) {
                targetPos = null;
                return;
            }

            // 3. Harvest
            if (shouldHarvest(targetPos, waterPos)) {
                if (world instanceof ServerWorld serverWorld) {
                    LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                            .add(LootContextParameters.TOOL, golem.getHeldItem())
                            .addOptional(LootContextParameters.THIS_ENTITY, golem);

                    List<ItemStack> drops = state.getDroppedStacks(builder);
                    serverWorld.breakBlock(targetPos, false, golem);
                    for (ItemStack drop : drops) {
                        ItemStack remaining = golem.getInventory().addStack(drop);
                        if (!remaining.isEmpty()) {
                            Block.dropStack(serverWorld, targetPos, remaining);
                        }
                    }
                    
                    // Swing hand to show action
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }
                targetPos = null;
                return;
            }

            // 4. Till
            if (shouldTill(targetPos, waterPos)) {
                world.setBlockState(targetPos, Blocks.FARMLAND.getDefaultState());
                world.playSound(null, targetPos, net.minecraft.sound.SoundEvents.ITEM_HOE_TILL, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
                
                // Damage hoe
                ItemStack hoe = golem.getHeldItem();
                if (world instanceof ServerWorld serverWorld) {
                    hoe.damage(1, serverWorld, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
                }
                targetPos = null; // Reset target after action
                return;
            }

            // 5. Plant
            if (shouldPlant(targetPos, waterPos)) {
                ItemStack seeds = golem.getHeldItem();
                if (!isSeed(seeds)) {
                    seeds = getSeeds();
                }
                
                if (!seeds.isEmpty()) {
                    BlockPos plantPos = targetPos.up();
                    Block seedBlock = getSeedBlock(seeds);
                        if (seedBlock != null) {
                            // Specialized planting for certain crops
                            if (seedBlock == Blocks.COCOA) {
                                // Find jungle log to plant on
                                BlockPos logPos = findJungleLogNearby(targetPos);
                                if (logPos != null) {
                                    Direction dir = getDirectionToPlantCocoa(targetPos, logPos);
                                    world.setBlockState(targetPos, seedBlock.getDefaultState().with(CocoaBlock.FACING, dir));
                                    seeds.decrement(1);
                                    if (seeds.isEmpty() && golem.getHeldItem() == seeds) {
                                        golem.setHeldItem(ItemStack.EMPTY);
                                    }
                                    world.playSound(null, targetPos, net.minecraft.sound.SoundEvents.BLOCK_GRASS_PLACE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
                                }
                            } else {
                                world.setBlockState(plantPos, seedBlock.getDefaultState());
                                seeds.decrement(1);
                                if (seeds.isEmpty() && golem.getHeldItem() == seeds) {
                                    golem.setHeldItem(ItemStack.EMPTY);
                                }
                                world.playSound(null, targetPos, net.minecraft.sound.SoundEvents.BLOCK_GRASS_PLACE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
                            }

                            // After planting, if we have a hoe in inventory, swap back to it
                            if (hasHoe() && !UtilityGolem.isHoe(golem.getHeldItem())) {
                                swapToHoe();
                            }
                        }
                }
                targetPos = null; // Reset target after action
            }
        }

        private ItemStack getWaterBucket() {
            if (golem.getHeldItem().isOf(Items.WATER_BUCKET)) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }

        private void useWaterBucket(ItemStack waterBucket) {
            if (waterBucket.getCount() == 1) {
                // If it's in the hand, we can just replace it.
                // If it's in the inventory, it's more complex because it becomes an empty bucket.
                if (golem.getHeldItem() == waterBucket) {
                    golem.setHeldItem(new ItemStack(Items.BUCKET));
                } else {
                    // It's in the inventory. We need to find its slot.
                    SimpleInventory inv = golem.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        if (inv.getStack(i) == waterBucket) {
                            inv.setStack(i, new ItemStack(Items.BUCKET));
                            break;
                        }
                    }
                }
            } else {
                waterBucket.decrement(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                ItemStack remaining = golem.getInventory().addStack(emptyBucket);
                if (!remaining.isEmpty()) {
                    Block.dropStack(golem.getEntityWorld(), golem.getBlockPos(), remaining);
                }
            }
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().isOf(Items.WATER_BUCKET)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private ItemStack getSeeds() {
            if (isSeed(golem.getHeldItem())) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isSeed(inv.getStack(i))) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getSeedsForTarget(BlockPos pos) {
            World world = golem.getEntityWorld();
            BlockState state = world.getBlockState(pos);
            if (state.isOf(Blocks.FARMLAND)) return getStandardSeeds();
            if (state.isOf(Blocks.SOUL_SAND)) return getNetherWart();
            if (findJungleLogNearby(pos) != null) return getCocoaBeans();
            return getSeeds();
        }

        private ItemStack getStandardSeeds() {
            if (isSeed(golem.getHeldItem()) && !golem.getHeldItem().isOf(Items.NETHER_WART) && !golem.getHeldItem().isOf(Items.COCOA_BEANS)) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (isSeed(stack) && !stack.isOf(Items.NETHER_WART) && !stack.isOf(Items.COCOA_BEANS)) return stack;
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getNetherWart() {
            if (golem.getHeldItem().isOf(Items.NETHER_WART)) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.NETHER_WART)) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getCocoaBeans() {
            if (golem.getHeldItem().isOf(Items.COCOA_BEANS)) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.COCOA_BEANS)) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }

        private Block getSeedBlock(ItemStack seeds) {
            if (seeds.isOf(Items.WHEAT_SEEDS)) return Blocks.WHEAT;
            if (seeds.isOf(Items.CARROT)) return Blocks.CARROTS;
            if (seeds.isOf(Items.POTATO)) return Blocks.POTATOES;
            if (seeds.isOf(Items.BEETROOT_SEEDS)) return Blocks.BEETROOTS;
            if (seeds.isOf(Items.PUMPKIN_SEEDS)) return Blocks.PUMPKIN_STEM;
            if (seeds.isOf(Items.MELON_SEEDS)) return Blocks.MELON_STEM;
            if (seeds.isOf(Items.NETHER_WART)) return Blocks.NETHER_WART;
            if (seeds.isOf(Items.COCOA_BEANS)) return Blocks.COCOA;
            return null;
        }

        private BlockPos findJungleLogNearby(BlockPos pos) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos p = pos.offset(dir);
                if (golem.getEntityWorld().getBlockState(p).isIn(BlockTags.JUNGLE_LOGS)) {
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() != GolemType.BAMBOO) return false;
            if (hasWaterBucket()) return false;
            if (!hasEmptyBucket()) return false;
            
            waterSource = findNearbyWater();
            return waterSource != null;
        }

        private boolean hasWaterBucket() {
            if (golem.getHeldItem().isOf(Items.WATER_BUCKET)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return true;
            }
            return false;
        }

        private boolean hasEmptyBucket() {
            if (golem.getHeldItem().isOf(Items.BUCKET)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.BUCKET)) return true;
            }
            return false;
        }

        private BlockPos findNearbyWater() {
            BlockPos pos = golem.getBlockPos();
            for (int x = -16; x <= 16; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -16; z <= 16; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (golem.getEntityWorld().getBlockState(p).isOf(Blocks.WATER)) {
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
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(waterSource.getX(), waterSource.getY(), waterSource.getZ(), 1.0);
                }
                golem.getLookControl().lookAt(waterSource.getX() + 0.5, waterSource.getY() + 0.5, waterSource.getZ() + 0.5, 30.0F, 30.0F);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(waterSource.getX() + 0.5, waterSource.getY() + 0.5, waterSource.getZ() + 0.5, 30.0F, 30.0F);
                
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
                        golem.setHeldItem(new ItemStack(Items.WATER_BUCKET));
                    } else {
                        SimpleInventory inv = golem.getInventory();
                        for (int i = 0; i < inv.size(); i++) {
                            if (inv.getStack(i) == emptyBucket) {
                                inv.setStack(i, new ItemStack(Items.WATER_BUCKET));
                                break;
                            }
                        }
                    }
                } else {
                    emptyBucket.decrement(1);
                    ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                    ItemStack remaining = golem.getInventory().addStack(waterBucket);
                    if (!remaining.isEmpty()) {
                        Block.dropStack(golem.getEntityWorld(), golem.getBlockPos(), remaining);
                    }
                }
                golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                waterSource = null;
            }
        }

        private ItemStack getEmptyBucket() {
            if (golem.getHeldItem().isOf(Items.BUCKET)) return golem.getHeldItem();
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.BUCKET)) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }
    }
    public static class TriggerRedstoneGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos componentPosition;
        private int delay;

        public TriggerRedstoneGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            componentPosition = findRedstoneComponents();
            return componentPosition != null;
        }

        @Override
        public void start() {
            delay = 0;
        }

        private BlockPos findRedstoneComponents() {
            BlockPos pos = golem.getBlockPos();
            BlockPos chestPos = golem.getChestPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (block instanceof ButtonBlock ||
                            block instanceof LeverBlock ||
                            block instanceof PressurePlateBlock) {
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                return p;
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void tick() {
            if (componentPosition == null) return;

            double dx = golem.getX() - (componentPosition.getX() + 0.5);
            double dy = golem.getY() - (componentPosition.getY() + 0.5);
            double dz = golem.getZ() - (componentPosition.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().startMovingTo(componentPosition.getX(), golem.getY(), componentPosition.getZ(), 1.2D);
                    } else {
                        golem.getNavigation().startMovingTo(componentPosition.getX(), componentPosition.getY(), componentPosition.getZ(), 1.2D);
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(componentPosition.getX() + 0.5, componentPosition.getY() + 0.5, componentPosition.getZ() + 0.5);

                if (delay > 0) {
                    delay--;
                    if (delay == 0) {
                        if (golem.getGolemType() == GolemType.REDSTONE) {
                            stop();
                        }
                    }
                    return;
                }

                if (golem.getRandom().nextInt(10) == 0) {
                    interactWithComponent();
                    delay = 20;
                }
            }
        }

        public void interactWithComponent() {
            if (componentPosition == null) return;
            BlockState state = golem.getEntityWorld().getBlockState(componentPosition);
            Block block = state.getBlock();
            if (block instanceof ButtonBlock button) {
                golem.setAnimation(GolemAnimation.PRESSING_BUTTON, 20);
                // Alternative: manually set the state if onUse requires a player
                golem.getEntityWorld().setBlockState(componentPosition, state.with(ButtonBlock.POWERED, true));
                golem.getEntityWorld().scheduleBlockTick(componentPosition, block, 20);
            } else if (block instanceof LeverBlock lever) {
                golem.setAnimation(GolemAnimation.PRESSING_BUTTON, 20);
                golem.getEntityWorld().setBlockState(componentPosition, state.cycle(LeverBlock.POWERED));
            } else if (block instanceof PressurePlateBlock) {
                // Golem standing on it already triggers it usually, but we can move him there
                golem.getNavigation().startMovingTo(componentPosition.getX() + 0.5, componentPosition.getY(), componentPosition.getZ() + 0.5, 1.2D);
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() != GolemType.REDSTONE) return false;
            SimpleInventory inv = golem.getInventory();
            boolean hasRedstone = false;
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.REDSTONE)) {
                    hasRedstone = true;
                    break;
                }
            }
            if (!hasRedstone) return false;

            findComponentsToConnect();
            return startPos != null && endPos != null;
        }

        private void findComponentsToConnect() {
            BlockPos pos = golem.getBlockPos();
            int range = 16;
            List<BlockPos> components = new ArrayList<>();
            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
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
                        if (p1.getSquaredDistance(p2) > 4 && p1.getSquaredDistance(p2) < 256) {
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
            return b instanceof ButtonBlock || b instanceof LeverBlock || b instanceof PressurePlateBlock || b == Blocks.TNT || b == Blocks.REDSTONE_LAMP || b instanceof net.minecraft.block.DoorBlock;
        }

        private boolean areConnected(BlockPos p1, BlockPos p2) {
            // Very basic check: is there redstone dust near p1 that leads towards p2?
            // For now, let's just assume they are not connected if there is no dust immediately adjacent
            for (Direction dir : Direction.values()) {
                if (golem.getEntityWorld().getBlockState(p1.offset(dir)).isOf(Blocks.REDSTONE_WIRE)) return true;
                if (golem.getEntityWorld().getBlockState(p2.offset(dir)).isOf(Blocks.REDSTONE_WIRE)) return true;
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
                if (golem.getEntityWorld().getBlockState(target).isReplaceable()) {
                    p.add(target);
                } else if (golem.getEntityWorld().getBlockState(target.up()).isReplaceable()) {
                    currY++;
                    p.add(target.up());
                } else if (golem.getEntityWorld().getBlockState(target.down()).isReplaceable()) {
                    currY--;
                    p.add(target.down());
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
            double distSq = golem.getBlockPos().getSquaredDistance(target);

            if (distSq > 4) {
                golem.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), 1.2D);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
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
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.REDSTONE)) {
                    if (golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                        golem.getEntityWorld().setBlockState(pos, Blocks.REDSTONE_WIRE.getDefaultState());
                        stack.decrement(1);
                        golem.getEntityWorld().playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }

        private void placeRepeater(BlockPos pos) {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.REPEATER)) {
                    if (golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                        // Direction should be facing towards endPos
                        Direction facing = Direction.fromHorizontalDegrees(golem.getYaw());
                        golem.getEntityWorld().setBlockState(pos, Blocks.REPEATER.getDefaultState().with(HorizontalFacingBlock.FACING, facing));
                        stack.decrement(1);
                        golem.getEntityWorld().playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }
    }
    public static class BreedAnimalsGoal extends Goal {
        private UtilityGolem golem;
        private AnimalEntity animalA;
        private AnimalEntity animalB;
        private int delay;
        private long lastBreedTime;

        public BreedAnimalsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getEntityWorld().getTime() < lastBreedTime + 6000) return false;
            if (!hasBreedingItem()) return false;

            BlockPos chestPos = golem.getChestPos();
            List<AnimalEntity> animals = golem.getEntityWorld()
                    .getEntitiesByClass(
                            AnimalEntity.class,
                            golem.getBoundingBox().expand(8.0),
                            a -> a.isAlive() && a.isInLove() == false
                    );

            for (int i = 0; i < animals.size(); i++) {
                for (int j = i + 1; j < animals.size(); j++) {
                    AnimalEntity a = animals.get(i);
                    AnimalEntity b = animals.get(j);

                    if (a.getClass() == b.getClass()
                            && a.canEat() && a.age > 0
                            && b.canEat() && b.age > 0) {
                        
                        // Only breed if within 32 blocks of chest (if chest is known)
                        if (chestPos != null) {
                            if (a.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 1024) continue;
                            if (b.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 1024) continue;
                        }

                        animalA = a;
                        animalB = b;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return animalA != null && animalB != null
                    && animalA.isAlive()
                    && animalB.isAlive()
                    && !animalA.isInLove()
                    && !animalB.isInLove()
                    && animalA.age > 0
                    && animalB.age > 0;
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

            Vec3d animalAPos = Vec3d.of(animalA.getBlockPos());
            Vec3d animalBPos = Vec3d.of(animalB.getBlockPos());
            Vec3d center = animalAPos.add(animalBPos).multiply(0.5);
            if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                golem.getNavigation().startMovingTo(center.x, center.y, center.z, 1.1D);
            }
            golem.getLookControl().lookAt(center.x, center.y, center.z);

            if (++delay >= 40) {
                breed();
                stop();
            }
        }

        private void breed() {
            lastBreedTime = golem.getEntityWorld().getTime();
            ItemStack food = getBreedingItem();
            if (food.isEmpty()) return;

            golem.equipStack(EquipmentSlot.MAINHAND, food);

            // Consume 1 item
            food.decrement(1);

            // Trigger vanilla breeding
            animalA.lovePlayer(null);
            animalB.lovePlayer(null);
        }

        private boolean hasBreedingItem() {
            return !getBreedingItem().isEmpty();
        }

        private ItemStack getBreedingItem() {
            SimpleInventory inv = golem.getInventory();

            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && isValidBreedingItem(stack)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private boolean isValidBreedingItem(ItemStack stack) {
            return stack.isOf(Items.WHEAT)
                    || stack.isOf(Items.CARROT)
                    || stack.isOf(Items.POTATO)
                    || stack.isOf(Items.BEETROOT)
                    || stack.isOf(Items.WHEAT_SEEDS);
        }
    }
    public static class ChopTreeGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos targetPos;
        private BlockPos currentTreePos;
        private int breakingTime;
        private int maxBreakingTime;
        private int stuckTicks;
        private Vec3d lastPos;
        private final java.util.Map<BlockPos, Long> failedBlocks = new java.util.HashMap<>();

        public ChopTreeGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            ItemStack tool = golem.getHeldItem();
            if (tool.isEmpty() || (!UtilityGolem.isAxe(tool))) {
                return false;
            }
            if (isInventoryFull()) {
                return false;
            }

            // Cleanup failed blocks older than 30 seconds
            long now = golem.getEntityWorld().getTime();
            failedBlocks.entrySet().removeIf(entry -> now - entry.getValue() > 600);

            // If it's a deepslate golem, only chop trees if we have an axe AND (we need saplings OR no other tasks are high priority)

            if (golem.getGolemType() == GolemType.DEEPSLATE && hasEnoughSaplings()) {

                if (!findNearbySaplingsOnGround()) {
                    return false; 
                }
            }
            
            targetPos = findTargetBlock();
            if (targetPos != null) {
                this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                return true;
            }
            return false;
        }

        private boolean hasEnoughSaplings() {
            int count = 0;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                    count += inv.getStack(i).getCount();
                }
            }
            return count >= 8;
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
            }
            return true;
        }

        private boolean findNearbySaplingsOnGround() {
            List<net.minecraft.entity.ItemEntity> items = golem.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    golem.getBoundingBox().expand(16.0, 4.0, 16.0),
                    item -> item.getStack().isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)
            );
            return !items.isEmpty();
        }

        private int calculateBreakingTime(ItemStack tool, BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            float hardness = state.getHardness(golem.getEntityWorld(), pos);
            if (hardness < 0) return 200;

            float speed = 1.0f;
            if (UtilityGolem.isShears(tool)) {
                if (state.isIn(BlockTags.LEAVES)) {
                    speed = 15.0f;
                } else {
                    speed = 1.0f;
                }
            } else if (UtilityGolem.isAxe(tool)) {
                if (tool.isOf(Items.GOLDEN_AXE)) speed = 12.0f;
                else if (tool.isOf(Items.NETHERITE_AXE)) speed = 9.0f;
                else if (tool.isOf(Items.DIAMOND_AXE)) speed = 8.0f;
                else if (tool.isOf(Items.IRON_AXE)) speed = 6.0f;
                else if (tool.isOf(Items.STONE_AXE)) speed = 4.0f;
                else if (tool.isOf(Items.WOODEN_AXE)) speed = 2.0f;
            }

            return Math.max(1, (int) (hardness * 30 / speed));
        }

        private BlockPos findTargetBlock() {
            BlockPos chestPos = golem.getChestPos();
            
            // Priority 1: Find connected log/leaf to current tree
            if (currentTreePos != null) {
                // If the current tree is way too far from the golem (e.g. golem was teleported),
                // or too far from the chest, reset it.
                if (currentTreePos.getSquaredDistance(golem.getX(), golem.getY(), golem.getZ()) > 1024 ||
                    (chestPos != null && currentTreePos.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 1024)) {
                    currentTreePos = null;
                } else {
                    BlockPos connected = findConnectedTarget(currentTreePos);
                    if (connected != null) {
                        return connected;
                    }
                    currentTreePos = null; // No more connected blocks found for this tree
                }
            }

            BlockPos pos = golem.getBlockPos();
            BlockPos closest = null;
            double minDistSq = Double.MAX_VALUE;
            int range = 15;

            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (canChop(p) && !failedBlocks.containsKey(p) && !golem.isBlacklisted(p)) {
                            // Only chop if within 32 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                double distSq = p.getSquaredDistance(golem.getX(), golem.getY(), golem.getZ());
                                // Heavy bias towards logs to prioritize them over leaves
                                double score = distSq + (isLog(p) ? 0 : 1000.0);
                                if (score < minDistSq) {
                                    minDistSq = score;
                                    closest = p;
                                }
                            }
                        }
                    }
                }
            }
            
            if (closest != null) {
                currentTreePos = closest;
            }
            return closest;
        }

        private BlockPos findConnectedTarget(BlockPos startPos) {
            BlockPos chestPos = golem.getChestPos();
            int searchRange = 3; // Slightly larger range to catch branches
            BlockPos closest = null;
            double minDistSq = Double.MAX_VALUE;

            for (int x = -searchRange; x <= searchRange; x++) {
                for (int y = -searchRange; y <= searchRange; y++) {
                    for (int z = -searchRange; z <= searchRange; z++) {
                        BlockPos p = startPos.add(x, y, z);
                        if (canChop(p) && !failedBlocks.containsKey(p) && !golem.isBlacklisted(p)) {
                            // Only chop if within 32 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
                                double distSq = p.getSquaredDistance(golem.getX(), golem.getY(), golem.getZ());
                                // Heavy bias towards logs to prioritize them over leaves
                                double score = distSq + (isLog(p) ? 0 : 1000.0);
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
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            if (state.isIn(BlockTags.LOGS)) return true;
            if (state.isIn(BlockTags.LEAVES)) {
                // If using an axe, only break leaves if we don't have shears in inventory
                // AND it's a "necessary" leaf (in this case, we'll allow it if there are no logs left)
                // But the scoring already prioritizes logs.
                // To truly only break "necessary" leaves with an axe, we can restrict it further.
                ItemStack tool = golem.getHeldItem();
                if (UtilityGolem.isAxe(tool)) {
                    // Check if we have shears in inventory that we COULD use
                    if (hasShearsInInventory()) return true; // swapTool will handle switching to shears
                    
                    // If we ONLY have an axe, maybe we should only break leaves if they are blocking?
                    // For now, let's just say if we have an axe and NO shears, we don't break leaves
                    // unless we are already "in" a tree and no logs are nearby.
                    // This matches "only break necessary leaves when using axe"
                    return false; 
                }
                return true; // Shears can always break leaves
            }
            return false;
        }

        private boolean hasShearsInInventory() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (UtilityGolem.isShears(inv.getStack(i))) return true;
            }
            return false;
        }

        private boolean isLog(BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            return state.isIn(BlockTags.LOGS);
        }

        @Override
        public void start() {
            breakingTime = 0;
            stuckTicks = 0;
            lastPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
            golem.setAnimation(GolemAnimation.CHOPPING, Math.min(100, Math.max(40, this.maxBreakingTime)));
        }

        @Override
        public boolean shouldContinue() {
            ItemStack tool = golem.getHeldItem();
            return targetPos != null && canChop(targetPos) && !tool.isEmpty() && (UtilityGolem.isAxe(tool) || UtilityGolem.isShears(tool)) &&
                    breakingTime < maxBreakingTime && golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 400 &&
                    stuckTicks < 100;
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, -1);
                if (stuckTicks >= 100) {
                    failedBlocks.put(targetPos, golem.getEntityWorld().getTime());
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
            Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
            if (lastPos != null && currentPos.squaredDistanceTo(lastPos) < 0.01 * 0.01) {
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

            // If it's too far horizontally or too far vertically
            if (horizontalDistSq > 16.0D || verticalDist > 15.0D) {
                if (stuckTicks > 100) {
                    golem.blacklistPosition(targetPos);
                    targetPos = null;
                    return;
                }
                // If it's high up, move to the base of it
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    boolean possible;
                    // If it's high up, move to the base of it
                    if (verticalDist > 2.0D) {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        possible = golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    }
                    if (!possible) {
                        golem.blacklistPosition(targetPos);
                        targetPos = null;
                        return;
                    }
                }
                breakingTime = 0;
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

                if (breakingTime % 5 == 0) {
                    golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }

                breakingTime++;
                int progress = (int) ((float) breakingTime / (float) maxBreakingTime * 10.0F);
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, progress);

                if (breakingTime >= maxBreakingTime) {
                    breakBlock();
                }
            }
        }

        private void swapTool(java.util.function.Predicate<ItemStack> toolPredicate) {
            SimpleInventory inv = golem.getInventory();
            ItemStack currentHeld = golem.getHeldItem();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (toolPredicate.test(stack)) {
                    ItemStack newTool = inv.removeStack(i, 1);
                    if (!currentHeld.isEmpty()) {
                        ItemStack remaining = inv.addStack(currentHeld);
                        if (!remaining.isEmpty()) {
                            golem.getEntityWorld().spawnEntity(new net.minecraft.entity.ItemEntity(golem.getEntityWorld(), golem.getX(), golem.getY(), golem.getZ(), remaining));
                        }
                    }
                    golem.setHeldItem(newTool);
                    // Recalculate maxBreakingTime for the new tool
                    this.maxBreakingTime = calculateBreakingTime(newTool, targetPos);
                    break;
                }
            }
        }

        private void breakBlock() {
            if (!(golem.getEntityWorld() instanceof ServerWorld serverWorld)) return;

            BlockState state = serverWorld.getBlockState(targetPos);
            if (canChop(targetPos)) {
                ItemStack tool = golem.getHeldItem();
                
                LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                        .add(LootContextParameters.TOOL, tool)
                        .addOptional(LootContextParameters.THIS_ENTITY, golem);

                serverWorld.breakBlock(targetPos, false, golem);

                List<ItemStack> drops = state.getDroppedStacks(builder);
                for (ItemStack drop : drops) {
                    ItemStack remaining = golem.getInventory().addStack(drop);
                    if (!remaining.isEmpty()) {
                        Block.dropStack(serverWorld, targetPos, remaining);
                    }
                }

                if (!tool.isEmpty()) {
                    if (UtilityGolem.isAxe(tool) || UtilityGolem.isShears(tool)) {
                        tool.damage(1, serverWorld, null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
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
        private Vec3d lastPos;
        private final java.util.Map<BlockPos, Long> failedPositions = new java.util.HashMap<>();

        public ReplantSaplingGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (getSaplingFromInventory().isEmpty()) {
                return false;
            }

            // Cleanup failed positions older than 30 seconds
            long now = golem.getEntityWorld().getTime();
            failedPositions.entrySet().removeIf(entry -> now - entry.getValue() > 600);

            targetPos = findPlantingPos();
            return targetPos != null;
        }

        private ItemStack getSaplingFromInventory() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private BlockPos findPlantingPos() {
            BlockPos pos = golem.getBlockPos();
            BlockPos chestPos = golem.getChestPos();
            int range = 8;
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    for (int y = -2; y <= 2; y++) {
                        BlockPos p = pos.add(x, y, z);
                        if (canPlantAt(p) && !failedPositions.containsKey(p)) {
                            // Sparse pattern check: No other saplings within 3 blocks
                            if (isSparse(p)) {
                                // Only plant if within 32 blocks of chest (if chest is known)
                                if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024) {
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
            net.minecraft.world.World world = golem.getEntityWorld();
            BlockState state = world.getBlockState(pos);
            BlockState floor = world.getBlockState(pos.down());
            return state.isAir() && (floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.GRASS_BLOCK));
        }

        private boolean isSparse(BlockPos pos) {
            net.minecraft.world.World world = golem.getEntityWorld();
            int sparseRange = 3;
            for (int x = -sparseRange; x <= sparseRange; x++) {
                for (int z = -sparseRange; z <= sparseRange; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos p = pos.add(x, 0, z);
                    BlockState s = world.getBlockState(p);
                    // Check if it's a sapling or a log (tree already there)
                    if (s.isIn(BlockTags.SAPLINGS) || s.isIn(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public void start() {
            stuckTicks = 0;
            lastPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
        }

        @Override
        public void stop() {
            if (stuckTicks >= 100 && targetPos != null) {
                failedPositions.put(targetPos, golem.getEntityWorld().getTime());
            }
            targetPos = null;
        }

        @Override
        public boolean shouldContinue() {
            return targetPos != null && canPlantAt(targetPos) && !getSaplingFromInventory().isEmpty() && stuckTicks < 100;
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Stuck detection
            Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
            if (lastPos != null && currentPos.squaredDistanceTo(lastPos) < 0.01 * 0.01) {
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
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                ItemStack saplingStack = getSaplingFromInventory();
                if (!saplingStack.isEmpty()) {
                    Block saplingBlock = Block.getBlockFromItem(saplingStack.getItem());
                    if (saplingBlock != Blocks.AIR) {
                        golem.getEntityWorld().setBlockState(targetPos, saplingBlock.getDefaultState());
                        saplingStack.decrement(1);
                        golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        targetPos = null;
                    }
                }
            }
        }
    }
    public static class TradeWithPiglinGoal extends Goal {
        private final UtilityGolem golem;
        private net.minecraft.entity.mob.PiglinEntity targetPiglin;
        private int tradeDelay;
        private boolean waitingForPiglin;
        private net.minecraft.entity.ItemEntity suspectedTradedItem;

        public TradeWithPiglinGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() != GolemType.GOLD) return false;
            // Only start if we have gold and aren't holding something that's NOT gold (unless it's empty)
            if (!hasGoldIngot()) return false;
            
            // If we are holding something that isn't gold, maybe we should finish depositing it first?
            // Actually, if we have gold in inventory but holding a trade result, we should probably finish that.
            if (!golem.getHeldItem().isEmpty() && !golem.getHeldItem().isOf(Items.GOLD_INGOT)) return false;

            targetPiglin = findNearbyPiglin();
            return targetPiglin != null;
        }

        private boolean hasGoldIngot() {
            if (golem.getHeldItem().isOf(Items.GOLD_INGOT)) return true;
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.GOLD_INGOT)) return true;
            }
            return false;
        }

        private net.minecraft.entity.mob.PiglinEntity findNearbyPiglin() {
            List<net.minecraft.entity.mob.PiglinEntity> piglins = golem.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.mob.PiglinEntity.class,
                    golem.getBoundingBox().expand(16.0),
                    piglin -> piglin.isAlive() && !piglin.isBaby()
            );
            return piglins.stream()
                    .min(Comparator.comparingDouble(golem::squaredDistanceTo))
                    .orElse(null);
        }

        @Override
        public boolean shouldContinue() {
            if (targetPiglin == null || !targetPiglin.isAlive() || golem.squaredDistanceTo(targetPiglin) > 256) return false;
            
            if (waitingForPiglin) {
                // If we are waiting, we don't necessarily need gold ingot in inventory right now (we just dropped it)
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

            golem.getLookControl().lookAt(targetPiglin, 30.0F, 30.0F);
            double distSq = golem.squaredDistanceTo(targetPiglin);

            if (distSq > 4.0D) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPiglin, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                
                if (waitingForPiglin) {
                    // Check if piglin dropped something
                    if (suspectedTradedItem == null || !suspectedTradedItem.isAlive()) {
                        suspectedTradedItem = findNearbyDroppedItem();
                    }

                    if (suspectedTradedItem != null && suspectedTradedItem.isAlive()) {
                        if (golem.squaredDistanceTo(suspectedTradedItem) < 2.0D) {
                            pickupTradedItem(suspectedTradedItem);
                            waitingForPiglin = false;
                            suspectedTradedItem = null;
                            tradeDelay = 0;
                        } else {
                            // Move to item
                            golem.getNavigation().startMovingTo(suspectedTradedItem, 1.2D);
                        }
                    } else {
                        // Still waiting for piglin to finish admiring and drop
                        if (++tradeDelay > 200) { // Timeout after 10 seconds
                            waitingForPiglin = false;
                            tradeDelay = 0;
                        }
                    }
                    return;
                }

                // Ensure golem is holding gold ingot before trading
                if (!golem.getHeldItem().isOf(Items.GOLD_INGOT)) {
                    SimpleInventory inv = golem.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        ItemStack stack = inv.getStack(i);
                        if (stack.isOf(Items.GOLD_INGOT)) {
                            ItemStack held = golem.getHeldItem();
                            // If we were holding something else, try to put it in inventory
                            if (!held.isEmpty()) {
                                ItemStack remaining = inv.addStack(held);
                                if (!remaining.isEmpty()) {
                                    // No space? Just drop it
                                    Block.dropStack(golem.getEntityWorld(), golem.getBlockPos(), remaining);
                                }
                            }
                            ItemStack toHold = stack.copy();
                            toHold.setCount(1);
                            golem.setHeldItem(toHold);
                            stack.decrement(1);
                            break;
                        }
                    }
                }

                if (++tradeDelay % 10 == 0) {
                    if (isPiglinReady(targetPiglin) && golem.getHeldItem().isOf(Items.GOLD_INGOT)) {
                        dropGoldIngot();
                        waitingForPiglin = true;
                        tradeDelay = 0;
                    }
                }
            }
        }

        private net.minecraft.entity.ItemEntity findNearbyDroppedItem() {
            List<net.minecraft.entity.ItemEntity> items = golem.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    golem.getBoundingBox().expand(4.0),
                    item -> !item.cannotPickup() && !item.getStack().isOf(Items.GOLD_INGOT)
            );
            return items.stream()
                    .min(Comparator.comparingDouble(golem::squaredDistanceTo))
                    .orElse(null);
        }

        private void pickupTradedItem(net.minecraft.entity.ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            golem.setHeldItem(stack.copy());
            itemEntity.discard();
            golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }

        private boolean isPiglinReady(net.minecraft.entity.mob.PiglinEntity piglin) {
            // Piglin is ready if it's not currently admiring a gold ingot or holding one to admire.
            // When admiring, they hold the item in their main hand and have a specific NBT or AI state.
            // However, simply checking if they are holding gold is a good start.
            ItemStack mainHand = piglin.getMainHandStack();
            ItemStack offHand = piglin.getOffHandStack();
            return !mainHand.isOf(Items.GOLD_INGOT) && !offHand.isOf(Items.GOLD_INGOT);
        }

        private void dropGoldIngot() {
            ItemStack goldIngot = golem.getHeldItem();

            if (goldIngot.isOf(Items.GOLD_INGOT)) {
                ItemStack toDrop = goldIngot.copy();
                toDrop.setCount(1);
                
                goldIngot.decrement(1);
                if (goldIngot.isEmpty()) {
                    golem.setHeldItem(ItemStack.EMPTY);
                }
                
                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                        golem.getEntityWorld(),
                        golem.getX(), golem.getY() + 1.0, golem.getZ(),
                        toDrop
                );
                itemEntity.setPickupDelay(20);
                Vec3d targetPos = new Vec3d(targetPiglin.getX(), targetPiglin.getY(), targetPiglin.getZ());
                Vec3d golemPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
                Vec3d velocity = targetPos.subtract(golemPos).normalize().multiply(0.3);
                itemEntity.setVelocity(velocity);
                golem.getEntityWorld().spawnEntity(itemEntity);
                golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
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
        public boolean canStart() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            return hasEmeraldBlocks();
        }

        private boolean hasEmeraldBlocks() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.EMERALD_BLOCK)) {
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
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.EMERALD_BLOCK)) {
                    int count = stack.getCount();
                    stack.decrement(count);
                    
                    ItemStack emeralds = new ItemStack(Items.EMERALD, count * 9);
                    ItemStack remaining = inv.addStack(emeralds);
                    
                    if (!remaining.isEmpty()) {
                        net.minecraft.block.Block.dropStack(golem.getEntityWorld(), golem.getBlockPos(), remaining);
                    }
                    
                    golem.debugLog("CraftEmeraldsGoal: Crafted " + (count * 9) + " emeralds from " + count + " blocks.");
                    golem.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

    public static class PickupItemGoal extends Goal {
        private final UtilityGolem golem;
        private net.minecraft.entity.ItemEntity targetItem;
        private int cooldown;
        private int stuckTicks;
        private Vec3d lastPos;

        public PickupItemGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (isInventoryFull()) {
                cooldown = 40;
                return false;
            }
            targetItem = findNearbyItem();
            if (targetItem == null) {
                cooldown = 20;
            } else {
                golem.debugLog("PickupItemGoal: Found item " + targetItem.getStack().getItem().getName().getString() + " at " + targetItem.getBlockPos().toShortString());
            }
            return targetItem != null;
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
            }
            return true;
        }

        private net.minecraft.entity.ItemEntity findNearbyItem() {
            BlockPos chestPos = golem.getChestPos();
            List<net.minecraft.entity.ItemEntity> items = golem.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    golem.getBoundingBox().expand(16.0, 4.0, 16.0),
                    item -> {
                        if (item.cannotPickup()) return false;
                        ItemStack stack = item.getStack();
                        
                        boolean isFamiliar = false;
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            boolean isSapling = stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS);
                            boolean isApple = stack.isOf(Items.APPLE);
                            boolean isStick = stack.isOf(Items.STICK);
                            isFamiliar = isSapling || isApple || isStick;
                        } else if (golem.getGolemType() == GolemType.BAMBOO) {
                            boolean isCrop = stack.isOf(Items.WHEAT) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT) ||
                                            stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS) || stack.isOf(Items.PUMPKIN) || stack.isOf(Items.MELON);
                            boolean isSeed = stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.BEETROOT_SEEDS) || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS);
                            isFamiliar = isCrop || isSeed;
                        } else if (golem.getGolemType() == GolemType.DIAMOND) {
                            // User requested only blocks. Some "seeds" (like Wheat Seeds) are BlockItems but 
                            // user might not want them. For now, let's keep it to BlockItem but exclude common "seed/non-blocky" items if they cause confusion.
                            // But usually, Diamond Golem is for building.
                            isFamiliar = stack.getItem() instanceof net.minecraft.item.BlockItem && !stack.isOf(Items.WHEAT_SEEDS) && !stack.isOf(Items.BEETROOT_SEEDS) && !stack.isOf(Items.PUMPKIN_SEEDS) && !stack.isOf(Items.MELON_SEEDS);
                        } else if (golem.getGolemType() == GolemType.SPONGE) {
                            isFamiliar = stack.isOf(Items.COD) || stack.isOf(Items.SALMON) || stack.isOf(Items.PUFFERFISH) || stack.isOf(Items.TROPICAL_FISH) ||
                                            stack.isOf(Items.ENCHANTED_BOOK) || stack.isOf(Items.NAME_TAG) || stack.isOf(Items.SADDLE) || stack.isOf(Items.BOW) || stack.isOf(Items.FISHING_ROD);
                        } else if (golem.getGolemType() == GolemType.JUKEBOX) {
                            isFamiliar = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null;
                        } else if (golem.getGolemType() == GolemType.GOLD) {
                            // Gold golem wants to pick up gold AND anything else (traded items)
                            isFamiliar = true; 
                        } else if (golem.getGolemType() == GolemType.LAMP) {
                            isFamiliar = UtilityGolem.isTorch(stack);
                        } else if (golem.getGolemType() == GolemType.EMERALD) {
                            boolean isEmerald = stack.isOf(Items.EMERALD) || stack.isOf(Items.EMERALD_BLOCK);
                            boolean isOnSellingList = golem.getDiscoveredTrades().stream().anyMatch(tradeStack -> ItemStack.areItemsEqual(tradeStack, stack));
                            isFamiliar = isEmerald || isOnSellingList;
                        } else if (golem.getGolemType() == GolemType.NETHER_WART) {
                            boolean isIngredient = isIngredient(stack);
                            boolean isSupply = stack.isOf(Items.GLASS_BOTTLE) || stack.isOf(Items.BLAZE_POWDER) || stack.isOf(Items.BREWING_STAND);
                            boolean isPotionOrWater = BrewingGoal.isWaterBottleStatic(stack) || stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
                            isFamiliar = isIngredient || isSupply || isPotionOrWater;
                        } else {
                            // Default: only pick up blocks to avoid cluttering inventory with junk
                            isFamiliar = stack.getItem() instanceof net.minecraft.item.BlockItem && !stack.isOf(Items.WHEAT_SEEDS) && !stack.isOf(Items.BEETROOT_SEEDS) && !stack.isOf(Items.PUMPKIN_SEEDS) && !stack.isOf(Items.MELON_SEEDS);
                        }
                        
                        if (!isFamiliar) return false;
                        
                        // Only pickup if within 32 blocks of chest (if chest is known)
                        if (chestPos != null) {
                            return item.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 1024;
                        }
                        return true;
                    }
            );

            return items.stream()
                    .min(Comparator.comparingDouble(golem::squaredDistanceTo))
                    .orElse(null);
        }

        @Override
        public void start() {
            stuckTicks = 0;
            lastPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
        }

        @Override
        public boolean shouldContinue() {
            return targetItem != null && targetItem.isAlive() && !isInventoryFull() &&
                    golem.squaredDistanceTo(targetItem) < 256 && stuckTicks < 60;
        }

        @Override
        public void stop() {
            if (stuckTicks >= 60 && targetItem != null) {
                // Add to a temporary blacklist for items?
                // PickupItemGoal already has a cooldown that triggers on stop if stuck.
                // But it might pick the same item again.
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
            Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
            if (currentPos.squaredDistanceTo(lastPos) < 0.01 * 0.01) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }
            lastPos = currentPos;

            if (golem.squaredDistanceTo(targetItem) > 1.5D) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetItem, 1.2D);
                }
                golem.getLookControl().lookAt(targetItem, 30.0F, 30.0F);
            } else {
                pickup();
            }
        }

        private void pickup() {
            ItemStack stack = targetItem.getStack();
            ItemStack remaining = golem.getInventory().addStack(stack);
            if (remaining.isEmpty()) {
                targetItem.discard();
            } else {
                targetItem.setStack(remaining);
            }
            golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            targetItem = null;
        }
    }
    public static class PlayRecordGoal extends Goal {
        private final UtilityGolem golem;

        public PlayRecordGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.noneOf(Control.class));
        }

        @Override
        public boolean canStart() {
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
        public boolean shouldContinue() {
            return isRecord();
        }

        public boolean isRecord() {
            return golem.getHeldItem().get(DataComponentTypes.JUKEBOX_PLAYABLE) != null;
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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
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
                // Replace mode needs blocks in inventory to work
                return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
            }

            if (golem.getBuildPattern() == BuildPattern.TOWER) {
                // Tower mode needs blocks; ladders no longer required
                return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
            }

            return findBlockInInventory() != -1 && patternProgress < getMaxProgress();
        }

        private boolean hasLadders() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                if (golem.getInventory().getStack(i).isOf(Items.LADDER)) return true;
            }
            return false;
        }

        private int findBlockInInventory() {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void start() {
            startPos = golem.getBlockPos();
            startFacing = golem.getHorizontalFacing();
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
                        schematicOrigin = startPos.offset(startFacing, 2);
                        golem.debugLog("Schematic loaded. Size: " + schematic.width + "x" + schematic.height + "x" + schematic.length + " | Origin: " + schematicOrigin.toShortString());
                        // Count non-air blocks
                        int total = schematic.getTotalBlocks();
                        int count = 0;
                        for (int i = 0; i < total; i++) {
                            net.minecraft.block.Block b = schematic.getBlockAtIndex(i);
                            if (b != null && b != net.minecraft.block.Blocks.AIR) count++;
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
        public boolean shouldContinue() {
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

            double distSq = golem.squaredDistanceTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            if (distSq > 4.0) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.debugLog("Moving to target: " + targetPos.toShortString() + " | dist: " + Math.sqrt(distSq));
                    golem.getNavigation().startMovingTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
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
            Direction facing = golem.getHorizontalFacing();
            switch (golem.getBuildPattern()) {
                case PLATFORM:
                    Direction side = facing.rotateYClockwise();
                    int width = golem.getWallWidth();
                    int length = golem.getWallLength();
                    if (width <= 0 || length <= 0) return null;
                    int dx = patternProgress % width;
                    int dz = patternProgress / width;
                    return startPos.offset(facing, 2 + dz).offset(side, dx - width/2);
                case REPLACE:
                    int rx = (patternProgress % 5) - 2;
                    int rz = (patternProgress / 5) - 2;
                    return startPos.add(rx, 0, rz);
                case TOWER:
                    // Build directly under the golem
                    return golem.getBlockPos().down();
                case SCHEMATIC:
                    if (schematic == null || schematicOrigin == null) return null;
                    // Iterate indices until find next non-air and not already correct block
                    int total = schematic.getTotalBlocks();
                    int seen = 0;
                    for (int i = 0; i < total; i++) {
                        net.minecraft.block.Block block = schematic.getBlockAtIndex(i);
                        if (block == null || block == net.minecraft.block.Blocks.AIR) continue;
                        // Map linear index to x,y,z
                        int sx = schematic.indexToX(i);
                        int sy = schematic.indexToY(i);
                        int sz = schematic.indexToZ(i);
                        BlockPos rel = translateByFacing(sx, sy, sz, facing);
                        BlockPos worldPos = schematicOrigin.add(rel.getX(), rel.getY(), rel.getZ());
                        // Check if already correct
                        if (golem.getEntityWorld().getBlockState(worldPos).isOf(block)) continue;
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
            if (filterStack.isEmpty() || !(filterStack.getItem() instanceof net.minecraft.item.BlockItem)) {
                patternProgress++; // Skip if no filter
                return;
            }
            
            Block filterBlock = ((net.minecraft.item.BlockItem) filterStack.getItem()).getBlock();
            BlockState currentState = golem.getEntityWorld().getBlockState(pos);
            
            if (currentState.isOf(filterBlock)) {
                int slot = findBlockInInventory();
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getStack(slot);
                    Block newBlock = ((net.minecraft.item.BlockItem) stack.getItem()).getBlock();
                    
                    if (newBlock != filterBlock) {
                        boolean success = golem.getEntityWorld().setBlockState(pos, newBlock.getDefaultState(), 3);
                        if (success) {
                            stack.decrement(1);
                            golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                            golem.getEntityWorld().playSound(null, pos, newBlock.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                            cooldown = 8; // 2.5 blocks per second
                        }
                    }
                }
            }
            patternProgress++;
        }

        private void placeTowerStep(BlockPos pos) {
            // Place a block directly beneath the golem and jump up
            if (golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                int blockSlot = findBlockInInventory();
                if (blockSlot != -1) {
                    ItemStack stack = golem.getInventory().getStack(blockSlot);
                    Block block = ((net.minecraft.item.BlockItem) stack.getItem()).getBlock();
                    if (golem.getEntityWorld().setBlockState(pos, block.getDefaultState(), 3)) {
                        stack.decrement(1);
                        golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        golem.getEntityWorld().playSound(null, pos, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                        // Apply an upward velocity to simulate jumping
                        golem.setVelocity(golem.getVelocity().x, 0.5, golem.getVelocity().z);
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
            if (!golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                 // There's already a block here. If we are still below where we need to be to place the NEXT one,
                 // we might need to jump.
                 if (golem.getY() < pos.getY() + 1.5) {
                     golem.setVelocity(golem.getVelocity().x, 0.3, golem.getVelocity().z);
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
            Direction facing = golem.getHorizontalFacing();
            // Find index that maps to this pos; iterate through as in getTargetPos to keep order
            int total = schematic.getTotalBlocks();
            int seen = 0;
            net.minecraft.block.Block needed = null;
            for (int i = 0; i < total; i++) {
                net.minecraft.block.Block block = schematic.getBlockAtIndex(i);
                if (block == null || block == net.minecraft.block.Blocks.AIR) continue;
                int sx = schematic.indexToX(i);
                int sy = schematic.indexToY(i);
                int sz = schematic.indexToZ(i);
                BlockPos rel = translateByFacing(sx, sy, sz, facing);
                BlockPos worldPos = schematicOrigin.add(rel.getX(), rel.getY(), rel.getZ());
                if (golem.getEntityWorld().getBlockState(worldPos).isOf(block)) continue;
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

            if (golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                int slot = findBlockInInventory(needed);
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getStack(slot);
                    Block block = ((net.minecraft.item.BlockItem) stack.getItem()).getBlock();
                    golem.debugLog("Placing " + block.getName().getString() + " at " + pos.toShortString());
                    boolean success = golem.getEntityWorld().setBlockState(pos, block.getDefaultState(), 3);
                    if (success) {
                        stack.decrement(1);
                        golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        golem.getEntityWorld().playSound(null, pos, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
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
                    golem.debugLog("Missing block in inventory: " + needed.getName().getString());
                }
            } else {
                golem.debugLog("Block at " + pos.toShortString() + " is not replaceable: " + golem.getEntityWorld().getBlockState(pos).getBlock().getName().getString());
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

        private int findBlockInInventory(net.minecraft.block.Block needed) {
            for (int i = 0; i < golem.getInventory().size(); i++) {
                ItemStack stack = golem.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                    if (blockItem.getBlock() == needed) return i;
                }
            }
            return -1;
        }

        private BlockPos translateByFacing(int sx, int sy, int sz, Direction facing) {
            // Origin at schematicOrigin; X goes to right (rotateYClockwise), Z goes forward (facing), Y is up
            Direction right = facing.rotateYClockwise();
            return schematicOrigin == null ? new BlockPos(0,0,0) : new BlockPos(
                    right.getOffsetX() * sx + facing.getOffsetX() * sz,
                    sy,
                    right.getOffsetZ() * sx + facing.getOffsetZ() * sz
            );
        }

        private void placeBlock(BlockPos pos) {
            if (golem.getEntityWorld().getBlockState(pos).isReplaceable()) {
                int slot = findBlockInInventory();
                if (slot != -1) {
                    ItemStack stack = golem.getInventory().getStack(slot);
                    Block block = ((net.minecraft.item.BlockItem) stack.getItem()).getBlock();
                    
                    // Use setBlockState with flags to ensure updates
                    boolean success = golem.getEntityWorld().setBlockState(pos, block.getDefaultState(), 3);
                    if (success) {
                        stack.decrement(1);
                        golem.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                        golem.getEntityWorld().playSound(null, pos, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                        
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
}
