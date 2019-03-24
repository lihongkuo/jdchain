package test.com.jd.blockchain.intgr;

import com.jd.blockchain.crypto.asymmetric.CryptoKeyPair;
import com.jd.blockchain.crypto.asymmetric.PrivKey;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.gateway.GatewayConfigProperties;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.ledger.core.LedgerRepository;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.jd.blockchain.storage.service.DbConnectionFactory;
import com.jd.blockchain.tools.initializer.LedgerBindingConfig;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.concurrent.ThreadInvoker;

import org.junit.Test;
import test.com.jd.blockchain.intgr.initializer.LedgerInitializeTest;
import test.com.jd.blockchain.intgr.initializer.LedgerInitializeWeb4Nodes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static test.com.jd.blockchain.intgr.IntegrationBase.*;

public class IntegrationTest4Bftsmart {

    private static final boolean isRegisterUser = true;

    private static final boolean isRegisterDataAccount = true;

    private static final boolean isWriteKv = true;

    private static final String DB_TYPE_MEM = "mem";

    private static final String DB_TYPE_REDIS = "redis";

    private static final String DB_TYPE_ROCKSDB = "rocksdb";

    @Test
    public void test4Memory() {
        test(LedgerInitConsensusConfig.bftsmartProvider, DB_TYPE_MEM, LedgerInitConsensusConfig.memConnectionStrings);
    }

    @Test
    public void test4Redis() {
//        test(LedgerInitConsensusConfig.bftsmartProvider, DB_TYPE_REDIS, LedgerInitConsensusConfig.redisConnectionStrings);
    }

    public void test(String[] providers, String dbType, String[] dbConnections) {


        final ExecutorService sendReqExecutors = Executors.newFixedThreadPool(20);


        // 内存账本初始化
        HashDigest ledgerHash = initLedger(dbConnections);

        // 启动Peer节点
        PeerTestRunner[] peerNodes = peerNodeStart(ledgerHash, dbType);

        DbConnectionFactory dbConnectionFactory0 = peerNodes[0].getDBConnectionFactory();
        DbConnectionFactory dbConnectionFactory1 = peerNodes[1].getDBConnectionFactory();
        DbConnectionFactory dbConnectionFactory2 = peerNodes[2].getDBConnectionFactory();
        DbConnectionFactory dbConnectionFactory3 = peerNodes[3].getDBConnectionFactory();

        String encodedBase58Pwd = KeyGenCommand.encodePasswordAsBase58(LedgerInitializeTest.PASSWORD);

        GatewayConfigProperties.KeyPairConfig gwkey0 = new GatewayConfigProperties.KeyPairConfig();
        gwkey0.setPubKeyValue(IntegrationBase.PUB_KEYS[0]);
        gwkey0.setPrivKeyValue(IntegrationBase.PRIV_KEYS[0]);
        gwkey0.setPrivKeyPassword(encodedBase58Pwd);
        GatewayTestRunner gateway = new GatewayTestRunner("127.0.0.1", 11000, gwkey0,
                peerNodes[0].getServiceAddress(), providers,null);

        ThreadInvoker.AsyncCallback<Object> gwStarting = gateway.start();

        gwStarting.waitReturn();

        // 执行测试用例之前，校验每个节点的一致性；
        LedgerRepository[] ledgers = buildLedgers(new LedgerBindingConfig[]{
                        peerNodes[0].getLedgerBindingConfig(),
                        peerNodes[1].getLedgerBindingConfig(),
                        peerNodes[2].getLedgerBindingConfig(),
                        peerNodes[3].getLedgerBindingConfig(),
                },
                new DbConnectionFactory[]{
                        dbConnectionFactory0,
                        dbConnectionFactory1,
                        dbConnectionFactory2,
                        dbConnectionFactory3});

        IntegrationBase.testConsistencyAmongNodes(ledgers);

        LedgerRepository ledgerRepository = ledgers[0];

        GatewayServiceFactory gwsrvFact = GatewayServiceFactory.connect(gateway.getServiceAddress());

        PrivKey privkey0 = KeyGenCommand.decodePrivKeyWithRawPassword(IntegrationBase.PRIV_KEYS[0], IntegrationBase.PASSWORD);

        PubKey pubKey0 = KeyGenCommand.decodePubKey(IntegrationBase.PUB_KEYS[0]);

        CryptoKeyPair adminKey = new CryptoKeyPair(pubKey0, privkey0);

        BlockchainService blockchainService = gwsrvFact.getBlockchainService();

        int size = 15;
        CountDownLatch countDownLatch = new CountDownLatch(size);
        if (isRegisterUser) {
            for (int i = 0; i < size; i++) {
                sendReqExecutors.execute(() -> {

                    System.out.printf(" sdk execute time = %s threadId = %s \r\n", System.currentTimeMillis(), Thread.currentThread().getId());
                    IntegrationBase.KeyPairResponse userResponse = IntegrationBase.testSDK_RegisterUser(adminKey, ledgerHash, blockchainService);

                    validKeyPair(userResponse, ledgerRepository, IntegrationBase.KeyPairType.USER);
                    countDownLatch.countDown();
                });

            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isRegisterDataAccount) {
            IntegrationBase.KeyPairResponse dataAccountResponse = IntegrationBase.testSDK_RegisterDataAccount(adminKey, ledgerHash, blockchainService);

            validKeyPair(dataAccountResponse, ledgerRepository, IntegrationBase.KeyPairType.DATAACCOUNT);

            if (isWriteKv) {
                BlockchainKeyPair da = dataAccountResponse.keyPair;
                IntegrationBase.KvResponse kvResponse = IntegrationBase.testSDK_InsertData(adminKey, ledgerHash, blockchainService, da.getAddress());
                validKvWrite(kvResponse, ledgerRepository, blockchainService);
            }
        }

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IntegrationBase.testConsistencyAmongNodes(ledgers);
    }
    private HashDigest initLedger(String[] dbConnections) {
        LedgerInitializeWeb4Nodes ledgerInit = new LedgerInitializeWeb4Nodes();
        HashDigest ledgerHash = ledgerInit.testInitWith4Nodes(LedgerInitConsensusConfig.bftsmartConfig, dbConnections);
        System.out.printf("LedgerHash = %s \r\n", ledgerHash.toBase58());
        return ledgerHash;
    }
}
