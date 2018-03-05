package bg.softuni.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class MasterPeer extends Thread implements Runnable{

	private static final int MASTER_PORT = 20000;
	private ServerSocket serverSocket;
	
	private Map<String, Socket> connectedPeers;
	
	
	public MasterPeer() throws IOException{
		this.serverSocket = new ServerSocket(MASTER_PORT);
		this.connectedPeers = new HashMap<String,Socket>();
		System.out.println("Master is running on port " + MASTER_PORT);
	}

	@Override
	public void run() {
		while(true){
			//Accept all peers and send them commands
			try {
				Socket connectedPeer = this.serverSocket.accept();
				//Read handshake:
				InputStream in = connectedPeer.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String peerId = br.readLine();
				//System.out.println("-" + peerId + " is connected to master");
				this.connectedPeers.put(peerId, connectedPeer);
				
				
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void sendCommand(Command cmd) throws IOException{
		Socket peer = this.connectedPeers.get(cmd.getNodeId());
		if(peer != null){
			OutputStream out = peer.getOutputStream();
			PrintWriter pw = new PrintWriter(out, true);
			pw.println(cmd.toString());
		}else{
			System.out.println("Wrong peer name");
		}
	}
	
}
