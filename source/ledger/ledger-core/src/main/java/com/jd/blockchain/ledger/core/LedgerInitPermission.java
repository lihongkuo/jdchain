package com.jd.blockchain.ledger.core;

import com.jd.blockchain.base.data.TypeCodes;
import com.jd.blockchain.binaryproto.DataContract;
import com.jd.blockchain.binaryproto.DataField;
import com.jd.blockchain.crypto.asymmetric.SignatureDigest;
import com.jd.blockchain.ledger.LedgerInitOperation;
import com.jd.blockchain.utils.ValueType;

/**
 * 账本初始化许可；
 * 
 * @author huanghaiquan
 *
 */
@DataContract(code = TypeCodes.METADATA_INIT_PERMISSION)
public interface LedgerInitPermission {

	/**
	 * 做出许可的参与方 ID；
	 * 
	 * @return
	 */
	@DataField(order = 1, primitiveType = ValueType.INT32)
	int getParticipantId();

	/**
	 * 参数方对初始化交易的签名；
	 * 
	 * <p>
	 * 
	 * 初始化交易即账本的第一个交易，需要满足如下规则： <br>
	 * 1、不指定账本 hash； <br>
	 * 2、交易的第一个操作是({@link LedgerInitOperation}) ；<br>
	 * 3、后续的操作是按照参与者列表({@link LedgerInitOperation#getInitSetting()})的顺序依次注册相应的初始用户的操作；
	 * 
	 * <p>
	 * 此签名将作为交易的节点签名，按照参与者列表的顺序加入到初始化交易的节点签名列表；
	 * 
	 * @return
	 */
	@DataField(order = 2, primitiveType = ValueType.BYTES)
	SignatureDigest getTransactionSignature();

}
