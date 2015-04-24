LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += \
    $(NDK_PATH)/platforms/android-21/arch-arm/usr/include \
    $(LOCAL_PATH)/wiringPi

LOCAL_MODULE    := wiringPi
LOCAL_SRC_FILES := \
    wiringPi/wiringPi.c \
    wiringPi/wiringShift.c \
    wiringPi/piHiPri.c \
    wiringPi/piThread.c \
    wiringPi/wiringPiSPI.c \
    wiringPi/wiringPiI2C.c \
    wiringPi/softPwm.c \
    wiringPi/softTone.c \
    wiringPi/mcp23008.c \
    wiringPi/mcp23016.c \
    wiringPi/mcp23017.c \
    wiringPi/mcp23s08.c \
    wiringPi/mcp23s17.c \
    wiringPi/sr595.c \
    wiringPi/pcf8574.c \
    wiringPi/pcf8591.c \
    wiringPi/mcp3002.c \
    wiringPi/mcp3004.c \
    wiringPi/mcp4802.c \
    wiringPi/mcp3422.c \
    wiringPi/max31855.c \
    wiringPi/max5322.c \
    wiringPi/sn3218.c

LOCAL_CFLAGS    += -UNDEBUG -DANDROID

LOCAL_LDLIBS    := -ldl -llog
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_C_INCLUDES += \
    $(NDK_PATH)/platforms/android-21/arch-arm/usr/include \
    $(LOCAL_PATH)/wiringPi

LOCAL_MODULE    := wiringPiDev
LOCAL_SRC_FILES := \
    devLib/ds1302.c \
    devLib/maxdetect.c \
    devLib/piNes.c \
    devLib/gertboard.c \
    devLib/piFace.c \
    devLib/lcd128x64.c \
    devLib/lcd.c \
    devLib/piGlow.c

LOCAL_SHARED_LIBRARIES := libwiringPi

LOCAL_LDLIBS    := -ldl -llog

LOCAL_CFLAGS    += -UNDEBUG -DANDROID
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_C_INCLUDES += \
    $(NDK_PATH)/platforms/android-21/arch-arm/usr/include \
    $(LOCAL_PATH)/wiringPi \
    $(LOCAL_PATH)/devLib/

LOCAL_MODULE    := wpi_android
LOCAL_SRC_FILES := wpi_android.c
LOCAL_LDLIBS    := -ldl -llog
LOCAL_SHARED_LIBRARIES := libwiringPi libwiringPiDev
include $(BUILD_SHARED_LIBRARY)
