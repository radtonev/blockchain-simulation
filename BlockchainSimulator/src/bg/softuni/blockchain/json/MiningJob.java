package bg.softuni.blockchain.json;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;

public class MiningJob {
	
	private Block blockToMine;
	private int difficulty;
	
	public MiningJob(){
		
	}
	
	public MiningJob(Block block, Transaction reward, int difficulty){
		this.blockToMine = block;
		this.difficulty = difficulty;
	}
	
	
	public Block getBlockToMine() {
		return blockToMine;
	}
	public int getDifficulty() {
		return difficulty;
	}


	

	
}
