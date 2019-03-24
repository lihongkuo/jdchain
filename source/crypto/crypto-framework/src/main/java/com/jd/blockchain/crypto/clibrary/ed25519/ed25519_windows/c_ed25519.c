#include <stdio.h>
#include <stdlib.h>
#include "jni.h" 
#include <string.h>
#include <stdint.h>
#include "com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils.h"
#include "ed25519.h"
#include "sha512.h"
#include "ge.h"


/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    generateKeyPair
 * Signature: ([B[B)V
 */

JNIEXPORT void JNICALL _Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair(JNIEnv *env, jobject jo, jbyteArray privKey, jbyteArray pubKey)
{

	unsigned char public_key[32], private_key[64];
	ge_p3 A;
	jbyte *seed;
	jbyte *pk;
 	
	seed = (*env)->GetByteArrayElements(env, privKey, NULL);        // obtain privKey input	
	pk = (*env)->GetByteArrayElements(env, pubKey, NULL);	        // obtain pubKey input
	
	ed25519_create_seed(seed);
	
	sha512(seed, 32, private_key);
    private_key[0] &= 248;
    private_key[31] &= 63;
    private_key[31] |= 64;

    ge_scalarmult_base(&A, private_key);
    ge_p3_tobytes(pk, &A);

	
	(*env)->SetByteArrayRegion(env, privKey, 0, 32, seed);	    // set privKey from seed
	(*env)->SetByteArrayRegion(env, pubKey, 0, 32, pk);	        // set pubKey from pk
}


/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    getPubKey
 * Signature: ([B)[B
 */

JNIEXPORT jbyteArray JNICALL _Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_getPubKey(JNIEnv *env, jobject jo, jbyteArray privKey)
{
	jbyte *seed;
	jbyteArray result;
	unsigned char public_key[32], private_key[64];
	ge_p3 A;
 
	seed = (*env)->GetByteArrayElements(env, privKey, NULL);	// obtain input
	if(NULL == seed)
		return NULL;	
	
	sha512(seed, 32, private_key);
    private_key[0] &= 248;
    private_key[31] &= 63;
    private_key[31] |= 64;

    ge_scalarmult_base(&A, private_key);
    ge_p3_tobytes(public_key, &A);
	
	 
	result = (*env)->NewByteArray(env, 32);	// initiate result
	
	(*env)->SetByteArrayRegion(env, result, 0, 32, public_key);	// set result from buf
 
	(*env)->ReleaseByteArrayElements(env, privKey, seed, 0);	//release privKey and seed
 
	return result;
}


/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    sign
 * Signature: ([B[B)[B
 */

JNIEXPORT jbyteArray JNICALL _Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_sign(JNIEnv *env, jobject jo, jbyteArray msg, jbyteArray privKey, jbyteArray pubKey)
{
	unsigned char public_key[32], private_key[64];
	unsigned char signature[64];
	jbyteArray result;
	jint dataLength = 0;	
	jbyte *data;
	jbyte *seed;
	jbyte *pk;
	
	
	
	data = (*env)->GetByteArrayElements(env, msg, NULL);        // obtain msg input	
	seed = (*env)->GetByteArrayElements(env, privKey, NULL);	// obtain privKey input
	pk  = (*env)->GetByteArrayElements(env, pubKey, NULL);      // obtain pubKey input
	if(NULL == seed || NULL == data || NULL == pk)
		return NULL;	
	
	dataLength = (*env)->GetArrayLength(env, msg);

	sha512(seed, 32, private_key);
    private_key[0] &= 248;
    private_key[31] &= 63;
    private_key[31] |= 64;
	
	ed25519_sign(signature, data,(int) dataLength, pk, private_key);
	
	result = (*env)->NewByteArray(env, 64);	// initiate result
	
    (*env)->SetByteArrayRegion(env, result, 0, 64, signature);	// set result from signature
	
	(*env)->ReleaseByteArrayElements(env, msg, data, 0);	    //release msg and data
	(*env)->ReleaseByteArrayElements(env, privKey, seed, 0);	//release privKey and seed
	(*env)->ReleaseByteArrayElements(env, pubKey, pk, 0);	    //release pubKey and pk
	
	return result;
}


/*
 * Class:     com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils
 * Method:    verify
 * Signature: ([B[B[B)Z
 */

JNIEXPORT jboolean JNICALL _Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_verify(JNIEnv *env, jobject jo, jbyteArray msg, jbyteArray pubKey, jbyteArray signature)
{
	jint dataLength = 0;	
	jbyte *data;
	jbyte *pk;
	jbyte *digest;
	int result;
		
	data   = (*env)->GetByteArrayElements(env, msg, NULL);        // obtain msg input	
	pk    = (*env)->GetByteArrayElements(env, pubKey, NULL);	  // obtain pubKey input
	digest = (*env)->GetByteArrayElements(env, signature, NULL);  // obtain signature input
	
	if(NULL == pk || NULL == data || NULL == digest)
		return 0;	
	
	dataLength = (*env)->GetArrayLength(env, msg);
	
	result = ed25519_verify(digest, data, dataLength, pk);
	
	(*env)->ReleaseByteArrayElements(env, msg, data, 0);	     //release msg and data
	(*env)->ReleaseByteArrayElements(env, pubKey, pk, 0);	     //release pubKey and pk
	(*env)->ReleaseByteArrayElements(env, signature, digest, 0); //release signature and digest
	
	return result;
}