package popjava.jobmanager;

import popjava.dataswaper.IPOPBase;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 * Define the method used to retreive the next host to use for an object
 * it also need to receive the update from the JM about the status of the
 * services
 * @author Dosky
 */
public interface ResourceAllocator extends IPOPBase {
	ServiceConnector getNextHost(ObjectDescriptionInput od);
	void registerService(ServiceConnector service);
}
