package bg.softuni.blockchain.peers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bg.softuni.blockchain.peers.runnable.PeerAcceptor;
import bg.softuni.blockchain.peers.runnable.PeerCommandManager;
import bg.softuni.blockchain.peers.runnable.PeerScanner;

public class Peer {

	private String nodeId;
	private int nodePort;
	private Thread nodeAcceptor;
	private Thread nodeScanner;
	
	private ConcurrentHashMap<String, Socket> connectedPeers;
	
	public Peer(String id, int port, int totalNodes) throws UnknownHostException, IOException{	
		this.nodeId = id;
		this.nodePort = port;
		this.connectedPeers = new ConcurrentHashMap<String, Socket>();
		this.nodeAcceptor = new PeerAcceptor(port, this);
		this.nodeScanner = new PeerScanner(totalNodes,this);
		
		
		this.nodeAcceptor.start();
		this.nodeScanner.start();
	}
	
	
	public synchronized void addPeer(String peerId, Socket socket){
		this.connectedPeers.put(peerId, socket);
	}
	
	public synchronized ConcurrentHashMap<String, Socket> getConnectedPeers(){
		return this.connectedPeers;
	}


	public String getNodeId() {
		return nodeId;
	}


	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}


	public int getNodePort() {
		return nodePort;
	}


	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}
	
	
	public synchronized void printConnectedPeersCount(){
		System.out.print(this.nodeId + "(Connected to " + this.connectedPeers.size() + " peers)");
	}
	public synchronized void printConnectedPeers(){
		System.out.print("Connected peers: [");
		for (Iterator iterator = this.connectedPeers.keySet().iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");
		}
		System.out.print("]");
		System.out.println();
	}

	
	public void broadcast(String data) throws IOException{
		for (Map.Entry<String, Socket> entry : connectedPeers.entrySet())
		{
			Socket socket = (Socket) entry.getValue();
			OutputStream out = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(out, true);
			pw.println(data);
		}
	}
	
}
