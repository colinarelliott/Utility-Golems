package rehdpanda.utilitygolems;

import net.minecraft.block.*;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

public class GolemAI {
    public static void initLapisGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.COPPER_PICKAXE
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new DigBlockGoal(golem));
        golem.getGoalSelector().add(4, new DepositItemsGoal(golem));
        golem.getGoalSelector().add(5, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
    }

    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.REDSTONE), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new TriggerRedstoneGoal(golem));
    }

    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.EMERALD), false));
        golem.getGoalSelector().add(2, new TradeWithVillagerGoal(golem));
        golem.getGoalSelector().add(3, new DepositItemsGoal(golem));
        golem.getGoalSelector().add(4, new LookAtEntityGoal(golem, VillagerEntity.class, 8.0F));
        golem.getGoalSelector().add(5, new FollowMobGoal(golem, 1.0D, 3.0F, 10.0F));
    }

    public static void initGoldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.GOLD_INGOT), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
    }
    
    public static void initAmethystGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.WHEAT_SEEDS), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new BreedAnimals(golem));
    }
    
    public static void initNetheriteGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD, Items.WOODEN_SWORD, Items.COPPER_SWORD
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new MeleeAttackGoal(golem, 1.2D, false));
        golem.getTargetSelector().add(1, new RevengeGoal(golem).setGroupRevenge());
        golem.getTargetSelector().add(2, new ActiveTargetGoal<>(golem, HostileEntity.class, true));
    }

    public static void initFurnaceGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F));
        golem.getGoalSelector().add(4, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
    }

    public static void initBambooGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_HOE, Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.STONE_HOE, Items.WOODEN_HOE, Items.COPPER_HOE
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
    }

    public static void initDiamondGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.DIAMOND), false));
    }

    public static void initSpongeGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.FISHING_ROD), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new FishGoal(golem));
        golem.getGoalSelector().add(4, new DepositItemsGoal(golem));
    }

    public static void initDeepslateGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.WOODEN_AXE, Items.COPPER_AXE, Items.SHEARS
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new PickupItemGoal(golem));
        golem.getGoalSelector().add(4, new ChopTreeGoal(golem));
        golem.getGoalSelector().add(5, new ReplantSaplingGoal(golem));
        golem.getGoalSelector().add(6, new DepositItemsGoal(golem));
    }

    public static void initJukeboxGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new FollowPlayerGoal(golem, 1.1D, 3.0F, 16.0F));
        golem.getGoalSelector().add(4, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
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
        public void stop() {
            targetPlayer = null;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetPlayer == null) return;
            golem.getLookControl().lookAt(targetPlayer, 30.0F, 30.0F);
            if (golem.squaredDistanceTo(targetPlayer) > (double)(minDistance * minDistance)) {
                golem.getNavigation().startMovingTo(targetPlayer, speed);
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
                golem.getNavigation().startMovingTo(targetVillager, 1.2D);
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

        private BlockPos findNearbyWater(BlockPos center) {
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
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
                    golem.getBlockPos().getSquaredDistance(waterPos.getX(), waterPos.getY(), waterPos.getZ()) < 100 &&
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

            if (horizontalDistSq > 16.0D || verticalDist > 4.0D) {
                // If it's far vertically, target our current height
                if (verticalDist > 2.0D) {
                    golem.getNavigation().startMovingTo(waterPos.getX(), golem.getY(), waterPos.getZ(), 1.1D);
                } else {
                    golem.getNavigation().startMovingTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.1D);
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

        @Override
        public boolean canStart() {
            if (golem.getGolemType() == GolemType.EMERALD && hasEmeralds()) {
                chestPos = findNearbyChest();
                return chestPos != null;
            }
            if ((golem.getGolemType() == GolemType.SPONGE || golem.getGolemType() == GolemType.DEEPSLATE) && !isInventoryEmpty()) {
                chestPos = findNearbyChest();
                return chestPos != null;
            }
            if (!hasFullStack() && !isInventoryFull()) {
                return false;
            }
            chestPos = findNearbyChest();
            return chestPos != null;
        }

        private boolean hasEmeralds() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.EMERALD)) return true;
            }
            return false;
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
            if (golem.getGolemType() == GolemType.EMERALD && hasEmeralds()) {
                return chestPos != null && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.SPONGE && !isInventoryEmpty()) {
                return chestPos != null && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            return chestPos != null && !isInventoryEmpty() &&
                    golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
        }

        private boolean isInventoryEmpty() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (!inv.getStack(i).isEmpty()) return false;
            }
            return true;
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
                    depositItems();
                    if (golem.getGolemType() == GolemType.EMERALD && !hasEmeralds()) {
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
                        if (golem.getGolemType() == GolemType.DEEPSLATE) {
                            if (isSapling(stack) && getSaplingCount() <= 8) {
                                continue;
                            }
                            if (UtilityGolem.isAxe(stack) || UtilityGolem.isShears(stack)) {
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
                if (!golem.getHeldItem().isEmpty() && UtilityGolem.isHoe(golem.getHeldItem())) {
                    return false;
                }
                chestPos = findNearbyChest();
                return chestPos != null;
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
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return true;
            }
            return false;
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
                }
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
                return chestPos != null && (golem.getHeldItem().isEmpty() || !UtilityGolem.isHoe(golem.getHeldItem())) && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.AMETHYST) {
                return chestPos != null && !hasEnoughBreedingItems() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.REDSTONE) {
                return chestPos != null && !hasRedstone() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
            }
            if (golem.getGolemType() == GolemType.GOLD) {
                return chestPos != null && !hasGold() && !isInventoryFull() && golem.getEntityWorld().getBlockEntity(chestPos) instanceof Inventory;
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

                    if (golem.getGolemType() == GolemType.BAMBOO && UtilityGolem.isHoe(containerStack)) {
                        ItemStack hoe = containerStack.split(1);
                        golem.setHeldItem(hoe);
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
                }
                golemInv.markDirty();
                container.markDirty();
            }
            return false;
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
            int range = 8;
            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
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
                    breakingTime < maxBreakingTime && golem.getBlockPos().getSquaredDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 64;
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

            if (horizontalDistSq > 4.0D || verticalDist > 4.0D) {
                // If it's high up or far below, move to the XZ position at our current height
                if (verticalDist > 2.0D) {
                    golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                } else {
                    golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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

    /// UNFINISHED
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
                if (verticalDist > 2.0D) {
                    golem.getNavigation().startMovingTo(componentPosition.getX(), golem.getY(), componentPosition.getZ(), 1.2D);
                } else {
                    golem.getNavigation().startMovingTo(componentPosition.getX(), componentPosition.getY(), componentPosition.getZ(), 1.2D);
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

    public static class BreedAnimals extends Goal {
        private UtilityGolem golem;
        private AnimalEntity animalA;
        private AnimalEntity animalB;
        private int delay;

        public BreedAnimals(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
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

            Vec3d center = Vec3d.of(animalA.getBlockPos().add(animalB.getBlockPos().multiply(1/2)));
            golem.getNavigation().startMovingTo(center.x, center.y, center.z, 1.1D);
            golem.getLookControl().lookAt(center.x, center.y, center.z);

            if (++delay >= 40) {
                breed();
                stop();
            }
        }

        private void breed() {
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
        private int breakingTime;
        private int maxBreakingTime;

        public ChopTreeGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            ItemStack tool = golem.getHeldItem();
            if (tool.isEmpty() || (!UtilityGolem.isAxe(tool) && !UtilityGolem.isShears(tool))) {
                return false;
            }
            if (isInventoryFull()) {
                return false;
            }
            targetPos = findTargetBlock();
            if (targetPos != null) {
                this.maxBreakingTime = calculateBreakingTime(tool, targetPos);
                return true;
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
            BlockPos pos = golem.getBlockPos();
            BlockPos chestPos = golem.getChestPos();
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
                                // Small bias towards logs to prefer them if distance is similar
                                double score = distSq + (isLog(p) ? 0 : 1.0);
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
            return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
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
            if (horizontalDistSq > 4.0D || verticalDist > 15.0D) {
                // If it's high up, move to the base of it
                if (verticalDist > 2.0D) {
                    golem.getNavigation().startMovingTo(targetPos.getX(), golem.getY(), targetPos.getZ(), 1.2D);
                } else {
                    golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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
                golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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
    
    public static class PickupItemGoal extends Goal {
        private final UtilityGolem golem;
        private net.minecraft.entity.ItemEntity targetItem;

        public PickupItemGoal(UtilityGolem golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (isInventoryFull()) {
                return false;
            }
            targetItem = findNearbyItem();
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
                    golem.getBoundingBox().expand(8.0),
                    item -> {
                        ItemStack stack = item.getStack();
                        boolean isSapling = stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS);
                        boolean isApple = stack.isOf(Items.APPLE);
                        boolean isStick = stack.isOf(Items.STICK);
                        if (!isSapling && !isApple && !isStick) return false;
                        
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
        public boolean shouldContinue() {
            return targetItem != null && targetItem.isAlive() && !isInventoryFull() &&
                    golem.squaredDistanceTo(targetItem) < 256;
        }

        @Override
        public void stop() {
            targetItem = null;
        }

        @Override
        public void tick() {
            if (targetItem == null) return;

            if (golem.squaredDistanceTo(targetItem) > 1.0D) {
                golem.getNavigation().startMovingTo(targetItem, 1.2D);
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

    public static class WanderNearChestGoal extends WanderAroundGoal {
        private final UtilityGolem golem;

        public WanderNearChestGoal(UtilityGolem golem, double speed) {
            super(golem, speed);
            this.golem = golem;
        }

        @Override
        public boolean canStart() {
            if (golem.getChestPos() != null) {
                if (golem.squaredDistanceTo(golem.getChestPos().getX(), golem.getChestPos().getY(), golem.getChestPos().getZ()) > 4096) {
                    return true; // Force return if too far
                }
            }
            return super.canStart();
        }

        @Override
        protected Vec3d getWanderTarget() {
            BlockPos chestPos = golem.getChestPos();
            if (chestPos != null) {
                if (golem.squaredDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) > 4096) {
                    // Head back to chest
                    return Vec3d.ofCenter(chestPos);
                }
            }
            return super.getWanderTarget();
        }
    }
}
