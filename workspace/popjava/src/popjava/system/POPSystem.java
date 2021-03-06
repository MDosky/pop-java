package popjava.system;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javassist.util.proxy.ProxyFactory;
import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.codemanager.AppService;
import popjava.combox.ComboxSocketFactory;
import popjava.service.jobmanager.POPJavaAppService;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.SystemUtil;
import popjava.util.Util;
import popjava.util.Util.OSType;

/**
 * This class is responsible for the initialization of a POP-Java application. It has also the responsibility to retrieve the configuration parameters.
 */
public class POPSystem {
	private static POPRemoteLogThread prlt;
	private static String platform = "linux";
	private static volatile boolean initialized = false;
	private static ExecutorService asyncConstructorExecutor = Executors.newFixedThreadPool(20);
	
	/**
	 * POP-Java location environement variable name
	 */
	public static final String POP_LOCATION_ENVIRONMENT_NAME = "POP_LOCATION";
	/**
	 * POP-Java Job service access point
	 */
	public static POPAccessPoint jobService = new POPAccessPoint();
	private static AppService coreServiceManager;
	private static POPJobService jobmanager;
	
	/**
	 * POP-Java application service access point 
	 */
	public static POPAccessPoint appServiceAccessPoint = new POPAccessPoint();
	
	public static void writeLog(String log){
		if(!Configuration.DEBUG){
			System.out.println(log);
		}
		LogWriter.writeDebugInfo(log);
		/*try {
			POPAppService app = (POPAppService)PopJava.newActive(POPAppService.class, POPSystem.AppServiceAccessPoint);
			if(app != null){
				app.logPJ(app.getPOPCAppID(), log);
			}else{
				System.out.println(log);
			}
			
		} catch (Exception e) {
			System.out.println(log);
			try{
				POPAppService app = (POPAppService)PopJava.newActive(POPJavaAppService.class, POPSystem.AppServiceAccessPoint);
				app.logPJ(app.getPOPCAppID(), log);
			} catch (POPException e2) {
				e2.printStackTrace();
			}
		}*/
	}

	static {
		// Trick :(( I don't know why the system i386 doesn't work
		String osName = System.getProperty("os.name");
		String osArchitect = System.getProperty("os.arch");
		
		if(osArchitect.contains("64")){
		    osArchitect = "x86_64";
		}
		
		platform = String.format("%s-%s", osArchitect, osName);
	}

	/**
	 * Creates a new instance of POPSystem
	 */
	public POPSystem() {
	}

	/**
	 * Retrieve the local IP address and format it as an int
	 * @return int value of the local IP address
	 */
	public static int getIPAsInt() {
		try {
			java.net.InetAddress localAddress = java.net.InetAddress
					.getLocalHost();
			byte[] ip = localAddress.getAddress();
			return ip[0] * 256 * 256 * 256 + ip[1] * 256 * 256 + ip[2] * 256
					+ ip[3];
		} catch (java.net.UnknownHostException e) {

		}
		return 127 * 256 * 256 * 256;
	}
	
	private static String getInterfaceIP(NetworkInterface ni){
		try {
			if(ni != null && ni.isUp()){
				Enumeration<InetAddress> enina = ni.getInetAddresses();
				for(InterfaceAddress interfaceAddress: ni.getInterfaceAddresses()){
					String address = interfaceAddress.getAddress().getHostAddress();
					
					if(!address.contains(":") && 
							!address.equals("127.0.0.1") &&
							!address.equals("127.0.1.1") &&
							!address.isEmpty() &&
							(Util.getOSType() == OSType.Windows || interfaceAddress.getAddress().isReachable(20))
							){
						return address;
					}
				}
				
			}
		} catch (IOException e) {
		}
		
		return null;
	}
	
	/**
	 * Get the host of the local node
	 * @return	Host name as a string value
	 */
	public static String getHostIP() {
		String preferedInterface = System.getenv("POPJ_IFACE");
		
		if(preferedInterface != null){
			try {
				NetworkInterface ni = NetworkInterface.getByName(preferedInterface);
				
				String ip = getInterfaceIP(ni);
				if(ip != null){
					return ip;
				}
			} catch (SocketException e) {
				LogWriter.writeExceptionLog(e);
			}
		}
		
		Enumeration<NetworkInterface> en;
		try {
			en = NetworkInterface.getNetworkInterfaces();
			while(en.hasMoreElements()){
				NetworkInterface ni = en.nextElement();
				String ip = getInterfaceIP(ni);
				if(ip != null){
					return ip;
				}
				
			}
		} catch (SocketException e) {
		}
		
		return "127.0.0.1";
	}

	/**
	 * Get the default local access point
	 * @return the default local access point
	 */
	public static POPAccessPoint getDefaultAccessPoint() {
		POPAccessPoint parrocAccessPoint = new POPAccessPoint();
		parrocAccessPoint.setAccessString(String.format("socket://%s://127.0.0.1:0", ComboxSocketFactory.PROTOCOL));
		return parrocAccessPoint;
	}

	/**
	 * Get the default object description
	 * @return	a new empty object description
	 */
	public static ObjectDescription getDefaultOD() {
		ObjectDescription od = new ObjectDescription();
		return od;
	}

	/**
	 * Get the local environment variable
	 * @param name	Name of the variable
	 * @return	Variable value or empty string
	 */
	public static String getEnviroment(String name) {
		String enviroment = System.getenv(name);
		if (enviroment == null)
			enviroment = "";
		else
			enviroment = enviroment.trim();
		return enviroment;
	}

	/**
	 * Get the system platform
	 * @return platform as a string value
	 */
	public static String getPlatform() {
		return platform;
	}

	private static String getNeededClasspath(){
		
		try{
			if(ProxyFactory.class.
					getProtectionDomain().getCodeSource().getLocation().
					getPath().equals(POPJavaConfiguration.getPopJavaJar())){
				return POPJavaConfiguration.getPopJavaJar();
			}
		}catch(Exception e){
		}
		
		return POPJavaConfiguration.getPOPJavaCodePath();
	}
	
	/**
	 * Entry point for the application scope initialization
	 * @param argvs	Any arguments to pass to the initialization
	 * @return	true if the initialization is succeed
	 * @throws POPException thrown is any problems occurred during the initialization
	 */
	public static String[] initialize(String ... args){
		asyncConstructorExecutor = Executors.newFixedThreadPool(20);
		ArrayList<String> argvList = new ArrayList<String>();
		
		for (String str : args){
			argvList.add(str);
		}
		
		initialize(argvList);
		
		return argvList.toArray(new String[0]);
	}
	
	private static boolean isStarted = false;
	
	public static void setStarted(){
	    isStarted = true;
	}
	
	public synchronized static boolean start(){
	    if(isStarted){
	        return true;
	    }
	    isStarted = true;
	    
	    jobService.setAccessString(jobservice);
        if (appservicecode == null || appservicecode.length() == 0) {
            appservicecode = POPJavaConfiguration.getPopAppCoreService();
        }
        
        if ((jobservice == null || jobservice.length() == 0) && (appservicecontact == null || appservicecontact.length() == 0)){
            return false;
        }
        
        coreServiceManager = getCoreService(proxy, appservicecontact, appservicecode);
        
        if(coreServiceManager != null){
            appServiceAccessPoint = coreServiceManager.getAccessPoint();
            
            prlt = new POPRemoteLogThread(coreServiceManager.getPOPCAppID());
            prlt.start();
        }
        
        if (codeconf == null || codeconf.isEmpty()) {
            codeconf = String.format("%s%setc%sdefaultobjectmap.xml",
                    POPJavaConfiguration.getPopJavaLocation(),
                    File.separator,
                    File.separator);
        }
        
        String popJavaObjectExecuteCommand = String.format(
                POPJavaConfiguration.getBrokerCommand(),
                POPJavaConfiguration.getPopJavaJar(),
                getNeededClasspath());
        
        if(Configuration.START_JOBMANAGER){
        	jobmanager = PopJava.newActive(POPJavaJobManager.class, "localhost:"+POPJobManager.DEFAULT_PORT);
        }
        
        initialized = initCodeService(codeconf, popJavaObjectExecuteCommand, coreServiceManager);
	    
        return initialized;
	}
    
    public static void registerCode(String file, String clazz){
        start();
        
        String popJavaObjectExecuteCommand = String.format(
                POPJavaConfiguration.getBrokerCommand(),
                POPJavaConfiguration.getPopJavaJar(),
                getNeededClasspath());
        
        if(coreServiceManager != null){
            coreServiceManager.registerCode(clazz, POPAppService.ALL_PLATFORMS, popJavaObjectExecuteCommand+file);
        }
    }    

	private static String jobservice = String.format("%s:%d", POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT);
	private static String codeconf;
	private static String appservicecode;
	private static String proxy;
	private static String appservicecontact;
	
	/**
	 * Initialize the application scope services 
	 * @param argvList	Any arguments to pass to the initialization
	 * @return	true if the initialization is succeed
	 * @throws POPException	thrown is any problems occurred during the initialization
	 */
	private static void initialize(List<String> argvList){
		String tempJobservice = Util.removeStringFromList(argvList, "-jobservice=");
		
		if (tempJobservice != null && !tempJobservice.isEmpty()) {
			jobservice = tempJobservice;
		}
		
		codeconf = Util.removeStringFromList(argvList, "-codeconf=");		
		appservicecode = Util.removeStringFromList(argvList, "-appservicecode=");
		proxy = Util.removeStringFromList(argvList, "-proxy=");
        appservicecontact = Util.removeStringFromList(argvList, "-appservicecontact=");
	}
	
	private static AppService getCoreService(String proxy, String appservicecontact, String appservicecode){
		LogWriter.printDebug("getCoreService "+proxy+ " "+appservicecontact+" "+appservicecode);
		
		if(appservicecontact == null || appservicecontact.length() == 0){
			
			String url = "";
			if (proxy == null || proxy.length() == 0) {
				url = appservicecode;
			} else {
				url = String.format("%s -proxy=%s", appservicecode, proxy);
			}
			
			if(Configuration.CONNECT_TO_POPCPP && 
			        (appservicecode.contains(" ") || new File(appservicecode).exists())){
				try{
					return createAppCoreService(url);
				}catch(POPException e){
					e.printStackTrace();
				}
			}
		} else {
			POPAccessPoint accessPoint = new POPAccessPoint();
			accessPoint.setAccessString(appservicecontact);
			try{
				return PopJava.newActive(POPAppService.class, accessPoint);
			}catch(POPException e){
			}
		}
		
		//Create a pure java AppService as a backup (probably no popc++ present)
		try{
			LogWriter.writeDebugInfo("Create appservice in Java");
			return PopJava.newActive(POPJavaAppService.class);
		}catch(POPException e){
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * Initialize the CodeMgr by reading the object map and register all code location
	 * @param fileconf			Object map file location
	 * @param appCoreService	Reference to the AppCoreService
	 * @return	true if the initialization is well done
	 * @throws POPException 
	 */
	public static boolean initCodeService(String fileconf,
			String popJavaObjectExecuteCommand,
			AppService appCoreService) throws POPException {
		fileconf = fileconf.trim();
		XMLWorker xw = new XMLWorker();
		
		if(!new File(POPJavaConfiguration.getPopJavaLocation()+"/etc/objectmap.xsd").exists()){
			LogWriter.printDebug("Could not open objectmap.xsd at "+POPJavaConfiguration.getPopJavaLocation());
			return false;
		}
		
		if(!xw.isValid(fileconf, POPJavaConfiguration.getPopJavaLocation()+"/etc/objectmap.xsd")){
			throw new POPException(0, "Object map not valid");
		}
		
		if (appCoreService == null || fileconf == null || fileconf.length() == 0){
			return false;
		}
		
		fileconf = fileconf.replaceAll("\"", "");
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(fileconf));
			// mainNode: <CodeInfoList>
			Element codeInfoListElement = document.getDocumentElement();
			// list: list of <CodeInfo> & text node
			NodeList list = codeInfoListElement.getChildNodes();
			for (int index = 0; index < list.getLength(); ++index) {
				// node: a <CodeInfo> node
				Node node = list.item(index);
				String objectname = "";
				String codefile = "";
				String platform = "";
				// Handle <CodeInfo> node only
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& node.getNodeName().equals("CodeInfo")) {
					Node childNode = node.getFirstChild();
					while (childNode != null) {
						if (childNode.getNodeType() == Node.ELEMENT_NODE) {

							if (childNode.getNodeName().equals("ObjectName")) {
								objectname = childNode.getTextContent();
							} else if (childNode.getNodeName().equals(
									"CodeFile")) {
								codefile = childNode.getTextContent();
								Element codeFileElement = (Element) childNode;
								String codeFileType = codeFileElement
										.getAttribute("Type");
								if (codeFileType != null
										&& codeFileType
												.equalsIgnoreCase("popjava")) {
									codefile = popJavaObjectExecuteCommand  + codefile;
								}
							} else if (childNode.getNodeName().equals(
									"PlatForm")) {
								platform = childNode.getTextContent();
							}
						}
						childNode = childNode.getNextSibling();
					}
					appCoreService.registerCode(objectname, platform, codefile);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Start the application scope services. This services is a POP-C++ parallel object.
	 * @param codelocation	location of the POP-C++ AppCoreService executable file
	 * @return	Interface of AppCoreService
	 * @throws POPException
	 */
	public static AppService createAppCoreService(String codelocation)
			throws POPException {
		Random random = new Random((new Date()).getTime());
		int maxUsignedByte = 255;
		byte[] bytes = new byte[255];
		random.nextBytes(bytes);
		String randString = "";
		for (int i = 0; i < 255; i++) {
			char randChar = (char) (((double) (bytes[i] + 128) / maxUsignedByte) * 25 + 97);
			randString += Character.toString(randChar);
		}

		ObjectDescription objectDescription = POPSystem.getDefaultOD();
		objectDescription.setHostname(POPSystem.getHostIP());
		objectDescription.setCodeFile(codelocation);
		
		POPAppService appService = PopJava.newActive(POPAppService.class, objectDescription, randString, false, codelocation);
				
		return appService;
	}
	
	public static void end(){
		LogWriter.writeDebugInfo("Shutting down POP-Java");
		waitForAsyncConstructors();
		
		asyncConstructorExecutor.shutdownNow();
		
		if(coreServiceManager != null){
			coreServiceManager.exit();
		}
		
		SystemUtil.endAllChildren();
		if(prlt != null){ //If initialize failed, prlt will be null
			try {
				Thread.sleep(1000); //TODO: this looks like a huge HACK
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			prlt.setRunning(false);
		}
		
		prlt = null;
		appservicecode = null;
		initialized = false;
	}
	
	public static boolean isInitialized(){
		return initialized;
	}
	
	public static void startAsyncConstructor(Runnable constructor){
		asyncConstructorExecutor.execute(constructor);
	}
	
	public static void waitForAsyncConstructors(){
		try {
			asyncConstructorExecutor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
