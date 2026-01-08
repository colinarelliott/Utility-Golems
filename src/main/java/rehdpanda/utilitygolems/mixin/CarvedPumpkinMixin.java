package rehdpanda.utilitygolems.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rehdpanda.utilitygolems.GolemPatterns;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinMixin {
    @Inject(method = "onBlockAdded", at = @At("TAIL"))
    private void onAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.isOf(state.getBlock())) {
            GolemPatterns.onPumpkinPlaced(world, pos);
        }
    }
}
