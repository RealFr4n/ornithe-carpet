package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.InventoryCustom;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandInventory extends CarpetAbstractCommand {
    @Override
    public String getName() {
        return "view";
    }

    @Override
    public String getUsage(CommandSource source) {
        return "Usage: /view inv|echest <playername>";
    }

    @Override
    public boolean canUse(MinecraftServer server, CommandSource source) {
        return canUseCommand(source, CarpetSettings.commandInventory);
    }

    @Override
    public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new IncorrectUsageException(getUsage(source));
        }

        String viewType = args[0].toLowerCase();
        String targetName = args[1];

        if (!viewType.equals("inv") && !viewType.equals("echest")) {
            throw new IncorrectUsageException("Invalid view type. Use 'inv' or 'echest'");
        }

        // Get the viewing player
        ServerPlayerEntity viewer = getPlayer(source);
        if (viewer == null) {
            throw new CommandException("This command can only be used by players");
        }

        // Get the target player
        ServerPlayerEntity target = server.getPlayerManager().get(targetName);
        if (target == null) {
            throw new CommandException("Player not found: " + targetName);
        }

        // Prevent viewing own inventory (optional, can be removed)
        if (target.getUuid().equals(viewer.getUuid())) {
            throw new CommandException("You cannot view your own inventory");
        }

        // Create and display the inventory
        InventoryCustom inventory;
        if (viewType.equals("inv")) {
            inventory = new InventoryCustom(
                target.getName() + "'s Inventory",
                true,
                45,
                target,
                viewer
            );
        } else {
            inventory = new InventoryCustom(
                target.getName() + "'s Ender Chest",
                false,
                27,
                target,
                viewer
            );
        }

        viewer.openInventoryMenu(inventory);
        sendSuccess(source, this, "Opened " + targetName + "'s " + (viewType.equals("inv") ? "inventory" : "ender chest"));
    }

    @Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource source, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return suggestMatching(args, "inv", "echest");
        }
        if (args.length == 2) {
            List<String> players = new ArrayList<>();
            for (ServerPlayerEntity player : server.getPlayerManager().getAll()) {
                players.add(player.getName());
            }
            return suggestMatching(args, players.toArray(new String[0]));
        }
        return Collections.emptyList();
    }

    private ServerPlayerEntity getPlayer(CommandSource source) {
        if (source instanceof ServerPlayerEntity) {
            return (ServerPlayerEntity) source;
        }
        return null;
    }
}
