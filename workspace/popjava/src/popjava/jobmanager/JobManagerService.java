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

	int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts);
	
	void exit();
}
