package popjava.jobmanager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
import popjava.dataswaper.ObjectDescriptionInput;

/**
 * A simple round-robin RA
 *
 * @author Dosky
 */
@POPClass
public class RoundRobinAllocator extends ResourceAllocator {

	private final List<ServiceConnector> services;

	private final AtomicInteger currentHost = new AtomicInteger();

	private final Semaphore await = new Semaphore(0, true);

	@POPObjectDescription(url = "localhost")
	public RoundRobinAllocator() {
		services = new LinkedList<>();
	}

	@Override
	@POPSyncSeq
	public ServiceConnector getNextHost(ObjectDescriptionInput od) {
		if (services.isEmpty()) {
			try {
				await.acquire();
			} catch (InterruptedException ex) {
			}
		}

		// out of bound, go to first service
		if (currentHost.get() >= services.size()) {
			currentHost.set(0);
		}

		// linear allocation
		return services.get(currentHost.getAndIncrement());
	}

	@Override
	@POPSyncConc
	public void registerService(ServiceConnector service) {
		if (services.isEmpty()) {
			await.release();
		}
		services.add(service);
	}

}
