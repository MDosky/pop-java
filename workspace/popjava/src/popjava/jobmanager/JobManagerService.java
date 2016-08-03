package popjava.jobmanager;

import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 * Abstraction of what does a JM need to do
 * Usually we only have two versions, the POPJobService and
 * POPJavaPobManager.
 * In both cases the JM need to be created in advance and 
 * @author Dosky
 */
public interface JobManagerService {

	/**
	 * Create a new remote object
	 * @param localservice
	 * @param objname
	 * @param od
	 * @param howmany
	 * @param objcontacts
	 * @param howmany2
	 * @param remotejobcontacts
	 * @return 
	 */
	int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts);
	
	/**
	 * From POPObject
	 */
	void exit();
	
	/**
	 * Increment the JM object counter
	 * @param identifier If used, the identifier of the machine
	 */
	void signalCreateObject(int identifier);
	/**
	 * Decrement the JM object counter
	 * @param identifier If used, the identifier of the machine
	 */
	void signalReleaseObject(int identifier);
	/**
	 * Consult the JM object counter
	 * @param identifier If used, the identifier of the machine
	 * @return How many objects are running on the machine
	 */
	int objectReport(int identifier);
}
