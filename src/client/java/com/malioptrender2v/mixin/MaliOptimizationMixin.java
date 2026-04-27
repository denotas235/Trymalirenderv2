package com.malioptrender2v.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Camera;
import org.joml.Vector4f;

// Pacote atualizado conforme o teu scan
@Mixin(targets = "net.minecraft.client.renderer.fog.FogRenderer")
public class MaliOptimizationMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true, remap = true)
    private static void onSetupFog(Camera camera, int i, Object deltaTracker, float f, Object clientLevel, CallbackInfoReturnable<Vector4f> cir) {
        // Na 1.21.11, retornar um vetor zero e cancelar desativa o processamento
        cir.setReturnValue(new Vector4f(0f, 0f, 0f, 0f));
    }
}
