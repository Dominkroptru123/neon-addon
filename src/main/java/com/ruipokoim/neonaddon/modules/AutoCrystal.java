package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.InteractionManager;
import com.ruipokoim.neonaddon.InvManager;
import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class AutoCrystal extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> BreakCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("break-crystal")
            .description("Auto break crystals")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> SafeCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("safe-crystal")
            .description("Break crystals only 1 block above your feet")
            .visible(BreakCrystal::get)
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> OnlyWhenHoldingTotem = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-holding-totem")
            .description("Only break when holding totem")
            .visible(BreakCrystal::get)
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> OnlyWhenHoldingCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-holding-crystal")
            .description("Only break when holding crystal")
            .visible(BreakCrystal::get)
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> PlaceCrystal = sgGeneral.add(new BoolSetting.Builder()
            .name("place-crystal")
            .description("Auto place crystals")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> OnlyEntities = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-entities-are-near")
            .description("Only do crystals when selected entities are near")
            .visible(PlaceCrystal::get)
            .defaultValue(true)
            .build()
    );
    private final Setting<Set<EntityType<?>>> Entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Select specific entities.")
            .visible(OnlyEntities::get)
            .defaultValue(EntityType.PLAYER)
            .build()
    );
    private final Setting<Double> Range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("Maximum range from crystals to entities")
            .visible(OnlyEntities::get)
            .defaultValue(10)
            .min(6)
            .sliderMax(15)
            .build()
    );
    public AutoCrystal() {
        super(NeonMain.CATEGORY, "auto-crystal", "Automatically breaks and places end crystals");
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null || mc.currentScreen instanceof Screen) return;
        HitResult hitResult = mc.crosshairTarget;
        if(hitResult instanceof EntityHitResult entityHit){
            if(OnlyWhenHoldingCrystal.get() && !InvManager.IsHolding(Items.END_CRYSTAL)) return;
            if(OnlyWhenHoldingTotem.get() && !(InvManager.IsOffHolding(Items.TOTEM_OF_UNDYING) || InvManager.IsHolding(Items.TOTEM_OF_UNDYING))) return;
            if(entityHit.getEntity() instanceof EndCrystalEntity crystal){
                if(SafeCrystal.get() && !(crystal.getBlockPos().getY() >= (mc.player.getEntityPos().getY()) + 1.0)) return;
                InteractionManager.Attack(crystal);
            }
        }
        else if(PlaceCrystal.get() && hitResult instanceof BlockHitResult blockHit){
            Block block = mc.world.getBlockState(blockHit.getBlockPos()).getBlock();
            if(block == Blocks.OBSIDIAN || block == Blocks.BEDROCK){
                if(HasSpace(blockHit.getBlockPos()) && (InvManager.IsHolding(Items.END_CRYSTAL) || InvManager.IsOffHolding(Items.END_CRYSTAL))){
                    if(OnlyEntities.get() && !CanCrystal(blockHit.getBlockPos())) return;
                    InteractionManager.Interact(blockHit);
                }
            }
        }
    }
    private boolean HasSpace(BlockPos pos) {
        if(!mc.world.isAir(pos.up())) return false;
        int x = pos.up().getX(), y = pos.up().getY(), z = pos.up().getZ();
        return mc.world.getOtherEntities(null, new Box(x, y, z, x + 1.0, y + 2.0, z + 1.0)).isEmpty();
    }
    public boolean IsBlocked(BlockPos centerPos, Entity entity) {
        Vec3d start = new Vec3d(entity.getEntityPos().getX(),entity.getEntityPos().getY() + 0.375,entity.getEntityPos().getZ());
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
            if (mc.world.getBlockState(checkPos).getBlock().getBlastResistance() > 6.0) {
                if (start.squaredDistanceTo(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5) < Dist) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean CanCrystal(BlockPos pos){
        for(Entity entity : mc.world.getEntities()){
            double dx = entity.getEntityPos().x - pos.up().getX();
            double dz = entity.getEntityPos().z - pos.up().getZ();
            if(pos.getY() >= mc.player.getEntityPos().getY() && Math.sqrt(dx*dx+dz*dz) <= Range.get() && entity != mc.player.getEntity() && entity.isAlive() && !IsBlocked(pos.up(),entity) && Entities.get().contains(entity.getType())){
                return true;
            }
        }
        return false;
    }
}