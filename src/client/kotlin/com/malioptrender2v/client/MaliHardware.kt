package com.malioptrender2v.client

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1
import org.slf4j.LoggerFactory

object MaliHardware {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")
    private val GL_EXT_SET = mutableSetOf<String>()
    private val VK_EXT_SET = mutableSetOf<String>()

    // --- FLAGS OpenGL ---
    var hasParallelCompile = false
    var hasBufferStorage = false
    var hasFBFetch = false

    // --- FLAGS Vulkan ---
    var vkSwapchain = false
    var vkTimelineSemaphore = false
    var vkFloat16Int8 = false
    var vkBufferDeviceAddress = false
    var vkCreateRenderpass2 = false

    // --- MASTER LIST OpenGL (Mali-G52 MC2) ---
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
        "GL_OVR_multiview", "GL_OVR_multiview2"
    )

    // --- MASTER LIST Vulkan (68 extensões) por tier ---
    private data class VkExt(val name: String, val tier: String)

    private val VK_MASTER_LIST = listOf(
        // TIER 1 — Crítico
        VkExt("VK_KHR_swapchain", "🔴 CRÍTICO"),
        VkExt("VK_KHR_surface", "🔴 CRÍTICO"),
        VkExt("VK_KHR_android_surface", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance1", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance2", "🔴 CRÍTICO"),
        VkExt("VK_KHR_maintenance3", "🔴 CRÍTICO"),
        VkExt("VK_KHR_bind_memory2", "🔴 CRÍTICO"),
        VkExt("VK_KHR_get_memory_requirements2", "🔴 CRÍTICO"),
        // TIER 2 — Muito Alto
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
        // TIER 3 — Alto
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
        // TIER 4 — Moderado
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
        // TIER 5 — Baixo
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
        checkVulkan()
    }

    // ─── OpenGL ───────────────────────────────────────────────
    private fun checkOpenGL() {
        GL_EXT_SET.clear()
        try {
            val num = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS)
            for (i in 0 until num) {
                GL30.glGetStringi(GL11.GL_EXTENSIONS, i)?.let { GL_EXT_SET.add(it) }
            }
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro ao ler extensões OpenGL: ${e.message}")
        }

        hasParallelCompile = GL_EXT_SET.contains("GL_KHR_parallel_shader_compile")
        hasBufferStorage   = GL_EXT_SET.contains("GL_EXT_buffer_storage")
        hasFBFetch         = GL_EXT_SET.contains("GL_ARM_shader_framebuffer_fetch")

        LOGGER.info("════════════════════════════════════════")
        LOGGER.info("  MALIOPT — RELATÓRIO OpenGL ES")
        LOGGER.info("  Dispositivo: TECNO KH7 / Mali-G52 MC2")
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
        "GL_EXT_buffer_storage"          -> hasBufferStorage
        "GL_KHR_parallel_shader_compile" -> hasParallelCompile
        "GL_ARM_shader_framebuffer_fetch" -> hasFBFetch
        else -> false
    }

    // ─── Vulkan ───────────────────────────────────────────────
    private fun checkVulkan() {
        VK_EXT_SET.clear()
        var vulkanVersion = "desconhecida"
        var deviceName = "desconhecido"

        try {
            MemoryStack.stackPush().use { stack ->
                // Criar instância mínima
                val appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .apiVersion(VK_API_VERSION_1_1)

                val createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)

                val pInstance = stack.mallocPointer(1)
                val result = vkCreateInstance(createInfo, null, pInstance)
                if (result != VK_SUCCESS) {
                    LOGGER.warn("[MaliOpt] Vulkan não disponível (código $result)")
                    return
                }
                val instance = VkInstance(pInstance[0], createInfo)

                // Enumerate physical devices
                val pCount = stack.mallocInt(1)
                vkEnumeratePhysicalDevices(instance, pCount, null)
                if (pCount[0] == 0) {
                    LOGGER.warn("[MaliOpt] Nenhuma GPU Vulkan encontrada")
                    vkDestroyInstance(instance, null)
                    return
                }
                val pDevices = stack.mallocPointer(pCount[0])
                vkEnumeratePhysicalDevices(instance, pCount, pDevices)
                val physDevice = VkPhysicalDevice(pDevices[0], instance)

                // Device properties
                val props = VkPhysicalDeviceProperties.malloc(stack)
                vkGetPhysicalDeviceProperties(physDevice, props)
                deviceName = props.deviceNameString()
                val major = VK_VERSION_MAJOR(props.apiVersion())
                val minor = VK_VERSION_MINOR(props.apiVersion())
                val patch = VK_VERSION_PATCH(props.apiVersion())
                vulkanVersion = "$major.$minor.$patch"

                // Enumerate device extensions
                vkEnumerateDeviceExtensionProperties(physDevice, null as CharSequence?, pCount, null)
                val extProps = VkExtensionProperties.malloc(pCount[0], stack)
                vkEnumerateDeviceExtensionProperties(physDevice, null as CharSequence?, pCount, extProps)
                for (i in 0 until extProps.capacity()) {
                    VK_EXT_SET.add(extProps[i].extensionNameString())
                }

                vkDestroyInstance(instance, null)
            }
        } catch (e: Exception) {
            LOGGER.error("[MaliOpt] Erro ao ler extensões Vulkan: ${e.message}")
            return
        }

        // Atualizar flags Vulkan
        vkSwapchain           = VK_EXT_SET.contains("VK_KHR_swapchain")
        vkTimelineSemaphore   = VK_EXT_SET.contains("VK_KHR_timeline_semaphore")
        vkFloat16Int8         = VK_EXT_SET.contains("VK_KHR_shader_float16_int8")
        vkBufferDeviceAddress = VK_EXT_SET.contains("VK_KHR_buffer_device_address")
        vkCreateRenderpass2   = VK_EXT_SET.contains("VK_KHR_create_renderpass2")

        LOGGER.info("════════════════════════════════════════")
        LOGGER.info("  MALIOPT — RELATÓRIO VULKAN")
        LOGGER.info("  GPU: $deviceName")
        LOGGER.info("  Vulkan API: $vulkanVersion")
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
            val status = if (present) "✅" else "❌"
            LOGGER.info("$status ${vkExt.name}")
        }
        LOGGER.info("────────────────────────────────────────")
        LOGGER.info("Vulkan: $found/${VK_MASTER_LIST.size} extensões detetadas")
        LOGGER.info("  swapchain=$vkSwapchain | timeline=$vkTimelineSemaphore | float16=$vkFloat16Int8")
        LOGGER.info("  bufferDeviceAddr=$vkBufferDeviceAddress | renderpass2=$vkCreateRenderpass2")
        LOGGER.info("════════════════════════════════════════")
    }
}
