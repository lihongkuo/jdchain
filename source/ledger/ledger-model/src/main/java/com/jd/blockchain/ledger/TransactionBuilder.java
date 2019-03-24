package com.jd.blockchain.ledger;

import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.data.ClientOperator;
import com.jd.blockchain.ledger.data.LedgerInitOperator;

/**
 * 区块链交易模板；
 * 
 * @author huanghaiquan
 *
 */
public interface TransactionBuilder extends ClientOperator, LedgerInitOperator {
	
	HashDigest getLedgerHash();

	/**
	 * 完成交易定义，并生成就绪的交易数据； <br>
	 * 
	 * 注：调用此方法后，不能再向当前对象加入更多的操作；
	 * 
	 * @return
	 */
	TransactionRequestBuilder prepareRequest();
	
	/**
	 * 生成交易内容；
	 * @return
	 */
	TransactionContent prepareContent();

}
