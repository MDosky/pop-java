package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.service.jobmanager.connector.POPConnectorDirect;
import popjava.service.jobmanager.connector.POPConnectorTFC;
import popjava.util.Configuration;
import popjava.util.Util;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetworkNodeFactory {
	
	private static final Configuration conf = Configuration.getInstance();
	
	public static POPNetworkNode makeNode(String... other) {
		return makeNode(new ArrayList<>(Arrays.asList(other)));
	}

	public static POPNetworkNode makeNode(List<String> other) {
		String connector = Util.removeStringFromList(other, "connector=");
		// use job manager if nothing is specified
		if (connector == null) {
			connector = conf.getJobManagerDefaultConnector();
		}

		switch (connector.toLowerCase()) {
			case POPConnectorJobManager.IDENTITY: return new NodeJobManager(other);
			case POPConnectorDirect.IDENTITY: return new NodeDirect(other);
			case POPConnectorTFC.IDENTITY: return new NodeTFC(other);
			default: return null;
		}
	}
}
