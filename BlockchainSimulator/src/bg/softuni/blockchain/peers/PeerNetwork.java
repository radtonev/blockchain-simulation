package bg.softuni.blockchain.peers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNetwork {
	
	private static final int MIN_PORT = 20002;
	private ConcurrentHashMap<String, Peer> nodes; 
	
	public PeerNetwork(){
		this.nodes = new ConcurrentHashMap<String, Peer>();
	}
	
	public void initNetwork(int totalPeers, int totalMiners) throws IOException, InterruptedException{
		
		for (int port = MIN_PORT; port < MIN_PORT+totalPeers; port++) {
			try {
				Peer node = new Node("node" + String.valueOf(port-MIN_PORT), port, totalPeers);
				nodes.put(String.valueOf(port-MIN_PORT), node);
				Thread.sleep(Math.round(Math.random()*150));
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Thread.sleep(1000);
		//Create genesis
		System.out.println("Creating genesis block");
		Faucet facet = new Faucet(100);
		System.out.println("Genesis block is mined!");
		Thread.sleep(2000);
		
		for (int i = 20150; i < 20150 + totalMiners; i++) {
			Miner m = new Miner("miner"+(i-20150), i, 100);
			m.startMining();
		}
		
		System.out.println("Network established!");
	}
	
	
	public void printNodeInfo(){
		for (Map.Entry<String, Peer> entry : this.nodes.entrySet())
		{
		    entry.getValue().printConnectedPeersCount();
		    System.out.println();
		}
	}
}
