package com.malioptrender2v.client

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.slf4j.LoggerFactory

object MaliHardware {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")
    private val EXT_SET = mutableSetOf<String>()

    var hasParallelCompile = false
    var hasBufferStorage = false
    var hasFBFetch = false

    // Lista mestre das 102 extensões GLES
    private val MASTER_LIST = listOf(
        "GL_ARM_rgba8", "GL_ARM_mali_shader_binary", "GL_ARM_mali_program_binary", "GL_ARM_shader_framebuffer_fetch",
        "GL_ARM_shader_framebuffer_fetch_depth_stencil", "GL_ARM_texture_unnormalized_coordinates",
        "GL_OES_depth24", "GL_OES_depth_texture", "GL_OES_depth_texture_cube_map", "GL_OES_packed_depth_stencil",
        "GL_OES_rgb8_rgba8", "GL_OES_compressed_paletted_texture", "GL_OES_compressed_ETC1_RGB8_texture",
        "GL_OES_standard_derivatives", "GL_OES_EGL_image", "GL_OES_EGL_image_external", "GL_OES_EGL_image_external_essl3",
        "GL_OES_EGL_sync", "GL_OES_texture_npot", "GL_OES_vertex_half_float", "GL_OES_required_internalformat",
        "GL_OES_vertex_array_object", "GL_OES_mapbuffer", "GL_OES_fbo_render_mipmap", "GL_OES_element_index_uint",
        "GL_OES_texture_compression_astc", "GL_OES_texture_3D", "GL_OES_texture_stencil8", "GL_OES_surfaceless_context",
        "GL_OES_get_program_binary", "GL_OES_draw_buffers_indexed", "GL_OES_texture_border_clamp",
        "GL_OES_texture_cube_map_array", "GL_OES_texture_storage_multisample_2d_array", "GL_OES_sample_variables",
        "GL_OES_sample_shading", "GL_OES_shader_multisample_interpolation", "GL_OES_shader_io_blocks",
        "GL_OES_tessellation_shader", "GL_OES_primitive_bounding_box", "GL_OES_geometry_shader", "GL_OES_gpu_shader5",
        "GL_OES_texture_buffer", "GL_OES_copy_image", "GL_OES_draw_elements_base_vertex", "GL_OES_shader_image_atomic",
        "GL_EXT_debug_marker", "GL_EXT_read_format_bgra", "GL_EXT_texture_format_BGRA8888", "GL_EXT_texture_rg",
        "GL_EXT_texture_type_2_10_10_REV", "GL_EXT_shadow_samplers", "GL_EXT_texture_compression_astc_decode_mode",
        "GL_EXT_texture_compression_astc_decode_mode_rgb9e5", "GL_EXT_occlusion_query_boolean", "GL_EXT_disjoint_timer_query",
        "GL_EXT_blend_minmax", "GL_EXT_discard_framebuffer", "GL_EXT_texture_storage", "GL_EXT_multisampled_render_to_texture",
        "GL_EXT_multisampled_render_to_texture2", "GL_EXT_shader_pixel_local_storage", "GL_EXT_sRGB",
        "GL_EXT_sRGB_write_control", "GL_EXT_texture_sRGB_decode", "GL_EXT_texture_sRGB_R8", "GL_EXT_texture_sRGB_RG8",
        "GL_EXT_robustness", "GL_EXT_draw_buffers_indexed", "GL_EXT_texture_border_clamp", "GL_EXT_texture_cube_map_array",
        "GL_EXT_shader_io_blocks", "GL_EXT_tessellation_shader", "GL_EXT_primitive_bounding_box", "GL_EXT_geometry_shader",
        "GL_EXT_gpu_shader5", "GL_EXT_texture_buffer", "GL_EXT_copy_image", "GL_EXT_shader_non_constant_global_initializers",
        "GL_EXT_color_buffer_half_float", "GL_EXT_unpack_subimage", "GL_EXT_color_buffer_float", "GL_EXT_YUV_target",
        "GL_EXT_draw_elements_base_vertex", "GL_EXT_protected_textures", "GL_EXT_buffer_storage", "GL_EXT_external_buffer",
        "GL_EXT_EGL_image_array", "GL_EXT_texture_filter_anisotropic", "GL_EXT_shader_framebuffer_fetch",
        "GL_EXT_KHR_blend_equation_advanced", "GL_KHR_texture_compression_astc_ldr", "GL_KHR_texture_compression_astc_hdr",
        "GL_KHR_texture_compression_astc_sliced_3d", "GL_KHR_debug", "GL_KHR_robustness", "GL_KHR_robust_buffer_access_behavior",
        "GL_KHR_blend_equation_advanced", "GL_KHR_blend_equation_advanced_coherent", "GL_ANDROID_extension_pack_es31a",
        "GL_OVR_multiview", "GL_OVR_multiview2", "GL_OVR_multiview_multisampled_render_to_texture"
    )

    fun checkExtensions() {
        EXT_SET.clear()
        try {
            val num = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS)
            for (i in 0 until num) {
                GL30.glGetStringi(GL11.GL_EXTENSIONS, i)?.let { EXT_SET.add(it) }
            }
        } catch (e: Exception) {}

        hasParallelCompile = EXT_SET.contains("GL_KHR_parallel_shader_compile")
        hasBufferStorage = EXT_SET.contains("GL_EXT_buffer_storage")
        hasFBFetch = EXT_SET.contains("GL_ARM_shader_framebuffer_fetch")

        LOGGER.info("---")
        LOGGER.info("# TECNO KH7 — RELATÓRIO DE CAPACIDADES")
        LOGGER.info("---")
        
        var foundCount = 0
        MASTER_LIST.forEach { ext ->
            val present = EXT_SET.contains(ext)
            if (present) foundCount++
            val status = if (present) "✅ [DETETADA]" else "❌ [AUSENTE]"
            val active = if (isBeingUsed(ext)) " ⭐ (MOD_ACTIVE)" else ""
            LOGGER.info("$status $ext$active")
        }
        LOGGER.info("Resumo: $foundCount/102 extensões suportadas.")
    }

    private fun isBeingUsed(ext: String) = when(ext) {
        "GL_EXT_buffer_storage" -> hasBufferStorage
        "GL_KHR_parallel_shader_compile" -> hasParallelCompile
        "GL_ARM_shader_framebuffer_fetch" -> hasFBFetch
        else -> false
    }
}
