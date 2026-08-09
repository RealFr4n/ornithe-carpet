package carpet.mixins.rule.instamineEndstone;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.state.BlockState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "getMiningSpeed(Lnet/minecraft/block/state/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private void endStoneInstamine(BlockState state, World world, net.minecraft.util.math.BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (CarpetSettings.instamineEndstone && ((Block) (Object) this) == Blocks.END_STONE) {
            cir.setReturnValue(1.55f);
        }
    }
}
