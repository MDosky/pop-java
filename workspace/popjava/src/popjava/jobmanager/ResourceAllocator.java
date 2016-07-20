package popjava.jobmanager;

import popjava.annotation.POPClass;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 * Define the method used to retrieve the next host to use for an object
 * it also need to receive the update from the JM about the status of the
 * services
 * @author Dosky
 */
@POPClass
public interface ResourceAllocator {
	ServiceConnector getNextHost(ObjectDescriptionInput od);
	void registerService(ServiceConnector service);
}
