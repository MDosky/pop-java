package popjava.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import popjava.PopJava;
import popjava.baseobject.AccessPoint;
import popjava.broker.Broker;
import popjava.combox.ComboxServer;
import popjava.combox.ComboxServerSocket;
import popjava.combox.ComboxSocketFactory;
import popjava.serviceadapter.POPJobManager;
import popjava.system.POPSystem;

/**
 * Central JobManager for Java based applications
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
        
        ComboxSocketFactory csf = new ComboxSocketFactory();
        AccessPoint accessPoint = new AccessPoint(AccessPoint.SOCKET_PROTOCOL, AccessPoint.DEFAULT_HOST, POPJobManager.DEFAULT_PORT);
        ComboxServer cs = csf.createServerCombox(accessPoint, null, Broker.getBroker());
        //JavaJobManager jobM;
        //jobM = PopJava.newActive(JavaJobManager.class, (Object[]) args);
        
        POPSystem.end();
    }
    
    private void start() {
    }

    @Override
    public void run() {
        
    }
}
