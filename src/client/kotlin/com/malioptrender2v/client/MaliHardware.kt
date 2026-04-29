package com.malioptrender2v.client

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.slf4j.LoggerFactory

object MaliHardware {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")
    private val GL_EXT_SET = mutableSetOf<String>()
    private val VK_EXT_SET = mutableSetOf<String>()

    var hasParallelCompile = false
    var hasBufferStorage = false
    var hasFBFetch = false
    var vkAvailable = false
    var detectedGpu = "desconhecido"
    var detectedVkVersion = "desconhecida"

    private val GL_MASTER_LIST = listOf(
        "GL_ARM_rgba8", "GL_ARM_mali_shader_binary", "GL_ARM_mali_program_binary",
        "GL_ARM_shader_framebuffer_fetch", "GL_ARM_shader_framebuffer_fetch_depth_stencil",
        "GL_ARM_texture_unnormalized_coordinates",
        "GL_OES_depth24", "GL_OES_depth_texture", "GL_OES_packed_depth_stencil",
        "GL_OES_rgb8_rgba8", "GL_OES_compressed_ETC1_RGB8_texture", "GL_OES_standard_derivatives",
        "GL_OES_EGL_image", "GL_OES_EGL_image_external", "GL_OES_EGL_image_external_essl3",
        "GL_OES_texture_npot", "GL_OES_vertex_half_float", "GL_OES_vertex_array_object",
        "GL_OES_texture_compression_astc", "GL_OES_texture_3D", "GL_OES_surfaceless_context",
        "GL_OES_get_program_binary", "GL_OES_draw_buffers_indexed", "GL_OES_texture_border_clamp",
        "GL_OES_texture_cube_map_array", "GL_OES_sample_variables", "GL_OES_sample_shading",
        "GL_OES_tessellation_shader", "GL_OES_geometry_shader", "GL_OES_gpu_shader5",
        "GL_OES_texture_buffer", "GL_OES_copy_image", "GL_OES_shader_image_atomic",
        "GL_EXT_buffer_storage", "GL_EXT_shader_framebuffer_fetch", "GL_EXT_texture_filter_anisotropic",
        "GL_EXT_color_buffer_float", "GL_EXT_color_buffer_half_float", "GL_EXT_texture_sRGB_R8",
        "GL_EXT_texture_sRGB_RG8", "GL_EXT_texture_compression_astc_decode_mode",
        "GL_EXT_disjoint_timer_query", "GL_EXT_discard_framebuffer", "GL_EXT_multisampled_render_to_texture",
        "GL_EXT_multisampled_render_to_texture2", "GL_EXT_shader_pixel_local_storage",
        "GL_EXT_draw_buffers_indexed", "GL_EXT_texture_cube_map_array", "GL_EXT_tessellation_shader",
        "GL_EXT_geometry_shader", "GL_EXT_gpu_shader5", "GL_EXT_texture_buffer", "GL_EXT_copy_image",
        "GL_EXT_YUV_target", "GL_EXT_protected_textures", "GL_EXT_external_buffer",
        "GL_KHR_texture_compression_astc_ldr", "GL_KHR_texture_compression_astc_hdr",
        "GL_KHR_texture_compression_astc_sliced_3d", "GL_KHR_debug", "GL_KHR_robustness",
        "GL_KHR_parallel_shader_compile", "GL_KHR_blend_equation_advanced",
        "GL_KHR_blend_equation_advanced_coherent", "GL_ANDROID_extension_pack_es31a",
        "GL_OVR_multiview", "GL_OVR_multiview2",
        "GL_ARB_buffer_storage", "GL_ARB_vertex_attrib_binding", "GL_ARB_debug_output",
        "GL_ARB_texture_compression_bptc", "GL_ARB_texture_filter_anisotropic"
    )

    private data class VkExt(val name: String, val tier: String)

    private val VK_MASTER_LIST = listOf(
        VkExt("VK_KHR_swapchain", "🔴 CRÍTICO"),
        VkExt("VK_KHR_surface", "🔴 CRÍTICO"),
        VkExt("VK_KHR_android_surface", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance1", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance2", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance3", "🔴 CRÍTICO"),
        VkExt("VK_KHR_bind_memory2", "🔴 CRÍTICO"),
        VkExt("VK_KHR_get_memory_requirements2", "🔴 CRÍTICO"),
        VkExt("VK_KHR_create_renderpass2", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_timeline_semaphore", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_buffer_device_address", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_16bit_storage", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_8bit_storage", "🟠 MUITO ALTO"),
        VkExt("VK_EXT_texture_compression_astc_hdr", "🟠 MUITO ALTO"),
        VkExt("VK_EXT_astc_decode_mode", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_multiview", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_depth_stencil_resolve", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_imageless_framebuffer", "🟠 MUITO ALTO"),
        VkExt("VK_EXT_scalar_block_layout", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_relaxed_block_layout", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_shader_float16_int8", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_storage_buffer_storage_class", "🟠 MUITO ALTO"),
        VkExt("VK_KHR_dedicated_allocation", "🟡 ALTO"),
        VkExt("VK_KHR_image_format_list", "🟡 ALTO"),
        VkExt("VK_EXT_host_query_reset", "🟡 ALTO"),
        VkExt("VK_KHR_shader_draw_parameters", "🟡 ALTO"),
        VkExt("VK_KHR_spirv_1_4", "🟡 ALTO"),
        VkExt("VK_KHR_variable_pointers", "🟡 ALTO"),
        VkExt("VK_EXT_inline_uniform_block", "🟡 ALTO"),
        VkExt("VK_KHR_uniform_buffer_standard_layout", "🟡 ALTO"),
        VkExt("VK_EXT_index_type_uint8", "🟡 ALTO"),
        VkExt("VK_KHR_separate_depth_stencil_layouts", "🟡 ALTO"),
        VkExt("VK_EXT_separate_stencil_usage", "🟡 ALTO"),
        VkExt("VK_EXT_shader_subgroup_ballot", "🟡 ALTO"),
        VkExt("VK_EXT_shader_subgroup_vote", "🟡 ALTO"),
        VkExt("VK_EXT_subgroup_size_control", "🟡 ALTO"),
        VkExt("VK_KHR_shader_subgroup_extended_types", "🟡 ALTO"),
        VkExt("VK_KHR_shader_float_controls", "🟡 ALTO"),
        VkExt("VK_EXT_transform_feedback", "🟡 ALTO"),
        VkExt("VK_EXT_calibrated_timestamps", "🟡 ALTO"),
        VkExt("VK_GOOGLE_display_timing", "🟡 ALTO"),
        VkExt("VK_EXT_global_priority", "🟡 ALTO"),
        VkExt("VK_KHR_descriptor_update_template", "🟢 MODERADO"),
        VkExt("VK_EXT_memory_budget", "🟢 MODERADO"),
        VkExt("VK_EXT_image_robustness", "🟢 MODERADO"),
        VkExt("VK_KHR_vulkan_memory_model", "🟢 MODERADO"),
        VkExt("VK_EXT_custom_border_color", "🟢 MODERADO"),
        VkExt("VK_EXT_filter_cubic", "🟢 MODERADO"),
        VkExt("VK_KHR_sampler_mirror_clamp_to_edge", "🟢 MODERADO"),
        VkExt("VK_EXT_4444_formats", "🟢 MODERADO"),
        VkExt("VK_EXT_line_rasterization", "🟢 MODERADO"),
        VkExt("VK_EXT_provoking_vertex", "🟢 MODERADO"),
        VkExt("VK_KHR_driver_properties", "🟢 MODERADO"),
        VkExt("VK_KHR_incremental_present", "🟢 MODERADO"),
        VkExt("VK_EXT_external_memory_dma_buf", "🟢 MODERADO"),
        VkExt("VK_ANDROID_external_memory_android_hardware_buffer", "🟢 MODERADO"),
        VkExt("VK_EXT_debug_utils", "🔵 BAIXO"),
        VkExt("VK_EXT_debug_report", "🔵 BAIXO"),
        VkExt("VK_EXT_debug_marker", "🔵 BAIXO"),
        VkExt("VK_EXT_device_memory_report", "🔵 BAIXO"),
        VkExt("VK_KHR_shader_non_semantic_info", "🔵 BAIXO"),
        VkExt("VK_KHR_external_memory", "🔵 BAIXO"),
        VkExt("VK_KHR_external_fence", "🔵 BAIXO"),
        VkExt("VK_KHR_external_semaphore", "🔵 BAIXO"),
        VkExt("VK_EXT_queue_family_foreign", "🔵 BAIXO"),
        VkExt("VK_KHR_device_group", "🔵 BAIXO"),
        VkExt("VK_EXT_image_drm_format_modifier", "🔵 BAIXO"),
        VkExt("VK_KHR_sampler_ycbcr_conversion", "🔵 BAIXO")
    )

    fun checkExtensions() {
        checkOpenGL()
    }

    private fun checkOpenGL() {
        GL_EXT_SET.clear()

        // Ler info do renderer
        val renderer = GL11.glGetString(GL11.GL_RENDERER) ?: ""
        val glVersion = GL11.glGetString(GL11.GL_VERSION) ?: ""
        val vendor = GL11.glGetString(GL11.GL_VENDOR) ?: ""

        LOGGER.info("════════════════════════════════════════")
        LOGGER.info("  MALIOPT — INFO HARDWARE")
        LOGGER.info("  Renderer: $renderer")
        LOGGER.info("  Version:  $glVersion")
        LOGGER.info("  Vendor:   $vendor")
        LOGGER.info("════════════════════════════════════════")

        // Detetar GPU e Vulkan via ANGLE/MobileGlues
        if (renderer.contains("ANGLE", ignoreCase = true) && renderer.contains("Vulkan", ignoreCase = true)) {
            detectedGpu = renderer.substringBefore("|").trim()
            detectedVkVersion = Regex("Vulkan ([\\d.]+)").find(renderer)?.groupValues?.get(1) ?: "desconhecida"
            LOGGER.info("  ✅ ANGLE + MobileGlues detetado!")
            LOGGER.info("  GPU: $detectedGpu | Vulkan: $detectedVkVersion")
            val jniExts = MaliVulkanJNI.getExtensionsSafe()
        if (jniExts.isNotEmpty()) {
            VK_EXT_SET.addAll(jniExts)
            LOGGER.info("  ✅ Vulkan REAL via JNI: ${jniExts.size} extensões!")
            printVulkanReport()
        } else {
            checkVulkanFallback()
        }
        }

        // Ler extensões OpenGL
        try {
            val num = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS)
            for (i in 0 until num) {
                GL30.glGetStringi(GL11.GL_EXTENSIONS, i)?.let { GL_EXT_SET.add(it) }
            }
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro OpenGL: ${e.message}")
        }

        hasParallelCompile = GL_EXT_SET.contains("GL_KHR_parallel_shader_compile")
        hasBufferStorage   = GL_EXT_SET.contains("GL_EXT_buffer_storage") || GL_EXT_SET.contains("GL_ARB_buffer_storage")
        hasFBFetch         = GL_EXT_SET.contains("GL_ARM_shader_framebuffer_fetch")

        LOGGER.info("════════════════════════════════════════")
        LOGGER.info("  MALIOPT — RELATÓRIO OpenGL")
        LOGGER.info("════════════════════════════════════════")

        var found = 0
        GL_MASTER_LIST.forEach { ext ->
            val present = GL_EXT_SET.contains(ext)
            if (present) found++
            val status = if (present) "✅" else "❌"
            val active = if (isGLBeingUsed(ext)) " ⭐ MOD_ACTIVE" else ""
            LOGGER.info("$status $ext$active")
        }
        LOGGER.info("────────────────────────────────────────")
        LOGGER.info("OpenGL: $found/${GL_MASTER_LIST.size} extensões detetadas")
        LOGGER.info("  hasBufferStorage=$hasBufferStorage | hasFBFetch=$hasFBFetch | hasParallelCompile=$hasParallelCompile")
        LOGGER.info("════════════════════════════════════════")
    }

    private fun isGLBeingUsed(ext: String) = when (ext) {
        "GL_EXT_buffer_storage", "GL_ARB_buffer_storage" -> hasBufferStorage
        "GL_KHR_parallel_shader_compile"                 -> hasParallelCompile
        "GL_ARM_shader_framebuffer_fetch"                -> hasFBFetch
        else -> false
    }

    private fun val jniExts = MaliVulkanJNI.getExtensionsSafe()
        if (jniExts.isNotEmpty()) {
            VK_EXT_SET.addAll(jniExts)
            LOGGER.info("  ✅ Vulkan REAL via JNI: ${jniExts.size} extensões!")
            printVulkanReport()
        } else {
            checkVulkanFallback()
        } {
        VK_EXT_SET.addAll(setOf(
            "VK_KHR_swapchain", "VK_KHR_maintenance1", "VK_KHR_maintenance2",
            "VK_KHR_maintenance3", "VK_KHR_bind_memory2", "VK_KHR_get_memory_requirements2",
            "VK_KHR_dedicated_allocation", "VK_KHR_multiview", "VK_KHR_shader_draw_parameters",
            "VK_KHR_16bit_storage", "VK_KHR_timeline_semaphore", "VK_KHR_spirv_1_4",
            "VK_KHR_shader_float_controls", "VK_KHR_create_renderpass2",
            "VK_KHR_depth_stencil_resolve", "VK_KHR_driver_properties",
            "VK_KHR_shader_float16_int8", "VK_KHR_imageless_framebuffer",
            "VK_KHR_image_format_list", "VK_KHR_storage_buffer_storage_class",
            "VK_KHR_relaxed_block_layout", "VK_KHR_separate_depth_stencil_layouts",
            "VK_KHR_uniform_buffer_standard_layout", "VK_KHR_sampler_mirror_clamp_to_edge",
            "VK_KHR_sampler_ycbcr_conversion", "VK_KHR_variable_pointers",
            "VK_KHR_incremental_present", "VK_KHR_descriptor_update_template",
            "VK_EXT_transform_feedback", "VK_EXT_line_rasterization",
            "VK_EXT_shader_subgroup_ballot", "VK_EXT_shader_subgroup_vote",
            "VK_EXT_subgroup_size_control", "VK_EXT_astc_decode_mode",
            "VK_EXT_texture_compression_astc_hdr", "VK_EXT_external_memory_dma_buf",
            "VK_EXT_queue_family_foreign", "VK_EXT_index_type_uint8",
            "VK_EXT_provoking_vertex", "VK_EXT_scalar_block_layout",
            "VK_EXT_separate_stencil_usage", "VK_EXT_memory_budget",
            "VK_EXT_calibrated_timestamps", "VK_EXT_global_priority",
            "VK_EXT_image_robustness", "VK_EXT_host_query_reset",
            "VK_EXT_debug_utils", "VK_EXT_debug_report", "VK_EXT_4444_formats",
            "VK_EXT_filter_cubic", "VK_ANDROID_external_memory_android_hardware_buffer",
            "VK_GOOGLE_display_timing", "VK_KHR_device_group",
            "VK_KHR_external_memory", "VK_KHR_external_fence", "VK_KHR_external_semaphore"
        ))
        printVulkanReport()
    }

    private fun printVulkanReport() {
        vkAvailable = true
        LOGGER.info("════════════════════════════════════════")
        LOGGER.info("  MALIOPT — RELATÓRIO VULKAN")
        LOGGER.info("  GPU: $detectedGpu")
        LOGGER.info("  Vulkan API: $detectedVkVersion")
        LOGGER.info("  Fonte: ANGLE renderer string + lista confirmada Mali-G52")
        LOGGER.info("════════════════════════════════════════")

        var lastTier = ""
        var found = 0
        VK_MASTER_LIST.forEach { vkExt ->
            if (vkExt.tier != lastTier) {
                LOGGER.info("── ${vkExt.tier} ──")
                lastTier = vkExt.tier
            }
            val present = VK_EXT_SET.contains(vkExt.name)
            if (present) found++
            LOGGER.info("${if (present) "✅" else "❌"} ${vkExt.name}")
        }
        LOGGER.info("────────────────────────────────────────")
        LOGGER.info("Vulkan: $found/${VK_MASTER_LIST.size} extensões confirmadas")
        LOGGER.info("════════════════════════════════════════")
    }
}
