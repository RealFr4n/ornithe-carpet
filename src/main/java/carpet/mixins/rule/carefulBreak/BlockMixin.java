package carpet.mixins.rule.carefulBreak;

import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
        method = "dropItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z"
        )
    )
    private static void doCarefulBreakItemCollide(World world, BlockPos pos, ItemStack stack, CallbackInfo ci, @Local ItemEntity item) {
        if (!CarpetSettings.carefulBreak.equals("off")) {
            ServerPlayerEntity player = CarefulBreakHelper.miningPlayer.get();
            if (player != null) {
                boolean shouldPickup = CarpetSettings.carefulBreak.equals("always") || 
                    (CarpetSettings.carefulBreak.equals("sneak") && player.isSneaking());
                if (shouldPickup) {
                    item.setNoPickUpDelay();
                    item.onPlayerCollision(player);
                }
            }
        }
    }
}
