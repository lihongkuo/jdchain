��������Ŀ�У�native method �������ǻ����package·���ģ�����������JAVA_JNIED25519Utils_generateKeyPair������Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair
�����Ҫ����dll�ļ������http://blog.sina.com.cn/s/blog_b37338430101ety9.html���������£�
1.����javac�������JNIED25519.java����֮ǰ��ʽ��ͬ����JNIED25519.java��·���±��뼴�ɣ�
2.��ת��com/����һ��Ŀ¼������javah com.jd.blockchain.crypto.jniutils.asymmetric.JNIED25519Utils��������com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils.h�ļ���
3.������c_ed25519.c�е�method���ֶ��ĳ�����Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair�����ӣ����±���dll�ļ���
4.�����ַ�ʽ����ʹ����native method��java�ļ�Ѱ�ҵ�dll�ļ���
(1)ֱ�Ӱ�dll�ļ��ŵ�jdk��binĿ¼��(��Ŀ�ļ�����ֲ��̫��)��
(2)ʹ��String path = JNIED25519Utils.class.getClassLoader().getResource("com/jd/blockchain/crypto/jniutils/asymmetric/c_ed25519.dll").getPath()��ȡ·����Ȼ��ʹ��System.load(path)������·��������java�ļ��Ϳ����ҵ�dll�ļ���


��jni.h��jni_md.h��c����ŵ�һ��Ŀ¼�£�ʹ��""��include��Щͷ�ļ�,����ϵͳ�µ������ļ��ǲ�ͬ�ģ������Ҫ��jdk��include�ļ����ҵ�jni.h�������windowsϵͳ����win32�ļ����ҵ�jni_md.h�������linuxϵͳ����linux�ļ����ҵ�jni_md.h


��windows�±���dll�ļ���ָ��ֱ�Ϊ
1.ed25519��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_ed25519.dll
2.sha256��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_sha256.dll
3.ripemd160��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_ripemd160.dll
��linux�±���so�ļ���ָ��ֱ�Ϊ
1.ed25519��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_ed25519.so -fPIC
2.sha256��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_sha256.so -fPIC
3.ripemd160��Ӧ��gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_ripemd160.so -fPIC


��������ָ�����ɵ�dll��so�ļ���Ҫ����resouces·���¶�Ӧ�ļ�����


windows�汾�����linux�汾����Ĳ�ͬ������jni.h��jni_md.h��c_ed25519.c����Ӧ�ľ���c_sha256.c��c_ripemd160.c��


