package com.malioptrender2v.client.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public class MaliTerrainMixin {
    
    @Inject(method = "doTask", at = @At("HEAD"))
    private void mali_SFTGS_rebuild(SectionRenderDispatcher.RenderSection.CompileTask.Source source, CallbackInfoReturnable<CompletableFuture<SectionRenderDispatcher.SectionTaskResult>> cir) {
        // SFTGS: Interceptando a criação da malha para otimização TBDR
    }
}
