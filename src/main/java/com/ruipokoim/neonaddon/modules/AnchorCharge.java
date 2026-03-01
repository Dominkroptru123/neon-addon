package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.InteractionManager;
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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import com.ruipokoim.neonaddon.InvManager;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AnchorCharge extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public boolean isSneak = false;
    private final Setting<Boolean> BreakAfterCharge = sgGeneral.add(new BoolSetting.Builder()
            .name("break-after-charge")
            .description("Self-explain")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> safeAnchor = sgGeneral.add(new BoolSetting.Builder()
            .name("safe-anchor")
            .description("Place a glowstone after charge")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> OnlyWhenHoldingTotem = sgGeneral.add(new BoolSetting.Builder()
            .name("only-explode-when-holding-a-totem")
            .description("Self-explain")
            .defaultValue(true)
            .build()
    );
    public AnchorCharge(){
        super(NeonMain.CATEGORY, "auto-charge-anchor", "Self-explain.");
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null) return;
        if(mc.crosshairTarget instanceof BlockHitResult hitResult){
            BlockState state = mc.world.getBlockState(hitResult.getBlockPos());
            if(state.getBlock() == Blocks.RESPAWN_ANCHOR){
                int charges = state.get(RespawnAnchorBlock.CHARGES);
                if(charges == 0){
                    InvManager.HotbarSwitch(Items.GLOWSTONE);
                    InteractionManager.Interact(hitResult);
                }
                else if(charges > 0){
                    if(OnlyWhenHoldingTotem.get() && InvManager.IsOffHolding(Items.TOTEM_OF_UNDYING)){
                        InvManager.HotbarSwitch(Items.RESPAWN_ANCHOR);
                        if(BreakAfterCharge.get()){
                            InteractionManager.Interact(hitResult);
                        }
                    }
                    else if(safeAnchor.get()){
                        if(IsGlowstoneCloser(hitResult.getBlockPos())){
                            InvManager.HotbarSwitch(Items.RESPAWN_ANCHOR);
                            InteractionManager.Interact(hitResult);
                        }
                        else{
                            InvManager.HotbarSwitch(Items.GLOWSTONE);
                            Sneak(true);
                            InteractionManager.Place();
                            Sneak(false);
                        }
                    }
                }
            }
        }
    }
    public void Sneak(boolean toggle){
        mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, toggle, false)));
    }
    public boolean IsGlowstoneCloser(BlockPos centerPos) {
        BlockPos[] adjacentPositions = new BlockPos[] {centerPos.north(), centerPos.south(), centerPos.east(), centerPos.west()};
        for (BlockPos pos : adjacentPositions) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.GLOWSTONE) {
                if (mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < mc.player.squaredDistanceTo(centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5)) {
                    return true;
                }
            }
        }
        return false;
    }
}