package com.malioptrender2v.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MaliCullingMixin {
    // Usamos o mapeamento do método direto para evitar falhas de refmap
    @Inject(method = "prepareCull", at = @At("HEAD"), remap = true)
    private void mali_SFCRS_Culling(Camera camera, Frustum frustum, boolean bl, CallbackInfo ci) {
        // Lógica de culling
    }
}
