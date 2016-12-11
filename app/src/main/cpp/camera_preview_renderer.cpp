//
// Created by LimSeungTaek on 2016. 12. 11..
//

#include <jni.h>
#include <string>
#include "logger.h"

extern "C"
void
Java_com_st_android_device_camera_gl_ImageProcessor_test(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "Hello from C++";
    printf("%s", hello.c_str());

    LOGD("hello");
}
