package carpet.mixins.rule.optimizedDespawnRange;

import carpet.CarpetSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Unique
    private static boolean carpet$wouldImmediatelyDespawn(Entity entity) {
        if (!CarpetSettings.optimizedDespawnRange) return false;
        if (!(entity instanceof MobEntity)) return false;
        MobEntity mob = (MobEntity) entity;
        if (mob.isPersistent()) return false;

        World world = mob.world;
        boolean hasPlayer = false;
        for (PlayerEntity player : world.players) {
            if (!player.isSpectator()) {
                hasPlayer = true;
                double distSq = player.getSquaredDistanceTo(mob.x, mob.y, mob.z);
                if (distSq <= 16384.0D) {
                    return false;
                }
            }
        }
        return hasPlayer;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;addEntity(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean optimizedDespawnTick(ServerWorld world, Entity entity, Operation<Boolean> original) {
        if (carpet$wouldImmediatelyDespawn(entity)) {
            entity.remove();
            return false;
        }
        return original.call(world, entity);
    }

    @WrapOperation(
            method = "populateChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean optimizedDespawnPopulate(World world, Entity entity, Operation<Boolean> original) {
        if (carpet$wouldImmediatelyDespawn(entity)) {
            entity.remove();
            return false;
        }
        return original.call(world, entity);
    }
}
