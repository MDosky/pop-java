package popjava.service;

import java.util.Arrays;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncMutex;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;

/**
 *
 * @author Dosky
 */
@POPClass
public class JavaJobManager extends POPJobService {

    public JavaJobManager() {
    }

    public JavaJobManager(String challenge) {
    }

    @Override
    @POPSyncMutex
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
}
