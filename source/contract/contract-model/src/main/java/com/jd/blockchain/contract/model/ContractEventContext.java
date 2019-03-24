package com.jd.blockchain.contract.model;

import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.BlockchainIdentity;
import com.jd.blockchain.ledger.TransactionRequest;

import java.util.Set;


public interface ContractEventContext  {

	/**
	 * 当前账本哈希；
	 *
	 * @return
	 */
	HashDigest getCurrentLedgerHash();

	/**
	 * 执行合约事件的交易请求；
	 *
	 * @return
	 */
	TransactionRequest getTransactionRequest();

	/**
	 * 交易的签署人集合；
	 *
	 * @return
	 */
	Set<BlockchainIdentity> getTxSigners();

	/**
	 * 事件名称；
	 *
	 * @return
	 */
	String getEvent();

	/**
	 * 参数列表；
	 *
	 * @return
	 */
	byte[] getArgs();

	/**
	 * 账本操作上下文；
	 *
	 * @return
	 */
	LedgerContext getLedger();

	/**
	 * 合约的拥有者集合；
	 *
	 * <br>
	 * 合约的拥有者是部署合约时的签名者；
	 *
	 * @return
	 */
	Set<BlockchainIdentity> getContracOwners();

}
