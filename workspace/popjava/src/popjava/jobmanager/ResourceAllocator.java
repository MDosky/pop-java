package popjava.jobmanager;

import popjava.dataswaper.ObjectDescriptionInput;

/**
 * Define the method used to retrieve the next host to use for an object it also
 * need to receive the update from the JM about the status of the services
 *
 * @author Dosky
 */
public interface ResourceAllocator {
	/**
	 * This method should return new next Service the object should
	 * connect to, an OD is given in case we want to handle its 
	 * parameters
	 * @param od
	 * @return 
	 * */
	ServiceConnector getNextHost(ObjectDescriptionInput od);
	
	/**
	 * This method register a service for latter use, the JM usually
	 * call this method
	 * @param service 
	 */
	void registerService(ServiceConnector service);
}
