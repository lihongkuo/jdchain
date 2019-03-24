package test.com.jd.blockchain.intgr;

import com.jd.blockchain.consensus.ConsensusProvider;
import com.jd.blockchain.consensus.ConsensusProviders;
import com.jd.blockchain.consensus.ConsensusSettings;
import com.jd.blockchain.crypto.asymmetric.CryptoKeyPair;
import com.jd.blockchain.crypto.asymmetric.PrivKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.gateway.GatewayConfigProperties.KeyPairConfig;
import com.jd.blockchain.ledger.LedgerBlock;
import com.jd.blockchain.ledger.core.LedgerRepository;
import com.jd.blockchain.tools.initializer.*;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.concurrent.ThreadInvoker.AsyncCallback;
import com.jd.blockchain.utils.net.NetworkAddress;

import org.springframework.core.io.ClassPathResource;
import test.com.jd.blockchain.intgr.IntegratedContext.Node;
import test.com.jd.blockchain.intgr.initializer.LedgerInitializeWeb4SingleStepsTest;
import test.com.jd.blockchain.intgr.initializer.LedgerInitializeWeb4SingleStepsTest.NodeWebContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IntegrationBaseTest {

    LedgerInitConsensusConfig.ConsensusConfig bftsmartConfig = LedgerInitConsensusConfig.bftsmartConfig;

	public IntegratedContext context = initLedgers(bftsmartConfig.getConfigPath(), bftsmartConfig.getProvider());
	public GatewayTestRunner gateway0;
	public GatewayTestRunner gateway1;

	public void startPeer() {
		// init ledgers of all nodes ;
		Node node0 = context.getNode(0);
		Node node1 = context.getNode(1);
		Node node2 = context.getNode(2);
		Node node3 = context.getNode(3);

		NetworkAddress peerSrvAddr0 = new NetworkAddress("127.0.0.1", 13200);
		PeerTestRunner peer0 = new PeerTestRunner(peerSrvAddr0, node0.getBindingConfig(), node0.getStorageDB());

		NetworkAddress peerSrvAddr1 = new NetworkAddress("127.0.0.1", 13210);
		PeerTestRunner peer1 = new PeerTestRunner(peerSrvAddr1, node1.getBindingConfig(), node1.getStorageDB());

		NetworkAddress peerSrvAddr2 = new NetworkAddress("127.0.0.1", 13220);
		PeerTestRunner peer2 = new PeerTestRunner(peerSrvAddr2, node2.getBindingConfig(), node2.getStorageDB());

		NetworkAddress peerSrvAddr3 = new NetworkAddress("127.0.0.1", 13230);
		PeerTestRunner peer3 = new PeerTestRunner(peerSrvAddr3, node3.getBindingConfig(), node3.getStorageDB());

		AsyncCallback<Object> peerStarting0 = peer0.start();
		AsyncCallback<Object> peerStarting1 = peer1.start();
		AsyncCallback<Object> peerStarting2 = peer2.start();
		AsyncCallback<Object> peerStarting3 = peer3.start();

		peerStarting0.waitReturn();
		peerStarting1.waitReturn();
		peerStarting2.waitReturn();
		peerStarting3.waitReturn();

		String encodedBase58Pwd = KeyGenCommand.encodePasswordAsBase58(LedgerInitializeWeb4SingleStepsTest.PASSWORD);

		KeyPairConfig gwkey0 = new KeyPairConfig();
		gwkey0.setPubKeyValue(LedgerInitializeWeb4SingleStepsTest.PUB_KEYS[0]);
		gwkey0.setPrivKeyValue(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[0]);
		gwkey0.setPrivKeyPassword(encodedBase58Pwd);
		gateway0 = new GatewayTestRunner("127.0.0.1", 13300, gwkey0, peerSrvAddr0);

		KeyPairConfig gwkey1 = new KeyPairConfig();
		gwkey1.setPubKeyValue(LedgerInitializeWeb4SingleStepsTest.PUB_KEYS[1]);
		gwkey1.setPrivKeyValue(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[1]);
		gwkey1.setPrivKeyPassword(encodedBase58Pwd);
		gateway1 = new GatewayTestRunner("127.0.0.1", 13310, gwkey1, peerSrvAddr1);

		AsyncCallback<Object> gwStarting0 = gateway0.start();
		AsyncCallback<Object> gwStarting1 = gateway1.start();

		gwStarting0.waitReturn();
		gwStarting1.waitReturn();
	}

	public void testConsistencyAmongNodes(IntegratedContext context) {
		int[] ids = context.getNodeIds();
		Node[] nodes = new Node[ids.length];
		LedgerRepository[] ledgers = new LedgerRepository[ids.length];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = context.getNode(ids[i]);
			HashDigest ledgerHash = nodes[i].getLedgerManager().getLedgerHashs()[0];
			ledgers[i] = nodes[i].getLedgerManager().getLedger(ledgerHash);
		}
		LedgerRepository ledger0 = ledgers[0];
		LedgerBlock latestBlock0 = ledger0.retrieveLatestBlock();
		for (int i = 1; i < ledgers.length; i++) {
			LedgerRepository otherLedger = ledgers[i];
			LedgerBlock otherLatestBlock = otherLedger.retrieveLatestBlock();
			assertEquals(ledger0.getHash(), otherLedger.getHash());
			assertEquals(ledger0.getLatestBlockHeight(), otherLedger.getLatestBlockHeight());
			assertEquals(ledger0.getLatestBlockHash(), otherLedger.getLatestBlockHash());

			assertEquals(latestBlock0.getHeight(), otherLatestBlock.getHeight());
			assertEquals(latestBlock0.getHash(), otherLatestBlock.getHash());
			assertEquals(latestBlock0.getAdminAccountHash(), otherLatestBlock.getAdminAccountHash());
			assertEquals(latestBlock0.getTransactionSetHash(), otherLatestBlock.getTransactionSetHash());
			assertEquals(latestBlock0.getUserAccountSetHash(), otherLatestBlock.getUserAccountSetHash());
			assertEquals(latestBlock0.getDataAccountSetHash(), otherLatestBlock.getDataAccountSetHash());
			assertEquals(latestBlock0.getContractAccountSetHash(), otherLatestBlock.getContractAccountSetHash());
			assertEquals(latestBlock0.getPreviousHash(), otherLatestBlock.getPreviousHash());
		}
	}

	private IntegratedContext initLedgers(String configPath, String providerName) {
		Prompter consolePrompter = new PresetAnswerPrompter("N"); // new ConsolePrompter();
		LedgerInitProperties initSetting = loadInitSetting_integration();
		Properties props = LedgerInitializeWeb4SingleStepsTest.loadConsensusSetting(configPath);
		ConsensusProvider csProvider = LedgerInitConsensusConfig.getConsensusProvider(providerName);
		ConsensusSettings csProps = csProvider.getSettingsFactory().getConsensusSettingsBuilder().createSettings(props);

		// 启动服务器；
		NetworkAddress initAddr0 = initSetting.getConsensusParticipant(0).getInitializerAddress();
		NodeWebContext nodeCtx0 = new NodeWebContext(0, initAddr0);

		NetworkAddress initAddr1 = initSetting.getConsensusParticipant(1).getInitializerAddress();
		NodeWebContext nodeCtx1 = new NodeWebContext(1, initAddr1);

		NetworkAddress initAddr2 = initSetting.getConsensusParticipant(2).getInitializerAddress();
		NodeWebContext nodeCtx2 = new NodeWebContext(2, initAddr2);

		NetworkAddress initAddr3 = initSetting.getConsensusParticipant(3).getInitializerAddress();
		NodeWebContext nodeCtx3 = new NodeWebContext(3, initAddr3);

		PrivKey privkey0 = KeyGenCommand.decodePrivKeyWithRawPassword(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[0],
				LedgerInitializeWeb4SingleStepsTest.PASSWORD);
		PrivKey privkey1 = KeyGenCommand.decodePrivKeyWithRawPassword(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[1],
				LedgerInitializeWeb4SingleStepsTest.PASSWORD);
		PrivKey privkey2 = KeyGenCommand.decodePrivKeyWithRawPassword(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[2],
				LedgerInitializeWeb4SingleStepsTest.PASSWORD);
		PrivKey privkey3 = KeyGenCommand.decodePrivKeyWithRawPassword(LedgerInitializeWeb4SingleStepsTest.PRIV_KEYS[3],
				LedgerInitializeWeb4SingleStepsTest.PASSWORD);

		String encodedPassword = KeyGenCommand.encodePasswordAsBase58(LedgerInitializeWeb4SingleStepsTest.PASSWORD);

		CountDownLatch quitLatch = new CountDownLatch(4);

		DBConnectionConfig testDb0 = new DBConnectionConfig();
		testDb0.setConnectionUri(LedgerInitConsensusConfig.memConnectionStrings[0]);
		LedgerBindingConfig bindingConfig0 = new LedgerBindingConfig();
		AsyncCallback<HashDigest> callback0 = nodeCtx0.startInitCommand(privkey0, encodedPassword, initSetting, csProps,
				csProvider, testDb0, consolePrompter, bindingConfig0, quitLatch);

		DBConnectionConfig testDb1 = new DBConnectionConfig();
		testDb1.setConnectionUri(LedgerInitConsensusConfig.memConnectionStrings[1]);
		LedgerBindingConfig bindingConfig1 = new LedgerBindingConfig();
		AsyncCallback<HashDigest> callback1 = nodeCtx1.startInitCommand(privkey1, encodedPassword, initSetting, csProps,
				csProvider, testDb1, consolePrompter, bindingConfig1, quitLatch);

		DBConnectionConfig testDb2 = new DBConnectionConfig();
		testDb2.setConnectionUri(LedgerInitConsensusConfig.memConnectionStrings[2]);
		LedgerBindingConfig bindingConfig2 = new LedgerBindingConfig();
		AsyncCallback<HashDigest> callback2 = nodeCtx2.startInitCommand(privkey2, encodedPassword, initSetting, csProps,
				csProvider, testDb2, consolePrompter, bindingConfig2, quitLatch);

		DBConnectionConfig testDb3 = new DBConnectionConfig();
		testDb3.setConnectionUri(LedgerInitConsensusConfig.memConnectionStrings[3]);
		LedgerBindingConfig bindingConfig3 = new LedgerBindingConfig();
		AsyncCallback<HashDigest> callback3 = nodeCtx3.startInitCommand(privkey3, encodedPassword, initSetting, csProps,
				csProvider, testDb3, consolePrompter, bindingConfig3, quitLatch);

		HashDigest ledgerHash0 = callback0.waitReturn();
		HashDigest ledgerHash1 = callback1.waitReturn();
		HashDigest ledgerHash2 = callback2.waitReturn();
		HashDigest ledgerHash3 = callback3.waitReturn();

		assertNotNull(ledgerHash0);
		assertEquals(ledgerHash0, ledgerHash1);
		assertEquals(ledgerHash0, ledgerHash2);
		assertEquals(ledgerHash0, ledgerHash3);

		LedgerRepository ledger0 = nodeCtx0.registLedger(ledgerHash0);
		LedgerRepository ledger1 = nodeCtx1.registLedger(ledgerHash1);
		LedgerRepository ledger2 = nodeCtx2.registLedger(ledgerHash2);
		LedgerRepository ledger3 = nodeCtx3.registLedger(ledgerHash3);

		assertNotNull(ledger0);
		assertNotNull(ledger1);
		assertNotNull(ledger2);
		assertNotNull(ledger3);

		IntegratedContext context = new IntegratedContext();

		Node node0 = new Node(0);
		node0.setConsensusSettings(csProps);
		node0.setLedgerManager(nodeCtx0.getLedgerManager());
		node0.setStorageDB(nodeCtx0.getStorageDB());
		node0.setPartiKeyPair(new CryptoKeyPair(initSetting.getConsensusParticipant(0).getPubKey(), privkey0));
		node0.setBindingConfig(bindingConfig0);
		context.addNode(node0);

		Node node1 = new Node(1);
		node1.setConsensusSettings(csProps);
		node1.setLedgerManager(nodeCtx1.getLedgerManager());
		node1.setStorageDB(nodeCtx1.getStorageDB());
		node1.setPartiKeyPair(new CryptoKeyPair(initSetting.getConsensusParticipant(1).getPubKey(), privkey1));
		node1.setBindingConfig(bindingConfig1);
		context.addNode(node1);

		Node node2 = new Node(2);
		node2.setConsensusSettings(csProps);
		node2.setLedgerManager(nodeCtx2.getLedgerManager());
		node2.setStorageDB(nodeCtx2.getStorageDB());
		node2.setPartiKeyPair(new CryptoKeyPair(initSetting.getConsensusParticipant(2).getPubKey(), privkey2));
		node2.setBindingConfig(bindingConfig2);
		context.addNode(node2);

		Node node3 = new Node(3);
		node3.setConsensusSettings(csProps);
		node3.setLedgerManager(nodeCtx3.getLedgerManager());
		node3.setStorageDB(nodeCtx3.getStorageDB());
		node3.setPartiKeyPair(new CryptoKeyPair(initSetting.getConsensusParticipant(3).getPubKey(), privkey3));
		node3.setBindingConfig(bindingConfig3);
		context.addNode(node3);

		nodeCtx0.closeServer();
		nodeCtx1.closeServer();
		nodeCtx2.closeServer();
		nodeCtx3.closeServer();

		return context;
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
}
