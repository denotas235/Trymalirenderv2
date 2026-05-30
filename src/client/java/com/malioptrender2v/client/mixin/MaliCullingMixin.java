package com.malioptrender2v.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.render.chunk.WorldRenderer", remap = false)
public abstract class MaliCullingMixin {

    @Shadow(remap = false)
    private boolean graphNeedsUpdate;

    @Unique
    private static Boolean malioptrender$isMali = null;

    @Inject(method = "setupRenderer", at = @At("HEAD"), remap = false)
    private void malioptrender$forceGraphUpdate(
            Camera camera,
            Frustum frustum,
            boolean isCapturedFrustum,
            boolean spectator,
            CallbackInfo ci) {

        if (isCapturedFrustum) return;

        if (malioptrender$isMali == null) {
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            malioptrender$isMali = renderer != null
                    && renderer.toUpperCase().contains("MALI");
        }

        if (!malioptrender$isMali) return;

        this.graphNeedsUpdate = true;
    }
}
