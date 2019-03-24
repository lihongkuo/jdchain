package com.jd.blockchain.ledger.core;

import com.jd.blockchain.binaryproto.BinaryEncodingUtils;
import com.jd.blockchain.binaryproto.DataContractRegistry;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.ledger.ParticipantNode;
import com.jd.blockchain.ledger.CryptoSetting;
import com.jd.blockchain.storage.service.ExPolicyKVStorage;
import com.jd.blockchain.storage.service.VersioningKVStorage;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.Transactional;

public class ParticipantDataSet implements Transactional, MerkleProvable {

	static {
		DataContractRegistry.register(ParticipantNode.class);
	}

	private MerkleDataSet dataset;

	public ParticipantDataSet(CryptoSetting cryptoSetting, String prefix, ExPolicyKVStorage exPolicyStorage,
			VersioningKVStorage verStorage) {
		dataset = new MerkleDataSet(cryptoSetting, prefix, exPolicyStorage, verStorage);
	}

	public ParticipantDataSet(HashDigest merkleRootHash, CryptoSetting cryptoSetting, String prefix,
			ExPolicyKVStorage exPolicyStorage, VersioningKVStorage verStorage, boolean readonly) {
		dataset = new MerkleDataSet(merkleRootHash, cryptoSetting, prefix, exPolicyStorage, verStorage, readonly);
	}

	@Override
	public HashDigest getRootHash() {
		return dataset.getRootHash();
	}

	@Override
	public MerkleProof getProof(Bytes key) {
		return dataset.getProof(key);
	}

	@Override
	public boolean isUpdated() {
		return dataset.isUpdated();
	}

	@Override
	public void commit() {
		dataset.commit();
	}

	@Override
	public void cancel() {
		dataset.cancel();
	}

	public long getParticipantCount() {
		return dataset.getDataCount();
	}

	/**
	 * 加入新的共识参与方； <br>
	 * 如果指定的共识参与方已经存在，则引发 {@link LedgerException} 异常；
	 * 
	 * @param participant
	 */
	public void addConsensusParticipant(ParticipantNode participant) {
		Bytes key = encodeKey(participant.getAddress());
		byte[] participantBytes = BinaryEncodingUtils.encode(participant, ParticipantNode.class);
		long nv = dataset.setValue(key, participantBytes, -1);
		if (nv < 0) {
			throw new LedgerException("Participant already exist! --[id=" + key + "]");
		}
	}

	private Bytes encodeKey(String address) {
		// return id + "";
		return Bytes.fromString(address);
	}

	/**
	 * 返回指定地址的参与方凭证；
	 * 
	 * <br>
	 * 如果不存在，则返回 null；
	 * 
	 * @param address
	 * @return
	 */
	public ParticipantNode getParticipant(String address) {
		Bytes key = encodeKey(address);
		byte[] bytes = dataset.getValue(key);
		if (bytes == null) {
			return null;
		}
		return BinaryEncodingUtils.decode(bytes);
	}
	
	public ParticipantNode[] getParticipants() {
		byte[][] bytes = dataset.getLatestValues(0, (int)dataset.getDataCount());
		ParticipantNode[] pns = new ParticipantNode[bytes.length];
		
		for (int i = 0; i < pns.length; i++) {
			pns[i] = BinaryEncodingUtils.decode(bytes[i]);
		}
		return pns;
	}

}
