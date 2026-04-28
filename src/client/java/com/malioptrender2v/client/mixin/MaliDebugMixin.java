package com.malioptrender2v.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MaliDebugMixin {
    @Inject(method = "renderLines", at = @At("HEAD"))
    private void mali_addDebugInfo(GuiGraphics guiGraphics, List<String> list, boolean leftSide, CallbackInfo ci) {
        if (leftSide) { // Adiciona apenas na coluna da esquerda
            list.add("");
            list.add("§6[MaliOpt V2]§r SFCRS: §aON§r | SFTGS: §aACTIVE (v2.0)§r");
            list.add("§7Hardware: ARM Mali-G52 (TECNO KH7)§r");
        }
    }
}
