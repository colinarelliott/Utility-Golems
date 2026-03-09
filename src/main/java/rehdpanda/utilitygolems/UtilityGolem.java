package rehdpanda.utilitygolems;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.tag.BlockTags;
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

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Base class for Utility Golems
public class UtilityGolem extends CopperGolemEntity implements InventoryOwner {

    private final GolemType golemType;
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleInventory inventory = new SimpleInventory(9);
    private final SimpleInventory furnaceInventory = new SimpleInventory(3);
    private final SimpleInventory jukeboxInventory = new SimpleInventory(9);
    private final Set<BlockPos> blacklistedPositions = new HashSet<>();
    private int jukeboxCooldown = 0;
    private BlockPos jukeboxStartPos = null;
    private ItemStack currentlyPlayingStack = ItemStack.EMPTY;
    private int currentJukeboxSlot = -1;
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int cookTimeTotal;
    private BlockPos lastLightPos;

    private final net.minecraft.screen.PropertyDelegate furnacePropertyDelegate = new net.minecraft.screen.PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> UtilityGolem.this.burnTime;
                case 1 -> UtilityGolem.this.fuelTime;
                case 2 -> UtilityGolem.this.cookTime;
                case 3 -> UtilityGolem.this.cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> UtilityGolem.this.burnTime = value;
                case 1 -> UtilityGolem.this.fuelTime = value;
                case 2 -> UtilityGolem.this.cookTime = value;
                case 3 -> UtilityGolem.this.cookTimeTotal = value;
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
    private static final TrackedData<ItemStack> SELECTED_BUY_ITEM = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<String> SCHEMATIC_NAME = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> MINING_DIRECTION = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> SMELTING = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> JUKEBOX_PLAYING = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> JUKEBOX_SHUFFLE = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> JUKEBOX_REPEAT = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);

    // Animation state syncing (server -> client)
    private static final TrackedData<Integer> ANIMATION_ID = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANIMATION_TICKS = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANIMATION_START_TICKS = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> REDSTONE_PROGRAM_STARTED = DataTracker.registerData(UtilityGolem.class, TrackedDataHandlerRegistry.BOOLEAN);

    public record RedstoneInteraction(BlockPos pos, int interval) {}
    private final List<RedstoneInteraction> redstoneProgram = new ArrayList<>();
    private int currentInteractionIndex = 0;
    private int redstoneTickCounter = 0;

    public List<RedstoneInteraction> getRedstoneProgram() {
        return redstoneProgram;
    }

    public void setRedstoneProgram(List<RedstoneInteraction> program) {
        this.redstoneProgram.clear();
        this.redstoneProgram.addAll(program);
        if (currentInteractionIndex >= this.redstoneProgram.size()) {
            currentInteractionIndex = 0;
        }
    }

    public boolean isRedstoneProgramStarted() {
        return this.dataTracker.get(REDSTONE_PROGRAM_STARTED);
    }

    public void setRedstoneProgramStarted(boolean started) {
        this.dataTracker.set(REDSTONE_PROGRAM_STARTED, started);
        if (started) {
            this.redstoneTickCounter = 0;
        }
    }

    public int getCurrentInteractionIndex() {
        return currentInteractionIndex;
    }

    public void setCurrentInteractionIndex(int index) {
        this.currentInteractionIndex = index;
    }

    public int getRedstoneTickCounter() {
        return redstoneTickCounter;
    }

    public void setRedstoneTickCounter(int counter) {
        this.redstoneTickCounter = counter;
    }

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
        builder.add(SELECTED_BUY_ITEM, ItemStack.EMPTY);
        builder.add(SCHEMATIC_NAME, "");
        builder.add(MINING_DIRECTION, -1);
        builder.add(SMELTING, false);
        builder.add(JUKEBOX_PLAYING, false);
        builder.add(JUKEBOX_SHUFFLE, false);
        builder.add(JUKEBOX_REPEAT, false);
        builder.add(ANIMATION_ID, GolemAnimation.IDLE.ordinal());
        builder.add(ANIMATION_TICKS, 0);
        builder.add(ANIMATION_START_TICKS, 0);
        builder.add(REDSTONE_PROGRAM_STARTED, false);
    }

    public GolemAnimation getAnimation() {
        int id = this.dataTracker.get(ANIMATION_ID);
        if (id < 0 || id >= GolemAnimation.values().length) return GolemAnimation.IDLE;
        return GolemAnimation.values()[id];
    }

    public void setAnimation(GolemAnimation animation, int durationTicks) {
        this.dataTracker.set(ANIMATION_ID, animation == null ? GolemAnimation.IDLE.ordinal() : animation.ordinal());
        this.dataTracker.set(ANIMATION_TICKS, Math.max(0, durationTicks));
        this.dataTracker.set(ANIMATION_START_TICKS, Math.max(1, durationTicks));

        // Sync with CopperGolemEntity states for built-in animations
        if (animation == GolemAnimation.SPINNING_HEAD) {
            this.setState(net.minecraft.entity.passive.CopperGolemState.IDLE);
            // Trigger the spin head animation by setting the age to match the timer
            // Since we can't set the private field, we just let it happen naturally if possible,
            // or we might need a mixin if we really want to force it.
            // But wait, CopperGolemEntity's clientTick handles it.
        } else if (animation == GolemAnimation.DEPOSITING) {
            this.setState(net.minecraft.entity.passive.CopperGolemState.DROPPING_ITEM);
        } else if (animation == GolemAnimation.WITHDRAWING) {
            this.setState(net.minecraft.entity.passive.CopperGolemState.GETTING_ITEM);
        } else if (animation == GolemAnimation.PRESSING_BUTTON) {
            // No longer mapping to DROPPING_ITEM to allow custom rendering
            this.setState(net.minecraft.entity.passive.CopperGolemState.IDLE);
        } else if (animation == GolemAnimation.IDLE) {
            this.setState(net.minecraft.entity.passive.CopperGolemState.IDLE);
        }
    }

    public int getAnimationTicks() {
        return this.dataTracker.get(ANIMATION_TICKS);
    }

    public float getAnimationProgress(float tickDelta) {
        int ticks = getAnimationTicks();
        if (ticks <= 0) return 0f;
        
        GolemAnimation anim = getAnimation();
        if (anim == GolemAnimation.DIGGING || anim == GolemAnimation.CHOPPING || anim == GolemAnimation.FARMING || 
            anim == GolemAnimation.FISHING || anim == GolemAnimation.PLAYING_MUSIC) {
            // Loop every 20 ticks
            return ((this.getEntityWorld().getTime() % 20) + tickDelta) / 20.0f;
        }

        // Normalize into [0..1], counting down
        int startTicks = this.dataTracker.get(ANIMATION_START_TICKS);
        return Math.max(0f, Math.min(1f, 1f - (ticks - tickDelta) / Math.max(1f, (float)startTicks)));
    }

    public void debugLog(String message) {
        if (this.hasCustomName()) {
            Text customName = this.getCustomName();
            if (customName != null && customName.getString().toUpperCase().contains("DEBUG")) {
                World world = this.getEntityWorld();
                if (!world.isClient()) {
                    List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0D), player -> true);
                    for (PlayerEntity player : players) {
                        player.sendMessage(Text.literal("[DEBUG] " + message), false);
                    }
                }
            }
        }
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
    
    public boolean isSmelting() {
        return this.dataTracker.get(SMELTING);
    }

    public boolean isJukeboxPlaying() {
        return this.dataTracker.get(JUKEBOX_PLAYING);
    }

    public void setJukeboxPlaying(boolean playing) {
        this.dataTracker.set(JUKEBOX_PLAYING, playing);
    }

    public boolean isJukeboxShuffle() {
        return this.dataTracker.get(JUKEBOX_SHUFFLE);
    }

    public void setJukeboxShuffle(boolean shuffle) {
        this.dataTracker.set(JUKEBOX_SHUFFLE, shuffle);
    }

    public boolean isJukeboxRepeat() {
        return this.dataTracker.get(JUKEBOX_REPEAT);
    }

    public void setJukeboxRepeat(boolean repeat) {
        this.dataTracker.set(JUKEBOX_REPEAT, repeat);
    }

    public SimpleInventory getJukeboxInventory() {
        return jukeboxInventory;
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

    public void setSelectedBuyItem(ItemStack stack) {
        this.dataTracker.set(SELECTED_BUY_ITEM, stack);
    }

    public ItemStack getSelectedBuyItem() {
        return this.dataTracker.get(SELECTED_BUY_ITEM);
    }

    public void setSchematicName(String name) {
        this.dataTracker.set(SCHEMATIC_NAME, name == null ? "" : name);
    }

    public String getSchematicName() {
        return this.dataTracker.get(SCHEMATIC_NAME);
    }

    public void setMiningDirection(@Nullable net.minecraft.util.math.Direction direction) {
        this.dataTracker.set(MINING_DIRECTION, direction == null ? -1 : direction.ordinal());
    }

    @Nullable
    public net.minecraft.util.math.Direction getMiningDirection() {
        int ordinal = this.dataTracker.get(MINING_DIRECTION);
        return ordinal == -1 ? null : net.minecraft.util.math.Direction.values()[ordinal];
    }

    private final List<ItemStack> discoveredTrades = new ArrayList<>();

    public List<ItemStack> getDiscoveredTrades() {
        return discoveredTrades;
    }

    public void addDiscoveredTrade(ItemStack stack) {
        for (ItemStack s : discoveredTrades) {
            if (ItemStack.areItemsEqual(s, stack)) return;
        }
        discoveredTrades.add(stack.copy());
        if (!this.getEntityWorld().isClient()) {
            syncDiscoveredTrades();
        }
    }

    public void syncDiscoveredTrades() {
        if (this.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld) {
            UGInit.syncDiscoveredTrades(this);
        }
    }

    public void setFishingTarget(@Nullable BlockPos pos) {
        this.dataTracker.set(FISHING_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getFishingTarget() {
        return this.dataTracker.get(FISHING_TARGET).orElse(null);
    }

    public void setDebugTarget(@Nullable BlockPos pos) {
        this.dataTracker.set(DEBUG_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getDebugTarget() {
        return this.dataTracker.get(DEBUG_TARGET).orElse(null);
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

    public BlockPos getLastLightPos() {
        return lastLightPos;
    }

    public UtilityGolem(EntityType<? extends UtilityGolem> type, World world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;
        initGolemsGoals();
        updateAttackDamage();
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        super.onDeath(source);
        if (!this.getEntityWorld().isClient()) {
            removeLight();

            if (this.golemType == GolemType.JUKEBOX) {
                BlockPos stopPos = this.jukeboxStartPos != null ? this.jukeboxStartPos : this.getBlockPos();
                this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STOPS_PLAYING, stopPos, 0);

                if (!this.currentlyPlayingStack.isEmpty()) {
                    JukeboxPlayableComponent playable = this.currentlyPlayingStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
                    if (playable != null) {
                        playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                            stopMusicSound();
                        });
                    }
                }

                this.currentlyPlayingStack = ItemStack.EMPTY;
                this.jukeboxCooldown = 0;
                this.jukeboxStartPos = null;
            }

            // CopperGolemEntity already drops what is in its POPPY_SLOT, but our golem
            // uses HELD_ITEM_SLOT (MAINHAND). We must drop it manually.
            ItemStack heldItem = this.getHeldItem();
            if (!heldItem.isEmpty()) {
                net.minecraft.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), heldItem.copy());
                this.setHeldItem(ItemStack.EMPTY);
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
    protected void mobTick(net.minecraft.server.world.ServerWorld world) {
        // Skip CopperGolemEntity.mobTick which triggers brain.tick and CopperGolemBrain.updateActivity
        // This ensures the vanilla copper golem brain never runs
        net.minecraft.util.profiler.Profiler profiler = net.minecraft.util.profiler.Profilers.get();
        profiler.push("utilityGolemTick");
        // Custom golem logic can be added here if needed, but currently it's in tick() and goals
        profiler.pop();
        // Skip CopperGolemEntity's mobTick entirely
    }

    @Override
    protected net.minecraft.entity.ai.brain.Brain.Profile<net.minecraft.entity.passive.CopperGolemEntity> createBrainProfile() {
        // Return an empty brain profile to ensure the copper golem brain is never initialized
        return net.minecraft.entity.ai.brain.Brain.createProfile(java.util.Collections.emptyList(), java.util.Collections.emptyList());
    }

    @Override
    protected net.minecraft.entity.ai.brain.Brain<?> deserializeBrain(com.mojang.serialization.Dynamic<?> dynamic) {
        // Return an empty brain to bypass any brain-based AI from CopperGolemEntity
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public net.minecraft.entity.ai.brain.Brain<net.minecraft.entity.passive.CopperGolemEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    public void tick() {
        if (this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) {
            // Suppression of CopperGolemEntity behaviors is handled by disabling POPPY_SLOT 
            // and clearing goals in initGoals.
        }
        // Proactively prevent any vanilla CopperGolem container targeting each tick (server-side)
        if (!this.getEntityWorld().isClient()) {
            this.resetTargetContainerPos();
        }

        super.tick();

        if (!this.getEntityWorld().isClient()) {
            Text customName = this.getCustomName();
            boolean isDebug = this.hasCustomName() && customName != null && customName.getString().equalsIgnoreCase("debug");
            if (this.isGlowing() != isDebug) {
                this.setGlowing(isDebug);
            }
        }

        if (this.jukeboxCooldown > 0) {
            this.jukeboxCooldown--;
            if (this.jukeboxCooldown == 0 && !this.currentlyPlayingStack.isEmpty()) {
                if (!this.getEntityWorld().isClient()) {
                    BlockPos stopPos = this.jukeboxStartPos != null ? this.jukeboxStartPos : this.getBlockPos();
                    this.getEntityWorld().syncWorldEvent(null, WorldEvents.JUKEBOX_STOPS_PLAYING, stopPos, 0);
                    
                    // Stop the music sound if it was playing via playSound
                    JukeboxPlayableComponent playable = this.currentlyPlayingStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
                    if (playable != null) {
                        playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                            stopMusicSound();
                        });
                    }

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
                    this.jukeboxStartPos = null;
                }
            }
            if (!this.getEntityWorld().isClient() && this.jukeboxCooldown % 20 == 0 && this.jukeboxCooldown > 0) {
                ((net.minecraft.server.world.ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.NOTE, this.getParticleX(0.5D), this.getRandomBodyY() + 0.5D, this.getParticleZ(0.5D), 1, 0, 0, 0, (double)this.random.nextInt(24) / 24.0D);
            }
        }
        if (!this.getEntityWorld().isClient()) {
            // Update animation timer server-side
            int t = this.dataTracker.get(ANIMATION_TICKS);
            if (t > 0) {
                this.dataTracker.set(ANIMATION_TICKS, t - 1);
                if (t - 1 == 0) {
                    // Reset to idle when finished
                    this.setAnimation(GolemAnimation.IDLE, 0);
                }
            }

            if (this.golemType == GolemType.FURNACE) {
                tickFurnace();
            }
            if (this.golemType == GolemType.GOLD) {
                tickGold();
            }
            if (this.golemType == GolemType.JUKEBOX) {
                tickJukebox();
                // Ensure music animation stays active if music is playing
                if (!this.currentlyPlayingStack.isEmpty() && (this.getAnimation() == GolemAnimation.IDLE || this.getAnimationTicks() <= 1)) {
                    this.setAnimation(GolemAnimation.PLAYING_MUSIC, 60);
                }
            }
            if (this.golemType == GolemType.LAMP) {
                tickLamp();
            }

            // Occasionally spin head when idle
            if (this.getAnimation() == GolemAnimation.IDLE && this.random.nextInt(200) == 0) {
                this.setAnimation(GolemAnimation.SPINNING_HEAD, 60);
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
            if (this.burnTime <= 0 && isFuel(fuelStack)) {
                if (!getSmeltingResult(inputStack).isEmpty()) {
                    this.burnTime = getFuelTime(fuelStack);
                    this.fuelTime = this.burnTime;
                    if (this.burnTime > 0) {
                        Item item = fuelStack.getItem();
                        fuelStack.decrement(1);
                        if (fuelStack.isEmpty()) {
                            ItemStack itemRemainder = item.getRecipeRemainder();
                            this.furnaceInventory.setStack(1, itemRemainder);
                        }
                    }
                }
            }

            if (this.burnTime > 0) {
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
        } else if (this.cookTime > 0) {
            this.cookTime = Math.max(0, this.cookTime - 2);
        }

        if (wasBurning != this.burnTime > 0) {
            this.dataTracker.set(SMELTING, this.burnTime > 0);
        }

        if (this.burnTime > 0 && !getSmeltingResult(this.furnaceInventory.getStack(0)).isEmpty()) {
            updateLightEmission(6);
            if (this.getAnimation() == GolemAnimation.IDLE || this.getAnimationTicks() <= 1) {
                this.setAnimation(GolemAnimation.SMELTING, 40);
            }
        } else {
            stopLightEmission();
            if (this.getAnimation() == GolemAnimation.SMELTING) {
                this.setAnimation(GolemAnimation.IDLE, 0);
            }
        }
    }

    private void smeltItem() {
        ItemStack input = this.furnaceInventory.getStack(0);
        if (input.isEmpty()) return;

        ItemStack result = getSmeltingResult(input);
        if (result.isEmpty()) return;

        ItemStack output = this.furnaceInventory.getStack(2);
        if (output.isEmpty()) {
            this.furnaceInventory.setStack(2, result.copy());
            input.decrement(1);
        } else if (ItemStack.areItemsEqual(output, result) && output.getCount() < output.getMaxCount()) {
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
        if (input.isOf(Items.SAND) || input.isOf(Items.RED_SAND)) return new ItemStack(Items.GLASS);
        if (input.isIn(net.minecraft.registry.tag.ItemTags.LOGS) || input.isOf(Items.BAMBOO_BLOCK)) return new ItemStack(Items.CHARCOAL);
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
        // Ores
        if (input.isOf(Items.ANCIENT_DEBRIS)) return new ItemStack(Items.NETHERITE_SCRAP);
        // Food
        if (input.isOf(Items.CHORUS_FRUIT)) return new ItemStack(Items.POPPED_CHORUS_FRUIT);
        // Blocks
        if (input.isOf(Items.NETHER_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
        if (input.isOf(Items.DIAMOND_ORE) || input.isOf(Items.DEEPSLATE_DIAMOND_ORE)) return new ItemStack(Items.DIAMOND);
        if (input.isOf(Items.EMERALD_ORE) || input.isOf(Items.DEEPSLATE_EMERALD_ORE)) return new ItemStack(Items.EMERALD);
        if (input.isOf(Items.LAPIS_ORE) || input.isOf(Items.DEEPSLATE_LAPIS_ORE)) return new ItemStack(Items.LAPIS_LAZULI);
        if (input.isOf(Items.REDSTONE_ORE) || input.isOf(Items.DEEPSLATE_REDSTONE_ORE)) return new ItemStack(Items.REDSTONE);
        if (input.isOf(Items.COAL_ORE) || input.isOf(Items.DEEPSLATE_COAL_ORE)) return new ItemStack(Items.COAL);
        if (input.isOf(Items.NETHER_QUARTZ_ORE)) return new ItemStack(Items.QUARTZ);
        // Miscellaneous
        if (input.isIn(net.minecraft.registry.tag.ItemTags.SAND)) return new ItemStack(Items.GLASS);
        if (input.isOf(Items.SEA_PICKLE)) return new ItemStack(Items.LIME_DYE);

        return ItemStack.EMPTY;
    }

    private boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL) || stack.isOf(Items.BLAZE_ROD) || stack.isOf(Items.LAVA_BUCKET)) return true;
        if (stack.isOf(Items.COAL_BLOCK) || stack.isOf(Items.DRIED_KELP_BLOCK)) return true;
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.LOGS) || stack.isIn(net.minecraft.registry.tag.ItemTags.PLANKS) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_SLABS) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_STAIRS)) return true;
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_BUTTONS) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_PRESSURE_PLATES) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_DOORS) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_TRAPDOORS)) return true;
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_FENCES) || stack.isIn(net.minecraft.registry.tag.ItemTags.FENCE_GATES)) return true;
        if (stack.isOf(Items.STICK) || stack.isOf(Items.BOWL) || stack.isOf(Items.LADDER) || stack.isOf(Items.CRAFTING_TABLE) || stack.isOf(Items.BOOKSHELF) || stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST) || stack.isOf(Items.JUKEBOX) || stack.isOf(Items.DAYLIGHT_DETECTOR)) return true;
        if (stack.isOf(Items.BAMBOO) || stack.isOf(Items.SCAFFOLDING) || stack.isOf(Items.MANGROVE_PROPAGULE)) return true;
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.WOOL) || stack.isIn(net.minecraft.registry.tag.ItemTags.WOOL_CARPETS) || stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS) || stack.isIn(net.minecraft.registry.tag.ItemTags.BANNERS)) return true;
        return false;
    }

    private int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (fuel.isOf(Items.COAL) || fuel.isOf(Items.CHARCOAL)) return 1600;
        if (fuel.isOf(Items.BLAZE_ROD)) return 2400;
        if (fuel.isOf(Items.LAVA_BUCKET)) return 20000;
        if (fuel.isOf(Items.COAL_BLOCK)) return 16000;
        if (fuel.isOf(Items.DRIED_KELP_BLOCK)) return 4000;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.LOGS) || fuel.isIn(net.minecraft.registry.tag.ItemTags.PLANKS)) return 300;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_SLABS)) return 150;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_STAIRS)) return 300;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_FENCES) || fuel.isIn(net.minecraft.registry.tag.ItemTags.FENCE_GATES)) return 300;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_PRESSURE_PLATES)) return 300;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_TRAPDOORS)) return 300;
        if (fuel.isOf(Items.STICK)) return 100;
        if (fuel.isOf(Items.BOWL)) return 100;
        if (fuel.isOf(Items.LADDER)) return 300;
        if (fuel.isOf(Items.CRAFTING_TABLE)) return 300;
        if (fuel.isOf(Items.BOOKSHELF)) return 300;
        if (fuel.isOf(Items.CHEST) || fuel.isOf(Items.TRAPPED_CHEST)) return 300;
        if (fuel.isOf(Items.JUKEBOX)) return 300;
        if (fuel.isOf(Items.DAYLIGHT_DETECTOR)) return 300;
        if (fuel.isOf(Items.BAMBOO)) return 100;
        if (fuel.isOf(Items.SCAFFOLDING)) return 400;
        if (fuel.isOf(Items.MANGROVE_PROPAGULE)) return 100;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOODEN_BUTTONS)) return 100;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOOL)) return 100;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.WOOL_CARPETS)) return 67;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS)) return 100;
        if (fuel.isIn(net.minecraft.registry.tag.ItemTags.BANNERS)) return 300;
        return 0;
    }

    public void stopMusicSound() {
        if (!this.getEntityWorld().isClient() && this.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld) {
            StopSoundS2CPacket stopPacket = new StopSoundS2CPacket(null, SoundCategory.RECORDS);
            for (ServerPlayerEntity player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
                player.networkHandler.sendPacket(stopPacket);
            }
        }
    }

    public void stopJukebox() {
        this.stopMusicSound();
        if (this.jukeboxStartPos != null) {
            this.getEntityWorld().syncWorldEvent(null, net.minecraft.world.WorldEvents.JUKEBOX_STOPS_PLAYING, this.jukeboxStartPos, 0);
        }
        this.currentlyPlayingStack = ItemStack.EMPTY;
        this.jukeboxCooldown = 0;
        this.jukeboxStartPos = null;
        this.setHeldItem(ItemStack.EMPTY);
        this.setSearching(false);
        this.setAnimation(GolemAnimation.IDLE, 0);
    }

    private void tickJukebox() {
        if (!this.isJukeboxPlaying()) {
            if (this.jukeboxCooldown > 0 || !this.currentlyPlayingStack.isEmpty()) {
                this.stopJukebox();
            }
            return;
        }

        if (this.jukeboxCooldown > 0) {
            this.jukeboxCooldown--;
            if (this.jukeboxCooldown == 0) {
                this.stopMusicSound();
                this.currentlyPlayingStack = ItemStack.EMPTY;
                this.setHeldItem(ItemStack.EMPTY);
                this.setSearching(false);
                this.setAnimation(GolemAnimation.IDLE, 0);
                
                // If it was playing from playlist, try to play next
                if (this.isJukeboxPlaying()) {
                    playNextFromPlaylist();
                }
            }
            return;
        }

        if (this.isJukeboxPlaying() && this.currentlyPlayingStack.isEmpty()) {
            playNextFromPlaylist();
        }
    }

    private void playNextFromPlaylist() {
        if (!this.isJukeboxPlaying()) return;

        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < jukeboxInventory.size(); i++) {
            if (!jukeboxInventory.getStack(i).isEmpty()) {
                validSlots.add(i);
            }
        }

        if (validSlots.isEmpty()) {
            this.setJukeboxPlaying(false);
            return;
        }

        int nextSlot = -1;
        if (this.isJukeboxShuffle()) {
            nextSlot = validSlots.get(this.random.nextInt(validSlots.size()));
        } else {
            // Find next slot after currentJukeboxSlot
            for (int slot : validSlots) {
                if (slot > currentJukeboxSlot) {
                    nextSlot = slot;
                    break;
                }
            }
            
            // If no next slot and repeat is on, go back to start
            if (nextSlot == -1 && this.isJukeboxRepeat()) {
                nextSlot = validSlots.get(0);
            }
        }

        if (nextSlot != -1) {
            this.currentJukeboxSlot = nextSlot;
            ItemStack stack = jukeboxInventory.getStack(nextSlot);
            JukeboxPlayableComponent playable = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
            if (playable != null) {
                playable.song().resolveEntry(this.getEntityWorld().getRegistryManager()).ifPresent(songEntry -> {
                    this.currentlyPlayingStack = stack.copy();
                    this.currentlyPlayingStack.setCount(1);
                    this.jukeboxCooldown = (int) (songEntry.value().lengthInSeconds() * 20);
                    this.jukeboxStartPos = this.getBlockPos();
                    
                    if (!this.getEntityWorld().isClient()) {
                        this.jukeboxStartPos = this.getBlockPos();
                        this.getEntityWorld().syncWorldEvent(null, net.minecraft.world.WorldEvents.JUKEBOX_STARTS_PLAYING, this.jukeboxStartPos, Item.getRawId(this.currentlyPlayingStack.getItem()));
                        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), songEntry.value().soundEvent().value(), SoundCategory.RECORDS, 3.0F, 1.0F);
                        this.setAnimation(GolemAnimation.PLAYING_MUSIC, this.jukeboxCooldown);
                    }

                    this.setHeldItem(this.currentlyPlayingStack.copy());
                    this.setSearching(true);
                });
            }
        } else {
            this.setJukeboxPlaying(false);
            this.currentJukeboxSlot = -1;
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
            if (this.getAnimation() == GolemAnimation.IDLE || this.getAnimation() == GolemAnimation.SEARCHING) {
                this.setAnimation(GolemAnimation.SEARCHING, 20);
            }
        } else {
            if (this.getAnimation() == GolemAnimation.SEARCHING) {
                this.setAnimation(GolemAnimation.IDLE, 0);
            }
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

        // SHIFT+RIGHT CLICK to take item back or toggle lamp
        if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (this.golemType == GolemType.LAMP) {
                if (!player.getEntityWorld().isClient()) {
                    this.setLampOn(!this.isLampOn());
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 0.5F, 1.2F);
                }
                return ActionResult.SUCCESS;
            }

            ItemStack golemStack = this.getHeldItem();
            if (!golemStack.isEmpty()) {
                if (!player.getEntityWorld().isClient()) {
                    if (!player.getInventory().insertStack(golemStack)) {
                        player.dropItem(golemStack, false);
                    }
                    this.setHeldItem(ItemStack.EMPTY);
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F);
                }
                return ActionResult.SUCCESS;
            }
        }

        // Prevent using incompatible special items on the wrong golem types
        // e.g., prevent records on non-jukebox golems to avoid unintended handlers/UI and crashes
        if (this.golemType != GolemType.JUKEBOX) {
            JukeboxPlayableComponent playableCheck = playerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
            if (playableCheck != null) {
                if (!player.getEntityWorld().isClient()) {
                    player.sendMessage(Text.literal("This golem can't play records."), true);
                }
                return ActionResult.SUCCESS;
            }
        }

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

        if ((this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) && isSword(playerStack)) {
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
            // Already handled in Shift+Right Click logic
        }

        if (this.golemType == GolemType.JUKEBOX) {
            JukeboxPlayableComponent playable = playerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
            if (playable != null) {
                if (!player.getEntityWorld().isClient()) {
                    ItemStack disc = playerStack.copy();
                    disc.setCount(1);
                    ItemStack remaining = this.jukeboxInventory.addStack(disc);
                    if (remaining.isEmpty()) {
                        if (!player.getAbilities().creativeMode) {
                            playerStack.decrement(1);
                        }
                        player.sendMessage(Text.literal("Added to playlist"), true);
                    } else {
                        player.sendMessage(Text.literal("Playlist is full"), true);
                    }
                }
                return ActionResult.SUCCESS;
            }

            // Always open the Jukebox UI for Jukebox Golems
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(ServerPlayerEntity player) {
                        return UtilityGolem.this.getId();
                    }

                    @Override
                    public Text getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return new GolemJukeboxScreenHandler(syncId, playerInventory, UtilityGolem.this.jukeboxInventory, UtilityGolem.this);
                    }
                });
            }
            return ActionResult.SUCCESS;
        }

        if (this.golemType == GolemType.REDSTONE) {
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(ServerPlayerEntity player) {
                        return UtilityGolem.this.getId();
                    }

                    @Override
                    public Text getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return new RedstoneGolemScreenHandler(syncId, playerInventory, UtilityGolem.this);
                    }
                });
            }
            return ActionResult.SUCCESS;
        }

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

    private void swapTool(PlayerEntity player, ItemStack playerStack) {
        ItemStack golemStack = this.getHeldItem();
        ItemStack newStack = playerStack.copy();
        newStack.setCount(1);
        this.setHeldItem(newStack);
        // Do NOT equip CopperGolemEntity.POPPY_SLOT for any golem — it triggers vanilla copper golem behaviors we don't want
        // This ensures tool-using golems never inherit copper golem item-interaction logic
        // (rendering should rely on HELD_ITEM_SLOT / setHeldItem instead)
        // Intentionally left blank to avoid POPPY_SLOT usage.
        if (!player.getAbilities().creativeMode) {
            playerStack.decrement(1);
        }
        if (!golemStack.isEmpty()) {
            if (!player.getInventory().insertStack(golemStack)) {
                player.dropItem(golemStack, false);
            }
        }
    }

    public static boolean isOre(ItemStack stack) {
        return stack.isOf(Items.COAL) || stack.isOf(Items.RAW_IRON) || stack.isOf(Items.RAW_COPPER)
                || stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.DIAMOND) || stack.isOf(Items.EMERALD)
                || stack.isOf(Items.LAPIS_LAZULI) || stack.isOf(Items.REDSTONE) || stack.isOf(Items.QUARTZ)
                || stack.isOf(Items.AMETHYST_SHARD) || stack.isOf(Items.IRON_INGOT) || stack.isOf(Items.GOLD_INGOT)
                || stack.isOf(Items.COPPER_INGOT) || stack.isOf(Items.RAW_IRON_BLOCK) || stack.isOf(Items.RAW_COPPER_BLOCK)
                || stack.isOf(Items.RAW_GOLD_BLOCK) || stack.isOf(Items.NETHERITE_SCRAP) || stack.isOf(Items.ANCIENT_DEBRIS);
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
        return stack.isOf(Items.TORCH) || stack.isOf(Items.SOUL_TORCH) || stack.isOf(Items.REDSTONE_TORCH) || stack.isOf(Items.COPPER_TORCH);
    }

    public static boolean isShovel(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SHOVEL) || stack.isOf(Items.STONE_SHOVEL) ||
                stack.isOf(Items.IRON_SHOVEL) || stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL) || stack.isOf(Items.GOLDEN_SHOVEL) ||
                stack.isOf(Items.COPPER_SHOVEL);
    }

    public static boolean isBow(ItemStack stack) {
        return stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW);
    }

    public static boolean isShield(ItemStack stack) {
        return stack.isOf(Items.SHIELD);
    }

    public static boolean isTrident(ItemStack stack) {
        return stack.isOf(Items.TRIDENT);
    }

    public static boolean isFlintAndSteel(ItemStack stack) {
        return stack.isOf(Items.FLINT_AND_STEEL);
    }

    public static boolean isTool(ItemStack stack) {
        return isPickaxe(stack) || isSword(stack) || isAxe(stack) || isHoe(stack) || isShovel(stack) || isFishingRod(stack) || isShears(stack)
                || isBow(stack) || isShield(stack) || isTrident(stack) || isFlintAndSteel(stack);
    }

    public static boolean isLightSource(BlockState state) {
        return state.isIn(BlockTags.CANDLES) || state.isIn(BlockTags.CAMPFIRES) || state.isOf(Blocks.TORCH) || state.isOf(Blocks.SOUL_TORCH) || state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.COPPER_TORCH) || state.isOf(Blocks.WALL_TORCH) || state.isOf(Blocks.SOUL_WALL_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH) || state.isOf(Blocks.COPPER_WALL_TORCH) || state.isOf(Blocks.LANTERN) || state.isOf(Blocks.SOUL_LANTERN) || state.isOf(Blocks.GLOWSTONE) || state.isOf(Blocks.SEA_LANTERN) || state.isOf(Blocks.OCHRE_FROGLIGHT) || state.isOf(Blocks.PEARLESCENT_FROGLIGHT) || state.isOf(Blocks.VERDANT_FROGLIGHT) || state.isOf(Blocks.JACK_O_LANTERN) || state.isOf(Blocks.SHROOMLIGHT);
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
        net.minecraft.inventory.Inventories.writeData(writeView.get("JukeboxInventory"), this.jukeboxInventory.getHeldStacks());
        if (!this.currentlyPlayingStack.isEmpty()) {
            writeView.put("PlayingDisc", ItemStack.CODEC, this.currentlyPlayingStack);
        }
        writeView.putInt("JukeboxCooldown", this.jukeboxCooldown);
        writeView.putInt("CurrentJukeboxSlot", this.currentJukeboxSlot);
        writeView.putBoolean("JukeboxPlaying", this.isJukeboxPlaying());
        writeView.putBoolean("JukeboxShuffle", this.isJukeboxShuffle());
        writeView.putBoolean("JukeboxRepeat", this.isJukeboxRepeat());
        writeView.putInt("BurnTime", this.burnTime);
        writeView.putInt("FuelTime", this.fuelTime);
        writeView.putInt("CookTime", this.cookTime);
        writeView.putInt("CookTimeTotal", this.cookTimeTotal);
        if (!this.getSelectedBuyItem().isEmpty()) {
            writeView.put("SelectedBuyItem", ItemStack.CODEC, this.getSelectedBuyItem());
        }
        if (this.chestPos != null) {
            writeView.putInt("ChestX", this.chestPos.getX());
            writeView.putInt("ChestY", this.chestPos.getY());
            writeView.putInt("ChestZ", this.chestPos.getZ());
        }
        if (!this.getSchematicName().isEmpty()) {
            writeView.putString("SchematicName", this.getSchematicName());
        }
        writeView.putInt("MiningDirection", this.dataTracker.get(MINING_DIRECTION));
        // Persist current animation for seamless reloads
        writeView.putInt("AnimationId", this.dataTracker.get(ANIMATION_ID));
        writeView.putInt("AnimationTicks", this.dataTracker.get(ANIMATION_TICKS));
        writeView.putInt("AnimationStartTicks", this.dataTracker.get(ANIMATION_START_TICKS));
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
        net.minecraft.inventory.Inventories.readData(readView.getReadView("JukeboxInventory"), this.jukeboxInventory.getHeldStacks());
        readView.read("PlayingDisc", ItemStack.CODEC).ifPresent(stack -> this.currentlyPlayingStack = stack);
        this.jukeboxCooldown = readView.getInt("JukeboxCooldown", 0);
        this.currentJukeboxSlot = readView.getInt("CurrentJukeboxSlot", -1);
        this.setJukeboxPlaying(readView.getBoolean("JukeboxPlaying", false));
        this.setJukeboxShuffle(readView.getBoolean("JukeboxShuffle", false));
        this.setJukeboxRepeat(readView.getBoolean("JukeboxRepeat", false));
        this.burnTime = readView.getInt("BurnTime", 0);
        this.fuelTime = readView.getInt("FuelTime", 0);
        this.cookTime = readView.getInt("CookTime", 0);
        this.cookTimeTotal = readView.getInt("CookTimeTotal", 0);
        readView.read("SelectedBuyItem", ItemStack.CODEC).ifPresent(this::setSelectedBuyItem);
        if (readView.contains("ChestX")) {
            this.chestPos = new BlockPos(readView.getInt("ChestX", 0), readView.getInt("ChestY", 0), readView.getInt("ChestZ", 0));
        }
        this.setSchematicName(readView.getString("SchematicName", ""));
        this.dataTracker.set(MINING_DIRECTION, readView.getInt("MiningDirection", -1));
        // Restore animation
        int animId = readView.getInt("AnimationId", GolemAnimation.IDLE.ordinal());
        int animTicks = readView.getInt("AnimationTicks", 0);
        int animStartTicks = readView.getInt("AnimationStartTicks", Math.max(1, animTicks));
        this.dataTracker.set(ANIMATION_ID, animId);
        this.dataTracker.set(ANIMATION_TICKS, animTicks);
        this.dataTracker.set(ANIMATION_START_TICKS, animStartTicks);

        updateAttackDamage();
    }

    public BlockPos getChestPos() {
        return chestPos;
    }

    public void setChestPos(BlockPos chestPos) {
        this.chestPos = chestPos;
        this.setMiningDirection(null); // Reset mining direction when chest changes
    }

    public Inventory getChestInventory(BlockPos pos) {
        BlockState state = this.getEntityWorld().getBlockState(pos);
        if (state.getBlock() instanceof GolemChestBlock block) {
            return GolemChestBlockEntity.getInventory(block, state, this.getEntityWorld(), pos, false);
        }
        BlockEntity be = this.getEntityWorld().getBlockEntity(pos);
        if (be instanceof Inventory inv) {
            return inv;
        }
        return null;
    }

    public BlockPos findNearbyChest() {
        if (this.chestPos != null) {
            if (this.isBlacklisted(this.chestPos)) {
                this.chestPos = null;
            } else {
                BlockEntity be = this.getEntityWorld().getBlockEntity(this.chestPos);
                BlockState bs = this.getEntityWorld().getBlockState(this.chestPos);
                if (be instanceof Inventory && bs.getBlock() == this.getGolemType().getChestBlock()) {
                    return this.chestPos;
                }
            }
        }

        BlockPos pos = this.getBlockPos();
        int range = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS) ? 32 : 16;
        int verticalRange = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS) ? 15 : 4;
        
        for (int x = -range; x <= range; x++) {
            for (int y = -verticalRange; y <= verticalRange; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.add(x, y, z);
                    if (this.isBlacklisted(p)) continue;
                    BlockEntity be = this.getEntityWorld().getBlockEntity(p);
                    BlockState bs = this.getEntityWorld().getBlockState(p);
                    if (be instanceof Inventory && bs.getBlock() == this.getGolemType().getChestBlock()) {
                        this.setChestPos(p);
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public void initGolemsGoals() {
        if (this.golemType != null) {
            this.golemType.initGoals(this);
        }
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
            // Never equip the CopperGolemEntity.POPPY_SLOT to avoid vanilla copper golem behaviors being triggered
            // Rendering/usage will rely solely on HELD_ITEM_SLOT
            // Intentionally no POPPY_SLOT assignment here.
            updateAttackDamage();
        }

        return data;
    }

    public GolemType getGolemType() {
        return golemType;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        // Clear any goals inherited from parent to avoid unintended behaviors (e.g., random chest checks)
        this.goalSelector.getGoals().clear();
        this.targetSelector.getGoals().clear();

        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new SwimGoal(this)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new net.minecraft.entity.ai.goal.LongDoorInteractGoal(this, true)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new GolemAI.ClimbLadderGoal(this)));

        initGolemsGoals();
        
        // Remove any remaining goals that might have been added by CopperGolemEntity after our clear
        // specifically targeting anything that looks like chest sorting or putting away items if we could identify them.
        // Since we already cleared them, we are mostly safe unless CopperGolemEntity adds them in a way we don't expect.
    }

    // ===== Override CopperGolemEntity container interaction hooks to fully disable vanilla chest use =====
    @Override
    public void setTargetContainerPos(BlockPos pos) {
        // Intentionally ignore container targeting to prevent any vanilla copper golem item transport
    }

    @Override
    public boolean isViewingContainerAt(net.minecraft.block.entity.ViewerCountManager viewerCountManager, BlockPos pos) {
        // Never considered as viewing any container
        return false;
    }

    @Override
    public double getContainerInteractionRange() {
        // Zero range prevents interaction checks from succeeding
        return 0.0;
    }


    @Override
    public boolean tryAttack(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && (this.getGolemType() == GolemType.NETHERITE || this.getGolemType() == GolemType.ANCIENT) && this.isSword(this.getHeldItem())) {
            this.spawnSweepingAttackParticles(world);
            this.applySweepingDamage(world, target);
        }
        return success;
    }

    private void spawnSweepingAttackParticles(net.minecraft.server.world.ServerWorld world) {
        double d = -Math.sin(this.getYaw() * (Math.PI / 180.0));
        double e = Math.cos(this.getYaw() * (Math.PI / 180.0));
        world.spawnParticles(net.minecraft.particle.ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
    }

    private void applySweepingDamage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.Entity target) {
        float damage = (float)this.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);
        float sweepingDamage = 1.0f + (0.0f * damage); // Default ratio if EnchantmentHelper method not available
        
        for (net.minecraft.entity.LivingEntity livingEntity : world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0), (entity) -> {
            return entity != this && entity != target && !this.isTeammate(entity) && (!(entity instanceof net.minecraft.entity.decoration.ArmorStandEntity) || !((net.minecraft.entity.decoration.ArmorStandEntity)entity).isMarker()) && this.squaredDistanceTo(entity) < 9.0;
        })) {
            livingEntity.takeKnockback(0.4000000059604645, Math.sin(this.getYaw() * (Math.PI / 180.0)), -Math.cos(this.getYaw() * (Math.PI / 180.0)));
            livingEntity.damage(world, this.getDamageSources().mobAttack(this), sweepingDamage);
        }
        
        world.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0f, 1.0f);
    }

    @Override
    public boolean canPickUpLoot() {
        if (this.golemType == GolemType.REDSTONE) return false;
        return super.canPickUpLoot();
    }

    @Override
    public boolean canTarget(net.minecraft.entity.LivingEntity target) {
        if (target instanceof net.minecraft.entity.passive.AllayEntity) return false;
        return super.canTarget(target);
    }

    @Override
    public boolean canHaveStatusEffect(net.minecraft.entity.effect.StatusEffectInstance effect) {
        return super.canHaveStatusEffect(effect);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void pushAway(net.minecraft.entity.Entity entity) {
        if (entity instanceof net.minecraft.entity.passive.AllayEntity) return;
        super.pushAway(entity);
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
        ConfigManager.GolemStats stats = ConfigManager.getConfig().golems.get(this.getGolemType().getName());
        float baseDamage = (stats != null) ? (float) stats.attackDamage : 0.5f; 
        
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
