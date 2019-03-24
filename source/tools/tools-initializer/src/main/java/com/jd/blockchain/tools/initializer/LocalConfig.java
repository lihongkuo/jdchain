package com.jd.blockchain.tools.initializer;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Properties;

import com.jd.blockchain.utils.PathUtils;
import com.jd.blockchain.utils.PropertiesUtils;
import com.jd.blockchain.utils.io.FileUtils;

public class LocalConfig {

	// 当前参与方的 id；
	public static final String LOCAL_PARTI_PUBKEY = "local.parti.pubkey";

	// 当前参与方的私钥（密文编码）；
	public static final String LOCAL_PARTI_PRIVKEY = "local.parti.privkey";

	// 当前参与方的私钥解密密钥(原始口令的一次哈希，Base58格式)，如果不设置，则启动过程中需要从控制台输入；
	public static final String LOCAL_PARTI_PWD = "local.parti.pwd";

	// 账本初始化完成后生成的"账本绑定配置文件"的输出目录；
	public static final String LEDGER_BINDING_OUT = "ledger.binding.out";

	// 账本数据库的连接字符串；
	public static final String LEDGER_DB_URI = "ledger.db.uri";

	// 账本数据库的连接口令；
	public static final String LEDGER_DB_PWD = "ledger.db.pwd";

	// 账本消息队列的地址
//	public static final String LEDGER_MQ_SERVER = "ledger.mq.server";
//
//	// 账本消息队列的主题
//	public static final String LEDGER_MQ_TOPIC = "ledger.mq.topic";

	// 共识系统的参数配置；必须参数；
	public static final String CONSENSUS_CONF = "consensus.conf";
	
	// 共识系统的参数配置；必须参数；
	public static final String CONSENSUS_SERVICE_PROVIDER = "consensus.service-provider";

	private LocalParticipantConfig local = new LocalParticipantConfig();

	private String bindingOutDir;

	private DBConnectionConfig storagedDb = new DBConnectionConfig();

//	private MQConnectionConfig handleMq = new MQConnectionConfig();

	private String consensusConfig;
	private String consensusProvider;

	public LocalParticipantConfig getLocal() {
		return local;
	}

	public void setLocal(LocalParticipantConfig local) {
		this.local = local;
	}

	public String getBindingOutDir() {
		return bindingOutDir;
	}

	public void setBindingOutDir(String bindingOutDir) {
		this.bindingOutDir = bindingOutDir;
	}

	public DBConnectionConfig getStoragedDb() {
		return storagedDb;
	}

	public void setStoragedDb(DBConnectionConfig storagedDb) {
		this.storagedDb = storagedDb;
	}

//	public MQConnectionConfig getHandleMq() {
//		return handleMq;
//	}
//
//	public void setHandleMq(MQConnectionConfig handleMq) {
//		this.handleMq = handleMq;
//	}

	public String getConsensusConfig() {
		return consensusConfig;
	}

	public void setConsensusConfig(String consensusConfig) {
		this.consensusConfig = consensusConfig;
	}

	public static LocalConfig resolve(String initSettingFile) {
		Properties props = FileUtils.readProperties(initSettingFile, "UTF-8");
		return resolve(props, initSettingFile);
	}

	public static LocalConfig resolve(InputStream in) {
		Properties props = FileUtils.readProperties(in, "UTF-8");
		return resolve(props, null);
	}

	private static LocalConfig resolve(Properties props, String initSettingFile) {

		LocalConfig conf = new LocalConfig();

		String pubKeyString = PropertiesUtils.getRequiredProperty(props, LOCAL_PARTI_PUBKEY);
		conf.local.pubKeyString = pubKeyString;

		conf.local.privKeyString = PropertiesUtils.getRequiredProperty(props, LOCAL_PARTI_PRIVKEY);
		conf.local.password = PropertiesUtils.getProperty(props, LOCAL_PARTI_PWD, false);

		conf.storagedDb.setConnectionUri(PropertiesUtils.getRequiredProperty(props, LEDGER_DB_URI));
		conf.storagedDb.setPassword(PropertiesUtils.getProperty(props, LEDGER_DB_PWD, false));

		if (initSettingFile == null) {
			conf.bindingOutDir = PropertiesUtils.getRequiredProperty(props, LEDGER_BINDING_OUT);
			conf.consensusConfig = PropertiesUtils.getRequiredProperty(props, CONSENSUS_CONF);
		} else {
			String bindingOutDir = PropertiesUtils.getRequiredProperty(props, LEDGER_BINDING_OUT);
			String consensusConfig = PropertiesUtils.getRequiredProperty(props, CONSENSUS_CONF);
			String initSettingDir = PathUtils.concatPaths(initSettingFile, "../");
			conf.bindingOutDir = absolutePath(initSettingDir, bindingOutDir);
			conf.consensusConfig = absolutePath(initSettingDir, consensusConfig);
		}

		conf.consensusProvider = PropertiesUtils.getRequiredProperty(props, CONSENSUS_SERVICE_PROVIDER);

		return conf;
	}

	private static String absolutePath(String currPath, String settingPath) {
		String absolutePath = settingPath;
		File settingFile = new File(settingPath);
		if (!settingFile.isAbsolute()) {
            absolutePath = PathUtils.concatPaths(currPath, settingPath);
		}
		return absolutePath;
	}

	private static int getInt(String strInt) {
		return Integer.parseInt(strInt.trim());
	}

	public String getConsensusProvider() {
		return consensusProvider;
	}

	public void setConsensusProvider(String consensusProvider) {
		this.consensusProvider = consensusProvider;
	}

	/**
	 * 当前参与方的本地配置信息；
	 * 
	 * @author huanghaiquan
	 *
	 */
	public static class LocalParticipantConfig {
		private String pubKeyString;

		private String privKeyString;

		private String password;

		public String getPubKeyString() {
			return pubKeyString;
		}

		public void setId(String pubKeyString) {
			this.pubKeyString = pubKeyString;
		}

		public String getPrivKeyString() {
			return privKeyString;
		}

		public void setPrivKeyString(String privKeyString) {
			this.privKeyString = privKeyString;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public static void main(String[] args) {
		String currPath = "/Users/zhanglin33/Ddisk/20mintest/0/config/init/local.conf";
//		String settingPath = "../";
		String settingPath = "bftsmart.config";
		String path = absolutePath(PathUtils.concatPaths(currPath, "../"), settingPath);
		System.out.println(path);

	}
}
