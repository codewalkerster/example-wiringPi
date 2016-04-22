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

#define I2C_DEVICE  "/dev/i2c-1"

int pressure;
int temperature;
int humidity;

float SEALEVELPRESSURE_HPA = 1024.25;

jint Java_com_hardkernel_wiringpi_MainActivity_analogRead(JNIEnv* env, jobject obj, jint port) {
    return analogRead(port);
}

void Java_com_hardkernel_wiringpi_MainActivity_digitalWrite(JNIEnv* env, jobject obj, jint port, jint onoff) {
    digitalWrite(port, onoff);
}

jint Java_com_hardkernel_wiringpi_MainActivity_wiringPiSetupSys(JNIEnv* env, jobject obj) {
    wiringPiSetupSys();
    return 0;
}

jint Java_com_hardkernel_wiringpi_MainActivity_openWeatherBoard(JNIEnv* env, jobject obj) {
    int result = -1;
    result = si1132_begin(I2C_DEVICE);
    if (result == -1)
        return result;
    result = bme280_begin(I2C_DEVICE);
    return result;
}

void Java_com_hardkernel_wiringpi_MainActivity_closeWeatherBoard(JNIEnv* env, jobject obj) {
    bme280_end();
    si1132_end();
}

jint Java_com_hardkernel_wiringpi_MainActivity_bme280_begin(JNIEnv* env, jobject obj) {
    return bme280_begin(I2C_DEVICE);
}

void Java_com_hardkernel_wiringpi_MainActivity_bme280_end(JNIEnv* env, jobject obj) {
    bme280_end();
}

jint Java_com_hardkernel_wiringpi_MainActivity_getUVindex(JNIEnv* env, jobject obj) {
    int result = 0;
    Si1132_readUV(&result);
    return result;
}

jfloat Java_com_hardkernel_wiringpi_MainActivity_getVisible(JNIEnv* env, jobject obj) {
    float result = 0.0;
    Si1132_readVisible(&result);
    return result;
}

jfloat Java_com_hardkernel_wiringpi_MainActivity_getIR(JNIEnv* env, jobject obj) {
    float result = 0.0;
    Si1132_readIR(&result);
    return result;
}

void Java_com_hardkernel_wiringpi_MainActivity_readyData(JNIEnv* env, jobject obj) {
    bme280_read_pressure_temperature_humidity(&pressure, &temperature, &humidity);
}

jint Java_com_hardkernel_wiringpi_MainActivity_getTemperature(JNIEnv* env, jobject obj) {
    return temperature;
}

jint Java_com_hardkernel_wiringpi_MainActivity_getPressure(JNIEnv* env, jobject obj) {
    return pressure;
}

jint Java_com_hardkernel_wiringpi_MainActivity_getHumidity(JNIEnv* env, jobject obj) {
    return humidity;
}

jint Java_com_hardkernel_wiringpi_MainActivity_getAltitude(JNIEnv* env, jobject obj) {
    int result = 0;
    bme280_readAltitude(pressure, &SEALEVELPRESSURE_HPA, &result);
    LOGI("altitude = %d", result);
    return result;
}
