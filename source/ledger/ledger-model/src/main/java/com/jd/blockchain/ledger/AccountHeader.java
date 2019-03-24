package com.jd.blockchain.ledger;

import com.jd.blockchain.base.data.TypeCodes;
import com.jd.blockchain.binaryproto.DataContract;
import com.jd.blockchain.binaryproto.DataField;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.ValueType;

@DataContract(code= TypeCodes.ACCOUNT_HEADER)
public interface AccountHeader {
	
	@DataField(order=1, primitiveType = ValueType.BYTES)
	Bytes getAddress();
	
	@DataField(order=2, primitiveType = ValueType.BYTES)
	PubKey getPubKey();
	
	@DataField(order=3, primitiveType = ValueType.BYTES)
	HashDigest getRootHash();
	
}
