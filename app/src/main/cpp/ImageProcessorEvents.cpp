#include "ImageProcessorEvents.h"
#include <jni.h>
#include <stdio.h>
#include "include/default.h"
#include "include/logger.h"


#define TAG		"ImageProcessorEvents"


extern  JavaVM 	*g_VM;

void ImageProcessorEvents::saveBitmap(int width, int height)
{
	int status;
	JNIEnv *env;

	if (g_VM == NULL) {
		LOGI(TAG, "g_VM is NULL");
		return;
	}

	status = g_VM->GetEnv((void **) &env, JNI_VERSION_1_6);
	if (status < 0) {
		LOGE(TAG, "callback_handler: failed to attach current thread");
		return;
	}

	/* Construct a Java string */
	jclass imageProcessClz = env->FindClass("com/st/android/device/camera/gl/ImageProcessor");
	if (imageProcessClz == NULL) {
		LOGE(TAG, "callback_handler: failed to get class reference");
		return;
	}

	/* Find the callBack method ID */
	jmethodID method = env->GetStaticMethodID(imageProcessClz, "saveBitmap", "(II)V");
	if (method == NULL) {
		LOGE(TAG, "callback_handler: failed to get method ID");
		return;
	}
	env->CallStaticVoidMethod(imageProcessClz, method, width, height);
}
