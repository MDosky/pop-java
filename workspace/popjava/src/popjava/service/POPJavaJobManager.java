package popjava.service;

import java.util.Arrays;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPString;

/**
 *
 * @author Dosky
 */
@POPClass(classId = 99924, deconstructor = false, isDistributable = true)
public class POPJavaJobManager extends POPObject implements JobManagerService {

    @POPObjectDescription(url = "localhost:2711")
    public POPJavaJobManager() {
    }

    @POPObjectDescription(url = "localhost:2711")
    public POPJavaJobManager(String challenge) {
    }

    @Override
    @POPSyncConc
    public int createObject(POPAccessPoint localservice, String objname,
            ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
        System.out.println(localservice);
        System.out.println(objname);
        System.out.println(od);
        System.out.println(howmany);
        System.out.println(Arrays.toString(objcontacts));
        System.out.println(howmany2);
        System.out.println(remotejobcontacts);
        return 0;
    }

	@Override
	public void registerNode(String url) {
		System.out.println("registerNode " + url);
	}

	@Override
	public int query(POPString type, POPString value) {
		System.out.println("query " + type + " " + value);
		return 0;
	}

	@Override
	public boolean allocResource(String localservice, String objname, ObjectDescriptionInput od, int howmany, float[] fitness, POPAccessPoint[] jobcontacts, int[] reserveIDs, int[] requestInfo, int[] trace, int tracesize) {
		System.out.println("allocResource " + localservice + " " + objname + " " + od);
		return true;
	}

	@Override
	public void cancelReservation(int[] req, int howmany) {
		System.out.println("cancelReservation " + req + " " + howmany);
	}

	@Override
	public int execObj(POPString objname, int howmany, int[] reserveIDs, String localservice, POPAccessPoint[] objcontacts) {
		System.out.println("execObj");
		return 0;
	}

	@Override
	public void dump() {
		System.out.println("dump");
	}

	@Override
	public void start() {
		System.out.println("start");
	}

	@Override
	public void selfRegister() {
		System.out.println("selfRegister");
	}
}
