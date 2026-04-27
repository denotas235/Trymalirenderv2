package com.malioptrender2v.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Camera;

/* 
 * Patch para 1.21.11 (Mounts of Mayhem)
 * Alvo dinâmico para evitar erros de símbolo no build
 */
@Mixin(targets = "net.minecraft.client.renderer.FogRenderer")
public class MaliOptimizationMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true, remap = true)
    private static void onSetupFog(Camera camera, Object fogMode, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        // Cancela o cálculo de neblina original para aliviar o Mali-G52
        ci.cancel();
    }
}
