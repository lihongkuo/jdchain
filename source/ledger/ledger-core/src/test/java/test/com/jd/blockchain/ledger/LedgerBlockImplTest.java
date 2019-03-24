/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: test.com.jd.blockchain.ledger.LedgerBlockImplTest
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/8/30 上午10:45
 * Description:
 */
package test.com.jd.blockchain.ledger;

import com.jd.blockchain.binaryproto.BinaryEncodingUtils;
import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.LedgerBlock;
import com.jd.blockchain.ledger.LedgerDataSnapshot;
import com.jd.blockchain.ledger.core.impl.LedgerBlockData;
import com.jd.blockchain.ledger.core.impl.TransactionStagedSnapshot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author shaozhuguang
 * @create 2018/8/30
 * @since 1.0.0
 */

public class LedgerBlockImplTest {

    private LedgerBlockData data;

    @Before
    public void initLedgerBlockImpl() {
        DataContractRegistry.register(LedgerBlock.class);
        DataContractRegistry.register(LedgerDataSnapshot.class);
        long height = 9999L;
        HashDigest ledgerHash = new HashDigest(CryptoAlgorithm.SHA256, "zhangsan".getBytes());
        HashDigest previousHash = new HashDigest(CryptoAlgorithm.SHA256, "lisi".getBytes());
        data = new LedgerBlockData(height, ledgerHash, previousHash);
        data.setHash(new HashDigest(CryptoAlgorithm.SHA256, "wangwu".getBytes()));
        data.setTransactionSetHash(new HashDigest(CryptoAlgorithm.SHA256, "zhaoliu".getBytes()));

        // 设置LedgerDataSnapshot相关属性
        data.setAdminAccountHash(new HashDigest(CryptoAlgorithm.SHA256, "jd1".getBytes()));
        data.setDataAccountSetHash(new HashDigest(CryptoAlgorithm.SHA256, "jd2".getBytes()));
        data.setUserAccountSetHash(new HashDigest(CryptoAlgorithm.SHA256, "jd3".getBytes()));
        data.setContractAccountSetHash(new HashDigest(CryptoAlgorithm.SHA256, "jd4".getBytes()));

    }

    @Test
    public void testSerialize_LedgerBlock() throws Exception {
        byte[] serialBytes = BinaryEncodingUtils.encode(data, LedgerBlock.class);
        LedgerBlock resolvedData = BinaryEncodingUtils.decode(serialBytes);
        System.out.println("------Assert start ------");
        assertEquals(resolvedData.getHash(), data.getHash());
        assertEquals(resolvedData.getHeight(), data.getHeight());
        assertEquals(resolvedData.getLedgerHash(), data.getLedgerHash());
        assertEquals(resolvedData.getPreviousHash(), data.getPreviousHash());
        assertEquals(resolvedData.getTransactionSetHash(), data.getTransactionSetHash());
        assertEquals(resolvedData.getAdminAccountHash(), data.getAdminAccountHash());
        assertEquals(resolvedData.getContractAccountSetHash(), data.getContractAccountSetHash());
        assertEquals(resolvedData.getDataAccountSetHash(), data.getDataAccountSetHash());
        assertEquals(resolvedData.getUserAccountSetHash(), data.getUserAccountSetHash());
        System.out.println("------Assert OK ------");
    }

    //notice: LedgerBlock interface has more field info than LedgerDataSnapshot interface, so cannot deserialize LedgerBlock
    // with LedgerDataSnapshot encode
    //@Test
    //public void testSerialize_LedgerDataSnapshot() throws Exception {
    //byte[] serialBytes = BinaryEncodingUtils.encode(data, LedgerDataSnapshot.class);
    //LedgerDataSnapshot resolvedData = BinaryEncodingUtils.decode(serialBytes, null,
    //LedgerBlockData.class);
    //System.out.println("------Assert start ------");
    //assertEquals(resolvedData.getAdminAccountHash(), data.getAdminAccountHash());
    //assertEquals(resolvedData.getAdminAccountHash(), data.getAdminAccountHash());
    //assertEquals(resolvedData.getContractAccountSetHash(), data.getContractAccountSetHash());
    //assertEquals(resolvedData.getDataAccountSetHash(), data.getDataAccountSetHash());
    //assertEquals(resolvedData.getUserAccountSetHash(), data.getUserAccountSetHash());
    //System.out.println("------Assert OK ------");
    //}

    @Test
    public void testSerialize_LedgerDataSnapshot() throws Exception {
        TransactionStagedSnapshot transactionStagedSnapshot = new TransactionStagedSnapshot();

        HashDigest admin = new HashDigest(CryptoAlgorithm.SHA256, "alice".getBytes());
        HashDigest contract = new HashDigest(CryptoAlgorithm.SHA256, "bob".getBytes());
        HashDigest data = new HashDigest(CryptoAlgorithm.SHA256, "jerry".getBytes());
        HashDigest user = new HashDigest(CryptoAlgorithm.SHA256, "tom".getBytes());

        transactionStagedSnapshot.setAdminAccountHash(admin);
        transactionStagedSnapshot.setContractAccountSetHash(contract);
        transactionStagedSnapshot.setDataAccountSetHash(data);
        transactionStagedSnapshot.setUserAccountSetHash(user);

        byte[] serialBytes = BinaryEncodingUtils.encode(transactionStagedSnapshot, LedgerDataSnapshot.class);
        LedgerDataSnapshot resolvedData = BinaryEncodingUtils.decode(serialBytes);

        //verify start
        assertEquals(resolvedData.getAdminAccountHash(), transactionStagedSnapshot.getAdminAccountHash());
        assertEquals(resolvedData.getContractAccountSetHash(), transactionStagedSnapshot.getContractAccountSetHash());
        assertEquals(resolvedData.getDataAccountSetHash(), transactionStagedSnapshot.getDataAccountSetHash());
        assertEquals(resolvedData.getUserAccountSetHash(), transactionStagedSnapshot.getUserAccountSetHash());
        //verify succeed

    }

}