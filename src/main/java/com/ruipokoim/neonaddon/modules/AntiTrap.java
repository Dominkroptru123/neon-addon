package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class AntiTrap extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
            .name("notifications")
            .description("Chat feedback")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> ArmorStand = sgGeneral.add(new BoolSetting.Builder()
            .name("armor-stands")
            .description("Armor stands")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> ChestMinecart = sgGeneral.add(new BoolSetting.Builder()
            .name("chest-minecarts")
            .description("Chest minecarts")
            .defaultValue(true)
            .build()
    );
    public AntiTrap(){
        super(NeonMain.CATEGORY, "anti-trap", "Disable trap entities");
    }
    @Override
    public void onActivate(){
        RemoveEntities();
    }
    @EventHandler
    private void onEntityAdded(EntityAddedEvent event){
        Entity entity = event.entity;
        if(IsTrap(entity)){
            entity.discard();
            if(notifications.get()){
                info("Prevented 1 %s from spawning", entity.getStringifiedName());
            }
        }
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.world == null) return;
        if(mc.player.age % 10 == 0){
            List<Entity> RemoveList = new ArrayList<>();
            for(Entity entity : mc.world.getEntities()){
                if(IsTrap(entity)){
                    RemoveList.add(entity);
                }
            }
            for(Entity entity : RemoveList){
                entity.discard();
            }
        }
    }
    private void RemoveEntities(){
        if (mc.world == null) return;
        List<Entity> Entities = new ArrayList<>();
        for(Entity entity : mc.world.getEntities()){
            if(IsTrap(entity)){
                Entities.add(entity);
            }
        }
        for(Entity entity : Entities){
            entity.discard();
        }
        if(!Entities.isEmpty()){
            if(notifications.get()){
                info("Removed %d entities", Entities.size());
            }
        }
    }
    private boolean IsTrap(Entity entity){
        if (entity == null) return false;
        EntityType<?> type = entity.getType();
        if(ArmorStand.get() && type == EntityType.ARMOR_STAND){
            return true;
        }
        if(ChestMinecart.get() && type == EntityType.CHEST_MINECART){
            return true;
        }
        return false;
    }
}