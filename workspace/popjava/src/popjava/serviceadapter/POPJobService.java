package popjava.serviceadapter;

import java.util.Arrays;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 10, className = "JobCoreService", deconstructor = true)
public class POPJobService extends POPServiceBase {
	
	/**
	 * Default constructor of POPJobService.
	 * Create a POP-C++ object JobCoreService
	 */
	@POPObjectDescription(id = 10)
	public POPJobService() {
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	@POPObjectDescription(id = 11)
	public POPJobService(String challenge) {

	}

	/**
	 * Ask the JobCoreService service to create a new parallel object
	 * @param localservice	Access to the local application scope services
	 * @param objname		Name of the object to create
	 * @param od			Object description for the resource requirements of this object
	 * @param howmany		Number of objects to create
	 * @param objcontacts	Output arguments - contacts to the objects created
	 * @return 0 if the object is created correctly
	 */
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
			ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		System.out.println(this.getClassName());
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
