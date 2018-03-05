package bg.softuni.api;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import bg.softuni.blockchain.peers.Faucet;
import bg.softuni.blockchain.peers.Miner;
import bg.softuni.blockchain.peers.Peer;
import bg.softuni.blockchain.peers.PeerNetwork;


public class ApplicationMain {

	
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			//Start master node
			MasterPeer master = new MasterPeer();
			master.start();
			
			//Init network
			System.out.println("Generating peers...");
			PeerNetwork network = new PeerNetwork();
			network.initNetwork(100, 10);

		
			//Accept commands
			Scanner scanner = new Scanner(System.in);
			String input = "";
			while(!input.equals("exit")){
				input = scanner.nextLine();
				if(input.length() >= 4){
					Command cmd = new Command(input);
					master.sendCommand(cmd);
				}
			}	
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	
	}

}
