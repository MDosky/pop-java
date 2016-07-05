package popjava.service;

import java.io.IOException;
import java.net.ServerSocket;
import popjava.PopJava;
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
        args = POPSystem.initialize(args);
        
        JavaJobManager jobM;
        jobM = PopJava.newActive(JavaJobManager.class, (Object[]) args);
        
        POPSystem.end();
    }
    
    private void start() {
    }

    @Override
    public void run() {
        
    }
}
