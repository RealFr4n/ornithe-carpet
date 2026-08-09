package carpet.mixins.rule.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import carpet.utils.Messenger;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    private static boolean carpet$isUpdateSuppression(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ThrowableSuppression) return true;
            if (current instanceof StackOverflowError) return true;
            if (current instanceof CrashException) {
                CrashReport report = ((CrashException) current).getReport();
                current = report.getException();
            } else {
                current = current.getCause();
            }
        }
        return false;
    }

    @WrapOperation(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick()V"
            )
    )
    private void catchUpdateSuppressionInWorldTick(ServerWorld instance, Operation<Void> original) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            original.call(instance);
            return;
        }
        try {
            original.call(instance);
        } catch (Throwable e) {
            if (carpet$isUpdateSuppression(e)) {
                Messenger.print_server_message((MinecraftServer) (Object) this, "You just caused a server crash in world tick.");
            } else {
                throw e;
            }
        }
    }

    @WrapOperation(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickEntities()V"
            )
    )
    private void catchUpdateSuppressionInEntityTick(ServerWorld instance, Operation<Void> original) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            original.call(instance);
            return;
        }
        try {
            original.call(instance);
        } catch (Throwable e) {
            if (carpet$isUpdateSuppression(e)) {
                Messenger.print_server_message((MinecraftServer) (Object) this, "You just caused a server crash in update entities.");
            } else {
                throw e;
            }
        }
    }
}
