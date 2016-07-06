package popjava.service;

import java.util.Arrays;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;

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
    @POPSyncConc(id = 12)
    public int createObject(POPAccessPoint localservice, String objname,
            ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
        System.out.println(localservice);
        System.out.println(objname);
        System.out.println(od);
        System.out.println(howmany);
        System.out.println(Arrays.toString(objcontacts));
        System.out.println(howmany2);
        System.out.println(remotejobcontacts);
		
		for(int i = 0; i < howmany; i++)
			objcontacts[i] = new POPAccessPoint();
		
		for(int i = 0; i < howmany2; i++)
			remotejobcontacts[i] = new POPAccessPoint();
        return 0;
    }
}
