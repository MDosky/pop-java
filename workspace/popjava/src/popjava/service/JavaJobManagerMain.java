package popjava.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import popjava.PopJava;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.combox.ComboxServer;
import popjava.combox.ComboxSocketFactory;
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

	public JavaJobManagerMain(String... args) throws IOException {
		//jobServer = new ServerSocket(POPJobManager.DEFAULT_PORT);
		POPSystem.initialize(args);

		System.out.println("Initialized");

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				ComboxSocketFactory csf = new ComboxSocketFactory();
//				AccessPoint accessPoint = new AccessPoint(AccessPoint.SOCKET_PROTOCOL, AccessPoint.DEFAULT_HOST, POPJobManager.DEFAULT_PORT);
//				ComboxServer cs = csf.createServerCombox(accessPoint, null, Broker.getBroker());
//			}
//		}).start();

		System.out.println("Thread started");

		POPAccessPoint pap = new POPAccessPoint(String.format("%s://%s:%d", ComboxSocketFactory.PROTOCOL, AccessPoint.DEFAULT_HOST, POPJobManager.DEFAULT_PORT));

		System.out.println("AP Created");

		POPJavaJobManager jobM;
		jobM = PopJava.newActive(POPJavaJobManager.class);

		try {
			Thread.sleep(15000);
		} catch (InterruptedException ex) {
			Logger.getLogger(JavaJobManagerMain.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		POPSystem.end();
	}

	private void start() {
	}

	@Override
	public void run() {

	}
}
