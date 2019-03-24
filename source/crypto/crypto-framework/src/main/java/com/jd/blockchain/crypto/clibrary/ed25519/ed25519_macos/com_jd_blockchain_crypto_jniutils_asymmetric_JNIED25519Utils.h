/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils */

#ifndef _Included_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
#define _Included_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    generateKeyPair
 * Signature: ([B[B)V
 */
JNIEXPORT void JNICALL Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair
  (JNIEnv *, jobject, jbyteArray, jbyteArray);

/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    getPubKey
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_getPubKey
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    sign
 * Signature: ([B[B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_sign
  (JNIEnv *, jobject, jbyteArray, jbyteArray, jbyteArray);

/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    verify
 * Signature: ([B[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_verify
  (JNIEnv *, jobject, jbyteArray, jbyteArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
