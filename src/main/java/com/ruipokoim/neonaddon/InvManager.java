package com.ruipokoim.neonaddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InvManager{
    public static DefaultedList<ItemStack> GetInventory(ClientPlayerEntity player) {
        DefaultedList<ItemStack> list = DefaultedList.of();
        for (int i = 0; i < 36; i++) {
            list.add(player.getInventory().getStack(i));
        }
        return list;
    }
    public static void HotbarSwitch(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
        }
    }
    public static boolean HotbarHas(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                return true;
            }
        }
        return false;
    }
    public static boolean IsHolding(Item item){
        return mc.player.getMainHandStack().getItem() == item;
    }
    public static boolean IsOffHolding(Item item){
        return mc.player.getOffHandStack().getItem() == item;
    }
}