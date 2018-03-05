package bg.softuni.blockchain.peers.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Iterator;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.jackson.databind.ObjectMapper;

import bg.softuni.blockchain.json.Block;
import bg.softuni.blockchain.json.MiningJob;
import bg.softuni.blockchain.json.Transaction;
import bg.softuni.blockchain.peers.Miner;
import bg.softuni.blockchain.peers.Peer;

public class MinerWorker extends Thread implements Runnable{
	
	private static final int PORT_RANGE_MIN = 20001;
	private static final int TIMEOUT = 1000;
	
	private Miner target;
	
	private long lastMiningJobRequestedTime = 0;
	
	private int nonce = 0;
	private String currentHash;
	private long currentTime;
	

	//Connect to one random node;
	public MinerWorker(int totalNodes, Miner miner) throws IOException{
		this.target = miner;
		boolean isConnected = false;
		while(!isConnected){
			int i = (int) Math.round(PORT_RANGE_MIN + Math.random() * (totalNodes));
	
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress("localhost", i), TIMEOUT);
			if(socket.isConnected()){
					//TODO: Handshake
					OutputStream out = socket.getOutputStream();
					PrintWriter pw = new PrintWriter(out, true);
					pw.println(this.target.getMinerId());
						
					InputStream in = socket.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String peerId = br.readLine();
					System.out.println(this.target.getMinerId() + " is connected to " + peerId);	
					this.target.setConnectedPeer(peerId, socket);
					isConnected=true;
			}
		}
	}
	
	public void run(){
		MiningJob job = null; 
		while(true){
			
			if((System.currentTimeMillis() - this.lastMiningJobRequestedTime) > Miner.TIMESTAMP_TO_CHECK_FOR_NEW_BLOCK){
				job = getMinigJobHash();
				this.lastMiningJobRequestedTime = System.currentTimeMillis();
			}
			boolean isBlockMined = mine(job);
			if(isBlockMined == true){
				//Send block to node
				Block newBlock = job.getBlockToMine();
				
				newBlock.setBlockHash(this.currentHash);
				newBlock.setCreatedTimestamp(this.currentTime);
				newBlock.setNonce(this.nonce);

				sendMinedBlockHash(newBlock);
			
				try {
					Thread.sleep((long)(5000 + Math.random()*10000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private MiningJob getMinigJobHash(){
		//Connect to node via socket to return a mining job hash
		Socket socket = this.target.getConnectedPeer();
		OutputStream out;
		try {
			String input = "";
			while(!input.startsWith("MiningJob")){
				out = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(out, true);
				pw.println("MiningJob"+this.target.getWallet().getAddress());
				InputStream in = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				input = br.readLine();
				
			}
			ObjectMapper mapper = new ObjectMapper();
			MiningJob job = mapper.readValue(input.substring(9), MiningJob.class);
			return job;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private boolean mine(MiningJob miningJob){
		String dataHash = calculateBlockDataHash(miningJob.getBlockToMine());
		int dificulty = miningJob.getDifficulty();
		this.currentTime = System.currentTimeMillis();
		this.nonce++;
		MessageDigest sha256 = new SHA256.Digest();
		byte[] hashedData = sha256.digest((dataHash + String.valueOf(currentTime) + String.valueOf(this.nonce)).getBytes());
		this.currentHash = Hex.toHexString(hashedData);
		if(this.currentHash.substring(0, dificulty).equals(new String(new char[dificulty]).replace('\0', '0'))){
			return true;
		}else{
			return false;
		}
	}
	
	private void sendMinedBlockHash(Block block){
		try {
			ObjectMapper mapper = new ObjectMapper();
			String blockString = mapper.writeValueAsString(block);
			//Connect to node and push him the calculated hash
			Socket socket = this.target.getConnectedPeer();
			OutputStream out;
			out = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(out, true);
			System.out.println(blockString);
			pw.println("MininedBlock"+blockString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String calculateBlockDataHash(Block blockToMine) {
		String blockData = String.valueOf(blockToMine.getIndex());
		
		for (Iterator iterator = blockToMine.getTransactions().iterator(); iterator.hasNext();) {
			Transaction tr = (Transaction) iterator.next();
			blockData += tr.toString();
		}
		
		blockData += blockToMine.getDifficulty();
		blockData += blockToMine.getPrevBlockHash();
		blockData += blockToMine.getMinedBy();
		
		MessageDigest sha256 = new SHA256.Digest();
		byte[] hashedData = sha256.digest(blockData.getBytes());
		
		String blockDataHash = Hex.toHexString(hashedData);
		return blockDataHash;
	}
	
}
