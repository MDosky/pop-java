package popjava.jobmanager;

import popjava.annotation.POPClass;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 * Define the method used to retrieve the next host to use for an object it also
 * need to receive the update from the JM about the status of the services
 *
 * @author Dosky
 */
@POPClass
public class ResourceAllocator {
	/**
	 * This method should return new next Service the object should
	 * connect to, an OD is given in case we want to handle its 
	 * parameters
	 * @param od
	 * @return 
	 */
	ServiceConnector getNextHost(ObjectDescriptionInput od) {
		return null;
	}
	
	/**
	 * This method register a service for latter use, the JM usually
	 * call this method
	 * @param service 
	 */
	void registerService(ServiceConnector service) {
	}
}
