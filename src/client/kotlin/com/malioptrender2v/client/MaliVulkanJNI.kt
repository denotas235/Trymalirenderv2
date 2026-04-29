package com.malioptrender2v.client

import org.slf4j.LoggerFactory

object MaliVulkanJNI {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")
    private var loaded = false

    init {
        try {
            System.loadLibrary("malivulkan")
            loaded = true
            LOGGER.info("[MaliOpt] libmalivulkan.so carregada com sucesso!")
        } catch (e: UnsatisfiedLinkError) {
            LOGGER.warn("[MaliOpt] libmalivulkan.so não encontrada: ${e.message}")
        }
    }

    @JvmStatic
    external fun getExtensions(): Array<String>?

    fun getExtensionsSafe(): Set<String> {
        if (!loaded) return emptySet()
        return try {
            getExtensions()?.toHashSet() ?: emptySet()
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro JNI: ${e.message}")
            emptySet()
        }
    }
}
