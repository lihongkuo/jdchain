/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: com.jd.blockchain.consensus.mq.config.MsgQueueClientIncomingSettings
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/12/13 下午4:35
 * Description:
 */
package com.jd.blockchain.consensus.mq.settings;

import com.jd.blockchain.base.data.TypeCodes;
import com.jd.blockchain.binaryproto.DataContract;
import com.jd.blockchain.binaryproto.DataField;
import com.jd.blockchain.consensus.ClientIncomingSettings;
import com.jd.blockchain.consensus.ConsensusSettings;
import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.utils.ValueType;

/**
 *
 * @author shaozhuguang
 * @create 2018/12/13
 * @since 1.0.0
 */
@DataContract(code = TypeCodes.CONSENSUS_MSGQUEUE_CLI_INCOMING_SETTINGS)
public interface MsgQueueClientIncomingSettings extends ClientIncomingSettings {

    @DataField(order = 1, primitiveType=ValueType.BYTES)
    PubKey getPubKey();
}