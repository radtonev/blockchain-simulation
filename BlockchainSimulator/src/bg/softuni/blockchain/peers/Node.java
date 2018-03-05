package bg.softuni.blockchain.peers;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bg.softuni.api.Command;
import bg.softuni.blockchain.json.Block;
import bg.softuni.blockchain.json.BlockChain;
import bg.softuni.blockchain.json.MiningJob;
import bg.softuni.blockchain.json.Transaction;
import bg.softuni.blockchain.node.Wallet;
import bg.softuni.blockchain.peers.runnable.PeerCommandManager;
import bg.softuni.blockchain.peers.runnable.PeerCommunication;

public class Node extends Peer{

	public static int NETWORK_DIFICULTY = 4;
	
	private Thread commandManager; 
	
	private Thread nodeCommunication;
	protected Wallet wallet;
	protected BlockChain blockchain;
	private Map<String, Transaction> pendingTransactions;
	private MiningJob currentMiningJob;
	
	public Node(String id, int port, int totalNodes) throws IOException{
		super( id,  port, totalNodes);
		this.blockchain = new BlockChain();
		this.pendingTransactions = new HashMap<String,Transaction>();
		this.wallet = Wallet.createWallet();
		this.commandManager = new PeerCommandManager(this);
		this.commandManager.start();
		this.nodeCommunication = new PeerCommunication(this);
		this.nodeCommunication.start();
	}
	
	
	

	protected void sendTransaction(Transaction trx){
		this.pendingTransactions.put(this.wallet.getAddress(), trx);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(trx);
			broadcast("Transaction" + json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPendingTransaction(Transaction transaction){
		//Verify transaction
		if(!this.pendingTransactions.containsKey(transaction.getTransactionHash())){
			if(this.blockchain.getAddressBalance(transaction.getAddressFrom()) >= (transaction.getAmount() + transaction.getFee())){
				transaction.setPaid(true);
			}
			if(this.wallet.verify(transaction)){
				this.pendingTransactions.put(transaction.getTransactionHash(), transaction);
				ObjectMapper mapper = new ObjectMapper();
				try {
					String json = mapper.writeValueAsString(transaction);
					broadcast("Transaction" + json);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}	
	}

	public void verifyAndBroadcastMinedBlock(Block newBlock){
		
		String blockData = String.valueOf(newBlock.getIndex());
		
		for (Iterator iterator = newBlock.getTransactions().iterator(); iterator.hasNext();) {
			Transaction tr = (Transaction) iterator.next();
			blockData += tr.toString();
		}
		
		blockData += newBlock.getDifficulty();
		blockData += newBlock.getPrevBlockHash();
		blockData += newBlock.getMinedBy();

		MessageDigest sha256 = new SHA256.Digest();
		byte[] hashedBlockData = sha256.digest(blockData.getBytes());
		
		blockData = Hex.toHexString(hashedBlockData);
		blockData += String.valueOf(newBlock.getCreatedTimestamp());
		blockData += String.valueOf(newBlock.getNonce());
		
		
		byte[] hashedData = sha256.digest(blockData.getBytes());
		
		String blockHash = Hex.toHexString(hashedData);

		if(blockHash.substring(0, NETWORK_DIFICULTY).equals(new String(new char[NETWORK_DIFICULTY]).replace('\0', '0'))){
			 //BLOCK is valid
			if(newBlock.getIndex() == this.blockchain.getLastBlockIndex() + 1 || this.blockchain.getLastBlockIndex() == -1){
				clearVerifiedTransactions(newBlock);
				this.blockchain.getBlockChain().add(newBlock);
				//Broadcast new block
				ObjectMapper mapper = new ObjectMapper();
				String json;
				try {
					json = mapper.writeValueAsString(newBlock);
					broadcast("Block" + json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}else{
				//The block is old
			}
		}
	
	}
	
	private void clearVerifiedTransactions(Block block){
		List<Transaction> confirmedTransactions = block.getTransactions();
		for (Transaction transaction : confirmedTransactions) {
		   this.pendingTransactions.remove(transaction.getTransactionHash());
		}
	}
	
	public MiningJob createNewMiningJob(String minerAddress){
		Block block = new Block();
		block.setIndex(this.blockchain.getLastBlockIndex()+1);
		block.setDifficulty(NETWORK_DIFICULTY);
		block.setPrevBlockHash(this.blockchain.getLastBlockHash());
		block.setMinedBy(minerAddress);
		block.getTransactions().addAll(this.pendingTransactions.values());
		
		Transaction reward = new Transaction();
		//TODO create reward
		
		
		this.currentMiningJob = new MiningJob(block, reward, NETWORK_DIFICULTY);
		return this.currentMiningJob;
	}

	
	public BlockChain getBlockchain() {
		return blockchain;
	}

	
	public void setBlockchain(BlockChain blockchain) {
		this.blockchain = blockchain;
	}





	public void executeAction(Command cmd){
		
		switch(cmd.getCommand()){
			case "wallet":
				if(cmd.getParams().size() > 0){
					if(cmd.getParams().get(0).equals("new")){
						this.wallet = Wallet.createWallet();
					}else if(cmd.getParams().get(0).equals("restore")){
						String seed = cmd.getParams().get(1);
						this.wallet = Wallet.restoreWallet(this.blockchain, seed);
					}else if(cmd.getParams().get(0).equals("address")){
						System.out.println(this.wallet.getAddress());
					}else if(cmd.getParams().get(0).equals("seed")){
						System.out.println(this.wallet.getSeed());
					}else if(cmd.getParams().get(0).equals("balance")){

						System.out.println("Pending balance for " + this.wallet.getAddress() + " : " + this.wallet.getPendingBalance());
						System.out.println("Confirmed balance for " + this.wallet.getAddress() + " : " + this.wallet.getConfirmedBalance());
						
					}else if(cmd.getParams().get(0).equals("history")){
						
						for (Iterator iterator = this.wallet.getTransactionHistory().iterator(); iterator.hasNext();) {
							Transaction trx = (Transaction) iterator.next();
							System.out.println(trx.toString());
						}
					}else if(cmd.getParams().get(0).equals("send")){
						String addressTo = cmd.getParams().get(1);
						String amount = cmd.getParams().get(2);
						String fee = cmd.getParams().get(3);
						
						Transaction trx = this.wallet.sign(addressTo, Integer.parseInt(amount), Integer.parseInt(fee));
						sendTransaction(trx);

						System.out.println(trx.toString());
					}
					else if(cmd.getParams().get(0).equals("sync")){
						this.wallet.syncWallet(this.blockchain);
						System.out.println("Wallet synchronized with the current blockchain");
					}
					
				}
				break;
			case "blockchain":
				if(cmd.getParams().get(0).equals("size")){
					System.out.println(this.blockchain.getBlockChain().size() + " total blocks");
				}else if(cmd.getParams().get(0).equals("print")){
					List<Block> blocks = this.blockchain.getBlockChain();
					for (Iterator iterator = blocks.iterator(); iterator.hasNext();) {
						Block block = (Block) iterator.next();
						System.out.println(block.toString());
					}
				}else if(cmd.getParams().get(0).equals("balance")){
					String address = cmd.getParams().get(1);
					System.out.println(this.blockchain.getAddressBalance(address)); 
				}
				
				break;
			case "transactions":
				if(cmd.getParams().get(0).equals("size")){
					System.out.println(this.blockchain.getBlockChain().size() + " total blocks");
				}else if(cmd.getParams().get(0).equals("print")){
					for (Iterator iterator = this.pendingTransactions.entrySet().iterator(); iterator.hasNext();) {
						Transaction transaction = (Transaction) iterator.next();
						System.out.println(transaction.toString());
					}
				}
				
				break;
			case "peers":
				if(cmd.getParams().get(0).equals("size")){
					printConnectedPeersCount();
				}else if(cmd.getParams().get(0).equals("print")){
					printConnectedPeers();
				}
				break;
			
		}
		
	}

	
}
