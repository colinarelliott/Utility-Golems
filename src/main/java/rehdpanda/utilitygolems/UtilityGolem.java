package rehdpanda.utilitygolems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

/// BASE FOR THE GOLEM EXTENDS THE COPPER GOLEMS,
/// DEFINES BASIC BEHAVOUR, TYPE AND HELD ITEM

public class UtilityGolem extends CopperGolemEntity {

    private final GolemType golemType;
    private static final EquipmentSlot HELD_ITEM_SLOT = EquipmentSlot.MAINHAND;

    public UtilityGolem(EntityType<? extends UtilityGolem> type, World world, GolemType golemType) {
        super(type, world);
        this.golemType = golemType;

        // Equip items based on type
        if (golemType == GolemType.REDSTONE) {
            ItemStack item = new ItemStack(Items.REDSTONE);
            this.equipStack(HELD_ITEM_SLOT, item);
        } else if (golemType == GolemType.LAPIS) {
            ItemStack item = new ItemStack(Items.IRON_PICKAXE);
            this.equipStack(HELD_ITEM_SLOT, item);
        }
        // MIGHT DELETE LATER

    }

    public GolemType getGolemType() {
        return golemType;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new WanderAroundGoal(this, 1.0D));
    }

    public ItemStack getHeldItem() {
        return this.getEquippedStack(HELD_ITEM_SLOT);
    }

    public void setHeldItem(ItemStack stack) {
        this.equipStack(HELD_ITEM_SLOT, stack);
    }
}
