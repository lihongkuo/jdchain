package test.com.jd.blockchain.intgr;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.jd.blockchain.crypto.AddressEncoding;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.asymmetric.CryptoKeyPair;
import com.jd.blockchain.crypto.asymmetric.PrivKey;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.gateway.GatewayConfigProperties.KeyPairConfig;
import com.jd.blockchain.ledger.BlockchainKeyGenerator;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.ledger.DataAccountKVSetOperation;
import com.jd.blockchain.ledger.KVDataEntry;
import com.jd.blockchain.ledger.LedgerBlock;
import com.jd.blockchain.ledger.LedgerInfo;
import com.jd.blockchain.ledger.PreparedTransaction;
import com.jd.blockchain.ledger.TransactionResponse;
import com.jd.blockchain.ledger.TransactionState;
import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.ledger.core.DataAccount;
import com.jd.blockchain.ledger.core.DataAccountSet;
import com.jd.blockchain.ledger.core.LedgerManage;
import com.jd.blockchain.ledger.core.LedgerRepository;
import com.jd.blockchain.ledger.core.impl.LedgerManager;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.jd.blockchain.storage.service.DbConnection;
import com.jd.blockchain.storage.service.DbConnectionFactory;
import com.jd.blockchain.tools.initializer.LedgerBindingConfig;
import com.jd.blockchain.tools.initializer.LedgerInitProperties;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.codec.HexUtils;
import com.jd.blockchain.utils.concurrent.ThreadInvoker.AsyncCallback;
import com.jd.blockchain.utils.net.NetworkAddress;

import test.com.jd.blockchain.intgr.initializer.LedgerInitializeWeb4SingleStepsTest;

public class IntegrationTestAll4Redis {


	public static final String PASSWORD = "abc";

	public static final String[] PUB_KEYS = { "endPsK36imXrY66pru6ttZ8dZ3TynWekmdqoM1K7ZRRoRBBiYVzM",
			"endPsK36jQE1uYpdVRSnwQXVYhgAMWTaMJiAqii7URiULoBDLUUN",
			"endPsK36fc7FSecKAJCJdFhTejbPHMLaGcihJVQCv95czCq4tW5n",
			"endPsK36m1grx8mkTMgh8XQHiiaNzajdC5hkuqP6pAuLmMbYkzd4" };

	public static final String[] PRIV_KEYS = {
			"177gjsuHdbf3PU68Sm1ZU2aMcyB7sLWj94xwBUoUKvTgHq7qGUfg6ynDB62hocYYXSRXD4X",
			"177gjwQwTdXthkutDKVgKwiq6wWfLWYuxhji1U2N1C5MzqLRWCLZXo3i2g4vpfcEAQUPG8H",
			"177gjvLHUjxvAWsqVcGgV8eHgVNBvJZYDfpP9FLjTouR1gEJNiamYu1qjTNDh18XWyLg8or",
			"177gk2VtYeGbK5TS2xWhbSZA4BsT9Xj5Fb8hqCzxzgbojVVcqaDSFFrFPsLbZBx7rszyCNy" };

	// batch transactions keys
	BlockchainKeyPair userKey = BlockchainKeyGenerator.getInstance().generate();
	BlockchainKeyPair dataKey = BlockchainKeyGenerator.getInstance().generate();

	// 合约测试使用的初始化数据;
	BlockchainKeyPair contractDataKey = BlockchainKeyGenerator.getInstance().generate();
	BlockchainKeyPair contractDeployKey = BlockchainKeyGenerator.getInstance().generate();
	private String contractZipName = "AssetContract1.contract";
	private String eventName = "issue-asset";
	HashDigest txContentHash;
	String pubKeyVal = "jd.com"+System.currentTimeMillis();
//	String userPubKeyVal = "this is user's pubKey";
	// 保存资产总数的键；
	private static final String KEY_TOTAL = "TOTAL";
	// 第二个参数;
	private static final String KEY_ABC = "abc";

	@Test
	public void test() {

		NetworkAddress peerSrvAddr0 = new NetworkAddress("127.0.0.1", 10200);
		LedgerBindingConfig bindingConfig0 = loadBindingConfig(0);
		PeerTestRunner peer0 = new PeerTestRunner(peerSrvAddr0, bindingConfig0);

		NetworkAddress peerSrvAddr1 = new NetworkAddress("127.0.0.1", 10210);
		LedgerBindingConfig bindingConfig1 = loadBindingConfig(1);
		PeerTestRunner peer1 = new PeerTestRunner(peerSrvAddr1, bindingConfig1);

		NetworkAddress peerSrvAddr2 = new NetworkAddress("127.0.0.1", 10220);
		LedgerBindingConfig bindingConfig2 = loadBindingConfig(2);
		PeerTestRunner peer2 = new PeerTestRunner(peerSrvAddr2, bindingConfig2);

		NetworkAddress peerSrvAddr3 = new NetworkAddress("127.0.0.1", 10230);
		LedgerBindingConfig bindingConfig3 = loadBindingConfig(3);
		PeerTestRunner peer3 = new PeerTestRunner(peerSrvAddr3, bindingConfig3);

		AsyncCallback<Object> peerStarting0 = peer0.start();
		AsyncCallback<Object> peerStarting1 = peer1.start();
		AsyncCallback<Object> peerStarting2 = peer2.start();
		AsyncCallback<Object> peerStarting3 = peer3.start();

		peerStarting0.waitReturn();
		peerStarting1.waitReturn();
		peerStarting2.waitReturn();
		peerStarting3.waitReturn();

		DbConnectionFactory dbConnectionFactory0 = peer0.getDBConnectionFactory();
		DbConnectionFactory dbConnectionFactory1 = peer1.getDBConnectionFactory();
		DbConnectionFactory dbConnectionFactory2 = peer2.getDBConnectionFactory();
		DbConnectionFactory dbConnectionFactory3 = peer3.getDBConnectionFactory();

		String encodedBase58Pwd = KeyGenCommand.encodePasswordAsBase58(LedgerInitializeWeb4SingleStepsTest.PASSWORD);

		KeyPairConfig gwkey0 = new KeyPairConfig();
		gwkey0.setPubKeyValue(PUB_KEYS[0]);
		gwkey0.setPrivKeyValue(PRIV_KEYS[0]);
		gwkey0.setPrivKeyPassword(encodedBase58Pwd);
		GatewayTestRunner gateway0 = new GatewayTestRunner("127.0.0.1", 11000, gwkey0, peerSrvAddr0);

		AsyncCallback<Object> gwStarting0 = gateway0.start();

		gwStarting0.waitReturn();

		// 执行测试用例之前，校验每个节点的一致性；
		LedgerRepository[] ledgers = buildLedgers(new LedgerBindingConfig[]{bindingConfig0, bindingConfig1, bindingConfig2, bindingConfig3},
				new DbConnectionFactory[]{dbConnectionFactory0,dbConnectionFactory1,dbConnectionFactory2,dbConnectionFactory3});
		testConsistencyAmongNodes(ledgers);

		PrivKey privkey0 = KeyGenCommand.decodePrivKeyWithRawPassword(PRIV_KEYS[0], PASSWORD);
		PrivKey privkey1 = KeyGenCommand.decodePrivKeyWithRawPassword(PRIV_KEYS[1], PASSWORD);
		PrivKey privkey2 = KeyGenCommand.decodePrivKeyWithRawPassword(PRIV_KEYS[2], PASSWORD);
		PrivKey privkey3 = KeyGenCommand.decodePrivKeyWithRawPassword(PRIV_KEYS[3], PASSWORD);

		PubKey pubKey0 = KeyGenCommand.decodePubKey(PUB_KEYS[0]);
		PubKey pubKey1 = KeyGenCommand.decodePubKey(PUB_KEYS[1]);
		PubKey pubKey2 = KeyGenCommand.decodePubKey(PUB_KEYS[2]);
		PubKey pubKey3 = KeyGenCommand.decodePubKey(PUB_KEYS[3]);

		CryptoKeyPair adminKey = new CryptoKeyPair(pubKey0,privkey0);

		testWriteBatchTransactions(gateway0, adminKey, ledgers[0]);


		testSDK(gateway0, adminKey, ledgers[0]);

		// 执行测试用例之后，校验每个节点的一致性；
		testConsistencyAmongNodes(ledgers);
	}

	private LedgerBindingConfig loadBindingConfig(int id){
		ClassPathResource res = new ClassPathResource("ledger-binding-redis-" + id + ".conf");
		try(InputStream in = res.getInputStream()){
			return LedgerBindingConfig.resolve(in);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private LedgerRepository[] buildLedgers(LedgerBindingConfig[] bindingConfigs, DbConnectionFactory[] dbConnectionFactories){
		int[] ids = {0, 1, 2, 3};
		LedgerRepository[] ledgers = new LedgerRepository[ids.length];
		LedgerManager[] ledgerManagers = new LedgerManager[ids.length];
		for (int i = 0; i < ids.length; i++) {
			ledgerManagers[i] = new LedgerManager();
			HashDigest ledgerHash = bindingConfigs[0].getLedgerHashs()[0];
			DbConnection conn = dbConnectionFactories[i].connect(bindingConfigs[i].getLedger(ledgerHash).getDbConnection().getUri(),
					bindingConfigs[i].getLedger(ledgerHash).getDbConnection().getPassword());
			ledgers[i] = ledgerManagers[i].register(ledgerHash, conn.getStorageService());
		}
		return ledgers;
	}

	private void testConsistencyAmongNodes(LedgerRepository[] ledgers) {
		LedgerRepository ledger0 = ledgers[0];
		LedgerBlock latestBlock0 = ledger0.retrieveLatestBlock();
		for (int i = 1; i < ledgers.length; i++) {
			LedgerRepository otherLedger = ledgers[i];
			LedgerBlock otherLatestBlock = otherLedger.retrieveLatestBlock();
			assertEquals(ledger0.getHash(), otherLedger.getHash());
			assertEquals(ledger0.getLatestBlockHeight(), otherLedger.getLatestBlockHeight());
			assertEquals(latestBlock0.getHeight(), otherLatestBlock.getHeight());
			assertEquals(latestBlock0.getAdminAccountHash(), otherLatestBlock.getAdminAccountHash());
			assertEquals(latestBlock0.getUserAccountSetHash(), otherLatestBlock.getUserAccountSetHash());
			assertEquals(latestBlock0.getDataAccountSetHash(), otherLatestBlock.getDataAccountSetHash());
			assertEquals(latestBlock0.getContractAccountSetHash(), otherLatestBlock.getContractAccountSetHash());
			assertEquals(latestBlock0.getPreviousHash(), otherLatestBlock.getPreviousHash());

			assertEquals(latestBlock0.getTransactionSetHash(), otherLatestBlock.getTransactionSetHash());
			assertEquals(ledger0.getLatestBlockHash(), otherLedger.getLatestBlockHash());
			assertEquals(latestBlock0.getHash(), otherLatestBlock.getHash());
		}
	}

	//测试一个区块包含多个交易的写入情况，并验证写入结果；
	private void testWriteBatchTransactions(GatewayTestRunner gateway, CryptoKeyPair adminKey,LedgerRepository ledgerRepository) {
		// 连接网关；
		GatewayServiceFactory gwsrvFact = GatewayServiceFactory.connect(gateway.getServiceAddress());
		BlockchainService blockchainService = gwsrvFact.getBlockchainService();

		HashDigest[] ledgerHashs = blockchainService.getLedgerHashs();

		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHashs[0]);

		//regist user account
		txTpl.users().register(userKey.getIdentity());

		//regist data account
		txTpl.dataAccounts().register(dataKey.getIdentity());

		//add kv ops for data account
		DataAccountKVSetOperation dataKvsetOP = txTpl.dataAccount(dataKey.getAddress())
				.set("A", "Value_A_0".getBytes(), -1)
				.set("B", "Value_B_0".getBytes(), -1)
				.set("C", "Value_C_0".getBytes(), -1)
				.set("D", "Value_D_0".getBytes(), -1).getOperation();

		// 签名；
		PreparedTransaction ptx = txTpl.prepare();
		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();

		assertTrue(txResp.isSuccess());
		assertEquals(ledgerRepository.retrieveLatestBlockHeight(), txResp.getBlockHeight());

		assertArrayEquals("Value_A_0".getBytes(), ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getBytes("A"));
		assertArrayEquals("Value_B_0".getBytes(), ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getBytes("B"));
		assertArrayEquals("Value_C_0".getBytes(), ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getBytes("C"));
		assertArrayEquals("Value_D_0".getBytes(), ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getBytes("D"));
		assertEquals(0, ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getDataVersion("A"));
		assertEquals(0, ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getDataVersion("B"));
		assertEquals(0, ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getDataVersion("C"));
		assertEquals(0, ledgerRepository.getDataAccountSet(ledgerRepository.retrieveLatestBlock()).getDataAccount(dataKey.getAddress()).getDataVersion("D"));

		return;
	}


	private void testSDK(GatewayTestRunner gateway, CryptoKeyPair adminKey,LedgerRepository ledgerRepository) {
		// 连接网关；
		GatewayServiceFactory gwsrvFact = GatewayServiceFactory.connect(gateway.getServiceAddress());
		BlockchainService bcsrv = gwsrvFact.getBlockchainService();

		HashDigest[] ledgerHashs = bcsrv.getLedgerHashs();
		BlockchainKeyPair newUserAcount = testSDK_RegisterUser(adminKey, ledgerHashs[0], bcsrv, ledgerRepository);
		BlockchainKeyPair newDataAccount = testSDK_RegisterDataAccount(adminKey, ledgerHashs[0], bcsrv, ledgerRepository);
		testSDK_InsertData(adminKey, ledgerHashs[0], bcsrv, newDataAccount.getAddress(), ledgerRepository);
		LedgerBlock latestBlock = testSDK_Contract(adminKey, ledgerHashs[0], bcsrv, ledgerRepository);

	}

	private void testSDK_InsertData(CryptoKeyPair adminKey, HashDigest ledgerHash, BlockchainService blockchainService,
			Bytes dataAccountAddress, LedgerRepository ledgerRepository) {

		// 在本地定义注册账号的 TX；
		TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);

		// --------------------------------------
		// 将商品信息写入到指定的账户中；
		// 对象将被序列化为 JSON 形式存储，并基于 JSON 结构建立查询索引；
		Bytes dataAccount = dataAccountAddress;

		String dataKey = "jingdong" + new Random().nextInt(100000);
		byte[] dataVal = "www.jd.com".getBytes();

		txTemp.dataAccount(dataAccount).set(dataKey, dataVal, -1);

		// TX 准备就绪；
		PreparedTransaction prepTx = txTemp.prepare();

		// 使用私钥进行签名；
		prepTx.sign(adminKey);

		// 提交交易；
		TransactionResponse txResp = prepTx.commit();

		ledgerRepository.retrieveLatestBlock(); // 更新内存

		// 先验证应答
		assertEquals(TransactionState.SUCCESS, txResp.getExecutionState());
		assertEquals(txResp.getBlockHeight(), ledgerRepository.getLatestBlockHeight());
		assertEquals(txResp.getContentHash(), prepTx.getHash());
		assertEquals(txResp.getBlockHash(), ledgerRepository.getLatestBlockHash());

		KVDataEntry[] kvDataEntries = blockchainService.getDataEntries(ledgerHash, dataAccountAddress.toString(), dataKey);
		for (KVDataEntry kvDataEntry : kvDataEntries) {
			assertEquals(dataKey, kvDataEntry.getKey());
			String valHexText = (String) kvDataEntry.getValue();
			byte[] valBytes = HexUtils.decode(valHexText);
			String valText = new String(valBytes);
			System.out.println(valText);
		}
	}

	private BlockchainKeyPair testSDK_RegisterDataAccount(CryptoKeyPair adminKey, HashDigest ledgerHash,
														  BlockchainService blockchainService, LedgerRepository ledgerRepository) {
		// 注册数据账户，并验证最终写入；
		BlockchainKeyPair dataAccount = BlockchainKeyGenerator.getInstance().generate();

		// 定义交易；
		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
		txTpl.dataAccounts().register(dataAccount.getIdentity());

		// 签名；
		PreparedTransaction ptx = txTpl.prepare();

		HashDigest transactionHash = ptx.getHash();

		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();

		// 验证结果;
		// LedgerRepository ledgerOfNode0 =
		// node0.getLedgerManager().getLedger(ledgerHash);
		LedgerManage ledgerManager = new LedgerManager();
		long latestBlockHeight = ledgerRepository.retrieveLatestBlockHeight();

		assertEquals(txResp.getExecutionState(), TransactionState.SUCCESS);
		assertEquals(txResp.getBlockHeight(), latestBlockHeight);
		assertEquals(txResp.getContentHash(), transactionHash);
		assertEquals(txResp.getBlockHash(), ledgerRepository.getLatestBlockHash());
		assertNotNull(ledgerRepository.getDataAccountSet(ledgerRepository.getLatestBlock())
				.getDataAccount(dataAccount.getAddress()));

		return dataAccount;
	}

	private BlockchainKeyPair testSDK_RegisterUser(CryptoKeyPair adminKey, HashDigest ledgerHash,
												   BlockchainService blockchainService, LedgerRepository ledgerRepository) {
		// 注册用户，并验证最终写入；
		BlockchainKeyPair user = BlockchainKeyGenerator.getInstance().generate();

		// 定义交易；
		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
		txTpl.users().register(user.getIdentity());

		// 签名；
		PreparedTransaction ptx = txTpl.prepare();

		HashDigest transactionHash = ptx.getHash();

		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();

		// 验证结果;
		LedgerManage ledgerManager = new LedgerManager();
		assertEquals(txResp.getExecutionState(), TransactionState.SUCCESS);
		assertEquals(txResp.getBlockHeight(), ledgerRepository.getLatestBlockHeight());
		assertEquals(txResp.getContentHash(), transactionHash);
		assertEquals(txResp.getBlockHash(), ledgerRepository.getLatestBlockHash());
		assertTrue(ledgerRepository.getUserAccountSet(ledgerRepository.getLatestBlock()).contains(user.getAddress()));

		return user;
	}


	public static LedgerInitProperties loadInitSetting_integration() {
		ClassPathResource ledgerInitSettingResource = new ClassPathResource("ledger_init_test_integration.init");
		try (InputStream in = ledgerInitSettingResource.getInputStream()) {
			LedgerInitProperties setting = LedgerInitProperties.resolve(in);
			return setting;
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private LedgerBlock testSDK_Contract(CryptoKeyPair adminKey, HashDigest ledgerHash,
										 BlockchainService blockchainService,LedgerRepository ledgerRepository) {
		System.out.println("adminKey="+AddressEncoding.generateAddress(adminKey.getPubKey()));
		BlockchainKeyPair userKey = BlockchainKeyGenerator.getInstance().generate();
		System.out.println("userKey="+userKey.getAddress());
		// valid the basic data in contract;
//		prepareContractData(adminKey, ledgerHash, blockchainService,ledgerRepository);

		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
		txTpl.users().register(userKey.getIdentity());

		// 定义交易；
		// 注册数据账户，并验证最终写入；
		txTpl.dataAccounts().register(contractDataKey.getIdentity());
//		dataAccountSet.getDataAccount(dataAddress)
		DataAccount dataAccount = ledgerRepository.getDataAccountSet(ledgerRepository.getLatestBlock()).getDataAccount(contractDataKey.getAddress());

		DataAccountKVSetOperation kvsetOP = txTpl.dataAccount(contractDataKey.getAddress())
				.set("A", "Value_A_0".getBytes(), -1)
				.set("B", "Value_B_0".getBytes(), -1)
				.set(KEY_TOTAL, "total value,dataAccount".getBytes(), -1)
				.set(KEY_ABC, "abc value,dataAccount".getBytes(), -1)
				// 所有的模拟数据都在这个dataAccount中填充;
				.set("ledgerHash", ledgerHash.getRawDigest(), -1).getOperation();

		byte[] contractCode = getChainCodeBytes();
		txTpl.contracts().deploy(contractDeployKey.getIdentity(), contractCode);

		// 签名；
		PreparedTransaction ptx = txTpl.prepare();
		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();
		assertTrue(txResp.isSuccess());

		// 验证结果；
		txResp.getContentHash();

		LedgerBlock block = ledgerRepository.getBlock(txResp.getBlockHeight());
		byte[] contractCodeInDb = ledgerRepository.getContractAccountSet(block).getContract(contractDeployKey.getAddress())
				.getChainCode();
		assertArrayEquals(contractCode, contractCodeInDb);
		txContentHash = ptx.getHash();

		// execute the contract;
		testContractExe(adminKey, ledgerHash, userKey,  blockchainService, ledgerRepository);

		return block;
	}

	private void testContractExe(CryptoKeyPair adminKey, HashDigest ledgerHash, BlockchainKeyPair userKey,
								 BlockchainService blockchainService,LedgerRepository ledgerRepository) {
		LedgerInfo ledgerInfo = blockchainService.getLedger(ledgerHash);
		LedgerBlock previousBlock = blockchainService.getBlock(ledgerHash, ledgerInfo.getLatestBlockHeight() - 1);

		// 定义交易；
		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);

		txTpl.contractEvents().send(contractDeployKey.getAddress(), eventName,
				("888##abc##" + contractDataKey.getAddress() + "##" + previousBlock.getHash().toBase58() + "##"
						+ userKey.getAddress() + "##" + contractDeployKey.getAddress() + "##"
						+ txContentHash.toBase58()+"##"+pubKeyVal).getBytes());

		// 签名；
		PreparedTransaction ptx = txTpl.prepare();
		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();

		// 验证结果；
		txResp.getContentHash();

		LedgerInfo latestLedgerInfo = blockchainService.getLedger(ledgerHash);
		assertEquals(ledgerInfo.getLatestBlockHeight() + 1, latestLedgerInfo.getLatestBlockHeight());
		assertEquals(txResp.getBlockHeight(), latestLedgerInfo.getLatestBlockHeight());

		LedgerBlock backgroundLedgerBlock = ledgerRepository.retrieveLatestBlock();
		assertEquals(txResp.getBlockHeight(), backgroundLedgerBlock.getHeight());

		// 验证合约中的赋值，外部可以获得;
		DataAccountSet dataAccountSet = ledgerRepository.getDataAccountSet(backgroundLedgerBlock);
		PubKey pubKey = new PubKey(CryptoAlgorithm.ED25519, pubKeyVal.getBytes());
		Bytes dataAddress = AddressEncoding.generateAddress(pubKey);
		assertEquals(dataAddress, dataAccountSet.getDataAccount(dataAddress).getAddress());
		assertEquals("hello", new String(dataAccountSet.getDataAccount(dataAddress).getBytes(KEY_TOTAL, -1)));

		// 验证userAccount，从合约内部赋值，然后外部验证;内部定义动态key，外部不便于得到，临时屏蔽;
//		UserAccountSet userAccountSet = ledgerRepository.getUserAccountSet(backgroundLedgerBlock);
//		PubKey userPubKey = new PubKey(CryptoAlgorithm.ED25519, userPubKeyVal.getBytes());
//		String userAddress = AddressEncoding.generateAddress(userPubKey);
//		assertEquals(userAddress, userAccountSet.getUser(userAddress).getAddress());
	}

	private void prepareContractData(CryptoKeyPair adminKey, HashDigest ledgerHash,
									 BlockchainService blockchainService, LedgerRepository ledgerRepository) {

		// 定义交易；
		TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);


		// 签名；
		PreparedTransaction ptx = txTpl.prepare();
		ptx.sign(adminKey);

		// 提交并等待共识返回；
		TransactionResponse txResp = ptx.commit();
		assertTrue(txResp.isSuccess());

		// 验证结果；
		LedgerBlock block = ledgerRepository.getBlock(txResp.getBlockHeight());
		byte[] val1InDb = ledgerRepository.getDataAccountSet(block).getDataAccount(contractDataKey.getAddress())
				.getBytes("A");
		byte[] val2InDb = ledgerRepository.getDataAccountSet(block).getDataAccount(contractDataKey.getAddress())
				.getBytes(KEY_TOTAL);
		assertArrayEquals("Value_A_0".getBytes(), val1InDb);
		assertArrayEquals("total value,dataAccount".getBytes(), val2InDb);
	}

	/**
	 * 根据合约构建字节数组;
	 *
	 * @return
	 */
	private byte[] getChainCodeBytes() {
		// 构建合约的字节数组;
		byte[] contractCode = null;
		File file = null;
		InputStream input = null;
		try {
			ClassPathResource contractPath = new ClassPathResource(contractZipName);
			file = new File(contractPath.getURI());
			assertTrue("contract zip file is not exist.", file.exists() == true);
			input = new FileInputStream(file);
			// 这种暴力的读取压缩包，在class解析时有问题，所有需要改进;
			contractCode = new byte[input.available()];
			input.read(contractCode);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return contractCode;
	}
}
