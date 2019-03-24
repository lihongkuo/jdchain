#include <stdio.h>
#include <stdlib.h>
#include "jni.h" 
#include <string.h>
#include <stdint.h>
#include "ripemd160.h"
#include "com_jd_blockchain_crypto_jniutils_hash_JNIRIPEMD160Utils.h"
/*
 * Class:     com_jd_blockchain_crypto_jniutils_hash_JNIRIPEMD160Utils
 * Method:    hash
 * Signature: ([B)[B
 */

JNIEXPORT jbyteArray JNICALL _Java_com_jd_blockchain_crypto_jniutils_hash_JNIRIPEMD160Utils_hash(JNIEnv *env, jobject jo, jbyteArray msg)
{
	jbyte *data;
	jint dataLength = 0;
	jbyteArray result;
	uint8_t digest[RIPEMD160_DIGEST_LENGTH];
 
	data = (*env)->GetByteArrayElements(env, msg, NULL);	// obtain input
	if(NULL == data)
		return NULL;	
 
	dataLength = (*env)->GetArrayLength(env, msg);	// get length of msg
	
	ripemd160(data, (uint32_t) dataLength, digest);
	 
	result = (*env)->NewByteArray(env, 20);	// initiate result
	
	(*env)->SetByteArrayRegion(env, result, 0, 20, digest);	// set result from digest
 
	(*env)->ReleaseByteArrayElements(env, msg, data, 0);	//release msg and  data
 
	return result;
}