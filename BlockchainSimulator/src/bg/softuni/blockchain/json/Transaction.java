package bg.softuni.blockchain.json;

import java.util.Arrays;

public class Transaction {
	private String addressFrom;
	private String addressTo;
	private int amount;
	private int fee;
	private long createdTimestamp;
	private String senderPublicKey;
	private String[] senderSigniture;
	
	private String transactionHash;
	private int minedInBlockIndex;
	private int confirmations;
	
	private boolean paid;
	
	
	public String getAddressFrom() {
		return addressFrom;
	}
	public void setAddressFrom(String addressFrom) {
		this.addressFrom = addressFrom;
	}
	public String getAddressTo() {
		return addressTo;
	}
	public void setAddressTo(String addressTo) {
		this.addressTo = addressTo;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getFee() {
		return fee;
	}
	public void setFee(int fee) {
		this.fee = fee;
	}
	public long getCreatedTimestamp() {
		return createdTimestamp;
	}
	public void setCreatedTimestamp(long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	public String getSenderPublicKey() {
		return senderPublicKey;
	}
	public void setSenderPublicKey(String senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}
	public String[] getSenderSigniture() {
		return senderSigniture;
	}
	public void setSenderSigniture(String[] senderSigniture) {
		this.senderSigniture = senderSigniture;
	}
	public String getTransactionHash() {
		return transactionHash;
	}
	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}
	public int getMinedInBlockIndex() {
		return minedInBlockIndex;
	}
	public void setMinedInBlockIndex(int minedInBlockIndex) {
		this.minedInBlockIndex = minedInBlockIndex;
	}
	public int getConfirmations() {
		return confirmations;
	}
	public void setConfirmations(int confirmations) {
		this.confirmations = confirmations;
	}
	public boolean isPaid() {
		return paid;
	}
	public void setPaid(boolean paid) {
		this.paid = paid;
	}
	@Override
	public String toString() {
		return "Transaction [addressFrom=" + addressFrom + ", addressTo=" + addressTo + ", amount=" + amount + ", fee="
				+ fee + ", createdTimestamp=" + createdTimestamp + ", senderPublicKey=" + senderPublicKey
				+ ", senderSigniture=" + Arrays.toString(senderSigniture) + ", transactionHash=" + transactionHash
				+ ", minedInBlockIndex=" + minedInBlockIndex + ", confirmations=" + confirmations + ", paid=" + paid
				+ "]";
	}
	
	
	
}
