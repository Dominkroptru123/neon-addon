package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class LightESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> chunkRadius = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-radius")
            .description("Radius of chunks to scan around the player")
            .defaultValue(4)
            .min(1)
            .max(16)
            .sliderMax(16)
            .build()
    );
    private final Setting<Integer> TickDelay = sgGeneral.add(new IntSetting.Builder()
            .name("tick-delay")
            .description("Delay in ticks")
            .defaultValue(10)
            .min(1)
            .max(20)
            .sliderMax(16)
            .build()
    );
    private final Setting<Integer> min = sgGeneral.add(new IntSetting.Builder()
            .name("min-y")
            .description("Minimum Y level to scan")
            .defaultValue(-64)
            .min(-64)
            .max(319)
            .sliderMin(-64)
            .sliderMax(319)
            .build()
    );
    private final Setting<Integer> max = sgGeneral.add(new IntSetting.Builder()
            .name("max-y")
            .description("Maximum Y level to scan")
            .defaultValue(0)
            .min(-64)
            .max(319)
            .sliderMin(-64)
            .sliderMax(319)
            .build()
    );
    private final Setting<Integer> MinLightLevel = sgGeneral.add(new IntSetting.Builder()
            .name("min-light-level")
            .description("Minimum light level to display.")
            .defaultValue(8)
            .min(0)
            .max(15)
            .sliderMax(15)
            .build()
    );
    private final Setting<ShapeMode> LightShapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );
    private final Map<BlockPos, Integer> FlaggedLight = new ConcurrentHashMap<>();
    private long tick = TickDelay.get();
    public LightESP() {
        super(NeonMain.CATEGORY, "light-esp", "Checks for lights");
    }
    @Override
    public void onActivate() {
        FlaggedLight.clear();
        tick = 0;
    }
    @Override
    public void onDeactivate() {
        FlaggedLight.clear();
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        ++tick;
        tick = Math.min(tick,TickDelay.get());
    }
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        if(tick >= TickDelay.get()){
            ChunkPos ChunkPos = mc.player.getChunkPos();
            Map<BlockPos, Integer> newCache = new HashMap<>();
            int radius = chunkRadius.get();
            for(int chunkX = ChunkPos.x - radius; chunkX <= ChunkPos.x + radius; chunkX++){
                for(int chunkZ = ChunkPos.z - radius; chunkZ <= ChunkPos.z + radius; chunkZ++){
                    WorldChunk chunk = mc.world.getChunk(chunkX, chunkZ);
                    if(chunk != null && chunk.getStatus().isAtLeast(ChunkStatus.FULL)){
                        ScanChunk(chunk, newCache);
                    }
                }
            }
            FlaggedLight.clear();
            FlaggedLight.putAll(newCache);
            tick = 0;
        }
        for(Map.Entry<BlockPos, Integer> entry : FlaggedLight.entrySet()){
            float[] thermal = GetThermal(entry.getValue());
            SettingColor ColorSide = new SettingColor(
                    (int)(thermal[0] * 255),
                    (int)(thermal[1] * 255),
                    (int)(thermal[2] * 255),
                    (int)(thermal[3] * 255)
            );
            SettingColor ColorOutline = new SettingColor(
                    (int)(thermal[0] * 255),
                    (int)(thermal[1] * 255),
                    (int)(thermal[2] * 255),
                    255
            );
            event.renderer.box(entry.getKey(), ColorSide, ColorOutline, LightShapeMode.get(), 0);
        }
    }
    private void ScanChunk(WorldChunk chunk, Map<BlockPos,Integer>cache){
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        for(int x = 0;x < 16;++x){
            for(int z = 0;z < 16;++z){
                for(int y = min.get();y <= max.get();++y){
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    int blockLight = mc.world.getLightLevel(LightType.BLOCK, pos);
                    if(blockLight >= MinLightLevel.get() && blockLight > mc.world.getLightLevel(LightType.SKY, pos)){
                        cache.put(pos, blockLight);
                    }
                }
            }
        }
    }
    private float[] GetThermal(int lightLevel){
        float[] color = new float[4];
        if(lightLevel <= 5){
            color[3] = 0.25F + lightLevel / 5.0F * 0.25F;
        }
        else if(lightLevel <= 10){
            color[3] = 0.5F + (lightLevel - 5) / 5.0F * 0.35F;
        }
        else{
            color[3] = 0.85F + (lightLevel - 10) / 5.0F * 0.15F;
        }
        color[3] = Math.max(0.3F, color[3]);
        if(lightLevel <= 5){
            float intensity = lightLevel / 5.0F;
            color[0] = intensity * 0.4F;
            color[1] = intensity * 0.2F;
            color[2] = 0.0F;
        }
        else if(lightLevel <= 10){
            float intensity = (lightLevel - 5) / 5.0F;
            color[0] = 0.4F + intensity * 0.6F;
            color[1] = 0.2F + intensity * 0.4F;
            color[2] = intensity * 0.2F;
        }
        else{
            float intensity = (lightLevel - 10) / 5.0F;
            color[0] = 1.0F;
            color[1] = 0.6F + intensity * 0.4F;
            color[2] = 0.2F + intensity * 0.3F;
        }
        return color;
    }
}