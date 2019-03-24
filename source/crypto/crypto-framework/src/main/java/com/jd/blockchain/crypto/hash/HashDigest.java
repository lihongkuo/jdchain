package com.jd.blockchain.crypto.hash;

import java.io.Serializable;

import com.jd.blockchain.crypto.CryptoAlgorithm;
import com.jd.blockchain.crypto.CryptoDigest;
import com.jd.blockchain.crypto.base.BaseCryptoBytes;

public class HashDigest extends BaseCryptoBytes implements CryptoDigest,Serializable {

	private static final long serialVersionUID = 693895170514236428L;

	public HashDigest(CryptoAlgorithm algorithm, byte[] rawDigestBytes) {
		super(algorithm, rawDigestBytes);
	}

	public HashDigest() {
		super();
	}

	public HashDigest(byte[] encodedDigestBytes) {
		super(encodedDigestBytes);
	}
	
	@Override
	protected boolean support(CryptoAlgorithm algorithm) {
		return algorithm.isHash();
	}

	@Override
	public byte[] getRawDigest() {
		return getRawCryptoBytes().getBytesCopy();
	}

}
