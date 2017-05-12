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
#export NDK_PATH /home/user/android-ndk-r10d
include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(NDK_PATH)/platforms/android-21/arch-arm/usr/include

LOCAL_MODULE    := wiringPi
LOCAL_SRC_FILES := wiringPi.c						\
		wiringShift.c						\
		piHiPri.c piThread.c					\
		wiringPiSPI.c wiringPiI2C.c				\
		softPwm.c softTone.c					\
		mcp23008.c mcp23016.c mcp23017.c			\
		mcp23s08.c mcp23s17.c					\
		sr595.c							\
		pcf8574.c pcf8591.c					\
		mcp3002.c mcp3004.c mcp4802.c mcp3422.c			\
		max31855.c max5322.c					\
		sn3218.c
		
#wiringSerial.c drcSerial.c
#disabled due to errors:
#wiringSerial.c:82: error: undefined reference to 'tcgetattr'
#wiringSerial.c:84: error: undefined reference to 'cfmakeraw'
#wiringSerial.c:85: error: undefined reference to 'cfsetispeed'
#wiringSerial.c:86: error: undefined reference to 'cfsetospeed'
#wiringSerial.c:99: error: undefined reference to 'tcsetattr'
#wiringSerial.c:122: error: undefined reference to 'tcflush'
#so far something with termios

LOCAL_CFLAGS    += -UNDEBUG -DANDROID

LOCAL_LDLIBS    := -ldl -llog

include $(BUILD_SHARED_LIBRARY)
