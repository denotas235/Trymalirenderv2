#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

// Tipos Vulkan mínimos
typedef void* VkInstance;
typedef void* VkPhysicalDevice;
typedef int   VkResult;
#define VK_SUCCESS 0
#define VK_STRUCTURE_TYPE_APPLICATION_INFO 0
#define VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO 1
#define VK_API_VERSION_1_1 ((1<<22)|(1<<12)|177)

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
    int                 sType;
    void*               pNext;
    int                 flags;
    VkApplicationInfo*  pApplicationInfo;
    int                 enabledLayerCount;
    char**              ppEnabledLayerNames;
    int                 enabledExtensionCount;
    char**              ppEnabledExtensionNames;
} VkInstanceCreateInfo;

typedef struct {
    char    extensionName[256];
    int     specVersion;
} VkExtensionProperties;

// Ponteiros de função
typedef VkResult (*PFN_vkCreateInstance)(const VkInstanceCreateInfo*, void*, VkInstance*);
typedef VkResult (*PFN_vkEnumeratePhysicalDevices)(VkInstance, unsigned int*, VkPhysicalDevice*);
typedef VkResult (*PFN_vkEnumerateDeviceExtensionProperties)(VkPhysicalDevice, const char*, unsigned int*, VkExtensionProperties*);
typedef void     (*PFN_vkDestroyInstance)(VkInstance, void*);

JNIEXPORT jobjectArray JNICALL
Java_com_malioptrender2v_client_MaliVulkanJNI_getExtensions(JNIEnv* env, jclass clazz) {
    void* lib = dlopen("libvulkan.so", RTLD_NOW | RTLD_LOCAL);
    if (!lib) return NULL;

    PFN_vkCreateInstance vkCreateInstance =
        (PFN_vkCreateInstance)dlsym(lib, "vkCreateInstance");
    PFN_vkEnumeratePhysicalDevices vkEnumeratePhysicalDevices =
        (PFN_vkEnumeratePhysicalDevices)dlsym(lib, "vkEnumeratePhysicalDevices");
    PFN_vkEnumerateDeviceExtensionProperties vkEnumerateDeviceExtensionProperties =
        (PFN_vkEnumerateDeviceExtensionProperties)dlsym(lib, "vkEnumerateDeviceExtensionProperties");
    PFN_vkDestroyInstance vkDestroyInstance =
        (PFN_vkDestroyInstance)dlsym(lib, "vkDestroyInstance");

    if (!vkCreateInstance || !vkEnumeratePhysicalDevices ||
        !vkEnumerateDeviceExtensionProperties || !vkDestroyInstance) {
        dlclose(lib);
        return NULL;
    }

    VkApplicationInfo appInfo = {0};
    appInfo.sType      = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.apiVersion = VK_API_VERSION_1_1;

    VkInstanceCreateInfo createInfo = {0};
    createInfo.sType            = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    VkInstance instance = NULL;
    if (vkCreateInstance(&createInfo, NULL, &instance) != VK_SUCCESS) {
        dlclose(lib);
        return NULL;
    }

    unsigned int deviceCount = 0;
    vkEnumeratePhysicalDevices(instance, &deviceCount, NULL);
    if (deviceCount == 0) {
        vkDestroyInstance(instance, NULL);
        dlclose(lib);
        return NULL;
    }

    VkPhysicalDevice* devices = malloc(sizeof(VkPhysicalDevice) * deviceCount);
    vkEnumeratePhysicalDevices(instance, &deviceCount, devices);
    VkPhysicalDevice physDevice = devices[0];
    free(devices);

    unsigned int extCount = 0;
    vkEnumerateDeviceExtensionProperties(physDevice, NULL, &extCount, NULL);

    VkExtensionProperties* exts = malloc(sizeof(VkExtensionProperties) * extCount);
    vkEnumerateDeviceExtensionProperties(physDevice, NULL, &extCount, exts);

    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray result = (*env)->NewObjectArray(env, extCount, strClass, NULL);

    for (unsigned int i = 0; i < extCount; i++) {
        jstring str = (*env)->NewStringUTF(env, exts[i].extensionName);
        (*env)->SetObjectArrayElement(env, result, i, str);
        (*env)->DeleteLocalRef(env, str);
    }

    free(exts);
    vkDestroyInstance(instance, NULL);
    dlclose(lib);
    return result;
}
