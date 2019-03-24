package com.jd.blockchain.ledger;

import com.jd.blockchain.base.data.TypeCodes;
import com.jd.blockchain.binaryproto.DataContract;
import com.jd.blockchain.binaryproto.DataField;
import com.jd.blockchain.utils.ValueType;

@DataContract(code= TypeCodes.TX_OP_CONTRACT_DEPLOY)
public interface ContractCodeDeployOperation extends Operation {
	
	@DataField(order=2, refContract = true)
	BlockchainIdentity getContractID();
	
	@DataField(order=3, primitiveType=ValueType.BYTES)
	byte[] getChainCode();
	
    
    /**
     * 地址签名；
     * 
     * <br>
     * 这是合约账户身份 ({@link #getContractID()}) 使用对应的私钥对地址做出的签名；
     * <br>
     * 在注册时将校验此签名与账户地址、公钥是否相匹配，以此保证只有私钥的持有者才能注册相应的合约账户，确保合约账户的唯一性；
     * 
     * @return
     */
	@DataField(order=4, refContract = true)
    DigitalSignature getAddressSignature();
	
}
