package popjava.jobmanager;

import java.util.Objects;
import popjava.baseobject.ConnectionType;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 *
 * @author Dosky
 */
public class ServiceConnector implements IPOPBase {

	private String hostname;
	private String secret;
	private int servicePort;
	private ConnectionType connectionType;

	public ServiceConnector() {
		this("localhost");
	}

	public ServiceConnector(String secret) {
		this(secret, 22);
	}

	public ServiceConnector(String secret, int servicePort) {
		this(secret, servicePort, ConnectionType.SSH);
	}

	public ServiceConnector(String secret, int servicePort, ConnectionType connectionType) {
		this.hostname = "localhost";
		this.secret = secret;
		this.servicePort = servicePort;
		this.connectionType = connectionType;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
		
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null || o.getClass() != this.getClass())
			return false;
		ServiceConnector sc = (ServiceConnector) o;
		return hostname.equals(sc.hostname) && secret.equals(sc.secret) 
			&& servicePort == sc.servicePort && connectionType == sc.connectionType;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.hostname);
		hash = 37 * hash + Objects.hashCode(this.secret);
		hash = 37 * hash + this.servicePort;
		hash = 37 * hash + Objects.hashCode(this.connectionType);
		return hash;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(hostname);
		buffer.putString(secret);
		buffer.putInt(servicePort);
		buffer.putValue(connectionType, ConnectionType.class);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		hostname = buffer.getString();
		secret = buffer.getString();
		servicePort = buffer.getInt();
		connectionType = (ConnectionType) buffer.getValue(ConnectionType.class);
		return true;
	}

	@Override
	public String toString() {
		return String.format("[%s] %s@%s:%d", connectionType.toString(), secret, hostname, servicePort);
	}
}
