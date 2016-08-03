package popjava.jobmanager;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.BufferXDR;
import popjava.codemanager.AppService;
import popjava.codemanager.POPJavaAppService;
import popjava.combox.ComboxAllocateSocket;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.service.POPJavaDeamonConnector;
import static popjava.interfacebase.Interface.getAppcoreService;
import static popjava.interfacebase.Interface.getCodeFile;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.SystemUtil;
import popjava.util.Util;

/**
 * A centralized JobManager which use the POPJavaDeamon class to create
 * remote objects.
 * @author Dosky
 * @see Interface.java regarding the remote object creation
 */
@POPClass(classId = 99924, deconstructor = false, isDistributable = true)
public class POPJavaJobManager extends POPObject implements JobManagerService {
	
	// the allocator currently used
	private final ResourceAllocator allocator;
	// the allocator class, just in case
	private final Class allocatorClass;
	
	// running objects, map
	private final Map<Integer, AtomicInteger> runningObjects = Collections.synchronizedMap(new HashMap());

	// ObjectDescription used for creation
	private ObjectDescription nod;
	
	/**
	 * Don't use this. We need it since it's a POPClass.
	 */
	@POPObjectDescription(url = "localhost")
	public POPJavaJobManager() {
		allocator = null;
		allocatorClass = null;
	}
	
	/**
	 * Usually for a custom Allocator, the URL parameter is to set the port.
	 * The given class need to implement ResourceAllocator.
	 * @param url The url the JM will be created
	 * @param clazzString The implementation of ResourceAllocator
	 * @param pap The Access POint the the instantiated RA
	 * @throws POPException
	 * @throws ClassNotFoundException 
	 */
	public POPJavaJobManager(@POPConfig(POPConfig.Type.URL) String url, String clazzString, POPAccessPoint pap) throws POPException, ClassNotFoundException {
		this(clazzString, pap);
	}
	
	/**
	 * Custom allocator fixed on localhost.
	 * The given class need to implement ResourceAllocator.
	 * @param <T>
	 * @param clazzString The implementation of ResourceAllocator
	 * @param pap The Access POint the the instantiated RA
	 * @throws POPException
	 * @throws ClassNotFoundException 
	 */
	@POPObjectDescription(url = "localhost")
	public <T extends ResourceAllocator> POPJavaJobManager(String clazzString, POPAccessPoint pap) throws POPException, ClassNotFoundException {
		Class<T> clazz = (Class<T>) Class.forName(clazzString);
		allocator = PopJava.newActive(clazz, pap);
		allocatorClass = clazz;
	}

	/**
	 * Add a new daemon to the available ones
	 * @param service 
	 */
	@POPAsyncConc(id = 20)
    public void registerService(ServiceConnector service) {
		allocator.registerService(service);
	}
	
	/**
	 * Return the allocator class for identification
	 * @return 
	 */
	@POPSyncConc
	public Class getAllocatorClass() {
		return allocatorClass;
	}

	/**
	 * Return the allocator POPObject
	 * @return 
	 */
	@POPSyncConc
	public ResourceAllocator getAllocator() {
		return allocator;
	}

	@Override
	@POPAsyncConc
	public void signalCreateObject(int identifier) {
		// get counter
		AtomicInteger refs = runningObjects.getOrDefault(identifier, new AtomicInteger());
		// increment
		refs.incrementAndGet();
		
		// add to map if necessary
		if(runningObjects.containsKey(identifier))
			runningObjects.put(identifier, refs);
	}

	@Override
	@POPAsyncConc
	public void signalReleaseObject(int identifier) {
		// get counter
		AtomicInteger refs = runningObjects.get(identifier);
		if(refs == null)
			return;
		
		// decrement
		refs.decrementAndGet();
	}

	@Override
	@POPSyncConc
	public int objectReport(int identifier) {
		AtomicInteger refs = runningObjects.get(identifier);
		if(refs == null)
			return -1;
		return refs.get();
	}
	
	@Override
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od,
		int howmany, final @POPParameter(POPParameter.Direction.INOUT) POPAccessPoint[] objcontacts,
		int howmany2, final @POPParameter(POPParameter.Direction.INOUT) POPAccessPoint[] remotejobcontacts) {

		LogWriter.writeDebugInfo(String.format("[JM] Request for [%d] %s", howmany, objname));
		// skip if it's not a request
		if (howmany <= 0) {
			return 0;
		}
		
		// error if there is no allocator
		if (allocator == null)
			return 1;

		try {
			POPAccessPoint pap;
			ServiceConnector service;
			for (int i = 0; i < howmany; i++) {
				// connection info, random from pool
				service = allocator.getNextHost(od);
				// new od
				nod = POPSystem.getDefaultOD();
				// set daemon infromations
				nod.setConnectionType(service.getConnectionType());
				nod.setHostname(String.format("%s:%d", service.getHostname(), service.getServicePort()));
				nod.setConnectionSecret(service.getSecret());
				// out access point
				pap = new POPAccessPoint();
				// taken from Interface.java
				
				// use resource allocator
				createCmd(objname, pap);
				if(pap.isEmpty())
					throw new Exception("Failed to retreive AP");
				
				objcontacts[i] = pap;
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("Exception in JogMgr::CreateObject: %s", e.getMessage()));
			return POPErrorCode.POP_JOBSERVICE_FAIL;
		}

		return 0;
	}
	
	/**
	 * Try a local execution for the associated parallel object
	 *
	 * @param objectName	Name of the object
	 * @param accesspoint	Output parameter - Access point of the object
	 * @return true if the local execution succeed
	 * @throws POPException thrown if any exception occurred during the creation
	 * process
	 */
	private boolean createCmd(String objectName, POPAccessPoint accesspoint)
		throws POPException {
		//	String hostname = "";
		String joburl = "";
		String codeFile = "";
		// Get ClassName

		// Check if Od is empty
		boolean odEmpty = nod.isEmpty();
		if (odEmpty) {
			return false;
		}

		joburl = nod.getHostName();
		LogWriter.writeDebugInfo("Joburl " + joburl + " " + objectName);
		/*if (joburl == null || joburl.length() == 0 || !Util.sameContact(joburl, POPSystem.getHost())){
			return false;
		}*/

		if (joburl == null || joburl.length() == 0) {
			return false;
		}

		codeFile = nod.getCodeFile();

		// Host name existed
		if (codeFile == null || codeFile.length() == 0) {

			codeFile = getRemoteCodeFile(objectName);
			if (codeFile == null || codeFile.length() == 0) {
				return false;
			}
		}

		String rport = "";
		int index = joburl.lastIndexOf(":");
		if (index > 0) {
			rport = joburl.substring(index + 1);
			joburl = joburl.substring(0, index);
		}

		int status = remoteExec(joburl, codeFile, objectName, rport,
			POPSystem.appServiceAccessPoint, accesspoint);

		if (status != 0) {
			// Throw exception
			LogWriter.writeDebugInfo("Could not create " + objectName + " on " + joburl);
		}
		return (status == 0);
	}

	/**
	 * Lookup local code manager for the binary source....
	 *
	 * @param objectName
	 * @return
	 */
	private static String getRemoteCodeFile(String objectName) {
		if (objectName.equals(POPAppService.class.getName())
			|| objectName.equals(POPJavaAppService.class.getName())) {
			return getPOPCodeFile();
		}

		AppService appCoreService = getAppcoreService();

		if (appCoreService != null) {
			String codeFile = getCodeFile(appCoreService, objectName);
			return codeFile;
		}

		return getPOPCodeFile();
	}

	private static String getPOPCodeFile() {
		String popPath = POPJavaConfiguration.getClassPath();
		String popJar = POPJavaConfiguration.getPopJavaJar();

		return String.format(
				POPJavaConfiguration.getBrokerCommand(),
				popJar,
				popPath);
	}

	/**
	 * Launch a parallel object with a command
	 *
	 * @param hostname	Hostname to create the object
	 * @param codeFile	Path of the executable code file
	 * @param classname	Name of the Class of the parallel object
	 * @param rport	port
	 * @param jobserv	jobMgr service access point
	 * @param appserv	Application service access point
	 * @param objaccess	Output arguments - Access point to the object
	 * @return -1 if the local execution failed
	 */
	private int remoteExec(String hostname, String codeFile,
		String classname, String rport,
		POPAccessPoint appserv, POPAccessPoint objaccess) {

		if (codeFile == null || codeFile.length() == 0) {
			return -1;
		}
		codeFile = codeFile.trim();

		ArrayList<String> argvList = new ArrayList<String>();

		ArrayList<String> codeList = Util.splitTheCommand(codeFile);
		argvList.addAll(codeList);

		if (codeFile.startsWith("java") && Configuration.ACTIVATE_JMX) {
			argvList.add(1, "-Dcom.sun.management.jmxremote.port=" + (int) (Math.random() * 1000 + 3000));
			argvList.add(1, "-Dcom.sun.management.jmxremote.ssl=false");
			argvList.add(1, "-Dcom.sun.management.jmxremote.authenticate=false");
		}

		if (nod.getJVMParameters() != null && !nod.getJVMParameters().isEmpty()) {
			String[] jvmParameters = nod.getJVMParameters().split(" ");
			for (String parameter : jvmParameters) {
				argvList.add(1, parameter);
			}
		}

		ComboxAllocateSocket allocateCombox = new ComboxAllocateSocket();
		String callbackString = String.format(Broker.CALLBACK_PREFIX + "%s", allocateCombox
			.getUrl());
		argvList.add(callbackString);
		if (classname != null && classname.length() > 0) {
			String objectString = String.format(Broker.OBJECT_NAME_PREFIX + "%s", classname);
			argvList.add(objectString);
		}
		if (appserv != null && !appserv.isEmpty()) {
			String appString = String.format(Broker.APPSERVICE_PREFIX + "%s", appserv.toString());
			argvList.add(appString);
		}
		// always use this job manager for every object
		String jobString = String.format(Broker.JOBSERVICE_PREFIX + "%s", getAccessPoint().toString());
		argvList.add(jobString);
		
		// remove codelocation
		Util.removeStringFromList(argvList, "-codelocation");

		int ret = -1;

		switch (nod.getConnectionType()) {
			case ANY:
			case SSH:
				// different port for SSH setup
				if(!rport.isEmpty())
					hostname += ":" + rport;
				ret = SystemUtil.runRemoteCmd(hostname, argvList);
				break;
			case DEAMON:
				POPJavaDeamonConnector connector;
				try {
					// the port in this case is the daemon's not the one we want
					int port = Integer.parseInt(rport);
					connector = new POPJavaDeamonConnector(hostname, port);
					if (connector.sendCommand(nod.getConnectionSecret(), argvList)) {
						ret = 0;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		if (ret == -1) {
			return ret;
		}

		allocateCombox.startToAcceptOneConnection();

		if (!allocateCombox.isComboxConnected()) {
			LogWriter.writeDebugInfo("Could not connect broker");
			return -1;
		}

		BufferXDR buffer = new BufferXDR();
		int result = 0;

		if (allocateCombox.receive(buffer) > 0) {
			int status = buffer.getInt();
			String str = buffer.getString();
			
			if (status == 0) {
				objaccess.setAccessString(str);
			} else {
				result = status;
			}
		} else {
			result = -1;
		}
		allocateCombox.close();
		return result;
	}

}
