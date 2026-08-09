package carpet.helpers;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class InventoryCustom implements Inventory {
    private final String name;
    private final boolean isPlayerInventory;
    private final int size;
    public final PlayerEntity entityPlayerOwner;
    public final PlayerEntity viewerPlayer;

    public InventoryCustom(String name, boolean isPlayerInventory, int size, PlayerEntity owner, PlayerEntity viewer) {
        this.name = name;
        this.isPlayerInventory = isPlayerInventory;
        this.size = size;
        this.entityPlayerOwner = owner;
        this.viewerPlayer = viewer;
    }

    /**
     * Maps GUI slot index to actual player inventory slot.
     *
     * GUI layout (5 rows of 9 = 45 slots):
     *   Row 1 (0-8):   armor[0-3] + empty[4-8]
     *   Row 2 (9-17):  main[9-17]
     *   Row 3 (18-26): main[18-26]
     *   Row 4 (27-35): main[0-8]  (hotbar internal)
     *   Row 5 (36-44): main[27-35]
     *
     * PlayerInventory internal layout:
     *   0-8:   hotbar (main inventory)
     *   9-35:  main inventory
     *   36-39: armor (helmet, chest, legs, boots)
     *   40:    offhand
     */
    private int mapSlot(int slot) {
        if (!isPlayerInventory) return slot;
        if (slot < 4) return slot + 36;          // GUI 0-3 -> armor 36-39
        if (slot < 9) return -1;                  // GUI 4-8 -> empty
        if (slot < 18) return slot;               // GUI 9-17 -> main 9-17
        if (slot < 27) return slot;               // GUI 18-26 -> main 18-26
        if (slot < 36) return slot - 27;          // GUI 27-35 -> main/hotbar 0-8
        if (slot < 45) return slot - 9;           // GUI 36-44 -> main 27-35
        return -1;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            int real = mapSlot(i);
            if (real >= 0 && !entityPlayerOwner.inventory.getStack(real).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        int real = mapSlot(slot);
        if (real >= 0) {
            return entityPlayerOwner.inventory.getStack(real);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        int real = mapSlot(slot);
        if (real >= 0) {
            return entityPlayerOwner.inventory.removeStack(real, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackQuietly(int slot) {
        int real = mapSlot(slot);
        if (real >= 0) {
            return entityPlayerOwner.inventory.removeStackQuietly(real);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        int real = mapSlot(slot);
        if (real >= 0) {
            entityPlayerOwner.inventory.setStack(real, stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void markDirty() {
        entityPlayerOwner.inventory.markDirty();
    }

    @Override
    public boolean isValid(PlayerEntity player) {
        return true;
    }

    @Override
    public void onOpen(PlayerEntity player) {
    }

    @Override
    public void onClose(PlayerEntity player) {
    }

    @Override
    public boolean canSetStack(int slot, ItemStack stack) {
        int real = mapSlot(slot);
        return real >= 0;
    }

    @Override
    public int getData(int id) {
        return 0;
    }

    @Override
    public void setData(int id, int value) {
    }

    @Override
    public int getDataSize() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            int real = mapSlot(i);
            if (real >= 0) {
                entityPlayerOwner.inventory.removeStackQuietly(real);
            }
        }
        entityPlayerOwner.inventory.markDirty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText(name);
    }
}
