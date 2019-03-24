package com.jd.blockchain.sdk.samples;

import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.asymmetric.AsymmetricCryptography;
import com.jd.blockchain.crypto.asymmetric.CryptoKeyPair;
import com.jd.blockchain.crypto.asymmetric.SignatureFunction;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.crypto.impl.AsymmtricCryptographyImpl;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.ledger.data.CryptoKeyEncoding;
import com.jd.blockchain.sdk.BlockchainTransactionService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.jd.blockchain.utils.net.NetworkAddress;

public class SDKDemo_DataAccount {

	public static BlockchainKeyPair CLIENT_CERT = BlockchainKeyGenerator.getInstance().generate(CryptoAlgorithm.ED25519);

	public static AsymmetricCryptography asymmetricCryptography = new AsymmtricCryptographyImpl();

	/**
	 * 生成一个区块链用户，并注册到区块链；
	 */
	public static void registerDataAccount() {
		// 区块链共识域；
		String realm = "SUPPLY_CHAIN_ALLIANCE";
		// 节点地址列表；
		NetworkAddress[] peerAddrs = { new NetworkAddress("192.168.10.10", 8080),
				new NetworkAddress("192.168.10.11", 8080), new NetworkAddress("192.168.10.12", 8080),
				new NetworkAddress("192.168.10.13", 8080) };

		// 网关客户端编号；
		int gatewayId = 1001;
		// 账本地址；
		String ledgerAddress = "ffkjhkeqwiuhivnsh3298josijdocaijsda==";
		// 客户端的认证账户；
		String clientAddress = "kkjsafieweqEkadsfaslkdslkae998232jojf==";
		String privKey = "safefsd32q34vdsvs";
		// 创建服务代理；
		final String GATEWAY_IP = "127.0.0.1";
		final int GATEWAY_PORT = 80;
		final boolean SECURE = false;
		GatewayServiceFactory serviceFactory = GatewayServiceFactory.connect(GATEWAY_IP, GATEWAY_PORT, SECURE,
				CLIENT_CERT);
		BlockchainTransactionService service = serviceFactory.getBlockchainService();

		HashDigest ledgerHash = getLedgerHash();
		// 在本地定义注册账号的 TX；
		TransactionTemplate txTemp = service.newTransaction(ledgerHash);

		// --------------------------------------
		// 在本地产生要注册的账户的秘钥；
		BlockchainKeyGenerator generator = BlockchainKeyGenerator.getInstance();

		BlockchainKeyPair dataAccount = generator.generate(CryptoAlgorithm.ED25519);

		txTemp.dataAccounts().register(dataAccount.getIdentity());

		// --------------------------------------

		// TX 准备就绪；
		PreparedTransaction prepTx = txTemp.prepare();

		// 使用私钥进行签名；
		CryptoKeyPair keyPair = getSponsorKey();
		prepTx.sign(keyPair);

		// 提交交易；
		prepTx.commit();
	}

	private static HashDigest getLedgerHash() {
		// TODO Init ledger hash;
		return null;
	}

	private static CryptoKeyPair getSponsorKey() {
		SignatureFunction signatureFunction = asymmetricCryptography.getSignatureFunction(CryptoAlgorithm.ED25519);
		return signatureFunction.generateKeyPair();
	}

}
