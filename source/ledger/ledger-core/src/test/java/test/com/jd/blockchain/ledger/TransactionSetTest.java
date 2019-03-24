package test.com.jd.blockchain.ledger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyFactory;
import java.util.Random;

import com.jd.blockchain.binaryproto.DataContractRegistry;
import org.junit.Test;

import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeyGenerator;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.ledger.ContractCodeDeployOperation;
import com.jd.blockchain.ledger.ContractEventSendOperation;
import com.jd.blockchain.ledger.CryptoSetting;
import com.jd.blockchain.ledger.DataAccountKVSetOperation;
import com.jd.blockchain.ledger.DataAccountKVSetOperation.KVWriteEntry;
import com.jd.blockchain.ledger.DataAccountRegisterOperation;
import com.jd.blockchain.ledger.DigitalSignature;
import com.jd.blockchain.ledger.LedgerTransaction;
import com.jd.blockchain.ledger.Operation;
import com.jd.blockchain.ledger.TransactionBuilder;
import com.jd.blockchain.ledger.TransactionRequest;
import com.jd.blockchain.ledger.TransactionRequestBuilder;
import com.jd.blockchain.ledger.TransactionState;
import com.jd.blockchain.ledger.UserRegisterOperation;
import com.jd.blockchain.ledger.core.TransactionSet;
import com.jd.blockchain.ledger.core.impl.LedgerTransactionData;
import com.jd.blockchain.ledger.core.impl.TransactionStagedSnapshot;
import com.jd.blockchain.ledger.data.TxBuilder;
import com.jd.blockchain.storage.service.utils.MemoryKVStorage;
import com.jd.blockchain.utils.io.BytesUtils;

public class TransactionSetTest {

	private static final String keyPrefix = "";

	private Random rand = new Random();

	@Test
	public void test() {
		DataContractRegistry.register(UserRegisterOperation.class);
		DataContractRegistry.register(DataAccountRegisterOperation.class);
		DataContractRegistry.register(DataAccountKVSetOperation.class);
		DataContractRegistry.register(ContractCodeDeployOperation.class);
		DataContractRegistry.register(ContractEventSendOperation.class);
		CryptoSetting defCryptoSetting = LedgerTestUtils.createDefaultCryptoSetting();

		MemoryKVStorage testStorage = new MemoryKVStorage();

		// Create a new TransactionSet, it's empty;
		TransactionSet txset = new TransactionSet(defCryptoSetting, keyPrefix, testStorage, testStorage);
		assertFalse(txset.isUpdated());
		assertFalse(txset.isReadonly());
		assertNull(txset.getRootHash());

		// Build transaction request;
		HashDigest ledgerHash = LedgerTestUtils.generateRandomHash();
		TransactionBuilder txBuilder = new TxBuilder(ledgerHash);

		BlockchainKeyPair userKey = BlockchainKeyGenerator.getInstance().generate();
		UserRegisterOperation userRegOp = txBuilder.users().register(userKey.getIdentity());

		BlockchainKeyPair dataKey = BlockchainKeyGenerator.getInstance().generate();
		DataAccountRegisterOperation dataAccRegOp = txBuilder.dataAccounts().register(dataKey.getIdentity());

		DataAccountKVSetOperation kvsetOP = txBuilder.dataAccount(dataKey.getAddress())
				.set("A", "Value_A_0".getBytes(), -1).set("B", "Value_B_0".getBytes(), -1).getOperation();

		byte[] chainCode = new byte[128];
		rand.nextBytes(chainCode);
		BlockchainKeyPair contractKey = BlockchainKeyGenerator.getInstance().generate();
		ContractCodeDeployOperation contractDplOP = txBuilder.contracts().deploy(contractKey.getIdentity(), chainCode);

		ContractEventSendOperation contractEvtSendOP = txBuilder.contractEvents().send(contractKey.getAddress(), "test",
				"TestContractArgs".getBytes());

		TransactionRequestBuilder txReqBuilder = txBuilder.prepareRequest();

		BlockchainKeyPair sponsorKey = BlockchainKeyGenerator.getInstance().generate();
		txReqBuilder.signAsEndpoint(sponsorKey);
		BlockchainKeyPair gatewayKey = BlockchainKeyGenerator.getInstance().generate();
		txReqBuilder.signAsNode(gatewayKey);

		TransactionRequest txReq = txReqBuilder.buildRequest();

		TransactionStagedSnapshot txSnapshot = new TransactionStagedSnapshot();
		HashDigest adminAccountHash = LedgerTestUtils.generateRandomHash();
		txSnapshot.setAdminAccountHash(adminAccountHash);
		HashDigest userAccountSetHash = LedgerTestUtils.generateRandomHash();
		txSnapshot.setUserAccountSetHash(userAccountSetHash);
		HashDigest dataAccountSetHash = LedgerTestUtils.generateRandomHash();
		txSnapshot.setDataAccountSetHash(dataAccountSetHash);
		HashDigest contractAccountSetHash = LedgerTestUtils.generateRandomHash();
		txSnapshot.setContractAccountSetHash(contractAccountSetHash);

		long blockHeight = 8922L;
		LedgerTransactionData tx = new LedgerTransactionData(blockHeight, txReq, TransactionState.SUCCESS, txSnapshot);
		txset.add(tx);

		assertTrue(txset.isUpdated());

		txset.commit();
		HashDigest txsetRootHash = txset.getRootHash();
		assertNotNull(txsetRootHash);
		assertEquals(1, txset.getTotalCount());
		assertEquals(blockHeight, tx.getBlockHeight());
		assertEquals(ledgerHash, tx.getTransactionContent().getLedgerHash());
		assertEquals(5, tx.getTransactionContent().getOperations().length);

		// Reload ;
		TransactionSet reloadTxset = new TransactionSet(txsetRootHash, defCryptoSetting, keyPrefix, testStorage,
				testStorage, true);

		assertEquals(1, reloadTxset.getTotalCount());

		HashDigest txCtnHash = txReq.getTransactionContent().getHash();
		LedgerTransaction reloadTx = reloadTxset.get(txCtnHash);
		assertNotNull(reloadTx);

		assertEquals(tx.getHash(), reloadTx.getHash());
		assertEquals(tx.getBlockHeight(), reloadTx.getBlockHeight());
		assertEquals(tx.getAdminAccountHash(), reloadTx.getAdminAccountHash());
		assertEquals(tx.getContractAccountSetHash(), reloadTx.getContractAccountSetHash());
		assertEquals(tx.getDataAccountSetHash(), reloadTx.getDataAccountSetHash());
		assertEquals(tx.getUserAccountSetHash(), reloadTx.getUserAccountSetHash());
		assertEquals(TransactionState.SUCCESS, reloadTx.getExecutionState());

		DigitalSignature[] expEndpointSignatures = tx.getEndpointSignatures();
		DigitalSignature[] actualEndpointSignatures = reloadTx.getEndpointSignatures();
		assertEquals(expEndpointSignatures.length, actualEndpointSignatures.length);
		for (int i = 0; i < actualEndpointSignatures.length; i++) {
			assertEquals(expEndpointSignatures[i].getPubKey(), actualEndpointSignatures[i].getPubKey());
			assertEquals(expEndpointSignatures[i].getDigest(), actualEndpointSignatures[i].getDigest());
		}

		DigitalSignature[] expNodeSignatures = tx.getNodeSignatures();
		DigitalSignature[] actualNodeSignatures = reloadTx.getNodeSignatures();
		assertEquals(expNodeSignatures.length, actualNodeSignatures.length);
		for (int i = 0; i < actualNodeSignatures.length; i++) {
			assertEquals(expNodeSignatures[i].getPubKey(), actualNodeSignatures[i].getPubKey());
			assertEquals(expNodeSignatures[i].getDigest(), actualNodeSignatures[i].getDigest());
		}

		Operation[] expOperations = tx.getTransactionContent().getOperations();
		Operation[] actualOperations = reloadTx.getTransactionContent().getOperations();
		assertEquals(expOperations.length, actualOperations.length);
		assertEquals(5, actualOperations.length);
		assertTrue(actualOperations[0] instanceof UserRegisterOperation);
		assertTrue(actualOperations[1] instanceof DataAccountRegisterOperation);
		assertTrue(actualOperations[2] instanceof DataAccountKVSetOperation);
		assertTrue(actualOperations[3] instanceof ContractCodeDeployOperation);
		assertTrue(actualOperations[4] instanceof ContractEventSendOperation);

		UserRegisterOperation actualUserRegOp = (UserRegisterOperation) actualOperations[0];
		assertEquals(userRegOp.getUserID().getAddress(), actualUserRegOp.getUserID().getAddress());
		assertEquals(userRegOp.getUserID().getPubKey(), actualUserRegOp.getUserID().getPubKey());

		DataAccountRegisterOperation actualDataAccRegOp = (DataAccountRegisterOperation) actualOperations[1];
		assertEquals(dataAccRegOp.getAccountID().getAddress(), actualDataAccRegOp.getAccountID().getAddress());
		assertEquals(dataAccRegOp.getAccountID().getPubKey(), actualDataAccRegOp.getAccountID().getPubKey());

		DataAccountKVSetOperation actualKVSetOp = (DataAccountKVSetOperation) actualOperations[2];
		assertEquals(kvsetOP.getAccountAddress(), actualKVSetOp.getAccountAddress());

		KVWriteEntry[] expKVWriteSet = kvsetOP.getWriteSet();
		KVWriteEntry[] acutualKVWriteSet = actualKVSetOp.getWriteSet();
		assertEquals(expKVWriteSet.length, acutualKVWriteSet.length);
		for (int i = 0; i < acutualKVWriteSet.length; i++) {
			assertEquals(expKVWriteSet[i].getKey(), acutualKVWriteSet[i].getKey());
			assertEquals(expKVWriteSet[i].getExpectedVersion(), acutualKVWriteSet[i].getExpectedVersion());
			assertTrue(BytesUtils.equals(expKVWriteSet[i].getValue().getValue().toBytes(), acutualKVWriteSet[i].getValue().getValue().toBytes()));
		}

		ContractCodeDeployOperation actualContractDplOp = (ContractCodeDeployOperation) actualOperations[3];
		assertEquals(contractDplOP.getContractID().getAddress(), actualContractDplOp.getContractID().getAddress());
		assertEquals(contractDplOP.getContractID().getPubKey(), actualContractDplOp.getContractID().getPubKey());
		assertTrue(BytesUtils.equals(contractDplOP.getChainCode(), actualContractDplOp.getChainCode()));

		ContractEventSendOperation actualContractEvtSendOp = (ContractEventSendOperation) actualOperations[4];
		assertEquals(contractEvtSendOP.getContractAddress(), actualContractEvtSendOp.getContractAddress());
		assertEquals(contractEvtSendOP.getEvent(), actualContractEvtSendOp.getEvent());
		assertEquals("test", actualContractEvtSendOp.getEvent());
		assertTrue(BytesUtils.equals(contractEvtSendOP.getArgs(), actualContractEvtSendOp.getArgs()));
		assertTrue(BytesUtils.equals("TestContractArgs".getBytes(), actualContractEvtSendOp.getArgs()));
	}

}
