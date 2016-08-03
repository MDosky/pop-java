package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.jobmanager.JobManagerService;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 10, className = "JobCoreService", deconstructor = true)
public class POPJobService extends POPServiceBase implements JobManagerService {
	
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
		return 0;
	}

	@Override
	public void signalCreateObject(int identifier) {
	}

	@Override
	public void signalReleaseObject(int identifier) {
	}

	@Override
	public int objectReport(int identifier) {
		return -1;
	}



}
