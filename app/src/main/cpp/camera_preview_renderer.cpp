//
// Created by LimSeungTaek on 2016. 12. 11..
//

#include <jni.h>
#include <string>
#include "logger.h"

#define NELEM(x) ((int)(sizeof(x)/sizeof((x)[0])))

#ifdef __cplusplus
extern "C" {
#endif
void test(JNIEnv *env, jobject /* this */) {

    std::string hello = "Hello from C++";
    printf("%s", hello.c_str());

    LOGD("hello");
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
            {"test", "()V", (void*)test}
    };

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK){
        return result;
    }

    jniRegisterNativMethod(env, "com/st/android/device/camera/gl/ImageProcessor", methodList, NELEM(methodList));

    return JNI_VERSION_1_6;
}