package bg.softuni.blockchain.peers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Iterator;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;

import bg.softuni.blockchain.json.Block;
import bg.softuni.blockchain.json.BlockChain;
import bg.softuni.blockchain.json.Transaction;
import bg.softuni.blockchain.node.Wallet;

public class Faucet extends Node{

	private int nonce = 0;
	private String currentHash;
	private long currentTime;
	
	public Faucet(int totalNodes) throws IOException {
		super("faucet", 20001, totalNodes);
		Block genesis = generateGenesisBlock();
		this.blockchain = new BlockChain();
		verifyAndBroadcastMinedBlock(genesis);
	}
	
	
	private Block generateGenesisBlock(){
		Block genesis = new Block();
		genesis.setIndex(0);
		genesis.setDifficulty(NETWORK_DIFICULTY);
		genesis.setPrevBlockHash("");
		genesis.setMinedBy(this.wallet.getAddress());
		Transaction ico = this.wallet.sign(this.wallet.getAddress(), 1000000, 0);
		ico.setAddressFrom("");
		ico.setMinedInBlockIndex(1);
		ico.setPaid(true);
		genesis.getTransactions().add(ico);
		
		
		
		//Calculate block hash
		String blockData = String.valueOf(genesis.getIndex());
		
		for (Iterator iterator = genesis.getTransactions().iterator(); iterator.hasNext();) {
			Transaction tr = (Transaction) iterator.next();
			blockData += tr.toString();
		}
		
		blockData += genesis.getDifficulty();
		blockData += genesis.getPrevBlockHash();
		blockData += genesis.getMinedBy();
		
		MessageDigest sha256 = new SHA256.Digest();
		byte[] hashedData = sha256.digest(blockData.getBytes());
		
		String blockDataHash = Hex.toHexString(hashedData);
		
		
		//Mine the block
		boolean isBlockMined = false;
		while(isBlockMined == false){
			int dificulty = NETWORK_DIFICULTY;
			this.currentTime = System.currentTimeMillis();
			this.nonce++;
			byte[] hash = sha256.digest((blockDataHash + String.valueOf(currentTime) + String.valueOf(this.nonce)).getBytes());
			this.currentHash = Hex.toHexString(hash);
			if(this.currentHash.substring(0, dificulty).equals(new String(new char[dificulty]).replace('\0', '0'))){
				isBlockMined = true;
			}

		}

		genesis.setBlockHash(this.currentHash);
		genesis.setCreatedTimestamp(this.currentTime);
		genesis.setNonce(this.nonce);
		
		return genesis;
	}

}
