package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import popjava.baseobject.ConnectionType;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.protocol.POPConnectorJobManager;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface POPObjectDescription {
	
	public static final String LOCAL_DEBUG_URL = "localhost-debug";
	
	String url() default "";
	
	/**
	 * JVM parameters to be used when creating this object
	 * @return
	 */
	String jvmParameters() default "";
	
	/**
	 * The type of connection to be used to the remote host if the object
	 * has to be created remotely
	 * @return
	 */
	ConnectionType connection() default ConnectionType.SSH;
	
	String connectionSecret() default "";
	
	Encoding encoding() default Encoding.Default;
	
	/**
	 * A network available on this machine
	 * @return 
	 */
	String network() default POPJavaJobManager.DEFAULT_NETWORK;
	
	/**
	 * An available connector present in a network
	 *  jobmanager: contact remote machine via the jobmanager
	 *  direct: connect directly (ex SSH) to the remote machine
	 * @return 
	 */
	String connector() default POPConnectorJobManager.IDENTITY;
	
	/**
	 * Power requested
	 * @return 
	 */
	float power() default -1;
	/**
	 * Minimum power necessary
	 * @return 
	 */
	float minPower() default -1;
	/**
	 * Memory requested
	 * @return 
	 */
	float memory() default -1;
	/**
	 * Minimum memory necessary
	 * @return 
	 */
	float minMemory() default -1;
	/**
	 * Bandwidth requested
	 * @return 
	 */
	float bandwidth() default -1;
	/**
	 * Minimum bandwidth necessary
	 * @return 
	 */
	float minBandwidth() default -1;
	
	/**
	 * Method id of the constructor.
	 * Only use this if you absolutely know what you are doing.
	 * @return
	 */
	int id() default -1;
}
