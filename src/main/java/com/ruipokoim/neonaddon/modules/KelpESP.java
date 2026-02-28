package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.KelpPlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import java.util.HashSet;
import java.util.Set;

public class KelpESP extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
            .name("notifications")
            .description("Chat feedback")
            .defaultValue(true)
            .build());
    private final Setting<ShapeMode> KelpShapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How chunks render")
        .defaultValue(ShapeMode.Both)
        .build());
    private final Setting<SettingColor> KelpColor = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("The color of chunks")
            .defaultValue(new SettingColor(0, 255, 0, 100))
            .build());
    public KelpESP() {
        super(NeonMain.CATEGORY, "KelpESP", "Flags chunks that has high kelps level");
    }
    private final Set<ChunkPos> FlaggedChunks = new HashSet<>();
    @Override
    public void onActivate(){
        if(mc.world == null) return;
        FlaggedChunks.clear();
        for(Chunk chunk : Utils.chunks()){
            if(chunk instanceof WorldChunk worldChunk){
                ScanChunk(worldChunk);
            }
        }
    }
    @EventHandler
    private void onChunkLoad(ChunkDataEvent event){
        ScanChunk(event.chunk());
    }
    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event){
        if (!(event.newState.getBlock() instanceof KelpBlock || event.newState.getBlock() instanceof KelpPlantBlock || event.oldState.getBlock() instanceof KelpBlock || event.oldState.getBlock() instanceof KelpPlantBlock)) return;
        Chunk chunk = mc.world.getChunk(event.pos);
        if(chunk instanceof WorldChunk worldChunk){
            ScanChunk(worldChunk);
        }
    }
    private void ScanChunk(WorldChunk chunk){
        ChunkPos ChunkPos = chunk.getPos();
        FlaggedChunks.remove(ChunkPos);
        int x1 = ChunkPos.getStartX();
        int z1 = ChunkPos.getStartZ();
        int y1 = chunk.getBottomY();
        int y2 = y1 + chunk.getHeight();
        int KelpCol = 0;
        int KelpsTopped = 0;
        for(int x = x1;x < x1 + 16;++x){
            for(int z = z1;z < z1 + 16;++z){
                int top = 0;
                int bottom = -320;
                for(int y = y1;y < y2;++y){
                    Block block = chunk.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if(block instanceof KelpBlock || block instanceof KelpPlantBlock){
                        if(bottom < 0){
                            bottom = y;
                        }
                        top = y;
                    }
                }
                if(bottom >= 0 && top - bottom + 1 >= 8){
                    ++KelpCol;
                    if(top == 62){
                        ++KelpsTopped;
                    }
                }
            }
        }
        if(KelpCol >= 10 && ((double) KelpsTopped / KelpCol) >= 0.6){
            FlaggedChunks.add(ChunkPos);
            if(notifications.get()){
                info("§bChunk " + ChunkPos + " §bflagged with " +  KelpsTopped + " / " + KelpCol + " §bcolumns");
            }
        }
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        Color ColorSide = new Color(KelpColor.get());
        Color ColorOutline = new Color(KelpColor.get());
        for (ChunkPos pos : FlaggedChunks) {
            event.renderer.box(pos.getStartX(), 63, pos.getStartZ(), pos.getStartX() + 16, 63, pos.getStartZ() + 16, ColorSide, ColorOutline, KelpShapeMode.get(), 0);
        }
    }
}
