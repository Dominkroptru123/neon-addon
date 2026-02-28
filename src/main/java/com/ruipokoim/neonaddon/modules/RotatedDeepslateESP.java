package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import java.util.HashSet;
import java.util.Set;

public class RotatedDeepslateESP extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<ShapeMode> DeepslateShapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How deepslates render")
        .defaultValue(ShapeMode.Both)
        .build());
    private final Setting<Boolean> Tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draws tracers to deepslate blocks")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> TracersColor = sgGeneral.add(new ColorSetting.Builder()
            .name("tracers-color")
            .description("The color of tracers")
            .defaultValue(new SettingColor(0, 255, 0, 100))
            .build());
    private final Setting<SettingColor> DeepslateColor = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("The color of deepslates")
            .defaultValue(new SettingColor(0, 255, 0, 100))
            .build());
    private final SettingGroup Range = settings.createGroup("Range");
    private final Setting<Integer> min = Range.add(new IntSetting.Builder()
            .name("min-y")
            .description("Minimum Y level to scan")
            .defaultValue(-64)
            .min(-64)
            .max(128)
            .sliderRange(-64, 128)
            .build());
    private final Setting<Integer> max = Range.add(new IntSetting.Builder()
            .name("max-y")
            .description("Maximum Y level to scan")
            .defaultValue(128)
            .min(128)
            .max(320)
            .sliderRange(-64, 320)
            .build());
    public RotatedDeepslateESP() {
        super(NeonMain.CATEGORY, "RotatedDeepslateESP", "Flags deepslates that has been rotated.");
    }
    private Set<BlockPos> FlaggedBlocks = new HashSet<>();
    @Override
    public void onActivate(){
        if(mc.world == null) return;
        FlaggedBlocks.clear();
        for(Chunk chunk : Utils.chunks()){
            if(chunk instanceof WorldChunk worldChunk){
                ScanChunk(worldChunk);
            }
        }
    }
    @Override
    public void onDeactivate(){
        FlaggedBlocks.clear();
    }
    @EventHandler
    private void onChunkLoad(ChunkDataEvent event){
        ScanChunk(event.chunk());
    }
    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event){
        BlockPos pos = event.pos;
        BlockState state = event.newState;
        if(IsRotated(state, pos.getY())){
            FlaggedBlocks.add(pos);
            info("Flagged rotated deepslate at x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ());
        }
        else{
            FlaggedBlocks.remove(pos);
        }
    }
    private void ScanChunk(WorldChunk chunk){
        ChunkPos chunkPos = chunk.getPos();
        int x1 = chunkPos.getStartX();
        int z1 = chunkPos.getStartZ();
        int y1 = Math.max(chunk.getBottomY(), min.get());
        int y2 = Math.min(chunk.getBottomY() + chunk.getHeight(), max.get());
        for(int x = x1;x < x1 + 16;++x){
            for(int z = z1;z < z1 + 16;++z){
                for(int y = y1;y < y2;++y){
                    BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                    if(IsRotated(state, y)){
                        FlaggedBlocks.add(new BlockPos(x, y, z));
                        info("Flagged rotated deepslate at x=" + x + ", y=" + y + ", z=" + z);
                    }
                }
            }
        }
    }
    private boolean IsRotated(BlockState state, int y){
        if(y < min.get() || y > max.get()) return false;
        if(!state.contains(Properties.AXIS)) return false;
        if(state.get(Properties.AXIS) == Direction.Axis.Y) return false;
        if(state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.DEEPSLATE_BRICKS) || state.isOf(Blocks.CHISELED_DEEPSLATE) || state.isOf(Blocks.POLISHED_DEEPSLATE) || state.isOf(Blocks.DEEPSLATE_TILES)){
            return true;
        }
        return false;
    }
    @EventHandler
    private void onRender(Render3DEvent event){
        Color ColorSide = new Color(DeepslateColor.get());
        Color ColorOutline = new Color(DeepslateColor.get());
        for(BlockPos pos : FlaggedBlocks){
            event.renderer.box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, ColorSide, ColorOutline, DeepslateShapeMode.get(), 0);
            if(Tracers.get()){
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TracersColor.get());
            }
        }
    }
}
