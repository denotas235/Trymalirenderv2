package com.malioptrender2v.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.DeltaTracker;
import org.joml.Vector4f;

@Mixin(targets = "net.minecraft.client.renderer.fog.FogRenderer")
public class MaliOptimizationMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true, remap = true)
    private static void onSetupFog(Camera camera, int i, DeltaTracker deltaTracker, float f, ClientLevel clientLevel, CallbackInfoReturnable<Vector4f> cir) {
        // Retorna um vetor de cor vazio e cancela o processamento de neblina
        cir.setReturnValue(new Vector4f(0f, 0f, 0f, 0f));
    }
}
