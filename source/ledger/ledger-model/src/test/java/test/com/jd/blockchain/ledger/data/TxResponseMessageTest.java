/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: test.com.jd.blockchain.ledger.data.TxResponseMessageTest
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/9/6 上午11:00
 * Description:
 */
package test.com.jd.blockchain.ledger.data;

import com.jd.blockchain.binaryproto.BinaryEncodingUtils;
import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.ledger.data.TxResponseMessage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author shaozhuguang
 * @create 2018/9/6
 * @since 1.0.0
 */

public class TxResponseMessageTest {

    private TxResponseMessage data;

    @Before
    public void initTxRequestMessage() throws Exception {
        DataContractRegistry.register(TransactionResponse.class);

        HashDigest contentHash = new HashDigest(CryptoAlgorithm.SHA256, "jd-content".getBytes());

        HashDigest blockHash = new HashDigest(CryptoAlgorithm.SHA256, "jd-block".getBytes());

        long blockHeight = 9999L;
        data = new TxResponseMessage(contentHash);
        data.setBlockHash(blockHash);
        data.setBlockHeight(blockHeight);
        data.setExecutionState(TransactionState.SUCCESS);
    }

    @Test
    public void testSerialize_TransactionResponse() {
        byte[] serialBytes = BinaryEncodingUtils.encode(data, TransactionResponse.class);
        TransactionResponse resolvedData = BinaryEncodingUtils.decode(serialBytes);
        System.out.println("------Assert start ------");
        assertEquals(resolvedData.getBlockHash(), data.getBlockHash());
        assertEquals(resolvedData.getBlockHeight(), data.getBlockHeight());
        assertEquals(resolvedData.getContentHash(), data.getContentHash());
        assertEquals(resolvedData.getExecutionState(), data.getExecutionState());
        assertEquals(resolvedData.isSuccess(), data.isSuccess());
        System.out.println("------Assert OK ------");
    }
}