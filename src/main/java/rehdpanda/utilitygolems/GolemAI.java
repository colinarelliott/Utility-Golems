package rehdpanda.utilitygolems;

import com.ibm.icu.impl.Utility;
import net.minecraft.block.*;
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
                Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE
        ), false));
        golem.getGoalSelector().add(2, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(3, new DigBlockGoal(golem));
        golem.getGoalSelector().add(4, new DepositItemsGoal(golem));
        golem.getGoalSelector().add(5, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
    }

    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.REDSTONE), false));
        //golem.getGo
    }

    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.EMERALD), false));
        golem.getGoalSelector().add(2, new TradeWithVillagerGoal(golem));
        golem.getGoalSelector().add(3, new DepositItemsGoal(golem));
        golem.getGoalSelector().add(4, new LookAtEntityGoal(golem, VillagerEntity.class, 8.0F));
        golem.getGoalSelector().add(5, new FollowMobGoal(golem, 1.0D, 3.0F, 10.0F));
    }

    public static void initGoldGoals(UtilityGolem golem) {
        //gold golem behaviour
    }
    public static void initAmethystGoals(UtilityGolem golem) {
        //amethyst golem behaviour
        golem.getGoalSelector().add(1, new WithdrawItemsGoal(golem));
        golem.getGoalSelector().add(2, new BreedAnimals(golem));
    }
    public static void initNetheriteGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new MeleeAttackGoal(golem, 1.2D, false));
        golem.getTargetSelector().add(1, new RevengeGoal(golem).setGroupRevenge());
        golem.getTargetSelector().add(2, new ActiveTargetGoal<>(golem, HostileEntity.class, true));
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

        @Override
        public void stop() {
            chestPos = null;
        }

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dist = golem.squaredDistanceTo(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);
            if (dist > 4.0D) {
                golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (++delay % 20 == 0) {
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
                    if (!stack.isEmpty() && !isPickaxe(stack)) {
                        if (golem.getGolemType() == GolemType.EMERALD && !stack.isOf(Items.EMERALD)) {
                            continue;
                        }
                        ItemStack remaining = transferStack(stack, container);
                        golemInv.setStack(i, remaining);
                    }
                }
                golemInv.markDirty();
                container.markDirty();
            }
        }

        private boolean isPickaxe(ItemStack stack) {
            return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
                    stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.DIAMOND_PICKAXE) ||
                    stack.isOf(Items.NETHERITE_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE);
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

    /// BUGGY
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
                chestPos = findNearbyChest();
                return chestPos != null;
            }
            if (!isInventoryFull()) {
                return false;
            }
            chestPos = findNearbyChest();
            return chestPos != null;
        }

        private boolean isInventoryFull() {
            SimpleInventory inv = golem.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isEmpty()) return false;
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
                        BlockEntity be = golem.getEntityWorld().getBlockEntity(p);
                        BlockState bs = golem.getEntityWorld().getBlockState(p);
                        Block block = bs.getBlock();
                        if (be instanceof Inventory && block == golem.getGolemType().getChestBlock()) {
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

        @Override
        public void stop() {
            chestPos = null;
        }

        @Override
        public void tick() {
            if (chestPos == null) return;

            double dist = golem.squaredDistanceTo(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);
            if (dist > 4.0D) {
                golem.getNavigation().startMovingTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.2D);
            } else {
                golem.getNavigation().stop();
                golem.getLookControl().lookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);

                if (++delay % 20 == 0) {
                    withdrawItems();
                    if (golem.getGolemType() == GolemType.LAPIS) {
                        stop();
                    }
                }
            }
        }

        private void withdrawItems() {
            BlockEntity be = golem.getEntityWorld().getBlockEntity(chestPos);
            if (be instanceof Inventory container) {
                SimpleInventory golemInv = golem.getInventory();
                for (int i = 0; i < container.size(); i++) {
                    ItemStack containerStack = container.getStack(i);
                    if (!containerStack.isEmpty() && isPickaxe(containerStack)) {
                        if (golem.getGolemType() == GolemType.LAPIS) {
                            continue;
                        }
                        ItemStack remaining = transferStack(containerStack, golemInv);
                        container.setStack(i, remaining);
                    }
                }
                golemInv.markDirty();
                container.markDirty();
            }
        }

        private boolean isPickaxe(ItemStack stack) {
            return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
                    stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.DIAMOND_PICKAXE) ||
                    stack.isOf(Items.NETHERITE_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE);
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
            if (pickaxe.isEmpty() || !isPickaxe(pickaxe)) {
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

        private boolean isPickaxe(ItemStack stack) {
            return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
                    stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.DIAMOND_PICKAXE) ||
                    stack.isOf(Items.NETHERITE_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE);
        }

        private BlockPos findTargetBlock() {
            BlockPos pos = golem.getBlockPos();
            List<BlockPos> potentialTargets = new ArrayList<>();
            int range = 8;
            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (canDig(p)) {
                            potentialTargets.add(p);
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
            return targetPos != null && canDig(targetPos) && !pickaxe.isEmpty() && isPickaxe(pickaxe) &&
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

            double dist = golem.squaredDistanceTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            if (dist > 4.0D) {
                golem.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
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
                if (!pickaxe.isEmpty() && isPickaxe(pickaxe)) {
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
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void tick() {
            if (componentPosition == null) return;

            double dist = golem.squaredDistanceTo(componentPosition.getX() + 0.5, componentPosition.getY() + 0.5, componentPosition.getZ() + 0.5);
            if (dist > 4.0D) {
                golem.getNavigation().startMovingTo(componentPosition.getX(), componentPosition.getY(), componentPosition.getZ(), 1.2D);
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
}
