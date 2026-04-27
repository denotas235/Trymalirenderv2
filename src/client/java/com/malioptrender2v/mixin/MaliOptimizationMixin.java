package com.malioptrender2v.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MaliOptimizationMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevelHead(CallbackInfo ci) {
        // Desativa o nevoeiro a nível de sistema para poupar o GPU Mali
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
    }
}
