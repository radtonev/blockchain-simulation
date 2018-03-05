package bg.softuni.blockchain.peers;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import bg.softuni.blockchain.node.Wallet;
import bg.softuni.blockchain.peers.runnable.MinerWorker;

/**
 * Connects to one node and starts mining
 * @author radostin-t
 *
 */
public class Miner extends Node{

	public static final int TIMESTAMP_TO_CHECK_FOR_NEW_BLOCK = 1000; 
	
	private String minerId;
	private MinerWorker mineWorker;
	
	private String connectedPeerId;
	private Socket connectedPeer;
	
	
	private int computingPower = 0; //Number between 1 and 100
	
	public Miner(String id, int port, int totalNodes) throws IOException{
		super(id,port,totalNodes);
		this.minerId = id;
	//	this.computingPower = computingPower;
		this.mineWorker = new MinerWorker(totalNodes, this);
		this.wallet = Wallet.createWallet();
	}
	
	
	public Wallet getWallet(){
		return this.wallet;
	}
	
	public String getMinerId() {
		return minerId;
	}


	public void setMinerId(String minerId) {
		this.minerId = minerId;
	}

	public void startMining(){
		this.mineWorker.start();
	}
	
	public void stopMining(){
		this.mineWorker.interrupt();
	}

	public void setConnectedPeer(String id, Socket socket){
		this.connectedPeerId = id;
		this.connectedPeer = socket;
	}
	public Socket getConnectedPeer(){
		return this.connectedPeer;
	}
	
}
