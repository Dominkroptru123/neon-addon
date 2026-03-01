package com.ruipokoim.neonaddon;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.render.RenderUtils.center;

public class InteractionManager{
    public static void Interact(BlockHitResult HitResult){
        BlockUtils.interact(HitResult, Hand.MAIN_HAND, false);
    }
    public static void Place(){
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)mc.crosshairTarget).getBlockPos().offset(((BlockHitResult)mc.crosshairTarget).getSide());
            BlockUtils.place(pos, Hand.MAIN_HAND, mc.player.getInventory().getSelectedSlot(), false, 2147483647, false, true, false);
        }
    }
    public static void Attack(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}