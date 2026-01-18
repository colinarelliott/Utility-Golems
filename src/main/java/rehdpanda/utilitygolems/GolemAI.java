package rehdpanda.utilitygolems;

import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.ItemEntity;

import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.world.WorldAccess;

public class GolemAI {

    /// INITIALIZE EACH GOLEM WITH UNIQUE AI

    public static void initLapisGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.COPPER_PICKAXE
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new DigBlockGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F)));
    }

    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.REDSTONE), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new TriggerRedstoneGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
    }

    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.EMERALD), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new TradeWithVillagerGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new LookAtEntityGoal(golem, VillagerEntity.class, 8.0F)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.0D, 3.0F, 10.0F)));
    }

    public static void initGoldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.GOLD_INGOT, Items.GOLD_NUGGET), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new TradeWithPiglinGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
    }
    
    public static void initAmethystGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.WHEAT_SEEDS), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new BreedAnimalsGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
    }
    
    public static void initNetheriteGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD, Items.WOODEN_SWORD, Items.COPPER_SWORD
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new MeleeAttackGoal(golem, 1.2D, false)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
        golem.getTargetSelector().add(1, new DebugGoalWrapper(golem, new RevengeGoal(golem).setGroupRevenge()));
        golem.getTargetSelector().add(2, new DebugGoalWrapper(golem, new ActiveTargetGoal<>(golem, HostileEntity.class, true)));
    }

    public static void initFurnaceGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F)));
    }

    /// AI IN PROGRESS
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
        golem.getGoalSelector().add(7, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
    }

    /// AI NOT STARTED
    public static void initDiamondGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.DIAMOND), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WanderAroundFarGoal(golem, 1.0D)));
    }

    /// AI IN PROGRESS
    public static void initSpongeGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.FISHING_ROD), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new FishGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new DepositItemsGoal(golem)));
    }

    /// AI FUNCTIONAL
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

    /// AI FUNCTIONAL
    public static void initJukeboxGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new DebugGoalWrapper(golem, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
                Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_PRECIPICE
        ), false)));
        golem.getGoalSelector().add(2, new DebugGoalWrapper(golem, new WithdrawItemsGoal(golem)));
        golem.getGoalSelector().add(3, new DebugGoalWrapper(golem, new PickupItemGoal(golem)));
        golem.getGoalSelector().add(4, new DebugGoalWrapper(golem, new PlayRecordGoal(golem)));
        golem.getGoalSelector().add(5, new DebugGoalWrapper(golem, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F)));
        golem.getGoalSelector().add(6, new DebugGoalWrapper(golem, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F)));

    }
    
    /// GOAL WRAPPER
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
            golem.broadcastDebugMessage("Started " + goalName);
            innerGoal.start();
        }

        @Override
        public void stop() {
            golem.setDebugTarget(null);
            innerGoal.stop();
        }

        @Override
        public void tick() {
            innerGoal.tick();
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
            return targetVillager != null;
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
        }

        @Override
        public void stop() {
            targetVillager = null;
        }

        @Override
        public void tick() {
            if (targetVillager == null) return;

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
            for (TradeOffer offer : offers) {
                if (offer.isDisabled()) continue;
                if (offer.getSellItem().isOf(Items.EMERALD)) {
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

                        // Notify villager of trade
                        targetVillager.trade(offer);
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
            return waterPos != null;
        }

        private BlockPos findNearbyChest() {
            if (golem.getChestPos() != null) {
                BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                    return golem.getChestPos();
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (be instanceof Inventory && block == golem.getGolemType().getChestBlock()) {
                            golem.setChestPos(p);
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        private BlockPos findNearbyWater(BlockPos center) {
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = center.add(x, y, z);
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
            fishingTime = 0;
            maxFishingTime = 100 + golem.getRandom().nextInt(200); // 5-15 seconds
        }

        @Override
        public boolean shouldContinue() {
            ItemStack rod = golem.getHeldItem();
            return waterPos != null && golem.getEntityWorld().getBlockState(waterPos).isOf(Blocks.WATER) &&
                    chestPos != null && golem.getEntityWorld().getBlockState(chestPos).getBlock() == golem.getGolemType().getChestBlock() &&
                    !rod.isEmpty() && UtilityGolem.isFishingRod(rod) &&
                    fishingTime < maxFishingTime && !isInventoryFull() &&
                    golem.getBlockPos().getSquaredDistance(waterPos.getX(), waterPos.getY(), waterPos.getZ()) < 400 &&
                    golem.getBlockPos().getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096; // within 64 blocks of chest
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
            } else if (chance < 95) {
                loot = new ItemStack(Items.SADDLE); // Simplified junk/treasure for now
            } else {
                loot = Items.ENCHANTED_BOOK.getDefaultStack();
                if (serverWorld.getRegistryManager() != null) {
                    var enchantmentRegistry = serverWorld.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
                    var optionalEnchantment = enchantmentRegistry.getRandom(golem.getRandom());
                    if (optionalEnchantment.isPresent()) {
                        loot.addEnchantment(optionalEnchantment.get(), net.minecraft.util.math.MathHelper.nextInt(golem.getRandom(), 1, 3));
                    }
                }
            }

            ItemStack remaining = golem.getInventory().addStack(loot);
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

    public static class DepositItemsGoal extends Goal {
        private final UtilityGolem golem;
        private BlockPos chestPos;
        private int delay;

        public DepositItemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        public boolean hasItemsToDeposit() {
            if (golem.getGolemType() == GolemType.EMERALD) return hasEmeralds();
            if (golem.getGolemType() == GolemType.BAMBOO) return hasCropsToDeposit();
            if (golem.getGolemType() == GolemType.DEEPSLATE) return hasDeepslateItemsToDeposit();
            if (golem.getGolemType() == GolemType.SPONGE) return !isInventoryEmpty();
            if (golem.getGolemType() == GolemType.GOLD) return hasNonGoldItems();
            return hasFullStack() || isInventoryFull();
        }

        private boolean hasNonGoldItems() {
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
                if (UtilityGolem.isPickaxe(stack)) continue; // Common check in depositItems
                
                if (isSapling(stack)) {
                    if (getSaplingCount() > 8) return true;
                    continue;
                }
                if (UtilityGolem.isAxe(stack) || UtilityGolem.isShears(stack)) {
                    continue;
                }
                return true; // Anything else should be deposited
            }
            return false;
        }

        @Override
        public boolean canStart() {
            if (hasItemsToDeposit()) {
                chestPos = findNearbyChest();
                return chestPos != null;
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

        private boolean hasCropsToDeposit() {
            SimpleInventory inv = golem.getInventory();
            
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (isCrop(stack)) {
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (isSeed(stack) || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS)) {
                            continue;
                        }
                    }
                    return true;
                }
            }
            
            return false;
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
                /// MAKE SURE THE STACK IS NOT A PICKAXE
                if (!stack.isEmpty() && stack.getCount() >= stack.getMaxCount()/8 && !stack.getName().contains(Text.of("PICKAXE"))
                ) {
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
                BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                    return golem.getChestPos();
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (be instanceof Inventory && block == golem.getGolemType().getChestBlock()) {
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
        }

        @Override
        public boolean shouldContinue() {
            return chestPos != null && hasItemsToDeposit() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
        }

        private boolean isInventoryEmpty() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).getCount() > 8 && inv.getMaxCountPerStack() > 1) return false;
            }
            return true;
        }

        private boolean isSeed(ItemStack stack) {
            return stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT_SEEDS)
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS);
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
        }

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dx = golem.getX() - (chestPos.getX() + 0.5);
            double dy = golem.getY() - (chestPos.getY() + 0.5);
            double dz = golem.getZ() - (chestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                // If it's far vertically, target our current height
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().startMovingTo(chestPos.getX(), golem.getY(), chestPos.getZ(), 1.2D);
                    } else {
                        golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
                    }
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (++delay % 20 == 0) {
                    golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    depositItems();
                    if (!hasItemsToDeposit()) {
                        stop();
                    }
                }
            }
        }

        private void depositItems() {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                SimpleInventory golemInv = golem.getInventory();
                for (int i = 0; i < golemInv.size(); i++) {
                    ItemStack stack = golemInv.getStack(i);
                    if (!stack.isEmpty() && !UtilityGolem.isPickaxe(stack)) {
                        if (golem.getGolemType() == GolemType.EMERALD && !stack.isOf(Items.EMERALD)) {
                            continue;
                        }
                        if (golem.getGolemType() == GolemType.BAMBOO) {
                            if (isSeed(stack) || stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET) || UtilityGolem.isHoe(stack)
                                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            if (isSapling(stack) && getSaplingCount() <= 8) {
                                continue;
                            }
                            if (UtilityGolem.isAxe(stack) || UtilityGolem.isShears(stack)) {
                                continue;
                            }
                        }
                        if (golem.getGolemType() == GolemType.GOLD) {
                            if (stack.isOf(Items.GOLD_INGOT) || stack.isOf(Items.GOLD_NUGGET)) {
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

        public WithdrawItemsGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() == GolemType.LAPIS) {
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isPickaxe(golem.getHeldItem())) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                if ((hasAxe() || hasShears()) && hasEnoughSaplings()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null && hasNeededItemsInChest(chestPos);
            }

            if (golem.getGolemType() == GolemType.NETHERITE) {
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isSword(golem.getHeldItem())) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.BAMBOO) {
                // If we have EVERYTHING, we don't need to withdraw
                if (!hasAnythingNeeded()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null && hasNeededItemsInChest(chestPos);
            }
            
            if (golem.getGolemType() == GolemType.AMETHYST) {
                if (hasEnoughBreedingItems()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.REDSTONE) {
                if (hasRedstone()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.GOLD) {
                if (hasGold()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.JUKEBOX) {
                if (hasMusicDisc()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.FURNACE) {
                if (hasFuel()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.SPONGE) {
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isFishingRod(golem.getHeldItem())) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
            }

            if (golem.getGolemType() == GolemType.EMERALD) {
                if (hasTradeItems()) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null && hasTradeItemsInChest(chestPos);
            }

            return false;
        }

        private boolean hasTradeItems() {
            TradeOfferList offers = findNearbyVillagerOffers();
            if (offers == null) return false;
            SimpleInventory inventory = golem.getInventory();
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
            return false;
        }

        private TradeOfferList findNearbyVillagerOffers() {
            List<VillagerEntity> villagers = golem.getEntityWorld().getEntitiesByClass(VillagerEntity.class, golem.getBoundingBox().expand(16.0), villager -> true);
            for (VillagerEntity villager : villagers) {
                TradeOfferList offers = villager.getOffers();
                if (!offers.isEmpty()) return offers;
            }
            return null;
        }

        private boolean hasTradeItemsInChest(BlockPos pos) {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(pos);
            if (be instanceof Inventory container) {
                TradeOfferList offers = findNearbyVillagerOffers();
                if (offers == null) return false;
                
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
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS);
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

        private boolean hasMusicDisc() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) return true;
            }
            return false;
        }

        private boolean hasGold() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.GOLD_INGOT)) return true;
            }
            return false;
        }

        private boolean hasRedstone() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.REDSTONE)) return true;
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
                    if (golem.getGolemType() == GolemType.DEEPSLATE) {
                        if (!hasAxe() && UtilityGolem.isAxe(stack)) return true;
                        if (!hasShears() && UtilityGolem.isShears(stack)) return true;
                        if (!hasEnoughSaplings() && stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) return true;
                    }
                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (!UtilityGolem.isHoe(golem.getHeldItem()) && UtilityGolem.isHoe(stack)) return true;
                        if (!hasWaterBucket() && !hasEmptyBucket() && (stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET))) return true;
                        if (!hasSeeds() && isSeed(stack)) return true;
                    }
                    if (golem.getGolemType() == GolemType.GOLD) {
                        if (!hasGoldIngot() && stack.isOf(Items.GOLD_INGOT)) return true;
                        if (!hasGoldNugget() && stack.isOf(Items.GOLD_NUGGET)) return true;
                    }
                }
            }
            return false;
        }

        public boolean hasAnythingNeeded() {
            if (golem.getGolemType() == GolemType.BAMBOO) {
                return !UtilityGolem.isHoe(golem.getHeldItem()) || (!hasWaterBucket() && !hasEmptyBucket()) || !hasSeeds();
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
                BlockEntity be = golem.getEntityWorld().getBlockEntity(golem.getChestPos());
                if (be instanceof Inventory && golem.getEntityWorld().getBlockState(golem.getChestPos()).getBlock() == golem.getGolemType().getChestBlock()) {
                    return golem.getChestPos();
                }
            }

            BlockPos pos = golem.getBlockPos();
            int range = 16;
            for (int x = -range; x <= range; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (be instanceof Inventory && block == golem.getGolemType().getChestBlock()) {
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
        }

        @Override
        public boolean shouldContinue() {
            if (golem.getGolemType() == GolemType.LAPIS) {
                return chestPos != null && golem.getHeldItem().isEmpty() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.DEEPSLATE) {
                return chestPos != null && (!(hasAxe() || hasShears()) || !hasEnoughSaplings()) && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.NETHERITE) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isSword(golem.getHeldItem())) && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.BAMBOO) {
                // Should continue if we still need something AND there's a chest to get it from
                return chestPos != null && hasAnythingNeeded() && !isInventoryFull() && 
                       golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory &&
                       hasNeededItemsInChest(chestPos);
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
            if (golem.getGolemType() == GolemType.SPONGE) {
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isFishingRod(golem.getHeldItem())) && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
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
        }

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dx = golem.getX() - (chestPos.getX() + 0.5);
            double dy = golem.getY() - (chestPos.getY() + 0.5);
            double dz = golem.getZ() - (chestPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                // If it's far vertically, target our current height
                if (verticalDist > 2.0D) {
                    golem.getNavigation().startMovingTo(chestPos.getX(), golem.getY(), chestPos.getZ(), 1.2D);
                } else {
                    golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (++delay % 20 == 0) {
                    golem.getEntityWorld().addSyncedBlockEvent(chestPos, golem.getEntityWorld().getBlockState(chestPos).getBlock(), 1, 1);
                    golem.setSearching(true);
                    withdrawItems();
                }
            }
        }

        private boolean withdrawItems() {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                SimpleInventory golemInv = golem.getInventory();
                for (int i = 0; i < container.size(); i++) {
                    ItemStack containerStack = container.getStack(i);
                    if (containerStack.isEmpty()) continue;

                    if (golem.getGolemType() == GolemType.LAPIS && UtilityGolem.isPickaxe(containerStack)) {
                        ItemStack pickaxe = containerStack.split(1);
                        golem.setHeldItem(pickaxe);
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.AMETHYST && isValidBreedingItem(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        if (hasEnoughBreedingItems() || isInventoryFull()) return true;
                    }

                    if (golem.getGolemType() == GolemType.REDSTONE && containerStack.isOf(Items.REDSTONE)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        if (hasRedstone() || isInventoryFull()) return true;
                    }

                    if (golem.getGolemType() == GolemType.GOLD && containerStack.isOf(Items.GOLD_INGOT)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        if (hasGold() || isInventoryFull()) return true;
                    }

                    if (golem.getGolemType() == GolemType.JUKEBOX && containerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        if (hasMusicDisc() || isInventoryFull()) return true;
                    }

                    if (golem.getGolemType() == GolemType.FURNACE && isFuel(containerStack)) {
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                        if (hasFuel() || isInventoryFull()) return true;
                    }

                    if (golem.getGolemType() == GolemType.BAMBOO) {
                        if (UtilityGolem.isHoe(containerStack)) {
                            ItemStack hoe = containerStack.split(1);
                            golem.setHeldItem(hoe);
                            if ((hasWaterBucket() || hasEmptyBucket()) && hasSeeds()) return true;
                        } else if (containerStack.isOf(Items.BUCKET) || containerStack.isOf(Items.WATER_BUCKET)) {
                            if (!hasWaterBucket() && !hasEmptyBucket()) {
                                ItemStack bucket = containerStack.split(1);
                                golem.getInventory().addStack(bucket);
                                if (UtilityGolem.isHoe(golem.getHeldItem()) && hasSeeds()) return true;
                            }
                        } else if (isSeed(containerStack)) {
                             if (!hasSeeds()) {
                                 ItemStack seeds = containerStack.split(Math.min(containerStack.getCount(), 64));
                                 golem.getInventory().addStack(seeds);
                                 if (UtilityGolem.isHoe(golem.getHeldItem()) && (hasWaterBucket() || hasEmptyBucket())) return true;
                             }
                        }
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.SPONGE && UtilityGolem.isFishingRod(containerStack)) {
                        ItemStack rod = containerStack.split(1);
                        golem.setHeldItem(rod);
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
                        } else if (!hasShears() && UtilityGolem.isShears(containerStack)) {
                            ItemStack tool = containerStack.split(1);
                            if (golem.getHeldItem().isEmpty()) {
                                golem.setHeldItem(tool);
                            } else {
                                golem.getInventory().addStack(tool);
                            }
                        } else if (!hasEnoughSaplings() && containerStack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) {
                            int needed = 8 - getSaplingCount();
                            if (needed > 0) {
                                ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                golem.getInventory().addStack(toWithdraw);
                            }
                        }
                        
                        if (hasAxe() || hasShears()) {
                            if (hasEnoughSaplings()) return true;
                        }
                        continue;
                    }

                    if (golem.getGolemType() == GolemType.NETHERITE && UtilityGolem.isSword(containerStack)) {
                        ItemStack sword = containerStack.split(1);
                        golem.setHeldItem(sword);
                        return true;
                    }

                    if (golem.getGolemType() == GolemType.EMERALD) {
                        TradeOfferList offers = findNearbyVillagerOffers();
                        if (offers != null) {
                            for (TradeOffer offer : offers) {
                                if (offer.isDisabled()) continue;
                                if (offer.getSellItem().isOf(Items.EMERALD)) {
                                    TradedItem buyItem1 = offer.getFirstBuyItem();
                                    Optional<TradedItem> buyItem2 = offer.getSecondBuyItem();
                                    
                                    if (buyItem1.matches(containerStack)) {
                                        int needed = buyItem1.count() - getCountInInventory(buyItem1);
                                        if (needed > 0) {
                                            ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                            golem.getInventory().addStack(toWithdraw);
                                            if (hasTradeItems()) return true;
                                        }
                                    }
                                    if (buyItem2.isPresent() && buyItem2.get().matches(containerStack)) {
                                        int needed = buyItem2.get().count() - getCountInInventory(buyItem2.get());
                                        if (needed > 0) {
                                            ItemStack toWithdraw = containerStack.split(Math.min(needed, containerStack.getCount()));
                                            golem.getInventory().addStack(toWithdraw);
                                            if (hasTradeItems()) return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                golemInv.markDirty();
                container.markDirty();
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

        public DigBlockGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            ItemStack pickaxe = golem.getHeldItem();
            if (pickaxe.isEmpty() || !UtilityGolem.isPickaxe(pickaxe)) {
                return false;
            }
            targetPos = findTargetBlock();
            if (targetPos != null) {
                this.maxBreakingTime = calculateBreakingTime(pickaxe, targetPos);
                return true;
            }
            return false;
        }

        private int calculateBreakingTime(ItemStack pickaxe, BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            float hardness = state.getHardness(golem.getEntityWorld(), pos);
            if (hardness < 0) return 200; // Unbreakable

            float speed = 1.0f;
            if (pickaxe.isOf(Items.GOLDEN_PICKAXE)) speed = 9.0f;
            else if (pickaxe.isOf(Items.NETHERITE_PICKAXE)) speed = 12.0f;
            else if (pickaxe.isOf(Items.DIAMOND_PICKAXE)) speed = 12.0f;
            else if (pickaxe.isOf(Items.IRON_PICKAXE)) speed = 6.0f;
            else if (pickaxe.isOf(Items.STONE_PICKAXE)) speed = 4.0f;
            else if (pickaxe.isOf(Items.WOODEN_PICKAXE)) speed = 2.0f;
            else if (pickaxe.isOf(Items.COPPER_PICKAXE)) speed = 5.0f;

            // Player mining formula is roughly (hardness * 30) / speed if using correct tool
            // We want it slower than player, so let's use a higher multiplier
            return Math.max(20, (int) (hardness * 60 / speed));
        }


        private BlockPos findTargetBlock() {
            BlockPos pos = golem.getBlockPos();
            BlockPos chestPos = golem.getChestPos();
            List<BlockPos> potentialTargets = new ArrayList<>();
            int range = 15;
            for (int x = -8; x <= 8; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -8; z <= 8; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (canDig(p)) {
                            // Only dig if within 64 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
                                potentialTargets.add(p);
                            }
                        }
                    }
                }
            }
            return potentialTargets.stream()
                    .min(Comparator.comparingDouble(p -> p.getSquaredDistance(golem.getX(), golem.getY(), golem.getZ())))
                    .orElse(null);
        }

        private boolean canDig(BlockPos pos) {
            BlockState state = golem.getEntityWorld().getBlockState(pos);
            return state.isIn( BlockTags.BASE_STONE_OVERWORLD)
                || state.isIn(BlockTags.BASE_STONE_NETHER)
                || state.isIn(BlockTags.COAL_ORES)
                || state.isIn(BlockTags.IRON_ORES)
                || state.isIn(BlockTags.COPPER_ORES)
                || state.isIn(BlockTags.GOLD_ORES)
                || state.isIn(BlockTags.DIAMOND_ORES)
                || state.isIn(BlockTags.EMERALD_ORES)
                || state.isIn(BlockTags.LAPIS_ORES)
                || state.isIn(BlockTags.REDSTONE_ORES);
        }

        @Override
        public void start() {
            breakingTime = 0;
        }

        @Override
        public boolean shouldContinue() {
            ItemStack pickaxe = golem.getHeldItem();
            return targetPos != null && canDig(targetPos) && !pickaxe.isEmpty() && UtilityGolem.isPickaxe(pickaxe) &&
                    breakingTime < maxBreakingTime && golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 400;
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, -1);
            }
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            double dx = golem.getX() - (targetPos.getX() + 0.5);
            double dy = golem.getY() - (targetPos.getY() + 0.5);
            double dz = golem.getZ() - (targetPos.getZ() + 0.5);
            double horizontalDistSq = dx * dx + dz * dz;
            double verticalDist = Math.abs(dy);

            if (horizontalDistSq > 16.0D || verticalDist > 15.0D) {
                // If it's high up or far below, move to the XZ position at our current height
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                    }
                }
                breakingTime = 0;
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                
                // Swing arm every 5 ticks
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

        private void breakBlock() {
            if (!(golem.getEntityWorld() instanceof ServerWorld serverWorld)) return;

            BlockState state = serverWorld.getBlockState(targetPos);
            if (canDig(targetPos)) {
                LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                        .add(LootContextParameters.TOOL, golem.getHeldItem())
                        .addOptional(LootContextParameters.THIS_ENTITY, golem);

                serverWorld.breakBlock(targetPos, false, golem);

                List<ItemStack> drops = state.getDroppedStacks(builder);
                for (ItemStack drop : drops) {
                    ItemStack remaining = golem.getInventory().addStack(drop);
                    if (!remaining.isEmpty()) {
                        Block.dropStack(serverWorld, targetPos, remaining);
                    }
                }
                
                // Damage the pickaxe
                ItemStack pickaxe = golem.getHeldItem();
                if (!pickaxe.isEmpty() && UtilityGolem.isPickaxe(pickaxe)) {
                    pickaxe.damage(1, (ServerWorld) golem.getEntityWorld(), null, (item) -> golem.setHeldItem(ItemStack.EMPTY));
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
            if (chestPos == null) return false;

            targetPos = findTargetPos();
            return targetPos != null;
        }

        private BlockPos findTargetPos() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos == null) return null;

            // Define the 9x9 area centered on water which is near the chest
            BlockPos waterPos = findWaterCenter(chestPos);
            BlockPos center = waterPos;
            
            if (center == null) {
                // If no water exists, find a place for it
                center = findPlaceForWater(chestPos);
            }
            
            if (center == null) {
                // Fallback to chest if no water or water spot found
                center = chestPos;
            }

            // EXTREMELY IMPORTANT: We MUST have water before tilling or planting
            // If we have a bucket and the center isn't water, prioritize placing it
            if (!isWater(center) && hasWaterBucket()) {
                // If center is chest, we shouldn't place water there! 
                // But findPlaceForWater should have found a better spot if possible.
                // If center is a valid spot for water, return it.
                if (canPlaceWater(center) || (waterPos != null && center.equals(waterPos))) {
                    return center;
                }
            }


            // Check for crops to harvest or land to till/plant in 9x9 area around center
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    for (int y = -1; y <= 1; y++) {
                        BlockPos p = center.add(x, y, z);
                        if (p.equals(center)) {
                            continue; 
                        }
                        
                        // Harvesting is allowed even without water
                        if (shouldHarvest(p, center)) {
                            return p;
                        }
                        
                        // Tilling and planting ONLY allowed if water is present
                        if (isWater(center)) {
                            if (shouldTill(p, center) || shouldPlant(p, center)) {
                                return p;
                            }
                        } else if (!hasWaterBucket()) {
                            // If we don't have water and can't place it, still till/plant 
                            // as long as there is some water nearby (vanilla farm behavior)
                            if (shouldTill(p, center) || shouldPlant(p, center)) {
                                return p;
                            }
                        }
                    }
                }
            }

            // Check if center block itself needs action (in case it's not water)
            if (!center.equals(chestPos)) {
                if (shouldHarvest(center, null)) return center;
                if (isWater(center)) {
                    // center is already water, do nothing
                } else if (!hasWaterBucket()) {
                    if (shouldTill(center, null) || shouldPlant(center, null)) return center;
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

            // Nether wart
            if (state.isOf(Blocks.SOUL_SAND) && isAboveSafe && hasNetherWart()) return true;

            // Cocoa beans
            if (state.isAir() && hasCocoaBeans() && findJungleLogNearby(pos) != null) return true;

            return false;
        }

        private boolean hasNetherWart() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.NETHER_WART)) return true;
            }
            return false;
        }

        private boolean hasCocoaBeans() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.COCOA_BEANS)) return true;
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
                    || stack.isOf(Items.PUMPKIN_SEEDS) || stack.isOf(Items.MELON_SEEDS) || stack.isOf(Items.NETHER_WART) || stack.isOf(Items.COCOA_BEANS);
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
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            // Ensure we are holding the right tool for the job
            ensureCorrectTool();

            double dist = golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if (dist > 4.0) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
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
                                    world.playSound(null, targetPos, net.minecraft.sound.SoundEvents.BLOCK_GRASS_PLACE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
                                }
                            } else {
                                world.setBlockState(plantPos, seedBlock.getDefaultState());
                                seeds.decrement(1);
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
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (isSeed(stack) && !stack.isOf(Items.NETHER_WART) && !stack.isOf(Items.COCOA_BEANS)) return stack;
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getNetherWart() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.NETHER_WART)) return inv.getStack(i);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getCocoaBeans() {
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
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (block instanceof ButtonBlock ||
                            block instanceof LeverBlock ||
                            block instanceof PressurePlateBlock) {
                            // Only trigger if within 64 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
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
                // If it's far vertically, target our current height
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

                if (++delay % 20 == 0) {
                    interactWithComponent();
                    if (golem.getGolemType() == GolemType.REDSTONE) {
                        stop();
                    }
                }
            }
        }

        public void interactWithComponent() {
            BlockPos p =  golem.getBlockPos();
            BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
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
                        
                        // Only breed if within 64 blocks of chest (if chest is known)
                        if (chestPos != null) {
                            if (a.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 4096) continue;
                            if (b.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 4096) continue;
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
        }

        @Override
        public void stop() {
            animalA = null;
            animalB = null;
        }

        @Override
        public void tick() {
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
                    (chestPos != null && currentTreePos.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 4096)) {
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
                        if (canChop(p)) {
                            // Only chop if within 64 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
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
                        if (canChop(p)) {
                            // Only chop if within 64 blocks of chest (if chest is known)
                            if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
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
        }

        @Override
        public boolean shouldContinue() {
            ItemStack tool = golem.getHeldItem();
            return targetPos != null && canChop(targetPos) && !tool.isEmpty() && (UtilityGolem.isAxe(tool) || UtilityGolem.isShears(tool)) &&
                    breakingTime < maxBreakingTime && golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 400;
        }

        @Override
        public void stop() {
            if (targetPos != null) {
                golem.getEntityWorld().setBlockBreakingInfo(golem.getId(), targetPos, -1);
            }
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

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
                // If it's high up, move to the base of it
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    // If it's high up, move to the base of it
                    if (verticalDist > 2.0D) {
                        golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                    } else {
                        golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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

        public ReplantSaplingGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (getSaplingFromInventory().isEmpty()) {
                return false;
            }
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
                        if (canPlantAt(p)) {
                            // Sparse pattern check: No other saplings within 3 blocks
                            if (isSparse(p)) {
                                // Only plant if within 64 blocks of chest (if chest is known)
                                if (chestPos == null || p.getSquaredDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096) {
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
        }

        @Override
        public boolean shouldContinue() {
            return targetPos != null && canPlantAt(targetPos) && !getSaplingFromInventory().isEmpty();
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

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

        public TradeWithPiglinGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (golem.getGolemType() != GolemType.GOLD) return false;
            if (!hasGoldIngot()) return false;
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
            return targetPiglin != null && targetPiglin.isAlive() && hasGoldIngot() && golem.squaredDistanceTo(targetPiglin) < 256;
        }

        @Override
        public void start() {
            tradeDelay = 0;
        }

        @Override
        public void stop() {
            targetPiglin = null;
        }

        @Override
        public void tick() {
            if (targetPiglin == null) return;

            golem.getLookControl().lookAt(targetPiglin, 30.0F, 30.0F);
            double distSq = golem.squaredDistanceTo(targetPiglin);

            if (distSq > 9.0D) {
                if (golem.getNavigation().isIdle() || golem.getRandom().nextInt(10) == 0) {
                    golem.getNavigation().startMovingTo(targetPiglin, 1.2D);
                }
            } else {
                golem.getNavigation().stop();
                if (++tradeDelay % 10 == 0) {
                    if (isPiglinReady(targetPiglin)) {
                        dropGoldIngot();
                    }
                }
            }
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
            ItemStack goldIngot = ItemStack.EMPTY;
            boolean inHand = false;
            if (golem.getHeldItem().isOf(Items.GOLD_INGOT)) {
                goldIngot = golem.getHeldItem();
                inHand = true;
            } else {
                SimpleInventory inv = golem.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStack(i).isOf(Items.GOLD_INGOT)) {
                        goldIngot = inv.getStack(i);
                        break;
                    }
                }
            }

            if (!goldIngot.isEmpty()) {
                ItemStack toDrop = goldIngot.split(1);
                if (inHand && goldIngot.isEmpty()) {
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
                golem.broadcastDebugMessage("Offered Gold Ingot to Piglin");
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
                cooldown = 10;
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
                        } else if (golem.getGolemType() == GolemType.JUKEBOX) {
                            isFamiliar = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE) != null;
                        } else if (golem.getGolemType() == GolemType.GOLD) {
                            // Gold golem wants to pick up gold AND anything else (traded items)
                            isFamiliar = true; 
                        }
                        
                        if (!isFamiliar) return false;
                        
                        // Only pickup if within 64 blocks of chest (if chest is known)
                        if (chestPos != null) {
                            return item.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < 4096;
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
            targetItem = null;
            if (stuckTicks >= 60) {
                cooldown = 100; // Longer cooldown if we got stuck
            }
        }

        @Override
        public void tick() {
            if (targetItem == null) return;

            // Stuck detection: if we are far and not moving much
            Vec3d currentPos = new Vec3d(golem.getX(), golem.getY(), golem.getZ());
            if (currentPos.squaredDistanceTo(lastPos) < 0.01 * 0.01) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
                lastPos = currentPos;
            }

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
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        public boolean canStart() {
            ItemStack tool = golem.getHeldItem();
            World world = golem.getEntityWorld();
            BlockPos blockPos = golem.getBlockPos();
            float vol = 1.0f;
            float pitch = 1.0f;
            if (tool.isEmpty() || (!isRecord())) {
                return false;
            }

            //Something like this for playing the record, depending on the type
            //world.playSound(null, blockPos, SoundEvents.MUSIC_DISC_PIGSTEP, SoundCategory.RECORDS, vol, pitch);
            return true;
        }

        public boolean canContinue() {
            return true;
        }

        public boolean isRecord() {
            return golem.getHeldItem().isIn((RegistryEntryList<Item>) Items.MUSIC_DISC_LAVA_CHICKEN);
                    /*Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                    Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                    Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
                    Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_PRECIPICE*/

        }
    }
}
