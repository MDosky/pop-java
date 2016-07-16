package popjava.jobmanager;

import popjava.baseobject.ConnectionType;
import popjava.dataswaper.IPOPBase;

/**
 *
 * @author Dosky
 */
public abstract class ServiceConnector implements IPOPBase {
	public abstract ConnectionType getConnectionType();
	public abstract String getSecret();
	public abstract String getHostname();
	public abstract int getServicePort();
	
	public static ServiceConnector fromString() {
		return null;
	}

	@Override
	public abstract boolean equals(Object o);
	@Override
	public abstract int hashCode();
	
}
