package popjava.service.jobmanager.network;

import java.util.Objects;

/**
 * A SSH node for direct IP connections
 * XXX: port is not used ATM it will always default to port 22
 * @author Davide Mazzoleni
 */
public class NodeSSH extends NetworkNode {
	
	private String host;
	private int port;
	private boolean daemon;
	private boolean initialized = true;
	
	NodeSSH(String[] params) {
		if (params.length == 2 && params[1].equals("deamon"))
			daemon = true;
		
		// single string, can be <host>[:<port>] [deamon]
		if (params.length <= 2) {
			String[] ip = params[0].split(":");
			
			// simple ip or host
			if (ip.length == 1) {
				host = ip[0];
				port = 22;
			} 
			// port specified
			else {
				host = ip[0];
				try {
					port = Integer.parseInt(ip[1]);
				} catch (NumberFormatException e) {
					// fallback to 22
					port = 22;
				}
			}
		}
		
		else {
			// fail
			initialized = false;
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public boolean isDaemon() {
		return daemon;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.host);
		hash = 59 * hash + this.port;
		hash = 59 * hash + (this.daemon ? 1 : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NodeSSH other = (NodeSSH) obj;
		if (this.port != other.port) {
			return false;
		}
		if (this.daemon != other.daemon) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		return true;
	}
}
