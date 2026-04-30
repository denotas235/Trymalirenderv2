#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>

// ═══════════════════════════════════════════════════════════
//  Vulkan — tipos mínimos
// ═══════════════════════════════════════════════════════════

typedef void* VkInstance;
typedef void* VkPhysicalDevice;
typedef int   VkResult;

#define VK_SUCCESS                             0
#define VK_STRUCTURE_TYPE_APPLICATION_INFO     0
#define VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO 1
#define VK_API_VERSION_1_1                     ((1<<22)|(1<<12)|177)

typedef struct {
    int    sType;
    void*  pNext;
    int    flags;
    int    apiVersion;
    char*  pApplicationName;
    int    applicationVersion;
    char*  pEngineName;
    int    engineVersion;
} VkApplicationInfo;

typedef struct {
    int                sType;
    void*              pNext;
    int                flags;
    VkApplicationInfo* pApplicationInfo;
    int                enabledLayerCount;
    char**             ppEnabledLayerNames;
    int                enabledExtensionCount;
    char**             ppEnabledExtensionNames;
} VkInstanceCreateInfo;

typedef struct {
    char extensionName[256];
    int  specVersion;
} VkExtensionProperties;

typedef VkResult (*PFN_vkCreateInstance)(const VkInstanceCreateInfo*, void*, VkInstance*);
typedef VkResult (*PFN_vkEnumeratePhysicalDevices)(VkInstance, unsigned int*, VkPhysicalDevice*);
typedef VkResult (*PFN_vkEnumerateDeviceExtensionProperties)(VkPhysicalDevice, const char*, unsigned int*, VkExtensionProperties*);
typedef void     (*PFN_vkDestroyInstance)(VkInstance, void*);

// ═══════════════════════════════════════════════════════════
//  EGL / GLES — tipos mínimos
// ═══════════════════════════════════════════════════════════

typedef void*        EGLDisplay;
typedef void*        EGLConfig;
typedef void*        EGLContext;
typedef void*        EGLSurface;
typedef int          EGLint;
typedef unsigned int EGLenum;
typedef int          EGLBoolean;

#define EGL_DEFAULT_DISPLAY        ((void*)0)
#define EGL_NO_DISPLAY             ((void*)0)
#define EGL_NO_CONTEXT             ((void*)0)
#define EGL_NO_SURFACE             ((void*)0)
#define EGL_DRAW                   0x3059
#define EGL_READ                   0x305A
#define EGL_OPENGL_ES2_BIT         0x0004
#define EGL_OPENGL_ES_API          0x30A0
#define EGL_CONTEXT_CLIENT_VERSION 0x3098
#define EGL_PBUFFER_BIT            0x0001
#define EGL_SURFACE_TYPE           0x3033
#define EGL_RENDERABLE_TYPE        0x3040
#define EGL_WIDTH                  0x3057
#define EGL_HEIGHT                 0x3056
#define EGL_NONE                   0x3038
#define EGL_TRUE                   1
#define EGL_FALSE                  0
#define GL_EXTENSIONS              0x1F03
#define GL_NUM_EXTENSIONS          0x821D

typedef EGLDisplay (*PFN_eglGetDisplay)(void*);
typedef EGLBoolean (*PFN_eglInitialize)(EGLDisplay, EGLint*, EGLint*);
typedef EGLBoolean (*PFN_eglBindAPI)(EGLenum);
typedef EGLBoolean (*PFN_eglChooseConfig)(EGLDisplay, const EGLint*, EGLConfig*, EGLint, EGLint*);
typedef EGLSurface (*PFN_eglCreatePbufferSurface)(EGLDisplay, EGLConfig, const EGLint*);
typedef EGLContext (*PFN_eglCreateContext)(EGLDisplay, EGLConfig, EGLContext, const EGLint*);
typedef EGLBoolean (*PFN_eglMakeCurrent)(EGLDisplay, EGLSurface, EGLSurface, EGLContext);
typedef EGLDisplay (*PFN_eglGetCurrentDisplay)(void);
typedef EGLSurface (*PFN_eglGetCurrentSurface)(EGLint);
typedef EGLContext (*PFN_eglGetCurrentContext)(void);
typedef EGLBoolean (*PFN_eglDestroyContext)(EGLDisplay, EGLContext);
typedef EGLBoolean (*PFN_eglDestroySurface)(EGLDisplay, EGLSurface);
typedef EGLBoolean (*PFN_eglTerminate)(EGLDisplay);
typedef void       (*PFN_glGetIntegerv)(unsigned int, int*);
typedef const unsigned char* (*PFN_glGetStringi)(unsigned int, unsigned int);

// ═══════════════════════════════════════════════════════════
//  Helper interno: extensões Vulkan de um .so específico
// ═══════════════════════════════════════════════════════════

static jobjectArray vulkanExtsFromPath(JNIEnv* env, const char* libPath) {
    void* lib = dlopen(libPath, RTLD_NOW | RTLD_LOCAL);
    if (!lib) return NULL;

    PFN_vkCreateInstance vkCI =
        (PFN_vkCreateInstance)dlsym(lib, "vkCreateInstance");
    PFN_vkEnumeratePhysicalDevices vkEPD =
        (PFN_vkEnumeratePhysicalDevices)dlsym(lib, "vkEnumeratePhysicalDevices");
    PFN_vkEnumerateDeviceExtensionProperties vkEDEP =
        (PFN_vkEnumerateDeviceExtensionProperties)dlsym(lib, "vkEnumerateDeviceExtensionProperties");
    PFN_vkDestroyInstance vkDI =
        (PFN_vkDestroyInstance)dlsym(lib, "vkDestroyInstance");

    if (!vkCI || !vkEPD || !vkEDEP || !vkDI) { dlclose(lib); return NULL; }

    VkApplicationInfo appInfo = {0};
    appInfo.sType      = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.apiVersion = VK_API_VERSION_1_1;

    VkInstanceCreateInfo ci = {0};
    ci.sType            = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    ci.pApplicationInfo = &appInfo;

    VkInstance instance = NULL;
    if (vkCI(&ci, NULL, &instance) != VK_SUCCESS) { dlclose(lib); return NULL; }

    unsigned int deviceCount = 0;
    vkEPD(instance, &deviceCount, NULL);
    if (deviceCount == 0) { vkDI(instance, NULL); dlclose(lib); return NULL; }

    VkPhysicalDevice* devices = malloc(sizeof(VkPhysicalDevice) * deviceCount);
    vkEPD(instance, &deviceCount, devices);
    VkPhysicalDevice gpu = devices[0];
    free(devices);

    unsigned int extCount = 0;
    vkEDEP(gpu, NULL, &extCount, NULL);

    VkExtensionProperties* exts = malloc(sizeof(VkExtensionProperties) * extCount);
    vkEDEP(gpu, NULL, &extCount, exts);

    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray result = (*env)->NewObjectArray(env, (jsize)extCount, strClass, NULL);

    for (unsigned int i = 0; i < extCount; i++) {
        jstring s = (*env)->NewStringUTF(env, exts[i].extensionName);
        (*env)->SetObjectArrayElement(env, result, i, s);
        (*env)->DeleteLocalRef(env, s);
    }

    free(exts);
    vkDI(instance, NULL);
    dlclose(lib);
    return result;
}

// ═══════════════════════════════════════════════════════════
//  JNI existente — getExtensions (libvulkan.so padrão)
// ═══════════════════════════════════════════════════════════

JNIEXPORT jobjectArray JNICALL
Java_com_malioptrender2v_client_MaliVulkanJNI_getExtensions(JNIEnv* env, jclass clazz) {
    return vulkanExtsFromPath(env, "libvulkan.so");
}

// ═══════════════════════════════════════════════════════════
//  JNI NOVO — getVulkanExtensionsFromLib (path explícito)
// ═══════════════════════════════════════════════════════════

JNIEXPORT jobjectArray JNICALL
Java_com_malioptrender2v_client_MaliVulkanJNI_getVulkanExtensionsFromLib(
        JNIEnv* env, jclass clazz, jstring libPathJ) {
    const char* path = (*env)->GetStringUTFChars(env, libPathJ, NULL);
    jobjectArray result = vulkanExtsFromPath(env, path);
    (*env)->ReleaseStringUTFChars(env, libPathJ, path);
    return result;
}

// ═══════════════════════════════════════════════════════════
//  JNI NOVO — getGLESExtensionsFromPlugin (pbuffer EGL isolado)
// ═══════════════════════════════════════════════════════════

JNIEXPORT jobjectArray JNICALL
Java_com_malioptrender2v_client_MaliVulkanJNI_getGLESExtensionsFromPlugin(
        JNIEnv* env, jclass clazz, jstring pluginDirJ) {

    const char* dir = (*env)->GetStringUTFChars(env, pluginDirJ, NULL);

    char eglPath[512], glesPath[512];
    snprintf(eglPath,  sizeof(eglPath),  "%s/libEGL.so",    dir);
    snprintf(glesPath, sizeof(glesPath), "%s/libGLESv2.so", dir);
    (*env)->ReleaseStringUTFChars(env, pluginDirJ, dir);

    void* eglLib  = dlopen(eglPath,  RTLD_NOW | RTLD_LOCAL);
    if (!eglLib) return NULL;

    void* glesLib = dlopen(glesPath, RTLD_NOW | RTLD_LOCAL);
    if (!glesLib) { dlclose(eglLib); return NULL; }

    PFN_eglGetDisplay         fGetDisplay  = (PFN_eglGetDisplay)dlsym(eglLib, "eglGetDisplay");
    PFN_eglInitialize         fInit        = (PFN_eglInitialize)dlsym(eglLib, "eglInitialize");
    PFN_eglBindAPI            fBindAPI     = (PFN_eglBindAPI)dlsym(eglLib, "eglBindAPI");
    PFN_eglChooseConfig       fChooseCfg   = (PFN_eglChooseConfig)dlsym(eglLib, "eglChooseConfig");
    PFN_eglCreatePbufferSurface fCreatePbuf = (PFN_eglCreatePbufferSurface)dlsym(eglLib, "eglCreatePbufferSurface");
    PFN_eglCreateContext      fCreateCtx   = (PFN_eglCreateContext)dlsym(eglLib, "eglCreateContext");
    PFN_eglMakeCurrent        fMakeCurrent = (PFN_eglMakeCurrent)dlsym(eglLib, "eglMakeCurrent");
    PFN_eglGetCurrentDisplay  fGetCurDisp  = (PFN_eglGetCurrentDisplay)dlsym(eglLib, "eglGetCurrentDisplay");
    PFN_eglGetCurrentSurface  fGetCurSurf  = (PFN_eglGetCurrentSurface)dlsym(eglLib, "eglGetCurrentSurface");
    PFN_eglGetCurrentContext  fGetCurCtx   = (PFN_eglGetCurrentContext)dlsym(eglLib, "eglGetCurrentContext");
    PFN_eglDestroyContext     fDestroyCtx  = (PFN_eglDestroyContext)dlsym(eglLib, "eglDestroyContext");
    PFN_eglDestroySurface     fDestroySurf = (PFN_eglDestroySurface)dlsym(eglLib, "eglDestroySurface");
    PFN_eglTerminate          fTerminate   = (PFN_eglTerminate)dlsym(eglLib, "eglTerminate");
    PFN_glGetIntegerv         fGetInt      = (PFN_glGetIntegerv)dlsym(glesLib, "glGetIntegerv");
    PFN_glGetStringi          fGetStringi  = (PFN_glGetStringi)dlsym(glesLib, "glGetStringi");

    if (!fGetDisplay || !fInit || !fBindAPI || !fChooseCfg ||
        !fCreatePbuf || !fCreateCtx || !fMakeCurrent ||
        !fDestroyCtx || !fDestroySurf || !fTerminate ||
        !fGetInt     || !fGetStringi) {
        dlclose(glesLib); dlclose(eglLib); return NULL;
    }

    // Guardar contexto EGL atual
    EGLDisplay prevDisp = fGetCurDisp ? fGetCurDisp() : EGL_NO_DISPLAY;
    EGLSurface prevDraw = (fGetCurSurf && prevDisp != EGL_NO_DISPLAY) ? fGetCurSurf(EGL_DRAW) : EGL_NO_SURFACE;
    EGLSurface prevRead = (fGetCurSurf && prevDisp != EGL_NO_DISPLAY) ? fGetCurSurf(EGL_READ)  : EGL_NO_SURFACE;
    EGLContext prevCtx  = fGetCurCtx  ? fGetCurCtx()                  : EGL_NO_CONTEXT;

    EGLDisplay display = fGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) { dlclose(glesLib); dlclose(eglLib); return NULL; }

    EGLint major = 0, minor = 0;
    if (!fInit(display, &major, &minor)) { dlclose(glesLib); dlclose(eglLib); return NULL; }

    fBindAPI(EGL_OPENGL_ES_API);

    const EGLint cfgAttribs[] = {
        EGL_SURFACE_TYPE,    EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_NONE
    };
    EGLConfig cfg = NULL; EGLint numCfg = 0;
    if (!fChooseCfg(display, cfgAttribs, &cfg, 1, &numCfg) || numCfg == 0) {
        fTerminate(display); dlclose(glesLib); dlclose(eglLib); return NULL;
    }

    const EGLint pbufAttribs[] = { EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE };
    EGLSurface surface = fCreatePbuf(display, cfg, pbufAttribs);

    const EGLint ctxAttribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
    EGLContext context = fCreateCtx(display, cfg, EGL_NO_CONTEXT, ctxAttribs);

    jobjectArray result = NULL;

    if (fMakeCurrent(display, surface, surface, context)) {
        int numExt = 0;
        fGetInt(GL_NUM_EXTENSIONS, &numExt);

        jclass strClass = (*env)->FindClass(env, "java/lang/String");
        result = (*env)->NewObjectArray(env, numExt, strClass, NULL);

        for (int i = 0; i < numExt; i++) {
            const char* ext = (const char*)fGetStringi(GL_NUM_EXTENSIONS, (unsigned int)i);
            if (ext) {
                jstring s = (*env)->NewStringUTF(env, ext);
                (*env)->SetObjectArrayElement(env, result, i, s);
                (*env)->DeleteLocalRef(env, s);
            }
        }

        // Restaurar contexto anterior
        if (prevDisp != EGL_NO_DISPLAY)
            fMakeCurrent(prevDisp, prevDraw, prevRead, prevCtx);
        else
            fMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    }

    fDestroyCtx(display, context);
    fDestroySurf(display, surface);
    fTerminate(display);
    dlclose(glesLib);
    dlclose(eglLib);
    return result;
}
