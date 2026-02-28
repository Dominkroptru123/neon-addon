package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import com.ruipokoim.neonaddon.InvManager;

public class AnchorCharge extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> BreakAfterCharge = sgGeneral.add(new BoolSetting.Builder()
            .name("break-after-charge")
            .description("Self-explain")
            .defaultValue(true)
            .build()
    );
//    private final Setting<Boolean> safeAnchor = sgGeneral.add(new BoolSetting.Builder()
//            .name("safe-anchor")
//            .description("Place a glowstone after charge")
//            .defaultValue(true)
//            .build()
//    );
    public AnchorCharge(){
        super(NeonMain.CATEGORY, "auto-charge-anchor", "Self-explain.");
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null) return;
        if(mc.crosshairTarget instanceof BlockHitResult hitResult){
            BlockState state = mc.world.getBlockState(hitResult.getBlockPos());
            Block block = state.getBlock();
            if(block == Blocks.RESPAWN_ANCHOR){
                int charges = state.get(RespawnAnchorBlock.CHARGES);
                if(charges == 0){
                    SwitchGlowstone();
                    UseAnchor(hitResult);
                }
                else if(charges > 0){
                    SwitchAnchor();
                    if(BreakAfterCharge.get()){
                        UseAnchor(hitResult);
                    }
                }
            }
        }
    }
    private boolean isHolding(Item item){
        if(mc.player == null || mc.world == null) return false;
        return mc.player.getMainHandStack().getItem() == item || mc.player.getOffHandStack().getItem() == item;
    }
    private void UseAnchor(BlockHitResult hitResult){
        if(mc.player == null || mc.world == null) return;
        try{
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void SwitchGlowstone() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.player == null) return;
        for(int i = 0;i < 9;i++){
            ItemStack stack = mc.player.getInventory().getStack(i);
            if(stack.getItem() == Items.GLOWSTONE){
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
        }
    }
    private void SwitchAnchor() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.player == null) return;
        for(int i = 0;i < 9;i++){
            ItemStack stack = mc.player.getInventory().getStack(i);
            if(stack.getItem() == Items.RESPAWN_ANCHOR){
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
        }
    }
}
