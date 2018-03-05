package bg.softuni.blockchain.peers.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import bg.softuni.blockchain.peers.Peer;

public class PeerAcceptor extends Thread implements Runnable{

	private static final int TOTAL_PEERS_TO_CONNECT = 3;
	
	private Peer target;
	private ServerSocket serverSocket;
	private int listeningPort;
	
	public PeerAcceptor(int portNumber, Peer node) throws IOException{
		this.serverSocket = new ServerSocket(portNumber);	
		this.listeningPort = portNumber;
		this.target = node;
	}
	
	@Override
	public void run() {
		while(true){
			//if(this.target.getConnectedPeersCount() < TOTAL_PEERS_TO_CONNECT){
				try {
					Socket connectedPeer = this.serverSocket.accept();
					//Do handshake
					OutputStream out = connectedPeer.getOutputStream();
					PrintWriter pw = new PrintWriter(out, true);
					pw.println(this.target.getNodeId());
					
					InputStream in = connectedPeer.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String peerId = br.readLine();
					
					this.target.addPeer(peerId, connectedPeer);
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			//}else{
				//Stay idle
			
			//}
		}
		
	}

}
