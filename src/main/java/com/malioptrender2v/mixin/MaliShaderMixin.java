package com.malioptrender2v.mixin;

import com.malioptrender2v.client.MaliHardware;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.ARBParallelShaderCompile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MaliShaderMixin {
    @Inject(method = "initRenderSystem", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) {
        // Se o G52 suportar compilação paralela, usamos 4 threads (comum em Helio G80/85)
        if (MaliHardware.INSTANCE.getHasParallelCompile()) {
            GL11.glHint(ARBParallelShaderCompile.GL_MAX_SHADER_COMPILER_THREADS_ARB, 4);
        }
    }
}
