package rehdpanda.utilitygolems;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Simple spawn-egg-like item that spawns a UtilityGolem of the associated GolemType when used on a block.
 * This mirrors vanilla spawn egg behavior sufficiently for our custom entities without relying on
 * component wiring that may vary across mappings.
 */
public class UtilityGolemSpawnEggItem extends Item {
    private final GolemType golemType;

    public UtilityGolemSpawnEggItem(Settings settings, GolemType golemType) {
        super(settings);
        this.golemType = golemType;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockPos pos = context.getBlockPos();
        Direction face = context.getSide();
        BlockPos spawnPos = pos.offset(face);

        EntityType<UtilityGolem> entityType = UGInit.GOLEM_TYPES.get(golemType);
        if (entityType == null) return ActionResult.PASS;

        UtilityGolem golem = new UtilityGolem(entityType, world, golemType);
        golem.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
        boolean spawned = world.spawnEntity(golem);
        if (spawned) {
            PlayerEntity player = context.getPlayer();
            ItemStack stack = context.getStack();
            if (player == null || !player.isCreative()) {
                stack.decrement(1);
            }
            world.playSound(null, spawnPos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.PLAYERS, 0.5f, 1.0f);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
