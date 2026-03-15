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

public class DeepslateESP extends Module{
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
            .visible(Tracers::get)
            .build());
    private final Setting<SettingColor> DeepslateColor = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("The color of deepslates")
            .defaultValue(new SettingColor(0, 255, 0, 100))
            .build());
    private final SettingGroup Range = settings.createGroup("Range");
    public DeepslateESP() {
        super(NeonMain.CATEGORY, "DeepslateESP", "Flags deepslates that has Y level higher than 8");
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
        if(event.newState.isOf(Blocks.DEEPSLATE) && pos.getY() > 8){
            FlaggedBlocks.add(pos);
            info("Flagged deepslate at x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ());
        }
        else{
            FlaggedBlocks.remove(pos);
        }
    }
    private void ScanChunk(WorldChunk chunk){
        ChunkPos chunkPos = chunk.getPos();
        int x1 = chunkPos.getStartX();
        int z1 = chunkPos.getStartZ();
        for(int x = x1;x < x1 + 16;++x){
            for(int z = z1;z < z1 + 16;++z){
                for(int y = 9;y < chunk.getBottomY() + chunk.getHeight();++y){
                    if(chunk.getBlockState(new BlockPos(x, y, z)).isOf(Blocks.DEEPSLATE)){
                        FlaggedBlocks.add(new BlockPos(x, y, z));
                        info("Flagged deepslate at x=" + x + ", y=" + y + ", z=" + z);
                    }
                }
            }
        }
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
