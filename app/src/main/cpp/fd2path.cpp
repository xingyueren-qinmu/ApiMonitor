//
// Created by 醒月人 on 11/17/21.
//

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
//#include "com_example_viewpager_MainActivity.h"
#define F_LEN 1024


JNIEXPORT jstring JNICALL Java_com_softsec_mobsec_dae_apimonitor_hook_apis_FileOutputStreamHook_readlink(
		JNIEnv *env, jobject thiz, jint fd) {
	char filename[F_LEN] = "temp";
	char buf[F_LEN];
	snprintf(filename, F_LEN, "/proc/%ld/fd/%d", (long) getpid(), fd);
	readlink(filename, buf, F_LEN);
	return env->NewStringUTF(buf);
}