package carpet.mixins.rule.scoreboardStats;

import net.minecraft.scoreboard.criterion.StatCriterion;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatCriterion.class)
public interface StatCriterionAccessor {
    @Accessor("stat")
    Stat getStat();
}
