package popjava.service;

import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 *
 * @author Dosky
 */
public interface JobManagerService {

	int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts);
	
	void exit();
}
