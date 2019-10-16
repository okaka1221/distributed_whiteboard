package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class ServerRunner implements Runnable  {
	private WhiteBoardServer server;
	private Map<String, Socket> sockets;
    private Socket currentSocket;
    private boolean isManger;
    private Socket managerSocket;
    private String name;
    private String op;
   
    public ServerRunner (WhiteBoardServer server, Socket currentSocket, boolean isManager)  {
        this.server = server;
    	this.currentSocket = currentSocket;
    	this.isManger = isManager;
    	
    	if(isManager) {
    		managerSocket = currentSocket;
    	}
    	
    	JSONObject canvasJson = server.getCanvasJson();
    	JSONObject chatboxJson = server.getChatboxJson();
    	
    	OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(currentSocket.getOutputStream(), "UTF-8");
			
			if (!canvasJson.isEmpty()) {
				writer.write(canvasJson.toString() + "\n");
				writer.flush();
	    	} 
			
			if (!chatboxJson.isEmpty()) {
				writer.write(chatboxJson.toString() + "\n");
				writer.flush();
	    	}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
   
    public void run()  {
		try {
			InputStream is = currentSocket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String line;
			while ((line = reader.readLine()) != null) {
				
				JSONObject json = new JSONObject(line);
				if (json.getString("header").length() > 0 && json.getString("body").length() > 0) {
					this.sockets = server.getSockets();
					
					if (json.getString("header").equals("canvas")) {
						server.setCanvasJson(json);
					} 
					
					if (json.getString("header").equals("chatbox")) {
						server.setChatboxJson(json);
					}
					
					if (json.getString("header").equals("Name")) {
						System.out.print("server successfully receive client name from client side: ");
						System.out.println(json.getString("body"));
					}
					
					if (json.getString("header").equals("Remove")) {
						String username = json.getString("body");       //remove socket and close socket						
						sockets.get(username).close();
						sockets.remove(username);
						System.out.println("gooooooooood");
					}
					
					if (json.getString("header").equals("close")) {
						System.out.print("server successfully receive empty: ");   //the close command will be distributed by server later
					}
					
					for (Socket s: sockets.values()) {
						OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream(), "UTF-8");
						writer.write(json.toString() + "\n");
						writer.flush();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}
}