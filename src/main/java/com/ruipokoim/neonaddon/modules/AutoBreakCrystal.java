package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class AutoBreakCrystal extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> OnlyWhenHoldingCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-holding-crystal")
            .description("Only activate when holding end crystal")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> SafeCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("safe-crystal")
            .description("If enabled, break crystals only 1 block above your feet.")
            .defaultValue(true)
            .build()
    );
    public AutoBreakCrystal() {
        super(NeonMain.CATEGORY, "auto-break-crystal", "Automatically breaks end crystals where you are pointing at.");
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null) return;
        if(OnlyWhenHoldingCrystal.get() && !IsHoldingCrystal()) return;
        HitResult HitResult = mc.crosshairTarget;
        if(HitResult instanceof EntityHitResult entityHit){
            if(entityHit.getEntity() instanceof EndCrystalEntity crystal){
                if(SafeCrystal.get()){
                    int PlayerY = (int) Math.floor(mc.player.getY());
                    if(crystal.getBlockPos().getY() >= (int)Math.floor(mc.player.getY()) + 1){
                        AttackCrystal(crystal);
                    }
                }
                else{
                    AttackCrystal(crystal);
                }
            }
        }
    }
    private void AttackCrystal(EndCrystalEntity crystal) {
        if (crystal == null || crystal.isRemoved()) return;
        try {
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
        } catch (Exception e) {
            //error("Failed to attack crystal: " + e.getMessage());
        }
    }
    private boolean IsHoldingCrystal(){
        if(mc.player == null) return false;
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }
}
