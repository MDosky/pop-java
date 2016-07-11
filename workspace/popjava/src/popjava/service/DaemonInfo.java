package popjava.service;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contain the credential of a specific POPJavaDeamon
 * its password, the port to connect to it and its name just in case.
 * These informations are going to be sent to the various YARN container and
 * processed via command line, so not much security is in place atm.
 * @author Dosky
 */
public class DaemonInfo {
    protected String hostname;
    protected String password;
    protected int port;

	/**
	 * Create a new named Daemon description
	 * @param hostname
	 * @param password
	 * @param port
	 * @param id 
	 */
    public DaemonInfo(String hostname, String password, int port) {
        this.hostname = hostname;
        this.password = password;
        this.port = port;
    }
	
	/**
	 * Create a generic Daemon by receiving only the password
	 * @param password 
	 */
	public DaemonInfo(String password) {
		this("localhost", password, 0);
	}
	
	/**
	 * Create a generic Daemon by receiving only the password
	 * @param password 
	 */
	public DaemonInfo(int port, String password) {
		this("localhost", password, port);
	}
	
	private DaemonInfo() {
	}

    public String getHostname() {
        return hostname;
    }

	protected void setHostname(String hostname) {
		this.hostname = hostname;
	}

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		if(password.isEmpty())
			return String.format("%s:%s", hostname, port);
		return String.format("%s@%s:%s", password, hostname, port);
	}
	
	/**
	 * Construct a DaemonInfo object from its toString representation
	 * @param arg
	 * @return 
	 */
	public static DaemonInfo fromString(String arg) {
		DaemonInfo di = new DaemonInfo();
		String[] args = arg.split(":|@");
		switch (args.length) {
			case 3:
				di.hostname = args[1];
				di.password = args[0];
				di.port = Integer.parseInt(args[2]);
				break;
			case 2:
				di.hostname = args[0];
				di.port = Integer.parseInt(args[1]);
				break;
			default:
				throw new IllegalArgumentException("DaemonInfo args don't match");
		}
		return di;
	}
	
	/**
	 * 
	 * @param args Console arguments
	 * @return A list of daemon we should be able to use
	 */
	public static List<DaemonInfo> parse(String... args) {
		List<DaemonInfo> daemons = new ArrayList<>();
		for(String arg : args) {
			try {
				daemons.add(DaemonInfo.fromString(arg));
			} catch(Exception e) {
				// we simply don't use the unknow and failed to parse arguments
			}
		}
		return daemons;
	}
}
