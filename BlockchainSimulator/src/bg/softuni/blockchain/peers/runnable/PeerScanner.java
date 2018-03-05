package bg.softuni.blockchain.peers.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import bg.softuni.blockchain.peers.Peer;

public class PeerScanner extends Thread implements Runnable {

	private static final int TOTAL_PEERS_TO_CONNECT = 3;
	private static final int PORT_RANGE_MIN = 20001;
	private static final int TIMEOUT = 1000;
	
	private Peer target;
	private int totalNodes;
	
	public PeerScanner(int totalNodes, Peer node){
		this.target = node;
		this.totalNodes = totalNodes; //Used for port scanner
	}
	
	@Override
	public void run() {
		while(true){
			if(this.target.getConnectedPeers().keySet().size() < TOTAL_PEERS_TO_CONNECT){
				try {
					//SCAN:
				
					int i = (int) Math.round(PORT_RANGE_MIN + Math.random() * (totalNodes));
					
					if(i != this.target.getNodePort()){

							Socket socket = new Socket();
							socket.connect(new InetSocketAddress("localhost", i), TIMEOUT);
							if(socket.isConnected()){
								//TODO: Handshake
								OutputStream out = socket.getOutputStream();
								PrintWriter pw = new PrintWriter(out, true);
								pw.println(this.target.getNodeId());
								
								InputStream in = socket.getInputStream();
								BufferedReader br = new BufferedReader(new InputStreamReader(in));
								String peerId = br.readLine();
								
								this.target.addPeer(peerId, socket);
							}
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}else{
				//Stay idle
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
