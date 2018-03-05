package bg.softuni.blockchain.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockChain {

	private List<Block> blockChain;
	
	public BlockChain(){
		this.blockChain = new ArrayList<Block>();
	}
	
	public void addBlock(Block block){
		this.blockChain.add(block);
	}
	public int getLastBlockIndex(){
		return this.blockChain.size()-1;
	}
	public String getLastBlockHash(){
		return this.blockChain.get(this.blockChain.size()-1).getBlockHash();
	}

	public List<Block> getBlockChain() {
		return blockChain;
	}

	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}
	
	
	public int getAddressBalance(String address){
		int confirmedBalance = 0;
		List<Block> blockChain = this.blockChain;
		for (int i = 0; i < blockChain.size(); i++) {
			Block block = blockChain.get(i);
			List<Transaction> transactions = block.getTransactions();
			for (Iterator iterator2 = transactions.iterator(); iterator2.hasNext();) {
				Transaction transaction = (Transaction) iterator2.next();
				if(transaction.isPaid()){
					if(transaction.getAddressFrom().equals(address)){
						//owner sent money
						if(i < blockChain.size()-7){
							//Transaction has 6 or more confirmations
							confirmedBalance -= transaction.getAmount();
							confirmedBalance -= transaction.getFee();
						}
					}
					if(transaction.getAddressTo().equals(address)){
						//owner recieved money
						if(i < blockChain.size()-7){
							//Transaction has 6 or more confirmations
							confirmedBalance += transaction.getAmount();
						}
					}
				}
			}
		}
		return confirmedBalance;
	}
	
}
