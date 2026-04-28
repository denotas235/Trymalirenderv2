package com.malioptrender2v.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class MaliLightTextureMixin {
    @Inject(method = "updateLightTexture(F)V", at = @At("RETURN"))
    private void mali_framebufferFetch(float partialTick, CallbackInfo ci) {
        // Aqui o hardware Mali usa o Framebuffer Fetch nativamente 
        // se o shader estiver configurado para ler do anexo de cor atual.
    }
}
