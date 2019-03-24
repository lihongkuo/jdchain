package test.com.jd.blockchain.ledger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.jd.blockchain.binaryproto.BinaryEncodingUtils;
import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.AddressEncoding;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.ParticipantNode;
import com.jd.blockchain.ledger.CryptoSetting;
import com.jd.blockchain.ledger.core.CryptoConfig;
import com.jd.blockchain.ledger.core.LedgerAdminAccount;
import com.jd.blockchain.ledger.core.LedgerConfiguration;
import com.jd.blockchain.ledger.core.LedgerMetadata;
import com.jd.blockchain.ledger.core.LedgerSetting;
import com.jd.blockchain.ledger.core.ParticipantCertData;
import com.jd.blockchain.utils.Bytes;

/**
 * Created by zhangshuang3 on 2018/8/31.
 */
public class LedgerMetaDataTest {
	byte[] seed = null;
	String consensusProvider = "test-provider";
	byte[] consensusSettingBytes = null;
	byte[] rawDigestBytes = null;

	@Before
	public void initCfg() throws Exception {
		Random rand = new Random();
		seed = new byte[8];
		consensusSettingBytes = new byte[8];
		rawDigestBytes = new byte[8];
		rand.nextBytes(seed);
		rand.nextBytes(consensusSettingBytes);
		rand.nextBytes(rawDigestBytes);
		DataContractRegistry.register(LedgerMetadata.class);
		DataContractRegistry.register(ParticipantNode.class);
	}

	@Test
	public void testSerialize_LedgerMetadata() {
		// LedgerCodes.METADATA

		// prepare work
		// ConsensusConfig consensusConfig = new ConsensusConfig();
		// consensusConfig.setValue(settingValue);

		CryptoConfig cryptoConfig = new CryptoConfig();
		cryptoConfig.setAutoVerifyHash(true);
		cryptoConfig.setHashAlgorithm(CryptoAlgorithm.SHA256);

		LedgerConfiguration ledgerConfiguration = new LedgerConfiguration(consensusProvider,
				new Bytes(consensusSettingBytes), cryptoConfig);
		LedgerAdminAccount.LedgerMetadataImpl ledgerMetadata = new LedgerAdminAccount.LedgerMetadataImpl();

		ledgerMetadata.setSeed(seed);
		ledgerMetadata.setSetting(ledgerConfiguration);

		HashDigest hashDigest = new HashDigest(CryptoAlgorithm.SHA256, rawDigestBytes);
		ledgerMetadata.setParticipantsHash(hashDigest);

		// encode and decode
		byte[] encodeBytes = BinaryEncodingUtils.encode(ledgerMetadata, LedgerMetadata.class);
		LedgerMetadata deLedgerMetaData = BinaryEncodingUtils.decode(encodeBytes);

		// verify start
		assertArrayEquals(ledgerMetadata.getSeed(), deLedgerMetaData.getSeed());
		assertEquals(ledgerMetadata.getParticipantsHash(), deLedgerMetaData.getParticipantsHash());
		assertNotEquals(ledgerMetadata.getSetting(), deLedgerMetaData.getSetting());

		return;
	}

	@Test
	public void testSerialize_LedgerSetting() {
		// LedgerCodes.METADATA_LEDGER_SETTING
		Random rand = new Random();
		byte[] csSettingsBytes = new byte[8];
		rand.nextBytes(csSettingsBytes);
		String consensusProvider = "testprovider";

		// ConsensusConfig consensusConfig = new ConsensusConfig();
		// consensusConfig.setValue(settingValue);

		CryptoConfig cryptoConfig = new CryptoConfig();
		cryptoConfig.setAutoVerifyHash(true);
		cryptoConfig.setHashAlgorithm(CryptoAlgorithm.SHA256);

		LedgerConfiguration ledgerConfiguration = new LedgerConfiguration(consensusProvider, new Bytes(csSettingsBytes), cryptoConfig);
		byte[] encodeBytes = BinaryEncodingUtils.encode(ledgerConfiguration, LedgerSetting.class);
		LedgerSetting deLedgerConfiguration = BinaryEncodingUtils.decode(encodeBytes);
		// verify start
		assertTrue(ledgerConfiguration.getConsensusSetting().equals(deLedgerConfiguration.getConsensusSetting()));
		assertEquals(ledgerConfiguration.getCryptoSetting().getAutoVerifyHash(),
				deLedgerConfiguration.getCryptoSetting().getAutoVerifyHash());
		assertEquals(ledgerConfiguration.getCryptoSetting().getHashAlgorithm(),
				deLedgerConfiguration.getCryptoSetting().getHashAlgorithm());

		return;
	}

	// @Test
	// public void testSerialize_ConsensusSetting() {
	// //LedgerCodes.METADATA_LEDGER_SETTING_CONSENSUS
	// Random rand = new Random();
	// byte[] settingValue = new byte[8];
	// rand.nextBytes(settingValue);
	//
	// ConsensusConfig consensusConfig = new ConsensusConfig();
	// consensusConfig.setValue(settingValue);
	// byte[] encodeBytes = BinaryEncodingUtils.encode(consensusConfig,
	// ConsensusSetting.class);
	// ConsensusSetting deConsensusConfig = BinaryEncodingUtils.decode(encodeBytes);
	//
	// //verify start
	// assertArrayEquals(consensusConfig.getValue(), deConsensusConfig.getValue());
	//
	// return;
	// }

	@Test
	public void testSerialize_CryptoSetting() {
		// LedgerCodes.METADATA_LEDGER_SETTING_CRYPTO
		CryptoConfig cryptoConfig = new CryptoConfig();
		cryptoConfig.setAutoVerifyHash(true);
		cryptoConfig.setHashAlgorithm(CryptoAlgorithm.SHA256);
		byte[] encodeBytes = BinaryEncodingUtils.encode(cryptoConfig, CryptoSetting.class);
		CryptoSetting deCryptoConfig = BinaryEncodingUtils.decode(encodeBytes);

		// verify start
		assertEquals(cryptoConfig.getHashAlgorithm(), deCryptoConfig.getHashAlgorithm());
		assertEquals(cryptoConfig.getAutoVerifyHash(), deCryptoConfig.getAutoVerifyHash());
		return;
	}

	@Test
	public void testSerialize_ParticipantCert() {
		// LedgerCodes.METADATA_PARTICIPANT_CERT
		// prepare work
		int id = 1;
		// String address = "xxxxxxxxxxxxxx";
		PubKey pubKey = new PubKey(CryptoAlgorithm.ED25519, rawDigestBytes);
		// ParticipantInfo info = new ParticipantCertData.ParticipantInfoData(1, "yyy");
		// SignatureDigest signature = new SignatureDigest(CryptoAlgorithm.SM2,
		// rawDigestBytes);
		String name = "John";
		// NetworkAddress consensusAddress = new NetworkAddress("192.168.1.1", 9001,
		// false);
		String address = AddressEncoding.generateAddress(pubKey).toBase58();
		ParticipantCertData participantCertData = new ParticipantCertData(address, name, pubKey);

		// encode and decode
		byte[] encodeBytes = BinaryEncodingUtils.encode(participantCertData, ParticipantNode.class);
		ParticipantNode deParticipantInfoData = BinaryEncodingUtils.decode(encodeBytes);

		// verify start
		assertEquals(participantCertData.getAddress(), deParticipantInfoData.getAddress());
		assertEquals(participantCertData.getPubKey(), deParticipantInfoData.getPubKey());
		assertEquals(participantCertData.getName(), deParticipantInfoData.getName());
		// assertEquals(participantCertData.getConsensusAddress().getHost(),
		// deParticipantInfoData.getConsensusAddress().getHost());
		// assertEquals(participantCertData.getConsensusAddress().getPort(),
		// deParticipantInfoData.getConsensusAddress().getPort());
		// assertEquals(participantCertData.getConsensusAddress().isSecure(),
		// deParticipantInfoData.getConsensusAddress().isSecure());

		return;
	}

	// @Test
	// public void testSerialize_ParticipantInfo() {
	// String name = "yyyy";
	//
	// ParticipantCertData.ParticipantInfoData participantInfoData = new
	// ParticipantCertData.ParticipantInfoData(1, name);
	// byte[] encodeBytes = BinaryEncodingUtils.encode(participantInfoData,
	// ParticipantInfo.class);
	// ParticipantCertData.ParticipantInfoData deParticipantInfoData =
	// BinaryEncodingUtils.decode(encodeBytes, null,
	// ParticipantCertData.ParticipantInfoData.class);
	//
	// //verify start
	// assertEquals(participantInfoData.getId(), deParticipantInfoData.getId());
	// assertEquals(participantInfoData.getName(), deParticipantInfoData.getName());
	//
	// return;
	// }
}
