package popjava.service;

import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPString;

/**
 *
 * @author Dosky
 */
public interface JobManagerService {
	
	void registerNode(String url);
	
	int query(POPString type, POPString value);

	int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts);
	
	boolean allocResource(String localservice, String objname,
			ObjectDescriptionInput od, int howmany, float[] fitness,
			POPAccessPoint[] jobcontacts, int[] reserveIDs, int[] requestInfo,
			int[] trace, int tracesize);
	
	void cancelReservation(int[] req, int howmany);
	
	int execObj(POPString objname, int howmany, int[] reserveIDs,
			String localservice, POPAccessPoint[] objcontacts);
	
	void dump();
	
	void start();
	
	void selfRegister();
}
