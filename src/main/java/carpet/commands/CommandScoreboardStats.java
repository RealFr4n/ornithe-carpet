package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Formatting;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandScoreboardStats extends CarpetAbstractCommand {

    @Override
    public String getName() {
        return "sb";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return "/sb <stat type(b|c|d|k|m|p|u|killedBy|health|playedOneMinute or empty for general stats)>.<stat target> <where to display(list|sidebar|belowName|empty for sidebar)> or /sb clear <where to clear(empty is sidebar)>";
    }

    @Override
    public boolean canUse(MinecraftServer server, CommandSource source) {
        return canUseCommand(source, CarpetSettings.scoreboardStats);
    }

    @Override
    public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
        if (args.length > 0 && args.length < 3) {
            final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            String[] arguments = args[0].split("\\.");
            Scoreboard scoreboard = server.getWorld(0).getScoreboard();
            String criteria;
            ScoreboardCriterion scoreCriteria = null;
            if (args[0].equalsIgnoreCase("clear")) {
                if (args.length == 2) {
                    switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "list":
                            this.checkAndDeleteScoreOnDisplay(0, scoreboard);
                            server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + "Tab cleared by " + source.getName()), false);
                            break;
                        case "sidebar":
                            this.checkAndDeleteScoreOnDisplay(1, scoreboard);
                            server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + "Sidebar cleared by " + source.getName()), false);
                            break;
                        case "belowname":
                            this.checkAndDeleteScoreOnDisplay(2, scoreboard);
                            server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + "belowName cleared by " + source.getName()), false);
                            break;
                        case "all":
                            this.deleteAllScores(scoreboard);
                            server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + "Cleared all scores by " + source.getName()), false);
                            break;
                        default:
                            throw new IncorrectUsageException("You can only clear list, sidebar or belowName");
                    }
                } else {
                    this.checkAndDeleteScoreOnDisplay(1, scoreboard);
                    server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + "Sidebar cleared by " + source.getName()), false);
                }
                return;
            }
            String statType = null;
            switch (arguments[0]) {
                case "b":
                    criteria = "stat.breakItem.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " broken";
                    break;
                case "c":
                    criteria = "stat.craftItem.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " crafted";
                    break;
                case "d":
                    criteria = "stat.drop.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " dropped";
                    break;
                case "k":
                    criteria = "stat.killEntity." + arguments[1];
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " killed";
                    break;
                case "m":
                    criteria = "stat.mineBlock.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " mined";
                    break;
                case "p":
                    criteria = "stat.pickup.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " picked up";
                    break;
                case "u":
                    criteria = "stat.useItem.minecraft." + arguments[1];
                    if (arguments.length == 3) {
                        criteria += "." + arguments[2];
                    }
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = " used";
                    break;
                case "killedBy":
                    criteria = "stat.entityKilledBy." + arguments[1];
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = "Killed by ";
                    break;
                case "health":
                    criteria = "health";
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = "Health";
                    break;
                case "playedOneMinute":
                    criteria = "minutesPlayed";
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = "Minutes played";
                    break;
                default:
                    criteria = "stat." + arguments[0];
                    scoreCriteria = ScoreboardCriterion.BY_NAME.get(criteria);
                    statType = "other";
            }
            if (scoreCriteria != null) {
                int i = 1;

                String objectiveName = "st." + args[0];

                if (arguments.length > 1) {
                    Item item = Item.REGISTRY.get(new net.minecraft.resource.Identifier(arguments[1]));

                    int idForItem = Item.REGISTRY.getId(item);
                    StringBuilder sb = new StringBuilder("st.");
                    sb.append(arguments[0]);
                    sb.append(".");
                    sb.append(idForItem);
                    if (arguments.length > 2) {
                        sb.append(".");
                        sb.append(arguments[2]);
                    }
                    objectiveName = sb.toString();
                }

                ScoreboardObjective objective;
                if (objectiveName.length() > 16) {
                    objectiveName = objectiveName.substring(0, 16);
                }
                if (scoreboard.getObjective(objectiveName) == null) {
                    String displayName = "ERROR";
                    objective = scoreboard.createObjective(objectiveName, scoreCriteria);
                    if (!statType.equals("Killed by ")) {
                        if (arguments.length == 3) {
                            displayName = Formatting.GOLD + arguments[1].replace('_', ' ').substring(0, 1).toUpperCase()
                                    + arguments[1].replace('_', ' ').substring(1) + " " + arguments[2] + statType;
                        } else {
                            switch (statType) {
                                case "Health":
                                case "Minutes played":
                                    displayName = Formatting.GOLD + statType;
                                    break;
                                case "other":
                                    displayName = Formatting.GOLD + arguments[0];
                                    break;
                                default:
                                    displayName = Formatting.GOLD + arguments[1].replace('_', ' ').substring(0, 1).toUpperCase()
                                            + arguments[1].replace('_', ' ').substring(1) + statType;
                                    break;
                            }
                        }
                    } else {
                        displayName = Formatting.GOLD + statType + arguments[1];
                    }
                    displayName = abbreviateIfNecessary(displayName, "glazed", "glz");
                    displayName = abbreviateIfNecessary(displayName, "terracotta", "terracota");
                    displayName = abbreviateIfNecessary(displayName, "pressure", "prsure");
                    displayName = abbreviateIfNecessary(displayName, "weighted", "wghtd");
                    displayName = abbreviateIfNecessary(displayName, "crafted", "craft");
                    displayName = abbreviateIfNecessary(displayName, "picked", "pick");
                    displayName = abbreviateIfNecessary(displayName, "dropped", "drop");

                    if (displayName.length() > 32) {
                        displayName = displayName.substring(0, 32);
                    }

                    objective.setDisplayName(displayName);

                    if (statType.equals("Minutes played")) {
                        singleThreadExecutor.submit(() -> {
                            StatHelper.initializeWithDividerWithDifferentCriteria(scoreboard, server, objective, 1200, ScoreboardCriterion.BY_NAME.get("stat.playOneMinute"));
                        });
                    } else {
                        singleThreadExecutor.submit(() -> {
                            StatHelper.initialize(scoreboard, server, objective);
                        });
                    }
                    singleThreadExecutor.shutdown();
                } else {
                    objective = scoreboard.getObjective(objectiveName);
                }
                if (args.length == 2) {
                    i = Scoreboard.getDisplaySlot(args[1]);
                    if (i == -1) {
                        throw new IncorrectUsageException("You can only display it on list, sidebar or belowName,");
                    } else {
                        ScoreboardObjective displayedObjective = scoreboard.getDisplayObjective(i);
                        if (displayedObjective == null) {
                            scoreboard.setDisplayObjective(i, objective);
                        } else {
                            scoreboard.setDisplayObjective(i, objective);
                            if (!(isObjectiveOnDisplay(displayedObjective, scoreboard)) && displayedObjective.getName().startsWith("st.")) {
                                scoreboard.removeObjective(displayedObjective);
                            }
                        }
                    }
                } else {
                    ScoreboardObjective displayedObjective = scoreboard.getDisplayObjective(1);
                    if (displayedObjective == null) {
                        scoreboard.setDisplayObjective(i, objective);
                    } else {
                        scoreboard.setDisplayObjective(i, objective);
                        if (!isObjectiveOnDisplay(displayedObjective, scoreboard) && displayedObjective.getName().startsWith("st.")) {
                            scoreboard.removeObjective(displayedObjective);
                        }
                    }
                }
                String displaySlotName = Scoreboard.getDisplayLocation(i);
                server.getPlayerManager().sendMessage(new LiteralText(Formatting.GRAY + displaySlotName.substring(0, 1).toUpperCase() + displaySlotName.substring(1) + " changed by " + source.getName()), false);
            } else {
                source.sendMessage(new LiteralText(Formatting.RED + "That is not a valid stat"));
            }
        } else {
            throw new IncorrectUsageException(getUsage(source));
        }
    }

    public boolean isObjectiveOnDisplay(ScoreboardObjective scoreObjective, Scoreboard scoreboard) {
        boolean isObjectiveOnDisplay = false;
        for (int i = 0; i <= 2; i++) {
            if (scoreObjective.equals(scoreboard.getDisplayObjective(i))) {
                isObjectiveOnDisplay = true;
            }
        }
        return isObjectiveOnDisplay;
    }

    public void deleteAllScores(Scoreboard scoreboard) {
        Collection<ScoreboardObjective> allScoreObjectives = scoreboard.getObjectives();
        Iterator<ScoreboardObjective> scoresIterator = allScoreObjectives.iterator();
        while (scoresIterator.hasNext()) {
            ScoreboardObjective scoreObjective = scoresIterator.next();
            if (scoreObjective.getName().startsWith("st.")) {
                scoresIterator.remove();
            }
        }
        for (int i = 0; i <= 2; i++) {
            scoreboard.setDisplayObjective(i, null);
        }
    }

    public void checkAndDeleteScoreOnDisplay(int displayIdentifier, Scoreboard scoreboard) throws CommandException {
        if (scoreboard.getDisplayObjective(displayIdentifier) != null) {
            if (scoreboard.getDisplayObjective(displayIdentifier).getName().startsWith("st.")) {
                scoreboard.removeObjective(scoreboard.getDisplayObjective(displayIdentifier));
            } else {
                throw new CommandException("You can't clear a handmade scoreboard");
            }
        } else {
            throw new IncorrectUsageException("There is no scoreboard to clear");
        }
    }

    public String abbreviateIfNecessary(String string, String textToReplace, String abbreviation) {
        String result = string;
        if (string.length() > 32 && string.contains(textToReplace)) {
            result = string.replace(textToReplace, abbreviation);
        }
        return result;
    }

    @Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource source, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            Set<String> instancesKeys = ScoreboardCriterion.BY_NAME.keySet();
            options.add("clear");
            options.add("health");
            options.add("playedOneMinute");
            for (String key : instancesKeys) {
                String[] splittedKey = key.split("\\.");
                StringBuilder finalOption = new StringBuilder();
                if (splittedKey[0].equals("stat")) {
                    switch (splittedKey[1]) {
                        case "breakItem":
                            finalOption.append("b.");
                            break;
                        case "craftItem":
                            finalOption.append("c.");
                            break;
                        case "drop":
                            finalOption.append("d.");
                            break;
                        case "killEntity":
                            finalOption.append("k.");
                            break;
                        case "mineBlock":
                            finalOption.append("m.");
                            break;
                        case "pickup":
                            finalOption.append("p.");
                            break;
                        case "useItem":
                            finalOption.append("u.");
                            break;
                        case "entityKilledBy":
                            finalOption.append("killedBy.");
                            break;
                        default:
                            finalOption.append(splittedKey[1]);
                    }
                    if (splittedKey.length > 2) {
                        if (splittedKey.length > 3) {
                            finalOption.append(splittedKey[3]);
                            if (splittedKey.length == 5) {
                                finalOption.append(".").append(splittedKey[4]);
                            }
                        } else {
                            finalOption.append(splittedKey[2]);
                        }
                    }
                    options.add(finalOption.toString());
                }
            }
            return suggestMatching(args, options.toArray(new String[0]));
        }
        if (args.length == 2) {
            if (args[0].equals("clear")) {
                return suggestMatching(args, "sidebar", "list", "belowName", "all");
            }
            return suggestMatching(args, "sidebar", "list", "belowName");
        }
        return Collections.emptyList();
    }
}
