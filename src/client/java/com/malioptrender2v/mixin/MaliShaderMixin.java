package com.malioptrender2v.mixin;

import com.malioptrender2v.client.MaliHardware;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public class MaliShaderMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (MaliHardware.INSTANCE.getHasParallelCompile()) {
            try {
                // 0x8DA1 = GL_MAX_SHADER_COMPILER_THREADS_ARB
                GL11.glHint(0x8DA1, 4);
            } catch (Exception ignored) {}
        }
    }
}
