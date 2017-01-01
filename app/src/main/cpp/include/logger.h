//
// Created by LimSeungTaek on 2016. 12. 11..
//

#ifndef MULTICAMERA_LOGGER_H
#define MULTICAMERA_LOGGER_H

#include <android/log.h>

#define  LOGUNK(TAG, ...)  __android_log_print(ANDROID_LOG_UNKNOWN, TAG,__VA_ARGS__)
#define  LOGDEF(TAG, ...)  __android_log_print(ANDROID_LOG_DEFAULT, TAG,__VA_ARGS__)
#define  LOGV(TAG, ...)  __android_log_print(ANDROID_LOG_VERBOSE, TAG,__VA_ARGS__)
#define  LOGD(TAG, ...)  __android_log_print(ANDROID_LOG_DEBUG, TAG,__VA_ARGS__)
#define  LOGI(TAG, ...)  __android_log_print(ANDROID_LOG_INFO, TAG,__VA_ARGS__)
#define  LOGW(TAG, ...)  __android_log_print(ANDROID_LOG_WARN, TAG,__VA_ARGS__)
#define  LOGE(TAG, ...)  __android_log_print(ANDROID_LOG_ERROR, TAG,__VA_ARGS__)
#define  LOGF(TAG, ...)  __android_log_print(ANDROID_FATAL_ERROR, TAG,__VA_ARGS__)
#define  LOGS(TAG, ...)  __android_log_print(ANDROID_SILENT_ERROR, TAG,__VA_ARGS__)

#endif //MULTICAMERA_LOGGER_H
