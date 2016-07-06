package popjava.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import popjava.PopJava;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxServer;
import popjava.combox.ComboxSocketFactory;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.serviceadapter.POPJobManager;
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
