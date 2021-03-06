package popjava;

import java.lang.reflect.Constructor;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.system.POPSystem;
import popjava.util.ClassUtil;
import popjava.util.LogWriter;

/**
 * POP-Java Proxy Factory : this class provide methods to create a proxy factory for a specified class. This class uses the Javassit library.
 * 
 *
 */
public class PJProxyFactory extends ProxyFactory {
	/**
	 * Target class to create a proxy
	 */
	protected Class<?> targetClass;

	/**
	 * Create a new proxy factory for the specified class
	 * @param targetClass : Class to be created by the Factory
	 */
	public PJProxyFactory(Class<?> targetClass) {
		this.targetClass = targetClass;
		
		try{
			targetClass.getConstructor();
		}catch(Exception e){
			throw new POPException(POPErrorCode.UNKNOWN_EXCEPTION, "Class "+targetClass.getName()+" does not have a default constructor");
		}
		
		setSuperclass(targetClass);
		MethodFilter methodFilter = new PJMethodFilter();
		setFilter(methodFilter);
	}

	/**
	 * Create a new object from the factory
	 * @param argvs : arguments to pass trough the constructor of the specific object
	 * @return the instance of the object
	 * @throws POPException
	 */
	public Object newPOPObject(Object... argvs) throws POPException {
		ObjectDescription objectDescription = POPSystem.getDefaultOD();
		return newPOPObject(objectDescription, argvs);
	}

	/**
	 * Create a new object from specific class and object description.
	 * @param od : Object description with the resource requirements
	 * @param argvs : arguments to pass trough the constructor of the specific object
	 * @return the instance of the object
	 * @throws POPException
	 */
	public Object newPOPObject(ObjectDescription od, Object... argvs) throws POPException {
		try {
			POPObject popObject = null;
			//Check if object has a default constructor
			
			try{
				Class<?>[] parameterTypes = ClassUtil.getObjectTypes(argvs);
				Constructor<?> constructor = targetClass.getConstructor(parameterTypes);
				popObject = (POPObject) targetClass.getConstructor().newInstance();
				popObject.loadPOPAnnotations(constructor, argvs);
			}catch(Exception e){
				e.printStackTrace();
				Constructor<?> constructor = targetClass.getConstructor();
				popObject = (POPObject) constructor.newInstance();
				popObject.loadPOPAnnotations(constructor);
			}
			
			ObjectDescription originalOd = popObject.getOd();
			originalOd.merge(od);
			
			if(originalOd.getRemoteAccessPoint() != null && !originalOd.getRemoteAccessPoint().isEmpty()){
				POPAccessPoint accessPoint = new POPAccessPoint();
				accessPoint.setAccessString(originalOd.getRemoteAccessPoint());
				return bindPOPObject(accessPoint);
			}
			
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			methodHandler.setOd(originalOd);
			methodHandler.popConstructor(targetClass, argvs);
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			Object result = c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			return result;
		} catch(POPException e){
		    throw e;
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}
		
		return null;
	}

	/**
	 * Bind an Interface to her parallel object (her associated Broker)
	 * @param accessPoint : The accesspoint of the broker
	 * @return ProxyObject which represent the Interface side
	 * @throws POPException : if anything goes wrong
	 */
	public Object bindPOPObject(POPAccessPoint accessPoint) throws POPException {
		try {
			Constructor<?> constructor = targetClass.getConstructor();
			POPObject popObject = (POPObject) constructor.newInstance();
			popObject.loadPOPAnnotations(constructor);
			
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			methodHandler.bindObject(accessPoint);
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			Object result = c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			return result;
		} catch (Exception e) {
			throw new POPException(0, e.getMessage());
		}
	}

	/**
	 * Recover a parallel object from the buffer
	 * @param buffer : buffer from which the object is recovered 
	 * @return the object recovered
	 * @throws POPException
	 */
	public Object newActiveFromBuffer(POPBuffer buffer) throws POPException {
		POPObject result = null;
		try {
			Constructor<?> constructor = targetClass.getConstructor();
			POPObject popObject = (POPObject) constructor.newInstance();
			popObject.loadPOPAnnotations(constructor);
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			methodHandler.setSetup();
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			result = (POPObject) c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			if (!result.deserialize(buffer)) {
				LogWriter.writeDebugInfo("bad deserialize");
				POPException.throwObjectBindException(methodHandler
						.getAccessPoint());
			}
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}
		
		LogWriter.writeDebugInfo("result");
		return result;
	}
}
