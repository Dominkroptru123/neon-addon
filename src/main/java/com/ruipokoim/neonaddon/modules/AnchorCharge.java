package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.InteractionManager;
import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import com.ruipokoim.neonaddon.InvManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AnchorCharge extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
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
    private final Setting<Boolean> StopOnEating = sgGeneral.add(new BoolSetting.Builder()
            .name("stop-while-eating")
            .description("Stop while eating")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> StopOnShielding = sgGeneral.add(new BoolSetting.Builder()
            .name("stop-while-blocking")
            .description("Stop while blocking with a shield")
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
        super(NeonMain.CATEGORY, "auto-charge-anchor", "Self-explain");
    }
    public boolean IsSneaking = false;
    private int tick = 0;
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null || mc.world.getRegistryKey() == World.NETHER || mc.currentScreen instanceof Screen || (StopOnEating.get() && mc.player.isUsingItem() && mc.player.getActiveItem().getUseAction() == UseAction.EAT) || (StopOnShielding.get() && mc.player.isBlocking())) return;
        if(mc.crosshairTarget instanceof BlockHitResult hitResult){
            BlockState state = mc.world.getBlockState(hitResult.getBlockPos());
            if(state.getBlock() == Blocks.RESPAWN_ANCHOR){
                int charges = state.get(RespawnAnchorBlock.CHARGES);
                if(charges == 0 && InvManager.HotbarHas(Items.GLOWSTONE)){
                    InvManager.HotbarSwitch(Items.GLOWSTONE);
                    InteractionManager.Interact(hitResult);
                }
                else if(charges > 0){
                    if(OnlyWhenHoldingTotem.get() && InvManager.IsOffHolding(Items.TOTEM_OF_UNDYING) && InvManager.HotbarHas(Items.RESPAWN_ANCHOR)){
                        InvManager.HotbarSwitch(Items.RESPAWN_ANCHOR);
                        if(BreakAfterCharge.get()){
                            InteractionManager.Interact(hitResult);
                        }
                    }
                    else if(safeAnchor.get() && InvManager.HotbarHas(Items.GLOWSTONE)){
                        if(IsBlocked(hitResult.getBlockPos()) && InvManager.HotbarHas(Items.RESPAWN_ANCHOR)){
                            IsSneaking = false;
                            InvManager.HotbarSwitch(Items.RESPAWN_ANCHOR);
                            InteractionManager.Interact(hitResult);
                        }
                        else{
                            if(!mc.player.isSneaking()){
                                IsSneaking = true;
                            }
                            else if(mc.player.isSneaking()){
                                if(InvManager.IsHolding(Items.GLOWSTONE)){
                                    InteractionManager.Interact(hitResult);
                                    IsSneaking = false;
                                }
                                else{
                                    InvManager.HotbarSwitch(Items.GLOWSTONE);
                                }
                            }
                        }
                    }
                }
            }
            else if(IsSneaking){
                ++tick;
                if(tick > 5){
                    IsSneaking = false;
                    tick = 0;
                }
            }
        }
    }
    private void Sneak(boolean toggle){
        mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(mc.player.input.playerInput.forward(), mc.player.input.playerInput.backward(), mc.player.input.playerInput.left(), mc.player.input.playerInput.right(), mc.player.input.playerInput.jump(), toggle, mc.player.input.playerInput.sprint())));
    }
    public boolean IsGlowstoneCloser(BlockPos centerPos) {
        BlockPos[] adjacentPositions = new BlockPos[] {centerPos.north(), centerPos.south(), centerPos.east(), centerPos.west()};
        for (BlockPos pos : adjacentPositions) {
            if (mc.world.getBlockState(pos).isFullCube(mc.world, new BlockPos(pos))) {
                if (mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < mc.player.squaredDistanceTo(centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean IsBlocked(BlockPos centerPos) {
        Vec3d start = mc.player.getEntityPos();
        Vec3d end = new Vec3d(centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5);
        double Dist = start.squaredDistanceTo(end);
        int steps = (int) Math.ceil(Math.sqrt(Dist) * 8);
        for(int i = 0;i < steps;++i){
            double pct = (double) i / steps;
            BlockPos checkPos = new BlockPos(
                    (int) Math.floor(start.x + ((end.x - start.x) * pct)),
                    (int) Math.floor(start.y + ((end.y - start.y) * pct)),
                    (int) Math.floor(start.z + ((end.z - start.z) * pct))
            );
            if (checkPos.equals(mc.player.getBlockPos()) || checkPos.equals(centerPos)) {
                continue;
            }
            if (mc.world.getBlockState(checkPos).isFullCube(mc.world, checkPos)) {
                if (start.squaredDistanceTo(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5) < Dist) {
                    return true;
                }
            }
        }
        return false;
    }
}