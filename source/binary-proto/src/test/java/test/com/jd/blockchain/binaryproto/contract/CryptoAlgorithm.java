//package test.com.jd.blockchain.binaryproto.contract;
//
//import com.jd.blockchain.binaryproto.EnumContract;
//import com.jd.blockchain.binaryproto.EnumField;
//import com.jd.blockchain.binaryproto.ValueType;
//import com.jd.blockchain.crypto.CryptoAlgorithmType;
//import my.utils.io.BytesUtils;
//
///**
// * Created by zhangshuang3 on 2018/7/30.
// */
//@EnumContract(code=0x0102)
//public enum CryptoAlgorithm {
//
//    // Hash 类；
//    // SHA_128(CryptoAlgorithmMask.HASH, (byte) 0x01, false, false),
//
//    SHA_256(CryptoAlgorithmType.HASH, (byte) 0x01, false, false),
//
//    RIPLE160(CryptoAlgorithmType.HASH, (byte) 0x02, false, false),
//
//    SM3(CryptoAlgorithmType.HASH, (byte) 0x03, false, false),
//
//    // 非对称签名/加密算法；
//
//    /**
//     * RSA 签名算法；可签名，可加密；
//     */
//    RSA(CryptoAlgorithmType.ASYMMETRIC, (byte) 0x01, true, true),
//
//    /**
//     * ED25519 签名算法；只用于签名，没有加密特性；
//     */
//    ED25519(CryptoAlgorithmType.ASYMMETRIC, (byte) 0x02, true, false),
//
//    /**
//     * ECDSA 签名算法；只用于签名，没有加密特性；？？？
//     */
//    ECDSA(CryptoAlgorithmType.ASYMMETRIC, (byte) 0x03, true, false),
//
//    /**
//     * 国密 SM2 算法；可签名，可加密；
//     */
//    SM2(CryptoAlgorithmType.ASYMMETRIC, (byte) 0x04, true, true),
//
//    // 对称加密；
//    /**
//     * AES 算法；可加密；
//     */
//    AES(CryptoAlgorithmType.SYMMETRIC, (byte) 0x01, false, true),
//
//    SM4(CryptoAlgorithmType.SYMMETRIC, (byte) 0x02, false, true),
//
//    // 随机性；
//    /**
//     * ?????  一种随机数算法，待定；
//     */
//    JAVA_SECURE(CryptoAlgorithmType.RANDOM, (byte) 0x01, false, false);
//
//    /**
//     * 密码算法的代号；<br>
//     * 注：只占16位；
//     */
//    @EnumField(type = ValueType.INT8)
//    public final byte CODE;
//
//    private final boolean signable;
//
//    private final boolean encryptable;
//
//    private CryptoAlgorithm(byte algType, byte algId, boolean signable, boolean encryptable) {
//        this.CODE = (byte) (algType | algId);
//        this.signable = signable;
//        this.encryptable = encryptable;
//    }
//
//    /**
//     * 是否属于摘要算法；
//     *
//     * @return
//     */
//    public boolean isHash() {
//        return (CODE & CryptoAlgorithmType.HASH) == CryptoAlgorithmType.HASH;
//    }
//
//    /**
//     * 是否属于非对称密码算法；
//     *
//     * @return
//     */
//    public boolean isAsymmetric() {
//        return (CODE & CryptoAlgorithmType.ASYMMETRIC) == CryptoAlgorithmType.ASYMMETRIC;
//    }
//
//    /**
//     * 是否属于对称密码算法；
//     *
//     * @return
//     */
//    public boolean isSymmetric() {
//        return (CODE & CryptoAlgorithmType.SYMMETRIC) == CryptoAlgorithmType.SYMMETRIC;
//    }
//
//    /**
//     * 是否属于随机数算法；
//     *
//     * @return
//     */
//    public boolean isRandom() {
//        return (CODE & CryptoAlgorithmType.RANDOM) == CryptoAlgorithmType.RANDOM;
//    }
//
//    /**
//     * 是否支持签名操作；
//     *
//     * @return
//     */
//    public boolean isSignable() {
//        return signable;
//    }
//
//    /**
//     * 是否支持加密操作；
//     *
//     * @return
//     */
//    public boolean isEncryptable() {
//        return encryptable;
//    }
//
//    /**
//     * 返回指定编码对应的枚举实例；<br>
//     *
//     * 如果不存在，则返回 null；
//     *
//     * @param code
//     * @return
//     */
//    public static CryptoAlgorithm valueOf(byte code) {
//        for (CryptoAlgorithm alg : CryptoAlgorithm.values()) {
//            if (alg.CODE == code) {
//                return alg;
//            }
//        }
//        throw new IllegalArgumentException("CryptoAlgorithm doesn't support enum code[" + code + "]!");
//    }
//
//    // /**
//    // * @return
//    // */
//    // public byte[] toBytes() {
//    // byte[] bytes = BytesUtils.toBytes(CODE);
//    // byte[] result = new byte[BYTES_SIZE];
//    // System.arraycopy(bytes, 2, result, 0, 2);
//    // // TODO: 只返回最后2个字节；
//    // return result;
//    // }
//}