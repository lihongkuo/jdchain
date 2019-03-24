#include <stdio.h>
#include <stdlib.h>
#include "jni.h" 
#include "sha256.h"
#include "com_jd_blockchain_crypto_jniutils_hash_JNISHA256Utils.h"
/*
 * Class:     com_jd_blockchain_crypto_jniutils_hash_JNISHA256Utils
 * Method:    hash
 * Signature: ([B)[B
 */

JNIEXPORT jbyteArray JNICALL _Java_com_jd_blockchain_crypto_jniutils_hash_JNISHA256Utils_hash(JNIEnv *env, jobject jo, jbyteArray msg)
{
	jbyte *data;
	jint dataLength = 0;
	jbyteArray result;
	SHA256_CTX ctx;
	BYTE digest[SHA256_BLOCK_SIZE];
 
	data = (*env)->GetByteArrayElements(env, msg, NULL);	// obtain input
	if(NULL == data)
		return NULL;	
 
	dataLength = (*env)->GetArrayLength(env, msg);	// get length of msg
	
	sha256_init(&ctx);
	sha256_update(&ctx, (BYTE*)data, (int) dataLength);
	sha256_final(&ctx, digest);	
 
	result = (*env)->NewByteArray(env, SHA256_BLOCK_SIZE);	// initiate result
	
	(*env)->SetByteArrayRegion(env, result, 0, SHA256_BLOCK_SIZE, digest);	// set result from digest
 
	(*env)->ReleaseByteArrayElements(env, msg, data, 0);	//release msg and  data
 
	return result;
}