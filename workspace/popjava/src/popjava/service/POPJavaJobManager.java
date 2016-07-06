package popjava.service;

import java.util.Arrays;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 *
 * @author Dosky
 */
@POPClass(classId = 99924, deconstructor = false)
public class POPJavaJobManager extends POPObject {

    @POPObjectDescription(url = "localhost")
    public POPJavaJobManager() {
    }

    @POPObjectDescription(url = "localhost")
    public POPJavaJobManager(String challenge) {
    }

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
