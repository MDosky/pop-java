package popjava.jobmanager;

import popjava.baseobject.ConnectionType;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 *
 * @author Dosky
 */
public abstract class ServiceConnector implements IPOPBase {

	public ServiceConnector() {
	}
	
	public abstract ConnectionType getConnectionType();
	public abstract String getSecret();
	public abstract String getHostname();
	public abstract int getServicePort();
	
	@Override
	public abstract boolean equals(Object o);
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean serialize(POPBuffer buffer);

	@Override
	public abstract boolean deserialize(POPBuffer buffer);
}
