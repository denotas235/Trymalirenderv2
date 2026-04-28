package com.malioptrender2v.client.mixin;
import net.minecraft.client.renderer.block.ModelBlockRenderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class MaliModelRendererMixin {
    @Inject(
        method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;I)V",
        at = @At("HEAD"),
        remap = true,
        cancellable = true
    )
    private void mali_SFTGS_optimizeQuad(BlockAndTintGetter level, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad bakedQuad, @Coerce Object storage, int light, CallbackInfo ci) {
        Direction dir = bakedQuad.direction();
        if (dir != null && !ModelBlockRenderer.shouldRenderFace(level, state, false, dir, pos.relative(dir))) {
            ci.cancel(); // Descarta a face se ela estiver escondida por outro bloco
        }
    }
}
