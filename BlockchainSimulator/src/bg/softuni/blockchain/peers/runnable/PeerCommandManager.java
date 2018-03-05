package bg.softuni.blockchain.peers.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import bg.softuni.api.Command;
import bg.softuni.blockchain.peers.Node;
import bg.softuni.blockchain.peers.Peer;

public class PeerCommandManager extends Thread implements Runnable{

	private static final String MASTER_HOST = "localhost";
	private static final int MASTER_PORT = 20000;
	
	private Socket socket;
	private Peer target;
	
	public PeerCommandManager(Peer node) throws UnknownHostException, IOException{
		this.target = node;
		//Connect to master 
		this.socket = new Socket(MASTER_HOST,MASTER_PORT);
		//Send handshake to master 
		if(this.socket.isConnected()){
			OutputStream out = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(out, true);
			pw.println(this.target.getNodeId());
		}
		
	}

	@Override
	public void run() {
	
		while(true){

			try {
				InputStream in = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String input = br.readLine();
				Command cmd = new Command(input);
				
				((Node) this.target).executeAction(cmd);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		
	}
	
}
