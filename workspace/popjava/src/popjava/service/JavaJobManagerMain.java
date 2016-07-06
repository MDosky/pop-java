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
		//jobServer = new ServerSocket(POPJobManager.DEFAULT_PORT);
		POPSystem.initialize(args);

		System.out.println("Initialized");

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				ComboxFactory cf = new ComboxSocketFactory();
//				AccessPoint accessPoint = new AccessPoint(AccessPoint.SOCKET_PROTOCOL, AccessPoint.DEFAULT_HOST, POPJobManager.DEFAULT_PORT);
//				ComboxServer cs = cf.createServerCombox(accessPoint, null, Broker.getBroker());
//			}
//		}).start();

//		System.out.println("Thread started");
//
//		pap = new POPAccessPoint(String.format("%s://%s:%d", AccessPoint.SOCKET_PROTOCOL, AccessPoint.DEFAULT_HOST, POPJobManager.DEFAULT_PORT));
//
//		System.out.println("AP Created");
//		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(10000);
//					int createObject = jobM.createObject(pap, "What?!", new ObjectDescriptionInput(), 1, new POPAccessPoint[1], 1, new POPAccessPoint[1]);
//					Thread.sleep(2000);
//					System.exit(0);
//				} catch (InterruptedException ex) {
//					Logger.getLogger(JavaJobManagerMain.class.getName()).log(Level.SEVERE, null, ex);
//				}
//			}
//		}).start();
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException ex) {
//			Logger.getLogger(JavaJobManagerMain.class.getName()).log(Level.SEVERE, null, ex);
//		}
		
		jobM = PopJava.newActive(POPJavaJobManager.class);

		
//		POPSystem.end();
	}

	private void start() {
	}

	@Override
	public void run() {

	}
}
