package popjava.jobmanager;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
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
import popjava.service.DaemonInfo;
import popjava.service.POPJavaDeamon;
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
 * Start a centralized JobManager which use the POPJavaDeamon class to create
 * remote objects.
 */
@POPClass(classId = 99924, deconstructor = false, isDistributable = true)
public class POPJavaJobManager extends POPObject implements JobManagerService {

	private final List<DaemonInfo> daemons;
	private final int size;

	private ObjectDescription nod;
	private int current = 0;

	/**
	 * Instantiate a new JM
	 *
	 * @param args Every argument is a Daemon's description
	 * {password}@{hostname}:{port}
	 */
	public static void main(String[] args) {
		args = POPSystem.initialize(args);
		
		System.out.println(POPSystem.appServiceAccessPoint);

		// get list of daemons in arguments
		List<DaemonInfo> daemons = DaemonInfo.parse(args);

		System.out.println("[JM] Initilizing");
		final POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, daemons.toArray(new DaemonInfo[0]));
		System.out.println("[JM] Initialized");

		new Thread(new Runnable() {
			@Override
			public void run() {
				POPJavaJobManager thisJm = PopJava.newActive(POPJavaJobManager.class, jm.getAccessPoint());
				while (true) {
					System.out.println(thisJm.nop());
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						System.out.println("inter");
					}
				}
			}
		}).start();
	}

	@POPObjectDescription(url = "localhost:2711")
	public POPJavaJobManager() {
		this(new DaemonInfo[]{new DaemonInfo("localhost", "", POPJavaDeamon.POP_JAVA_DEAMON_PORT, 0)});
	}

	@POPObjectDescription(url = "localhost:2711")
	public POPJavaJobManager(DaemonInfo[] daemons) {
		this.daemons = Collections.unmodifiableList(Arrays.asList(daemons));
		this.size = daemons.length;
	}

	private long nop = Long.MIN_VALUE;

	@POPSyncSeq
	public long nop() {
		return nop++;
	}

	/**
	 * Return the next host to use. Right now it's a Round-robin but in future
	 * it could change
	 *
	 * @return
	 */
	private int getNextHost() {
		int c = current;
		current = (current + 1) % size;
		return c;
	}

	@Override
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
		ObjectDescriptionInput od,
		int howmany, final @POPParameter(POPParameter.Direction.INOUT) POPAccessPoint[] objcontacts,
		int howmany2, final @POPParameter(POPParameter.Direction.INOUT) POPAccessPoint[] remotejobcontacts) {

		System.out.println(String.format("%s\n  %s %s\n  %d %s\n  %d %s", localservice, objname, od, howmany, objcontacts, howmany2, remotejobcontacts));
		// skip if it's not a request
		if (howmany <= 0) {
			return 0;
		}

		try {
			POPAccessPoint pap;
			DaemonInfo di;
			for (int i = 0; i < howmany; i++) {
				// connection info, random from pool
				di = daemons.get(getNextHost());
				// new od
				nod = POPSystem.getDefaultOD();
				// set daemon infromations
				nod.setHostname(objname);
				nod.setHostname(String.format("%s:%d", di.getHostname(), di.getPort()));
				nod.setConnectionSecret(di.getPassword());
				nod.setConnectionType(ConnectionType.DEAMON);
				// out access point
				pap = new POPAccessPoint();
				// taken from Interface.java
				tryLocal(objname, pap);
				objcontacts[i] = pap;
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("Exception in JogMgr::CreateObject: %s", e.getMessage()));
			return POPErrorCode.POP_JOBSERVICE_FAIL;
		}

		System.out.println("PUT AP: " + Arrays.toString(objcontacts));

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
	private int localExec(String hostname, String codeFile,
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
		String jobString = String.format("-jobservice=%s", PopJava.getAccessPoint(this).toString());
		argvList.add(jobString);

		int ret = -1;

		switch (nod.getConnectionType()) {
			case ANY:
			case SSH:
				// let it choose the port only with SSH
				if (rport != null && rport.length() > 0) {
					String portString = String.format("-socket_port=%s", rport);
					argvList.add(portString);
				}
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

	@Override
	public void exit() {
	}

}
