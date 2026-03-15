package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PillagerESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
            .name("notifications")
            .description("Chat feedback")
            .defaultValue(true)
            .build());
    private final Setting<Mode> NotificationsMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("notification-mode")
            .description("How it notifies")
            .visible(notifications::get)
            .defaultValue(Mode.Both)
            .build());
    private final Setting<SettingColor> PillagersColor = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("The color of pillagers")
            .defaultValue(new SettingColor(255, 0, 0, 100))
            .build());
    private final Setting<ShapeMode> PillagersShapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How pillagers render")
            .defaultValue(ShapeMode.Both)
            .build());
    private final Setting<Boolean> Tracers = sgGeneral.add(new BoolSetting.Builder()
            .name("tracers-enabled")
            .description("Draw tracers to pillagers")
            .defaultValue(true)
            .build());
    private final Setting<SettingColor> TracersColor = sgGeneral.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("The color of tracers")
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .visible(Tracers::get)
            .build());
    private final List<PillagerEntity> Pillagers = new ArrayList<>();
    private final Set<Integer> Detected = new HashSet<>();
    private int cnt = 0;
    public enum Mode {Chat, Toast, Both}
    public PillagerESP() {
        super(NeonMain.CATEGORY, "pillager-esp", "Flags pillagers");
    }
    @Override
    public void onActivate() {
        Pillagers.clear();
        Detected.clear();
        cnt = 0;
    }
    @Override
    public void onDeactivate() {
        Pillagers.clear();
        Detected.clear();
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || event.renderer == null) return;
            Pillagers.clear();
            Set<Integer> Current = new HashSet<>();
            for(Entity entity : mc.world.getEntities()){
                if(entity instanceof PillagerEntity pillager){
                    Pillagers.add(pillager);
                    Current.add(entity.getId());
                }
            }
            if(Pillagers.size() != cnt){
                if(Pillagers.size() > 0){
                    switch(NotificationsMode.get()){
                        case Chat -> info("Found " + Pillagers.size() + " pillagers in rendered chunks");
                        case Toast -> mc.getToastManager().add(new MeteorToast.Builder("Found Pillagers!").text("Found " + Pillagers.size() + " pillagers in rendered chunks").icon(Items.CROSSBOW).build());
                        case Both -> {
                            info("Found " + Pillagers.size() + " pillagers in rendered chunks");
                            mc.getToastManager().add(new MeteorToast.Builder("Found Pillagers!").text("Found " + Pillagers.size() + " pillagers in rendered chunks").icon(Items.CROSSBOW).build());
                        }
                    }
                }
                cnt = Pillagers.size();
            }
            if(!Current.isEmpty() && !Current.equals(Detected)){
                Set<Integer> newPillagers = new HashSet<>(Current);
                newPillagers.removeAll(Detected);
                if(!newPillagers.isEmpty()){
                    Detected.addAll(newPillagers);
                }
            }
            else if(Current.isEmpty()){
                Detected.clear();
            }
            for(PillagerEntity entity : Pillagers){
                if(entity == null || !entity.isAlive()) continue;
                Vec3d pos = entity.getEntityPos();
                Box box = entity.getBoundingBox();
                Color ColorSide = new Color(PillagersColor.get());
                Color ColorOutline = new Color(PillagersColor.get());
                event.renderer.box(box, ColorSide, ColorOutline, PillagersShapeMode.get(), 0);
                if(Tracers.get()){
                    event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, box.getCenter().getX(), box.getCenter().getY(), box.getCenter().getZ(), TracersColor.get());
                }
            }
    }
}