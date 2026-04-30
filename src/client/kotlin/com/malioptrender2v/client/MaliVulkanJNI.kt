package com.malioptrender2v.client

import org.slf4j.LoggerFactory
import java.io.File

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

    // ── Métodos nativos existentes ───────────────────────────────────────────

    @JvmStatic
    external fun getExtensions(): Array<String>?

    // ── Métodos nativos novos (Plugin) ───────────────────────────────────────

    /** Abre o .so no caminho indicado e enumera extensões Vulkan via vkEnumerate. */
    @JvmStatic
    external fun getVulkanExtensionsFromLib(libPath: String): Array<String>?

    /** Cria pbuffer EGL isolado com as libs do plugin e lê GL_NUM_EXTENSIONS. */
    @JvmStatic
    external fun getGLESExtensionsFromPlugin(pluginDirPath: String): Array<String>?

    // ── Wrapper seguro existente ─────────────────────────────────────────────

    fun getExtensionsSafe(): Set<String> {
        if (!loaded) return emptySet()
        return try {
            getExtensions()?.toHashSet() ?: emptySet()
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro JNI getExtensions: ${e.message}")
            emptySet()
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Plugin discovery
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Vasculha o java.library.path à procura do diretório injetado pelo plugin.
     * O plugin injeta o seu nativeLibraryDir via AndroidManifest <meta-data environment>.
     */
    fun findPluginDir(): File? {
        val libraryPath = System.getProperty("java.library.path") ?: run {
            LOGGER.warn("[MaliOpt|Plugin] java.library.path não definido.")
            return null
        }
        for (segment in libraryPath.split(":")) {
            if (segment.contains("arm64") || segment.contains("aarch64")) {
                val dir = File(segment)
                if (dir.isDirectory) {
                    LOGGER.info("[MaliOpt|Plugin] Diretório encontrado: ${dir.absolutePath}")
                    return dir
                }
            }
        }
        LOGGER.warn("[MaliOpt|Plugin] Diretório do plugin não localizado.")
        return null
    }

    /**
     * Carrega todas as .so do diretório do plugin.
     * Retorna os nomes das que foram carregadas com sucesso.
     */
    fun loadPluginLibraries(dir: File): List<String> {
        val soFiles = dir.listFiles { _, name -> name.endsWith(".so") }
            ?: return emptyList()

        val loaded = mutableListOf<String>()
        for (lib in soFiles) {
            try {
                System.load(lib.absolutePath)
                loaded.add(lib.name)
                LOGGER.info("[MaliOpt|Plugin] ✅ Carregada: ${lib.name}")
            } catch (e: UnsatisfiedLinkError) {
                LOGGER.warn("[MaliOpt|Plugin] ❌ Falhou:    ${lib.name} — ${e.message}")
            }
        }
        LOGGER.info("[MaliOpt|Plugin] Total: ${loaded.size}/${soFiles.size} bibliotecas carregadas.")
        return loaded
    }

    /**
     * Para cada .so com nome que sugira Vulkan ou Mali, tenta enumerar
     * extensões Vulkan diretamente. Retorna Map<nomeLib, extensões>.
     */
    fun getPluginVulkanExtensions(dir: File): Map<String, Set<String>> {
        if (!loaded) return emptyMap()

        val candidates = dir.listFiles { _, name ->
            name.endsWith(".so") &&
            (name.contains("vulkan", ignoreCase = true) ||
             name.contains("mali",   ignoreCase = true))
        } ?: return emptyMap()

        val result = mutableMapOf<String, Set<String>>()
        for (lib in candidates) {
            try {
                val exts = getVulkanExtensionsFromLib(lib.absolutePath)
                if (!exts.isNullOrEmpty()) {
                    result[lib.name] = exts.toHashSet()
                    LOGGER.info("[MaliOpt|Plugin] Vulkan via ${lib.name}: ${exts.size} extensões")
                } else {
                    LOGGER.warn("[MaliOpt|Plugin] Vulkan: ${lib.name} não devolveu extensões.")
                }
            } catch (e: Exception) {
                LOGGER.warn("[MaliOpt|Plugin] Vulkan falhou em ${lib.name}: ${e.message}")
            }
        }
        return result
    }

    /**
     * Cria contexto EGL offscreen usando libEGL.so + libGLESv2.so do plugin,
     * consulta as extensões e restaura o contexto EGL anterior.
     */
    fun getPluginGLESExtensions(dir: File): Set<String> {
        if (!loaded) return emptySet()
        return try {
            getGLESExtensionsFromPlugin(dir.absolutePath)?.toHashSet() ?: emptySet()
        } catch (e: Exception) {
            LOGGER.warn("[MaliOpt|Plugin] GLES JNI falhou: ${e.message}")
            emptySet()
        }
    }
}
