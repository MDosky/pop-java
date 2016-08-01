package popjava.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import popjava.baseobject.ConnectionType;
import popjava.jobmanager.ServiceConnector;

import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.SystemUtil;

/**
 * This class starts a deamon that listens to incoming POP requests.
 * It replaces the SSH service to start brokers.
 * @author Beat Wolf
 *
 */
public class POPJavaDeamon implements Runnable, Closeable{

	public static final String SUCCESS = "OK";
	public static final int POP_JAVA_DEAMON_PORT = 43424;
	private ServerSocket serverSocket;
	
	private static final String BACKUP_JAR = "build/jar/popjava.jar";
	
	private final ServiceConnector daemonInfo;

	public POPJavaDeamon(ServiceConnector daemonInfo) {
		this.daemonInfo = daemonInfo;
		this.daemonInfo.setConnectionType(ConnectionType.DEAMON);
		this.daemonInfo.setHostname(POPSystem.getHostIP());
	}
	
	public POPJavaDeamon(String password, int port){
		this(new ServiceConnector(password, port, ConnectionType.DEAMON));
	}
        
	public POPJavaDeamon(String password) {
		this(password, POP_JAVA_DEAMON_PORT);
	}
	
	/**
	 * Class that handles the accepted connections and runs the Broker with the provided paramters
	 * @author Beat Wolf
	 *
	 */
	private class Acceptor implements Runnable{
		
		private final Socket socket;
		private Random rand = new Random();
		private static final int SALT_LENGTH = 10;
		
		public Acceptor(Socket socket){
			this.socket = socket;
		}
		
		private String createSecret(){
			String secret = "";
			
			for(int i = 0;i < SALT_LENGTH; i++){
				secret += (char)(rand.nextInt(26) + 'a');
			}
			
			return secret;
		}
		
		@Override
		public void run() {
			
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String salt = createSecret();
				
				//Send salt to connectin client
				writer.write(salt+"\n");
				writer.flush();
				
				List<String> commands = new ArrayList<String>();
				
				System.err.println("Execute command: ");
				
				
				String saltedHash = getSaltedHash(salt, daemonInfo.getSecret());
				
				//Read command to execute
				String challengeAnswer = reader.readLine();
				if(!saltedHash.equals(challengeAnswer)){
					System.err.println("The supplied secret was wrong : "+challengeAnswer+" should be "+saltedHash+" using password "+daemonInfo.getSecret());
					writer.write("ERROR PASS\n");
					writer.close();
					reader.close();
					return;
				}
				
				int commandLength = Integer.parseInt(reader.readLine());
				
				boolean isJava = false;
				boolean isClassPath = false;
				for(int i = 0; i < commandLength; i++){
					String line = reader.readLine();
					//If the current parameter is the classpath, modify it to fit local system
					if(isJava && isClassPath){ 
						if(!isClasspathValid(line)){
							String temp = POPJavaConfiguration.getClassPath();
							if(temp != null && !temp.isEmpty()){
								line = temp;
							}
						}
					}
					
					if(line.startsWith("-javaagent:")){
						String popJavaJar = POPJavaConfiguration.getPOPJavaCodePath();
						if(!POPJavaConfiguration.isJar()){
							popJavaJar = BACKUP_JAR;
						}
						line = "-javaagent:"+popJavaJar;
					}
					
					commands.add(line);

					if(i == 0 && line.equals("java")){
						line = System.getProperty("java.home") + "/bin/java";
						isJava = true;
					}
					
					isClassPath = line.equals("-cp");
					
					System.err.print(line+" ");
				}
				System.err.println();
				
				//Execute command
				if(isJava){
					SystemUtil.runCmd(commands);
					writer.write(SUCCESS+"\n");
					writer.flush();
				}else{
					writer.write("ERROR NOT JAVA\n");
					writer.flush();
				}
				
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean isClasspathValid(String classPath){
	    
	    String [] parts = classPath.split(File.pathSeparator);
	    
	    for(String part: parts){
	        if(new File(part).exists() || part.startsWith("http://")){
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	public static void main(String ... args) throws IOException{
		POPJavaDeamon deamon = new POPJavaDeamon("test");
		deamon.start();
	}
	
	/**
	 * Starts the POP-Java listener deamon
	 * @throws IOException
	 */
	public void start() throws IOException{
		serverSocket = new ServerSocket(daemonInfo.getServicePort());
		
		// if port was 0, save and propage to references the real port
		if(daemonInfo.getServicePort() == 0)
			daemonInfo.setServicePort(serverSocket.getLocalPort());
		
		Executor executor = Executors.newCachedThreadPool();
		
		System.err.println("Started POP-Java deamon");
		
		try{
    		while(!Thread.currentThread().isInterrupted()){
		        Socket socket = serverSocket.accept();
	            System.err.println("Accepted connection");
	            executor.execute(new Acceptor(socket));
    		}
		}catch(IOException e){
        }
		
		if(serverSocket != null){
		    serverSocket.close();
		}
		
		System.err.println("Closed POP-Java deamon");
	}
	
	/**
	 * Stops the POP-Java listener deamon
	 * @throws IOException 
	 */
	@Override
    public synchronized void close() throws IOException{
		if(serverSocket != null){
			serverSocket.close();
			serverSocket = null;
		}
	}
	
	public static String getSaltedHash(String salt, String secret){
		return ""+(secret + salt).hashCode();
	}

	@Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
