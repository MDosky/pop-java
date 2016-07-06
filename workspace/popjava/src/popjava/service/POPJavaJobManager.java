package popjava.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ConnectionType;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.BufferXDR;
import popjava.codemanager.AppService;
import popjava.codemanager.POPJavaAppService;
import popjava.combox.ComboxAllocateSocket;
import popjava.dataswaper.ObjectDescriptionInput;
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
 *
 * @author Dosky
 */
@POPClass(classId = 99924, deconstructor = false, isDistributable = true)
public class POPJavaJobManager extends POPObject implements JobManagerService {

	private final List<DaemonInfo> daemons;
	private final int size;
	private final Random rnd;

	private ObjectDescription nod;

	@POPObjectDescription(url = "localhost:2711")
	public POPJavaJobManager() {
		this(Collections.singletonList(new DaemonInfo("localhost", "", POPJavaDeamon.POP_JAVA_DEAMON_PORT, 0)));
	}

	@POPObjectDescription(url = "localhost:2711")
	public POPJavaJobManager(List<DaemonInfo> daemons) {
		this.daemons = Collections.unmodifiableList(daemons);
		this.size = daemons.size();
		this.rnd = new SecureRandom();
	}

	@Override
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od,
		int howmany, POPAccessPoint[] objcontacts,
		int howmany2, POPAccessPoint[] remotejobcontacts) {
		
		System.out.println(String.format("%s %s %s %s %s %s %s", localservice, objname, od, howmany, 
		objcontacts, howmany2, remotejobcontacts));
		
		// skip if it's not a request
		if (howmany <= 0) {
			return 0;
		}
			
		// sanitize how many
		int n = 1;
		if (howmany > 0) {
			n = howmany;
		}

		System.out.println("Request received");
		System.out.println("My Daemons: " + Arrays.toString(daemons.toArray(new DaemonInfo[0])));

		try {
			objcontacts = new POPAccessPoint[n];
			POPAccessPoint pap;
			DaemonInfo di;
			for (int i = 0; i < n; i++) {
				System.out.println("Handling req " + (i + 1));
				// connection info, random from pool
				di = daemons.get(rnd.nextInt(size));
				// new od
				nod = POPSystem.getDefaultOD();
				// set daemon infromations
				nod.setHostname(objname);
				nod.setHostname(String.format("%s:%d", di.hostname, di.port));
				nod.setConnectionSecret(di.password);
				nod.setConnectionType(ConnectionType.DEAMON);
				// out access point
				pap = new POPAccessPoint();
				// taken from Interface.java
				tryLocal(objname, pap);
				objcontacts[i] = pap;
			}
		} catch(Exception e) {
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
	private boolean tryLocal(String objectName, POPAccessPoint accesspoint)
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

		int status = localExec(joburl, codeFile, objectName, rport,
			POPSystem.jobService, POPSystem.appServiceAccessPoint, accesspoint);

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
	private int localExec(String hostname, String codeFile,
		String classname, String rport, POPAccessPoint jobserv,
		POPAccessPoint appserv, POPAccessPoint objaccess) {

		boolean isLocal = Util.isLocal(hostname);
		/*if (!isLocal) {
			return -1;
		}*/
		if (codeFile == null || codeFile.length() == 0) {
			return -1;
		}
		codeFile = codeFile.trim();

		ArrayList<String> argvList = new ArrayList<String>();

		ArrayList<String> codeList = Util.splitTheCommand(codeFile);
		argvList.addAll(codeList);

		/*if(nod.getMemoryMin() >  0){
			argvList.add(1, "-Xms"+nod.getMemoryMin()+"m");
		}
		if(nod.getMemoryReq() >  0){
			argvList.add(1, "-Xmx"+nod.getMemoryReq()+"m");
		}*/
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
		if (jobserv != null && !jobserv.isEmpty()) {
			String jobString = String.format("-jobservice=%s", jobserv.toString());
			argvList.add(jobString);
		}

		if (rport != null && rport.length() > 0) {
			String portString = String.format("-socket_port=%s", rport);
			argvList.add(portString);
		}

		int ret = -1;

		//Allow local objects to be declared as remote to test remote object creation locally
		if (hostname.equals(POPObjectDescription.LOCAL_DEBUG_URL)) {
			hostname = "localhost";
		}

		if (isLocal) {
			ret = SystemUtil.runCmd(argvList);
		} else {
			switch (nod.getConnectionType()) {
				case ANY:
				case SSH:
					ret = SystemUtil.runRemoteCmd(hostname, argvList);
					break;
				case DEAMON:
					POPJavaDeamonConnector connector;
					try {
						if (rport == null || rport.isEmpty()) {
							connector = new POPJavaDeamonConnector(hostname);
						} else {
							int port = Integer.parseInt(rport);
							connector = new POPJavaDeamonConnector(hostname, port);
						}
						if (connector.sendCommand(nod.getConnectionSecret(), argvList)) {
							ret = 0;
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
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
