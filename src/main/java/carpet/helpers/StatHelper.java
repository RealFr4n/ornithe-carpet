package carpet.helpers;

import com.mojang.authlib.GameProfile;
import carpet.mixins.rule.scoreboardStats.StatCriterionAccessor;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.GameProfileCache;
import net.minecraft.server.stat.ServerPlayerStats;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<UUID, ServerPlayerStats> cache;
    private static long cacheTime;

    public static File[] getStatFiles(MinecraftServer server) {
        File statsDir = new File(server.getWorld(0).getStorage().getDir(), "stats");
        return statsDir.listFiles((dir, name) -> name.endsWith(".json"));
    }

    public static Map<UUID, ServerPlayerStats> getAllStatistics(MinecraftServer server) {
        if (cache != null && server.getTicks() - cacheTime < 100) return cache;
        File[] files = getStatFiles(server);
        HashMap<UUID, ServerPlayerStats> stats = new HashMap<>();
        PlayerManager players = server.getPlayerManager();
        for (File file : files) {
            String filename = file.getName();
            String uuidString = filename.substring(0, filename.lastIndexOf(".json"));
            try {
                UUID uuid = UUID.fromString(uuidString);
                PlayerEntity player = players.get(uuid);
                if (player != null) {
                    stats.put(uuid, players.getStats(player));
                } else {
                    ServerPlayerStats manager = new ServerPlayerStats(server, file);
                    manager.load();
                    stats.put(uuid, manager);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        cache = stats;
        cacheTime = server.getTicks();
        return stats;
    }

    @Nullable
    public static String getUsername(MinecraftServer server, UUID uuid) {
        GameProfileCache profileCache = server.getGameProfileCache();
        GameProfile profile = profileCache.get(uuid);
        if (profile != null) return profile.getName();
        return null;
    }

    public static void initialize(Scoreboard scoreboard, MinecraftServer server, ScoreboardObjective objective) {
        LOGGER.info("Initializing " + objective);
        ScoreboardCriterion criteria = objective.getCriterion();
        if (!(criteria instanceof net.minecraft.scoreboard.criterion.StatCriterion)) return;
        Stat stat = ((StatCriterionAccessor) criteria).getStat();
        for (Map.Entry<UUID, ServerPlayerStats> statEntry : getAllStatistics(server).entrySet()) {
            ServerPlayerStats stats = statEntry.getValue();
            int value = stats.get(stat);
            if (value == 0) continue;
            String username = getUsername(server, statEntry.getKey());
            if (username == null) continue;
            ScoreboardScore score = scoreboard.getScore(username, objective);
            score.set(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }

    public static void initializeWithDividerWithDifferentCriteria(Scoreboard scoreboard, MinecraftServer server, ScoreboardObjective objective, int divider, ScoreboardCriterion criteria) {
        LOGGER.info("Initializing " + objective);
        if (!(criteria instanceof net.minecraft.scoreboard.criterion.StatCriterion)) {
            return;
        }
        Stat stat = ((StatCriterionAccessor) criteria).getStat();
        for (Map.Entry<UUID, ServerPlayerStats> statEntry : getAllStatistics(server).entrySet()) {
            ServerPlayerStats stats = statEntry.getValue();
            int value = stats.get(stat) / divider;
            if (value == 0) {
                continue;
            }
            String username = getUsername(server, statEntry.getKey());
            if (username == null) {
                continue;
            }
            ScoreboardScore score = scoreboard.getScore(username, objective);
            score.set(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }


}
