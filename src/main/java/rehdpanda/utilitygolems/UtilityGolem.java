// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package rehdpanda.utilitygolems;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.effect.MobEffectInstance;
import rehdpanda.utilitygolems.UtilityGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

// Base class for Utility Golems
import net.minecraft.world.entity.PathfinderMob;
public class UtilityGolem extends PathfinderMob implements InventoryCarrier {

    private final GolemType golemType;
    private static final EntityDataAccessor<Integer> XP_SCORE = SynchedEntityData.defineId(UtilityGolem.class, EntityDataSerializers.INT);
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleContainer inventory = new SimpleContainer(9);
    private final SimpleContainer furnaceInventory = new SimpleContainer(3);
    private final SimpleContainer jukeboxInventory = new SimpleContainer(9);
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
        if (animation == GolemAnimation.SPINNING_HEAD) {
            this.setState(net.minecraft.world.entity.animal.golem.UtilityGolemState.IDLE);
            // Trigger the spin head animation by setting the age to match the timer
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
                        player.displayClientMessage(Component.literal("[DEBUG] " + message), false);
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
        if (this.getEntityWorld() instanceof net.minecraft.server.level.ServerLevel) {
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

    public UtilityGolem(EntityType<? extends UtilityGolem> type, Level world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;
        initGolemsGoals();
        updateAttackDamage();
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.onDeath(source);
        if (!this.getEntityWorld().isClient()) {
            removeLight();

            if (this.chestPos != null) {
                BlockEntity be = this.getEntityWorld().getBlockEntity(this.chestPos);
                if (be instanceof GolemChestBlockEntity golemChest) {
                    golemChest.setGolemDead(true);
                }
            }

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

            // UtilityGolemEntity already drops what is in its POPPY_SLOT, but our golem
            // uses HELD_ITEM_SLOT (MAINHAND). We must drop it manually.
            ItemStack heldItem = this.getHeldItem();
            if (!heldItem.isEmpty()) {
                net.minecraft.world.level.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), heldItem.copy());
                this.setHeldItem(ItemStack.EMPTY);
            }
            
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.getStack(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.level.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), stack.copy());
                    this.inventory.setStack(i, ItemStack.EMPTY);
                }
            }
            for (int i = 0; i < this.furnaceInventory.size(); i++) {
                ItemStack stack = this.furnaceInventory.getStack(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.level.block.Block.dropStack(this.getEntityWorld(), this.getBlockPos(), stack.copy());
                    this.furnaceInventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel world) {
        // Skip UtilityGolemEntity.mobTick which triggers brain.tick and UtilityGolemBrain.updateActivity
        // but call super.mobTick in MobEntity to ensure target management and other base behaviors work correctly.
        super.mobTick(world);
        
        // Proactively clear target if dead or removed, as super.mobTick might be slow or skip it in some conditions.
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target != null && (target.isDead() || target.isRemoved())) {
            this.setTarget(null);
        }

        // Proactively prevent any vanilla UtilityGolem container targeting each tick
        this.resetTargetContainerPos();
    }

    @Override
    protected net.minecraft.world.entity.ai.Brain.Profile<rehdpanda.utilitygolems.UtilityGolem> brainProvider() {
        // Return an empty brain profile to ensure the copper golem brain is never initialized
        return net.minecraft.world.entity.ai.Brain.createProfile(java.util.Collections.emptyList(), java.util.Collections.emptyList());
    }

    @Override
    protected net.minecraft.world.entity.ai.Brain<?> makeBrain(com.mojang.serialization.Dynamic<?> dynamic) {
        // Return an empty brain to bypass any brain-based AI from UtilityGolemEntity
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public net.minecraft.world.entity.ai.Brain<rehdpanda.utilitygolems.UtilityGolem> getBrain() {
        return super.getBrain();
    }

    @Override
    public void tick() {
        if (this.golemType == GolemType.BAMBOO && !this.getEntityWorld().isClient()) {
            ItemStack boots = this.getEquippedStack(EquipmentSlot.FEET);
            if (!boots.isEmpty()) {
                // In 1.21.1, Enchantments are handled via Registry. 
                // We need to check if the boots have the soul speed enchantment.
                int soulSpeedLevel = EnchantmentHelper.getLevel(this.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SOUL_SPEED), boots);
                if (soulSpeedLevel > 0) {
                    BlockState standingOn = this.getEntityWorld().getBlockState(this.getBlockPos().down());
                    if (standingOn.isOf(Blocks.SOUL_SAND) || standingOn.isOf(Blocks.SOUL_SOIL)) {
                        this.getAttributeInstance(Attributes.MOVEMENT_SPEED).addTemporaryModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ResourceLocation.fromNamespaceAndPath("utility-golems", "soul_speed_boost"), 0.05D + 0.01D * (double)soulSpeedLevel, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                    } else {
                        this.getAttributeInstance(Attributes.MOVEMENT_SPEED).removeModifier(ResourceLocation.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
                    }
                } else {
                    this.getAttributeInstance(Attributes.MOVEMENT_SPEED).removeModifier(ResourceLocation.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
                }
            } else {
                this.getAttributeInstance(Attributes.MOVEMENT_SPEED).removeModifier(ResourceLocation.fromNamespaceAndPath("utility-golems", "soul_speed_boost"));
            }
        }

        if (this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) {
            // Suppression of UtilityGolemEntity behaviors is handled by disabling POPPY_SLOT 
            // and clearing goals in initGoals.
        }
        // Proactively prevent any vanilla UtilityGolem container targeting each tick (server-side)
        if (!this.getEntityWorld().isClient()) {
            this.resetTargetContainerPos();
        }

        super.tick();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (!this.getEntityWorld().isClient()) {
            Component customName = this.getCustomName();
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
                        Player player = this.getEntityWorld().getClosestPlayer(this, 10.0D);
                        if (player != null) {
                            player.dropItem(this.currentlyPlayingStack.copy(), false);
                        } else {
                            this.getEntityWorld().spawnEntity(new net.minecraft.world.entity.item.ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), this.currentlyPlayingStack.copy()));
                        }
                        this.setHeldItem(ItemStack.EMPTY);
                        this.setSearching(false);
                    }
                    this.currentlyPlayingStack = ItemStack.EMPTY;
                    this.jukeboxStartPos = null;
                }
            }
            if (!this.getEntityWorld().isClient() && this.jukeboxCooldown % 20 == 0 && this.jukeboxCooldown > 0) {
                ((net.minecraft.server.level.ServerLevel)this.getEntityWorld()).spawnParticles(ParticleTypes.NOTE, this.getParticleX(0.5D), this.getRandomBodyY() + 0.5D, this.getParticleZ(0.5D), 1, 0, 0, 0, (double)this.random.nextInt(24) / 24.0D);
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
            if (this.getEntityWorld().getTime() % 1200 == 0) {
                long currentTime = this.getEntityWorld().getTime();
                this.plantedChorusFlowers.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > 4800); // 4 minutes
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
                this.getEntityWorld().setBlockState(lastLightPos, net.minecraft.world.level.block.Blocks.AIR.getDefaultState());
            }
            lastLightPos = null;
        }
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.Entity.Entity.RemovalReason reason) {
        if (!this.getEntityWorld().isClient()) {
            removeLight();
        }
        super.remove(reason);
    }


    @Override
    public boolean onClimbable() {
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
        if (this.golemType == GolemType.SMOKER) {
            if (input.isOf(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP);
            if (input.isOf(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF);
            if (input.isOf(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN);
            if (input.isOf(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON);
            if (input.isOf(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT);
            if (input.isOf(Items.COD)) return new ItemStack(Items.COOKED_COD);
            if (input.isOf(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON);
            if (input.isOf(Items.POTATO)) return new ItemStack(Items.BAKED_POTATO);
            if (input.isOf(Items.KELP)) return new ItemStack(Items.DRIED_KELP);
            return ItemStack.EMPTY;
        }

        if (this.golemType == GolemType.BLAST_FURNACE) {
            if (input.isOf(Items.RAW_IRON) || input.isOf(Items.IRON_ORE) || input.isOf(Items.DEEPSLATE_IRON_ORE)) return new ItemStack(Items.IRON_INGOT);
            if (input.isOf(Items.RAW_GOLD) || input.isOf(Items.GOLD_ORE) || input.isOf(Items.DEEPSLATE_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
            if (input.isOf(Items.RAW_COPPER) || input.isOf(Items.COPPER_ORE) || input.isOf(Items.DEEPSLATE_COPPER_ORE)) return new ItemStack(Items.COPPER_INGOT);
            if (input.isOf(Items.NETHER_GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT);
            if (input.isOf(Items.ANCIENT_DEBRIS)) return new ItemStack(Items.NETHERITE_SCRAP);
            // Chainmail
            if (input.isOf(Items.CHAINMAIL_HELMET)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.CHAINMAIL_CHESTPLATE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.CHAINMAIL_LEGGINGS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.CHAINMAIL_BOOTS)) return new ItemStack(Items.IRON_NUGGET);
            // Iron gear
            if (input.isOf(Items.IRON_HELMET)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_CHESTPLATE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_LEGGINGS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_BOOTS)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_PICKAXE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_SHOVEL)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_AXE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_HOE)) return new ItemStack(Items.IRON_NUGGET);
            if (input.isOf(Items.IRON_SWORD)) return new ItemStack(Items.IRON_NUGGET);
            // Golden gear
            if (input.isOf(Items.GOLDEN_HELMET)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_CHESTPLATE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_LEGGINGS)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_BOOTS)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_PICKAXE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_SHOVEL)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_AXE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_HOE)) return new ItemStack(Items.GOLD_NUGGET);
            if (input.isOf(Items.GOLDEN_SWORD)) return new ItemStack(Items.GOLD_NUGGET);
            return ItemStack.EMPTY;
        }

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
        if (!this.getEntityWorld().isClient() && this.getEntityWorld() instanceof net.minecraft.server.level.ServerLevel) {
            StopSoundS2CPacket stopPacket = new StopSoundS2CPacket(null, SoundCategory.RECORDS);
            for (ServerPlayer player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
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
            SimpleContainer inv = this.getInventory();
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
                    this.getEntityWorld().spawnEntity(new net.minecraft.world.entity.item.ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), remaining));
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
        ItemStack playerStack = player.getStackInHand(hand);

        // SHIFT+RIGHT CLICK to take item back or toggle lamp
        if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (this.golemType == GolemType.LAMP) {
                if (!player.getEntityWorld().isClient()) {
                    this.setLampOn(!this.isLampOn());
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 0.5F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }

            if (this.golemType == GolemType.MEDIC) {
                ItemStack wrench = ItemStack.EMPTY;
                for (int i = 0; i < this.inventory.size(); i++) {
                    if (this.inventory.getStack(i).isOf(UGItems.WRENCH_ITEM)) {
                        wrench = this.inventory.removeStack(i);
                        break;
                    }
                }
                if (!wrench.isEmpty()) {
                    if (!player.getEntityWorld().isClient()) {
                        if (!player.getInventory().insertStack(wrench)) {
                            player.dropItem(wrench, false);
                        }
                        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
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
                return InteractionResult.SUCCESS;
            }
        }

        // Prevent using incompatible special items on the wrong golem types
        // e.g., prevent records on non-jukebox golems to avoid unintended handlers/UI and crashes
        if (this.golemType != GolemType.JUKEBOX) {
            JukeboxPlayableComponent playableCheck = playerStack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
            if (playableCheck != null) {
                if (!player.getEntityWorld().isClient()) {
                    player.sendMessage(Component.literal("This golem can't play records."), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.golemType == GolemType.REDSTONE && playerStack.isOf(Items.CLOCK)) {
            if (!player.getEntityWorld().isClient()) {
                if (!player.getAbilities().creativeMode) {
                    playerStack.decrement(1);
                }
                BlockPos pos = this.getBlockPos();
                BlockState state = UGBlocks.REDSTONE_GOLEM_STATUE.getDefaultState().with(RedstoneGolemStatueBlock.FACING, this.getHorizontalFacing().getOpposite());
                this.getEntityWorld().setBlockState(pos, state);
                BlockEntity be = this.getEntityWorld().getBlockEntity(pos);
                if (be instanceof RedstoneGolemStatueBlockEntity statueBe) {
                    if (this.hasCustomName()) {
                        statueBe.setCustomName(this.getCustomName());
                    }
                }
                this.getEntityWorld().playSound(null, pos, SoundEvents.ENTITY_COPPER_GOLEM_BECOME_STATUE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                this.discard();
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && !this.isStripped() && isAxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                this.setStripped(true);
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                if (!player.getAbilities().creativeMode) {
                    playerStack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAPIS && isPickaxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if ((this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) && (isSword(playerStack) || isAxe(playerStack) || isSpear(playerStack) || isTrident(playerStack) || isMace(playerStack))) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isAxe(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.DEEPSLATE && isShears(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.BAMBOO && isTool(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.SPONGE && isFishingRod(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.LAMP && isTorch(playerStack)) {
            if (!player.getEntityWorld().isClient()) {
                swapTool(player, playerStack);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.MEDIC && playerStack.isOf(UGItems.WRENCH_ITEM)) {
            if (!player.getEntityWorld().isClient()) {
                ItemStack disc = playerStack.copy();
                disc.setCount(1);
                ItemStack remaining = this.inventory.addStack(disc);
                if (remaining.isEmpty()) {
                    if (!player.getAbilities().creativeMode) {
                        playerStack.decrement(1);
                    }
                    this.equipStack(EquipmentSlot.MAINHAND, disc);
                    player.sendMessage(Component.literal("Gave wrench to Medic Golem"), true);
                } else {
                    player.sendMessage(Component.literal("Medic Golem's inventory is full"), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (playerStack.isOf(UGItems.WRENCH_ITEM)) {
            float currentHealth = this.getHealth();
            float maxHealth = this.getMaxHealth();
            if (currentHealth < maxHealth) {
                if (!player.getEntityWorld().isClient()) {
                    float healAmount = maxHealth * 0.25f; // Base 25% heal

                    // Efficiency enchantment increases amount of health that's healed
                    int efficiencyLevel = EnchantmentHelper.getLevel(this.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), playerStack);
                    if (efficiencyLevel > 0) {
                        healAmount += (maxHealth * 0.05f * efficiencyLevel); // Add 5% per efficiency level
                    }

                    this.heal(healAmount);
                    if (!player.getAbilities().creativeMode) {
                        playerStack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    for (int i = 0; i < 7; ++i) {
                        double d = this.random.nextGaussian() * 0.02;
                        double e = this.random.nextGaussian() * 0.02;
                        double f = this.random.nextGaussian() * 0.02;
                        ((net.minecraft.server.level.ServerLevel)this.getEntityWorld()).spawnParticles(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 1, d, e, f, 0.0);
                    }
                    player.sendMessage(Component.literal(this.golemType.getFriendlyName() + " Health: " + (int)this.getHealth() + "/" + (int)maxHealth), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!player.getEntityWorld().isClient()) {
                    player.sendMessage(Component.literal(this.golemType.getFriendlyName() + " is already at full health (" + (int)maxHealth + "/" + (int)maxHealth + ")"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.golemType == GolemType.FURNACE || this.golemType == GolemType.SMOKER || this.golemType == GolemType.BLAST_FURNACE) {
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) -> new GolemFurnaceScreenHandler(syncId, playerInventory, this.furnaceInventory, this.furnacePropertyDelegate, this),
                        this.getDisplayName()
                ));
            }
            return InteractionResult.SUCCESS;
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
                        player.sendMessage(Component.literal("Added to playlist"), true);
                    } else {
                        player.sendMessage(Component.literal("Playlist is full"), true);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Always open the Jukebox UI for Jukebox Golems
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(ServerPlayer player) {
                        return UtilityGolem.this.getId();
                    }

                    @Override
                    public Component getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, Player player) {
                        return new GolemJukeboxScreenHandler(syncId, playerInventory, UtilityGolem.this.jukeboxInventory, UtilityGolem.this);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        if (this.golemType == GolemType.REDSTONE) {
            if (!player.getEntityWorld().isClient()) {
                player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(ServerPlayer player) {
                        return UtilityGolem.this.getId();
                    }

                    @Override
                    public Component getDisplayName() {
                        return UtilityGolem.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, Player player) {
                        return new RedstoneGolemScreenHandler(syncId, playerInventory, UtilityGolem.this);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        player.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
            @Override
            public Integer getScreenOpeningData(net.minecraft.server.network.ServerPlayer player) {
                return UtilityGolem.this.getId();
            }

            @Override
            public Component getDisplayName() {
                return UtilityGolem.this.getDisplayName();
            }

            @Override
            public net.minecraft.screen.ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, Player player) {
                return new GolemInventoryScreenHandler(syncId, playerInventory, UtilityGolem.this.inventory, UtilityGolem.this);
            }
        });
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

    public static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String name = stack.getItem().toString().toLowerCase();
        return name.contains("spear");
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

    public static boolean isMace(ItemStack stack) {
        return stack.isOf(Items.MACE);
    }

    public static boolean isFlintAndSteel(ItemStack stack) {
        return stack.isOf(Items.FLINT_AND_STEEL);
    }

    public static boolean isTool(ItemStack stack) {
        return isPickaxe(stack) || isSword(stack) || isAxe(stack) || isHoe(stack) || isShovel(stack) || isFishingRod(stack) || isShears(stack)
                || isBow(stack) || isShield(stack) || isTrident(stack) || isMace(stack) || isFlintAndSteel(stack) || isSpear(stack) || stack.isOf(Items.BUCKET) || stack.isOf(Items.WATER_BUCKET);
    }

    public static boolean isLightSource(BlockState state) {
        return state.isIn(BlockTags.CANDLES) || state.isIn(BlockTags.CAMPFIRES) || state.isOf(Blocks.TORCH) || state.isOf(Blocks.SOUL_TORCH) || state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.COPPER_TORCH) || state.isOf(Blocks.WALL_TORCH) || state.isOf(Blocks.SOUL_WALL_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH) || state.isOf(Blocks.COPPER_WALL_TORCH) || state.isOf(Blocks.LANTERN) || state.isOf(Blocks.SOUL_LANTERN) || state.isOf(Blocks.GLOWSTONE) || state.isOf(Blocks.SEA_LANTERN) || state.isOf(Blocks.OCHRE_FROGLIGHT) || state.isOf(Blocks.PEARLESCENT_FROGLIGHT) || state.isOf(Blocks.VERDANT_FROGLIGHT) || state.isOf(Blocks.JACK_O_LANTERN) || state.isOf(Blocks.SHROOMLIGHT);
    }

    private BlockPos chestPos;
    private BlockPos farmTarget;
    private final Map<BlockPos, Long> plantedChorusFlowers = new java.util.HashMap<>();

    public void recordChorusPlanting(BlockPos pos) {
        this.plantedChorusFlowers.put(pos, this.getEntityWorld().getTime());
    }

    public boolean isChorusReady(BlockPos pos) {
        if (!this.plantedChorusFlowers.containsKey(pos)) {
            // If we don't know when it was planted, assume it's ready 
            // (might have been planted by a different golem or player)
            return true;
        }
        long plantedTime = this.plantedChorusFlowers.get(pos);
        long currentTime = this.getEntityWorld().getTime();
        // 2 minutes = 120 seconds = 2400 ticks
        return (currentTime - plantedTime) >= 2400;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag writeView) {
        super.writeCustomData(writeView);
        writeView.putInt("BuildPattern", this.getBuildPattern().ordinal());
        writeView.putInt("WallWidth", this.getWallWidth());
        writeView.putInt("WallLength", this.getWallLength());
        writeView.putBoolean("BuildingStarted", this.isBuildingStarted());
        writeView.putInt("XpScore", this.getXpScore());
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
        writeView.putInt("DeletedItemsCount", this.getDeletedItemsCount());
        writeView.putInt("GoldTradeCount", this.getGoldTradeCount());
        writeView.putString("OwnerUUID", this.dataTracker.get(OWNER_UUID_STRING));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag readView) {
        super.readCustomData(readView);
        this.setBuildPattern(BuildPattern.values()[readView.getInt("BuildPattern", 0)]);
        this.setWallWidth(readView.getInt("WallWidth", 3));
        this.setWallLength(readView.getInt("WallLength", 3));
        this.setBuildingStarted(readView.getBoolean("BuildingStarted", false));
        this.setXpScore(readView.getInt("XpScore", 0));
        this.setLampOn(readView.getBoolean("LampOn", false));
        this.setStripped(readView.getBoolean("Stripped", false));
        net.minecraft.inventory.Inventories.readData(readView.getCompoundTag("Inventory"), this.inventory.getHeldStacks());
        net.minecraft.inventory.Inventories.readData(readView.getCompoundTag("FurnaceInventory"), this.furnaceInventory.getHeldStacks());
        net.minecraft.inventory.Inventories.readData(readView.getCompoundTag("JukeboxInventory"), this.jukeboxInventory.getHeldStacks());
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
        this.setDeletedItemsCount(readView.getInt("DeletedItemsCount", 0));
        this.setGoldTradeCount(readView.getInt("GoldTradeCount", 0));
        this.dataTracker.set(OWNER_UUID_STRING, readView.getString("OwnerUUID", ""));

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
        if (this.getGolemType() == GolemType.MEDIC) return null;
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
        int range = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS || this.getGolemType() == GolemType.BAMBOO) ? 32 : 16;
        int verticalRange = (this.getGolemType() == GolemType.DEEPSLATE || this.getGolemType() == GolemType.LAPIS || this.getGolemType() == GolemType.BAMBOO) ? 15 : 4;
        
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

        // Nether wart golems can fallback to normal chests if no golem chest found
        if (this.getGolemType() == GolemType.NETHER_WART) {
            for (int x = -range; x <= range; x++) {
                for (int y = -verticalRange; y <= verticalRange; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (this.isBlacklisted(p)) continue;
                        BlockEntity be = this.getEntityWorld().getBlockEntity(p);
                        BlockState bs = this.getEntityWorld().getBlockState(p);
                        if (be instanceof Inventory && (bs.getBlock() == net.minecraft.world.level.block.Blocks.CHEST || bs.getBlock() == net.minecraft.world.level.block.Blocks.TRAPPED_CHEST || bs.getBlock() == net.minecraft.world.level.block.Blocks.BARREL)) {
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
    public SynchedEntityData finalizeSpawn(ServerLevelAccess world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SynchedEntityData entityData) {
        SynchedEntityData data = super.initialize(world, difficulty, spawnReason, entityData);

        // Equip items based on type
        ItemStack item = ItemStack.EMPTY;
        if (golemType == GolemType.REDSTONE) {
            item = new ItemStack(Items.REDSTONE);
        } else if (golemType == GolemType.EMERALD) {
            item = new ItemStack(Items.EMERALD);
        }

        if (!item.isEmpty()) {
            this.equipStack(HELD_ITEM_SLOT, item);
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
    protected PathNavigation createNavigation(Level world) {
        return new MobNavigation(this, world);
    }

    @Override
    protected void registerGoals() {
        // Clear any goals inherited from parent to avoid unintended behaviors (e.g., random chest checks)
        this.goalSelector.getGoals().clear();
        this.targetSelector.getGoals().clear();

        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new SwimGoal(this)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new net.minecraft.world.entity.ai.goal.LongDoorInteractGoal(this, true)));
        this.goalSelector.add(0, new GolemAI.DebugGoalWrapper(this, new GolemAI.ClimbLadderGoal(this)));

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
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.getStack(i);
                if (isSword(stack) || isAxe(stack) || isSpear(stack) || isTrident(stack) || isMace(stack)) {
                    weapons.add(stack);
                }
            }

            if (!weapons.isEmpty()) {
                heldItem = weapons.get(this.random.nextInt(weapons.size()));
                // We don't necessarily swap the main hand item permanently, 
                // but we use this chosen item for the attack logic.
                // However, updateAttackDamage() uses getHeldItem(), so it might be better 
                // to temporarily set it or pass it to updateAttackDamage.
            }

            if (isSpear(heldItem) || isTrident(heldItem)) {
                // Lunge attack
                net.minecraft.util.math.Vec3d targetPos = new net.minecraft.util.math.Vec3d(target.getX(), target.getY(), target.getZ());
                net.minecraft.util.math.Vec3d myPos = new net.minecraft.util.math.Vec3d(this.getX(), this.getY(), this.getZ());
                net.minecraft.util.math.Vec3d vec3d = targetPos.subtract(myPos).normalize().multiply(1.5, 0.1, 1.5);
                this.addVelocity(vec3d.x, vec3d.y + 0.2, vec3d.z);
                this.attackCooldown = 40; // 2 seconds cooldown
                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0f, 1.0f);
            } else if (isAxe(heldItem)) {
                this.attackCooldown = 30; // 1.5 seconds cooldown
            } else if (isMace(heldItem)) {
                // Mace jump attack
                this.addVelocity(0, 0.6, 0);
                this.attackCooldown = 60; // 3 seconds cooldown
                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0f, 1.0f);
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
            success = super.tryAttack(world, target);
            if (success && isNetheriteOrAncient && target instanceof LivingEntity livingTarget && this.getOwnerUuid().isPresent()) {
                livingTarget.setAttacker(null); // Clear mob attacker to ensure player takes precedence for XP
                // In 1.21.1, we can't easily set lastAttackedByPlayerTime directly as it's private.
                // However, we can use a damage source that is attributed to a player if we have the player entity.
                Player owner = world.getPlayerByUuid(this.getOwnerUuid().get());
                if (owner != null) {
                    livingTarget.damage(world, this.getDamageSources().playerAttack(owner), 0.0f);
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
                int knockbackLevel = EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), heldItem);
                if (knockbackLevel > 0) {
                    livingTarget.takeKnockback((float) knockbackLevel * 0.5f, Math.sin(this.getYaw() * (Math.PI / 180.0)), -Math.cos(this.getYaw() * (Math.PI / 180.0)));
                    this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
                }

                // Apply fire aspect enchantment
                int fireAspectLevel = EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT), heldItem);
                if (fireAspectLevel > 0) {
                    livingTarget.setOnFireFor(fireAspectLevel * 4);
                }

                // Apply post-attack effects
                EnchantmentHelper.onTargetDamaged(world, livingTarget, this.getDamageSources().mobAttack(this));
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
        double d = -Math.sin(this.getYaw() * (Math.PI / 180.0));
        double e = Math.cos(this.getYaw() * (Math.PI / 180.0));
        world.spawnParticles(net.minecraft.particle.ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
    }

    private void applySweepingDamage(net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.Entity target, ItemStack heldItem) {
        float damage = (float)this.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        int sweepingLevel = EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOrThrow(net.minecraft.enchantment.Enchantments.SWEEPING_EDGE), heldItem);
        float sweepingDamage = 1.0f + (sweepingLevel > 0 ? (float)sweepingLevel / (float)(sweepingLevel + 1) : 0.0f) * damage;
        
        for (net.minecraft.world.entity.LivingEntity livingEntity : world.getEntitiesByClass(net.minecraft.world.entity.LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0), (entity) -> {
            return entity != this && entity != target && !this.isTeammate(entity) && this.canTarget(entity) && (!(entity instanceof net.minecraft.entity.decoration.ArmorStandEntity) || !((net.minecraft.entity.decoration.ArmorStandEntity)entity).isMarker()) && this.squaredDistanceTo(entity) < 9.0;
        })) {
            livingEntity.takeKnockback(0.4000000059604645, Math.sin(this.getYaw() * (Math.PI / 180.0)), -Math.cos(this.getYaw() * (Math.PI / 180.0)));
            DamageSource source = this.getDamageSources().mobAttack(this);
            if (this.getOwnerUuid().isPresent()) {
                Player owner = world.getPlayerByUuid(this.getOwnerUuid().get());
                if (owner != null) {
                    source = this.getDamageSources().playerAttack(owner);
                }
            }
            livingEntity.damage(world, source, sweepingDamage);
        }
        
        world.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0f, 1.0f);
    }

    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        if (this.getGolemType() == GolemType.NETHERITE || this.getGolemType() == GolemType.ANCIENT) {
            if (target != null) {
                this.debugLog("Targeting " + target.getType().getName().getString() + " at " + target.getBlockPos().toShortString() + " (HP: " + (int)target.getHealth() + "/" + (int)target.getMaxHealth() + ")");
            } else if (this.getTarget() != null) {
                net.minecraft.world.entity.LivingEntity oldTarget = this.getTarget();
                this.debugLog("Target cleared (null). Was " + oldTarget.getType().getName().getString() + " (Dead: " + oldTarget.isDead() + ", Removed: " + oldTarget.isRemoved() + ")");
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
        if (target instanceof net.minecraft.entity.passive.AllayEntity) return false;
        if (target.isDead() || target.isRemoved()) return false;
        
        // Netherite and Ancient golems only target hostile mobs
        if (this.golemType == GolemType.NETHERITE || this.golemType == GolemType.ANCIENT) {
            return target instanceof net.minecraft.entity.mob.HostileEntity;
        }
        
        return super.canTarget(target);
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        return super.canHaveStatusEffect(effect);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof net.minecraft.entity.passive.AllayEntity) return;
        super.pushAway(entity);
    }

    public net.minecraft.world.entity.ai.goal.GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public net.minecraft.world.entity.ai.goal.GoalSelector getTargetSelector() {
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
        
        if (stack.isOf(Items.NETHERITE_SWORD)) baseDamage += 8.0f;
        else if (stack.isOf(Items.DIAMOND_SWORD)) baseDamage += 7.0f;
        else if (stack.isOf(Items.IRON_SWORD)) baseDamage += 6.0f;
        else if (stack.isOf(Items.STONE_SWORD)) baseDamage += 5.0f;
        else if (stack.isOf(Items.WOODEN_SWORD)) baseDamage += 4.0f;
        else if (stack.isOf(Items.GOLDEN_SWORD)) baseDamage += 4.0f;
        else if (stack.isOf(Items.NETHERITE_AXE)) baseDamage += 10.0f;
        else if (stack.isOf(Items.DIAMOND_AXE)) baseDamage += 9.0f;
        else if (stack.isOf(Items.IRON_AXE)) baseDamage += 9.0f;
        else if (stack.isOf(Items.STONE_AXE)) baseDamage += 9.0f;
        else if (stack.isOf(Items.WOODEN_AXE)) baseDamage += 7.0f;
        else if (stack.isOf(Items.GOLDEN_AXE)) baseDamage += 7.0f;
        else if (stack.isOf(Items.TRIDENT)) baseDamage += 9.0f;
        else if (stack.isOf(Items.MACE)) baseDamage += 11.0f;
        else if (isSpear(stack)) {
            if (stack.getItem().toString().contains("netherite")) baseDamage += 10.0f;
            else if (stack.getItem().toString().contains("diamond")) baseDamage += 9.0f;
            else if (stack.getItem().toString().contains("iron")) baseDamage += 8.0f;
            else if (stack.getItem().toString().contains("stone")) baseDamage += 7.0f;
            else baseDamage += 6.0f;
        }
        
        var instance = this.getAttributeInstance(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.setBaseValue(baseDamage);
        }
    }
}
