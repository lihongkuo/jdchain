package com.jd.blockchain.peer.web;

import com.jd.blockchain.binaryproto.BinaryEncodingUtils;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.ledger.core.*;
import com.jd.blockchain.ledger.core.impl.LedgerQueryService;
import com.jd.blockchain.sdk.BlockchainQueryService;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.QueryUtil;
import com.jd.blockchain.utils.ValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/")
public class LedgerQueryController implements BlockchainQueryService {
	
    @Autowired
    private LedgerService ledgerService;

	@RequestMapping(method = RequestMethod.GET, path = "ledgers")
	@Override
	public HashDigest[] getLedgerHashs() {
		return ledgerService.getLedgerHashs();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}")
	@Override
	public LedgerInfo getLedger(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		//TODO: 需要配置返回值的 spring MsgQueueMessageDispatcher ，对返回对象仅仅序列化声明的返回值类型的属性，而不是整个对象本身；
		LedgerInfo ledgerInfo = new LedgerInfo();
		ledgerInfo.setHash(ledgerHash);
		ledgerInfo.setLatestBlockHash(ledger.getLatestBlockHash());
		ledgerInfo.setLatestBlockHeight(ledger.getLatestBlockHeight());
		return ledgerInfo;
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/participants")
	@Override
	public ParticipantNode[] getConsensusParticipants(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerAdministration ledgerAdministration = ledger.getAdminInfo();
		long participantCount = ledgerAdministration.getParticipantCount();
		if (participantCount <= 0) {
			return null;
		}
		ParticipantNode[] participantNodes = ledgerAdministration.getParticipants();
		// 重新封装，处理Proxy的问题
		if (participantNodes != null && participantNodes.length > 0) {
			ParticipantNode[] convertNodes = new ParticipantNode[participantNodes.length];
			for (int i = 0, length = participantNodes.length; i < length; i++) {
				convertNodes[i] = new ParticipantCertData(participantNodes[i]);
			}
			return convertNodes;
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}")
	@Override
	public LedgerBlock getBlock(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
								@PathVariable(name = "blockHeight") long blockHeight) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		//TODO: 需要配置返回值的 spring MsgQueueMessageDispatcher ，对返回对象仅仅序列化声明的返回值类型的属性，而不是整个对象本身；
		return ledger.getBlock(blockHeight);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}")
	@Override
	public LedgerBlock getBlock(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
								@PathVariable(name = "blockHash") HashDigest blockHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		//TODO: 需要配置返回值的 spring MsgQueueMessageDispatcher ，对返回对象仅仅序列化声明的返回值类型的属性，而不是整个对象本身；
		return ledger.getBlock(blockHash);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/txs/count")
	@Override
	public long getTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
									@PathVariable(name = "blockHeight") long blockHeight) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(blockHeight);
		TransactionSet txSet = ledger.getTransactionSet(block);
		return txSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/txs/count")
	@Override
	public long getTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
									@PathVariable(name = "blockHash") HashDigest blockHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(blockHash);
		TransactionSet txSet = ledger.getTransactionSet(block);
		return txSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/count")
	@Override
	public long getTransactionTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		TransactionSet txSet = ledger.getTransactionSet(block);
		return txSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/accounts/count")
	@Override
	public long getDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
									@PathVariable(name = "blockHeight") long height) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(height);
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		return dataAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/accounts/count")
	@Override
	public long getDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
									@PathVariable(name = "blockHash") HashDigest blockHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(blockHash);
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		return dataAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/count")
	@Override
	public long getDataAccountTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		return dataAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/users/count")
	@Override
	public long getUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
							 @PathVariable(name = "blockHeight") long height) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(height);
		UserAccountSet userAccountSet = ledger.getUserAccountSet(block);
		return userAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/users/count")
	@Override
	public long getUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
							 @PathVariable(name = "blockHash") HashDigest blockHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(blockHash);
		UserAccountSet userAccountSet = ledger.getUserAccountSet(block);
		return userAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users/count")
	@Override
	public long getUserTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		UserAccountSet userAccountSet = ledger.getUserAccountSet(block);
		return userAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/contracts/count")
	@Override
	public long getContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
								 @PathVariable(name = "blockHeight") long height) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(height);
		ContractAccountSet contractAccountSet = ledger.getContractAccountSet(block);
		return contractAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/contracts/count")
	@Override
	public long getContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
								 @PathVariable(name = "blockHash") HashDigest blockHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getBlock(blockHash);
		ContractAccountSet contractAccountSet = ledger.getContractAccountSet(block);
		return contractAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts/count")
	@Override
	public long getContractTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		ContractAccountSet contractAccountSet = ledger.getContractAccountSet(block);
		return contractAccountSet.getTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/txs")
	@Override
	public LedgerTransaction[] getTransactions(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
											   @PathVariable(name = "blockHeight") long blockHeight,
											   @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
											   @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {

		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock ledgerBlock = ledger.getBlock(blockHeight);
		TransactionSet transactionSet = ledger.getTransactionSet(ledgerBlock);
		int lastHeightTxTotalNums = 0;

		if (blockHeight > 0) {
			lastHeightTxTotalNums = (int) ledger.getTransactionSet(ledger.getBlock(blockHeight - 1)).getTotalCount();
		}

		int currentHeightTxTotalNums = (int)ledger.getTransactionSet(ledger.getBlock(blockHeight)).getTotalCount();
		//取当前高度的增量交易数，在增量交易里进行查找
		int currentHeightTxNums = currentHeightTxTotalNums - lastHeightTxTotalNums;

//		if (fromIndex < 0 || fromIndex >= currentHeightTxNums) {
//			fromIndex = 0;
//		}
//		if (count == -1) {
//			fromIndex = 0;
//			count = currentHeightTxNums;
//		}
//		if (count > currentHeightTxNums) {
//			count = currentHeightTxNums - fromIndex;
//		}
		int indexAndCount[] = QueryUtil.calFromIndexAndCount(fromIndex, count, currentHeightTxNums);
		return transactionSet.getTxs(lastHeightTxTotalNums + indexAndCount[0] , indexAndCount[1]);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/txs")
	@Override
	public LedgerTransaction[] getTransactions(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
											   @PathVariable(name = "blockHash") HashDigest blockHash,
											   @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
											   @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock ledgerBlock = ledger.getBlock(blockHash);
		long height = ledgerBlock.getHeight();
		TransactionSet transactionSet = ledger.getTransactionSet(ledgerBlock);
		int lastHeightTxTotalNums = 0;

		if (height > 0) {
			lastHeightTxTotalNums = (int) ledger.getTransactionSet(ledger.getBlock(height - 1)).getTotalCount();
		}

		int currentHeightTxTotalNums = (int)ledger.getTransactionSet(ledger.getBlock(height)).getTotalCount();
		//取当前块hash的增量交易数，在增量交易里进行查找
		int currentHeightTxNums = currentHeightTxTotalNums - lastHeightTxTotalNums;

//		if (fromIndex < 0 || fromIndex >= currentHeightTxNums) {
//			fromIndex = 0;
//		}
//		if (count == -1) {
//			fromIndex = 0;
//			count = currentHeightTxNums;
//		}
//		if (count > currentHeightTxNums) {
//			count = currentHeightTxNums - fromIndex;
//		}
		int indexAndCount[] = QueryUtil.calFromIndexAndCount(fromIndex, count, currentHeightTxNums);
		return transactionSet.getTxs(lastHeightTxTotalNums + indexAndCount[0] , indexAndCount[1]);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/{contentHash}")
	@Override
	public LedgerTransaction getTransactionByContentHash(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
														 @PathVariable(name = "contentHash")HashDigest contentHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		TransactionSet txset = ledger.getTransactionSet(block);
		return txset.get(contentHash);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/state/{contentHash}")
	@Override
	public TransactionState getTransactionStateByContentHash(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
															 @PathVariable(name = "contentHash") HashDigest contentHash) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		TransactionSet txset = ledger.getTransactionSet(block);
		return txset.getTxState(contentHash);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users/address/{address}")
	@Override
	public UserInfo getUser(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
							@PathVariable(name = "address") String address) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		UserAccountSet userAccountSet = ledger.getUserAccountSet(block);
		return userAccountSet.getUser(address);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/address/{address}")
	@Override
	public AccountHeader getDataAccount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
										@PathVariable(name = "address") String address) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		return dataAccountSet.getDataAccount(Bytes.fromBase58(address));
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "ledgers/{ledgerHash}/accounts/{address}/entries")
	@Override
	public KVDataEntry[] getDataEntries(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
										@PathVariable(name = "address") String address,
										@RequestParam("keys") String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		DataAccount dataAccount = dataAccountSet.getDataAccount(Bytes.fromBase58(address));
		
		KVDataEntry[] entries = new KVDataEntry[keys.length];
		long ver;
		for (int i = 0; i < entries.length; i++) {
			ver = dataAccount.getDataVersion(Bytes.fromString(keys[i]));
			if (ver < 0) {
				entries[i] = new KVDataObject(keys[i], -1, ValueType.NIL, null);
			}else {
				byte[] value = dataAccount.getBytes(Bytes.fromString(keys[i]), ver);
				BytesValue decodeData = BinaryEncodingUtils.decode(value);
				entries[i] = new KVDataObject(keys[i], ver, ValueType.valueOf(decodeData.getType().CODE), decodeData.getValue().toBytes());
			}
		}
		
		return entries;
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "ledgers/{ledgerHash}/accounts/address/{address}/entries")
	@Override
	public KVDataEntry[] getDataEntries(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
										@PathVariable(name = "address") String address,
										@RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
										@RequestParam(name = "count", required = false, defaultValue = "-1") int count) {

		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		DataAccount dataAccount = dataAccountSet.getDataAccount(Bytes.fromBase58(address));

		return dataAccount.getDataEntries(fromIndex, count);
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/address/{address}/entries/count")
	@Override
	public long getDataEntriesTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
										 @PathVariable(name = "address") String address) {

		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
		DataAccount dataAccount = dataAccountSet.getDataAccount(Bytes.fromBase58(address));

		return dataAccount.getDataEntriesTotalCount();
	}

	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts/address/{address}")
	@Override
	public AccountHeader getContract(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
									 @PathVariable(name = "address") String address) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		ContractAccountSet contractAccountSet = ledger.getContractAccountSet(block);
		return contractAccountSet.getContract(Bytes.fromBase58(address));
	}

	/**
	 * get more users by fromIndex and count;
	 * @param ledgerHash
	 * @param fromIndex
	 * @param count
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users")
	@Override
	public AccountHeader[] getUsers(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
							  @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
							  @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		UserAccountSet userAccountSet = ledger.getUserAccountSet(block);
        int pages[] = QueryUtil.calFromIndexAndCount(fromIndex,count,(int)userAccountSet.getTotalCount());
		return userAccountSet.getAccounts(pages[0],pages[1]);
	}

	/**
	 * get more dataAccounts by fromIndex and count;
	 * @param ledgerHash
	 * @param fromIndex
	 * @param count
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts")
    @Override
	public AccountHeader[] getDataAccounts(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
								@RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
								@RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
		LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
		LedgerBlock block = ledger.getLatestBlock();
		DataAccountSet dataAccountSet = ledger.getDataAccountSet(block);
        int pages[] = QueryUtil.calFromIndexAndCount(fromIndex,count,(int)dataAccountSet.getTotalCount());
		return dataAccountSet.getAccounts(pages[0],pages[1]);
	}

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts")
    @Override
    public AccountHeader[] getContractAccounts(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                      @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                      @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        LedgerRepository ledger = ledgerService.getLedger(ledgerHash);
        LedgerBlock block = ledger.getLatestBlock();
        ContractAccountSet contractAccountSet = ledger.getContractAccountSet(block);
        int pages[] = QueryUtil.calFromIndexAndCount(fromIndex,count,(int)contractAccountSet.getTotalCount());
        return contractAccountSet.getAccounts(pages[0],pages[1]);
    }

}
