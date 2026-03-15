package com.ruipokoim.neonaddon.mixin;

import com.ruipokoim.neonaddon.modules.AnchorCharge;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void isPressed(CallbackInfo ci) {
        if (Modules.get().get(AnchorCharge.class).IsSneaking){
            playerInput = new PlayerInput(
                    playerInput.forward(),
                    playerInput.backward(),
                    playerInput.left(),
                    playerInput.right(),
                    playerInput.jump(),
                    true,
                    playerInput.sprint()
            );
        }
    }
}