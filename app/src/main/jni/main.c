#include <jni.h>
#include <string.h>
#include <stdbool.h>

//for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>

#define TAG "NativeCodec"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)

bool isDebug(JNIEnv *env) {
    jclass cls_HelloJni = (*env)->FindClass(env, "io/vonley/mi/BuildConfig");
    jfieldID fid_HelloJNI_debug = (*env)->GetStaticFieldID(env, cls_HelloJni, "DEBUG", "Z");
    jboolean jDebug = (*env)->GetStaticBooleanField(env, cls_HelloJni, fid_HelloJNI_debug);
    //const char * buildType = (*env)->GetStringUTFChars(env, jBuildType, nullptr);
    //std::string out = (std::stringstream() << "Build type is " << buildType << ", debug says " << (jDebug ? "debug" : "not debug")).str();
    //(*env)->ReleaseStringUTFChars(jBuildType, buildType);
    return jDebug == JNI_TRUE;
}

bool isRelease(JNIEnv *env) {
    return isDebug(env) == false;
}

jstring getFlavor(JNIEnv *env) {
    jclass buildConfig = (*env)->FindClass(env, "io/vonley/mi/BuildConfig");
    jfieldID fidFlavor = (*env)->GetStaticFieldID(env, buildConfig, "FLAVOR", "Ljava/lang/String;");
    jstring flavor = (jstring) (*env)->GetStaticObjectField(env, buildConfig, fidFlavor);
    //const char * buildType = (*env)->GetStringUTFChars(env, flavor, 0);
    //(*env)->ReleaseStringUTFChars(env, flavor, buildType);
    return flavor;
}

jstring getBuildType(JNIEnv *env) {
    jclass buildConfig = (*env)->FindClass(env, "io/vonley/mi/BuildConfig");
    jfieldID fidBuildType = (*env)->GetStaticFieldID(env, buildConfig, "BUILD_TYPE",
                                                     "Ljava/lang/String;");
    jstring buildType = (jstring) (*env)->GetStaticObjectField(env, buildConfig, fidBuildType);
    //const char * buildType = (*env)->GetStringUTFChars(env, buildType, 0);
    //(*env)->ReleaseStringUTFChars(env, buildType, buildType);
    return buildType;
}

bool equals(JNIEnv *env, jstring javaString1, jstring javaString2) {
    /* Get java class String handle */
    jclass cls = (*env)->GetObjectClass(env, javaString1);
    /* Get method ID equals from String handle */
    jmethodID mID = (*env)->GetMethodID(env, cls, "equals", "(Ljava/lang/Object;)Z");
    /* Compare both methods, store in jboolean which can be case to uint8_t or bool if you're using C99*/
    jboolean equals = (*env)->CallBooleanMethod(env, javaString1, mID, javaString2);
    return equals == JNI_TRUE;
}


JNIEXPORT jstring JNICALL
Java_io_vonley_mi_MiApplication_getKey(JNIEnv *env, jobject instance) {
    jstring type = getBuildType(env);
    jstring flavor = getFlavor(env);
    if (equals(env, flavor, (*env)->NewStringUTF(env, "production"))) {
        return (*env)->NewStringUTF(env, "");
    }
    return (*env)->NewStringUTF(env, "");
}

