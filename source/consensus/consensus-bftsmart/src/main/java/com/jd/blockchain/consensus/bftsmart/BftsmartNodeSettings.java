package com.jd.blockchain.consensus.bftsmart;

import com.jd.blockchain.base.data.TypeCodes;
import com.jd.blockchain.binaryproto.DataContract;
import com.jd.blockchain.binaryproto.DataField;
import com.jd.blockchain.consensus.NodeSettings;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.utils.ValueType;
import com.jd.blockchain.utils.net.NetworkAddress;

@DataContract(code = TypeCodes.CONSENSUS_BFTSMART_NODE_SETTINGS)
public interface BftsmartNodeSettings extends NodeSettings {

	/**
	 * 节点所属的参与方的区块链地址；
	 */
//	@DataField(order = 0, primitiveType = ValueType.TEXT)
//	@Override
//	String getAddress();

	/**
	 * Base58 格式的公钥；
	 * 
	 * @return
	 */
//	@DataField(order = 1, primitiveType = ValueType.BYTES)
//	PubKey getPubKey();

	/**
	 * 节点的ID；
	 * 
	 * @return
	 */
	@DataField(order = 2, primitiveType = ValueType.INT32)
	int getId();

	/**
	 * 共识协议的网络地址；
	 * 
	 * @return
	 */
	@DataField(order = 3, primitiveType = ValueType.BYTES)
	NetworkAddress getNetworkAddress();

}
