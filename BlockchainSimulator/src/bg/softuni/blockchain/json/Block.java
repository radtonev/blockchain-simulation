package bg.softuni.blockchain.json;

import java.util.ArrayList;
import java.util.List;

public class Block {

	private int index;
	private List<Transaction> transactions;
	private int difficulty;
	private String prevBlockHash;
	private String minedBy;
	
	private int nonce;
	private long createdTimestamp;
	private String blockHash;
	
	public Block(){
		this.transactions = new ArrayList<Transaction>();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public String getPrevBlockHash() {
		return prevBlockHash;
	}

	public void setPrevBlockHash(String prevBlockHash) {
		this.prevBlockHash = prevBlockHash;
	}

	public String getMinedBy() {
		return minedBy;
	}

	public void setMinedBy(String minedBy) {
		this.minedBy = minedBy;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	@Override
	public String toString() {
		return "Block [index=" + index + ", transactions=" + transactions + ", difficulty=" + difficulty
				+ ", prevBlockHash=" + prevBlockHash + ", minedBy=" + minedBy + ", nonce=" + nonce
				+ ", createdTimestamp=" + createdTimestamp + ", blockHash=" + blockHash + "]";
	}

}
