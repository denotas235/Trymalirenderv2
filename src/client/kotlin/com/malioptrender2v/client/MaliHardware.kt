package com.malioptrender2v.client

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.slf4j.LoggerFactory

object MaliHardware {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")
    
    var hasParallelCompile = false
    var hasBufferStorage = false
    var hasFramebufferFetch = false

    fun checkExtensions() {
        try {
            val extensionCount = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS)
            LOGGER.info("[MaliOpt] Detetadas $extensionCount extensões OpenGL ES")

            val availableExtensions = mutableSetOf<String>()

            for (i in 0 until extensionCount) {
                val ext = GL30.glGetStringi(GL11.GL_EXTENSIONS, i) ?: continue
                availableExtensions.add(ext)
                // Opcional: Descomenta a linha abaixo se quiseres ver TODAS no log do jogo
                // LOGGER.info("[MaliOpt] Extensão disponível: $ext")
            }

            // Verificação Segura (Sem Crash)
            hasParallelCompile = availableExtensions.contains("GL_KHR_parallel_shader_compile")
            hasBufferStorage = availableExtensions.contains("GL_EXT_buffer_storage")
            hasFramebufferFetch = availableExtensions.contains("GL_ARM_shader_framebuffer_fetch")

            LOGGER.info("[MaliOpt] Scanner concluído. Paralelismo: $hasParallelCompile, Storage: $hasBufferStorage")
            
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro ao escanear extensões: ${e.message}")
        }
    }
}
