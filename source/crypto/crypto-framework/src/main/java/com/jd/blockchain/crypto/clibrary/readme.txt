在整个项目中，native method 的名字是会包含package路径的，即不仅仅是JAVA_JNIED25519Utils_generateKeyPair，而是Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair
因此需要重新dll文件，详见http://blog.sina.com.cn/s/blog_b37338430101ety9.html，步骤如下：
1.先用javac命令编译JNIED25519.java，与之前方式相同，在JNIED25519.java的路径下编译即可；
2.跳转到com/的上一层目录，输入javah com.jd.blockchain.crypto.jniutils.asymmetric.JNIED25519Utils，会生成com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils.h文件；
3.将所有c_ed25519.c中的method名字都改成类似Java_com_jd_blockchain_crypto_jniutils_asymmetric_JNIED25519Utils_generateKeyPair的样子，重新编译dll文件；
4.有两种方式可以使定义native method的java文件寻找到dll文件：
(1)直接把dll文件放到jdk的bin目录下(项目文件的移植性太差)；
(2)使用String path = JNIED25519Utils.class.getClassLoader().getResource("com/jd/blockchain/crypto/jniutils/asymmetric/c_ed25519.dll").getPath()获取路径，然后使用System.load(path)来加载路径，这样java文件就可以找到dll文件了


将jni.h和jni_md.h与c代码放到一个目录下，使用""来include这些头文件,各个系统下的两个文件是不同的，因此需要在jdk的include文件下找到jni.h，如果是windows系统则在win32文件夹找到jni_md.h，如果是linux系统则在linux文件夹找到jni_md.h


在windows下编译dll文件的指令分别为
1.ed25519对应：gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_ed25519.dll
2.sha256对应：gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_sha256.dll
3.ripemd160对应：gcc -Wall  -O3 -funroll-loops  -march=native -shared *.c -o c_ripemd160.dll
在linux下编译so文件的指令分别为
1.ed25519对应：gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_ed25519.so -fPIC
2.sha256对应：gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_sha256.so -fPIC
3.ripemd160对应：gcc -Wall  -O3 -funroll-loops  -march=native *.c -shared -o libc_ripemd160.so -fPIC


根据上述指令生成的dll或so文件需要放在resouces路径下对应文件夹内


windows版本代码和linux版本代码的不同点在于jni.h、jni_md.h和c_ed25519.c（对应的就是c_sha256.c和c_ripemd160.c）


