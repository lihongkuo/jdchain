/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: test.com.jd.blockchain.sdk.test.SDK_GateWay_InsertData_Test
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/9/4 上午11:06
 * Description: 插入数据测试
 */
package test.com.jd.blockchain.sdk.test;

import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.asymmetric.AsymmetricCryptography;
import com.jd.blockchain.crypto.asymmetric.CryptoKeyPair;
import com.jd.blockchain.crypto.asymmetric.SignatureFunction;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.crypto.impl.AsymmtricCryptographyImpl;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.ledger.data.TxResponseMessage;
import com.jd.blockchain.sdk.BlockchainTransactionService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.jd.blockchain.sdk.samples.SDKDemo_InsertData;
import com.jd.blockchain.utils.io.ByteArray;
import com.jd.blockchain.utils.net.NetworkAddress;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 插入数据测试
 * @author shaozhuguang
 * @create 2018/9/4
 * @since 1.0.0
 */

public class SDK_GateWay_InsertData_Test_ {

    private BlockchainKeyPair CLIENT_CERT = null;

    private String GATEWAY_IPADDR = null;

    private int GATEWAY_PORT;

    private boolean SECURE;

    private BlockchainTransactionService service;

    private AsymmetricCryptography asymmetricCryptography = new AsymmtricCryptographyImpl();

    @Before
    public void init() {
        CLIENT_CERT = BlockchainKeyGenerator.getInstance().generate(CryptoAlgorithm.ED25519);
        GATEWAY_IPADDR = "127.0.0.1";
        GATEWAY_PORT = 8000;
        SECURE = false;
        GatewayServiceFactory serviceFactory = GatewayServiceFactory.connect(GATEWAY_IPADDR, GATEWAY_PORT, SECURE,
                CLIENT_CERT);
        service = serviceFactory.getBlockchainService();

        DataContractRegistry.register(TransactionContent.class);
        DataContractRegistry.register(TransactionContentBody.class);
        DataContractRegistry.register(TransactionRequest.class);
        DataContractRegistry.register(NodeRequest.class);
        DataContractRegistry.register(EndpointRequest.class);
        DataContractRegistry.register(TransactionResponse.class);
    }

    @Test
    public void insertData_Test() {
        HashDigest ledgerHash = getLedgerHash();
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = service.newTransaction(ledgerHash);

        // --------------------------------------
        // 将商品信息写入到指定的账户中；
        // 对象将被序列化为 JSON 形式存储，并基于 JSON 结构建立查询索引；
        String dataAccount = "GGhhreGeasdfasfUUfehf9932lkae99ds66jf==";

        String dataKey = "jd_code";
        byte[] dataVal = "www.jd.com".getBytes();

        txTemp.dataAccount(dataAccount).set(dataKey, dataVal, -1);

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();

        // 使用私钥进行签名；
        CryptoKeyPair keyPair = getSponsorKey();
        prepTx.sign(keyPair);

        // 提交交易；
        TransactionResponse transactionResponse = prepTx.commit();

        // 期望返回结果
        TransactionResponse expectResp = initResponse();

        System.out.println("---------- assert start ----------");
        assertEquals(expectResp.isSuccess(), transactionResponse.isSuccess());
        assertEquals(expectResp.getExecutionState(), transactionResponse.getExecutionState());
        assertEquals(expectResp.getContentHash(), transactionResponse.getContentHash());
        assertEquals(expectResp.getBlockHeight(), transactionResponse.getBlockHeight());
        assertEquals(expectResp.getBlockHash(), transactionResponse.getBlockHash());
        System.out.println("---------- assert OK ----------");
    }

    private HashDigest getLedgerHash() {
        HashDigest ledgerHash = new HashDigest(CryptoAlgorithm.SHA256, "jd-gateway".getBytes());
        return ledgerHash;
    }


    private CryptoKeyPair getSponsorKey() {
        SignatureFunction signatureFunction = asymmetricCryptography.getSignatureFunction(CryptoAlgorithm.ED25519);
        return signatureFunction.generateKeyPair();
	}
	
    private TransactionResponse initResponse() {
        HashDigest contentHash = new HashDigest(CryptoAlgorithm.SHA256, "contentHash".getBytes());
        HashDigest blockHash = new HashDigest(CryptoAlgorithm.SHA256, "blockHash".getBytes());
        long blockHeight = 9998L;

        TxResponseMessage resp = new TxResponseMessage(contentHash);
        resp.setBlockHash(blockHash);
        resp.setBlockHeight(blockHeight);
        resp.setExecutionState(TransactionState.SUCCESS);
        return resp;
    }
}