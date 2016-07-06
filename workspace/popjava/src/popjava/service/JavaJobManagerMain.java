package popjava.service;

import java.io.IOException;
import java.net.ServerSocket;
import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;

/**
 * Central JobManager for Java based applications
 *
 * @author Dosky
 */
public class JavaJobManagerMain implements Runnable {

	ServerSocket jobServer;

	public static void main(String[] args) throws IOException {
		JavaJobManagerMain main = new JavaJobManagerMain(args);
		main.start();
	}

	POPJavaJobManager jobM;
	POPAccessPoint pap;
	
	public JavaJobManagerMain(String... args) throws IOException {
		POPSystem.initialize(args);

		System.out.println("[JM] Initilizing");
		jobM = PopJava.newActive(POPJavaJobManager.class);
		System.out.println("[JM] Initialized");
		
	}

	private void start() {
	}

	@Override
	public void run() {

	}
}
