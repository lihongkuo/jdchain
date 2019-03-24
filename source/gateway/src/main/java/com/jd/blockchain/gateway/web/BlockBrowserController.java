package com.jd.blockchain.gateway.web;

import com.jd.blockchain.crypto.AddressEncoding;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.gateway.PeerService;
import com.jd.blockchain.gateway.service.DataRetrievalService;
import com.jd.blockchain.gateway.service.GatewayQueryService;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.sdk.BlockchainExtendQueryService;
import com.jd.blockchain.tools.keygen.KeyGenCommand;
import com.jd.blockchain.utils.BaseConstant;
import com.jd.blockchain.utils.ConsoleUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/")
public class BlockBrowserController implements BlockchainExtendQueryService {
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BlockBrowserController.class);

	@Autowired
	private PeerService peerService;

	@Autowired
    private DataRetrievalService dataRetrievalService;

    @Autowired
    private GatewayQueryService gatewayQueryService;

    private String dataRetrievalUrl;

	private static final long BLOCK_MAX_DISPLAY = 3L;

	private static final long GENESIS_BLOCK_HEIGHT = 0L;

	@Deprecated
//    @RequestMapping(method = RequestMethod.GET, path = "ledgers")
    @Override
    public HashDigest[] getLedgerHashs() {
        return peerService.getQueryService().getLedgerHashs();
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}")
    @Override
    public LedgerInfo getLedger(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        return  peerService.getQueryService().getLedger(ledgerHash);
    }

//    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/participants")
    @Override
    public ParticipantNode[] getConsensusParticipants(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        return peerService.getQueryService().getConsensusParticipants(ledgerHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks")
    public LedgerBlock[] getBlocks(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        LedgerInfo ledgerInfo = peerService.getQueryService().getLedger(ledgerHash);
        long maxBlockHeight = ledgerInfo.getLatestBlockHeight();
        List<LedgerBlock> ledgerBlocks = new ArrayList<>();
        for (long blockHeight = maxBlockHeight; blockHeight > GENESIS_BLOCK_HEIGHT; blockHeight--) {
            LedgerBlock ledgerBlock = peerService.getQueryService().getBlock(ledgerHash, blockHeight);
            ledgerBlocks.add(0, ledgerBlock);
            if (ledgerBlocks.size() == BLOCK_MAX_DISPLAY) {
                break;
            }
        }
        // 最后增加创世区块
        LedgerBlock genesisBlock = peerService.getQueryService().getBlock(ledgerHash, GENESIS_BLOCK_HEIGHT);
        ledgerBlocks.add(0, genesisBlock);
        LedgerBlock[] blocks = new LedgerBlock[ledgerBlocks.size()];
        ledgerBlocks.toArray(blocks);
        return blocks;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}")
    @Override
    public LedgerBlock getBlock(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                @PathVariable(name = "blockHeight") long blockHeight) {
        return peerService.getQueryService().getBlock(ledgerHash, blockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}")
    @Override
    public LedgerBlock getBlock(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                @PathVariable(name = "blockHash") HashDigest blockHash) {
        return peerService.getQueryService().getBlock(ledgerHash, blockHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/txs/count")
    @Override
    public long getTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                    @PathVariable(name = "blockHeight") long blockHeight) {
        return peerService.getQueryService().getTransactionCount(ledgerHash, blockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/txs/count")
    @Override
    public long getTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                    @PathVariable(name = "blockHash") HashDigest blockHash) {
        return peerService.getQueryService().getTransactionCount(ledgerHash, blockHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/count")
    @Override
    public long getTransactionTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
	    return peerService.getQueryService().getTransactionTotalCount(ledgerHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/accounts/count")
    @Override
    public long getDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                    @PathVariable(name = "blockHeight") long blockHeight) {
        return peerService.getQueryService().getDataAccountCount(ledgerHash, blockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/accounts/count")
    @Override
    public long getDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                    @PathVariable(name = "blockHash") HashDigest blockHash) {
        return peerService.getQueryService().getDataAccountCount(ledgerHash, blockHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/count")
    @Override
    public long getDataAccountTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        return peerService.getQueryService().getDataAccountTotalCount(ledgerHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/users/count")
    @Override
    public long getUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                             @PathVariable(name = "blockHeight") long blockHeight) {
        return peerService.getQueryService().getUserCount(ledgerHash, blockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/users/count")
    @Override
    public long getUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                             @PathVariable(name = "blockHash") HashDigest blockHash) {
        return peerService.getQueryService().getUserCount(ledgerHash, blockHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users/count")
    @Override
    public long getUserTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
	    return peerService.getQueryService().getUserTotalCount(ledgerHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/contracts/count")
    @Override
    public long getContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                 @PathVariable(name = "blockHeight") long blockHeight) {
        return peerService.getQueryService().getContractCount(ledgerHash, blockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/contracts/count")
    @Override
    public long getContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                 @PathVariable(name = "blockHash") HashDigest blockHash) {
        return peerService.getQueryService().getContractCount(ledgerHash, blockHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts/count")
    @Override
    public long getContractTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
	    return peerService.getQueryService().getContractTotalCount(ledgerHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/txs")
    @Override
    public LedgerTransaction[] getTransactions(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                               @PathVariable(name = "blockHeight") long blockHeight,
                                               @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                               @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
	    return peerService.getQueryService().getTransactions(ledgerHash, blockHeight, fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/txs")
    @Override
    public LedgerTransaction[] getTransactions(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                               @PathVariable(name = "blockHash") HashDigest blockHash,
                                               @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                               @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
	    return peerService.getQueryService().getTransactions(ledgerHash, blockHash, fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/hash/{contentHash}")
    @Override
    public LedgerTransaction getTransactionByContentHash(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                                         @PathVariable(name = "contentHash") HashDigest contentHash) {
	    return peerService.getQueryService().getTransactionByContentHash(ledgerHash, contentHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/state/{contentHash}")
    @Override
    public TransactionState getTransactionStateByContentHash(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                                             @PathVariable(name = "contentHash") HashDigest contentHash) {
	    return peerService.getQueryService().getTransactionStateByContentHash(ledgerHash, contentHash);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users/address/{address}")
    @Override
    public UserInfo getUser(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                            @PathVariable(name = "address") String address) {
	    return peerService.getQueryService().getUser(ledgerHash, address);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/address/{address}")
    @Override
    public AccountHeader getDataAccount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                        @PathVariable(name = "address") String address) {

	    return peerService.getQueryService().getDataAccount(ledgerHash, address);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "ledgers/{ledgerHash}/accounts/{address}/entries")
    @Override
    public KVDataEntry[] getDataEntries(@PathVariable("ledgerHash") HashDigest ledgerHash,
                                        @PathVariable("address") String address,
                                        @RequestParam("keys") String... keys) {
	    return peerService.getQueryService().getDataEntries(ledgerHash, address, keys);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "ledgers/{ledgerHash}/accounts/address/{address}/entries")
    @Override
    public KVDataEntry[] getDataEntries(@PathVariable("ledgerHash") HashDigest ledgerHash,
                                        @PathVariable("address") String address,
                                        @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                        @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        return peerService.getQueryService().getDataEntries(ledgerHash, address, fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/address/{address}/entries/count")
    @Override
    public long getDataEntriesTotalCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                         @PathVariable(name = "address") String address) {
        return peerService.getQueryService().getDataEntriesTotalCount(ledgerHash, address);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts/address/{address}")
    @Override
    public AccountHeader getContract(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                     @PathVariable(name = "address") String address) {
	    return peerService.getQueryService().getContract(ledgerHash, address);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/latest")
    @Override
    public LedgerBlock getLatestBlock(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        long latestBlockHeight = peerService.getQueryService().getLedger(ledgerHash).getLatestBlockHeight();
        return peerService.getQueryService().getBlock(ledgerHash, latestBlockHeight);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/txs/additional-count")
    @Override
    public long getAdditionalTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                              @PathVariable(name = "blockHeight") long blockHeight) {
        // 获取某个区块的交易总数
        long currentBlockTxCount = peerService.getQueryService().getTransactionCount(ledgerHash, blockHeight);
        if (blockHeight == GENESIS_BLOCK_HEIGHT) {
            return currentBlockTxCount;
        }
        long lastBlockHeight = blockHeight - 1;
        long lastBlockTxCount = peerService.getQueryService().getTransactionCount(ledgerHash, lastBlockHeight);
        // 当前区块交易数减上个区块交易数
        return currentBlockTxCount - lastBlockTxCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/txs/additional-count")
    @Override
    public long getAdditionalTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                              @PathVariable(name = "blockHash") HashDigest blockHash) {
        LedgerBlock currentBlock = peerService.getQueryService().getBlock(ledgerHash, blockHash);
        long currentBlockTxCount = peerService.getQueryService().getTransactionCount(ledgerHash, blockHash);
        if (currentBlock.getHeight() == GENESIS_BLOCK_HEIGHT) {
            return currentBlockTxCount;
        }
        HashDigest previousHash = currentBlock.getPreviousHash();
        long lastBlockTxCount = peerService.getQueryService().getTransactionCount(ledgerHash, previousHash);
        // 当前区块交易数减上个区块交易数
        return currentBlockTxCount - lastBlockTxCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/txs/additional-count")
    @Override
    public long getAdditionalTransactionCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        LedgerInfo ledgerInfo = peerService.getQueryService().getLedger(ledgerHash);
        long maxBlockHeight = ledgerInfo.getLatestBlockHeight();
        long totalCount = peerService.getQueryService().getTransactionTotalCount(ledgerHash);
        if (maxBlockHeight == GENESIS_BLOCK_HEIGHT) { // 只有一个创世区块
            return totalCount;
        }
        long lastTotalCount = peerService.getQueryService().getTransactionCount(ledgerHash, maxBlockHeight - 1);
        return totalCount - lastTotalCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/accounts/additional-count")
    @Override
    public long getAdditionalDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                              @PathVariable(name = "blockHeight") long blockHeight) {
        long currentDaCount = peerService.getQueryService().getDataAccountCount(ledgerHash, blockHeight);
        if (blockHeight == GENESIS_BLOCK_HEIGHT) {
            return currentDaCount;
        }
        long lastBlockHeight = blockHeight - 1;
        long lastDaCount = peerService.getQueryService().getDataAccountCount(ledgerHash, lastBlockHeight);
        return currentDaCount - lastDaCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/accounts/additional-count")
    @Override
    public long getAdditionalDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                              @PathVariable(name = "blockHash") HashDigest blockHash) {
        LedgerBlock currentBlock = peerService.getQueryService().getBlock(ledgerHash, blockHash);
        long currentBlockDaCount = peerService.getQueryService().getDataAccountCount(ledgerHash, blockHash);
        if (currentBlock.getHeight() == GENESIS_BLOCK_HEIGHT) {
            return currentBlockDaCount;
        }
        HashDigest previousHash = currentBlock.getPreviousHash();
        long lastBlockDaCount = peerService.getQueryService().getDataAccountCount(ledgerHash, previousHash);
        // 当前区块数据账户数量减上个区块数据账户数量
        return currentBlockDaCount - lastBlockDaCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts/additional-count")
    @Override
    public long getAdditionalDataAccountCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        LedgerInfo ledgerInfo = peerService.getQueryService().getLedger(ledgerHash);
        long maxBlockHeight = ledgerInfo.getLatestBlockHeight();
        long totalCount = peerService.getQueryService().getDataAccountTotalCount(ledgerHash);
        if (maxBlockHeight == GENESIS_BLOCK_HEIGHT) { // 只有一个创世区块
            return totalCount;
        }
        long lastTotalCount = peerService.getQueryService().getDataAccountCount(ledgerHash, maxBlockHeight - 1);
        return totalCount - lastTotalCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/users/additional-count")
    @Override
    public long getAdditionalUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                       @PathVariable(name = "blockHeight") long blockHeight) {
        long currentUserCount = peerService.getQueryService().getUserCount(ledgerHash, blockHeight);
        if (blockHeight == GENESIS_BLOCK_HEIGHT) {
            return currentUserCount;
        }
        long lastBlockHeight = blockHeight - 1;
        long lastUserCount = peerService.getQueryService().getUserCount(ledgerHash, lastBlockHeight);
        return currentUserCount - lastUserCount;
    }


    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/users/additional-count")
    @Override
    public long getAdditionalUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                       @PathVariable(name = "blockHash") HashDigest blockHash) {
        LedgerBlock currentBlock = peerService.getQueryService().getBlock(ledgerHash, blockHash);
        long currentBlockUserCount = peerService.getQueryService().getUserCount(ledgerHash, blockHash);
        if (currentBlock.getHeight() == GENESIS_BLOCK_HEIGHT) {
            return currentBlockUserCount;
        }
        HashDigest previousHash = currentBlock.getPreviousHash();
        long lastBlockUserCount = peerService.getQueryService().getUserCount(ledgerHash, previousHash);
        // 当前区块用户数量减上个区块用户数量
        return currentBlockUserCount - lastBlockUserCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/users/additional-count")
    @Override
    public long getAdditionalUserCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        LedgerInfo ledgerInfo = peerService.getQueryService().getLedger(ledgerHash);
        long maxBlockHeight = ledgerInfo.getLatestBlockHeight();
        long totalCount = peerService.getQueryService().getUserTotalCount(ledgerHash);
        if (maxBlockHeight == GENESIS_BLOCK_HEIGHT) { // 只有一个创世区块
            return totalCount;
        }
        long lastTotalCount = peerService.getQueryService().getUserCount(ledgerHash, maxBlockHeight - 1);
        return totalCount - lastTotalCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/height/{blockHeight}/contracts/additional-count")
    @Override
    public long getAdditionalContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                           @PathVariable(name = "blockHeight") long blockHeight) {
        long currentContractCount = peerService.getQueryService().getContractCount(ledgerHash, blockHeight);
        if (blockHeight == GENESIS_BLOCK_HEIGHT) {
            return currentContractCount;
        }
        long lastBlockHeight = blockHeight - 1;
        long lastContractCount = peerService.getQueryService().getUserCount(ledgerHash, lastBlockHeight);
        return currentContractCount - lastContractCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/blocks/hash/{blockHash}/contracts/additional-count")
    @Override
    public long getAdditionalContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                           @PathVariable(name = "blockHash") HashDigest blockHash) {
        LedgerBlock currentBlock = peerService.getQueryService().getBlock(ledgerHash, blockHash);
        long currentBlockContractCount = peerService.getQueryService().getContractCount(ledgerHash, blockHash);
        if (currentBlock.getHeight() == GENESIS_BLOCK_HEIGHT) {
            return currentBlockContractCount;
        }
        HashDigest previousHash = currentBlock.getPreviousHash();
        long lastBlockContractCount = peerService.getQueryService().getUserCount(ledgerHash, previousHash);
        // 当前区块合约数量减上个区块合约数量
        return currentBlockContractCount - lastBlockContractCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts/additional-count")
    @Override
    public long getAdditionalContractCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        LedgerInfo ledgerInfo = peerService.getQueryService().getLedger(ledgerHash);
        long maxBlockHeight = ledgerInfo.getLatestBlockHeight();
        long totalCount = peerService.getQueryService().getContractTotalCount(ledgerHash);
        if (maxBlockHeight == GENESIS_BLOCK_HEIGHT) { // 只有一个创世区块
            return totalCount;
        }
        long lastTotalCount = peerService.getQueryService().getContractCount(ledgerHash, maxBlockHeight - 1);
        return totalCount - lastTotalCount;
    }

    @RequestMapping(method = RequestMethod.GET, path = "utils/pubkey/{pubkey}/addr")
    public String getAddrByPubKey(@PathVariable(name = "pubkey") String strPubKey) {
        PubKey pubKey = KeyGenCommand.decodePubKey(strPubKey);
        return AddressEncoding.generateAddress(pubKey).toBase58();
    }

    @RequestMapping(method = RequestMethod.GET, value = "ledgers/{ledgerHash}/**/search")
    public Object dataRetrieval(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,HttpServletRequest request) {
        String result;
        if (dataRetrievalUrl == null || dataRetrievalUrl.length() <= 0) {
            result = "{'message':'OK','data':'" + "data.retrieval.url is empty" + "'}";
        } else {
            String queryParams = request.getQueryString() == null ? "": request.getQueryString();
            String fullQueryUrl = new StringBuffer(dataRetrievalUrl)
                    .append(request.getRequestURI())
                    .append(BaseConstant.DELIMETER_QUESTION)
                    .append(queryParams)
                    .toString();
            try {
                result = dataRetrievalService.retrieval(fullQueryUrl);
                ConsoleUtils.info("request = {%s} \r\n result = {%s} \r\n", fullQueryUrl, result);
            } catch (Exception e) {
                result = "{'message':'OK','data':'" + e.getMessage() + "'}";
            }
        }
        return result;
    }

    public void setDataRetrievalUrl(String dataRetrievalUrl) {
        this.dataRetrievalUrl = dataRetrievalUrl;
    }

    /**
     *  get all ledgers count;
     */
    @RequestMapping(method = RequestMethod.GET, path = "ledgers/count")
    @Override
    public int getLedgersCount() {
        return  peerService.getQueryService().getLedgerHashs().length;
    }

    /**
     * get all ledgers hashs;
     */
    @RequestMapping(method = RequestMethod.GET, path = "ledgers")
    public HashDigest[] getLedgersHash(@RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                   @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        return gatewayQueryService.getLedgersHash(fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/participants/count")
    public int getConsensusParticipantCount(@PathVariable(name = "ledgerHash") HashDigest ledgerHash) {
        return peerService.getQueryService().getConsensusParticipants(ledgerHash).length;
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/participants")
    public ParticipantNode[] getConsensusParticipants(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                                      @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                                      @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        return gatewayQueryService.getConsensusParticipants(ledgerHash,fromIndex,count);
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
        return peerService.getQueryService().getUsers(ledgerHash, fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/accounts")
    @Override
    public AccountHeader[] getDataAccounts(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                           @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                           @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        return peerService.getQueryService().getDataAccounts(ledgerHash, fromIndex, count);
    }

    @RequestMapping(method = RequestMethod.GET, path = "ledgers/{ledgerHash}/contracts")
    @Override
    public AccountHeader[] getContractAccounts(@PathVariable(name = "ledgerHash") HashDigest ledgerHash,
                                           @RequestParam(name = "fromIndex", required = false, defaultValue = "0") int fromIndex,
                                           @RequestParam(name = "count", required = false, defaultValue = "-1") int count) {
        return peerService.getQueryService().getContractAccounts(ledgerHash, fromIndex, count);
    }
}
