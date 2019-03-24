package com.jd.blockchain.contract;

public interface ContractServiceProvider {

	String getName();

	/**
	 * 返回合约代码执行引擎实例；
	 * 
	 * @return
	 */
	ContractEngine getEngine();
	
}
