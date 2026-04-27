package com.malioptrender2v.mixin;

import com.malioptrender2v.client.MaliHardware;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MaliShaderMixin {
    @Inject(method = "initRenderSystem", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) {
        try {
            if (MaliHardware.INSTANCE.getHasParallelCompile()) {
                // 0x8DA1 = GL_MAX_SHADER_COMPILER_THREADS_ARB
                // Usamos 4 threads para o Mali-G52 equilibrar consumo/performance
                GL11.glHint(0x8DA1, 4);
            }
        } catch (Exception e) {
            // Se o driver falhar, o jogo continua normalmente sem a otimização
        }
    }
}
