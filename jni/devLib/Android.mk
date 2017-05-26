# Copyright (C) 2011 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)
#NDK_PATH := /home/codewalker/projects/android-ndk-r10d
include $(CLEAR_VARS)
LOCAL_MODULE := libWiringPi
LOCAL_SRC_FILES := \
					$(LOCAL_PATH)/../wiringPi/libwiringPi.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/..
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += \
					$(NDK_PATH)/platforms/android-21/arch-arm/usr/include \
					../../wiringPi/jni

LOCAL_MODULE    := wiringPiDev
LOCAL_SRC_FILES := \
				ds1302.c maxdetect.c  piNes.c		\
				gertboard.c piFace.c			\
				lcd128x64.c lcd.c			\
				piGlow.c 
LOCAL_SHARED_LIBRARIES := libWiringPi

LOCAL_CFLAGS    += -UNDEBUG

include $(BUILD_SHARED_LIBRARY)
