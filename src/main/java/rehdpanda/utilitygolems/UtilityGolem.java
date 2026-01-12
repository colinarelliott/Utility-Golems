package rehdpanda.utilitygolems;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/// BASE FOR THE GOLEM EXTENDS THE COPPER GOLEMS,
/// DEFINES BASIC BEHAVOUR, TYPE AND HELD ITEM


import net.minecraft.util.Identifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.WorldEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import java.util.Optional;

public class UtilityGolem extends CopperGolemEntity implements InventoryOwner {

    private final GolemType golemType;
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleInventory inventory = new SimpleInventory(9);
    private int jukeboxCooldown = 0;
    private ItemStack currentlyPlayingStack = ItemStack.EMPTY;
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int cookTimeTotal;

    private final net.minecraft.screen.PropertyDelegate furnacePropertyDelegate = new net.minecraft.screen.PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return UtilityGolem.this.burnTime;
                case 1: return UtilityGolem.this.fuelTime;
                case 2: return UtilityGolem.this.cookTime;
                case 3: return UtilityGolem.this.cookTimeTotal;
                default: return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: UtilityGolem.this.burnTime = value; break;
                case 1: UtilityGolem.this.fuelTime = value; break;
                case 2: UtilityGolem.this.cookTime = value; break;
                case 3: UtilityGolem.this.cookTimeTotal = value; break;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    private static final TrackedData<Optional<BlockPos>> FISHING_TARGET = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FISHING_TARGET, Optional.empty());
    }

    public void setFishingTarget(@Nullable BlockPos pos) {
        this.dataTracker.set(FISHING_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getFishingTarget() {
        return (BlockPos)((Optional)this.dataTracker.get(FISHING_TARGET)).orElse(null);
    }

    public UtilityGolem(EntityType<? extends UtilityGolem> type, World world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;
        if (this.golemType != null) {
            this.golemType.initGoals(this);
        }
        updateAttackDamage();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.jukeboxCooldown > 0) {
            this.jukeboxCooldown--;
            if (!this.getEntityWorld().isClient() && this.jukeboxCooldown % 20 == 0) {
                ((net.minecraft.server.world.ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.NOTE, this.getParticleX(0.5D), this.getRandomBodyY() + 0.5D, this.getParticleZ(0.5D), 1, 0, 0, 0, (double)this.random.nextInt(24) / 24.0D);
            }
        }
        if (!this.getEntityWorld().isClient() && this.golemType == GolemType.FURNACE) {
            tickFurnace();
        }
    }

    private void tickFurnace() {
    }

    public void setSearching(boolean searching) {
        if (searching) {
            this.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean canAcceptRecipeOutput(@Nullable net.minecraft.recipe.RecipeEntry<net.minecraft.recipe.SmeltingRecipe> recipe, int maxCount) {
        return false;
    }

    private boolean craftRecipe(@Nullable net.minecraft.recipe.RecipeEntry<net.minecraft.recipe.SmeltingRecipe> recipe, int maxCount) {
        return false;
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    @Override
    public Text getDisplayName() {
        if (this.hasCustomName()) {
            return super.getDisplayName();
        }
        return Text.literal(this.golemType.getFriendlyName());
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack playerStack = player.getStackInHand(hand);
        if (this.golemType == GolemType.LAPIS && isPickaxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.NETHERITE && isSword(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isAxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isShears(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && isHoe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.SPONGE && isFishingRod(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack golemStack = this.getHeldItem();
                ItemStack newStack = playerStack.copy();
                newStack.setCount(1);
                this.setHeldItem(newStack);
                this.equipStack(CopperGolemEntity.POPPY_SLOT, newStack.copy());
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                if (!golemStack.isEmpty()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        if (!player.getEntityWorld().isClient()) {
            if (this.golemType == GolemType.FURNACE) {
                player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) -> new GolemFurnaceScreenHandler(syncId, playerInventory, this.inventory, this.furnacePropertyDelegate, this),
                        this.getDisplayName()
                ));
            } else if (this.golemType == GolemType.JUKEBOX) {
                if (!this.currentlyPlayingStack.isEmpty()) {
                    // Stop playing and drop the disc
                    if (!player.getEntityWorld().isClient()) {
                        player.dropItem(this.currentlyPlayingStack.copy(), false);
                        this.currentlyPlayingStack = ItemStack.EMPTY;
                        this.jukeboxCooldown = 0;
                        // Stop the song on client
                        player.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STOPS_PLAYING, this.getBlockPos(), 0);
                    }
                    return ActionResult.SUCCESS;
                }

                JukeboxPlayableComponent playable = playerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
                if (playable != null) {
                    // Play the music disc
                    if (!player.getEntityWorld().isClient()) {
                        playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                            this.currentlyPlayingStack = playerStack.copy();
                            this.currentlyPlayingStack.setCount(1);
                            
                            // Start the song and show "Now Playing"
                            player.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STARTS_PLAYING, this.getBlockPos(), Item.getRawId(playerStack.getItem()));
                            
                            // Explicitly show "Now Playing" text for the player who interacted
                            player.sendMessage(Text.translatable("record.nowPlaying", songEntry.value().description()), true);
                            
                            // Set cooldown based on song length
                            this.jukeboxCooldown = (int) (songEntry.value().lengthInSeconds() * 20);
                            
                            if (!player.getAbilities().creativeMode) {
                                playerStack.decrement(1);
                            }
                        });
                    }
                    return ActionResult.SUCCESS;
                }
            } else {
                player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) -> new GolemInventoryScreenHandler(syncId, playerInventory, this.inventory, this),
                        this.getDisplayName()
                ));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    public static boolean isPickaxe(ItemStack stack) {
        return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
                stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.DIAMOND_PICKAXE) ||
                stack.isOf(Items.NETHERITE_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE) ||
                stack.isOf(Items.COPPER_PICKAXE);
    }

    public static boolean isSword(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SWORD) || stack.isOf(Items.STONE_SWORD) ||
                stack.isOf(Items.IRON_SWORD) || stack.isOf(Items.DIAMOND_SWORD) ||
                stack.isOf(Items.NETHERITE_SWORD) || stack.isOf(Items.GOLDEN_SWORD) ||
                stack.isOf(Items.COPPER_SWORD);
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.isOf(Items.WOODEN_AXE) || stack.isOf(Items.STONE_AXE) ||
                stack.isOf(Items.IRON_AXE) || stack.isOf(Items.DIAMOND_AXE) ||
                stack.isOf(Items.NETHERITE_AXE) || stack.isOf(Items.GOLDEN_AXE) ||
                stack.isOf(Items.COPPER_AXE);
    }

    public static boolean isHoe(ItemStack stack) {
        return stack.isOf(Items.WOODEN_HOE) || stack.isOf(Items.STONE_HOE) ||
                stack.isOf(Items.IRON_HOE) || stack.isOf(Items.DIAMOND_HOE) ||
                stack.isOf(Items.NETHERITE_HOE) || stack.isOf(Items.GOLDEN_HOE) ||
                stack.isOf(Items.COPPER_HOE);
    }

    public static boolean isFishingRod(ItemStack stack) {
        return stack.isOf(Items.FISHING_ROD);
    }

    public static boolean isShears(ItemStack stack) {
        return stack.isOf(Items.SHEARS);
    }

    private BlockPos chestPos;

    @Override
    public void writeCustomData(net.minecraft.storage.WriteView writeView) {
        super.writeCustomData(writeView);
        net.minecraft.inventory.Inventories.writeData(writeView.get("Inventory"), this.inventory.getHeldStacks());
        if (!this.currentlyPlayingStack.isEmpty()) {
            writeView.put("PlayingDisc", ItemStack.CODEC, this.currentlyPlayingStack);
        }
        writeView.putInt("JukeboxCooldown", this.jukeboxCooldown);
        writeView.putInt("BurnTime", this.burnTime);
        writeView.putInt("FuelTime", this.fuelTime);
        writeView.putInt("CookTime", this.cookTime);
        writeView.putInt("CookTimeTotal", this.cookTimeTotal);
        if (this.chestPos != null) {
            writeView.putInt("ChestX", this.chestPos.getX());
            writeView.putInt("ChestY", this.chestPos.getY());
            writeView.putInt("ChestZ", this.chestPos.getZ());
        }
    }

    @Override
    public void readCustomData(net.minecraft.storage.ReadView readView) {
        super.readCustomData(readView);
        net.minecraft.inventory.Inventories.readData(readView.getReadView("Inventory"), this.inventory.getHeldStacks());
        readView.read("PlayingDisc", ItemStack.CODEC).ifPresent(stack -> this.currentlyPlayingStack = stack);
        this.jukeboxCooldown = readView.getInt("JukeboxCooldown", 0);
        this.burnTime = readView.getInt("BurnTime", 0);
        this.fuelTime = readView.getInt("FuelTime", 0);
        this.cookTime = readView.getInt("CookTime", 0);
        this.cookTimeTotal = readView.getInt("CookTimeTotal", 0);
        if (readView.contains("ChestX")) {
            this.chestPos = new BlockPos(readView.getInt("ChestX", 0), readView.getInt("ChestY", 0), readView.getInt("ChestZ", 0));
        }
        updateAttackDamage();
    }

    public BlockPos getChestPos() {
        return chestPos;
    }

    public void setChestPos(BlockPos chestPos) {
        this.chestPos = chestPos;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);

        // Equip items based on type
        ItemStack item = ItemStack.EMPTY;
        if (golemType == GolemType.REDSTONE) {
            item = new ItemStack(Items.REDSTONE);
        } else if (golemType == GolemType.EMERALD) {
            item = new ItemStack(Items.EMERALD);
        }

        if (!item.isEmpty()) {
            this.equipStack(HELD_ITEM_SLOT, item);
            this.equipStack(CopperGolemEntity.POPPY_SLOT, item);
            updateAttackDamage();
        }

        return data;
    }

    public GolemType getGolemType() {
        return golemType;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new GolemAI.WanderNearChestGoal(this, 1.0D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));

        if (this.golemType != null) {
            this.golemType.initGoals(this);
        }
    }


    public net.minecraft.entity.ai.goal.GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public net.minecraft.entity.ai.goal.GoalSelector getTargetSelector() {
        return this.targetSelector;
    }

    public ItemStack getHeldItem() {
        return this.getEquippedStack(HELD_ITEM_SLOT);
    }

    public void setHeldItem(ItemStack stack) {
        this.equipStack(HELD_ITEM_SLOT, stack);
        updateAttackDamage();
    }

    private void updateAttackDamage() {
        if (this.golemType != GolemType.NETHERITE) return;
        
        float baseDamage = 0.5f; // Default low damage
        ItemStack stack = this.getHeldItem();
        
        if (stack.isOf(Items.NETHERITE_SWORD)) baseDamage += 6.0f;
        else if (stack.isOf(Items.DIAMOND_SWORD)) baseDamage += 5.0f;
        else if (stack.isOf(Items.IRON_SWORD)) baseDamage += 3.0f;
        else if (stack.isOf(Items.STONE_SWORD)) baseDamage += 2.0f;
        else if (stack.isOf(Items.WOODEN_SWORD)) baseDamage += 1.5f;
        else if (stack.isOf(Items.GOLDEN_SWORD)) baseDamage += 4.0f;
        
        var instance = this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.setBaseValue(baseDamage);
        }
    }
}
