package rehdpanda.utilitygolems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.entity.ai.Brain;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Base class for Utility Golems
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.golem.CopperGolem;
public class UtilityGolem extends CopperGolem implements InventoryCarrier {

    private final GolemType golemType;
    private static final EntityDataAccessor<Integer> XP_SCORE = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleContainer inventory = new SimpleContainer(9);
    private final SimpleContainer furnaceInventory = new SimpleContainer(3);
    private final SimpleContainer jukeboxInventory = new SimpleContainer(1);
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

    private final net.minecraft.world.inventory.ContainerData furnacePropertyDelegate = new net.minecraft.world.inventory.ContainerData() {
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
        public int getCount() {
            return 4;
        }
    };

    private static final EntityDataAccessor<Optional<BlockPos>> FISHING_TARGET = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> DEBUG_TARGET = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> SELECTED_PATTERN = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WALL_WIDTH = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WALL_LENGTH = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BUILDING_STARTED = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAMP_ON = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STRIPPED = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private int attackCooldown = 0;
    private static final EntityDataAccessor<ItemStack> SELECTED_BUY_ITEM = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<String> SCHEMATIC_NAME = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> MINING_DIRECTION = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SMELTING = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUKEBOX_PLAYING = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUKEBOX_SHUFFLE = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUKEBOX_REPEAT = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);

    // Animation state syncing (server -> client)
    private static final EntityDataAccessor<Integer> ANIMATION_ID = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_TICKS = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_START_TICKS = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> REDSTONE_PROGRAM_STARTED = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DELETED_ITEMS_COUNT = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GOLD_TRADE_COUNT = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER_UUID_STRING = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.STRING);

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
        return this.entityData.get(REDSTONE_PROGRAM_STARTED);
    }

    public void setRedstoneProgramStarted(boolean started) {
        this.entityData.set(REDSTONE_PROGRAM_STARTED, started);
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

    public int getDeletedItemsCount() {
        return this.entityData.get(DELETED_ITEMS_COUNT);
    }

    public void setDeletedItemsCount(int count) {
        this.entityData.set(DELETED_ITEMS_COUNT, count);
    }

    public void incrementDeletedItemsCount(int amount) {
        this.setDeletedItemsCount(this.getDeletedItemsCount() + amount);
    }

    public int getGoldTradeCount() {
        return this.entityData.get(GOLD_TRADE_COUNT);
    }

    public void setGoldTradeCount(int count) {
        this.entityData.set(GOLD_TRADE_COUNT, count);
    }

    public Optional<UUID> getOwnerUuid() {
        String uuidStr = this.entityData.get(OWNER_UUID_STRING);
        if (uuidStr == null || uuidStr.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(uuidStr));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID_STRING, uuid == null ? "" : uuid.toString());
    }

    public void incrementGoldTradeCount() {
        this.setGoldTradeCount(this.getGoldTradeCount() + 1);
    }

    public int getXpScore() {
        return this.entityData.get(XP_SCORE);
    }

    public void setXpScore(int score) {
        this.entityData.set(XP_SCORE, score);
    }

    public void incrementXpScore(int amount) {
        this.setXpScore(this.getXpScore() + amount);
    }

    public void resetGoldTradeCount() {
        this.setGoldTradeCount(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(XP_SCORE, 0);
        builder.define(FISHING_TARGET, Optional.empty());
        builder.define(DEBUG_TARGET, Optional.empty());
        builder.define(SELECTED_PATTERN, 0);
        builder.define(WALL_WIDTH, 3);
        builder.define(WALL_LENGTH, 3);
        builder.define(BUILDING_STARTED, false);
        builder.define(LAMP_ON, false);
        builder.define(STRIPPED, false);
        builder.define(SELECTED_BUY_ITEM, ItemStack.EMPTY);
        builder.define(SCHEMATIC_NAME, "");
        builder.define(MINING_DIRECTION, -1);
        builder.define(SMELTING, false);
        builder.define(JUKEBOX_PLAYING, false);
        builder.define(JUKEBOX_SHUFFLE, false);
        builder.define(JUKEBOX_REPEAT, false);
        builder.define(ANIMATION_ID, GolemAnimation.IDLE.ordinal());
        builder.define(ANIMATION_TICKS, 0);
        builder.define(ANIMATION_START_TICKS, 0);
        builder.define(REDSTONE_PROGRAM_STARTED, false);
        builder.define(DELETED_ITEMS_COUNT, 0);
        builder.define(GOLD_TRADE_COUNT, 0);
        builder.define(OWNER_UUID_STRING, "");
    }

    public GolemAnimation getAnimation() {
        int id = this.entityData.get(ANIMATION_ID);
        if (id < 0 || id >= GolemAnimation.values().length) return GolemAnimation.IDLE;
        return GolemAnimation.values()[id];
    }

    public void setAnimation(GolemAnimation animation, int durationTicks) {
        this.entityData.set(ANIMATION_ID, animation == null ? GolemAnimation.IDLE.ordinal() : animation.ordinal());
        this.entityData.set(ANIMATION_TICKS, Math.max(0, durationTicks));
        this.entityData.set(ANIMATION_START_TICKS, Math.max(1, durationTicks));

        // Sync with UtilityGolemEntity states for built-in animations
        /*
        if (animation == GolemAnimation.SPINNING_HEAD) {
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.IDLE);
            // Trigger the spin head animation by setting the tickCount to match the timer
            // Since we can't set the private field, we just let it happen naturally if possible,
            // or we might need a mixin if we really want to force it.
            // But wait, UtilityGolemEntity's clientTick handles it.
        } else if (animation == GolemAnimation.DEPOSITING) {
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.DROPPING_ITEM);
        } else if (animation == GolemAnimation.WITHDRAWING) {
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.GETTING_ITEM);
        } else if (animation == GolemAnimation.PRESSING_BUTTON) {
            // No longer mapping to DROPPING_ITEM to allow custom rendering
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.IDLE);
        } else if (animation == GolemAnimation.IDLE) {
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.IDLE);
        }
        */
    }

    public int getAnimationTicks() {
        return this.entityData.get(ANIMATION_TICKS);
    }

    public float getAnimationProgress(float tickDelta) {
        int ticks = getAnimationTicks();
        if (ticks <= 0) return 0f;
        
        GolemAnimation anim = getAnimation();
        if (anim == GolemAnimation.DIGGING || anim == GolemAnimation.CHOPPING || anim == GolemAnimation.FARMING || 
            anim == GolemAnimation.FISHING || anim == GolemAnimation.PLAYING_MUSIC) {
            // Loop every 20 ticks
            return ((this.level().getGameTime() % 20) + tickDelta) / 20.0f;
        }

        // Normalize into [0..1], counting down
        int startTicks = this.entityData.get(ANIMATION_START_TICKS);
        return Math.max(0f, Math.min(1f, 1f - (ticks - tickDelta) / Math.max(1f, (float)startTicks)));
    }

    public void debugLog(String message) {
        if (this.hasCustomName()) {
            Component customName = this.getCustomName();
            if (customName != null && customName.getString().toUpperCase().contains("DEBUG")) {
                Level world = this.level();
                if (!world.isClientSide()) {
                    List<Player> players = world.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D), player -> true);
                    for (Player player : players) {
                        player.sendOverlayMessage(Component.literal("[DEBUG] " + message));
                    }
                }
            }
        }
    }

    public void setBuildingStarted(boolean started) {
        this.entityData.set(BUILDING_STARTED, started);
    }

    public boolean isBuildingStarted() {
        return this.entityData.get(BUILDING_STARTED);
    }

    public void setLampOn(boolean on) {
        this.entityData.set(LAMP_ON, on);
    }

    public boolean isLampOn() {
        return this.entityData.get(LAMP_ON);
    }

    public void setStripped(boolean stripped) {
        this.entityData.set(STRIPPED, stripped);
    }

    public boolean isStripped() {
        return this.entityData.get(STRIPPED);
    }
    
    public boolean isSmelting() {
        return this.entityData.get(SMELTING);
    }

    public boolean isJukeboxPlaying() {
        return this.entityData.get(JUKEBOX_PLAYING);
    }

    public void setJukeboxPlaying(boolean playing) {
        this.entityData.set(JUKEBOX_PLAYING, playing);
    }

    public boolean isJukeboxShuffle() {
        return this.entityData.get(JUKEBOX_SHUFFLE);
    }

    public void setJukeboxShuffle(boolean shuffle) {
        this.entityData.set(JUKEBOX_SHUFFLE, shuffle);
    }

    public boolean isJukeboxRepeat() {
        return this.entityData.get(JUKEBOX_REPEAT);
    }

    public void setJukeboxRepeat(boolean repeat) {
        this.entityData.set(JUKEBOX_REPEAT, repeat);
    }

    public SimpleContainer getJukeboxInventory() {
        return jukeboxInventory;
    }

    public void setBuildPattern(BuildPattern pattern) {
        this.entityData.set(SELECTED_PATTERN, pattern.ordinal());
    }

    public BuildPattern getBuildPattern() {
        return BuildPattern.values()[this.entityData.get(SELECTED_PATTERN)];
    }

    public void setWallWidth(int width) {
        this.entityData.set(WALL_WIDTH, width);
    }

    public int getWallWidth() {
        return this.entityData.get(WALL_WIDTH);
    }

    public void setWallLength(int length) {
        this.entityData.set(WALL_LENGTH, length);
    }

    public int getWallLength() {
        return this.entityData.get(WALL_LENGTH);
    }

    public void setSelectedBuyItem(ItemStack stack) {
        this.entityData.set(SELECTED_BUY_ITEM, stack);
    }

    public ItemStack getSelectedBuyItem() {
        return this.entityData.get(SELECTED_BUY_ITEM);
    }

    public void setSchematicName(String name) {
        this.entityData.set(SCHEMATIC_NAME, name == null ? "" : name);
    }

    public String getSchematicName() {
        return this.entityData.get(SCHEMATIC_NAME);
    }

    public void setMiningDirection(@Nullable net.minecraft.core.Direction direction) {
        this.entityData.set(MINING_DIRECTION, direction == null ? -1 : direction.ordinal());
    }

    @Nullable
    public net.minecraft.core.Direction getMiningDirection() {
        int ordinal = this.entityData.get(MINING_DIRECTION);
        return ordinal == -1 ? null : net.minecraft.core.Direction.values()[ordinal];
    }

    private final List<ItemStack> discoveredTrades = new ArrayList<>();

    public List<ItemStack> getDiscoveredTrades() {
        return discoveredTrades;
    }

    public void addDiscoveredTrade(ItemStack stack) {
        for (ItemStack s : discoveredTrades) {
            if (ItemStack.isSameItemSameComponents(s, stack)) return;
        }
        discoveredTrades.add(stack.copy());
        if (!this.level().isClientSide()) {
            syncDiscoveredTrades();
        }
    }

    public void syncDiscoveredTrades() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel) {
            UGInit.syncDiscoveredTrades(this);
        }
    }

    public void setFishingTarget(@Nullable BlockPos pos) {
        this.getEntityData().set(FISHING_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getFishingTarget() {
        return this.getEntityData().get(FISHING_TARGET).orElse(null);
    }

    public void setDebugTarget(@Nullable BlockPos pos) {
        this.getEntityData().set(DEBUG_TARGET, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getDebugTarget() {
        return this.getEntityData().get(DEBUG_TARGET).orElse(null);
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

    public UtilityGolem(EntityType<? extends UtilityGolem> type, Level world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;
        initGolemsGoals();
        updateAttackDamage();
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            removeLight();

            if (this.chestPos != null) {
                BlockEntity be = this.level().getBlockEntity(this.chestPos);
                if (be instanceof GolemChestBlockEntity golemChest) {
                    golemChest.setGolemDead(true);
                }
            }

            if (this.golemType == GolemType.JUKEBOX) {
                BlockPos stopPos = this.jukeboxStartPos != null ? this.jukeboxStartPos : this.blockPosition();
                this.level().levelEvent(null, LevelEvent.SOUND_STOP_JUKEBOX_SONG, stopPos, 0);
                if (!this.currentlyPlayingStack.isEmpty()) {
                    JukeboxPlayable playable = this.currentlyPlayingStack.get(DataComponents.JUKEBOX_PLAYABLE);
                    if (playable != null) {
                        if (playable.song().isBound()) {
                            stopMusicSound();
                        }
                    }
                }

                this.currentlyPlayingStack = ItemStack.EMPTY;
                this.jukeboxCooldown = 0;
                this.jukeboxStartPos = null;
            }

            // UtilityGolemEntity already drops what is in its POPPY_SLOT, but our golem
            // uses HELD_ITEM_SLOT (MAINHAND). We must drop it manually.
            ItemStack heldItem = this.getHeldItem();
            if (!heldItem.isEmpty()) {
                this.spawnAtLocation((ServerLevel)this.level(), heldItem.copy());
                this.setHeldItem(ItemStack.EMPTY);
            }
            
            for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
                ItemStack stack = this.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation((ServerLevel)this.level(), stack.copy());
                    this.inventory.setItem(i, ItemStack.EMPTY);
                }
            }
            for (int i = 0; i < this.furnaceInventory.getContainerSize(); i++) {
                ItemStack stack = this.furnaceInventory.getItem(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation((ServerLevel)this.level(), stack.copy());
                    this.furnaceInventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel world) {
        // Skip UtilityGolemEntity.customServerAiStep which triggers brain.tick and UtilityGolemBrain.updateActivity
        // but ensure Mob-level target management works.
        // Proactively clear target if dead or removed.
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target != null && (target.isDeadOrDying() || target.isRemoved())) {
            this.setTarget(null);
        }
    }

    protected Brain.Provider<UtilityGolem> brainProvider() {
        return Brain.provider(java.util.Collections.emptySet());
    }

    @Override
    protected net.minecraft.world.entity.ai.Brain<net.minecraft.world.entity.animal.golem.CopperGolem> makeBrain(Brain.Packed packed) {
        Brain.Provider<UtilityGolem> provider = this.brainProvider();
        return (net.minecraft.world.entity.ai.Brain<net.minecraft.world.entity.animal.golem.CopperGolem>)(Object)provider.makeBrain(this, packed);
    }

    @Override
    public net.minecraft.world.entity.ai.Brain<net.minecraft.world.entity.animal.golem.CopperGolem> getBrain() {
        return (net.minecraft.world.entity.ai.Brain<net.minecraft.world.entity.animal.golem.CopperGolem>) super.getBrain();
    }

    public void tick() {
        if (this.golemType == GolemType.BAMBOO && !this.level().isClientSide()) {
            ItemStack boots = this.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty()) {
                // In 1.21.1, Enchantments are handled via Registry. 
                // We need to check if the boots have the soul speed enchantment.
                int soulSpeedLevel = EnchantmentHelper.getEnchantmentLevel(this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SOUL_SPEED), this);
                if (soulSpeedLevel > 0) {
                    BlockState standingOn = this.level().getBlockState(this.blockPosition().below());
                    if (standingOn.is(Blocks.SOUL_SAND) || standingOn.is(Blocks.SOUL_SOIL)) {
                        this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.fromNamespaceAndPath("utility-golems", "soul_speed_boost"), 0.05D + 0.01D * (double)soulSpeedLevel, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                    } else {
                        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(Identifier.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
                    }
                } else {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(Identifier.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
                }
            } else if (this.golemType == GolemType.BAMBOO) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(Identifier.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
            }
        }

        if (this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) {
            // Suppression of UtilityGolemEntity behaviors is handled by disabling POPPY_SLOT 
            // and clearing goals in initGoals.
        }
        // Proactively prevent any vanilla UtilityGolem container targeting each tick (server-side)
        if (!this.level().isClientSide()) {
            if (this instanceof net.minecraft.world.entity.animal.golem.CopperGolem copper) {
                copper.clearOpenedChestPos();
            }
        }

        super.tick();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (!this.level().isClientSide()) {
            Component customName = this.getCustomName();
            boolean isDebug = this.hasCustomName() && customName != null && customName.getString().equalsIgnoreCase("debug");
            if (this.isCurrentlyGlowing() != isDebug) {
                this.setGlowingTag(isDebug);
            }
        }

        if (this.jukeboxCooldown > 0) {
            this.jukeboxCooldown--;
            if (this.jukeboxCooldown == 0 && !this.currentlyPlayingStack.isEmpty()) {
                if (!this.level().isClientSide()) {
                    BlockPos stopPos = this.jukeboxStartPos != null ? this.jukeboxStartPos : this.blockPosition();
                    this.level().levelEvent(null, LevelEvent.SOUND_STOP_JUKEBOX_SONG, stopPos, 0);
                    
                    // Stop the music sound if it was playing via playSound
                    JukeboxPlayable playable = this.currentlyPlayingStack.get(DataComponents.JUKEBOX_PLAYABLE);
                    if (playable != null) {
                        if (playable.song().isBound()) {
                            stopMusicSound();
                        }
                    }

                    if (this.golemType == GolemType.JUKEBOX) {
                        Player player = this.level().getNearestPlayer(this, 10.0D);
                        if (player != null) {
                            player.drop(this.currentlyPlayingStack.copy(), false);
                        } else {
                            this.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.currentlyPlayingStack.copy()));
                        }
                        this.setHeldItem(ItemStack.EMPTY);
                        this.setSearching(false);
                    }
                    this.currentlyPlayingStack = ItemStack.EMPTY;
                    this.jukeboxStartPos = null;
                }
            }
            if (!this.level().isClientSide() && this.jukeboxCooldown % 20 == 0 && this.jukeboxCooldown > 0) {
                ((net.minecraft.server.level.ServerLevel)this.level()).sendParticles(ParticleTypes.NOTE, this.getRandomX(0.5D), this.getRandomY() + 0.5D, this.getRandomZ(0.5D), 1, 0, 0, 0, (double)this.getRandom().nextInt(24) / 24.0D);
            }
        }
        if (!this.level().isClientSide()) {
            // Update animation timer server-side
            int t = this.getEntityData().get(ANIMATION_TICKS);
            if (t > 0) {
                this.getEntityData().set(ANIMATION_TICKS, t - 1);
                if (t - 1 == 0) {
                    // Reset to idle when finished
                    this.setAnimation(GolemAnimation.IDLE, 0);
                }
            }

            if (this.golemType == GolemType.FURNACE || this.golemType == GolemType.SMOKER || this.golemType == GolemType.BLAST_FURNACE) {
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

            // Cleanup old chorus planting data every 1 minute
            if (this.level().getGameTime() % 1200 == 0) {
                long currentTime = this.level().getGameTime();
                this.plantedChorusFlowers.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > 4800); // 4 minutes
            }

            // Occasionally spin head when idle
            if (this.getAnimation() == GolemAnimation.IDLE && this.getRandom().nextInt(200) == 0) {
                this.setAnimation(GolemAnimation.SPINNING_HEAD, 60);
            }
        }
    }

    private void tickLamp() {
        if (this.level().isClientSide()) return;
        
        boolean isLampOn = this.isLampOn();
        if (isLampOn) {
            updateLightEmission(12);
        } else {
            stopLightEmission();
        }
    }

    private void updateLightEmission(int lightLevel) {
        BlockPos currentPos = this.blockPosition().above();
        if (lastLightPos == null || !lastLightPos.equals(currentPos) || !this.level().getBlockState(lastLightPos).is(UGBlocks.LIGHT_BLOCK) || this.level().getBlockState(lastLightPos).getValue(LightBlock.LEVEL) != lightLevel) {
            removeLight();
            if (this.level().getBlockState(currentPos).canBeReplaced()) {
                this.level().setBlock(currentPos, UGBlocks.LIGHT_BLOCK.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel), 3);
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
            if (this.level().getBlockState(lastLightPos).is(UGBlocks.LIGHT_BLOCK)) {
                this.level().setBlock(lastLightPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
            lastLightPos = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            removeLight();
        }
        super.remove(reason);
    }


    @Override
    public boolean onClimbable() {
        return super.onClimbable() || this.level().getBlockState(this.blockPosition()).is(net.minecraft.tags.BlockTags.CLIMBABLE);
    }

    private void tickFurnace() {
        if (this.level().isClientSide()) return;

        boolean wasBurning = this.burnTime > 0;

        if (this.burnTime > 0) {
            --this.burnTime;
        }

        ItemStack inputStack = this.furnaceInventory.getItem(0);
        ItemStack fuelStack = this.furnaceInventory.getItem(1);
        boolean hasInput = !inputStack.isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();

        if (this.burnTime > 0 || (hasFuel && hasInput)) {
            if (this.burnTime <= 0 && isFuel(fuelStack)) {
                if (!getSmeltingResult(inputStack).isEmpty()) {
                    this.burnTime = getFuelTime(fuelStack);
                    this.fuelTime = this.burnTime;
                    if (this.burnTime > 0) {
                        Item item = fuelStack.getItem();
                        fuelStack.shrink(1);
                        if (fuelStack.isEmpty()) {
                            ItemStack itemRemainder = item.getCraftingRemainder().create();
                            this.furnaceInventory.setItem(1, itemRemainder);
                        }
                    }
                }
            }

            if (this.burnTime > 0) {
                // Progress cook time
                this.cookTimeTotal = (this.golemType == GolemType.SMOKER || this.golemType == GolemType.BLAST_FURNACE) ? 100 : 200;
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
            this.getEntityData().set(SMELTING, this.burnTime > 0);
        }

        if (this.burnTime > 0 && !getSmeltingResult(this.furnaceInventory.getItem(0)).isEmpty()) {
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
        ItemStack input = this.furnaceInventory.getItem(0);
        if (input.isEmpty()) return;

        ItemStack result = getSmeltingResult(input);
        if (result.isEmpty()) return;

        ItemStack output = this.furnaceInventory.getItem(2);
        if (output.isEmpty()) {
            this.furnaceInventory.setItem(2, result.copy());
            input.shrink(1);
        } else if (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize()) {
            output.grow(1);
            input.shrink(1);
        }
    }

    private ItemStack getSmeltingResult(ItemStack input) {
        if (this.golemType == GolemType.SMOKER) {
            if (input.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP);
            if (input.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF);
            if (input.is(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN);
            if (input.is(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON);
            if (input.is(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT);
            if (input.is(Items.COD)) return new ItemStack(Items.COOKED_COD);
            if (input.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON);
            if (input.is(Items.POTATO)) return new ItemStack(Items.BAKED_POTATO);
            if (input.is(Items.KELP)) return new ItemStack(Items.DRIED_KELP);
            return ItemStack.EMPTY;
        }

        if (this.golemType == GolemType.BLAST_FURNACE) {
            if (input.is(Items.RAW_IRON) || input.is(Items.IRON_ORE) || input.is(Items.DEEPSLATE_IRON_ORE)) return new ItemStack(Items.IRON_INGOT);
            if (input.is(Items.RAW_GOLD) || input.is(Items.GOLD_ORE) || input.is(Items.DEEPSLATE_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
            if (input.is(Items.RAW_COPPER) || input.is(Items.COPPER_ORE) || input.is(Items.DEEPSLATE_COPPER_ORE)) return new ItemStack(Items.COPPER_INGOT);
            if (input.is(Items.NETHER_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
            if (input.is(Items.ANCIENT_DEBRIS)) return new ItemStack(Items.NETHERITE_SCRAP);
            // Chainmail
            if (input.is(Items.CHAINMAIL_HELMET)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.CHAINMAIL_CHESTPLATE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.CHAINMAIL_LEGGINGS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.CHAINMAIL_BOOTS)) return new ItemStack(Items.IRON_NUGGET);
            // Iron gear
            if (input.is(Items.IRON_HELMET)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_CHESTPLATE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_LEGGINGS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_BOOTS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_PICKAXE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_SHOVEL)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_AXE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_HOE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.is(Items.IRON_SWORD)) return new ItemStack(Items.IRON_NUGGET);
            // Golden gear
            if (input.is(Items.GOLDEN_HELMET)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_CHESTPLATE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_LEGGINGS)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_BOOTS)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_PICKAXE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_SHOVEL)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_AXE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_HOE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.is(Items.GOLDEN_SWORD)) return new ItemStack(Items.GOLD_NUGGET);
            return ItemStack.EMPTY;
        }

        if (input.is(Items.RAW_IRON) || input.is(Items.IRON_ORE) || input.is(Items.DEEPSLATE_IRON_ORE)) return new ItemStack(Items.IRON_INGOT);
        if (input.is(Items.RAW_GOLD) || input.is(Items.GOLD_ORE) || input.is(Items.DEEPSLATE_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
        if (input.is(Items.RAW_COPPER) || input.is(Items.COPPER_ORE) || input.is(Items.DEEPSLATE_COPPER_ORE)) return new ItemStack(Items.COPPER_INGOT);
        if (input.is(Items.COBBLESTONE)) return new ItemStack(Items.STONE);
        if (input.is(Items.STONE)) return new ItemStack(Items.SMOOTH_STONE);
        if (input.is(Items.SAND) || input.is(Items.RED_SAND)) return new ItemStack(Items.GLASS);
        if (input.is(net.minecraft.tags.ItemTags.LOGS) || input.is(Items.BAMBOO_BLOCK)) return new ItemStack(Items.CHARCOAL);
        if (input.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP);
        if (input.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF);
        if (input.is(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN);
        if (input.is(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON);
        if (input.is(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT);
        if (input.is(Items.COD)) return new ItemStack(Items.COOKED_COD);
        if (input.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON);
        if (input.is(Items.POTATO)) return new ItemStack(Items.BAKED_POTATO);
        if (input.is(Items.KELP)) return new ItemStack(Items.DRIED_KELP);
        if (input.is(Items.CLAY_BALL)) return new ItemStack(Items.BRICK);
        if (input.is(Items.CLAY)) return new ItemStack(Items.TERRACOTTA);
        if (input.is(Items.CACTUS)) return new ItemStack(Items.GREEN_DYE);
        if (input.is(Items.NETHERRACK)) return new ItemStack(Items.NETHER_BRICK);
        // Ores
        if (input.is(Items.ANCIENT_DEBRIS)) return new ItemStack(Items.NETHERITE_SCRAP);
        // Food
        if (input.is(Items.CHORUS_FRUIT)) return new ItemStack(Items.POPPED_CHORUS_FRUIT);
        // Blocks
        if (input.is(Items.NETHER_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
        if (input.is(Items.DIAMOND_ORE) || input.is(Items.DEEPSLATE_DIAMOND_ORE)) return new ItemStack(Items.DIAMOND);
        if (input.is(Items.EMERALD_ORE) || input.is(Items.DEEPSLATE_EMERALD_ORE)) return new ItemStack(Items.EMERALD);
        if (input.is(Items.LAPIS_ORE) || input.is(Items.DEEPSLATE_LAPIS_ORE)) return new ItemStack(Items.LAPIS_LAZULI);
        if (input.is(Items.REDSTONE_ORE) || input.is(Items.DEEPSLATE_REDSTONE_ORE)) return new ItemStack(Items.REDSTONE);
        if (input.is(Items.COAL_ORE) || input.is(Items.DEEPSLATE_COAL_ORE)) return new ItemStack(Items.COAL);
        if (input.is(Items.NETHER_QUARTZ_ORE)) return new ItemStack(Items.QUARTZ);
        // Miscellaneous
        if (input.is(net.minecraft.tags.ItemTags.SAND)) return new ItemStack(Items.GLASS);
        if (input.is(Items.SEA_PICKLE)) return new ItemStack(Items.LIME_DYE);

        return ItemStack.EMPTY;
    }

    private boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.BLAZE_ROD) || stack.is(Items.LAVA_BUCKET)) return true;
        if (stack.is(Items.COAL_BLOCK) || stack.is(Items.DRIED_KELP_BLOCK)) return true;
        if (stack.is(net.minecraft.tags.ItemTags.LOGS) || stack.is(net.minecraft.tags.ItemTags.PLANKS) || stack.is(net.minecraft.tags.ItemTags.WOODEN_SLABS) || stack.is(net.minecraft.tags.ItemTags.WOODEN_STAIRS)) return true;
        if (stack.is(net.minecraft.tags.ItemTags.WOODEN_BUTTONS) || stack.is(net.minecraft.tags.ItemTags.WOODEN_PRESSURE_PLATES) || stack.is(net.minecraft.tags.ItemTags.WOODEN_DOORS) || stack.is(net.minecraft.tags.ItemTags.WOODEN_TRAPDOORS)) return true;
        if (stack.is(net.minecraft.tags.ItemTags.WOODEN_FENCES) || stack.is(net.minecraft.tags.ItemTags.FENCE_GATES)) return true;
        if (stack.is(Items.STICK) || stack.is(Items.BOWL) || stack.is(Items.LADDER) || stack.is(Items.CRAFTING_TABLE) || stack.is(Items.BOOKSHELF) || stack.is(Items.CHEST) || stack.is(Items.TRAPPED_CHEST) || stack.is(Items.JUKEBOX) || stack.is(Items.DAYLIGHT_DETECTOR)) return true;
        if (stack.is(Items.BAMBOO) || stack.is(Items.SCAFFOLDING) || stack.is(Items.MANGROVE_PROPAGULE)) return true;
        if (stack.is(net.minecraft.tags.ItemTags.WOOL) || stack.is(net.minecraft.tags.ItemTags.WOOL_CARPETS) || stack.is(net.minecraft.tags.ItemTags.SAPLINGS) || stack.is(net.minecraft.tags.ItemTags.BANNERS)) return true;
        return false;
    }

    private int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL)) return 1600;
        if (fuel.is(Items.BLAZE_ROD)) return 2400;
        if (fuel.is(Items.LAVA_BUCKET)) return 20000;
        if (fuel.is(Items.COAL_BLOCK)) return 16000;
        if (fuel.is(Items.DRIED_KELP_BLOCK)) return 4000;
        if (fuel.is(net.minecraft.tags.ItemTags.LOGS) || fuel.is(net.minecraft.tags.ItemTags.PLANKS)) return 300;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_SLABS)) return 150;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_STAIRS)) return 300;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_FENCES) || fuel.is(net.minecraft.tags.ItemTags.FENCE_GATES)) return 300;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_PRESSURE_PLATES)) return 300;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_TRAPDOORS)) return 300;
        if (fuel.is(Items.STICK)) return 100;
        if (fuel.is(Items.BOWL)) return 100;
        if (fuel.is(Items.LADDER)) return 300;
        if (fuel.is(Items.CRAFTING_TABLE)) return 300;
        if (fuel.is(Items.BOOKSHELF)) return 300;
        if (fuel.is(Items.CHEST) || fuel.is(Items.TRAPPED_CHEST)) return 300;
        if (fuel.is(Items.JUKEBOX)) return 300;
        if (fuel.is(Items.DAYLIGHT_DETECTOR)) return 300;
        if (fuel.is(Items.BAMBOO)) return 100;
        if (fuel.is(Items.SCAFFOLDING)) return 400;
        if (fuel.is(Items.MANGROVE_PROPAGULE)) return 100;
        if (fuel.is(net.minecraft.tags.ItemTags.WOODEN_BUTTONS)) return 100;
        if (fuel.is(net.minecraft.tags.ItemTags.WOOL)) return 100;
        if (fuel.is(net.minecraft.tags.ItemTags.WOOL_CARPETS)) return 67;
        if (fuel.is(net.minecraft.tags.ItemTags.SAPLINGS)) return 100;
        if (fuel.is(net.minecraft.tags.ItemTags.BANNERS)) return 300;
        return 0;
    }

    public void stopMusicSound() {
        if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel world) {
            net.minecraft.network.protocol.game.ClientboundStopSoundPacket stopPacket = new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(null, net.minecraft.sounds.SoundSource.RECORDS);
            world.getChunkSource().sendToTrackingPlayers(this, stopPacket);
        }
    }

    public void stopJukebox() {
        this.stopMusicSound();
        if (this.jukeboxStartPos != null) {
            this.level().levelEvent(null, LevelEvent.SOUND_STOP_JUKEBOX_SONG, this.jukeboxStartPos, 0);
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
        for (int i = 0; i < jukeboxInventory.getContainerSize(); i++) {
            if (!jukeboxInventory.getItem(i).isEmpty()) {
                validSlots.add(i);
            }
        }

        if (validSlots.isEmpty()) {
            this.setJukeboxPlaying(false);
            return;
        }

        int nextSlot = -1;
        if (this.isJukeboxShuffle()) {
            nextSlot = validSlots.get(this.getRandom().nextInt(validSlots.size()));
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
            ItemStack stack = jukeboxInventory.getItem(nextSlot);
            JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
            if (playable != null) {
                if (playable.song().isBound()) {
                    net.minecraft.world.item.JukeboxSong song = playable.song().value();
                    this.currentlyPlayingStack = stack.copy();
                    this.currentlyPlayingStack.setCount(1);
                    this.jukeboxCooldown = (int) (song.lengthInSeconds() * 20);
                    this.jukeboxStartPos = this.blockPosition();
                    
                    if (!this.level().isClientSide()) {
                        this.jukeboxStartPos = this.blockPosition();
                        this.level().levelEvent(null, net.minecraft.world.level.block.LevelEvent.SOUND_PLAY_JUKEBOX_SONG, this.jukeboxStartPos, net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(this.currentlyPlayingStack.getItem()));
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), song.soundEvent().value(), SoundSource.RECORDS, 3.0F, 1.0F);
                        this.setAnimation(GolemAnimation.PLAYING_MUSIC, this.jukeboxCooldown);
                    }

                    this.setHeldItem(this.currentlyPlayingStack.copy());
                    this.setSearching(true);
                }
            }
        } else {
            this.setJukeboxPlaying(false);
            this.currentJukeboxSlot = -1;
        }
    }

    private void tickGold() {
        if (this.tickCount % 20 == 0) {
            SimpleContainer inv = this.getInventory();
            int nuggetCount = 0;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(Items.GOLD_NUGGET)) {
                    nuggetCount += stack.getCount();
                }
            }

            if (nuggetCount >= 9) {
                int toConsume = 9;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.is(Items.GOLD_NUGGET)) {
                        int amount = Math.min(toConsume, stack.getCount());
                        stack.shrink(amount);
                        toConsume -= amount;
                        if (toConsume <= 0) break;
                    }
                }
                ItemStack ingot = new ItemStack(Items.GOLD_INGOT);
                ItemStack remaining = inv.addItem(ingot);
                if (!remaining.isEmpty()) {
                    this.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), remaining));
                }
            }
        }
    }

    public void setSearching(boolean searching) {
        if (searching) {
            this.swing(InteractionHand.MAIN_HAND);
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
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        if (this.hasCustomName()) {
            return super.getDisplayName();
        }
        return Component.literal(this.golemType.getFriendlyName());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            UGInit.FabricBridge.sendToPlayer(serverPlayer, new UGInit.OpenGolemInventoryPayload(this.getId()));
        }
        
        ItemStack playerStack = player.getItemInHand(hand);

        // SHIFT+RIGHT CLICK to take item back or toggle lamp
        if (player.isCrouching() && hand == InteractionHand.MAIN_HAND) {
            if (this.golemType == GolemType.LAMP) {
                if (!player.level().isClientSide()) {
                    this.setLampOn(!this.isLampOn());
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.5F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }

            if (this.golemType == GolemType.MEDIC) {
                ItemStack wrench = ItemStack.EMPTY;
                for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
                    if (this.getInventory().getItem(i).is(UGItems.WRENCH_ITEM)) {
                        wrench = this.inventory.removeItemNoUpdate(i);
                        break;
                    }
                }
                if (!wrench.isEmpty()) {
                    if (!player.level().isClientSide()) {
                        if (!player.getInventory().add(wrench)) {
                            player.drop(wrench, false);
                        }
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.7F + 1.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            ItemStack golemStack = this.getHeldItem();
            if (!golemStack.isEmpty()) {
                if (!player.level().isClientSide()) {
                    if (!player.getInventory().add(golemStack)) {
                        player.drop(golemStack, false);
                    }
                    this.setHeldItem(ItemStack.EMPTY);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.7F + 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Prevent using incompatible special items on the wrong golem types
        // e.g., prevent records on non-jukebox golems to avoid unintended handlers/UI and crashes
        if (this.golemType != GolemType.JUKEBOX) {
            JukeboxPlayable playableCheck = playerStack.get(DataComponents.JUKEBOX_PLAYABLE);
            if (playableCheck != null) {
                if (!player.level().isClientSide()) {
                    player.sendOverlayMessage(Component.literal("This golem can't play records."));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.golemType == GolemType.REDSTONE && playerStack.is(Items.CLOCK)) {
            if (!player.level().isClientSide()) {
                if (!player.isCreative()) {
                    playerStack.shrink(1);
                }
                BlockPos pos = this.blockPosition();
                BlockState state = UGBlocks.REDSTONE_GOLEM_STATUE.defaultBlockState().setValue(RedstoneGolemStatueBlock.FACING, this.getDirection().getOpposite());
                this.level().setBlock(pos, state, 3);
                BlockEntity be = this.level().getBlockEntity(pos);
                if (be instanceof RedstoneGolemStatueBlockEntity statueBe) {
                    if (this.hasCustomName()) {
                        statueBe.setCustomName(this.getCustomName());
                    }
                }
                this.level().playSound(null, pos, SoundEvents.COPPER_GOLEM_BECOME_STATUE, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.discard();
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && !this.isStripped() && isAxe(playerStack)) {
            if (!player.level().isClientSide()) {
                this.setStripped(true);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AXE_STRIP, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    playerStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAPIS && isPickaxe(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if ((this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) && (isSword(playerStack) || isAxe(playerStack) || isSpear(playerStack) || isTrident(playerStack) || isMace(playerStack))) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isAxe(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isShears(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && isTool(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.SPONGE && isFishingRod(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAMP && isTorch(playerStack)) {
            if (!player.level().isClientSide()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.MEDIC && playerStack.is(UGItems.WRENCH_ITEM)) {
            if (!player.level().isClientSide()) {
                ItemStack disc = playerStack.copy();
                disc.setCount(1);
                ItemStack remaining = this.inventory.addItem(disc);
                if (remaining.isEmpty()) {
                    if (!player.isCreative()) {
                        playerStack.shrink(1);
                    }
                    this.setItemSlot(EquipmentSlot.MAINHAND, disc);
                    player.sendOverlayMessage(Component.literal("Gave wrench to Medic Golem"));
                } else {
                    player.sendOverlayMessage(Component.literal("Medic Golem's getInventory() is full"));
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (playerStack.is(UGItems.WRENCH_ITEM)) {
            float currentHealth = this.getHealth();
            float maxHealth = this.getMaxHealth();
            if (currentHealth < maxHealth) {
                if (!player.level().isClientSide()) {
                    float healAmount = maxHealth * 0.25f; // Base 25% heal

                    // Efficiency enchantment increases amount of health that's healed
                    int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), this);
                    if (efficiencyLevel > 0) {
                        healAmount += (maxHealth * 0.05f * efficiencyLevel); // Add 5% per efficiency level
                    }

                    this.heal(healAmount);
                    if (!player.isCreative()) {
                        playerStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    for (int i = 0; i < 7; ++i) {
                        double d = this.getRandom().nextGaussian() * 0.02;
                        double e = this.getRandom().nextGaussian() * 0.02;
                        double f = this.getRandom().nextGaussian() * 0.02;
                        ((net.minecraft.server.level.ServerLevel)this.level()).sendParticles(ParticleTypes.HEART, this.getX(1.0), this.getY(0.5), this.getZ(1.0), 1, d, e, f, 0.0);
                    }
                    player.sendOverlayMessage(Component.literal(this.golemType.getFriendlyName() + " Health: " + (int)this.getHealth() + "/" + (int)maxHealth));
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!player.level().isClientSide()) {
                    player.sendOverlayMessage(Component.literal(this.golemType.getFriendlyName() + " is already at full health (" + (int)maxHealth + "/" + (int)maxHealth + ")"));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.golemType == GolemType.FURNACE || this.golemType == GolemType.SMOKER || this.golemType == GolemType.BLAST_FURNACE) {
            if (!player.level().isClientSide()) {
                player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (syncId, playerInventory, p) -> new GolemFurnaceMenu(syncId, playerInventory, this.furnaceInventory, this.furnacePropertyDelegate, this),
                        this.getDisplayName()
                ));
            }
            return InteractionResult.SUCCESS;
        } else if (this.golemType == GolemType.LAMP) {
            // Already handled in Shift+Right Click logic
        }

        if (this.golemType == GolemType.JUKEBOX) {
            JukeboxPlayable playable = playerStack.get(DataComponents.JUKEBOX_PLAYABLE);
            if (playable != null) {
                if (!player.level().isClientSide()) {
                    ItemStack disc = playerStack.copy();
                    disc.setCount(1);
                    ItemStack remaining = this.jukeboxInventory.addItem(disc);
                    if (remaining.isEmpty()) {
                        if (!player.isCreative()) {
                            playerStack.shrink(1);
                        }
                        player.sendOverlayMessage(Component.literal("Added to playlist"));
                    } else {
                        player.sendOverlayMessage(Component.literal("Playlist is full"));
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Always open the Jukebox UI for Jukebox Golems
            if (!player.level().isClientSide()) {
                player.openMenu(new net.minecraft.world.MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                        return new GolemJukeboxMenu(syncId, playerInventory, UtilityGolem.this.jukeboxInventory, UtilityGolem.this);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.REDSTONE) {
            if (!player.level().isClientSide()) {
                player.openMenu(new net.minecraft.world.MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                        return new RedstoneGolemMenu(syncId, playerInventory, UtilityGolem.this);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        if (!player.level().isClientSide()) {
            player.openMenu(new net.minecraft.world.MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return UtilityGolem.this.getDisplayName();
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                    return new GolemInventoryMenu(syncId, playerInventory, UtilityGolem.this.inventory, UtilityGolem.this);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }

    private void swapTool(Player player, ItemStack playerStack) {
        ItemStack golemStack = this.getHeldItem();
        ItemStack newStack = playerStack.copy();
        newStack.setCount(1);
        this.setHeldItem(newStack);
        // Do NOT equip UtilityGolemEntity.POPPY_SLOT for any golem — it triggers vanilla copper golem behaviors we don't want
        // This ensures tool-using golems never inherit copper golem item-interaction logic
        // (rendering should rely on HELD_ITEM_SLOT / setHeldItem instead)
        // Intentionally left blank to avoid POPPY_SLOT usage.
        if (!player.isCreative()) {
            playerStack.shrink(1);
        }
        if (!golemStack.isEmpty()) {
            if (!player.getInventory().add(golemStack)) {
                player.drop(golemStack, false);
            }
        }
    }

    public static boolean isOre(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER)
                || stack.is(Items.RAW_GOLD) || stack.is(Items.DIAMOND) || stack.is(Items.EMERALD)
                || stack.is(Items.LAPIS_LAZULI) || stack.is(Items.REDSTONE) || stack.is(Items.QUARTZ)
                || stack.is(Items.AMETHYST_SHARD) || stack.is(Items.IRON_INGOT) || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.COPPER_INGOT) || stack.is(Items.RAW_IRON_BLOCK) || stack.is(Items.RAW_COPPER_BLOCK)
                || stack.is(Items.RAW_GOLD_BLOCK) || stack.is(Items.NETHERITE_SCRAP) || stack.is(Items.ANCIENT_DEBRIS);
    }

    public static boolean isPickaxe(ItemStack stack) {
        return stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE) ||
                stack.is(Items.IRON_PICKAXE) || stack.is(Items.DIAMOND_PICKAXE) ||
                stack.is(Items.NETHERITE_PICKAXE) || stack.is(Items.GOLDEN_PICKAXE) ||
                stack.is(Items.COPPER_PICKAXE);
    }

    public static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String name = stack.getItem().toString().toLowerCase();
        return name.contains("spear");
    }

    public static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
                stack.is(Items.IRON_SWORD) || stack.is(Items.DIAMOND_SWORD) ||
                stack.is(Items.NETHERITE_SWORD) || stack.is(Items.GOLDEN_SWORD) ||
                stack.is(Items.COPPER_SWORD);
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) ||
                stack.is(Items.IRON_AXE) || stack.is(Items.DIAMOND_AXE) ||
                stack.is(Items.NETHERITE_AXE) || stack.is(Items.GOLDEN_AXE) ||
                stack.is(Items.COPPER_AXE);
    }

    public static boolean isHoe(ItemStack stack) {
        return stack.is(Items.WOODEN_HOE) || stack.is(Items.STONE_HOE) ||
                stack.is(Items.IRON_HOE) || stack.is(Items.DIAMOND_HOE) ||
                stack.is(Items.NETHERITE_HOE) || stack.is(Items.GOLDEN_HOE) ||
                stack.is(Items.COPPER_HOE);
    }

    public static boolean isFishingRod(ItemStack stack) {
        return stack.is(Items.FISHING_ROD);
    }

    public static boolean isShears(ItemStack stack) {
        return stack.is(Items.SHEARS);
    }

    public static boolean isTorch(ItemStack stack) {
        return stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH) || stack.is(Items.COPPER_TORCH);
    }

    public static boolean isShovel(ItemStack stack) {
        return stack.is(Items.WOODEN_SHOVEL) || stack.is(Items.STONE_SHOVEL) ||
                stack.is(Items.IRON_SHOVEL) || stack.is(Items.DIAMOND_SHOVEL) ||
                stack.is(Items.NETHERITE_SHOVEL) || stack.is(Items.GOLDEN_SHOVEL) ||
                stack.is(Items.COPPER_SHOVEL);
    }

    public static boolean isBow(ItemStack stack) {
        return stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
    }

    public static boolean isShield(ItemStack stack) {
        return stack.is(Items.SHIELD);
    }

    public static boolean isTrident(ItemStack stack) {
        return stack.is(Items.TRIDENT);
    }

    public static boolean isMace(ItemStack stack) {
        return stack.is(Items.MACE);
    }

    public static boolean isFlintAndSteel(ItemStack stack) {
        return stack.is(Items.FLINT_AND_STEEL);
    }

    public static boolean isTool(ItemStack stack) {
        return isPickaxe(stack) || isSword(stack) || isAxe(stack) || isHoe(stack) || isShovel(stack) || isFishingRod(stack) || isShears(stack)
                || isBow(stack) || isShield(stack) || isTrident(stack) || isMace(stack) || isFlintAndSteel(stack) || isSpear(stack) || stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET);
    }

    public static boolean isLightSource(BlockState state) {
        return state.is(BlockTags.CANDLES) || state.is(BlockTags.CAMPFIRES) || state.is(Blocks.TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.COPPER_TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH) || state.is(Blocks.COPPER_WALL_TORCH) || state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN) || state.is(Blocks.GLOWSTONE) || state.is(Blocks.SEA_LANTERN) || state.is(Blocks.OCHRE_FROGLIGHT) || state.is(Blocks.PEARLESCENT_FROGLIGHT) || state.is(Blocks.VERDANT_FROGLIGHT) || state.is(Blocks.JACK_O_LANTERN) || state.is(Blocks.SHROOMLIGHT);
    }

    private BlockPos chestPos;
    private BlockPos farmTarget;
    private final Map<BlockPos, Long> plantedChorusFlowers = new java.util.HashMap<>();

    public void recordChorusPlanting(BlockPos pos) {
        this.plantedChorusFlowers.put(pos, this.level().getGameTime());
    }

    public boolean isChorusReady(BlockPos pos) {
        if (!this.plantedChorusFlowers.containsKey(pos)) {
            // If we don't know when it was planted, assume it's ready 
            // (might have been planted by a different golem or player)
            return true;
        }
        long plantedTime = this.plantedChorusFlowers.get(pos);
        long currentTime = this.level().getGameTime();
        // 2 minutes = 120 seconds = 2400 ticks
        return (currentTime - plantedTime) >= 2400;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput writeView) {
        super.addAdditionalSaveData(writeView);
        writeView.putInt("BuildPattern", this.getBuildPattern().ordinal());
        writeView.putInt("WallWidth", this.getWallWidth());
        writeView.putInt("WallLength", this.getWallLength());
        writeView.putBoolean("BuildingStarted", this.isBuildingStarted());
        writeView.putInt("XpScore", this.getXpScore());
        writeView.putBoolean("LampOn", this.isLampOn());
        writeView.putBoolean("Stripped", this.isStripped());
        this.inventory.storeAsItemList(writeView.list("Inventory", ItemStack.CODEC));
        this.furnaceInventory.storeAsItemList(writeView.list("FurnaceInventory", ItemStack.CODEC));
        this.jukeboxInventory.storeAsItemList(writeView.list("JukeboxInventory", ItemStack.CODEC));
        if (!this.currentlyPlayingStack.isEmpty()) {
            writeView.store("PlayingDisc", ItemStack.CODEC, this.currentlyPlayingStack);
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
            writeView.store("SelectedBuyItem", ItemStack.CODEC, this.getSelectedBuyItem());
        }
        if (this.chestPos != null) {
            writeView.putInt("ChestX", this.chestPos.getX());
            writeView.putInt("ChestY", this.chestPos.getY());
            writeView.putInt("ChestZ", this.chestPos.getZ());
        }
        if (!this.getSchematicName().isEmpty()) {
            writeView.putString("SchematicName", this.getSchematicName());
        }
        writeView.putInt("MiningDirection", this.entityData.get(MINING_DIRECTION));
        // Persist current animation for seamless reloads
        writeView.putInt("AnimationId", this.entityData.get(ANIMATION_ID));
        writeView.putInt("AnimationTicks", this.entityData.get(ANIMATION_TICKS));
        writeView.putInt("AnimationStartTicks", this.entityData.get(ANIMATION_START_TICKS));
        writeView.putInt("DeletedItemsCount", this.getDeletedItemsCount());
        writeView.putInt("GoldTradeCount", this.getGoldTradeCount());
        writeView.putString("OwnerUUID", this.entityData.get(OWNER_UUID_STRING));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput readView) {
        super.readAdditionalSaveData(readView);
        this.setBuildPattern(BuildPattern.values()[readView.getIntOr("BuildPattern", 0)]);
        this.setWallWidth(readView.getIntOr("WallWidth", 3));
        this.setWallLength(readView.getIntOr("WallLength", 3));
        this.setBuildingStarted(readView.getBooleanOr("BuildingStarted", false));
        this.setXpScore(readView.getIntOr("XpScore", 0));
        this.setLampOn(readView.getBooleanOr("LampOn", false));
        this.setStripped(readView.getBooleanOr("Stripped", false));
        this.inventory.fromItemList(readView.listOrEmpty("Inventory", ItemStack.CODEC));
        this.furnaceInventory.fromItemList(readView.listOrEmpty("FurnaceInventory", ItemStack.CODEC));
        this.jukeboxInventory.fromItemList(readView.listOrEmpty("JukeboxInventory", ItemStack.CODEC));
        readView.read("PlayingDisc", ItemStack.CODEC).ifPresent(stack -> this.currentlyPlayingStack = stack);
        this.jukeboxCooldown = readView.getIntOr("JukeboxCooldown", 0);
        this.currentJukeboxSlot = readView.getIntOr("CurrentJukeboxSlot", -1);
        this.setJukeboxPlaying(readView.getBooleanOr("JukeboxPlaying", false));
        this.setJukeboxShuffle(readView.getBooleanOr("JukeboxShuffle", false));
        this.setJukeboxRepeat(readView.getBooleanOr("JukeboxRepeat", false));
        this.burnTime = readView.getIntOr("BurnTime", 0);
        this.fuelTime = readView.getIntOr("FuelTime", 0);
        this.cookTime = readView.getIntOr("CookTime", 0);
        this.cookTimeTotal = readView.getIntOr("CookTimeTotal", 0);
        readView.read("SelectedBuyItem", ItemStack.CODEC).ifPresent(this::setSelectedBuyItem);
        if (readView.getInt("ChestX").isPresent()) {
            this.chestPos = new BlockPos(readView.getIntOr("ChestX", 0), readView.getIntOr("ChestY", 0), readView.getIntOr("ChestZ", 0));
        }
        this.setSchematicName(readView.getStringOr("SchematicName", ""));
        this.entityData.set(MINING_DIRECTION, readView.getIntOr("MiningDirection", -1));
        // Restore animation
        int animId = readView.getIntOr("AnimationId", GolemAnimation.IDLE.ordinal());
        int animTicks = readView.getIntOr("AnimationTicks", 0);
        int animStartTicks = readView.getIntOr("AnimationStartTicks", Math.max(1, animTicks));
        this.entityData.set(ANIMATION_ID, animId);
        this.entityData.set(ANIMATION_TICKS, animTicks);
        this.entityData.set(ANIMATION_START_TICKS, animStartTicks);
        this.setDeletedItemsCount(readView.getIntOr("DeletedItemsCount", 0));
        this.setGoldTradeCount(readView.getIntOr("GoldTradeCount", 0));
        this.entityData.set(OWNER_UUID_STRING, readView.getStringOr("OwnerUUID", ""));

        updateAttackDamage();
    }

    public BlockPos getChestPos() {
        return chestPos;
    }

    public BlockPos getFarmTarget() {
        return farmTarget;
    }

    public void setFarmTarget(BlockPos farmTarget) {
        this.farmTarget = farmTarget;
    }

    public void setChestPos(BlockPos chestPos) {
        this.chestPos = chestPos;
        this.setMiningDirection(null); // Reset mining direction when chest changes
    }

    public net.minecraft.world.Container getChestInventory(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof GolemChestBlock block) {
            return GolemChestBlockEntity.getInventory(block, state, this.level(), pos, false);
        }
        BlockEntity be = this.level().getBlockEntity(pos);
        if (be instanceof net.minecraft.world.Container inv) {
            return inv;
        }
        return null;
    }

    public BlockPos findNearbyChest() {
        if (this.getGolemType() == GolemType.MEDIC) return null;
        if (this.chestPos != null) {
            if (this.isBlacklisted(this.chestPos)) {
                this.chestPos = null;
            } else {
                BlockEntity be = this.level().getBlockEntity(this.chestPos);
                BlockState bs = this.level().getBlockState(this.chestPos);
                if (be instanceof net.minecraft.world.Container && bs.getBlock() == this.getGolemType().getChestBlock()) {
                    return this.chestPos;
                }
            }
        }

        BlockPos pos = this.blockPosition();
        int range = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS || this.getGolemType() == GolemType.BAMBOO) ? 32 : 16;
        int verticalRange = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS || this.getGolemType() == GolemType.BAMBOO) ? 15 : 4;
        
        for (int x = -range; x <= range; x++) {
            for (int y = -verticalRange; y <= verticalRange; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    if (this.isBlacklisted(p)) continue;
                    BlockEntity be = this.level().getBlockEntity(p);
                    BlockState bs = this.level().getBlockState(p);
                    if (be instanceof net.minecraft.world.Container && bs.getBlock() == this.getGolemType().getChestBlock()) {
                        this.setChestPos(p);
                        return p;
                    }
                }
            }
        }

        // Nether wart golems can fallback to normal chests if no golem chest found
        if (this.getGolemType() == GolemType.NETHER_WART) {
            for (int x = -range; x <= range; x++) {
                for (int y = -verticalRange; y <= verticalRange; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (this.isBlacklisted(p)) continue;
                        BlockEntity be = this.level().getBlockEntity(p);
                        BlockState bs = this.level().getBlockState(p);
                        if (be instanceof net.minecraft.world.Container && (bs.getBlock() == net.minecraft.world.level.block.Blocks.CHEST || bs.getBlock() == net.minecraft.world.level.block.Blocks.TRAPPED_CHEST || bs.getBlock() == net.minecraft.world.level.block.Blocks.BARREL)) {
                            this.setChestPos(p);
                            return p;
                        }
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, spawnReason, entityData);

        // Equip items based on type
        ItemStack item = ItemStack.EMPTY;
        if (golemType == GolemType.REDSTONE) {
            item = new ItemStack(Items.REDSTONE);
        } else if (golemType == GolemType.EMERALD) {
            item = new ItemStack(Items.EMERALD);
        }

        if (!item.isEmpty()) {
            this.setItemSlot(HELD_ITEM_SLOT, item);
            // Never equip the UtilityGolemEntity.POPPY_SLOT to avoid vanilla copper golem behaviors being triggered
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
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level world) {
        return new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, world);
    }

    @Override
    protected void registerGoals() {
        // Clear any goals inherited from parent to avoid unintended behaviors (e.g., random chest checks)
        this.goalSelector.removeAllGoals(goal -> true);
        this.targetSelector.removeAllGoals(goal -> true);

        this.goalSelector.addGoal(0, new GolemAI.DebugGoalWrapper(this, new net.minecraft.world.entity.ai.goal.FloatGoal(this)));
        this.goalSelector.addGoal(0, new GolemAI.DebugGoalWrapper(this, new net.minecraft.world.entity.ai.goal.OpenDoorGoal(this, true)));
        this.goalSelector.addGoal(0, new GolemAI.DebugGoalWrapper(this, new GolemAI.ClimbLadderGoal(this)));

        initGolemsGoals();
        
        // Remove any remaining goals that might have been added by UtilityGolemEntity after our clear
        // specifically targeting anything that looks like chest sorting or putting away items if we could identify them.
        // Since we already cleared them, we are mostly safe unless UtilityGolemEntity adds them in a way we don't expect.
    }

    // ===== Override UtilityGolemEntity container interaction hooks to fully disable vanilla chest use =====
    @Override
    public void setOpenedChestPos(BlockPos pos) {
        // Intentionally ignore container targeting to prevent any vanilla copper golem item transport
    }

    @Override
    public boolean hasContainerOpen(net.minecraft.world.level.block.entity.ContainerOpenersCounter viewerCountManager, BlockPos pos) {
        // Never considered as viewing any container
        return false;
    }

    @Override
    public double getContainerInteractionRange() {
        // Zero range prevents interaction checks from succeeding
        return 0.0;
    }


    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.Entity target) {
        if (this.attackCooldown > 0) {
            return false;
        }

        boolean isNetheriteOrAncient = this.getGolemType() == GolemType.NETHERITE || this.getGolemType() == GolemType.ANCIENT;
        ItemStack heldItem = this.getHeldItem();

        if (isNetheriteOrAncient) {
            List<ItemStack> weapons = new ArrayList<>();
            if (isSword(heldItem) || isAxe(heldItem) || isSpear(heldItem) || isTrident(heldItem) || isMace(heldItem)) {
                weapons.add(heldItem);
            }
            for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
                ItemStack stack = this.getInventory().getItem(i);
                if (isSword(stack) || isAxe(stack) || isSpear(stack) || isTrident(stack) || isMace(stack)) {
                    weapons.add(stack);
                }
            }

            if (!weapons.isEmpty()) {
                heldItem = weapons.get(this.getRandom().nextInt(weapons.size()));
                // We don't necessarily swap the main hand item permanently, 
                // but we use this chosen item for the attack logic.
                // However, updateAttackDamage() uses getHeldItem(), so it might be better 
                // to temporarily set it or pass it to updateAttackDamage.
            }

            if (isSpear(heldItem) || isTrident(heldItem)) {
                // Lunge attack
                net.minecraft.world.phys.Vec3 targetPos = new net.minecraft.world.phys.Vec3(target.getX(), target.getY(), target.getZ());
                net.minecraft.world.phys.Vec3 myPos = new net.minecraft.world.phys.Vec3(this.getX(), this.getY(), this.getZ());
                net.minecraft.world.phys.Vec3 vec3d = targetPos.subtract(myPos).normalize().multiply(1.5, 0.1, 1.5);
                this.push(vec3d.x, vec3d.y + 0.2, vec3d.z);
                this.attackCooldown = 40; // 2 seconds cooldown
                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0f, 1.0f);
            } else if (isAxe(heldItem)) {
                this.attackCooldown = 30; // 1.5 seconds cooldown
            } else if (isMace(heldItem)) {
                // Mace jump attack
                this.push(0, 0.6, 0);
                this.attackCooldown = 60; // 3 seconds cooldown
                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0f, 1.0f);
            }
        }

        // To ensure the correct damage is applied, we might need to temporarily swap the item
        ItemStack originalHeldItem = this.getHeldItem();
        boolean success;
        try {
            if (isNetheriteOrAncient && (heldItem != originalHeldItem || isSpear(heldItem) || isTrident(heldItem) || isAxe(heldItem) || isMace(heldItem))) {
                this.setHeldItem(heldItem);
                this.updateAttackDamage();
            }
            success = super.doHurtTarget(world, target);
            if (success && isNetheriteOrAncient && target instanceof LivingEntity livingTarget && this.getOwnerUuid().isPresent()) {
                livingTarget.setLastHurtByMob(null); // Clear mob attacker to ensure player takes precedence for XP
                // In 1.21.1, we can't easily set lastAttackedByPlayerTime directly as it's private.
                // However, we can use a damage source that is attributed to a player if we have the player entity.
                Player owner = world.getPlayerInAnyDimension(this.getOwnerUuid().get());
                if (owner != null) {
                    livingTarget.hurt(this.damageSources().playerAttack(owner), 0.0f);
                }
            }
        } finally {
            if (isNetheriteOrAncient && (heldItem != originalHeldItem || isSpear(heldItem) || isTrident(heldItem) || isAxe(heldItem) || isMace(heldItem))) {
                this.setHeldItem(originalHeldItem);
                this.updateAttackDamage();
            }
        }

        if (success) {
            if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                // Apply knockback enchantment
                int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), heldItem);
                if (knockbackLevel > 0) {
                    livingTarget.knockback((float) knockbackLevel * 0.5f, Math.sin(this.getYRot() * (Math.PI / 180.0)), -Math.cos(this.getYRot() * (Math.PI / 180.0)));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                }

                // Apply fire aspect enchantment
                int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT), heldItem);
                if (fireAspectLevel > 0) {
                    livingTarget.igniteForSeconds(fireAspectLevel * 4);
                }

                // Apply post-attack effects
                EnchantmentHelper.doPostAttackEffects(world, livingTarget, this.damageSources().mobAttack(this));
            }

            // Netherite/Ancient sweeping attack (Sword only)
            if (isNetheriteOrAncient && isSword(heldItem)) {
                this.spawnSweepingAttackParticles(world);
                this.applySweepingDamage(world, target, heldItem);
            }
        }
        return success;
    }

    private void spawnSweepingAttackParticles(net.minecraft.server.level.ServerLevel world) {
        double d = -Math.sin(this.getYRot() * (Math.PI / 180.0));
        double e = Math.cos(this.getYRot() * (Math.PI / 180.0));
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
    }

    private void applySweepingDamage(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.Entity target, ItemStack heldItem) {
        float damage = (float)this.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        int sweepingLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE), heldItem);
        float sweepingDamage = 1.0f + (sweepingLevel > 0 ? (float)sweepingLevel / (float)(sweepingLevel + 1) : 0.0f) * damage;
        
        for (net.minecraft.world.entity.LivingEntity livingEntity : world.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, target.getBoundingBox().inflate(1.0, 0.25, 1.0), (e) -> {
            return e != this && e != target && !this.isAlliedTo(e) && this.canAttack(e) && (!(e instanceof net.minecraft.world.entity.decoration.ArmorStand) || !((net.minecraft.world.entity.decoration.ArmorStand)e).isMarker()) && this.distanceToSqr(e) < 9.0;
        })) {
            livingEntity.knockback(0.4000000059604645, Math.sin(this.getYRot() * (Math.PI / 180.0)), -Math.cos(this.getYRot() * (Math.PI / 180.0)));
            DamageSource source = this.damageSources().mobAttack(this);
            if (this.getOwnerUuid().isPresent()) {
                Player owner = world.getPlayerInAnyDimension(this.getOwnerUuid().get());
                if (owner != null) {
                    source = this.damageSources().playerAttack(owner);
                }
            }
            livingEntity.hurt(source, sweepingDamage);
        }
        
        world.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0f, 1.0f);
    }

    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        if (this.getGolemType() == GolemType.NETHERITE || this.getGolemType() == GolemType.ANCIENT) {
            if (target != null) {
                this.debugLog("Targeting " + target.getType().getDescription().getString() + " at " + target.blockPosition().toShortString() + " (HP: " + (int)target.getHealth() + "/" + (int)target.getMaxHealth() + ")");
            } else if (this.getTarget() != null) {
                net.minecraft.world.entity.LivingEntity oldTarget = this.getTarget();
                this.debugLog("Target cleared (null). Was " + oldTarget.getType().getDescription().getString() + " (Dead: " + oldTarget.isDeadOrDying() + ", Removed: " + oldTarget.isRemoved() + ")");
            }
        }
        super.setTarget(target);
    }

    @Override
    public boolean canPickUpLoot() {
        if (this.golemType == GolemType.REDSTONE || this.golemType == GolemType.CACTUS) return false;
        return super.canPickUpLoot();
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        if (target instanceof net.minecraft.world.entity.animal.allay.Allay) return false;
        if (target.isDeadOrDying() || target.isRemoved()) return false;
        
        // Netherite and Ancient golems only target hostile mobs
        if (this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) {
            return target instanceof net.minecraft.world.entity.monster.Monster;
        }
        
        return super.canAttack(target);
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        return super.canBeAffected(effect);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof net.minecraft.world.entity.animal.allay.Allay) return;
        super.push(entity);
    }

    public net.minecraft.world.entity.ai.goal.GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public net.minecraft.world.entity.ai.goal.GoalSelector getTargetSelector() {
        return this.targetSelector;
    }

    public ItemStack getHeldItem() {
        return this.getItemBySlot(HELD_ITEM_SLOT);
    }

    public void setHeldItem(ItemStack stack) {
        this.setItemSlot(HELD_ITEM_SLOT, stack);
    }

    private void updateAttackDamage() {
        ConfigManager.GolemStats stats = ConfigManager.getConfig().golems.get(this.getGolemType().getName());
        float baseDamage = (stats != null) ? (float) stats.attackDamage : 0.5f; 
        
        ItemStack stack = this.getHeldItem();
        
        if (stack.is(Items.NETHERITE_SWORD)) baseDamage += 8.0f;
        else if (stack.is(Items.DIAMOND_SWORD)) baseDamage += 7.0f;
        else if (stack.is(Items.IRON_SWORD)) baseDamage += 6.0f;
        else if (stack.is(Items.STONE_SWORD)) baseDamage += 5.0f;
        else if (stack.is(Items.WOODEN_SWORD)) baseDamage += 4.0f;
        else if (stack.is(Items.GOLDEN_SWORD)) baseDamage += 4.0f;
        else if (stack.is(Items.NETHERITE_AXE)) baseDamage += 10.0f;
        else if (stack.is(Items.DIAMOND_AXE)) baseDamage += 9.0f;
        else if (stack.is(Items.IRON_AXE)) baseDamage += 9.0f;
        else if (stack.is(Items.STONE_AXE)) baseDamage += 9.0f;
        else if (stack.is(Items.WOODEN_AXE)) baseDamage += 7.0f;
        else if (stack.is(Items.GOLDEN_AXE)) baseDamage += 7.0f;
        else if (stack.is(Items.TRIDENT)) baseDamage += 9.0f;
        else if (stack.is(Items.MACE)) baseDamage += 11.0f;
        else if (isSpear(stack)) {
            if (stack.getItem().toString().contains("netherite")) baseDamage += 10.0f;
            else if (stack.getItem().toString().contains("diamond")) baseDamage += 9.0f;
            else if (stack.getItem().toString().contains("iron")) baseDamage += 8.0f;
            else if (stack.getItem().toString().contains("stone")) baseDamage += 7.0f;
            else baseDamage += 6.0f;
        }
        
        var instance = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.setBaseValue((double)baseDamage);
        }
    }
}
