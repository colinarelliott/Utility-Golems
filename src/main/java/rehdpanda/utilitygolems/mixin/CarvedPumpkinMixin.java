package rehdpanda.utilitygolems.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rehdpanda.utilitygolems.GolemPatterns;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinMixin {
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void onAdded(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.is(state.getBlock())) {
            GolemPatterns.onPumpkinPlaced(world, pos);
        }
    }
}
