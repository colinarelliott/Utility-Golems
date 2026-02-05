package rehdpanda.utilitygolems;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Base class for Utility Golems
public class UtilityGolem extends CopperGolemEntity implements InventoryOwner {

    private final GolemType golemType;
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleInventory inventory = new SimpleInventory(9);
    private final SimpleInventory furnaceInventory = new SimpleInventory(3);
    private final Set<BlockPos> blacklistedPositions = new HashSet<>();
    private int jukeboxCooldown = 0;
    private ItemStack currentlyPlayingStack = ItemStack.EMPTY;
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int cookTimeTotal;
    private BlockPos lastLightPos;

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
    private static final TrackedData<Optional<BlockPos>> DEBUG_TARGET = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Integer> SELECTED_PATTERN = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> WALL_WIDTH = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> WALL_LENGTH = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> BUILDING_STARTED = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LAMP_ON = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> STRIPPED = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FISHING_TARGET, Optional.empty());
        builder.add(DEBUG_TARGET, Optional.empty());
        builder.add(SELECTED_PATTERN, 0);
        builder.add(WALL_WIDTH, 3);
        builder.add(WALL_LENGTH, 3);
        builder.add(BUILDING_STARTED, false);
        builder.add(LAMP_ON, false);
        builder.add(STRIPPED, false);
    }

    public void setBuildingStarted(boolean started) {
        this.dataTracker.set(BUILDING_STARTED, started);
    }

    public boolean isBuildingStarted() {
        return this.dataTracker.get(BUILDING_STARTED);
    }

    public void setLampOn(boolean on) {
        this.dataTracker.set(LAMP_ON, on);
    }

    public boolean isLampOn() {
        return this.dataTracker.get(LAMP_ON);
    }

    public void setStripped(boolean stripped) {
        this.dataTracker.set(STRIPPED, stripped);
    }

    public boolean isStripped() {
        return this.dataTracker.get(STRIPPED);
    }

    public void setBuildPattern(BuildPattern pattern) {
        this.dataTracker.set(SELECTED_PATTERN, pattern.ordinal());
    }

    public BuildPattern getBuildPattern() {
        return BuildPattern.values()[this.dataTracker.get(SELECTED_PATTERN)];
    }

    public void setWallWidth(int width) {
        this.dataTracker.set(WALL_WIDTH, width);
    }

    public int getWallWidth() {
        return this.dataTracker.get(WALL_WIDTH);
    }

    public void setWallLength(int length) {
        this.dataTracker.set(WALL_LENGTH, length);
    }

    public int getWallLength() {
        return this.dataTracker.get(WALL_LENGTH);
    }

    public void setFishingTarget(@Nullable BlockPos pos) {
        this.dataTracker.set(FISHING_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getFishingTarget() {
        return (BlockPos)((Optional)this.dataTracker.get(FISHING_TARGET)).orElse(null);
    }

    public void setDebugTarget(@Nullable BlockPos pos) {
        this.dataTracker.set(DEBUG_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getDebugTarget() {
        return (BlockPos)((Optional)this.dataTracker.get(DEBUG_TARGET)).orElse(null);
    }

    public boolean isBlacklisted(BlockPos pos) {
        return blacklistedPositions.contains(pos);
    }

    public void blacklistPosition(BlockPos pos) {
        blacklistedPositions.add(pos);
    }

    public void clearBlacklist() {
        blacklistedPositions.clear();
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
    public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        super.onDeath(source);
        if (!this.getEntityWorld().isClient()) {
            removeLight();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = this.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    net.minecraft.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), stack.copy());
                }
            }
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.getStack(i);
                if (!stack.isEmpty()) {
                    net.minecraft.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), stack.copy());
                    this.inventory.setStack(i, ItemStack.EMPTY);
                }
            }
            for (int i = 0; i < this.furnaceInventory.size(); i++) {
                ItemStack stack = this.furnaceInventory.getStack(i);
                if (!stack.isEmpty()) {
                    net.minecraft.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), stack.copy());
                    this.furnaceInventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            boolean isDebug = this.hasCustomName() && this.getCustomName().getString().equalsIgnoreCase("debug");
            if (this.isGlowing() != isDebug) {
                this.setGlowing(isDebug);
            }
        }

        if (this.jukeboxCooldown > 0) {
            this.jukeboxCooldown--;
            if (this.jukeboxCooldown == 0 && !this.currentlyPlayingStack.isEmpty()) {
                if (!this.getEntityWorld().isClient()) {
                    this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STOPS_PLAYING, this.getBlockPos(), 0);
                    
                    // Stop the music sound if it was playing via playSound
                    this.currentlyPlayingStack.get(DataComponentTypes.JUKEBOX_PLAYABLE).song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                        // Unfortunately there is no easy stopSound on World, but typically music discs are handled by JUKEBOX_STOPS_PLAYING event on client
                        // However since we added a manual playSound, we should ensure it stops. 
                        // Actually, playSound for RECORDS category might not be easily stoppable from server without a specific packet.
                        // But JUKEBOX_STOPS_PLAYING should stop all records at that position on the client.
                    });

                    if (this.golemType == GolemType.JUKEBOX) {
                        PlayerEntity player = this.getEntityWorld().getClosestPlayer(this, 10.0D);
                        if (player != null) {
                            player.dropItem(this.currentlyPlayingStack.copy(), false);
                        } else {
                            this.getEntityWorld().spawnEntity(new net.minecraft.entity.ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), this.currentlyPlayingStack.copy()));
                        }
                        this.setHeldItem(ItemStack.EMPTY);
                        this.setSearching(false);
                    }
                    this.currentlyPlayingStack = ItemStack.EMPTY;
                }
            }
            if (!this.getEntityWorld().isClient() && this.jukeboxCooldown % 20 == 0 && this.jukeboxCooldown > 0) {
                ((net.minecraft.server.world.ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.NOTE, this.getParticleX(0.5D), this.getRandomBodyY() + 0.5D, this.getParticleZ(0.5D), 1, 0, 0, 0, (double)this.random.nextInt(24) / 24.0D);
            }
        }
        if (!this.getEntityWorld().isClient()) {
            if (this.golemType == GolemType.FURNACE) {
                tickFurnace();
            }
            if (this.golemType == GolemType.GOLD) {
                tickGold();
            }
            if (this.golemType == GolemType.JUKEBOX) {
                tickJukebox();
            }
            if (this.golemType == GolemType.LAMP) {
                tickLamp();
            }
        }
    }

    private void tickLamp() {
        if (this.getEntityWorld().isClient()) return;
        
        boolean isLampOn = this.isLampOn();
        if (isLampOn) {
            updateLightEmission(12);
        } else {
            stopLightEmission();
        }
    }

    private void updateLightEmission(int lightLevel) {
        BlockPos currentPos = this.getBlockPos().up();
        if (lastLightPos == null || !lastLightPos.equals(currentPos) || !this.getEntityWorld().getBlockState(lastLightPos).isOf(UGBlocks.LIGHT_BLOCK) || this.getEntityWorld().getBlockState(lastLightPos).get(LightBlock.LEVEL) != lightLevel) {
            removeLight();
            if (this.getEntityWorld().getBlockState(currentPos).isReplaceable()) {
                this.getEntityWorld().setBlockState(currentPos, UGBlocks.LIGHT_BLOCK.getDefaultState().with(LightBlock.LEVEL, lightLevel));
                lastLightPos = currentPos;
            }
        }
    }

    private void stopLightEmission() {
        if (lastLightPos == null) return;
        removeLight();
    }

    private void removeLight() {
        if (lastLightPos != null) {
            if (this.getEntityWorld().getBlockState(lastLightPos).isOf(UGBlocks.LIGHT_BLOCK)) {
                this.getEntityWorld().setBlockState(lastLightPos, net.minecraft.block.Blocks.AIR.getDefaultState());
            }
            lastLightPos = null;
        }
    }

    @Override
    public void remove(net.minecraft.entity.Entity.RemovalReason reason) {
        if (!this.getEntityWorld().isClient()) {
            removeLight();
        }
        super.remove(reason);
    }

    @Override
    public boolean isClimbing() {
        return super.isClimbing() || this.getEntityWorld().getBlockState(this.getBlockPos()).isIn(net.minecraft.registry.tag.BlockTags.CLIMBABLE);
    }

    private void tickFurnace() {
        if (this.getEntityWorld().isClient()) return;

        boolean wasBurning = this.burnTime > 0;

        if (this.burnTime > 0) {
            --this.burnTime;
        }

        ItemStack inputStack = this.furnaceInventory.getStack(0);
        ItemStack fuelStack = this.furnaceInventory.getStack(1);
        boolean hasInput = !inputStack.isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();

        if (this.burnTime > 0 || (hasFuel && hasInput)) {
            if (this.burnTime <= 0 && hasInput && isFuel(fuelStack)) {
                this.burnTime = getFuelTime(fuelStack);
                this.fuelTime = this.burnTime;
                if (this.burnTime > 0) {
                    if (fuelStack.isOf(Items.LAVA_BUCKET)) {
                        this.furnaceInventory.setStack(1, new ItemStack(Items.BUCKET));
                    } else {
                        fuelStack.decrement(1);
                    }
                }
            }

            if (this.burnTime > 0 && hasInput) {
                // Progress cook time
                this.cookTimeTotal = 200;
                this.cookTime++;
                if (this.cookTime >= this.cookTimeTotal) {
                    this.cookTime = 0;
                    smeltItem();
                }
            } else {
                this.cookTime = 0;
            }
        } else if (this.burnTime <= 0 && this.cookTime > 0) {
            this.cookTime = Math.max(0, this.cookTime - 2);
        }

        if (wasBurning != this.burnTime > 0) {
            // Updated
        }

        if (this.burnTime > 0) {
            updateLightEmission(6);
        } else {
            stopLightEmission();
        }
    }

    private void smeltItem() {
        ItemStack input = this.furnaceInventory.getStack(0);
        ItemStack result = getSmeltingResult(input);
        if (result.isEmpty()) return;

        ItemStack output = this.furnaceInventory.getStack(2);
        if (output.isEmpty()) {
            this.furnaceInventory.setStack(2, result.copy());
            input.decrement(1);
        } else if (output.isOf(result.getItem()) && output.getCount() < output.getMaxCount()) {
            output.increment(1);
            input.decrement(1);
        }
    }

    private ItemStack getSmeltingResult(ItemStack input) {
        if (input.isOf(Items.RAW_IRON) || input.isOf(Items.IRON_ORE) || input.isOf(Items.DEEPSLATE_IRON_ORE)) return new ItemStack(Items.IRON_INGOT);
        if (input.isOf(Items.RAW_GOLD) || input.isOf(Items.GOLD_ORE) || input.isOf(Items.DEEPSLATE_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
        if (input.isOf(Items.RAW_COPPER) || input.isOf(Items.COPPER_ORE) || input.isOf(Items.DEEPSLATE_COPPER_ORE)) return new ItemStack(Items.COPPER_INGOT);
        if (input.isOf(Items.COBBLESTONE)) return new ItemStack(Items.STONE);
        if (input.isOf(Items.STONE)) return new ItemStack(Items.SMOOTH_STONE);
        if (input.isOf(Items.SAND)) return new ItemStack(Items.GLASS);
        if (input.isOf(Items.RED_SAND)) return new ItemStack(Items.GLASS);
        if (input.isOf(Items.OAK_LOG) || input.isOf(Items.SPRUCE_LOG) || input.isOf(Items.BIRCH_LOG) || input.isOf(Items.JUNGLE_LOG) || input.isOf(Items.ACACIA_LOG) || input.isOf(Items.DARK_OAK_LOG) || input.isOf(Items.MANGROVE_LOG) || input.isOf(Items.CHERRY_LOG) || input.isOf(Items.BAMBOO_BLOCK)) return new ItemStack(Items.CHARCOAL);
        if (input.isOf(Items.OAK_WOOD) || input.isOf(Items.SPRUCE_WOOD) || input.isOf(Items.BIRCH_WOOD) || input.isOf(Items.JUNGLE_WOOD) || input.isOf(Items.ACACIA_WOOD) || input.isOf(Items.DARK_OAK_WOOD) || input.isOf(Items.MANGROVE_WOOD) || input.isOf(Items.CHERRY_WOOD)) return new ItemStack(Items.CHARCOAL);
        if (input.isOf(Items.STRIPPED_OAK_LOG) || input.isOf(Items.STRIPPED_SPRUCE_LOG) || input.isOf(Items.STRIPPED_BIRCH_LOG) || input.isOf(Items.STRIPPED_JUNGLE_LOG) || input.isOf(Items.STRIPPED_ACACIA_LOG) || input.isOf(Items.STRIPPED_DARK_OAK_LOG) || input.isOf(Items.STRIPPED_MANGROVE_LOG) || input.isOf(Items.STRIPPED_CHERRY_LOG)) return new ItemStack(Items.CHARCOAL);
        if (input.isOf(Items.STRIPPED_OAK_WOOD) || input.isOf(Items.STRIPPED_SPRUCE_WOOD) || input.isOf(Items.STRIPPED_BIRCH_WOOD) || input.isOf(Items.STRIPPED_JUNGLE_WOOD) || input.isOf(Items.STRIPPED_ACACIA_WOOD) || input.isOf(Items.STRIPPED_DARK_OAK_WOOD) || input.isOf(Items.STRIPPED_MANGROVE_WOOD) || input.isOf(Items.STRIPPED_CHERRY_WOOD)) return new ItemStack(Items.CHARCOAL);
        if (input.isOf(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP);
        if (input.isOf(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF);
        if (input.isOf(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN);
        if (input.isOf(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON);
        if (input.isOf(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT);
        if (input.isOf(Items.COD)) return new ItemStack(Items.COOKED_COD);
        if (input.isOf(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON);
        if (input.isOf(Items.POTATO)) return new ItemStack(Items.BAKED_POTATO);
        if (input.isOf(Items.KELP)) return new ItemStack(Items.DRIED_KELP);
        if (input.isOf(Items.CLAY_BALL)) return new ItemStack(Items.BRICK);
        if (input.isOf(Items.CLAY)) return new ItemStack(Items.TERRACOTTA);
        if (input.isOf(Items.CACTUS)) return new ItemStack(Items.GREEN_DYE);
        if (input.isOf(Items.NETHERRACK)) return new ItemStack(Items.NETHER_BRICK);
        return ItemStack.EMPTY;
    }

    private boolean isFuel(ItemStack stack) {
        return stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL) || stack.isOf(Items.BLAZE_ROD) || stack.isOf(Items.LAVA_BUCKET);
    }

    private int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (fuel.isOf(Items.COAL)) return 1600;
        if (fuel.isOf(Items.CHARCOAL)) return 1600;
        if (fuel.isOf(Items.BLAZE_ROD)) return 2400;
        if (fuel.isOf(Items.LAVA_BUCKET)) return 20000;
        return 0;
    }

    private void tickJukebox() {
        if (this.jukeboxCooldown == 0 && this.currentlyPlayingStack.isEmpty()) {
            SimpleInventory inv = this.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty()) {
                    JukeboxPlayableComponent playable = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
                    if (playable != null) {
                        playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                            this.currentlyPlayingStack = stack.copy();
                            this.currentlyPlayingStack.setCount(1);
                            stack.decrement(1);
                            this.jukeboxCooldown = (int) (songEntry.value().lengthInSeconds() * 20);
                            
                            if (!this.getEntityWorld().isClient()) {
                                this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STARTS_PLAYING, this.getBlockPos(), Item.getRawId(this.currentlyPlayingStack.getItem()));
                                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), songEntry.value().soundEvent().value(), SoundCategory.RECORDS, 3.0F, 1.0F);
                            }

                            // Equipping the record so it's visible and might help with client-side playing
                            this.setHeldItem(this.currentlyPlayingStack.copy());
                            this.setSearching(true);
                        });
                        break;
                    }
                }
            }
        }
    }

    private void tickGold() {
        if (this.age % 20 == 0) {
            SimpleInventory inv = this.getInventory();
            int nuggetCount = 0;
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.GOLD_NUGGET)) {
                    nuggetCount += stack.getCount();
                }
            }

            if (nuggetCount >= 9) {
                int toConsume = 9;
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isOf(Items.GOLD_NUGGET)) {
                        int amount = Math.min(toConsume, stack.getCount());
                        stack.decrement(amount);
                        toConsume -= amount;
                        if (toConsume <= 0) break;
                    }
                }
                ItemStack ingot = new ItemStack(Items.GOLD_INGOT);
                ItemStack remaining = inv.addStack(ingot);
                if (!remaining.isEmpty()) {
                    this.getEntityWorld().spawnEntity(new net.minecraft.entity.ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), remaining));
                }
            }
        }
    }

    public void setSearching(boolean searching) {
        if (searching) {
            this.swingHand(Hand.MAIN_HAND);
        }
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
        if (this.golemType == GolemType.BAMBOO && !this.isStripped() && isAxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                this.setStripped(true);
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                if (!player.getAbilities().creativeMode) {
                    playerStack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAPIS && isPickaxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.NETHERITE && isSword(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isAxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isShears(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && isHoe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.SPONGE && isFishingRod(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAMP && isTorch(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return ActionResult.SUCCESS;
        }

        if (playerStack.isOf(UGItems.WRENCH_ITEM)) {
            float currentHealth = this.getHealth();
            float maxHealth = this.getMaxHealth();
            if (currentHealth < maxHealth) {
                if (!player.getEntityWorld().isClient()) {
                    this.heal(maxHealth * 0.25f); // Heal 25% of max health
                    if (!player.getAbilities().creativeMode) {
                        playerStack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    for (int i = 0; i < 7; ++i) {
                        double d = this.random.nextGaussian() * 0.02;
                        double e = this.random.nextGaussian() * 0.02;
                        double f = this.random.nextGaussian() * 0.02;
                        ((net.minecraft.server.world.ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 1, d, e, f, 0.0);
                    }
                    player.sendMessage(Text.literal(this.golemType.getFriendlyName() + " Health: " + (int)this.getHealth() + "/" + (int)maxHealth), true);
                }
                return ActionResult.SUCCESS;
            } else {
                if (!player.getEntityWorld().isClient()) {
                    player.sendMessage(Text.literal(this.golemType.getFriendlyName() + " is already at full health (" + (int)maxHealth + "/" + (int)maxHealth + ")"), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        if (this.golemType == GolemType.FURNACE) {
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) -> new GolemFurnaceScreenHandler(syncId, playerInventory, this.furnaceInventory, this.furnacePropertyDelegate, this),
                        this.getDisplayName()
                ));
            }
            return ActionResult.SUCCESS;
        } else if (this.golemType == GolemType.LAMP) {
            if (player.isSneaking()) {
                if (!player.getEntityWorld().isClient()) {
                    this.setLampOn(!this.isLampOn());
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 0.5F, 1.2F);
                }
                return ActionResult.SUCCESS;
            }
        }

        if (!player.getEntityWorld().isClient()) {
            if (this.golemType == GolemType.JUKEBOX) {
                if (!this.currentlyPlayingStack.isEmpty()) {
                    player.dropItem(this.currentlyPlayingStack.copy(), false);
                    this.currentlyPlayingStack = ItemStack.EMPTY;
                    this.jukeboxCooldown = 0;
                    this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STOPS_PLAYING, this.getBlockPos(), 0);
                    this.setHeldItem(ItemStack.EMPTY);
                    this.setSearching(false);
                    return ActionResult.SUCCESS;
                }

                JukeboxPlayableComponent playable = playerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
                if (playable != null) {
                    playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                        this.currentlyPlayingStack = playerStack.copy();
                        this.currentlyPlayingStack.setCount(1);
                        player.sendMessage(Text.translatable("record.nowPlaying", songEntry.value().description()), true);
                        this.jukeboxCooldown = (int) (songEntry.value().lengthInSeconds() * 20);
                        
                        this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STARTS_PLAYING, this.getBlockPos(), Item.getRawId(this.currentlyPlayingStack.getItem()));
                        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), songEntry.value().soundEvent().value(), SoundCategory.RECORDS, 3.0F, 1.0F);
                        
                        this.setHeldItem(this.currentlyPlayingStack.copy());
                        this.setSearching(true);

                        if (!player.getAbilities().creativeMode) {
                            playerStack.decrement(1);
                        }
                    });
                    return ActionResult.SUCCESS;
                }
            } else {
                player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player) {
                        return UtilityGolem.this.getId();
                    }

                    @Override
                    public Text getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return new GolemInventoryScreenHandler(syncId, playerInventory, UtilityGolem.this.inventory, UtilityGolem.this);
                    }
                });
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    private void swapTool(PlayerEntity player, ItemStack playerStack) {
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

    public static boolean isTorch(ItemStack stack) {
        return stack.isOf(Items.TORCH) || stack.isOf(Items.SOUL_TORCH) || stack.isOf(Items.REDSTONE_TORCH);
    }

    public static boolean isShovel(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SHOVEL) || stack.isOf(Items.STONE_SHOVEL) ||
                stack.isOf(Items.IRON_SHOVEL) || stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL) || stack.isOf(Items.GOLDEN_SHOVEL) ||
                stack.isOf(Items.COPPER_SHOVEL);
    }

    private BlockPos chestPos;

    @Override
    public void writeCustomData(net.minecraft.storage.WriteView writeView) {
        super.writeCustomData(writeView);
        writeView.putInt("BuildPattern", this.getBuildPattern().ordinal());
        writeView.putInt("WallWidth", this.getWallWidth());
        writeView.putInt("WallLength", this.getWallLength());
        writeView.putBoolean("BuildingStarted", this.isBuildingStarted());
        writeView.putBoolean("LampOn", this.isLampOn());
        writeView.putBoolean("Stripped", this.isStripped());
        net.minecraft.inventory.Inventories.writeData(writeView.get("Inventory"), this.inventory.getHeldStacks());
        net.minecraft.inventory.Inventories.writeData(writeView.get("FurnaceInventory"), this.furnaceInventory.getHeldStacks());
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
        this.setBuildPattern(BuildPattern.values()[readView.getInt("BuildPattern", 0)]);
        this.setWallWidth(readView.getInt("WallWidth", 3));
        this.setWallLength(readView.getInt("WallLength", 3));
        this.setBuildingStarted(readView.getBoolean("BuildingStarted", false));
        this.setLampOn(readView.getBoolean("LampOn", false));
        this.setStripped(readView.getBoolean("Stripped", false));
        net.minecraft.inventory.Inventories.readData(readView.getReadView("Inventory"), this.inventory.getHeldStacks());
        net.minecraft.inventory.Inventories.readData(readView.getReadView("FurnaceInventory"), this.furnaceInventory.getHeldStacks());
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
    protected EntityNavigation createNavigation(World world) {
        MobNavigation mobNavigation = new MobNavigation(this, world);
        // mobNavigation.setCanPathThroughDoors(true);
        return mobNavigation;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new SwimGoal(this)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new net.minecraft.entity.ai.goal.LongDoorInteractGoal(this, true)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new GolemAI.ClimbLadderGoal(this)));

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
