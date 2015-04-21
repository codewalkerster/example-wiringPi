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
#include <wiringPiI2C.h>
#include <wiringSerial.h>
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
