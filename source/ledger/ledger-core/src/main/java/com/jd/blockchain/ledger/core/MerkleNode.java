package com.jd.blockchain.ledger.core;

import com.jd.blockchain.crypto.hash.HashDigest;

public interface MerkleNode {

	HashDigest getNodeHash();

	/**
	 * 节点的深度；
	 *
	 * 叶子节点的深度为 0；每一级父节点的深度加 1 ；
	 *
	 * @return
	 */
	int getLevel();

}