package popjava.system;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import popjava.broker.Broker;
import popjava.codemanager.POPJavaAppService;

public class POPJavaConfiguration {
	
	private static String getConfigurationValue(String value){
		try {
			ConfigurationWorker cw = new ConfigurationWorker();
			return cw.getValue(value);
		} catch (Exception e) {
		}
		
		return null;
	}
	
	public static String getBrokerCommand(){
		String brokerCommand = getConfigurationValue(ConfigurationWorker.POPJ_BROKER_COMMAND_ITEM);
		if(brokerCommand == null){
			brokerCommand = "/usr/bin/java -cp %s "+Broker.class.getName()+" -codelocation=";
		}
		
		return brokerCommand;
	}
	
	/**
	 * Retrieve the POP-C++ AppCoreService executable location
	 * @return string value of the POP-C++ AppCoreService executable location
	 */
	public static String getPopAppCoreService(){
		String appCoreService = getConfigurationValue(ConfigurationWorker.POPC_APPCORESERVICE_ITEM);
		
		if(appCoreService == null){
//			String service = POPSystem
//			.getEnviroment(POPSystem.PopAppCoreServiceEnviromentName);
//	if (service.length() <= 0)
//		return DefaultPopAppCoreService;
//	return service;
			appCoreService = "/usr/local/popc/services/appservice";
		}
		
		return appCoreService;
	}
	
	/**
	 * Retrieve the POP-Java installation location
	 * @return	string value of the POP-java location
	 */
	public static String getPopJavaLocation() {
		String popJavaLocation = getConfigurationValue(ConfigurationWorker.POPJ_LOCATION_ITEM);

		if(popJavaLocation == null){ //Popjava was not actually installed
			popJavaLocation = "";
		}
		
		return popJavaLocation;
	}
	
	/**
	 * Retrieve the POP-Java plugin location
	 * @return string value of the POP-Java plugin location
	 */
	public static String getPopPluginLocation() {
		String popJavaPluginLocation = getConfigurationValue(ConfigurationWorker.POPJ_PLUGIN_ITEM);
		
//		String pluginLocation = POPSystem
//		.getEnviroment(POPSystem.PopPluginLocationEnviromentName);
//if (pluginLocation.length() <= 0) {
//	return DefaultPopPluginLocation;
//}
//return pluginLocation;
		if(popJavaPluginLocation == null){
			popJavaPluginLocation = "";
		}
		
		return popJavaPluginLocation;
	}
	
	public static String getPOPJavaCodePath(){
		String popJar = "";
		URL [] urls = ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs();
		for(int i = 0; i < urls.length; i++){
			URL url = urls[i];
            popJar += url.getPath();
            if(i != urls.length - 1){
            	popJar += ":";
            }
        }
		
		return popJar;
	}
	
	public static String getPopJavaJar(){
		String popJar = "";
		for(URL url: ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs()){
			
            boolean exists = false;
            try{ //WIndows hack
                exists = new File(url.toURI()).exists();
            }catch(Exception e){
                exists = new File(url.getPath()).exists();
            }
            if(exists && url.getFile().endsWith("popjava.jar")){
            	popJar = url.getPath();
            }
        }
		
		if(popJar.isEmpty()){
			for(URL url: ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs()){
				if(url.getPath().endsWith(File.separator)){
					popJar = url.getPath();
					break;
				}
			}
		}
		
		return popJar;
	}

}
