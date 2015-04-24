#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>

#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOG_TAG "wpi_android"

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#include <unistd.h>
#include <string.h>
#include <time.h>

#include <wiringPi.h>
#include <lcd.h>

jint Java_com_hardkernel_wiringpi_MainActivity_analogRead(JNIEnv* env, jobject obj, jint port) {
    return analogRead(port);
}

void Java_com_hardkernel_wiringpi_MainActivity_digitalWrite(JNIEnv* env, jobject obj, jint port, jint onoff) {
    digitalWrite(port, onoff);
}

int Java_com_hardkernel_wiringpi_MainActivity_wiringPiSetupSys(JNIEnv* env, jobject obj) {
    wiringPiSetupSys();
    return 0;
}

int Java_com_hardkernel_wiringpi_MainActivity_lcdInit(JNIEnv* env, jobject obj, 
        jint rows, jint cols, jint bits,
        jint rs, jint strb,
        jint d0, jint d1, jint d2, jint d3, jint d4,
        jint d5, jint d6, jint d7)
{
    return lcdInit (rows, cols, bits, rs, strb,
            d0, d1, d2, d3, d4, d5, d6, d7);
}

void Java_com_hardkernel_wiringpi_MainActivity_lcdPosition(JNIEnv* env, jobject obj, 
        jint fd, jint x, jint y) {
    LOGI("%s", __func__);
    lcdPosition(fd, x, y);
}

void Java_com_hardkernel_wiringpi_MainActivity_lcdPutchar(JNIEnv* env, jobject obj, 
        jint fd, jchar data) {
    lcdPutchar(fd, data);
    LOGI("%c", data);
}
