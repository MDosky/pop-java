package popjava.jobmanager;

import popjava.baseobject.ConnectionType;
import popjava.dataswaper.IPOPBase;

/**
 *
 * @author Dosky
 */
public interface ServiceConnector extends IPOPBase {
	ConnectionType getConnectionType();
	String getSecret();
	String getHostname();
	int getServicePort();

	@Override
	public boolean equals(Object o);
	@Override
	public int hashCode();
	
}
