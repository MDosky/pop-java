package popjava.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import popjava.system.POPSystem;

/**
 * This glass gives some static method to deal with the system
 */

public class SystemUtil {
	
	private static List<Process> processes = new ArrayList<Process>();
	
	// setup unique identifier
	private static final int IDENTIFIER;
	static {
		// manually decided
		String var = System.getenv("POP_ID");
		// YARN specific
		if(var == null || var.isEmpty())
			var = System.getenv("CONTAINER_ID");
		// fallback to machine IP
		if(var == null || var.isEmpty())
			var = POPSystem.getHostIP();
		
		// integer identifier is the hash, it's always the same for strings
		IDENTIFIER = Objects.hashCode(var);
	}

	public static void endAllChildren(){
		for(int i = 0; i < processes.size(); i++){
			Process process = processes.get(i);
			if(process != null){
				process.destroy();
			}
		}
	}
	
	/**
	 * Return a unique identifier for the machine.
	 * It can be manually set by setting the environment variable POP_ID.
	 * When running inside Hadoop YARN the environment variable CONTAINER_ID is used.
	 * In case nothing has been manually set, the machine IP address is used.
	 * @return An unique integer representing the machine
	 */
	public static int machineIdentifier() {
		return IDENTIFIER;
	}
	
	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs) {
		long startTime = System.currentTimeMillis();
		LogWriter.writeDebugInfo("Run command");
		for(String arg: argvs){
			LogWriter.writeDebugInfo(arg);
		}
		
		// add java location to first argument
		if(argvs.size() > 0 && argvs.get(0).startsWith("java"))
			argvs.set(0, System.getProperty("java.home") + "/bin/" + argvs.get(0));
		
		ProcessBuilder pb = new ProcessBuilder(argvs);
		if(Configuration.REDIRECT_OUTPUT_TO_ROOT){
			pb = pb.inheritIO();
		}else{
			pb.redirectErrorStream(true);
			pb.redirectOutput(new File("/dev/null"));
		}
		
		if (pb != null) {
			try {
				String directory = System.getProperty("java.io.tmpdir");
				File currentDirectory = new File(directory);
				if (currentDirectory != null) {
					//pb.directory(currentDirectory);
				}
				processes.add(pb.start());
				LogWriter.writeDebugInfo("Started command after "+(System.currentTimeMillis() - startTime));
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static boolean commandExists(String command){
		try {
			Runtime.getRuntime().exec("ssh");
			return true;
		} catch (Exception e) {
		}
		
		return false;
	}
	
	public static int runRemoteCmdSSHJ(String url, List<String> command){
		int returnValue = -1;
		final SSHClient client = new SSHClient();
		LogWriter.writeDebugInfo("Connect to "+url+" using sshj");
		try {
			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(url);
			
			LogWriter.writeDebugInfo("Use user "+System.getProperty("user.name")+"for connection");
			client.authPublickey(System.getProperty("user.name"));

            final Session session = client.startSession();
            try{
            	String commandAsString = "";
                for(int i = 0; i < command.size(); i++){
                	commandAsString += command.get(i);
                	if(i < command.size() - 1){
                		commandAsString += " ";
                	}
                }
                LogWriter.writeDebugInfo("Run remote command");
                session.exec(commandAsString);
                returnValue = 0;
            }finally{
            	//session.close();
            }
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				client.disconnect();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return returnValue;
	}
	
	public static int runRemoteCmd(String url, List<String> command){
		if(Configuration.USE_NATIVE_SSH_IF_POSSIBLE && commandExists("ssh")){
			command.add(0, url);
			command.add(0, "ssh");
			
			return runCmd(command);
		}
		
		return runRemoteCmdSSHJ(url, command);
	}
}
