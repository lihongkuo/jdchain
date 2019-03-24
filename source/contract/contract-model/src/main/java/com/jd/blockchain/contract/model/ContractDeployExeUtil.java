package com.jd.blockchain.contract.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.asymmetric.PrivKey;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.BlockchainIdentity;
import com.jd.blockchain.ledger.BlockchainIdentityData;
import com.jd.blockchain.ledger.BlockchainKeyGenerator;
import com.jd.blockchain.ledger.BlockchainKeyPair;
import com.jd.blockchain.ledger.ContractCodeDeployOperation;
import com.jd.blockchain.ledger.ContractEventSendOperation;
import com.jd.blockchain.ledger.DataAccountKVSetOperation;
import com.jd.blockchain.ledger.DataAccountRegisterOperation;
import com.jd.blockchain.ledger.EndpointRequest;
import com.jd.blockchain.ledger.NodeRequest;
import com.jd.blockchain.ledger.Operation;
import com.jd.blockchain.ledger.PreparedTransaction;
import com.jd.blockchain.ledger.TransactionContent;
import com.jd.blockchain.ledger.TransactionContentBody;
import com.jd.blockchain.ledger.TransactionRequest;
import com.jd.blockchain.ledger.TransactionResponse;
import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.ledger.UserRegisterOperation;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.codec.Base58Utils;
import com.jd.blockchain.utils.net.NetworkAddress;

/**
 * @Author zhaogw
 * @Date 2018/11/2 10:18
 */
public enum ContractDeployExeUtil {
    instance;
    private BlockchainService bcsrv;
    private Bytes contractAddress;

    public BlockchainKeyPair getKeyPair(String pubPath, String prvPath, String rawPassword){
        PubKey pub = null;
        PrivKey prv = null;
        try {
            prv = KeyGenCommand.readPrivKey(prvPath, KeyGenCommand.encodePassword(rawPassword));
            pub = KeyGenCommand.readPubKey(pubPath);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BlockchainKeyPair(pub, prv);
    }

    public PubKey getPubKey(String pubPath){
        PubKey pub = null;
        try {
            if(pubPath == null){
                BlockchainKeyPair contractKeyPair = BlockchainKeyGenerator.getInstance().generate();
                pub = contractKeyPair.getPubKey();
            }else {
                pub = KeyGenCommand.readPubKey(pubPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pub;
    }
    public byte[] getChainCode(String path){
        byte[] chainCode = null;
        File file = null;
        InputStream input = null;
        try {
            file = new File(path);
            input = new FileInputStream(file);
            chainCode = new byte[input.available()];
            input.read(chainCode);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(input!=null){
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chainCode;
    }

    private void register(){
        DataContractRegistry.register(TransactionContent.class);
        DataContractRegistry.register(TransactionContentBody.class);
        DataContractRegistry.register(TransactionRequest.class);
        DataContractRegistry.register(NodeRequest.class);
        DataContractRegistry.register(EndpointRequest.class);
        DataContractRegistry.register(TransactionResponse.class);
        DataContractRegistry.register(DataAccountKVSetOperation.class);
        DataContractRegistry.register(DataAccountKVSetOperation.KVWriteEntry.class);
        DataContractRegistry.register(Operation.class);
        DataContractRegistry.register(ContractCodeDeployOperation.class);
        DataContractRegistry.register(ContractEventSendOperation.class);
        DataContractRegistry.register(DataAccountRegisterOperation.class);
        DataContractRegistry.register(UserRegisterOperation.class);
    }

    public BlockchainService initBcsrv(String host, int port) {
        if(bcsrv!=null){
            return bcsrv;
        }
        NetworkAddress addr = new NetworkAddress(host, port);
        GatewayServiceFactory gwsrvFact = GatewayServiceFactory.connect(addr);
        bcsrv = gwsrvFact.getBlockchainService();
        return bcsrv;
    }

    public boolean deploy(HashDigest ledgerHash, BlockchainIdentity contractIdentity, BlockchainKeyPair ownerKey, byte[] chainCode){
        register();

        TransactionTemplate txTpl = bcsrv.newTransaction(ledgerHash);
        txTpl.contracts().deploy(contractIdentity, chainCode);
        PreparedTransaction ptx = txTpl.prepare();
        ptx.sign(ownerKey);
        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 验证结果；
        contractAddress = contractIdentity.getAddress();
        this.setContractAddress(contractAddress);
        System.out.println("contract's address="+contractAddress);
        return txResp.isSuccess();
    }
    public boolean deploy(String host, int port, HashDigest ledgerHash, BlockchainKeyPair ownerKey, byte[] chainCode){
        register();

        BlockchainIdentity contractIdentity = BlockchainKeyGenerator.getInstance().generate().getIdentity();
        initBcsrv(host,port);
        return deploy(ledgerHash, contractIdentity, ownerKey, chainCode);
    }

    // 根据用户指定的公钥生成合约地址
    public boolean deploy(String host, int port, String ledger,String ownerPubPath, String ownerPrvPath,
                          String ownerPassword, String chainCodePath,String pubPath){
        PubKey pubKey = getPubKey(pubPath);
        BlockchainIdentity contractIdentity = new BlockchainIdentityData(pubKey);
        byte[] chainCode = getChainCode(chainCodePath);

        BlockchainKeyPair ownerKey = getKeyPair(ownerPubPath, ownerPrvPath, ownerPassword);
        HashDigest ledgerHash = new HashDigest(Base58Utils.decode(ledger));
        initBcsrv(host,port);
        return deploy(ledgerHash, contractIdentity, ownerKey, chainCode);
    }

    public boolean exeContract(String ledger,String ownerPubPath, String ownerPrvPath,
                                String ownerPassword,String event,String contractArgs){
        BlockchainKeyPair ownerKey = getKeyPair(ownerPubPath, ownerPrvPath, ownerPassword);
        HashDigest ledgerHash = new HashDigest(Base58Utils.decode(ledger));

        // 定义交易,传输最简单的数字、字符串、提取合约中的地址;
        TransactionTemplate txTpl = bcsrv.newTransaction(ledgerHash);
        txTpl.contractEvents().send(getContractAddress(),event,contractArgs.getBytes());

        // 签名；
        PreparedTransaction ptx = txTpl.prepare();
        ptx.sign(ownerKey);

        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 验证结果；
        return txResp.isSuccess();
    }

    public Bytes getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(Bytes contractAddress) {
        this.contractAddress = contractAddress;
    }
}
