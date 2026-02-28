package com.ruipokoim.neonaddon;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
public class InvManager{
    public static ItemStack GetArmor(ClientPlayerEntity player, int slot) {
        return player.getInventory().getStack(slot);
    }
    public static int GetSlot(ClientPlayerEntity player) {
        return player.getInventory().getSelectedSlot();
    }
    public static void SetSlot(ClientPlayerEntity player, int slot) {
        player.getInventory().setSelectedSlot(slot);;
    }
    public static DefaultedList<ItemStack> getMainInventory(ClientPlayerEntity player) {
        DefaultedList<ItemStack> list = DefaultedList.of();
        for (int i = 0; i < 36; i++) {
            list.add(player.getInventory().getStack(i));
        }
        return list;
    }
}