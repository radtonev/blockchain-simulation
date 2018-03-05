package bg.softuni.blockchain.peers.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bg.softuni.blockchain.json.Block;
import bg.softuni.blockchain.json.BlockChain;
import bg.softuni.blockchain.json.MiningJob;
import bg.softuni.blockchain.json.Transaction;
import bg.softuni.blockchain.peers.Node;
import bg.softuni.blockchain.peers.Peer;

public class PeerCommunication extends Thread implements Runnable{

	private Node target;
	
	public PeerCommunication(Node p){
		this.target = p;
	}
	
	public void run(){
		
		ConcurrentHashMap<String, Socket> peers = null;
		/*
		//Request the full blockchain
				ConcurrentHashMap<String, Socket> peers = null;
				System.out.println(this.target.getConnectedPeers());
				while(this.target.getConnectedPeers().isEmpty()){
					peers = this.target.getConnectedPeers();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				Socket peerSocket = peers.entrySet().iterator().next().getValue();
				
				try {
					OutputStream out;
					out = peerSocket.getOutputStream();
					PrintWriter pw = new PrintWriter(out, true);
					pw.println("BlockChain");
					InputStream in = peerSocket.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String data = br.readLine();
					ObjectMapper mapper = new ObjectMapper();
					BlockChain chain = mapper.readValue(data, BlockChain.class);
					this.target.setBlockchain(chain);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		
		
		*/
		
		while(true){
			
			peers = this.target.getConnectedPeers();
			
			for (Map.Entry<String, Socket> entry : peers.entrySet())
			{
			    Socket socket = entry.getValue();
			    try {
					if(socket.getInputStream().available() > 0){
						InputStream in = socket.getInputStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String data = br.readLine();
						String response = acceptData(data);
						if(response != null){
							OutputStream out = socket.getOutputStream();
							PrintWriter pw = new PrintWriter(out, true);
							pw.println(response);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	private String acceptData(String data){
		ObjectMapper mapper = new ObjectMapper();
		if(data.startsWith("Block")){
			//New block
			data = data.substring(5);
			//System.out.println(data);
			try {
				Block block = mapper.readValue(data, Block.class);
				this.target.verifyAndBroadcastMinedBlock(block);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}else if(data.startsWith("Transaction")){
			//New transaction
			data = data.substring(11);
			try {
				Transaction trx = mapper.readValue(data, Transaction.class);
				this.target.addPendingTransaction(trx);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}else if(data.startsWith("BlockChain")){
			//someone requests the current blockchain
			try {
				if(this.target.getBlockchain() == null){
					this.target.setBlockchain(new BlockChain());
				}
				String json = mapper.writeValueAsString(this.target.getBlockchain());
				return json;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}else if(data.startsWith("MiningJob")){
			//someone requests new mining job
			try {
				data = data.substring(9);
				String json = mapper.writeValueAsString(this.target.createNewMiningJob(data));
				return "MiningJob" + json;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}else if(data.startsWith("MininedBlock")){
			//miner mined a block and submits it to peerb
			data = data.substring(12);
			try {
				Block block = mapper.readValue(data, Block.class);
				this.target.verifyAndBroadcastMinedBlock(block);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		return null;
	}
}
