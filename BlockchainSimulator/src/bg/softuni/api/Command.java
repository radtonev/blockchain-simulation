package bg.softuni.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Command {
	private String nodeId;
	private String command;
	private List<String> params;
	private String stringEncoded;
	
	public Command(String input){
		//cmd -node1 -param1 -param2
		this.stringEncoded = input;
		String[] info = input.split(" -");
		this.command = info[0];
		this.nodeId = info[1];
		//System.out.println(info[0]);
		//System.out.println(info[1]);
		this.params = new ArrayList<String>();
		for (int i = 2; i < info.length; i++) {
			this.params.add(info[i]);
			//System.out.println(info[i]);
		}
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> param) {
		this.params = param;
	}

	public String toString(){
		return this.stringEncoded;
	}
}
