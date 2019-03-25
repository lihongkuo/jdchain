package com.jd.blockchain;

import com.jd.blockchain.contract.model.ContractDeployExeUtil;
import com.jd.blockchain.crypto.asymmetric.PrivKey;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.codec.Base58Utils;
import com.jd.blockchain.utils.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * for contract remote deploy;
 * @goal contractDeploy
 * @phase process-sources
 * @Author zhaogw
 * @Date 2018/10/18 10:12
 */

@Mojo(name = "deploy")
public class ContractDeployMojo extends AbstractMojo {
    Logger logger = LoggerFactory.getLogger(ContractDeployMojo.class);

    @Parameter
    private File config;

    @Override
    public void execute()throws MojoFailureException {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(config);
            prop.load(input);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
            throw new MojoFailureException("io error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        int port;
        try {
            port = Integer.parseInt(prop.getProperty("port"));
        }catch (NumberFormatException e){
            logger.error(e.getMessage());
            throw new MojoFailureException("invalid port");
        }
        String host = prop.getProperty("host");
        String ledger = prop.getProperty("ledger");
        String pubKey = prop.getProperty("pubKey");
        String prvKey = prop.getProperty("prvKey");
        String password = prop.getProperty("password");
        String contractPath = prop.getProperty("contractPath");


        if(StringUtils.isEmpty(host)){
            logger.info("host can not be empty");
            return;
        }

        if(StringUtils.isEmpty(ledger)){
            logger.info("ledger can not be empty.");
            return;
        }
        if(StringUtils.isEmpty(pubKey)){
            logger.info("pubKey can not be empty.");
            return;
        }
        if(StringUtils.isEmpty(prvKey)){
            logger.info("prvKey can not be empty.");
            return;
        }
        if(StringUtils.isEmpty(contractPath)){
            logger.info("contractPath can not be empty.");
            return;
        }

       File contract = new File(contractPath);
        if (!contract.isFile()){
            logger.info("file:"+contractPath+" is not exist");
            return;
        }
        byte[] contractBytes = FileUtils.readBytes(contractPath);


        PrivKey prv = KeyGenCommand.decodePrivKeyWithRawPassword(prvKey, password);
        PubKey pub = KeyGenCommand.decodePubKey(pubKey);
        BlockchainKeyPair blockchainKeyPair = new BlockchainKeyPair(pub, prv);
        HashDigest ledgerHash = new HashDigest(Base58Utils.decode(ledger));

        StringBuffer sb = new StringBuffer();
        sb.append("host:"+ host).append(",port:"+port).append(",ledgerHash:"+ledgerHash.toBase58()).
                append(",pubKey:"+pubKey).append(",prvKey:"+prv).append(",contractPath:"+contractPath);
        logger.info(sb.toString());
        ContractDeployExeUtil.instance.deploy(host,port,ledgerHash, blockchainKeyPair, contractBytes);
    }

}


