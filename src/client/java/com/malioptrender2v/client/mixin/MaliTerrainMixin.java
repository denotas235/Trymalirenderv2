package com.malioptrender2v.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public class MaliTerrainMixin {
    
    @Inject(method = "doTask", at = @At("HEAD"), cancellable = true)
    private void mali_SFTGS_rebuild(Object source, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        // Usamos CompletableFuture<?> para contornar o acesso privado ao SectionTaskResult
        // O parâmetro 'source' como Object evita o erro de 'class Source not found'
    }
}
