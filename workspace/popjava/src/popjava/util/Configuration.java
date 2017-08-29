package popjava.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import popjava.baseobject.ConnectionProtocol;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.util.ssl.KeyStoreOptions;
import popjava.util.ssl.KeyStoreOptions.KeyStoreFormat;

/**
 * This class regroup some configuration values
 */
public final class Configuration {
	
	/**
	 * Settable parameters for load and store options
	 */
	private enum Settable {
		SYSTEM_JOBMANAGER_CONFIG,
		DEBUG,
		DEBUG_COMBOBOX,
		RESERVE_TIMEOUT,
		ALLOC_TIMEOUT,
		CONNECTION_TIMEOUT,
		JOBMANAGER_UPDATE_INTERVAL,
		JOBMANAGER_SELF_REGISTER_INTERVAL,
		JOBMANAGER_DEFAULT_CONNECTOR,
		SEARCH_NODE_UNLOCK_TIMEOUT,
		SEARCH_NODE_SEARCH_TIMEOUT,
		SEARCH_NODE_MAX_REQUESTS,
		SEARCH_NODE_EXPLORATION_QUEUE_SIZE,
		TFC_SEARCH_TIMEOUT,
		DEFAULT_ENCODING,
		SELECTED_ENCODING,
		DEFAULT_PROTOCOL,
		ASYNC_CONSTRUCTOR,
		ACTIVATE_JMX,
		CONNECT_TO_POPCPP,
		CONNECT_TO_JAVA_JOBMANAGER,
		REDIRECT_OUTPUT_TO_ROOT,
		USE_NATIVE_SSH_IF_POSSIBLE,
		SSL_PROTOCOL_VERSION,
		SSL_KEY_STORE_FILE,
		SSL_KEY_STORE_PASSWORD,
		SSL_KEY_STORE_PRIVATE_KEY_PASSWORD,
		SSL_KEY_STORE_LOCAL_ALIAS,
		SSL_KEY_STORE_FORMAT,
		SSL_KEY_STORE_TEMP_LOCATION,
	}
	
	// instance
	private static Configuration instance;
	
	// Location of POPJava installation
	private static final String POPJAVA_LOCATION;
	static {
		String env = System.getenv("POPJAVA_LOCATION");
		if (Objects.isNull(env)) {
			POPJAVA_LOCATION = "./";
		} else {
			POPJAVA_LOCATION = env;
		}
	}
	
	// config files
	private static final File SYSTEM_CONFIG	     = Paths.get(POPJAVA_LOCATION, "etc", "popjava.properties").toFile();
	private File systemJobManagerConfig   = Paths.get(POPJAVA_LOCATION, "etc", "jobmgr.conf").toFile();
	
	// properties set by the user are found here
	private File userConfig = null;
	private boolean usingUserConfig = false;
	private final Properties USER_PROPERTIES = new Properties();
	private final Properties ALL_PROPERTIES = new Properties();
	
	// user configurable attributes w/ POP's defaults
	private boolean debug = true;
	private boolean debugCombox = false;
	private int reserveTimeout = 60000;
	private int allocTimeout = 30000;
	private int connectionTimeout = 30000;
	
	private int jobManagerUpdateInterval = 10000;
	private int jobManagerSelfRegisterInterval = 43_200_000;
	private String jobManagerDefaultConnector = POPConnectorJobManager.IDENTITY;
	private int searchNodeUnlockTimeout = 10000;
	private int searchNodeSearchTimeout = 0;
	private int tfcSearchTimeout = 5000;
	private int searchNodeUnlimitedHops = Integer.MAX_VALUE;
	private int searchNodeMaxRequests = 300;
	private int searchNodeExplorationQueueSize = 300;
	
	private String defaultEncoding = "xdr";
	private String selectedEncoding = "raw";
	private String defaultProtocol = ConnectionProtocol.SOCKET.getName();

	private boolean asyncConstructor = true;
	private boolean activateJmx = false;
	private boolean connectToPOPcpp = false;
	private boolean connectToJavaJobmanager = !connectToPOPcpp;
	
	private boolean redirectOutputToRoot = true;
	private boolean useNativeSSHifPossible = true;
	
	// all relevant information of the keystore (alias, keyStorePassword, privateKeyPassword, keyStoreLocation, keyStoreType, temporaryCertificatesDir)
	private KeyStoreOptions SSLKeyStoreOptions = new KeyStoreOptions();
	
	// NOTE this is waiting for TLSv1.3 to be officialized
	private String SSLProtocolVersion = "TLSv1.2";

	/**
	 * This is a singleton
	 */
	private Configuration() {
		try {
			load(SYSTEM_CONFIG);
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[Configuration] couldn't load '%s' using POP-Java's defaults.", SYSTEM_CONFIG);
		}
	}

	public static Configuration getInstance() {
		if (Objects.isNull(instance)) {
			instance = new Configuration();
		}
		return instance;
	}

	public String getPOPJAVA_LOCATION() {
		return POPJAVA_LOCATION;
	}

	public File getSystemJobManagerConfig() {
		return systemJobManagerConfig;
	}

	public File getUserConfig() {
		return userConfig;
	}

	public boolean isUsingUserConfig() {
		return usingUserConfig;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isDebugCombox() {
		return debugCombox;
	}

	public int getReserveTimeout() {
		return reserveTimeout;
	}

	public int getAllocTimeout() {
		return allocTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getJobManagerUpdateInterval() {
		return jobManagerUpdateInterval;
	}

	public int getJobManagerSelfRegisterInterval() {
		return jobManagerSelfRegisterInterval;
	}

	public String getJobManagerDefaultConnector() {
		return jobManagerDefaultConnector;
	}

	public int getSearchNodeUnlockTimeout() {
		return searchNodeUnlockTimeout;
	}

	public int getSearchNodeSearchTimeout() {
		return searchNodeSearchTimeout;
	}

	public int getTFCSearchTimeout() {
		return tfcSearchTimeout;
	}

	public int getSearchNodeUnlimitedHops() {
		return searchNodeUnlimitedHops;
	}

	public int getSearchNodeMaxRequests() {
		return searchNodeMaxRequests;
	}

	public int getSearchNodeExplorationQueueSize() {
		return searchNodeExplorationQueueSize;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public String getSelectedEncoding() {
		return selectedEncoding;
	}

	public String getDefaultProtocol() {
		return defaultProtocol;
	}

	public boolean isAsyncConstructor() {
		return asyncConstructor;
	}

	public boolean isActivateJmx() {
		return activateJmx;
	}

	public boolean isConnectToPOPcpp() {
		return connectToPOPcpp;
	}

	public boolean isConnectToJavaJobmanager() {
		return connectToJavaJobmanager;
	}

	public boolean isRedirectOutputToRoot() {
		return redirectOutputToRoot;
	}

	public boolean isUseNativeSSHifPossible() {
		return useNativeSSHifPossible;
	}

	public KeyStoreOptions getSSLKeyStoreOptions() {
		return new KeyStoreOptions(SSLKeyStoreOptions);
	}

	public String getSSLProtocolVersion() {
		return SSLProtocolVersion;
	}
	
	public File getKeyStoreFile() {
		return new File(SSLKeyStoreOptions.getKeyStoreFile());
	}

	public String getKeyStorePassword() {
		return SSLKeyStoreOptions.getStorePass();
	}

	public String getKeyStorePrivateKeyPassword() {
		return SSLKeyStoreOptions.getKeyPass();
	}

	public String getKeyStoreLocalAlias() {
		return SSLKeyStoreOptions.getAlias();
	}

	public KeyStoreFormat getKeyStoreFormat() {
		return SSLKeyStoreOptions.getKeyStoreFormat();
	}

	public File getKeyStoreTempLocation() {
		return new File(SSLKeyStoreOptions.getTempCertFolder());
	}



	
	public void setSystemJobManagerConfig(File systemJobManagerConfig) {
		USER_PROPERTIES.setProperty(Settable.SYSTEM_JOBMANAGER_CONFIG.name(), systemJobManagerConfig.toString());
		systemJobManagerConfig = systemJobManagerConfig;
	}

	public void setUserConfig(File userConfig) {
		userConfig = userConfig;
		usingUserConfig = true;
	}

	public void setDebug(boolean debug) {
		USER_PROPERTIES.setProperty(Settable.DEBUG.name(), String.valueOf(debug));
		debug = debug;
	}

	public void setDebugCombox(boolean debugCombox) {
		USER_PROPERTIES.setProperty(Settable.DEBUG_COMBOBOX.name(), String.valueOf(debugCombox));
		debugCombox = debugCombox;
	}

	public void setReserveTimeout(int reserveTimeout) {
		USER_PROPERTIES.setProperty(Settable.RESERVE_TIMEOUT.name(), String.valueOf(reserveTimeout));
		reserveTimeout = reserveTimeout;
	}

	public void setAllocTimeout(int allocTimeout) {
		USER_PROPERTIES.setProperty(Settable.ALLOC_TIMEOUT.name(), String.valueOf(allocTimeout));
		allocTimeout = allocTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		USER_PROPERTIES.setProperty(Settable.CONNECTION_TIMEOUT.name(), String.valueOf(connectionTimeout));
		connectionTimeout = connectionTimeout;
	}

	public void setJobManagerUpdateInterval(int jobManagerUpdateInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_UPDATE_INTERVAL.name(), String.valueOf(jobManagerUpdateInterval));
		jobManagerUpdateInterval = jobManagerUpdateInterval;
	}

	public void setJobManagerSelfRegisterInterval(int jobManagerSelfRegisterInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_SELF_REGISTER_INTERVAL.name(), String.valueOf(jobManagerSelfRegisterInterval));
		jobManagerSelfRegisterInterval = jobManagerSelfRegisterInterval;
	}

	public void setJobManagerDefaultConnector(String jobManagerDefaultConnector) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_DEFAULT_CONNECTOR.name(), String.valueOf(jobManagerDefaultConnector));
		jobManagerDefaultConnector = jobManagerDefaultConnector;
	}

	public void setSearchNodeUnlockTimeout(int searchNodeUnlockTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_UNLOCK_TIMEOUT.name(), String.valueOf(searchNodeUnlockTimeout));
		searchNodeUnlockTimeout = searchNodeUnlockTimeout;
	}

	public void setSearchNodeSearchTimeout(int searchNodeSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_SEARCH_TIMEOUT.name(), String.valueOf(searchNodeSearchTimeout));
		searchNodeSearchTimeout = searchNodeSearchTimeout;
	}

	public void setTFCSearchTimeout(int tfcSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.TFC_SEARCH_TIMEOUT.name(), String.valueOf(tfcSearchTimeout));
		tfcSearchTimeout = tfcSearchTimeout;
	}

	public void setSearchNodeMaxRequests(int searchNodeMaxRequests) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_MAX_REQUESTS.name(), String.valueOf(searchNodeMaxRequests));
		searchNodeMaxRequests = searchNodeMaxRequests;
	}

	public void setSearchNodeExplorationQueueSize(int searchNodeExplorationQueueSize) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_EXPLORATION_QUEUE_SIZE.name(), String.valueOf(searchNodeExplorationQueueSize));
		searchNodeExplorationQueueSize = searchNodeExplorationQueueSize;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_ENCODING.name(), defaultEncoding);
		defaultEncoding = defaultEncoding;
	}

	public void setSelectedEncoding(String selectedEncoding) {
		USER_PROPERTIES.setProperty(Settable.SELECTED_ENCODING.name(), selectedEncoding);
		selectedEncoding = selectedEncoding;
	}

	public void setDefaultProtocol(String defaultProtocol) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_PROTOCOL.name(), defaultProtocol);
		defaultProtocol = defaultProtocol;
	}

	public void setAsyncConstructor(boolean asyncConstructor) {
		USER_PROPERTIES.setProperty(Settable.ASYNC_CONSTRUCTOR.name(), String.valueOf(asyncConstructor));
		asyncConstructor = asyncConstructor;
	}

	public void setActivateJmx(boolean activateJmx) {
		USER_PROPERTIES.setProperty(Settable.ACTIVATE_JMX.name(), String.valueOf(activateJmx));
		activateJmx = activateJmx;
	}

	public void setConnectToPOPcpp(boolean connectToPOPcpp) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_POPCPP.name(), String.valueOf(connectToPOPcpp));
		connectToPOPcpp = connectToPOPcpp;
	}

	public void setConnectToJavaJobmanager(boolean connectToJavaJobmanager) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_JAVA_JOBMANAGER.name(), String.valueOf(connectToJavaJobmanager));
		connectToJavaJobmanager = connectToJavaJobmanager;
	}

	public void setRedirectOutputToRoot(boolean redirectOutputToRoot) {
		USER_PROPERTIES.setProperty(Settable.REDIRECT_OUTPUT_TO_ROOT.name(), String.valueOf(redirectOutputToRoot));
		redirectOutputToRoot = redirectOutputToRoot;
	}

	public void setUseNativeSSHifPossible(boolean useNativeSSHifPossible) {
		USER_PROPERTIES.setProperty(Settable.USE_NATIVE_SSH_IF_POSSIBLE.name(), String.valueOf(useNativeSSHifPossible));
		useNativeSSHifPossible = useNativeSSHifPossible;
	}

	public void setSSLProtocolVersion(String SSLProtocolVersion) {
		USER_PROPERTIES.setProperty(Settable.SSL_PROTOCOL_VERSION.name(), SSLProtocolVersion);
		SSLProtocolVersion = SSLProtocolVersion;
	}

	public void setKeyStoreFile(File file) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FILE.name(), file.toString());
		SSLKeyStoreOptions.setKeyStoreFile(file.toString());
	}

	public void setKeyStorePassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PASSWORD.name(), val);
		SSLKeyStoreOptions.setStorePass(val);
	}

	public void setKeyStorePrivateKeyPassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PRIVATE_KEY_PASSWORD.name(), val);
		SSLKeyStoreOptions.setKeyPass(val);
	}

	public void setKeyStoreLocalAlias(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_LOCAL_ALIAS.name(), val);
		SSLKeyStoreOptions.setAlias(val);
	}

	public void setKeyStoreFormat(KeyStoreFormat val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FORMAT.name(), val.name());
		SSLKeyStoreOptions.setKeyStoreFormat(val);
	}

	public void setKeyStoreTempLocation(File file) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_TEMP_LOCATION.name(), file.toString());
		SSLKeyStoreOptions.setTempCertFolder(file.toString());
	}
	
	public void setKeyStoreOptions(KeyStoreOptions options) {
		SSLKeyStoreOptions = new KeyStoreOptions(options);
	}
	
	
	/**
	 * Load a custom configuration file on top of the system and defaults one.
	 * Hierarchy:
	 *       User
	 *      Machine
	 *   POP Defaults
	 * 
	 * @param file The properties file to load
	 * @throws java.io.IOException 
	 */
	public void load(File file) throws IOException {
		Objects.requireNonNull(file);
		
		// mark as using user config file
		if (!file.equals(SYSTEM_CONFIG)) {
			userConfig = file.getCanonicalFile();
			usingUserConfig = true;
		}
		
		// abort if we can't load
		if (!file.exists()) {
			LogWriter.writeDebugInfo("[Configuration] '%s' doesn't exists or can't be read.", file.getCanonicalPath());
			return;
		}
		
		// merge previous values
		ALL_PROPERTIES.putAll(USER_PROPERTIES);
		
		// load user config and merge with all
		if (usingUserConfig) {
			try (InputStream in = new FileInputStream(file)) {
				USER_PROPERTIES.load(in);
			}
			// override system with user
			ALL_PROPERTIES.putAll(USER_PROPERTIES);
		}
		// load system config
		else {
			try (InputStream in = new FileInputStream(file)) {
				ALL_PROPERTIES.load(in);
			}
		}
		
		// set properties to class values
		for (Object prop : ALL_PROPERTIES.keySet()) {
			if (prop instanceof String) {
				String key = (String) prop;
				String value = ALL_PROPERTIES.getProperty(key);
				
				// get enum
				Settable keyEnum;
				try {
					keyEnum = Settable.valueOf(key.toUpperCase());
				} catch(IllegalArgumentException e) {
					LogWriter.writeDebugInfo("[Configuration] unknown key '%s'", key);
					continue;
				}
				
				try {
					switch(keyEnum) {
						case SYSTEM_JOBMANAGER_CONFIG:           systemJobManagerConfig = new File(value); break;
						case DEBUG:                              debug = Boolean.parseBoolean(value); break;
						case DEBUG_COMBOBOX:                     debugCombox = Boolean.parseBoolean(value); break;
						case RESERVE_TIMEOUT:                    reserveTimeout = Integer.parseInt(value); break;
						case ALLOC_TIMEOUT:                      allocTimeout = Integer.parseInt(value); break;
						case CONNECTION_TIMEOUT:                 connectionTimeout = Integer.parseInt(value); break;
						case JOBMANAGER_UPDATE_INTERVAL:         jobManagerUpdateInterval = Integer.parseInt(value); break;
						case JOBMANAGER_SELF_REGISTER_INTERVAL:  jobManagerSelfRegisterInterval = Integer.parseInt(value); break;
						case JOBMANAGER_DEFAULT_CONNECTOR:       jobManagerDefaultConnector = value; break;
						case SEARCH_NODE_UNLOCK_TIMEOUT:         searchNodeUnlockTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_SEARCH_TIMEOUT:         searchNodeSearchTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_MAX_REQUESTS:           searchNodeMaxRequests = Integer.parseInt(value); break;
						case SEARCH_NODE_EXPLORATION_QUEUE_SIZE: searchNodeExplorationQueueSize = Integer.parseInt(value); break;
						case TFC_SEARCH_TIMEOUT:                 tfcSearchTimeout = Integer.parseInt(value); break;
						case DEFAULT_ENCODING:                   defaultEncoding = value; break;
						case SELECTED_ENCODING:                  selectedEncoding = value; break;
						case DEFAULT_PROTOCOL:                   defaultProtocol = value; break;
						case ASYNC_CONSTRUCTOR:                  asyncConstructor = Boolean.parseBoolean(value); break;
						case ACTIVATE_JMX:                       activateJmx = Boolean.parseBoolean(value); break;
						case CONNECT_TO_POPCPP:                  connectToPOPcpp = Boolean.parseBoolean(value); break;
						case CONNECT_TO_JAVA_JOBMANAGER:         connectToJavaJobmanager = Boolean.parseBoolean(value); break;
						case REDIRECT_OUTPUT_TO_ROOT:            redirectOutputToRoot = Boolean.parseBoolean(value); break;
						case USE_NATIVE_SSH_IF_POSSIBLE:         useNativeSSHifPossible = Boolean.parseBoolean(value); break;
						case SSL_PROTOCOL_VERSION:               SSLProtocolVersion = value; break;
						case SSL_KEY_STORE_FILE:                 SSLKeyStoreOptions.setKeyStoreFile(value); break;
						case SSL_KEY_STORE_PASSWORD:             SSLKeyStoreOptions.setStorePass(value); break;
						case SSL_KEY_STORE_PRIVATE_KEY_PASSWORD: SSLKeyStoreOptions.setKeyPass(value); break;
						case SSL_KEY_STORE_LOCAL_ALIAS:          SSLKeyStoreOptions.setAlias(value); break;
						case SSL_KEY_STORE_FORMAT:               SSLKeyStoreOptions.setKeyStoreFormat(KeyStoreFormat.valueOf(value)); break;
						case SSL_KEY_STORE_TEMP_LOCATION:        SSLKeyStoreOptions.setTempCertFolder(value); break;
					}
				} catch(NumberFormatException e) {
					LogWriter.writeDebugInfo("[Configuration] unknown value '%s' for key '%s'.", value, key);
				}
			}
		}
	}
	
	/**
	 * Save the configuration to a new properties file	 
	 * 
	 * @throws IOException 
	 */
	public void store() throws IOException {
		Objects.requireNonNull(userConfig);
		File file = userConfig;
		
		try (PrintStream out = new PrintStream(file)) {
			USER_PROPERTIES.store(out, "Automatically generated by POP-Java");
		}
	}
}
