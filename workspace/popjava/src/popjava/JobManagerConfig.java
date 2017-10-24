package popjava;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.util.ssl.SSLUtils;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.Resource;
import popjava.service.jobmanager.external.POPNetworkDetails;
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNode;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;
import popjava.util.ssl.KeyStoreCreationOptions;

/**
 * Proxy to Job Manager for user use of the complexity
 *
 * @author Davide Mazzoleni
 */
public class JobManagerConfig {

	private final POPJavaJobManager jobManager;

	public JobManagerConfig() {
		Configuration conf = Configuration.getInstance();
		String protocol = conf.getJobManagerProtocols()[0];
		int port = conf.getJobManagerPorts()[0];
		POPAccessPoint jma = new POPAccessPoint(String.format("%s://%s:%d",
			protocol, POPSystem.getHostIP(), port));
		jobManager = PopJava.newActive(POPJavaJobManager.class, jma);
	}

	/**
	 * Register a POPObject and make it available for discovery in a network
	 *
	 * @param object
	 * @param tfcNetworkUUID
	 * @param secret
	 * @return
	 */
	public boolean publishTFCObject(Object object, String tfcNetworkUUID, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			return jobManager.registerTFCObject(tfcNetworkUUID, temp.getClassName(), temp.getAccessPoint(), secret);
		}
		return false;
	}

	/**
	 * Unregister a POPObject from the local JobManager
	 *
	 * @param object
	 * @param tfcNetworkUUID
	 * @param secret
	 */
	public void withdrawnTFCObject(Object object, String tfcNetworkUUID, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			jobManager.unregisterTFCObject(tfcNetworkUUID, temp.getClassName(), temp.getAccessPoint(), secret);
		}
	}

	/**
	 * Add a new Node/Friend to a network
	 *
	 * @param networksUUID The name of the network
	 * @param node A network node implementation
	 */
	public void registerNode(String networksUUID, POPNode node) {
		jobManager.registerPermanentNode(networksUUID, node.getCreationParams());
	}
	
	/**
	 * Register a new node with a certificate associated to it
	 * 
	 * @param networkUUID Name of the network
	 * @param node The node to add
	 * @param certificate The certificate to use
	 * @return 
	 */
	public boolean registerNode(String networkUUID, POPNode node, Certificate certificate) {
		try {
			SSLUtils.addConfidenceLink(node, certificate, networkUUID);
			jobManager.registerPermanentNode(networkUUID, node.getCreationParams());
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Add a confidence link to a previously added node
	 * 
	 * @param networkUUID Name of the network
	 * @param node The node to add
	 * @param certificate The certificate to use
	 * @return 
	 */
	public boolean assignCertificate(String networkUUID, POPNode node, Certificate certificate) {
		try {
			SSLUtils.addConfidenceLink(node, certificate, networkUUID);
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Add a confidence link to a previously added node
	 * 
	 * @param networkUUID Name of the network
	 * @param node The node to add
	 * @param certificate The certificate to load
	 * @return 
	 */
	public boolean replaceCertificate(String networkUUID, POPNode node, Certificate certificate) {
		try {
			SSLUtils.replaceConfidenceLink(node, certificate, networkUUID);
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Remove a confidence link to a previously added node, preserve the node.
	 * Use {@link #unregisterNode} to remove both node and certificate.
	 * 
	 * @param networkUUID Name of the network
	 * @param node The node to add
	 * @return 
	 */
	public boolean removeCertificate(String networkUUID, POPNode node) {
		try {
			SSLUtils.removeConfidenceLink(node, networkUUID);
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Remove a Node/Friend from a network
	 *
	 * @param networkUUID The name of the network
	 * @param node A network node implementation
	 */
	public void unregisterNode(String networkUUID, POPNode node) {
		jobManager.unregisterPermanentNode(networkUUID, node.getCreationParams());
		// try remove
		try {
			SSLUtils.removeConfidenceLink(node, networkUUID);
		} catch(IOException e) {
			// too bad
		}
	}

	/**
	 * Create a new network of interest, return the details with UUID
	 * 
	 * @param friendlyName 
	 * @return  
	 */
	public POPNetworkDetails createNetwork(String friendlyName) {
		return jobManager.createNetwork(friendlyName);
	}

	/**
	 * Remove a network of interest with all its members
	 * 
	 * @param networkUUID 
	 */
	public void removeNetwork(String networkUUID) {
		jobManager.removeNetwork(networkUUID);
	}

	/**
	 * Change the value of available power on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailablePower(float limit) {
		jobManager.changeAvailablePower(limit);
	}

	/**
	 * Change the value of available memory on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailableMemory(float limit) {
		jobManager.changeAvailableMemory(limit);
	}

	/**
	 * Change the value of available bandwidth on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailableBandwidth(float limit) {
		jobManager.changeAvailableBandwidth(limit);
	}

	/**
	 * Change the maximal number of object that can be create with this job manager
	 * 
	 * @param limit 
	 */
	public void changeMaxJobLimit(int limit) {
		jobManager.changeMaxJobLimit(limit);
	}

	/**
	 * Change the value maximal power an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobPower(float limit) {
		jobManager.changeMaxJobPower(limit);
	}

	/**
	 * Change the value maximal memory an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobMemory(float limit) {
		jobManager.changeMaxJobMemory(limit);
	}

	/**
	 * Change the value maximal bandwidth an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobBandwidth(float limit) {
		jobManager.changeMaxJobBandwidth(limit);
	}
	
	/**
	 * Array of networks available locally
	 * 
	 * @return 
	 */
	public POPNetworkDetails[] availableNetworks() {
		return jobManager.getAvailableNetworks();
	}
	
	/**
	 * All the node available in a network
	 * Use {@link POPNode#getConnectorDescriptor()} to know which type you are working with.
	 * 
	 * @param networkUUID
	 * @return 
	 */
	public POPNode[] networkNodes(String networkUUID) {
		// get nodes
		String[][] networkNodes = jobManager.getNetworkNodes(networkUUID);
		
		// no results
		if (networkNodes == null) {
			return new POPNode[0];
		}
		
		POPNode[] nodes = new POPNode[networkNodes.length];
		// make them real
		for (int i = 0; i < nodes.length; i++) {
			List<String> nodeParams = new ArrayList<>(Arrays.asList(networkNodes[i]));
			String connector = Util.removeStringFromList(nodeParams, "connector=");
			POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(connector);
			if (descriptor != null) {
				nodes[i] = descriptor.createNode(nodeParams);
			}
		}
		
		return nodes;
	}
	
	/**
	 * Generate a KeyStore with private key and certificate.
	 * Proxy for {@link SSLUtils#generateKeyStore(KeyStoreCreationOptions)}
	 * 
	 * @param options
	 * @return 
	 */
	public boolean generateKeyStore(KeyStoreCreationOptions options) {
		return SSLUtils.generateKeyStore(options);
	}
	
	/**
	 * Change configuration file location.
	 * This method will only change the location and try to write in it, it will not delete the old file.
	 * This method is NOT meant to be used to load a new configuration file.
	 * 
	 * @param location The new location of the configuration file.
	 * @throws java.io.IOException If you can't write in the location specified.
	 */
	public void setConfigurationFileLocation(File location) throws IOException {
		if (!location.canWrite()) {
			throw new IOException("Can't write in this location");
		}
		jobManager.setConfigurationFile(location.getAbsolutePath());
	}
	
	
	/**
	 * The initial capacity of the node
	 * 
	 * @return 
	 */
	public Resource getInitialAvailableResources() {
		return jobManager.getInitialAvailableResources();
	}

	/**
	 * The upper limit for each job
	 * 
	 * @return 
	 */
	public Resource getJobResourcesLimit() {
		return jobManager.getInitialAvailableResources();
	}

	/**
	 * The maximum number of simultaneous object available on the JM machine
	 * 
	 * @return 
	 */
	public int getMaxJobs() {
		return jobManager.getMaxJobs();
	}
	
	/**
	 * Don't use this
	 * 
	 * @deprecated To be removed in production
	 */
	@Deprecated
	public void dump() {
		jobManager.dump();
	}
}
