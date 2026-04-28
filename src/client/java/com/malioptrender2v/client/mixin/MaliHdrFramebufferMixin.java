package com.malioptrender2v.client.mixin;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
@Mixin(RenderTarget.class)
public class MaliHdrFramebufferMixin {
    @ModifyArgs(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/ByteBuffer;)V", remap = false))
    private void upgradeToHDR(Args args) {
        args.set(2, 34842); 
        args.set(7, 5131);
    }
}
