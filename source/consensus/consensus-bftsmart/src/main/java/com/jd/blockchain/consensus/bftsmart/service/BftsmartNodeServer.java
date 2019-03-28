package com.jd.blockchain.consensus.bftsmart.service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import bftsmart.tom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jd.blockchain.consensus.ConsensusManageService;
import com.jd.blockchain.consensus.NodeSettings;
import com.jd.blockchain.consensus.bftsmart.BftsmartConsensusProvider;
import com.jd.blockchain.consensus.bftsmart.BftsmartConsensusSettings;
import com.jd.blockchain.consensus.bftsmart.BftsmartNodeSettings;
import com.jd.blockchain.consensus.bftsmart.BftsmartTopology;
import com.jd.blockchain.consensus.service.MessageHandle;
import com.jd.blockchain.consensus.service.NodeServer;
import com.jd.blockchain.consensus.service.ServerSettings;
import com.jd.blockchain.consensus.service.StateHandle;
import com.jd.blockchain.consensus.service.StateMachineReplicate;
import com.jd.blockchain.ledger.TransactionState;
import com.jd.blockchain.utils.PropertiesUtils;
import com.jd.blockchain.utils.concurrent.AsyncFuture;
import com.jd.blockchain.utils.io.BytesUtils;
import bftsmart.reconfiguration.util.HostsConfig;
import bftsmart.reconfiguration.util.TOMConfiguration;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;

public class BftsmartNodeServer extends DefaultRecoverable implements NodeServer {

    private static Logger LOGGER = LoggerFactory.getLogger(BftsmartNodeServer.class);

    private List<StateHandle> stateHandles = new CopyOnWriteArrayList<>();

    // TODO 暂不处理队列溢出问题
    private ExecutorService notifyReplyExecutors = Executors.newSingleThreadExecutor();

    private volatile Status status = Status.STOPPED;

    private final Object mutex = new Object();

    private volatile ServiceReplica replica;

    private StateMachineReplicate stateMachineReplicate;

    private ServerSettings serverSettings;

    private BftsmartConsensusManageService manageService;


    private volatile BftsmartTopology topology;

    private volatile BftsmartConsensusSettings setting;

    private TOMConfiguration tomConfig;

    private HostsConfig hostsConfig;
    private Properties systemConfig;

    private MessageHandle messageHandle;

    private String providerName;

    private String realmName;

    private int serverId;

    public BftsmartNodeServer() {

    }

    public BftsmartNodeServer(ServerSettings serverSettings, MessageHandle messageHandler, StateMachineReplicate stateMachineReplicate) {
        this.serverSettings = serverSettings;
        this.realmName = serverSettings.getRealmName();
        //used later
        this.stateMachineReplicate = stateMachineReplicate;
        this.messageHandle = messageHandler;
        this.manageService = new BftsmartConsensusManageService(this);
        createConfig();
        serverId = findServerId();
        initConfig(serverId, systemConfig, hostsConfig);
    }

    protected int findServerId() {
        int serverId = 0;

        for (int i = 0; i < hostsConfig.getNum(); i++) {
            String host = ((BftsmartNodeSettings)serverSettings.getReplicaSettings()).getNetworkAddress().getHost();
            int port = ((BftsmartNodeSettings)serverSettings.getReplicaSettings()).getNetworkAddress().getPort();

            if (hostsConfig.getHost(i).equals(host) && hostsConfig.getPort(i) == port) {
                serverId = i;
                break;
            }
        }

        return serverId;
    }

    public int getServerId() {
        return serverId;
    }

    protected void createConfig() {

        setting = ((BftsmartServerSettings) serverSettings).getConsensusSettings();

        List<HostsConfig.Config> configList = new ArrayList<HostsConfig.Config>();

        NodeSettings[] nodeSettingsArray = setting.getNodes();
        for (NodeSettings nodeSettings : nodeSettingsArray) {
            BftsmartNodeSettings node = (BftsmartNodeSettings)nodeSettings;
            configList.add(new HostsConfig.Config(node.getId(), node.getNetworkAddress().getHost(), node.getNetworkAddress().getPort()));
        }

        //create HostsConfig instance based on consensus realm nodes
        hostsConfig = new HostsConfig(configList.toArray(new HostsConfig.Config[configList.size()]));

        systemConfig = PropertiesUtils.createProperties(setting.getSystemConfigs());

        return;
    }

    protected void initConfig(int id, String systemConfig, String hostsConfig) {

        this.tomConfig = new TOMConfiguration(id, systemConfig, hostsConfig);

    }

    protected void initConfig(int id, Properties systemsConfig, HostsConfig hostConfig) {
        this.tomConfig = new TOMConfiguration(id, systemsConfig, hostConfig);
    }

    @Override
    public ConsensusManageService getManageService() {
        return manageService;
    }

    @Override
    public ServerSettings getSettings() {
        return serverSettings;
    }

    @Override
    public String getProviderName() {
        return BftsmartConsensusProvider.NAME;
    }

    public TOMConfiguration getTomConfig() {
        return tomConfig;
    }

    public int getId() {
        return tomConfig.getProcessId();
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ReplicaID is negative!");
        }
        this.tomConfig.setProcessId(id);

    }

    public BftsmartConsensusSettings getConsensusSetting() {
        return setting;
    }

    public BftsmartTopology getTopology() {
        return topology;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        return messageHandle.processUnordered(bytes).get();
    }

    @Override
    public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
        return appExecuteBatch(commands, msgCtxs, fromConsensus, null);
    }

    @Override
    public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus, List<ReplyContextMessage> replyList) {

        if (replyList == null || replyList.size() == 0) {
            throw new IllegalArgumentException();
        }
        // todo 此部分需要重新改造
        /**
         * 默认BFTSmart接口提供的commands是一个或多个共识结果的顺序集合
         * 根据共识的规定，目前的做法是将其根据msgCtxs的内容进行分组，每组都作为一个结块标识来处理
         * 从msgCtxs可以获取对应commands的分组情况
         */
        int manageConsensusId = msgCtxs[0].getConsensusId();
        List<byte[]> manageConsensusCmds = new ArrayList<>();
        List<ReplyContextMessage> manageReplyMsgs = new ArrayList<>();

        int index = 0;
        for (MessageContext msgCtx : msgCtxs) {
            if (msgCtx.getConsensusId() == manageConsensusId) {
                manageConsensusCmds.add(commands[index]);
                manageReplyMsgs.add(replyList.get(index));
            } else {
                // 达到结块标准，需要进行结块并应答
                blockAndReply(manageConsensusCmds, manageReplyMsgs);
                // 重置链表和共识ID
                manageConsensusCmds = new ArrayList<>();
                manageReplyMsgs = new ArrayList<>();
                manageConsensusId = msgCtx.getConsensusId();
                manageConsensusCmds.add(commands[index]);
                manageReplyMsgs.add(replyList.get(index));
            }
            index++;
        }
        // 结束时，肯定有最后一个结块请求未处理
        if (!manageConsensusCmds.isEmpty()) {
            blockAndReply(manageConsensusCmds, manageReplyMsgs);
        }
        return null;
    }

    private void blockAndReply(List<byte[]> manageConsensusCmds, List<ReplyContextMessage> replyList) {
        String batchId = messageHandle.beginBatch(realmName);
        List<AsyncFuture<byte[]>> asyncFutureLinkedList = new ArrayList<>(manageConsensusCmds.size());
        try {
            int msgId = 0;
            for (byte[] txContent : manageConsensusCmds) {
                AsyncFuture<byte[]> asyncFuture = messageHandle.processOrdered(msgId++, txContent, realmName, batchId);
                asyncFutureLinkedList.add(asyncFuture);
            }
            messageHandle.completeBatch(realmName, batchId);
            messageHandle.commitBatch(realmName, batchId);
        } catch (Exception e) {
            // todo 需要处理应答码 404
            messageHandle.rollbackBatch(realmName, batchId, TransactionState.CONSENSUS_ERROR.CODE);
        }

        // 通知线程单独处理应答
        notifyReplyExecutors.execute(() -> {
            // 应答对应的结果
            int replyIndex = 0;
            for(ReplyContextMessage msg : replyList) {
                msg.setReply(asyncFutureLinkedList.get(replyIndex).get());
                TOMMessage request = msg.getTomMessage();
                ReplyContext replyContext = msg.getReplyContext();
                request.reply = new TOMMessage(replyContext.getId(), request.getSession(), request.getSequence(),
                        request.getOperationId(), msg.getReply(), replyContext.getCurrentViewId(),
                        request.getReqType());

                if (replyContext.getNumRepliers() > 0) {
                    bftsmart.tom.util.Logger.println("(ServiceReplica.receiveMessages) sending reply to "
                            + request.getSender() + " with sequence number " + request.getSequence()
                            + " and operation ID " + request.getOperationId() + " via ReplyManager");
                    replyContext.getRepMan().send(request);
                } else {
                    bftsmart.tom.util.Logger.println("(ServiceReplica.receiveMessages) sending reply to "
                            + request.getSender() + " with sequence number " + request.getSequence()
                            + " and operation ID " + request.getOperationId());
                    replyContext.getReplier().manageReply(request, msg.getMessageContext());
                }
                replyIndex++;
            }
        });
    }

    //notice
    public byte[] getSnapshot() {
        LOGGER.debug("------- GetSnapshot...[replica.id=" + this.getId() + "]");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BytesUtils.writeInt(stateHandles.size(), out);
        for (StateHandle stateHandle : stateHandles) {
            // TODO: 测试代码；
            return stateHandle.takeSnapshot();
        }
        return out.toByteArray();
    }

    public void installSnapshot(byte[] snapshot) {
//        System.out.println("Not implement!");
    }

    @Override
    public void start() {
        if (this.getId() < 0) {
            throw new IllegalStateException("Unset server node ID！");
        }
        LOGGER.debug("=============================== Start replica ===================================");

        if (status != Status.STOPPED) {
            return;
        }
        synchronized (mutex) {
            if (status != Status.STOPPED) {
                return;
            }
            status = Status.STARTING;

            try {
                LOGGER.debug("Start replica...[ID=" + getId() + "]");
                this.replica = new ServiceReplica(tomConfig, this, this);
                this.topology = new BftsmartTopology(replica.getReplicaContext().getCurrentView());
                status = Status.RUNNING;
//                createProxyClient();
                LOGGER.debug(
                        "=============================== Replica started success! ===================================");
            } catch (RuntimeException e) {
                status = Status.STOPPED;
                throw e;
            }
        }

    }

    @Override
    public void stop() {
        if (status != Status.RUNNING) {
            return;
        }
        synchronized (mutex) {
            if (status != Status.RUNNING) {
                return;
            }
            status = Status.STOPPING;

            try {
                ServiceReplica rep = this.replica;
                if (rep != null) {
                    LOGGER.debug("Stop replica...[ID=" + rep.getId() + "]");
                    this.replica = null;
                    this.topology = null;

                    rep.kill();
                    LOGGER.debug("Replica had stopped! --[ID=" + rep.getId() + "]");
                }
            } finally {
                status = Status.STOPPED;
            }
        }
    }
    
    enum Status {

        STARTING,

        RUNNING,

        STOPPING,

        STOPPED

    }

}
