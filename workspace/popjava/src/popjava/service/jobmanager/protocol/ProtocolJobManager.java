package popjava.service.jobmanager.protocol;

import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.network.NetworkNode;
import popjava.service.jobmanager.network.NodeJobManager;

/**
 *
 * @author Davide Mazzoleni
 */
public class ProtocolJobManager extends CreateObjectProtocolBase {
	
	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isValidNode(NetworkNode node) {
		return node instanceof NodeJobManager && ((NodeJobManager)node).isInitialized();
	}

	
}
