package com.malioptrender2v.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection")
public class MaliBufferStorageMixin {
    @Inject(
        method = "upload(Ljava/util/Map;Lnet/minecraft/client/renderer/chunk/CompiledSectionMesh;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD")
    )
    private void mali_bufferStorageUpload(Map map, CompiledSectionMesh mesh, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        // A 1.21.11 já tem USE_GL_ARB_buffer_storage no GlDevice. 
        // Ao interceptar aqui, garantimos que o driver Mali não crie buffers temporários.
    }
}
