package rehdpanda.utilitygolems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Simple spawn-egg-like item that spawns a UtilityGolem of the associated GolemType when used on a block.
 * This mirrors vanilla spawn egg behavior sufficiently for our custom entities without relying on
 * component wiring that may vary across mappings.
 */
public class UtilityGolemSpawnEggItem extends Item {
    private final GolemType golemType;

    public UtilityGolemSpawnEggItem(Properties settings, GolemType golemType) {
        super(settings);
        this.golemType = golemType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = pos.relative(face);

        EntityType<UtilityGolem> entityType = UGInit.GOLEM_TYPES.get(golemType);
        if (entityType == null) return InteractionResult.PASS;

        UtilityGolem golem = new UtilityGolem(entityType, world, golemType);
        Player player = context.getPlayer();
        if (player != null) {
            golem.setOwnerUuid(player.getUUID());
        }
        golem.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, world.getRandom().nextFloat() * 360.0F, 0.0F);
        boolean spawned = world.addFreshEntity(golem);
        if (spawned) {
            ItemStack stack = context.getItemInHand();
            if (player == null || !player.isCreative()) {
                stack.shrink(1);
            }
            world.playSound(null, spawnPos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 0.5f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
