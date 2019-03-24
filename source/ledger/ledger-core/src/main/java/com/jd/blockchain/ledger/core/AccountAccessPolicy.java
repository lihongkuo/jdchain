package com.jd.blockchain.ledger.core;


import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.ledger.AccountHeader;
import com.jd.blockchain.utils.Bytes;

/**
 * 账户访问策略；
 * 
 * @author huanghaiquan
 *
 */
public interface AccountAccessPolicy {

	/**
	 * Check access policy before committing the specified account; <br>
	 * 
	 * @param account
	 * @return Return true if it satisfies this policy, or false if it doesn't;
	 */
	boolean checkCommitting(AccountHeader account);

	boolean checkRegistering(Bytes address, PubKey pubKey);

}
