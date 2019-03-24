package test.com.jd.blockchain.ledger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.jd.blockchain.crypto.AddressEncoding;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeyGenerator;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.ledger.ParticipantNode;
import com.jd.blockchain.ledger.core.CryptoConfig;
import com.jd.blockchain.ledger.core.LedgerAdminAccount;
import com.jd.blockchain.ledger.core.LedgerConfiguration;
import com.jd.blockchain.ledger.core.LedgerMetadata;
import com.jd.blockchain.ledger.data.ConsensusParticipantData;
import com.jd.blockchain.ledger.data.LedgerInitSettingData;
import com.jd.blockchain.storage.service.utils.MemoryKVStorage;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.io.BytesUtils;
import com.jd.blockchain.utils.net.NetworkAddress;

public class LedgerAdminAccountTest {

	private Random rand = new Random();

	@Test
	public void test() {
		String keyPrefix = "";
		LedgerInitSettingData initSetting = new LedgerInitSettingData();
		ConsensusParticipantData[] parties = new ConsensusParticipantData[5];
		BlockchainKeyPair[] bckeys = new BlockchainKeyPair[parties.length];
		for (int i = 0; i < parties.length; i++) {
			bckeys[i] = BlockchainKeyGenerator.getInstance().generate();
			parties[i] = new ConsensusParticipantData();
			parties[i].setId(i);
			parties[i].setAddress(AddressEncoding.generateAddress(bckeys[i].getPubKey()).toBase58());
			parties[i].setHostAddress(new NetworkAddress("192.168.10." + (10 + i), 10010 + 10 * i));
			parties[i].setName("Participant[" + i + "]");
			parties[i].setPubKey(bckeys[i].getPubKey());
		}
		ConsensusParticipantData[] parties1 = Arrays.copyOf(parties, 4);
		initSetting.setConsensusParticipants(parties1);

		byte[] csSysSettingBytes = new byte[64];
		rand.nextBytes(csSysSettingBytes);
		initSetting.setConsensusSettings(new Bytes(csSysSettingBytes));
		initSetting.setConsensusProvider("consensus-provider");

		CryptoConfig cryptoSetting = new CryptoConfig();
		cryptoSetting.setAutoVerifyHash(true);
		cryptoSetting.setHashAlgorithm(CryptoAlgorithm.SHA256);
		initSetting.setCryptoSetting(cryptoSetting);

		byte[] ledgerSeed = new byte[16];
		rand.nextBytes(ledgerSeed);
		initSetting.setLedgerSeed(ledgerSeed);

		MemoryKVStorage testStorage = new MemoryKVStorage();

		// Create intance with init setting;
		LedgerAdminAccount ledgerAdminAccount = new LedgerAdminAccount(initSetting, keyPrefix, testStorage,
				testStorage);

		// New created instance is updated until being committed;
		assertTrue(ledgerAdminAccount.isUpdated());
		// Hash of account is null until being committed;
		assertNull(ledgerAdminAccount.getHash());

		LedgerMetadata meta = ledgerAdminAccount.getMetadata();
		assertNull(meta.getParticipantsHash());

		// Commit, and check the storage keys;
		ledgerAdminAccount.commit();

		// New created instance isn't updated after being committed;
		assertFalse(ledgerAdminAccount.isUpdated());
		// Hash of account isn't null after being committed;
		assertNotNull(ledgerAdminAccount.getHash());

		meta = ledgerAdminAccount.getMetadata();
		assertNotNull(meta.getParticipantsHash());

		// ----------------------
		// Reload account from storage with readonly mode, and check the integrity of
		// data;
		HashDigest adminAccHash = ledgerAdminAccount.getHash();
		LedgerAdminAccount reloadAdminAccount = new LedgerAdminAccount(adminAccHash, keyPrefix, testStorage,
				testStorage, true);

		// verify realod settings of admin account;
		verifyRealoadingSettings(reloadAdminAccount, adminAccHash, ledgerAdminAccount.getMetadata());

		// verify the consensus participant list；
		verifyReadlingParities(reloadAdminAccount, parties1);

		// It will throw exeception because of this account is readonly;
		verifyReadonlyState(reloadAdminAccount);

		// --------------
		// reload again with writing mode;
		reloadAdminAccount = new LedgerAdminAccount(adminAccHash, keyPrefix, testStorage, testStorage, false);
		LedgerConfiguration newSetting = new LedgerConfiguration(reloadAdminAccount.getPreviousSetting());
		byte[] newCsSettingBytes = new byte[64];
		rand.nextBytes(newCsSettingBytes);
		newSetting.setConsensusSetting(new Bytes(newCsSettingBytes));
		newSetting.getCryptoSetting().setAutoVerifyHash(false);
		reloadAdminAccount.setLedgerSetting(newSetting);

		reloadAdminAccount.addParticipant(parties[4]);
		reloadAdminAccount.commit();

		// record the new account hash;
		HashDigest newAccHash = reloadAdminAccount.getHash();
		LedgerMetadata newMeta = reloadAdminAccount.getMetadata();

		// load the last version of account and verify again;
		reloadAdminAccount = new LedgerAdminAccount(adminAccHash, keyPrefix, testStorage, testStorage, true);
		verifyRealoadingSettings(reloadAdminAccount, adminAccHash, ledgerAdminAccount.getMetadata());
		verifyReadlingParities(reloadAdminAccount, parties1);
		verifyReadonlyState(reloadAdminAccount);

		// load the hash of new committing;
		reloadAdminAccount = new LedgerAdminAccount(newAccHash, keyPrefix, testStorage, testStorage, true);
		verifyRealoadingSettings(reloadAdminAccount, newAccHash, newMeta);
		verifyReadlingParities(reloadAdminAccount, parties);
		verifyReadonlyState(reloadAdminAccount);

		// System.out.println("========= [LedgerAdminAccount Test] Show generated
		// storage keys... =======");
		// testStorage.printStoragedKeys();
	}

	private void verifyRealoadingSettings(LedgerAdminAccount actualAccount, HashDigest expHash,
			LedgerMetadata expMeta) {
		// 验证基本信息；
		assertFalse(actualAccount.isUpdated());
		assertTrue(actualAccount.isReadonly());

		assertEquals(expHash, actualAccount.getHash());

		// verify metadata；
		LedgerMetadata rlmeta = actualAccount.getMetadata();
		assertEquals(expMeta.getParticipantsHash(), rlmeta.getParticipantsHash());

		assertTrue(BytesUtils.equals(expMeta.getSeed(), rlmeta.getSeed()));

		assertNotNull(rlmeta.getSetting());
		assertTrue(expMeta.getSetting().getConsensusSetting().equals(rlmeta.getSetting().getConsensusSetting()));
		assertEquals(expMeta.getSetting().getConsensusProvider(), rlmeta.getSetting().getConsensusProvider());
		
		assertEquals(expMeta.getSetting().getCryptoSetting().getAutoVerifyHash(),
				rlmeta.getSetting().getCryptoSetting().getAutoVerifyHash());
		assertEquals(expMeta.getSetting().getCryptoSetting().getHashAlgorithm(),
				rlmeta.getSetting().getCryptoSetting().getHashAlgorithm());
	}

	private void verifyReadlingParities(LedgerAdminAccount actualAccount, ParticipantNode[] expParties) {
		assertEquals(expParties.length, actualAccount.getParticipantCount());
		ParticipantNode[] actualPaticipants = actualAccount.getParticipants();
		assertEquals(expParties.length, actualPaticipants.length);
		for (int i = 0; i < actualPaticipants.length; i++) {
			ParticipantNode rlParti = actualPaticipants[i];
			assertEquals(expParties[i].getAddress(), rlParti.getAddress());
			assertEquals(expParties[i].getName(), rlParti.getName());
//			assertEquals(expParties[i].getConsensusAddress(), rlParti.getConsensusAddress());
			assertEquals(expParties[i].getPubKey(), rlParti.getPubKey());
		}
	}

	private void verifyReadonlyState(LedgerAdminAccount actualAccount) {
		ConsensusParticipantData newParti = new ConsensusParticipantData();
		newParti.setId((int) actualAccount.getParticipantCount());
		newParti.setHostAddress(
				new NetworkAddress("192.168.10." + (10 + newParti.getAddress()), 10010 + 10 * newParti.getId()));
		newParti.setName("Participant[" + newParti.getAddress() + "]");

		BlockchainKeyPair newKey = BlockchainKeyGenerator.getInstance().generate();
		newParti.setPubKey(newKey.getPubKey());

		Throwable ex = null;
		try {
			actualAccount.addParticipant(newParti);
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);

		ex = null;
		try {
			LedgerConfiguration newLedgerSetting = new LedgerConfiguration(actualAccount.getSetting());
			actualAccount.setLedgerSetting(newLedgerSetting);
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
	}

}
