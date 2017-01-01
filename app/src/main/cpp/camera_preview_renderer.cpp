//
// Created by LimSeungTaek on 2016. 12. 11..
//

#include <jni.h>
#include <string>
#include "logger.h"
#include "image/yuv2rgb.neon.h"
#include "ImageProcessorEvents.h"

#define TAG 	"native_renderer"
#define NELEM(x) ((int)(sizeof(x)/sizeof((x)[0])))

#ifdef __cplusplus
extern "C" {
#endif

JavaVM 	    *g_VM = NULL;

char        *g_pARGBPreviewImg = NULL;
int		    g_nPreviewImageWidth = 0;
int		    g_nPreviewImageHeight = 0;

uint8_t     *g_pUptr = NULL;
uint8_t 	*g_pVptr = NULL;

void test(JNIEnv *env, jobject /* this */) {

    std::string hello = "Hello from C++";
    printf("%s", hello.c_str());

    LOGD(TAG, "hello");
}

void setPreviewImage(JNIEnv* env, jobject obj, jbyteArray yuvImage, jint imgWidth, jint imgHeight) {
    LOGD(TAG, "setPreviewImage");

    ImageProcessorEvents::saveBitmap(imgWidth, imgHeight);

    size_t len = env->GetArrayLength(yuvImage);
    jbyte *nativeBytes = env->GetByteArrayElements(yuvImage, 0);

    if (g_pARGBPreviewImg == NULL) {
        g_nPreviewImageWidth = imgWidth;
        g_nPreviewImageHeight = imgHeight;

        g_pARGBPreviewImg = new char[g_nPreviewImageWidth * g_nPreviewImageHeight * 4];
    }

    if (g_pUptr == NULL) {
        g_pUptr = new uint8_t[imgWidth * imgHeight / 4];
    }

    if (g_pVptr == NULL) {
        g_pVptr = new uint8_t[imgWidth * imgHeight / 4];
    }

    char *grayImg = (char *)nativeBytes;

    uint8_t *y_ptr = (uint8_t *)nativeBytes;
    uint8_t *u_ptr = (uint8_t *)(y_ptr + imgWidth * imgHeight);
    uint8_t *v_ptr = (uint8_t *)(u_ptr + (imgWidth * imgHeight) / 4);

//#if 1
//#if 1
    /**
     * Android supported preview format is NV21 this is same yuv420sp. yuv420sp is different with
     * yuv420 format. Below for loop is for proper color converting
     */
    for (int i = 0; i < imgWidth * imgHeight / 4; i++)
    {
        g_pUptr [i] = u_ptr[i * 2];
        g_pVptr [i] = u_ptr[i * 2 + 1];
    }

    _yuv420_2_rgb8888_neon((uint8_t *)(g_pARGBPreviewImg),
                           y_ptr,
                           g_pUptr,
                           g_pVptr,
                           imgWidth, imgHeight,
                           imgWidth, imgWidth / 2, imgWidth * 4);

//#else
//    _yuv420_2_rgb8888_neon((uint8_t *)(g_pCamAbgrImg->imageData),
//							y_ptr,
//							u_ptr,
//							v_ptr,
//							g_pCamAbgrImg->width, g_pCamAbgrImg->height,
//							g_pCamAbgrImg->width, g_pCamAbgrImg->width / 2, g_pCamAbgrImg->widthStep);
//
//#endif
//#else
//    //yuv420ToAbgr(g_pCamGrayImg->imageData, g_pCamAbgrImg->imageData, g_pCamAbgrImg->width, g_pCamAbgrImg->height);
//
//	yuv420ToAbgr2(g_pCamGrayImg->imageData, (int *) (g_pCamAbgrImg->imageData), g_pCamAbgrImg->width, g_pCamAbgrImg->height);
//#endif

    //g_LiveFilterRender.setTexture(g_pTextureImg, g_nTextureWidth, g_nTextureHeight);
//    g_LiveFilterRender.setTexture(g_pARGBPreviewImg, g_nPreviewImageWidth, g_nPreviewImageHeight);


    env->ReleaseByteArrayElements(yuvImage, nativeBytes, JNI_ABORT);
}

#ifdef __cplusplus
}
#endif

int jniRegisterNativMethod(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods ) {
    jclass clazz;

    clazz = env->FindClass(className);

    if(clazz == NULL){
        return -1;
    }

    if(env->RegisterNatives(clazz, gMethods, numMethods) < 0){
        return -1;
    }
    return 0;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    static JNINativeMethod methodList[] = {
            /* name, signature, funcPtr */
            {"test", "()V", (void*)test},
            {"setPreviewImage", "([BII)V", (void*) setPreviewImage}
    };

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK){
        return result;
    }

    jniRegisterNativMethod(env, "com/st/android/device/camera/gl/ImageProcessor", methodList, NELEM(methodList));
    env->GetJavaVM(&g_VM);

    return JNI_VERSION_1_6;
}