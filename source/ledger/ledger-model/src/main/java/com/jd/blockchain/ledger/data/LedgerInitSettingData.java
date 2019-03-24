package com.jd.blockchain.ledger.data;

import com.jd.blockchain.ledger.ParticipantNode;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.ledger.CryptoSetting;
import com.jd.blockchain.ledger.LedgerInitSetting;

public  class LedgerInitSettingData implements LedgerInitSetting {

	private byte[] ledgerSeed;

	private ParticipantNode[] consensusParticipants;

	private CryptoSetting cryptoSetting;
	
	private String consensusProvider;

	private Bytes consensusSettings;

	@Override
	public byte[] getLedgerSeed() {
		return ledgerSeed;
	}

	@Override
	public ParticipantNode[] getConsensusParticipants() {
		return consensusParticipants;
	}

	@Override
	public CryptoSetting getCryptoSetting() {
		return cryptoSetting;
	}

	@Override
	public Bytes getConsensusSettings() {
		return consensusSettings;
	}

	public void setLedgerSeed(byte[] ledgerSeed) {
		this.ledgerSeed = ledgerSeed;
	}

	public void setConsensusParticipants(ParticipantNode[] consensusParticipants) {
		this.consensusParticipants = consensusParticipants;
	}

	public void setCryptoSetting(CryptoSetting cryptoSetting) {
		this.cryptoSetting = cryptoSetting;
	}

	public void setConsensusSettings(Bytes consensusSettings) {
		this.consensusSettings = consensusSettings;
	}

	@Override
	public String getConsensusProvider() {
		return consensusProvider;
	}
	
	public void setConsensusProvider(String consensusProvider) {
		this.consensusProvider = consensusProvider;
	}

}