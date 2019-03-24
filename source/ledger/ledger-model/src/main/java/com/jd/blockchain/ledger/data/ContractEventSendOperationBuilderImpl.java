package com.jd.blockchain.ledger.data;

import com.jd.blockchain.ledger.ContractEventSendOperation;
import com.jd.blockchain.utils.Bytes;

public class ContractEventSendOperationBuilderImpl implements ContractEventSendOperationBuilder{
	

	@Override
	public ContractEventSendOperation send(String address, String event, byte[] args) {
		ContractEventSendOpTemplate op = new ContractEventSendOpTemplate(Bytes.fromBase58(address), event, args);
		return op;
	}
	
	@Override
	public ContractEventSendOperation send(Bytes address, String event, byte[] args) {
		ContractEventSendOpTemplate op = new ContractEventSendOpTemplate(address, event, args);
		return op;
	}

}
