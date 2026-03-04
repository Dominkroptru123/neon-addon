package com.ruipokoim.neonaddon.modules;

import com.ruipokoim.neonaddon.NeonMain;
import com.ruipokoim.neonaddon.mixin.HandledScreenAccessor;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HoverTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> Delay = sgGeneral.add(new IntSetting.Builder()
            .name("tick-delay")
            .description("Ticks to wait to swap")
            .defaultValue(1)
            .min(0)
            .max(20)
            .sliderMin(0)
            .sliderMax(20)
            .build()
    );
    private int tick = Delay.get();
    public HoverTotem() {
        super(NeonMain.CATEGORY, "hover-totem", "Self-explain");
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if(mc.player == null || mc.world == null) return;
        if(mc.currentScreen instanceof InventoryScreen inventoryScreen){
            Slot hoveredSlot = ((HandledScreenAccessor) inventoryScreen).getFocusedSlot();
            if (hoveredSlot != null && hoveredSlot.hasStack()) {
                int slotIndex = hoveredSlot.getIndex();
                if(slotIndex <= 35 && hoveredSlot.getStack().isOf(Items.TOTEM_OF_UNDYING)){
                    if(!mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)){
                        if(tick <= 0){
                            mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, slotIndex, 40, SlotActionType.SWAP, mc.player);
                            tick = Delay.get();
                        }
                        else{
                            tick = Math.min(tick, Delay.get());
                            --tick;
                        }
                    }
                }
            }
        }
    }
}