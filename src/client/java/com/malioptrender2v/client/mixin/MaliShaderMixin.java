package com.malioptrender2v.client.mixin;

import com.malioptrender2v.client.MaliHardware;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin em GlDevice para ativar compilação paralela de shaders no Mali-G52.
 * GL_MAX_SHADER_COMPILER_THREADS_ARB (0x91B0) define quantas threads
 * o driver pode usar para compilar shaders em paralelo.
 * Requer: GL_ARB_parallel_shader_compile ou GL_KHR_parallel_shader_compile
 */
@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice", remap = false)
public class MaliShaderMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mali_onInit(CallbackInfo ci) {
        if (MaliHardware.INSTANCE.getHasParallelCompile()) {
            try {
                // 0x91B0 = GL_MAX_SHADER_COMPILER_THREADS_ARB
                // Valor 4 = usar até 4 threads (ideal para Mali-G52 com 4 cores)
                // Valor 0xFFFFFFFF = deixar o driver decidir o máximo
                org.lwjgl.opengl.GL11.glHint(0x91B0, 4);
            } catch (Exception e) {
                // Falha silenciosa — o jogo continua sem a otimização
            }
        }
    }
}
