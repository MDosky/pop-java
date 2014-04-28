package popjava.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Connector class for the POPJavaDeamon.
 * Connects to the remote deamon and starts a command
 * @author Beat Wolf
 *
 */
public class POPJavaDeamonConnector {

	private final Socket socket;
	
	public POPJavaDeamonConnector(String url) throws UnknownHostException, IOException{
		this(url, POPJavaDeamon.POP_JAVA_DEAMON_PORT);
	}
	
	public POPJavaDeamonConnector(String url, int port) throws UnknownHostException, IOException{
		socket = new Socket(url, port);
	}
	
	/**
	 * Sends a command to the remote deamon and closes the connection.
	 * @param command
	 * @throws IOException
	 */
	public void sendCommand(List<String> command) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		for(String part: command){
			writer.write(part+"\n");
		}
		
		writer.close();
	}
}
