package com.malioptrender2v.client

import net.fabricmc.api.ClientModInitializer
import org.lwjgl.opengl.GL11
import org.slf4j.LoggerFactory

object Malioptrenderv2Client : ClientModInitializer {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")

    override fun onInitializeClient() {
        val renderer = GL11.glGetString(GL11.GL_RENDERER) ?: "Desconhecido"
        val vendor = GL11.glGetString(GL11.GL_VENDOR) ?: "Desconhecido"

        LOGGER.info("Iniciando MaliOpt para 1.21.11")
        
        if (renderer.contains("Mali", ignoreCase = true) || vendor.contains("ARM", ignoreCase = true)) {
            LOGGER.info("[MaliOpt] Hardware MALI detetado: $renderer")
        } else {
            LOGGER.info("[MaliOpt] Hardware não-Mali detetado: $renderer")
        }
    }
}
