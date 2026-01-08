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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/// BASE FOR THE GOLEM EXTENDS THE COPPER GOLEMS,
/// DEFINES BASIC BEHAVOUR, TYPE AND HELD ITEM

public class UtilityGolem extends CopperGolemEntity implements InventoryOwner {

    private final GolemType golemType;
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;
    private final SimpleInventory inventory = new SimpleInventory(9);

    public UtilityGolem(EntityType<? extends UtilityGolem> type, World world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;
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
        if (!player.getEntityWorld().isClient()) {
            player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, p) -> new GolemInventoryScreenHandler(syncId, playerInventory, this.inventory, this),
                this.getDisplayName()
            ));
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void writeCustomData(net.minecraft.storage.WriteView writeView) {
        super.writeCustomData(writeView);
        net.minecraft.inventory.Inventories.writeData(writeView.get("Inventory"), this.inventory.getHeldStacks());
    }

    @Override
    public void readCustomData(net.minecraft.storage.ReadView readView) {
        super.readCustomData(readView);
        net.minecraft.inventory.Inventories.readData(readView.getReadView("Inventory"), this.inventory.getHeldStacks());
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);

        // Equip items based on type
        ItemStack item = ItemStack.EMPTY;
        if (golemType == GolemType.REDSTONE) {
            item = new ItemStack(Items.REDSTONE);
        } else if (golemType == GolemType.LAPIS) {
            item = new ItemStack(Items.IRON_PICKAXE);
        } else if (golemType == GolemType.EMERALD) {
            item = new ItemStack(Items.EMERALD);
        }

        if (!item.isEmpty()) {
            this.equipStack(HELD_ITEM_SLOT, item);
            this.equipStack(CopperGolemEntity.POPPY_SLOT, item);
        }

        return data;
    }

    public GolemType getGolemType() {
        return golemType;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));

        if (golemType == GolemType.LAPIS) {
            // Lapis golems are attracted to pickaxes
            this.goalSelector.add(1, new TemptGoal(this, 1.2D, Ingredient.ofItems(
                    Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE
            ), false));
        } else if (golemType == GolemType.EMERALD) {
            // Emerald golems follow villagers and are attracted to emeralds
            this.goalSelector.add(1, new TemptGoal(this, 1.2D, Ingredient.ofItems(Items.EMERALD), false));
            this.goalSelector.add(2, new LookAtEntityGoal(this, VillagerEntity.class, 8.0F));
        }
    }

    public ItemStack getHeldItem() {
        return this.getEquippedStack(HELD_ITEM_SLOT);
    }

    public void setHeldItem(ItemStack stack) {
        this.equipStack(HELD_ITEM_SLOT, stack);
    }
}
