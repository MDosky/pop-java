package popjava.combox.ssl;

import popjava.util.ssl.SSLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.WatchDirectory;

/**
 * Two origin KeyStore TrustManager, single instance with Directory Watch and auto-reload.
 * @see https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 * @author John Calcote
 * @author Davide Mazzoleni
 */
public class POPTrustManager implements X509TrustManager {
	
	private class TemporaryDirectoryWatcher extends WatchDirectory.WatchMethod {
		@Override
		public void create(String file) {
			if (file.endsWith(".cer")) {
				reload();
			}
		}

		@Override
		public void delete(String file) {
			if (file.endsWith(".cer")) {
				reload();
			}
		}
		
		private void reload() {
			try {
				// reload certificates
				reloadTrustManager();
			} catch(Exception e) {}
		}
	}
	
	private class KeyStoreWatcher extends WatchDirectory.WatchMethod {
		private final Path keyStore;
		public KeyStoreWatcher(Path keyStore) {
			this.keyStore = keyStore;
		}
		@Override
		public void modify(String s) {
			// filter to handle only the keystore
			if (keyStore.equals(keyStore.getParent().resolve(s))) {
				reload();
			}
		}
		private void reload() {
			try {
				// reload certificates
				reloadTrustManager();
			} catch(Exception e) {}
		}
	}
	
	private final Configuration conf = Configuration.getInstance();
	
	// certificates stores
	private X509TrustManager trustManager;
	// Map(Fingerprint,Certificate)
	private Map<String,Certificate> loadedCertificates = new HashMap<>();
	// Set(Fingerprints)
	private Set<String> confidenceCertificates = new HashSet<>();
	// Map<Fingerprint,Network>
	private Map<String,String> certificatesNetwork = new HashMap<>();
	
	// reload and add new certificates
	private WatchDirectory temporaryWatcher;
	private WatchDirectory keyStoreWatcher;
	
	// easy access
	private static Certificate publicCertificate;
	private static final POPTrustManager instance;
        
	// static initializations
	static {
		instance = new POPTrustManager();
	}
	
	private POPTrustManager() {
		try {
			reloadTrustManager();
		} catch (Exception ex) {
			LogWriter.writeDebugInfo("[KeyStore] can't initialize TrustManager: %s", ex.getMessage());
		}
	}

	/**
	 * The TrustManager instance 
	 * 
	 * @return 
	 */
	public static POPTrustManager getInstance() {
		return instance;
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("[CHECKING CLIENT CERTIFICATE]");
		for (X509Certificate cert : chain) {
			System.out.println(SSLUtils.certificateFingerprint(cert));
			System.out.println(authType);
		}
		System.out.println("  [ALL CERTS]");
		for (X509Certificate acceptedIssuer : getAcceptedIssuers()) {
			System.out.println("  " + SSLUtils.certificateFingerprint(acceptedIssuer));
		}
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("[CHECKING SERVER CERTIFICATE]");
		for (X509Certificate cert : chain) {
			System.out.println(SSLUtils.certificateFingerprint(cert));
			System.out.println(authType);
		}
		System.out.println("  [ALL CERTS]");
		for (X509Certificate acceptedIssuer : getAcceptedIssuers()) {
			System.out.println("  " + SSLUtils.certificateFingerprint(acceptedIssuer));
		}
		trustManager.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return trustManager.getAcceptedIssuers();
	}
	
	/**
	 * Tell if a certificate is confidence link certificate or a temporary link
	 * 
	 * @param fingerprint The identifier of the certificate
	 * @return true if it's a confidence link, false otherwise
	 */
	public boolean isConfidenceLink(String fingerprint) {
		return confidenceCertificates.contains(fingerprint);
	}
	
	/**
	 * Get the network assigned to a specific certificate
	 * 
	 * @param fingerprint
	 * @return 
	 */
	public String getNetworkFromCertificate(String fingerprint) {
		return certificatesNetwork.get(fingerprint);
	}
	
	/**
	 * Refresh loadedCertificates after a reload of the keystore or of the temp dir
	 */
	private void saveCertificatesToMemory() {
		Map<String,Certificate> temp = new HashMap<>();
		Certificate[] certificates = getAcceptedIssuers();
		for (Certificate cert : certificates) {
			temp.put(SSLUtils.certificateFingerprint(cert), cert);
		}
		// empty and swap
		loadedCertificates.clear();
		loadedCertificates.putAll(temp);
	}

	public final void reloadTrustManager() throws Exception {
		long start = System.currentTimeMillis();
		SSLUtils.invalidateSSLSessions();
		// load keystore from specified cert store (or default)
		KeyStore trustedKS = KeyStore.getInstance(conf.getSSLKeyStoreFormat().name());
		try (InputStream trustedStore = new FileInputStream(conf.getSSLKeyStoreFile())) {
			// load stores in memory
			trustedKS.load(trustedStore, conf.getSSLKeyStorePassword().toCharArray());
		}
		
		// mark certificate in the keystore as confidence certificates
		confidenceCertificates.clear();
		for (Enumeration<String> certAlias = trustedKS.aliases(); certAlias.hasMoreElements();) {
			String alias = certAlias.nextElement();
			Certificate cert = trustedKS.getCertificate(alias);
			String fingerprint = SSLUtils.certificateFingerprint(cert);
			confidenceCertificates.add(fingerprint);
			
			// extract network
			int atLocation = alias.indexOf('@');
			if (atLocation >= 0) {
				String network = alias.substring(atLocation + 1);
				certificatesNetwork.put(fingerprint, network);
			}
			
			// save public certificate
			if (alias.equals(conf.getSSLKeyStoreLocalAlias())) {
				publicCertificate = cert;
			}
		}
		
		// add temporary certificates
		// get all files in directory and add them
		File tempCertDir = conf.getSSLTemporaryCertificateLocation();
		if (tempCertDir != null) {
			if (tempCertDir.exists()) {
				for (File file : tempCertDir.listFiles()) {
					if (file.isFile() && file.getName().endsWith(".cer")) {
						try {
							Certificate cert = SSLUtils.certificateFromBytes(Files.readAllBytes(file.toPath()));
							String alias = file.getName().substring(0, file.getName().length() - 4);
							trustedKS.setCertificateEntry(alias, cert);
						} catch(Exception e) {
						}
					}
				}
			}
			// directory doesn't exists, create it (may have changed)
			else {
				// create temp dir if not found
				Files.createDirectory(tempCertDir.toPath());
			}
			// watch temporaray certificate directory
			if (tempCertDir.canRead()) {
				// stop previous watcher
				boolean createWatcher = true;
				if (temporaryWatcher != null) {
					if (tempCertDir.toPath().equals(temporaryWatcher.getWatchedDir())) {
						createWatcher = false;
					}
					// change of directory
					else {
						temporaryWatcher.stop();
					}
				}

				if (createWatcher) {
					temporaryWatcher = new WatchDirectory(tempCertDir.toPath(), new TemporaryDirectoryWatcher(),
						StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
					Thread dirWatcher = new Thread(temporaryWatcher, "TrustStore temporary folder watcher");
					dirWatcher.setDaemon(true);
					dirWatcher.start();
				}
			}
		}
		
		// watch keystore
		File keyStoreFile = conf.getSSLKeyStoreFile();
		if (keyStoreFile != null && keyStoreFile.canRead()) {
			Path keyStorePath = keyStoreFile.toPath().toAbsolutePath();
			
			// stop previous watcher
			boolean createWatcher = true;
			if (keyStoreWatcher != null) {
				if (keyStorePath.getParent().equals(keyStoreWatcher.getWatchedDir())) {
					createWatcher = false;
				}
				// change of directory
				else {
					keyStoreWatcher.stop();
				}
			}

			if (createWatcher) {
				keyStoreWatcher = new WatchDirectory(keyStorePath.getParent(), new KeyStoreWatcher(keyStorePath), 
					StandardWatchEventKinds.ENTRY_MODIFY);
				Thread keyWatcher = new Thread(keyStoreWatcher, "KeyStore changes watcher");
				keyWatcher.setDaemon(true);
				keyWatcher.start();
			}
		}
		
		// initialize a new TMF with the trustedKS we just loaded
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustedKS);
		
		long end = System.currentTimeMillis();
		LogWriter.writeDebugInfo(String.format("[KeyStore] initiated in %d ms", end - start));

		// acquire X509 trust manager from factory
		TrustManager tms[] = tmf.getTrustManagers();
		for (TrustManager tm : tms) {
			if (tm instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tm;
				saveCertificatesToMemory();
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}
	
	/**
	 * Do we know the certificate
	 * 
	 * @param cert
	 * @return 
	 */
	public boolean isCertificateKnown(Certificate cert) {
		return loadedCertificates.values().contains(cert);
	}
	
	/**
	 * Public certificate from the ones loaded
	 * 
	 * @return 
	 */
	public static Certificate getLocalPublicCertificate() {
		return publicCertificate;
	}
	
	/**
	 * Any certificate from the local Trust manager
	 * 
	 * @param fingerprint
	 * @return 
	 */
	public Certificate getCertificate(String fingerprint) {
		return loadedCertificates.get(fingerprint);
	}
}
