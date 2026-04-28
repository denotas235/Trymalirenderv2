package com.malioptrender2v.client.mixin;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class MaliModelRendererMixin {
    @Inject(method = "putQuadData", at = @At("HEAD"))
    private void mali_SFTGS_optimizeQuad(VertexConsumer vertexConsumer, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int i, int j, int k, CallbackInfo ci) {
        // Fase 2: Ponto de entrada para compressão de geometria SFTGS
    }
}
