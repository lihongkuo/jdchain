package com.jd.blockchain.ledger.data;

import com.jd.blockchain.ledger.BlockchainIdentity;
import com.jd.blockchain.ledger.UserRegisterOperation;

public class UserRegisterOperationBuilderImpl implements UserRegisterOperationBuilder{

	@Override
	public UserRegisterOperation register(BlockchainIdentity userID) {
		return new UserRegisterOpTemplate(userID);
	}
	
	

}
